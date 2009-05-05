package org.sakaiproject.conditions.impl;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.conditions.api.EventKey;
import org.sakaiproject.conditions.api.Rule;


public class BaseConditionService implements ConditionService, Observer {
	
	private static Log log = LogFactory.getLog(BaseConditionService.class);

	public void addRule(String observableClass, EventKey key, Rule rule) {
		// TODO Auto-generated method stub
		
	}

	public String getClassNameForEvent(String event) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getEntitiesForService(String serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
