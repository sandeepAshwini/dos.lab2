package util;

import java.io.Serializable;

/**
 * Encapsulates a lamport clock.
 * 
 * @author aravind
 * 
 */
public class LamportClock implements Serializable {

	private static final long serialVersionUID = 8316408937788296192L;
	private Long time;

	public LamportClock() {
		this.time = 0L;
	}

	/**
	 * Synchronizes the local timestamp with the incoming timestamp.
	 * 
	 * @param incomingTimeStamp
	 */
	public void synchronizeTime(LamportClock incomingTimeStamp) {
		this.time = Math.max(this.getTime(), incomingTimeStamp.getTime());
	}

	/**
	 * @return The local time
	 */
	public Long getTime() {
		return this.time;
	}

	/**
	 * Increments the local time by 1.
	 */
	public void tick() {
		this.time++;
	}
}
