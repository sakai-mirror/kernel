package org.sakaiproject.conditions.api;

import java.util.Map;

public interface ConditionService {
	
	public void addRule(String observableClass, EventKey key, Rule rule);
	
	public Map<String, String> getEntitiesForService(String serviceName);
	
	public String getClassNameForEvent(String event);


}
