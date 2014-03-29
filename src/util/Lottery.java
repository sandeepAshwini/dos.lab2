package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Encapsulates the lottery draw functionality.
 * 
 * @author aravind
 * 
 */
public class Lottery implements Serializable {

	private static final long serialVersionUID = 5505253127956256012L;
	private ArrayList<String> participants;
	public int lotteryEnterFrequency = 100;
	private Boolean drawCompleted = false;
	private String winner;

	public Lottery() {
		this.participants = new ArrayList<String>();
	}

	/**
	 * Adds a new participant to the list of participants.
	 * 
	 * @param participantID
	 */
	public void addParticipant(String participantID) {
		this.participants.add(participantID);
	}

	/**
	 * Conducts the lucky draw and reports a random winner.
	 * 
	 * @return
	 */
	public String conductDraw() {
		if (!this.drawCompleted) {
			System.out.println("Conducting lottery.");
			this.drawCompleted = true;
			Random random = new Random();
			if (this.participants.size() == 0) {
				this.winner = null;
			} else {
				this.winner = this.participants.get(random
						.nextInt(this.participants.size()));
			}
			return this.winner;
		} else {
			return this.winner;
		}
	}
}
