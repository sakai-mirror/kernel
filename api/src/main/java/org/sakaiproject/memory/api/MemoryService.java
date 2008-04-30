/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.memory.api;

/**
 * <p>
 * MemoryService is the interface for the Sakai Memory service.
 * </p>
 * <p>
 * This tracks memory users (cachers), runs a periodic garbage collection to keep memory available, and can be asked to report memory usage.
 * </p>
 */
public interface MemoryService
{
	/**
	 * Report the amount of available memory.
	 * 
	 * @return the amount of available memory.
	 */
	long getAvailableMemory();

	/**
	 * Cause less memory to be used by clearing any optional caches.
	 * 
	 * @exception PermissionException
	 *            if the current user does not have permission to do this.
	 */
	void resetCachers() throws MemoryPermissionException;




	/**
	 * Construct a Cache. Attempts to keep complete on Event notification by calling the refresher.
	 * 
	 * @param cacheName Load a defined bean from ComponentManager or create a default cache with this name.
	 * @param refresher
	 *        The object that will handle refreshing of event notified modified or added entries.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
	 */
	Cache newCache(String cacheName, CacheRefresher refresher, String pattern);

	/**
	 * Construct a Cache. Attempts to keep complete on Event notification by calling the refresher.
	 * 
	 * @param cacheName Load a defined bean from ComponentManager or create a default cache with this name.
	 * @param pattern
	 *        The "startsWith()" string for all resources that may be in this cache - if null, don't watch events for updates.
	 */
	Cache newCache(String cacheName, String pattern);

	/**
	 * Construct a Cache. Automatic refresh handling if refresher is not null.
	 * 
	 * @param cacheName Load a defined bean from ComponentManager or create a default cache with this name.
	 * @param refresher
	 *        The object that will handle refreshing of expired entries.
	 */
	Cache newCache(String cacheName, CacheRefresher refresher);

	/**
	 * Construct a Cache. No automatic refresh handling.
	 * @param cacheName Load a defined bean from ComponentManager or create a default cache with this name.
	 */
	Cache newCache(String cacheName);

	/**
	 * Construct a multi-ref Cache. No automatic refresh: expire only, from time and events.
	 * 
	 * @param cacheName Load a defined bean from ComponentManager or create a default cache with this name.
	 */
	MultiRefCache newMultiRefCache(String cacheName);

	/**
	 * Get a status report of memory users.
	 * 
	 * @return A status report of memory users.
	 */
	String getStatus();
}
