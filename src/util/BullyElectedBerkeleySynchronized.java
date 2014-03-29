package util;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates an implementation for a Bully electable and Berkeley
 * Synchronizable server process.
 * 
 * @author aravind
 */
public abstract class BullyElectedBerkeleySynchronized extends ServiceComponent
		implements BullyElectable, BerkeleySynchronizable {

	public long clockOffset = 0;
	// protected VectorClock timeStamp;
	protected LamportClock timeStamp;
	private String timeServerName;
	private static String[] serviceNames = { "Obelix", "Orgetorix" };
	private static int JAVA_RMI_PORT = 1099;
	private volatile boolean electionEnded = false;
	private static int INITIATOR_ID = -1;

	public BullyElectedBerkeleySynchronized(String serviceName,
			String serviceFinderHost) {
		super(serviceName, serviceFinderHost);
		// this.timeStamp = new VectorClock(this.PID);
		this.timeStamp = new LamportClock();
	}

	public void initiateElection() {
		try {
			startElection(INITIATOR_ID);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets up a client stub of type BullyElectable.
	 * 
	 * @param participant
	 * @throws RemoteException
	 */
	public BullyElectable getBullyElectableClientStub(ServerDetail participant)
			throws RemoteException {
		Registry registry = null;
		BullyElectable client = null;
		registry = LocateRegistry.getRegistry(participant.getServiceAddress(),
				JAVA_RMI_PORT);
		try {
			client = (BullyElectable) registry.lookup(participant
					.getServerName());
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return client;
	}

	/**
	 * Sets up a client stub of type BerkeleySynchronizable.
	 * 
	 * @param participant
	 * @throws RemoteException
	 */
	BerkeleySynchronizable getBerkeleySynchronizableClientStub(
			ServerDetail participant) throws RemoteException {
		Registry registry = null;
		BerkeleySynchronizable client = null;
		registry = LocateRegistry.getRegistry(participant.getServiceAddress(),
				JAVA_RMI_PORT);
		try {
			client = (BerkeleySynchronizable) registry.lookup(participant
					.getServerName());
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return client;
	}

	/**
	 * Retrieves all Obelix and Orgetorix servers currently registered with
	 * {@link ServiceFinder}.
	 * 
	 * @return Server details for all Obelix and Orgetorix servers currently
	 *         registered with {@link ServiceFinder}.
	 * @throws RemoteException
	 */
	public List<ServerDetail> findAllParticipants() throws RemoteException {
		List<ServerDetail> participants = new ArrayList<ServerDetail>();
		for (String serviceName : serviceNames) {
			participants.addAll(findAllParticipants(serviceName));
		}
		return participants;
	}

	/**
	 * Retrieves all servers offering a specified service and registered with
	 * {@link ServiceFinder}.
	 * 
	 * @param serviceName
	 * @return Server details for all servers offering the service serviceName.
	 * @throws RemoteException
	 */
	public List<ServerDetail> findAllParticipants(String serviceName)
			throws RemoteException {
		List<ServerDetail> participants = new ArrayList<ServerDetail>();
		participants.addAll(getServersDetails(serviceName));
		return participants;
	}

	/**
	 * Utility function to broadcast victory message at the end of the election.
	 * Sets the current timeserver to the specified server name.
	 * 
	 * @param callerServerName
	 */
	@Override
	public void notifyVictory(String callerServerName) throws RemoteException {
		this.timeServerName = callerServerName;
		System.out.println("Elected time server: " + this.timeServerName);
	}

	/**
	 * Implements leader election based on the Bully Election algorithm.
	 * 
	 * @param callerPID
	 */
	@Override
	public void startElection(int callerPID) throws RemoteException {
		List<ServerDetail> participants = findAllParticipants();
		boolean foundParticipants = false;
		for (ServerDetail participant : participants) {
			System.out.println(participant.getServerName());
			if (!participant.getServerName().equals(this.getServerName())) {
				if (participant.getPID() == callerPID) {
					System.out.println(callerPID);
					BullyElectable clientStub = getBullyElectableClientStub(participant);
					if (clientStub != null) {
						System.out.println("Sending notifyAlive msg to "
								+ participant.getServerName() + ".");
						clientStub.notifyAlive();
					}
				} else if (participant.getPID() > this.PID) {
					BullyElectable clientStub = getBullyElectableClientStub(participant);
					if (clientStub != null) {
						System.out.println("Sending startElection msg to "
								+ participant.getServerName() + ".");
						clientStub.startElection(this.PID);
					}
					foundParticipants = true;
				}
			}
		}
		if (foundParticipants) {
			waitForEndOfElection();
		} else {
			this.notifyVictory(this.getServerName());
			Thread thread = new Thread(new BerkeleySynchronizer(this));
			thread.start();
			for (ServerDetail participant : participants) {
				if (!participant.getServerName().equals(this.getServerName())) {
					BullyElectable clientStub = getBullyElectableClientStub(participant);
					if (clientStub != null) {
						System.out.println("Sending notifyVictory msg to "
								+ participant.getServerName() + ".");
						clientStub.notifyVictory(this.getServerName());
					}
				}
			}
		}
	}

	/**
	 * Utility function to wait for end of election.
	 */
	private void waitForEndOfElection() {
		while (!this.electionEnded)
			;
		this.electionEnded = false;
	}

	/**
	 * Utility function to send 'Alive' message, that causes current process to
	 * stop waiting.
	 */
	@Override
	public void notifyAlive() throws RemoteException {
		this.electionEnded = true;
	}

	/**
	 * Retreives the current local time.
	 */
	@Override
	public long getTime() throws RemoteException {
		return (System.currentTimeMillis() + clockOffset);
	}

	/**
	 * Utility function to send clock offsets from time server to other
	 * processes.
	 */
	@Override
	public void setClockOffset(long clockOffset) throws RemoteException {
		this.clockOffset = clockOffset;
	}

	/**
	 * Utility function to check if current process is the time server.
	 * 
	 * @return If current process is the elected time server, true otherwise
	 *         false.
	 */
	public boolean isElectedTimeServer() {
		return this.timeServerName.equals(this.getServerName());
	}
}

/**
 * Encapsulates the functionality of the Berkeley Synchronization algorithm.
 * 
 * @author aravind
 * 
 */
class BerkeleySynchronizer implements Runnable {

	private BullyElectedBerkeleySynchronized timeServer;
	private static int CLOCK_SYNC_INTERVAL = 30000;

	public BerkeleySynchronizer(BullyElectedBerkeleySynchronized timeServer) {
		this.timeServer = timeServer;
	}

	public void setIsTimeServer(BullyElectedBerkeleySynchronized timeServer) {
		this.timeServer = timeServer;
	}

	/**
	 * Periodically polls other processes for their local time, averages
	 * (accounting for transmission delay as one half of Round Trip Time) and
	 * sends out clock offset messages to correct local time of other processes.
	 */
	@Override
	public void run() {
		while (true) {
			if (this.timeServer.isElectedTimeServer()) {
				try {
					List<ServerDetail> participants = this.timeServer
							.findAllParticipants();
					List<Long> participantTimes = new ArrayList<Long>();
					double average = 0.0;
					for (ServerDetail participant : participants) {
						long startTime = System.currentTimeMillis();
						BerkeleySynchronizable clientStub = timeServer
								.getBerkeleySynchronizableClientStub(participant);
						if (clientStub != null) {
							System.out.println("Sending getTime msg to "
									+ participant.getServerName() + ".");
							long participantTime = clientStub.getTime();
							participantTimes.add(participantTime);
							average += participantTime;
						}
						long RTT = (System.currentTimeMillis() - startTime);
						average += RTT / 2;
					}
					average /= participants.size();
					int participantIndex = 0;
					for (ServerDetail participant : participants) {
						BerkeleySynchronizable clientStub = timeServer
								.getBerkeleySynchronizableClientStub(participant);
						if (clientStub != null) {
							long clockOffset = (long) average
									- participantTimes.get(participantIndex++);
							System.out.println("Sending setClockOffset msg to "
									+ participant.getServerName()
									+ ". Clock offset: " + clockOffset);
							clientStub.setClockOffset(clockOffset);
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(CLOCK_SYNC_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}