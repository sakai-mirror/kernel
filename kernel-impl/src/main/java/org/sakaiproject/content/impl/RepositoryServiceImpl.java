/**
 * 
 */
package org.sakaiproject.content.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.RepositoryService;
import org.sakaiproject.content.model.File;
import org.sakaiproject.content.model.Folder;
import org.sakaiproject.content.model.RepositoryItem;
import org.sakaiproject.exception.KernelConfigurationError;

/**
 * 
 * This service is designed to work with sakai instances that store 
 * content-bodies in the file system and have completely converted 
 * to binary-entity serialization. 
 *
 * Later we will implement an alternative for sakai instances that 
 * store content-bodies in the database and/or have not yet completed
 * the conversion from XML serialization to binary-entity serialization,
 * but that will simply piggy-back on traditional CHS and will not be
 * expected to perform as well as this new service.	
 *
 */
public class RepositoryServiceImpl implements RepositoryService {
	
	private Log log = LogFactory.getLog(RepositoryServiceImpl.class);

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.RepositoryService#getFolder(java.lang.Long)
	 */
	@Override
	public Folder getFolder(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.RepositoryService#getFile(java.lang.Long)
	 */
	@Override
	public File getFile(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.RepositoryService#repositoryItem(org.sakaiproject.content.api.ContentEntity)
	 */
	@Override
	public RepositoryItem repositoryItem(ContentEntity entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void init() {
		boolean contentBodiesStoredInDatabase = getContentBodiesStoredInDatabase();
		int countNullResourceBinaryEntities = countNullResourceBinaryEntities();
		int countNullCollectionBinaryEntities = countNullCollectionBinaryEntities();
		int countNonNullResourceXml = countNonNullResourceXml();
		int countNonNullCollectionXml = countNonNullCollectionXml();
		
		if(countNullResourceBinaryEntities > 0 || countNullCollectionBinaryEntities> 0 
				|| countNonNullResourceXml > 0 || countNonNullCollectionXml > 0 
				|| contentBodiesStoredInDatabase) {
			log.fatal("\n\n" +
                    "You are attempting to run the Repository Service on an incompatible\n" +
                    "system. The Repository Service works with sakai instances that store\n" +
                    "content-bodies in the file system and uses BINARY_ENTITY serialization.\n" +
                    "Running the Repository Service with an incompatible system will result\n" +
					"in the loss of your users' data. Please remove the Repository Service\n" +
                    "from your sakai instance and satisfy these requirements before attempting\n" +
					"to restart it.\n\nContent Bodies Stored In Database: " + contentBodiesStoredInDatabase +
					"\nNull in Binary Entities CR table: " + countNullResourceBinaryEntities +
					"\nNull in Binary Entities CC table: " + countNullCollectionBinaryEntities +
					"\nNon Null XML in CR: " + countNonNullResourceXml +
					"\nNon Null XML in CC: " + countNonNullCollectionXml + "\n\n");
			
            log.fatal("STOP ============================================");

            if(contentBodiesStoredInDatabase) {
            	throw new KernelConfigurationError("Content Bodies Stored In Database");
            } else {
            	throw new KernelConfigurationError("Incomplete Conversion to Binary Entity Serialization");
            }
		}
	}

	protected boolean getContentBodiesStoredInDatabase() {
		// TODO Auto-generated method stub
		return false;
	}

	protected int countNonNullCollectionXml() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected int countNonNullResourceXml() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected int countNullCollectionBinaryEntities() {
		// TODO Auto-generated method stub
		return 0;
	}

	protected int countNullResourceBinaryEntities() {
		// TODO Auto-generated method stub
		return 0;
	}

}
