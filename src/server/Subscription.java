package server;

import java.util.HashSet;
import java.util.Set;

import base.EventCategories;

/**
 * Handles subscription to a particular event, 
 * with the name of the event and the set of subscribers
 * @author aravind
 *
 */
public class Subscription {
	private EventCategories eventName;
	private Set<String> subscribers;
	
	/**
	 * Returns the event category managed by this subscription.
	 * @return EventCategories
	 */
	public EventCategories getEventName() {
		return this.eventName;
	}
	
	/**
	 * Returns the set of subscribers managed by this subscription.
	 * @return Set<String>
	 */
	public Set<String> getSubscribers() {
		return this.subscribers;
	}
	
	/**
	 * Sets the event category managed by this subscription.
	 * @param eventName
	 */
	public void setEventName(EventCategories eventName) {
		this.eventName = eventName;
	}
	
	/**
	 * Adds a particular subscriber to the set of subscribers 
	 * for the event managed by this subscription
	 * @param subscriber
	 */
	public void addSubscriber(String subscriber) {
		if(subscribers == null) {
			subscribers = new HashSet<String>();
		}
		subscribers.add(subscriber);
	}
}
