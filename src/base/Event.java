package base;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Encapsulates an event in the Olympic games.
 */

public class Event implements Serializable, Runnable{
	private static final long serialVersionUID = -9092827493794079435L;
	private static int numberOfMedals = MedalCategories.values().length;
	private static int EVENT_LEGS = 5;
	private static long INTERVAL = 5 * 1000;
	private static int MIN_NUMBER_PARTICIPANTS = MedalCategories.values().length;
	private static int RANGE_NUMBER_PARTICIPANTS = 7;
	
	/**
	 * Event attributes.
	 */
	private EventCategories eventName;
	private Results result;
	private int numberOfParticipants;
	private ArrayList<Athlete> athletes;
	private EventStatus eventStatus;
	
	
	public Event(EventCategories eventName){
		this.eventName = eventName;
		this.result = new Results();
		this.eventStatus = EventStatus.SCHEDULED;
		setScores();
	}

	/**
	 * Generates a random number of players for the event.
	 * This number is greater than 3, since we have 3 places.
	 * 
	 */
	private void setScores(){
		Random rand = new Random();
		this.numberOfParticipants = rand.nextInt(RANGE_NUMBER_PARTICIPANTS) + MIN_NUMBER_PARTICIPANTS;
		this.athletes = new ArrayList<Athlete>();
		for(int i = 0; i < numberOfParticipants; i++)
		{
			this.athletes.add(new Athlete(this.eventName));
		}
		
	}
	
	
	/**
	 * Getters for results and event name.
	 * @return
	 */
	public Results getResult(){
		return this.result;
	}
	
	public EventCategories getName(){
		return this.eventName;
	}
	
	/**
	 * Prints the results of the event.
	 */
	public void printResults(){
		System.out.println("Event : " + this.eventName.getCategory());
		this.result.printResults();
	} 
	
	/**
	* Simulates the event.
	* Run method simulates the event.
	* The event consists of 5 legs and a random number of players.
	* There is a constant interval between legs.
	* All participant's scores are randomly initialized, and randomly updated in each leg.
	* */
	public void run(){
		int count = 0;
		this.eventStatus = EventStatus.IN_PROGRESS;
		try{
		while(count++ < EVENT_LEGS){
			updateScores();
			Thread.sleep(INTERVAL);
		}
		
		Collections.sort(this.athletes);
		ArrayList<NationCategories> winners = new ArrayList<NationCategories>();
		for(int i = 0; i < numberOfMedals; i++){
			winners.add(this.athletes.get(i).getNationality());
		}
		this.result.updateWinners(winners);	
		this.eventStatus = EventStatus.COMPLETED;
		}catch(InterruptedException e){
			this.eventStatus = EventStatus.INTERRUPTED;
			System.out.println("Event Interrupted by Vesuvius.");
			e.printStackTrace();
		}
		
	}	
	
	/**
	 * Calls incrementScore on each participant.
	 * Scores can be accessed at any time by the server, hence the update is synchronized.
	 */
	private synchronized void updateScores(){
		for(Athlete athlete : this.athletes){
			athlete.incrementScore();
		}
	}
	
	/**
	 * Returns the current scores.
	 * Again, updates may happen at any time, and the method must be synchronized.
	 * @return
	 */
	public synchronized ArrayList<Athlete> getScores(){
		Collections.sort(this.athletes);
		return this.athletes;
	}
	
	/**
	 * Returns True if event status has been set to COMPLETED.
	 * @return
	 */
	public boolean isCompleted()
	{
		return (this.eventStatus == EventStatus.COMPLETED);
	}
	
}