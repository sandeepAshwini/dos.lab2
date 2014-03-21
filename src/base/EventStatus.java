package base;

/**
 * Enum for Event Status.
 * SCHEDULED - The event is yet to start.
 * IN PROGRESS - The event is occurring currently.
 * COMPLETED - The event has finished.
 * INTERRUPTED - The event failed to complete.
 * @author sandeep
 *
 */
public enum EventStatus {
	SCHEDULED("Scheduled"),
	IN_PROGRESS("In Progress"),
	COMPLETED("Completed"),
	INTERRUPTED("Interrupted");
	
	private String value;
	
	EventStatus(String category) {
		this.value = category;
	}
	
	public String getCategory() {
		return this.value;
	}
}
