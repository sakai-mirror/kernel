/**
 * 
 */
package org.sakaiproject.content.impl;

import org.sakaiproject.content.model.Folder;

/**
 * 
 *
 */
public interface RepositoryDao {
	
	public Folder getFolder(String collectionId);
	
	public Folder getFolder(Long itemId);
}
