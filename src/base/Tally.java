package base;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Encapsulates the overall medal tally.
 * @author sandeep
 *
 */
public class Tally extends Printable implements Serializable {
	private static final long serialVersionUID = 4325549347170407101L;
	private HashMap<MedalCategories, Integer> medalTally;
	
	public Tally() {
		this.medalTally = new HashMap<MedalCategories, Integer>();
		for (MedalCategories category : MedalCategories.values()) {
			medalTally.put(category, 0);
		}
	}
	
	/**
	 * Used to update the current medal tally, when something new is won.
	 * @param category
	 */
	public void incrementTally(MedalCategories category) {
		int currentCount = this.medalTally.get(category);
		this.medalTally.put(category, currentCount + 1);
	}
	
	/**
	 * Print methods and Printable interface implementation.
	 */
	public void printMedalTally() {
		for(MedalCategories medal:medalTally.keySet()) {
			System.out.print(medal.getCategory() + " : " + medalTally.get(medal) + " ");
		}
		System.out.println();
	}

	public void printContents() {
		printMedalTally();
	}
	
	public void writeToFile(FileWriter writer)throws IOException{
		
		for(MedalCategories medal:medalTally.keySet()) {
			writer.write(medal.getCategory() + " : " + medalTally.get(medal) + " ");
		}
		writer.write("\n");
		
	}
	
	
	
}
