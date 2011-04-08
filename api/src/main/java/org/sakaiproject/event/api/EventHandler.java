package org.sakaiproject.event.api;


public interface EventHandler {
	
	/** 
	 * 
	 */
	public static final int PRIORITY_LOW = 20;
	public static final int PRIORITY_MEDIUM = 40;
	public static final int PRIORITY_HIGH = 60;

	/**
	 * An EventHandler's priority is used to determine the order in which 
	 * EventHandlers will be consulted to deal with newly posted events.  
	 * @return
	 */
	public int getPriority();

	/**
	 * Participate in handling this event to some extent -- entirely, partially or not at all.
	 * @param event The event that can be handled.
	 * @return True if the event is handled entirely and no further processing is needed, false otherwise.
	 */
	public boolean handleEvent(Event event);

}