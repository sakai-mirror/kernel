/**
 * 
 */
package org.sakaiproject.content.model;

import java.util.List;

/**
 * 
 *
 */
public interface Folder extends RepositoryItem {

	public abstract void setCollectionId(String collectionId);

	public abstract String getCollectionId();

	public abstract List<RepositoryItem> getMembers();
	
	
	
}
