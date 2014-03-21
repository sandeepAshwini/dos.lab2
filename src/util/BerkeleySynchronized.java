package util;

public abstract class BerkeleySynchronized {
	
	public long clockOffset = 0;
	
	public void synchronizeClocks() {
	}

	public long getTime() {
		return (System.currentTimeMillis() + clockOffset);
	}

	public void setClockOffset(long clockOffset) {
		this.clockOffset = clockOffset;
	}
}
