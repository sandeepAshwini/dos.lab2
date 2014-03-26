package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Lottery implements Serializable {

	private static final long serialVersionUID = 5505253127956256012L;
	private ArrayList<String> participants;
	public static int lotteryEnterFrequency = 100;
	private Boolean drawCompleted = false;
	private String winner;

	public Lottery() {
		this.participants = new ArrayList<String>();
	}

	public void addParticipant(String participantID) {
		this.participants.add(participantID);
	}

	public String conductDraw() {
		synchronized (this.drawCompleted) {
			if (!this.drawCompleted) {
				System.out.println("Conducting lottery.");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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

}
