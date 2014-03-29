package server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import util.RegistryService;
import util.ServerDetail;
import util.ServiceComponent;
import base.Athlete;
import base.Event;
import base.OlympicException;

/**
 * Encapsulates the functions of Cacophonix.
 * Receives updates from the Olympic Games and relays them on to 
 * Obelix by singing.
 * @author sandeep
 *
 */
/**
 * @author aravind
 * 
 */
public class Cacophonix extends ServiceComponent implements CacophonixInterface {

	private static String CACOPHONIX_SERVICE_NAME = "Cacophonix";
	private static String OBELIX_SERVER_NAME = "Obelix";
	private static String JAVA_RMI_HOSTNAME_PROPERTY = "java.rmi.server.hostname";
	private static int JAVA_RMI_PORT = 1099;
	private static String SERVICE_FINDER_HOST;

	// To prevent the server from being garbage collected.
	private static Cacophonix cacophonixServerInstance;

	private ObelixInterface clientStub;

	public Cacophonix(String serviceFinderHost) {
		super(CACOPHONIX_SERVICE_NAME, serviceFinderHost);
	}

	public Cacophonix(ObelixInterface clientStub, String serviceFinderHost) {
		super(CACOPHONIX_SERVICE_NAME, serviceFinderHost);
		this.clientStub = clientStub;
	}

	private static Cacophonix getCacophonixInstance() {
		if (Cacophonix.cacophonixServerInstance == null) {
			Cacophonix.cacophonixServerInstance = new Cacophonix(
					Cacophonix.SERVICE_FINDER_HOST);
		}
		return Cacophonix.cacophonixServerInstance;
	}

	/**
	 * Remote method called by Games when there is an update to the results of
	 * any event, that is when the event is completed. This in turn causes these
	 * results to be relayed on to Obelix, whose database is accordingly
	 * updated.
	 */
	public void updateResultsAndTallies(Event simulatedEvent)
			throws RemoteException {
		System.err.println("Sending updateResultsAndTallies msg.");
		if (clientStub != null) {
			clientStub.updateResultsAndTallies(simulatedEvent);
		}
	}

	/**
	 * Remote method called by Games when there is an update to scores in some
	 * event. The updates are relayed on to Obelix whose database is accordingly
	 * updated.
	 */
	public void updateCurrentScores(Event simulatedEvent,
			List<Athlete> currentScores) throws RemoteException {
		System.err.println("Sending updatedCurrentScores msg.");
		if (clientStub != null) {
			clientStub.updateCurrentScores(simulatedEvent.getName(),
					currentScores);
		}
	}

	/**
	 * Sets up client and server functions of Cacophonix.
	 * 
	 * @param args
	 * @throws OlympicException
	 */
	public static void main(String args[]) throws OlympicException {
		Cacophonix.SERVICE_FINDER_HOST = (args.length < 1) ? null : args[0];
		Cacophonix cacophonixInstance = Cacophonix.getCacophonixInstance();
		cacophonixInstance.setupClientInstance();
		try {
			RegistryService regService = new RegistryService();
			System.setProperty(JAVA_RMI_HOSTNAME_PROPERTY,
					regService.getLocalIPAddress());
			cacophonixInstance.setupServerInstance(regService);
		} catch (IOException e) {
			throw new OlympicException("Could not create Registry.", e);
		}
	}

	/**
	 * Sets up Cacophonix server and registers with {@link ServiceFinder} which
	 * is used by Games updates scores and results.
	 * 
	 * @param clientStub
	 * @throws IOException
	 *             , OlympicException
	 */
	private void setupServerInstance(RegistryService regService)
			throws IOException, OlympicException {
		Registry registry = null;
		CacophonixInterface serverStub = (CacophonixInterface) UnicastRemoteObject
				.exportObject(Cacophonix.getCacophonixInstance(), 0);
		this.register(CACOPHONIX_SERVICE_NAME, regService.getLocalIPAddress());
		try {
			registry = LocateRegistry.getRegistry(JAVA_RMI_PORT);
			registry.rebind(this.getServerName(), serverStub);
			System.err.println("Registry Service running at "
					+ regService.getLocalIPAddress() + ".");
			System.err.println("Cacophonix ready.");
		} catch (RemoteException e) {
			regService.setupLocalRegistry();
			registry = LocateRegistry.getRegistry(JAVA_RMI_PORT);
			registry.rebind(this.getServerName(), serverStub);
			System.err
					.println("New Registry Service created. Cacophonix ready.");
		}
	}

	/**
	 * Sets up the Obelix client stub by looking up {@link ServiceFinder}. This
	 * allows Cacophonix to 'sing' any received updates and hence inform Obelix
	 * of the same.
	 * 
	 * @return ObelixInterface
	 */
	private ObelixInterface setupClientInstance() {
		Registry registry = null;
		ObelixInterface clientStub = null;
		try {
			ServerDetail obelixDetail = this
					.getServerDetails(OBELIX_SERVER_NAME);
			registry = LocateRegistry.getRegistry(
					obelixDetail.getServiceAddress(), JAVA_RMI_PORT);
			clientStub = (ObelixInterface) registry.lookup(obelixDetail
					.getServerName());
			Cacophonix.getCacophonixInstance().clientStub = clientStub;
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return clientStub;
	}

	/**
	 * Propagates the conductLottery message to Obelix.
	 */
	@Override
	public String conductLottery() throws RemoteException {
		return clientStub.conductLottery();
	}
}
