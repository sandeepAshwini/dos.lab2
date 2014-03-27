package util;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Encapsulates an implementation of Vector clocks.
 * 
 * @author aravind
 * 
 */
public class VectorClock implements Serializable {
	private static final long serialVersionUID = 910351590403500446L;
	private HashMap<Integer, Long> timeVector;
	private Integer PID;
	private static long SLEEP_TIME = 100;

	public VectorClock(int PID) {
		this.timeVector = new HashMap<Integer, Long>();
		this.PID = PID;
	}

	/**
	 * Synchronizes own time vector with an incoming time vector.
	 * 
	 * @param callerID
	 * @param incomingVector
	 */
	public void synchronizeVector(int callerID, VectorClock incomingVector) {
		/*
		 * while (!causalConditionsSatisfied(callerID, incomingVector)) { try {
		 * Thread.sleep(SLEEP_TIME); } catch (InterruptedException e) {
		 * e.printStackTrace(); } }
		 */
		for (Integer id : this.timeVector.keySet()) {
			if (id == this.PID) {
				continue;
			} else {
				this.updateValue(id, Math.max(this.getValue(id),
						incomingVector.getValue(id)));
			}
		}
	}

	private boolean causalConditionsSatisfied(int callerID,
			VectorClock incomingVector) {
		if (incomingVector.getValue(callerID) == this.timeVector.get(callerID) + 1) {
			for (Integer key : this.timeVector.keySet()) {
				if (this.timeVector.get(key) < incomingVector.getValue(key)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Retreives the clock value for a specified process ID.
	 * 
	 * @param id
	 * @return
	 */
	public Long getValue(int id) {
		return this.timeVector.get(id);
	}

	/**
	 * Updates the clock value for a specified process ID.
	 * 
	 * @param id
	 * @param value
	 */
	public void updateValue(int id, long value) {
		this.timeVector.put(id, value);
	}

	/**
	 * Increments the current time by 1.
	 */
	public void incrementMyTime() {
		if (this.timeVector.containsKey(this.PID)) {
			this.timeVector.put(this.PID, this.timeVector.get(this.PID) + 1);
		} else {
			this.timeVector.put(this.PID, 1L);
		}
	}

	/**
	 * @return The sum of all the clock values in the time vector.
	 */
	public Long getCurrentTimeStamp() {
		Long timestamp = 0L;
		synchronized (this.timeVector) {
			for (Integer PID : this.timeVector.keySet()) {
				timestamp += this.getValue(PID);
			}
		}
		return timestamp;
	}
}
