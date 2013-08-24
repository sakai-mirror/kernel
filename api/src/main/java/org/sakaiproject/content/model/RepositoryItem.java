/**
 * 
 */
package org.sakaiproject.content.model;

import java.util.Collection;
import java.util.Date;

import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.entity.api.serialize.SerializableEntity;

/**
 * 
 *
 */
public interface RepositoryItem  extends SerializableEntity {
	
	public Long getItemId();
	
	public void setItemId(Long itemId);

	public void setItemId(long itemId);

	public void setResourceType(String resourceType);

	public String getResourceType();

	public void setGroups(Collection<String> groups);

	public Collection<String> getGroups();

	public void setRetractDate(Date retractDate);

	public Date getRetractDate();

	public void setReleaseDate(Date releaseDate);

	public Date getReleaseDate();

	public void setAccessMode(AccessMode accessMode);

	public AccessMode getAccessMode();

	public void setHidden(boolean hidden);

	public boolean isHidden();

	public abstract Long getParentId();

	public abstract void setParentId(long parentId);

}
