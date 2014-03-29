package util;

import java.io.Serializable;

public class LamportClock implements Serializable {
	
	private static final long serialVersionUID = 8316408937788296192L;
	private Long time;
	
	public LamportClock() {
		this.time = 0L;
	}
	
	public void synchronizeTime(LamportClock incomingTimeStamp) {
		this.time = Math.max(this.getTime(), incomingTimeStamp.getTime());
	}
	
	public Long getTime() {
		return this.time;
	}
	
	public void tick() {
		this.time++;
	}
}
