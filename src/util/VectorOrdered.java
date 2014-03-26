package util;

import java.io.Serializable;
import java.util.HashMap;

public class VectorOrdered implements Serializable {
	private static final long serialVersionUID = 910351590403500446L;
	private HashMap<Integer, Long> timeVector;
	private Integer PID;

	public VectorOrdered(int PID) {
		this.timeVector = new HashMap<Integer, Long>();
		this.PID = PID;
	}

	public void synchronizeVector(VectorOrdered incomingVector) {
		for (Integer PID : this.timeVector.keySet()) {
			if (PID == this.PID) {
				continue;
			} else {
				this.updateValue(PID, incomingVector.getValue(PID));
			}
		}
	}

	public Long getValue(int PID) {
		return this.timeVector.get(PID);
	}

	public void updateValue(int PID, long value) {
		this.timeVector.put(PID, value);
	}

	public void incrementMyTime() {
		if (this.timeVector.containsKey(this.PID)) {
			this.timeVector.put(this.PID, this.timeVector.get(this.PID) + 1);
		} else {
			this.timeVector.put(this.PID, 1L);
		}
	}

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
