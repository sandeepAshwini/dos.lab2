package server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.BullyElectableFrontend;
import util.BullyElectedBerkeleySynchronized;
import util.LamportClock;
import util.Lottery;
import util.RegistryService;
import util.ServerDetail;
import base.Athlete;
import base.Event;
import base.EventCategories;
import base.NationCategories;
import base.OlympicException;
import base.Results;
import base.Tally;
import client.TabletInterface;

/**
 * Encapsulates the functionality of Obelix.
 * 
 * @author sandeep
 * 
 */
/**
 * @author aravind
 * 
 */
public class Obelix extends BullyElectedBerkeleySynchronized implements
		BullyElectableFrontend, ObelixInterface {

	/**
	 * Various data structures forming Obelix's database for the games.
	 */
	private Set<Event> completedEvents;
	private Map<NationCategories, Tally> medalTallies;
	private Map<EventCategories, ArrayList<Athlete>> scores;

	/**
	 * Data structures to manage event subscriptions.private
	 * Map<EventCategories, ArrayList<Athlete>> scores;
	 */
	private Map<EventCategories, Subscription> subscriptionMap;
	private Map<String, String> subscriberHostMap;

	// To prevent the server from being garbage collected.
	private static Obelix obelixServerInstance;
	private static String OBELIX_SERVICE_NAME = "Obelix";
	private static String ORGETORIX_SERVICE_NAME = "Orgetorix";
	private static String JAVA_RMI_HOSTNAME_PROPERTY = "java.rmi.server.hostname";
	private static int JAVA_RMI_PORT = 1099;
	private static String SERVICE_FINDER_HOST;
	private OrgetorixInterface orgetorixStub;
	private Lottery lottery = new Lottery();
	private boolean lotteryFrozen;
	private Integer localRequestCounter = 0;

	public Obelix(String serviceFinderHost) {
		super(OBELIX_SERVICE_NAME, serviceFinderHost);
		this.completedEvents = new HashSet<Event>();
		this.medalTallies = new HashMap<NationCategories, Tally>();
		this.scores = new HashMap<EventCategories, ArrayList<Athlete>>();
		this.subscriptionMap = new HashMap<EventCategories, Subscription>();
		this.subscriberHostMap = new HashMap<String, String>();
		this.lotteryFrozen = false;

		for (NationCategories nation : NationCategories.values()) {
			this.medalTallies.put(nation, new Tally());
		}
	}

	/**
	 * Sets up the Orgetorix (backend process) client stub by looking up the
	 * address using {@link ServiceFinder}
	 * 
	 * @throws OlympicException
	 */
	private void setupOrgetorixStub() throws OlympicException {
		Registry registry = null;
		try {
			ServerDetail orgetorixDetail = this
					.getServerDetails(ORGETORIX_SERVICE_NAME);
			registry = LocateRegistry.getRegistry(
					orgetorixDetail.getServiceAddress(), JAVA_RMI_PORT);
			OrgetorixInterface orgetorixStub = (OrgetorixInterface) registry
					.lookup(orgetorixDetail.getServerName());
			this.orgetorixStub = orgetorixStub;
		} catch (Exception e) {
			throw new OlympicException("Could not set up Orgetorix Stub.");
		}
	}

	private static Obelix getObelixInstance() {
		if (Obelix.obelixServerInstance == null) {
			Obelix.obelixServerInstance = new Obelix(Obelix.SERVICE_FINDER_HOST);
		}
		return Obelix.obelixServerInstance;
	}

	/**
	 * Remote method to update results and medal tallies of a completed event.
	 * Called by Cacophonix when it receives an update from Games.
	 */
	public void updateResultsAndTallies(Event simulatedEvent)
			throws RemoteException {
		// System.err.println("Received updateResultsAndTallies msg.");
		orgetorixStub.updateResultsAndTallies(simulatedEvent);

	}

	/**
	 * Updates the scores of an on going event. Synchronized as scores are read
	 * to answer client queries.
	 * 
	 * @param eventResult
	 */
	public void updateCurrentScores(EventCategories eventName,
			List<Athlete> currentScores) throws RemoteException {
		// System.err.println("Received updateCurrentScores msg.");
		pushCurrentScores(eventName, currentScores);
		orgetorixStub.updateCurrentScores(eventName, currentScores);
	}

	/**
	 * Pushes new scores to all clients subscribed to the event.
	 * 
	 * @param eventName
	 * @param currentScores
	 */
	private void pushCurrentScores(final EventCategories eventName,
			final List<Athlete> currentScores) throws RemoteException {
		// System.err.println("Pushing current scores.");
		Thread scoreThread = new Thread(new Runnable() {

			@Override
			public void run() {
				sendScoresToSubscribers(eventName, currentScores);
			}
		}, "Score Update Thread");
		scoreThread.start();
	}

	/**
	 * Pushes final results of an event to all it's subscribers.
	 * 
	 * @param completedEvent
	 */
	private void pushResults(final Event completedEvent) {
		// System.err.println("Pushing results.");
		Thread resultThread = new Thread(new Runnable() {

			@Override
			public void run() {
				sendResultsToSubscribers(completedEvent.getName(),
						completedEvent.getResult());
			}
		}, "Result Update Thread");
		resultThread.start();
	}

	/**
	 * Remote function that can be called by clients to get the results of a
	 * completed event.
	 */
	public Results getResults(EventCategories eventName, String clientID) {
		// System.err.println("Sending results for " + eventName + ".");
		try {
			this.notifyEvent(clientID);
			Results result = orgetorixStub.getResults(eventName);
			return result;
		} catch (RemoteException r) {
			return null;
		}

	}

	/**
	 * Remote function that can be called by clients to get the current scores
	 * of an on going event.
	 */
	public List<Athlete> getCurrentScores(EventCategories eventName,
			String clientID) throws RemoteException {
		// System.err.println("Sending current scores for " + eventName + ".");
		try {
			this.notifyEvent(clientID);
			return orgetorixStub.getCurrentScores(eventName);
		} catch (RemoteException r) {
			return null;

		}
	}

	/**
	 * Remote function that can be called by clients to get the medal tally of a
	 * particular team.
	 */
	public Tally getMedalTally(NationCategories teamName, String clientID) {
		// System.err.println("Sending medal tally for " + teamName + ".");
		try {
			this.notifyEvent(clientID);
			return orgetorixStub.getMedalTally(teamName);
		} catch (RemoteException r) {
			return null;
		}
	}

	/**
	 * Remote function that can be called by a client to create a subscription
	 * to a particular event.
	 */
	public void registerClient(String clientID, String clientHost,
			EventCategories eventName) {
		// System.err.println("Registering client " + clientID + ".");
		Subscription subscription = null;

		synchronized (this.subscriptionMap) {
			if (this.subscriptionMap.containsKey(eventName)) {
				subscription = this.subscriptionMap.get(eventName);
			} else {
				subscription = new Subscription();
				subscription.setEventName(eventName);
				this.subscriptionMap.put(eventName, subscription);
			}

			subscription.addSubscriber(clientID);
		}

		synchronized (this.subscriberHostMap) {
			this.subscriberHostMap.put(clientID, clientHost);
		}

		for (Event completedEvent : completedEvents) {
			if (completedEvent.getName() == eventName) {
				pushResults(completedEvent);
				return;
			}
		}
	}

	/**
	 * Pushes new scores of an event to all subscribers of that event. Also
	 * measures the average push latency across all subscribers for each update
	 * set.
	 * 
	 * @param eventName
	 * @param currentScores
	 * @throws NotBoundException
	 * @throws RemoteException
	 */
	private void sendScoresToSubscribers(EventCategories eventName,
			List<Athlete> currentScores) {
		Subscription subscription = null;

		synchronized (this.subscriptionMap) {
			subscription = this.subscriptionMap.get(eventName);
		}

		if (subscription == null) {
			return;
		}

		long startTime = System.currentTimeMillis();
		synchronized (this.subscriptionMap) {
			for (String subscriber : subscription.getSubscribers()) {
				TabletInterface tabletStub;
				try {
					tabletStub = setupObelixClient(subscriber);
					tabletStub.updateScores(eventName, currentScores);
				} catch (NotBoundException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		long duration = System.currentTimeMillis() - startTime;
		System.out.println("Average push latency: "
				+ (duration / subscription.getSubscribers().size()));
	}

	/**
	 * Helper function to setup Obelix client that is used to push score updates
	 * and results to subscribers.
	 * 
	 * @param subscriber
	 * @return TabletInterface
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	private TabletInterface setupObelixClient(String subscriber)
			throws RemoteException, NotBoundException {
		Registry registry = null;
		synchronized (this.subscriberHostMap) {
			registry = LocateRegistry.getRegistry(
					this.subscriberHostMap.get(subscriber), JAVA_RMI_PORT);
			TabletInterface tabletStub = (TabletInterface) registry
					.lookup(subscriber);
			return tabletStub;
		}
	}

	/**
	 * Sends final results of an event to all subscribers of that event.
	 * 
	 * @param eventName
	 * @param result
	 */
	private void sendResultsToSubscribers(EventCategories eventName,
			Results result) {
		Subscription subscription = null;

		synchronized (this.subscriptionMap) {
			subscription = this.subscriptionMap.remove(eventName);
		}

		if (subscription == null) {
			return;
		}

		synchronized (this.subscriberHostMap) {
			for (String subscriber : subscription.getSubscribers()) {
				TabletInterface tabletStub;
				try {
					tabletStub = setupObelixClient(subscriber);
					tabletStub.updateResults(eventName, result);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Helper function to setup the server at Obelix and register with
	 * {@link ServiceFinder}.
	 * 
	 * @param regService
	 * @throws IOException
	 * @throws OlympicException
	 */
	private void setupObelixServer(RegistryService regService)
			throws IOException, OlympicException {
		Registry registry = null;

		this.register(OBELIX_SERVICE_NAME, regService.getLocalIPAddress());
		ObelixInterface serverStub = (ObelixInterface) UnicastRemoteObject
				.exportObject(Obelix.getObelixInstance(), 0);
		try {
			registry = LocateRegistry.getRegistry(JAVA_RMI_PORT);
			registry.rebind(this.getServerName(), serverStub);
			System.err.println("Registry Service running at "
					+ regService.getLocalIPAddress() + ".");
			System.err.println("Obelix ready.");
		} catch (RemoteException e) {
			regService.setupLocalRegistry();
			registry = LocateRegistry.getRegistry(JAVA_RMI_PORT);
			registry.rebind(this.getServerName(), serverStub);
			System.err.println("New Registry Service created. Obelix ready.");
		}
	}

	/**
	 * Sets up Obelix's client and server stubs so it may perform it's function
	 * of servicing client requests and registering updates from Cacophonix.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws OlympicException {
		// Bind the remote object's stub in the registry
		Obelix.SERVICE_FINDER_HOST = (args.length < 1) ? null : args[0];
		final Obelix obelixInstance = Obelix.getObelixInstance();
		try {
			RegistryService regService = new RegistryService();
			System.setProperty(JAVA_RMI_HOSTNAME_PROPERTY,
					regService.getLocalIPAddress());
			obelixInstance.setupObelixServer(regService);
			obelixInstance.setupOrgetorixStub();
			obelixInstance.initiateElection();
		} catch (IOException e) {
			throw new OlympicException(
					"Registry Service could not be created.", e);
		}

	}

	/**
	 * Sets up a client stub of type BullyElectableFrontend.
	 * 
	 * @param participant
	 * @throws RemoteException
	 */
	public BullyElectableFrontend getBullyElectableFrontendClientStub(
			ServerDetail participant) throws RemoteException {
		Registry registry = null;
		BullyElectableFrontend client = null;
		registry = LocateRegistry.getRegistry(participant.getServiceAddress(),
				JAVA_RMI_PORT);
		try {
			client = (BullyElectableFrontend) registry.lookup(participant
					.getServerName());
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return client;
	}

	/**
	 * Notifies the occurrence of a new event by synchronizing current process
	 * timestamp with other processes. Each new request received counts as a new
	 * event.
	 * 
	 * @param participantID
	 * @throws RemoteException
	 */
	private void notifyEvent(String participantID) throws RemoteException {
		long timestampValue = this.syncServers();		
		if(!lotteryFrozen) {
			synchronized(this.localRequestCounter) {
				localRequestCounter++;
			}
			if (timestampValue % lottery.lotteryEnterFrequency == 0) {			
				System.out.println("Entering " + participantID + " into lottery.");
				this.addParticipant(participantID);
				syncParticipants(participantID);
			}
		}
	}

	private void syncParticipants(String participantID) throws RemoteException {
		List<ServerDetail> participants = findAllParticipants(OBELIX_SERVICE_NAME);
		for (ServerDetail participant : participants) {
			if (participant.getPID() == this.PID) {
				continue;
			}

			BullyElectableFrontend clientStub = getBullyElectableFrontendClientStub(participant);
			clientStub.addParticipant(participantID);
		}
	}

	public void addParticipant(String participantID) throws RemoteException {
		synchronized (this.lottery) {
			this.lottery.addParticipant(participantID);
		}
	}

	/**
	 * Implements totally-ordered multicasting. Multicasts current process'
	 * timestamp and waits for updated timestamps from all processes.
	 * 
	 * @return Update timestamp for current process
	 * @throws RemoteException
	 */

	private synchronized long syncServers() throws RemoteException {
		this.timeStamp.tick();
		List<ServerDetail> participants = findAllParticipants(OBELIX_SERVICE_NAME);
		List<LamportClock> lamportClocks = new ArrayList<LamportClock>();
		for (ServerDetail participant : participants) {
			if (participant.getPID() == this.PID) {
				continue;
			}
			BullyElectableFrontend clientStub = getBullyElectableFrontendClientStub(participant);
			lamportClocks.add(clientStub.notifyTimeStamp(this.timeStamp));
		}
		for (LamportClock incomingClock : lamportClocks) {
			this.timeStamp.synchronizeTime(incomingClock);
		}

		return this.timeStamp.getTime();
	}

	/**
	 * Utility function that initiates the lottery draw.
	 * 
	 * @return clientID of the winner of the lottery.
	 * @throws RemoteException
	 */
	@Override
	public String conductLottery() throws RemoteException {
		List<ServerDetail> participants = findAllParticipants(OBELIX_SERVICE_NAME);
		for (ServerDetail participant : participants) {
			BullyElectableFrontend clientStub = getBullyElectableFrontendClientStub(participant);
			clientStub.freezeLottery();
		}
		synchronized (this.lottery) {
			return this.lottery.conductDraw();
		}
	}

	/**
	 * Evaluation function to print the load statistics.
	 */
	@Override
	public List<Double> getLoadStatistics() throws RemoteException {
		List<Double> loadFactors = new ArrayList<Double>();
		List<Double> loads = new ArrayList<Double>();
		double totalLoad = 0.0;
		List<ServerDetail> participants = findAllParticipants(OBELIX_SERVICE_NAME);
		for (ServerDetail participant : participants) {
			int load = 0;
			if (participant.getPID() == this.PID) {
				load = this.localRequestCounter;
			}
			BullyElectableFrontend clientStub = getBullyElectableFrontendClientStub(participant);
			load = clientStub.getRequestCount();
			loads.add(new Double(load));
			totalLoad += load;
		}

		for (Double load : loads) {
			loadFactors.add(load / totalLoad);
		}

		return loadFactors;
	}

	/**
	 * Set lottery enter frequency to specified value.
	 * 
	 * @param lotteryEnterFrequency
	 */
	@Override
	public void setLotteryEnterFrequency(int lotteryEnterFrequency)
			throws RemoteException {
		List<ServerDetail> participants = findAllParticipants(OBELIX_SERVICE_NAME);
		for (ServerDetail participant : participants) {
			BullyElectableFrontend clientStub = getBullyElectableFrontendClientStub(participant);
			clientStub
					.setLotteryEnterFrequency(this.PID, lotteryEnterFrequency);
		}
	}

	@Override
	public void setLotteryEnterFrequency(Integer PID, int lotteryEnterFrequency)
			throws RemoteException {
		lottery.lotteryEnterFrequency = lotteryEnterFrequency;
	}

	/**
	 * Multicasts the current timestamp to other processes.
	 * 
	 * @return Updated timestamp of the current process.
	 */

	@Override
	public LamportClock notifyTimeStamp(LamportClock incomingTimeStamp)
			throws RemoteException {
		this.timeStamp.synchronizeTime(incomingTimeStamp);
		return this.timeStamp;
	}

	@Override
	public int getRequestCount() throws RemoteException {
		return this.localRequestCounter;
	}

	@Override
	public void freezeLottery() throws RemoteException {
		this.lotteryFrozen = true;
	}
}