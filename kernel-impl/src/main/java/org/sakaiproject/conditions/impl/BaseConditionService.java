package org.sakaiproject.conditions.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.conditions.api.Condition;
import org.sakaiproject.conditions.api.ConditionProvider;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.conditions.api.ConditionTemplateSet;
import org.sakaiproject.conditions.api.Rule;


public class BaseConditionService implements ConditionService, Observer {

	
	private static Log log = LogFactory.getLog(BaseConditionService.class);
	
	private Map<String, String> eventLookup = new HashMap<String, String>();
	private Map<String, ConditionProvider> registeredProviders = new HashMap<String, ConditionProvider>();
	
	public void init() {
		eventLookup.put("gradebook.updateItemScore", "org.sakaiproject.conditions.impl.AssignmentGrading");
		eventLookup.put("gradebook.updateAssignment", "org.sakaiproject.conditions.impl.AssignmentUpdate");
	}

	public String addRule(String eventType, Rule rule) {
		return "foo";
	}

	public ConditionTemplateSet getConditionTemplateSetForService(
			String serviceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getRegisteredServiceNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerConditionTemplates(
			ConditionTemplateSet conditionTemplateSet) {
		// TODO Auto-generated method stub
		
	}

	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

	public Condition makeCondition(Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Map<String, String> getEntitiesForServiceAndContext(String serviceName, String contextId) {
		return registeredProviders.get(serviceName).getEntitiesForContext(contextId);
	}

	public String getClassNameForEvent(String event) {
		return eventLookup.get(event);
	}

	public void registerConditionProvider(ConditionProvider provider) {
		this.registeredProviders.put(provider.getId(), provider);
	}

}
