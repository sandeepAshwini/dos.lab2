package server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.util.UUID;

import util.RegistryService;
import base.Athlete;
import base.Event;
import base.EventCategories;
import base.MedalCategories;
import base.NationCategories;
import base.OlympicException;
import base.Results;
import base.Tally;

public class Orgetorix implements OrgetorixInterface {
	private static String JAVA_RMI_HOSTNAME_PROPERTY = "java.rmi.server.hostname";
	private static int JAVA_RMI_PORT = 1099;
	public static String FILE_LOCATION = "./";

	private static Orgetorix orgetorixServerInstance;
	private String resultFileName;
	private String tallyFileName;
	private String scoreFileName;
	private String dbName;

	public Orgetorix() {
		this.dbName = UUID.randomUUID().toString();
		this.resultFileName = FILE_LOCATION + "Results" + this.dbName;
		this.tallyFileName = FILE_LOCATION + "Tallies" + this.dbName;
		this.scoreFileName = FILE_LOCATION + "Scores" + this.dbName;
		this.initializeDatabase();
	}

	private void initializeDatabase() {
		this.writeToDatabase(new HashSet<Event>(), this.resultFileName);
		Map<NationCategories, Tally> medalTallies = new HashMap<NationCategories, Tally>();
		for (NationCategories nation : NationCategories.values()) {
			medalTallies.put(nation, new Tally());
		}
		this.writeToDatabase(medalTallies, this.tallyFileName);
		Map<EventCategories, ArrayList<Athlete>> scores = new HashMap<EventCategories, ArrayList<Athlete>>();
		for (EventCategories event : EventCategories.values()) {
			scores.put(event, new ArrayList<Athlete>());
		}
		this.writeToDatabase(scores, this.scoreFileName);
	}

	@Override
	public void updateResultsAndTallies(Event simulatedEvent)
			throws RemoteException {
		updateResults(simulatedEvent);
		updateMedalTallies(simulatedEvent.getResult());
	}

	private void updateResults(Event completedEvent) {
		Set<Event> completedEvents = readResultFile();
		completedEvents.add(completedEvent);
		writeToDatabase(completedEvents, this.resultFileName);
	}

	private void updateMedalTallies(Results eventResult) {
		Map<NationCategories, Tally> medalTallies = readTallyFile();
		for (MedalCategories medalType : MedalCategories.values()) {
			medalTallies.get(eventResult.getTeam(medalType)).incrementTally(
					medalType);
		}
		writeToDatabase(medalTallies, this.tallyFileName);
	}

	@Override
	public void updateCurrentScores(EventCategories eventType,
			List<Athlete> currentScores) throws RemoteException {
		Map<EventCategories, ArrayList<Athlete>> scores = readScoreFile();
		scores.put(eventType, (ArrayList<Athlete>) currentScores);
		writeToDatabase(scores, this.scoreFileName);
	}

	@Override
	public Tally getMedalTally(NationCategories teamName)
			throws RemoteException {
		Map<NationCategories, Tally> medalTallies = readTallyFile();
		return medalTallies.get(teamName);

	}

	@Override
	public Results getResults(EventCategories eventName) throws RemoteException {
		Set<Event> completedEvents = readResultFile();
		for (Event event : completedEvents) {
			if (event.getName() == eventName) {
				return event.getResult();
			}
		}
		return null;
	}

	@Override
	public List<Athlete> getCurrentScores(EventCategories eventName)
			throws RemoteException {
		Map<EventCategories, ArrayList<Athlete>> scores = readScoreFile();
		return scores.get(eventName);
	}

	private Set<Event> readResultFile() {
		Set<Event> completedEvents = (Set<Event>) readFromDatabase(this.resultFileName);
		return completedEvents;
	}

	private Map<NationCategories, Tally> readTallyFile() {
		Map<NationCategories, Tally> medalTallies = (Map<NationCategories, Tally>) readFromDatabase(this.tallyFileName);
		return medalTallies;
	}

	private Map<EventCategories, ArrayList<Athlete>> readScoreFile() {
		Map<EventCategories, ArrayList<Athlete>> scores = (Map<EventCategories, ArrayList<Athlete>>) readFromDatabase(this.scoreFileName);
		return scores;
	}

	private synchronized void writeToDatabase(Object object, String filename) {
		try {
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.flush();
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	private synchronized Object readFromDatabase(String filename) {
		Object object = null;
		try {
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			object = in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
		}
		return object;
	}

	private static Orgetorix getOrgetorixInstance() {
		if (Orgetorix.orgetorixServerInstance == null) {
			Orgetorix.orgetorixServerInstance = new Orgetorix();
		}
		return Orgetorix.orgetorixServerInstance;
	}

	private void setupOrgetorixServer(RegistryService regService)
			throws IOException, OlympicException {
		Registry registry = null;
		String SERVER_NAME = "Orgetorix";

		OrgetorixInterface serverStub = (OrgetorixInterface) UnicastRemoteObject
				.exportObject(Orgetorix.getOrgetorixInstance(), 0);
		try {
			registry = LocateRegistry.getRegistry(JAVA_RMI_PORT);
			registry.rebind(SERVER_NAME, serverStub);
			System.err.println("Registry Service running at "
					+ regService.getLocalIPAddress() + ".");
			System.err.println("Orgetorix ready.");
		} catch (RemoteException e) {
			regService.setupLocalRegistry();
			registry = LocateRegistry.getRegistry(JAVA_RMI_PORT);
			registry.rebind(SERVER_NAME, serverStub);
			System.err
					.println("New Registry Service created. Orgetorix ready.");
		}
	}

	public static void main(String[] args) throws OlympicException {
		// Bind the remote object's stub in the registry
		Orgetorix orgetorixInstance = Orgetorix.getOrgetorixInstance();

		try {
			RegistryService regService = new RegistryService();
			System.setProperty(JAVA_RMI_HOSTNAME_PROPERTY,
					regService.getLocalIPAddress());
			orgetorixInstance.setupOrgetorixServer(regService);
		} catch (IOException e) {
			throw new OlympicException(
					"Registry Service could not be created.", e);
		}
	}
}
