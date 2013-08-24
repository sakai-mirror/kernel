/**
 * 
 */
package org.sakaiproject.content.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.RepositoryService;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.User;

/**
 * 
 *
 */
public class RepoItem implements RepositoryItem {

	protected long itemId;
	protected long parentId;
	protected Map<String, Object> properties;
	protected ResourceProperties props = null;
	
	protected boolean hidden;
	protected AccessMode accessMode;
	protected Date releaseDate;
	protected Date retractDate;
	protected Collection<String> groups;
	protected String resourceTypeId;
	protected ResourceType resourceType;
	
	protected ContentEntity entity;
	
	protected String entityId;
	protected Long id;

	protected RepositoryService repoService;

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public AccessMode getAccessMode() {
		return accessMode;
	}

	@Override
	public void setAccessMode(AccessMode accessMode) {
		this.accessMode = accessMode;
	}

	@Override
	public Date getReleaseDate() {
		return releaseDate;
	}

	@Override
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	@Override
	public Date getRetractDate() {
		return retractDate;
	}

	@Override
	public void setRetractDate(Date retractDate) {
		this.retractDate = retractDate;
	}

	@Override
	public Collection<String> getGroups() {
		return groups;
	}

	@Override
	public void setGroups(Collection<String> groups) {
		this.groups = groups;
	}

	@Override
	public String getResourceType() {
		return resourceTypeId;
	}

	@Override
	public void setResourceType(String resourceType) {
		this.resourceTypeId = resourceType;
	}

	@Override
	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.model.RepositoryItem#getItemId()
	 */
	@Override
	public Long getItemId() {
		return this.itemId;
	}

	public boolean getSerializableHidden() {
		return this.hidden;
	}

	public AccessMode getSerializableAccess() {
		return this.accessMode;
	}

	public Time getSerializableReleaseDate() {
		return TimeService.newTime(this.releaseDate.getTime());
	}

	public Time getSerializableRetractDate() {
		return TimeService.newTime(this.retractDate.getTime());
	}

	public Collection<String> getSerializableGroup() {
		return this.groups;
	}

	public void setSerializableAccess(AccessMode access) {
		this.accessMode = access;
	}

	public void setSerializableHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void setSerializableResourceType(String resourceType) {
		this.resourceTypeId = resourceType;
	}

	public void setSerializableReleaseDate(Time releaseDate) {
		this.releaseDate = new Date(releaseDate.getTime());
	}

	public void setSerializableRetractDate(Time retractDate) {
		this.retractDate = new Date(retractDate.getTime());
	}

	public void setSerializableGroups(Collection<String> groups) {
		if(groups != null) {
			this.groups = new ArrayList(groups);
		}
	}

	@Override
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getSerializableResourceType() {
		return this.resourceTypeId;
	}

	@Override
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	@Override
	public Long getParentId() {
		return this.parentId;
	}
	
	public String getDisplayName() {
		String displayName = (String) props.get(ResourceProperties.PROP_DISPLAY_NAME);
		if(displayName == null) {
			displayName = "---";
		}
		return displayName;
	}

	public String getDescription() {
		String description = (String) props.get(ResourceProperties.PROP_DESCRIPTION);
		return description;
	}
	
	public String getAltText() {
		return this.resourceType.getLocalizedHoverText(entity);
		//return null;
	}
	
	public String getSizeLabel() {
		return this.resourceType.getSizeLabel(entity);
		//return null;
	}
	
	public String getLongSizeLabel() {
		String label = this.resourceType.getLongSizeLabel(entity);
		//String label = null;
		if(label == null) {
			label = "";
		}
		return label ;
	}

	public String getAccessURL() {
		//return entity.getUrl();
		return null;
	}
	 
	public Date getCreatedDate() {
		Date date = null;
		try {
			date = props.getDateProperty(ResourceProperties.PROP_CREATION_DATE);
		} catch (EntityPropertyNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EntityPropertyTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public Date getModifiedDate() {
		Date date = null;
		try {
			date = props.getDateProperty(ResourceProperties.PROP_MODIFIED_DATE);
		} catch (EntityPropertyNotDefinedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EntityPropertyTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public User getCreatedBy() {
		User user = null;
		String userId = (String) props.get(ResourceProperties.PROP_CREATOR);
		//user = userDirectoryService.getUser(userId);
		return user;
	}

	public User getModifiedBy() {
		User user = null;
		String userId = (String) props.get(ResourceProperties.PROP_MODIFIED_BY);
		// user = userDirectoryService.getUser(userId);
		return user;
	}
	
	public String getType() {
		String type = "file";
		if(this instanceof Folder) {
			type = "folder";
		}
		return type;
	}

	public String getResourceTypeId() {
		
		return this.resourceTypeId;
	}

	// e.g. hidden, locked
	public String getItemStatus() {
		// TODO
		return null;
	}

	// e.g. empty, non-empty, too-big
	public String getItemState() {
		// TODO
		return null;
	}
	
	public String getContainingCollectionId() {
		// return getContainingCollection().getId();
		return null;
	}

	public Long getParentFolderId() {
		//return repoLogic.getFolderId(this.entity.getContainingCollection().getId(), "folder");
		return this.parentId;
	}

	public RepoItem getParentFolder() {
		//return new Folder(this.entity.getContainingCollection(), sakaiProxy, repoLogic);
		return null;
	}

//	public List<Action> getItemActions() {
//		List<ActionType> actionTypes = new ArrayList<ActionType>();
//		actionTypes.add(ActionType.REVISE_CONTENT);
//		actionTypes.add(ActionType.REPLACE_CONTENT);
//		actionTypes.add(ActionType.REVISE_METADATA);
//		actionTypes.add(ActionType.COPY);
//		actionTypes.add(ActionType.MOVE);
//		actionTypes.add(ActionType.DELETE);
//		actionTypes.add(ActionType.DUPLICATE);
//		actionTypes.add(ActionType.REVISE_ORDER);
//		actionTypes.add(ActionType.REVISE_PERMISSIONS);
//		actionTypes.add(ActionType.PASTE_COPIED);
//		actionTypes.add(ActionType.PASTE_MOVED);
//		List<ResourceToolAction> resourceToolActions = this.resourceType.getActions(actionTypes);
//		List<Action> actions = Action.transform(resourceToolActions, entity);
//		return actions;
//	}
	
}
