/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/conditionalrelease/tags/sakai_2-4-1/api/src/java/org/sakaiproject/conditions/cover/ConditionService.java $
 * $Id: ConditionService.java 12907 2007-10-22 23:53:16Z zach.thomas@txstate.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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
package org.sakaiproject.conditions.cover;

import java.util.Map;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.conditions.api.EventKey;
import org.sakaiproject.conditions.api.Rule;

/**
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 *
 */
public class ConditionService {
	
	private static org.sakaiproject.conditions.api.ConditionService m_instance = null;

	/* (non-Javadoc)
	 * @see org.sakaiproject.conditions.api.ConditionService#addRule(java.lang.Class, org.sakaiproject.conditions.api.EventKey, org.sakaiproject.conditions.api.Rule)
	 */
	public static void addRule(String observableClass, EventKey key, Rule rule) {
		org.sakaiproject.conditions.api.ConditionService service = getInstance();
		if (service == null) return;
		
		service.addRule(observableClass, key, rule);
	}
	
	public static Map<String, String> getEntitiesForService(String serviceName) {
		org.sakaiproject.conditions.api.ConditionService service = getInstance();
		if (service == null) return null;
		
		return service.getEntitiesForService(serviceName);
	}
	
	public static String getClassNameForEvent(String event) {
		org.sakaiproject.conditions.api.ConditionService service = getInstance();
		if (service == null) return null;
		
		return service.getClassNameForEvent(event);
	}
	
	public static org.sakaiproject.conditions.api.ConditionService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.conditions.api.ConditionService) ComponentManager
						.get(org.sakaiproject.conditions.api.ConditionService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.conditions.api.ConditionService) ComponentManager.get(org.sakaiproject.conditions.api.ConditionService.class);
		}
	}

}
