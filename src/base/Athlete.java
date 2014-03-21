package base;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

/**
 * Athlete abstracts a single player. 
 * Associates a nationality, an event and a score with each player.
 * Allows two athletes to be compared in terms of their score.
 */
public class Athlete extends Printable implements Comparable<Athlete>, Serializable{
	private static final long serialVersionUID = 6637345322731549058L;
	private static int scoreRange = 10;
	private static Random randomNumberGenerator = new Random();
	
	/**
	 * Attributes of the athlete.
	 */
	private NationCategories nationality;
	private EventCategories event;
	private String name;
	
	/**
	 * Score of the athlete in the given event.
	 */
	private int score;
	
	public Athlete(EventCategories participatingEvent){
		int number = randomNumberGenerator.nextInt(NationCategories.values().length);
		this.nationality = NationCategories.values()[number];
		this.score = randomNumberGenerator.nextInt(scoreRange);
		this.event = participatingEvent;
		this.name = UUID.randomUUID().toString();

	}
	
	/**
	 * Function randomly increments the athlete's score.
	 */
	public void incrementScore(){
		int increment = randomNumberGenerator.nextInt(scoreRange);
		this.score += increment;
	}

	/**
	 * Overridden function allows two athletes to be compared in terms of their score.
	 */
	@Override
	public int compareTo(Athlete competitor) {
		return -(this.score - competitor.score);		
	}
	
	
	/**
	 * Returns the nationality of the current athlete.
	 * @return NationCategories
	 */
	public NationCategories getNationality(){
		return this.nationality;
	}
	
	/**
	 * Print methods.
	 * Print contents is an implementation of the abstract inherited functions.
	 */
	
	public void printScore(){
		System.out.printf("Name : %s \t Nationality : %s \t Score : %d. \n", this.name, this.nationality.getCategory(), this.score);
	}

	public void printContents() {
		printScore();
	}
	
	
	/**
	 * Overriden method to write contents to a file instead of printing to console.
	 * @param FileWriter
	 */	
	public void writeToFile(FileWriter writer) throws IOException {
		writer.write(String.format("Name : %s \t Nationality : %s \t Score : %d. \n", this.name, this.nationality.getCategory(), this.score));
	}
}
