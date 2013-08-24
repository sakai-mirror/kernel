/**
 * 
 */
package org.sakaiproject.content.api;

import org.sakaiproject.content.model.File;
import org.sakaiproject.content.model.Folder;
import org.sakaiproject.content.model.RepositoryItem;

/**
 * 
 *
 */
public interface RepositoryService {
	
	public File getFile(Long id);

	public Folder getFolder(String collectionId);

	public Folder getFolder(Long id);
	
	public RepositoryItem repositoryItem(ContentEntity entity);

}
