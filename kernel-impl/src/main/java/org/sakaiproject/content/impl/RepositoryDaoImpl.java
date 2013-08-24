/**
 * 
 */
package org.sakaiproject.content.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentCollectionSerializer;
import org.sakaiproject.content.model.Folder;
import org.sakaiproject.content.model.RepoFolder;
import org.sakaiproject.content.model.RepositoryItem;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.serialize.EntityParseException;

/**
 * @author jimeng
 *
 */
public class RepositoryDaoImpl implements RepositoryDao {
	
	protected SqlService sqlService;
	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}
	
    /** contains a map of the database dependent handlers. */
    protected Map<String, RepoSql> databaseBeans;

    /** The db handler we are using. */
	protected RepoSql repoSql;

	private Type1BaseContentCollectionSerializer collectionSerializer;
	
	public void init() {
		repoSql = new RepoSqlDefault();
		this.collectionSerializer = new Type1BaseContentCollectionSerializer();
		
	}

	
	/*
	 * COLLECTION_ID, IN_COLLECTION, XML, BINARY_ENTITY, id, parent_id
	 */

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.RepositoryDao#getFolder(java.lang.String)
	 */
	@Override
	public Folder getFolder(String collectionId) {
		
		Folder folder = null;
		String sql = repoSql.sqlGetFolderByStringId();
		Object[] fields = new Object[]{ collectionId };
		List<Folder> folders = sqlService.dbRead(sql, fields , new SqlReader(){

			@Override
			public Object readSqlResultRecord(ResultSet result)
					throws SqlReaderFinishedException {
				RepositoryItem f = new RepoFolder();
				try {
					try {
						collectionSerializer.parse(f, result.getBlob("binary_entity").getBytes(1, Integer.MAX_VALUE));
					} catch (EntityParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					f.setItemId(result.getLong("item_id"));
					f.setParentId(result.getLong("parent_id"));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return f;
			}
			
		});
		
		return folder ;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.RepositoryDao#getFolder(java.lang.Long)
	 */
	@Override
	public Folder getFolder(Long itemId) {
		// TODO Auto-generated method stub
		return null;
	}

}


