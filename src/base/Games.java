package base;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import server.CacophonixInterface;

/**
 * Class encapsulates the entire Olympic Games.
 * @author sandeep
 *
 */

public class Games {
	
	private static int JAVA_RMI_PORT = 1099;
	
	/**
	 * Game attributes.
	 */
	private ArrayList<Event> events;
	private String venue;
	private String year;
	private int currentEvent;
	
	public Games(String venue, String year) {
		this.events = new ArrayList<Event>();
		this.year = year;
		this.venue = venue;
		currentEvent = 0;

		for(EventCategories eventName : EventCategories.values()) {
			this.events.add(new Event(eventName));
		}
	}
	
	/**
	 * General getters.
	 */
	public String getGameVenue() {
		return this.venue;
	}
	
	public String getGameYear() {
		return this.year;
	}
	
	public ArrayList<Event> getEvents() {
		return this.events;
	}

	public Event getNextEvent() {
		return this.events.get(this.currentEvent);
	}

	/**
	 * Prints a welcome message.
	 */
	public void printGameIntro() {
		System.out.println("Welcome to the Stone Olympics of " + this.year + " at " + this.venue + ".");
	}

	
	/**
	 * Gets the next event and simulates it on a new thread.
	 * @return Event
	 */
	public Event simulateNextEvent() {
		Event currentEvent = events.get(this.currentEvent++);
		Thread thread = new Thread(currentEvent);
		thread.start();
		return currentEvent;
	}
	
	/**
	 * The Game class acts as a server for Cacophonix.
	 * The main method creates a new Game object and simulates the games.
	 * Events are simulated one after the other with a pre defined break between them.
	 * Scores of the current event are updated every 5 seconds by 
	 * sending a message to Cacophonix.
	 * Once an event is completed, the Results and Medal Tallies are 
	 * updated by sending a message to Cacophonix.
	 * @param args
	 * @throws OlympicException
	 */
	public static void main(String[] args) throws OlympicException {
		long TIME_DELAY = 20*1000;
		long SLEEP_DURATION = (long) 5.1*1000;
		Games game = new Games("Pompeii", "48 BC");
		int numEvents = game.events.size();
		String cacophonixHost = (args.length < 1) ? null : args[0];
		String CACOPHONIX_SERVER_NAME = "Cacophonix";
		
		game.printGameIntro();
		try {
			Registry registry = LocateRegistry.getRegistry(cacophonixHost, JAVA_RMI_PORT);
            CacophonixInterface stub = (CacophonixInterface) registry.lookup(CACOPHONIX_SERVER_NAME);
            for(int i = 0; i < numEvents; i++)
    		{
        		Event simulatedEvent = game.simulateNextEvent();
        		System.err.println("Beginning " + simulatedEvent.getName().getCategory() + ".");
    			if(simulatedEvent != null)
    			{
    				while(!simulatedEvent.isCompleted())
    				{
    					System.err.println("Sending latest scores.");
    					ArrayList<Athlete> currentScores = simulatedEvent.getScores();
    					stub.updateCurrentScores(simulatedEvent, currentScores);
    					Thread.sleep(SLEEP_DURATION);
    				}
    				System.err.println("Sending final results.");
    				stub.updateResultsAndTallies(simulatedEvent);
    			}
    			Thread.sleep(TIME_DELAY);
    		}
        }catch(NotBoundException e){
        	throw new OlympicException("Cannot find Cacophonix.", e);
        }catch(RemoteException e){
        	throw new OlympicException("Error while broadcasting.", e);
        }catch (InterruptedException e) {
        	throw new OlympicException("Games Interrupted.", e);
        }
	}
}
