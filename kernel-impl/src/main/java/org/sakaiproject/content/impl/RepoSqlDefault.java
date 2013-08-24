/**
 * 
 */
package org.sakaiproject.content.impl;

/**
 * 
 *
 */
public class RepoSqlDefault implements RepoSql {

	/*
	 * COLLECTION_ID, IN_COLLECTION, XML, BINARY_ENTITY, id, parent_id
	 */

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.RepoSql#sqlGetFolderByStringId()
	 */
	public String sqlGetFolderByStringId() {
		return "select item_id, parent_id, binary_entity from content_collection where collection_id=?";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.RepoSql#sqlGetFolderByIntegerId()
	 */
	public String sqlGetFolderByIntegerId() {
		return "select item_id, parent_id, binary_entity from content_collection where item_id=?";
	}

}

