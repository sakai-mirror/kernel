/**
 * 
 */
package org.sakaiproject.event.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventDelayHandler;
import org.sakaiproject.event.api.EventHandlerRegistry;
import org.sakaiproject.event.api.EventHandler;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * 
 *
 */
public  class BasicEventHandlerRegistry implements EventHandlerRegistry {
	
	private static Log log = LogFactory.getLog(BasicEventHandlerRegistry.class);
	
	protected SortedSet<EventHandler> eventHandlers;
		
	public void init() {
		log.info("init");
		
		this.eventHandlers = new TreeSet<EventHandler>(new Comparator<EventHandler>(){

			public int compare(EventHandler arg0, EventHandler arg1) {
				int c = arg0.getPriority() - arg1.getPriority();
				if(c == 0) {
					c = arg0.hashCode() - arg1.hashCode();
				}
				return c;
			}
			
		} );
	}
	
	public void destroy() {
		//log.info("destroy");
	}

	public void registerEventHandler(EventHandler handler) {
		log.info("regisetHandler(" + handler + ") priority: " + (handler == null ? "--" : handler.getPriority()));
		this.eventHandlers.add(handler);
		
	}

	public Collection<EventHandler> getEventHandlers() {
		
		return new ArrayList<EventHandler>(this.eventHandlers);
	}

}
