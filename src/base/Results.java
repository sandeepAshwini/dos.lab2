package base;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Encapsulates the results of a given event.
 * 
 * @author sandeep
 *
 */

public class Results extends Printable implements Serializable {
	private static final long serialVersionUID = -7858027301096005662L;
	
	/**
	 * The mapping of the medals to nations which won them.
	 */
	private HashMap<MedalCategories, NationCategories> winners;
	
	public Results(ArrayList<NationCategories> winners) {
		this.winners = new HashMap<MedalCategories, NationCategories>();
		updateWinners(winners);
	}

	public Results() {
		this.winners = new HashMap<MedalCategories, NationCategories>();
	}
	
	/**
	 * Getter to retrieve the winner of each medal.
	 * @param medalType
	 * @return
	 */
	public NationCategories getTeam(MedalCategories medalType) {
		return winners.get(medalType);
	}
	
	/**
	 * Sets the winners.
	 * @param winners
	 */
	public void updateWinners(ArrayList<NationCategories> winners) {
		int i = 0;
		for(MedalCategories medalType : MedalCategories.values()) {
			this.winners.put(medalType, winners.get(i));
			i++;
		}
	}
	
	/**
	 * Print methods and Printable Interface Implementation.
	 */
	public void printResults() {
		for(MedalCategories medal : winners.keySet()) {
			System.out.println(medal.getCategory() + " : " + winners.get(medal).getCategory());
		}
	}

	public void printContents() {
		printResults();
	}
	
	public void writeToFile(FileWriter writer) throws IOException {
		for(MedalCategories medal : winners.keySet()) {
			writer.write(medal.getCategory() + " : " + winners.get(medal).getCategory() + "\n");
		}
	}
}
