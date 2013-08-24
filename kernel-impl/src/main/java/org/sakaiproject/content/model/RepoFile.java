/**
 * 
 */
package org.sakaiproject.content.model;

import java.util.Collection;

import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.time.api.Time;

/**
 * @author jimeng
 *
 */
public class RepoFile extends RepoItem implements File, SerializableResourceAccess {

	protected String resourceId;
	protected String contentType;
	protected String filePath;
	protected long contentLength;
	protected byte[] body;
	
	/**
	 * @return the body
	 */
	@Override
	public byte[] getBody() {
		return body;
	}

	/**
	 * @param body the body to set
	 */
	@Override
	public void setBody(byte[] body) {
		this.body = body;
	}

	/**
	 * @return the resourceId
	 */
	@Override
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * @param resourceId the resourceId to set
	 */
	@Override
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * @return the contentType
	 */
	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the filePath
	 */
	@Override
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	@Override
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return the contentLength
	 */
	@Override
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * @param contentLength the contentLength to set
	 */
	@Override
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public SerializableEntity getSerializableProperties() {
		return null;
	}

	public String getSerializableId() {
		return this.resourceId;
	}

	public byte[] getSerializableBody() {
		return body;
	}

	public String getSerializableContentType() {
		return this.contentType;
	}

	public String getSerializableFilePath() {
		return filePath;
	}

	public long getSerializableContentLength() {
		return this.contentLength;
	}

	public void setSerializableId(String id) {
		this.resourceId = id;
	}

	public void setSerializableContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setSerializableContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public void setSerializableFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setSerializableBody(byte[] body) {
		this.body = body;
	}

	public ResourceTypeRegistry getResourceTypeRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

//	public String getMimeType() {
//		return ((ContentResource) entity).getContentType();
//	}
//	
//	public String getSizeSortCode() {
//		StringBuilder buf = new StringBuilder("80000000000000000000");
//		String contentLength = Long.toHexString(((ContentResource) entity).getContentLength());
//		buf.insert(buf.length() - contentLength.length(), contentLength);
//		return buf.toString();
//	}
//	

}
