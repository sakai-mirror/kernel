/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.SingleStorageUser;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BasePreferencesService is a Sakai Preferences implementation.
 * </p>
 */
public abstract class BasePreferencesService implements PreferencesService, SingleStorageUser
{
	

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BasePreferencesService.class);

	/** Storage manager for this service. */
	protected Storage m_storage = null;

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** The session cache variable for current user's preferences */
	protected String ATTR_PREFERENCE = "attr_preference";
	
	/** The session cache variable for indicating whether the current user's preference was null when last looked */
	protected String ATTR_PREFERENCE_IS_NULL = "attr_preference_is_null";
	
	/**
	 * Key used to store the locale preferences
	 */
	protected static final String LOCALE_PREFERENCE_KEY = "sakai:resourceloader";
	
	/** the cache for Preference objects **/
	private Cache m_cache;
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct storage for this service.
	 */
	protected abstract Storage newStorage();

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService().getAccessUrl()) + m_relativeAccessPoint;
	}

	/**
	 * @inheritDoc
	 */
	public String preferencesReference(String id)
	{
		return getAccessPoint(true) + Entity.SEPARATOR + id;
	}

	/**
	 * Access the preferences id extracted from a preferences reference.
	 * 
	 * @param ref
	 *        The preferences reference string.
	 * @return The the preferences id extracted from a preferences reference.
	 */
	protected String preferencesId(String ref)
	{
		String start = getAccessPoint(true) + Entity.SEPARATOR;
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowd, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!securityService().unlock(lock, resource))
		{
			return false;
		}

		return true;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	protected void unlock(String lock, String resource) throws PermissionException
	{
		if (!unlockCheck(lock, resource))
		{
			throw new PermissionException(sessionManager().getCurrentSessionUserId(), lock, resource);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected abstract FunctionManager functionManager();

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			m_relativeAccessPoint = REFERENCE_ROOT;

			// construct storage and read
			m_storage = newStorage();
			m_storage.open();

			// register as an entity producer
			entityManager().registerEntityProducer(this, REFERENCE_ROOT);

			// register functions
			functionManager().registerFunction(SECURE_ADD_PREFS);
			functionManager().registerFunction(SECURE_EDIT_PREFS);
			functionManager().registerFunction(SECURE_REMOVE_PREFS);

			
			//register a cache
			m_cache = memoryService().newCache(BasePreferencesService.class.getName() +".preferences");
			
			M_log.info("init()");
		}
		catch (Exception t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		m_storage.close();
		m_storage = null;

		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * PreferencesService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Preferences getPreferences(String id)
	{
		Preferences prefs = findPreferences(id);

		// if not found at all
		if (prefs == null)
		{
			// throwaway empty preferences %%%
			prefs = new BasePreferences(id);
		}

		return prefs;
	}

	/**
	 * @inheritDoc
	 */
	public PreferencesEdit edit(String id) throws PermissionException, InUseException, IdUnusedException
	{
		// security
		unlock(SECURE_EDIT_PREFS, preferencesReference(id));

		// check for existance
		if (!m_storage.check(id))
		{
			throw new IdUnusedException(id);
		}

		// ignore the cache - get the user with a lock from the info store
		PreferencesEdit edit = m_storage.edit(id);
		if (edit == null) throw new InUseException(id);

		((BasePreferences) edit).setEvent(SECURE_EDIT_PREFS);

		return edit;
	}

	/**
	 * @inheritDoc
	 */
	public void commit(PreferencesEdit edit)
	{
		if (edit != null)
		{
			// check for closed edit
			if (!edit.isActiveEdit())
			{
				try
				{
					throw new Exception();
				}
				catch (Exception e)
				{
					M_log.warn("commit(): closed PreferencesEdit", e);
				}
				return;
			}

			// update the properties
			// addLiveUpdateProperties(user.getPropertiesEdit());
			
			//invalidate the cache
			m_cache.remove(edit.getId());
				
			// complete the edit
			m_storage.commit(edit);
		
			SessionManager sManager = sessionManager();
			Session s = sManager.getCurrentSession();
		
			// update the session cache if the preference is for current session user
			if (sManager.getCurrentSessionUserId().equals(edit.getId()))
			{
				s.setAttribute(ATTR_PREFERENCE, new BasePreferences((BasePreferences) edit));
				s.setAttribute(ATTR_PREFERENCE_IS_NULL, Boolean.FALSE);
			}

			// track it
			eventTrackingService()
					.post(eventTrackingService().newEvent(((BasePreferences) edit).getEvent(), edit.getReference(), true));

			// close the edit object
			((BasePreferences) edit).closeEdit();
		}
	}

	/**
	 * @inheritDoc
	 */
	public void cancel(PreferencesEdit edit)
	{
		if (edit != null)
		{
			// if this was an add, remove it
			if (SECURE_ADD_PREFS.equals(((BasePreferences) edit).m_event))
			{
				remove(edit);
			}
			else
			{
				// check for closed edit
				if (!edit.isActiveEdit())
				{
					try
					{
						throw new Exception();
					}
					catch (Exception e)
					{
						M_log.warn("cancel(): closed PreferencesEdit", e);
					}
					return;
				}

				// release the edit lock
				m_storage.cancel(edit);

				// close the edit object
				((BasePreferences) edit).closeEdit();
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(PreferencesEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn("remove(): closed PreferencesEdit", e);
			}
			return;
		}

		// complete the edit
		m_storage.remove(edit);
		
		m_cache.remove(edit.getId());

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_PREFS, edit.getReference(), true));

		// close the edit object
		((BasePreferences) edit).closeEdit();
	}

	/**
	 * Find the preferences object, in cache or storage.
	 * 
	 * @param id
	 *        The preferences id.
	 * @return The preferences object found in cache or storage, or null if not found.
	 */
	protected BasePreferences findPreferences(String id)
	{
		BasePreferences prefs = null;
		
		if (id != null)
		{
			Session session = sessionManager().getCurrentSession();
			
			if (id.equals(sessionManager().getCurrentSessionUserId()))
			{
				// if the preference is for current user
				if (session.getAttribute(ATTR_PREFERENCE_IS_NULL)!=null)
				{
					if (!((Boolean) session.getAttribute(ATTR_PREFERENCE_IS_NULL)).booleanValue())
					{
						// if the session cache indicate the preference is not null, get the preferences from cache
						prefs = new BasePreferences((BasePreferences) session.getAttribute(ATTR_PREFERENCE));
					}
				}
				else
				{
					//is the preference in the cache?
					if (m_cache.containsKey(id))
					{
						prefs = (BasePreferences) m_cache.get(id);
					}
					else
					//otherwise, get preferences from storage and update caches
					{
						prefs = (BasePreferences) m_storage.get(id);
					}
					//its possible either call above returned null if the user has the default preferences
					if (prefs != null)
					{
						session.setAttribute(ATTR_PREFERENCE_IS_NULL, Boolean.FALSE);
						session.setAttribute(ATTR_PREFERENCE, new BasePreferences(prefs));
						m_cache.put(id, prefs);
					}
					else
					{
						session.setAttribute(ATTR_PREFERENCE_IS_NULL, Boolean.TRUE);
						session.removeAttribute(ATTR_PREFERENCE);
						m_cache.put(id, null);
					}
				}
			}
			else
			{
				//is the preference in the cache
				if (m_cache.containsKey(id))
				{
					prefs = (BasePreferences) m_cache.get(id);
				}
				else
				{
					// uf the preference is not for current user, ignore sessioncache completely
					prefs = (BasePreferences) m_storage.get(id);
				}
				
				m_cache.put(id, prefs);
			}
		}
		
		return prefs;
	}

	
	
	/**
	 ** Get user's preferred locale (or null if not set)
	 ***/
	public Locale getLocale(String userId)
	{
		Locale loc = null;
		Preferences prefs = getPreferences(userId);
		ResourceProperties locProps = prefs.getProperties(LOCALE_PREFERENCE_KEY);
		String localeString = locProps.getProperty(Preferences.FIELD_LOCALE);
		
		// Parse user locale preference if set
		if (localeString != null)
		{
			String[] locValues = localeString.split("_");
			if (locValues.length > 2)
				loc = new Locale(locValues[0], locValues[1], locValues[2]); // language, country, variant
			else if (locValues.length == 2)
				loc = new Locale(locValues[0], locValues[1]); // language, country
			else if (locValues.length == 1) 
				loc = new Locale(locValues[0]); // just language
		}
		
		return loc;
	}
	
	
	
	
	
	
	/**
	 * @inheritDoc
	 */
	public boolean allowUpdate(String id)
	{
		return unlockCheck(SECURE_EDIT_PREFS, preferencesReference(id));
	}

	/**
	 * @inheritDoc
	 */
	public PreferencesEdit add(String id) throws PermissionException, IdUsedException
	{
		// check security (throws if not permitted)
		unlock(SECURE_ADD_PREFS, preferencesReference(id));

		// reserve a user with this id from the info store - if it's in use, this will return null
		PreferencesEdit edit = m_storage.put(id);
		if (edit == null)
		{
			throw new IdUsedException(id);
		}

		((BasePreferences) edit).setEvent(SECURE_ADD_PREFS);

		return edit;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EntityProducer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public String getLabel()
	{
		return "preferences";
	}

	/**
	 * @inheritDoc
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public HttpAccess getHttpAccess()
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		// for preferences access
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String id = null;

			// we will get null, service, user/preferences Id
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if (parts.length > 2)
			{
				id = parts[2];
			}

			ref.set(APPLICATION_ID, null, id, null, null);

			return true;
		}

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		Collection rv = new Vector();

		// for preferences access: no additional role realms
		try
		{
			rv.add(userDirectoryService().userReference(ref.getId()));

			ref.addUserTemplateAuthzGroup(rv, userId);
		}
		catch (NullPointerException e)
		{
			M_log.warn("getEntityAuthzGroups(): " + e);
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return "";
	}

	/**
	 * @inheritDoc
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		return "";
	}


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if a preferences by this id exists.
		 * 
		 * @param id
		 *        The user id.
		 * @return true if a preferences for this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the preferences with this id, or null if not found.
		 * 
		 * @param id
		 *        The preferences id.
		 * @return The preferences with this id, or null if not found.
		 */
		public Preferences get(String id);

		/**
		 * Add a new preferences with this id.
		 * 
		 * @param id
		 *        The preferences id.
		 * @return The locked Preferences object with this id, or null if the id is in use.
		 */
		public PreferencesEdit put(String id);

		/**
		 * Get a lock on the preferences with this id, or null if a lock cannot be gotten.
		 * 
		 * @param id
		 *        The preferences id.
		 * @return The locked Preferences with this id, or null if this records cannot be locked.
		 */
		public PreferencesEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 * 
		 * @param user
		 *        The edit to commit.
		 */
		public void commit(PreferencesEdit edit);

		/**
		 * Cancel the changes and release the lock.
		 * 
		 * @param user
		 *        The edit to commit.
		 */
		public void cancel(PreferencesEdit edit);

		/**
		 * Remove this edit and release the lock.
		 * 
		 * @param user
		 *        The edit to remove.
		 */
		public void remove(PreferencesEdit edit);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BasePreferences(id);
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, Element element)
	{
		return new BasePreferences(element);
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, Entity other)
	{
		return new BasePreferences((Preferences) other);
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		BasePreferences e = new BasePreferences(id);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		BasePreferences e = new BasePreferences(element);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		BasePreferences e = new BasePreferences((Preferences) other);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Object[] storageFields(Entity r)
	{
		return null;
	}

}
