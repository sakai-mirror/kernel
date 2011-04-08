/**
 * 
 */
package org.sakaiproject.event.api;

import java.util.Collection;

import org.sakaiproject.event.api.EventHandler;

/**
 * 
 *
 */
public interface EventHandlerRegistry {
	
	public void registerEventHandler(EventHandler handler);
	
	public Collection<EventHandler> getEventHandlers();

}
