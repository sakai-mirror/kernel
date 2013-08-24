/**
 * 
 */
package org.sakaiproject.content.model;


import java.util.List;

import org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess;
import org.sakaiproject.entity.api.serialize.SerializableEntity;

/**
 * 
 *
 */
public class RepoFolder extends RepoItem implements Folder, SerializableCollectionAccess {
	
	private String collectionId;
	
	
	public RepoFolder() {
		
	}

	@Override
	public String getCollectionId() {
		return collectionId;
	}

	@Override
	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	@Override
	public String getSerializableId() {
		return this.collectionId;
	}

	@Override
	public void setSerializableId(String id) {
		this.collectionId = id;
		
	}

	@Override
	public SerializableEntity getSerializableProperties() {
		
		return null;
	}
	
	@Override
	public List<RepositoryItem> getMembers() {
		List<RepositoryItem> list = null;
		return list;
	}
	
//	public String getSizeSortCode() {
//		StringBuilder buf = new StringBuilder("00000000000000000000");
//		String memberCount = Long.toHexString(((ContentCollection) entity).getMemberCount());
//		buf.insert(buf.length() - memberCount.length(), memberCount);
//		return buf.toString();
//	}
//		
//	public List<Action> getAddActions() {
//		List<ResourceToolAction> resourceToolActions = new ArrayList<ResourceToolAction>();
//		
//		List<ActionType> actionTypes = new ArrayList<ActionType>();
//		actionTypes.add(ActionType.CREATE);
//		actionTypes.add(ActionType.CREATE_BY_HELPER);
//		
//		List<ResourceType> types = this.sakaiProxy.getResourceTypes();
//		for(ResourceType type : types) {
//			List<ResourceToolAction> subset = type.getActions(actionTypes);
//			if(subset != null) {
//				resourceToolActions.addAll(subset);
//			}
//		}
//		List<Action> actions = Action.transform(resourceToolActions, entity);
//		return actions;
//	}
//
//	// e.g. expand, collapse
//	public List<Action> getFolderActions() {
//		List<ActionType> actionTypes = new ArrayList<ActionType>();
//		actionTypes.add(ActionType.EXPAND_FOLDER);
//		actionTypes.add(ActionType.COLLAPSE_FOLDER);
//		actionTypes.add(ActionType.PASTE_COPIED);
//		actionTypes.add(ActionType.PASTE_MOVED);
//		List<ResourceToolAction> resourceToolActions = this.resourceType.getActions(actionTypes);
//		List<Action> actions = Action.transform(resourceToolActions, entity);
//		return actions;
//	}	
//	
}
