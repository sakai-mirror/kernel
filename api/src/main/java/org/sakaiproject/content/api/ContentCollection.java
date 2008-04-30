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

package org.sakaiproject.content.api;

import java.util.List;
import java.util.Map;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.time.api.Time;

/**
* <p>ContentCollection is the core interface for a Collection object in the GenericContentHostingService.</p>
* <p>A Collection has a list of internal members, each a resource id.</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public interface ContentCollection
	extends ContentEntity
{
	/**
	* Access a List of the collection's internal members, each a resource id String.
	* @return a List of the collection's internal members, each a resource id String (may be empty).
	*/
	public List getMembers();

	/**
	* Access a List of the collections' internal members as full ContentResource or
	* ContentCollection objects.
	* @return a List of the full objects of the members of the collection.
	*/
	public List getMemberResources();

	/**
	* Access the size of all the resource body bytes within this collection in Kbytes.
	* @return The size of all the resource body bytes within this collection in Kbytes.
	*/
	public long getBodySizeK();
	
	/**
	 * Access a count of the number of members (resources and collections) within this
	 * collection.  This count is not recursive.  Only items whose immediate parent is
	 * the current collection are counted.
	 * @return
	 */
	public int getMemberCount();
	
	/**
	 * Access the release date before which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @return The date/time at which the entity may be accessed by all users.
	 */
	public Time getReleaseDate();
	
	/**
	 * Access the retract date after which this entity should not be available to users 
	 * except those with adequate permission (what defines "adequate permission" is TBD).
	 * @return The date/time at which access to the entity should be restricted.
	 */
	public Time getRetractDate();
	
	

	/**
	 * @param priorities
	 */
	void setPriorityMap(Map priorities);

}	// ContentCollection



