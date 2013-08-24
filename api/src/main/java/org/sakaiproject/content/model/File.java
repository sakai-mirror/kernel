/**
 * 
 */
package org.sakaiproject.content.model;

/**
 * 
 *
 */
public interface File extends RepositoryItem {

	public abstract void setContentLength(long contentLength);

	public abstract long getContentLength();

	public abstract void setFilePath(String filePath);

	public abstract String getFilePath();

	public abstract void setContentType(String contentType);

	public abstract String getContentType();

	public abstract void setResourceId(String resourceId);

	public abstract String getResourceId();

	public abstract void setBody(byte[] body);

	public abstract byte[] getBody();

}
