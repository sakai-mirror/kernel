/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-impl/src/main/java/org/sakaiproject/memory/impl/MultiRefCacheImpl.java $
 * $Id: MultiRefCacheImpl.java 68286 2009-10-27 07:49:08Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.memory.impl;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.GenericMultiRefCache;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * WARNING: This is NOT cluster safe
 * <p>
 * MultiRefCacheImpl implements the MultiRefCache.
 * </p>
 * <p>
 * The references that each cache entry are sensitive to are kept in a separate map for easy access.<br />
 * Manipulation of this map is synchronized. This map is not used for cache access, just when items are added and removed.<br />
 * The cache map itself becomes synchronized when it's manipulated (not when reads occur), so this added sync. for the refs fits the existing pattern.
 * </p>
 */
public class GenericMultiRefCacheImpl extends MemCache implements GenericMultiRefCache, CacheEventListener
	{
	/** Our logger. */
	private final static Log M_log = LogFactory.getLog(GenericMultiRefCacheImpl.class);

	/** Map of reference string -> Collection of cache keys. */
	protected final ConcurrentMap<String, ConcurrentMap<Object, Object>> m_refsStore = new ConcurrentHashMap<String, ConcurrentMap<Object, Object>>();

	/**
	 * Construct the Cache - checks for expiration periodically.
	 */
	public GenericMultiRefCacheImpl(BasicMemoryService memoryService,
			EventTrackingService eventTrackingService, Ehcache cache)
	{
		super(memoryService, eventTrackingService, null, "", cache);
		cache.getCacheEventNotificationService().registerListener(this);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void put(String key, Object payload, String ref, Collection<String> dependRefs)
	{
		if(M_log.isDebugEnabled()) {
			M_log.debug("put(Object " + key + ", Object " + payload + ", Reference "+ ref
					+", Dependent Refs " + dependRefs + ")");
		}
		super.put(key, new MultiRefCacheEntry(payload, ref, dependRefs));

		// Why don't we do do this in the notify handler?
		if (ref != null)
		{
			addRefCachedKey(ref, key);
		}
		if (dependRefs != null)
		{
			for (String dependRef : dependRefs) {
				addRefCachedKey(dependRef, key);
			}
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void put(String key, Object payload)
	{
		put(key, payload, null, null);
	}

	/**
	 * Make sure there's an entry in refs for this ref that includes this key.
	 *
	 * @param ref
	 *        The entity reference string.
	 * @param key
	 *        The cache entry key dependent on this entity ref.
	 */
	protected void addRefCachedKey(String ref, Object key)
	{
		// Isn't this a threading issue. Two thread hit this and both put their own data into m_refsStore.
		ConcurrentMap<Object, Object> cachedKeys = new ConcurrentHashMap<Object, Object>();
		ConcurrentMap<Object, Object> oldCachedKeys = m_refsStore.putIfAbsent(ref, cachedKeys);
		if (oldCachedKeys != null)
		{
			cachedKeys = oldCachedKeys;
		}
		cachedKeys.put(key, key);
	}

    /**
     * {@inheritDoc}
     */
    @Override
	public void clear()
	{
		super.clear();
		m_refsStore.clear();
	}

	private void cleanEntityReferences(Object key, Object value)
	{
		if (M_log.isDebugEnabled())
			M_log.debug("cleanEntityReferences(Object " + key
					+ ", Object " + value + ")");
		if (value == null)
			return;

		final MultiRefCacheEntry cachedEntry = (MultiRefCacheEntry) value;

		// remove this key from any of the entity references in m_refs that are dependent on this entry
		for (Iterator iRefs = cachedEntry.getRefs().iterator(); iRefs.hasNext();)
		{
			String ref = (String) iRefs.next();
			ConcurrentMap<Object, Object> keys = m_refsStore.get(ref);
			if (keys != null && keys.remove(key) != null)
			{
				// remove the ref entry if it no longer has any cached keys in its collection
				// TODO This isn't thread safe.
				if (keys.isEmpty())
				{
					m_refsStore.remove(ref,keys);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription()
	{
		return "GenericMultiRefCache: " + super.getDescription();
	}

	@Override
	public Properties getProperties(boolean includeExpensiveDetails) {
		Properties p = super.getProperties(includeExpensiveDetails);
		p.put("class", this.getClass().getSimpleName());
		p.put("refsCount", m_refsStore.size());
		return p;
	}

	/*************************************************************************************************************
	 * Observer implementation
	 *************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public void update(Observable o, Object arg)
	{
		// arg is Event
		if (!(arg instanceof Event)) return;
		Event event = (Event) arg;

		// if this is just a read, not a modify event, we can ignore it
		if (!event.getModify()) return;

		String ref = event.getResource();

		if (M_log.isDebugEnabled())
			M_log.debug("continueUpdate() [" + m_resourcePattern + "] resource: " + ref
					+ " event: " + event.getEvent());

		// get the copy of the Collection of cache keys for this reference (the actual collection will be reduced as the removes occur)
		ConcurrentMap<Object, Object> cachedKeys = m_refsStore.get(ref);
		if (cachedKeys != null)
		{
			Set<Object> keySet = cachedKeys.keySet();
			for (Iterator<Object> iKeys = keySet.iterator(); iKeys.hasNext();)
			{
					Object key = iKeys.next();
					remove((String)key);

					if (M_log.isDebugEnabled()) {
						M_log.debug("Removed from cache: "+ key);
					}
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Object get(String key) {
		MultiRefCacheEntry mrce = (MultiRefCacheEntry) super.get(key);
		return (mrce != null ? mrce.getPayload(key) : null);
	}

	public void dispose()
	{
		M_log.debug("dispose()");
		// may not be necessary...
		m_refsStore.clear();
	}

	//////////////////////////////////////////////////////////////////////
	//  CacheEventListener methods. Cleanup HashMap of m_refs on eviction.
	//////////////////////////////////////////////////////////////////////

	public void notifyElementEvicted(Ehcache cache, Element element)
	{
		cleanEntityReferences(element.getObjectKey(), element
				.getObjectValue());
	}

	public void notifyElementExpired(Ehcache cache, Element element)
	{
		cleanEntityReferences(element.getObjectKey(), element
				.getObjectValue());
	}

	public void notifyElementPut(Ehcache cache, Element element)
			throws CacheException
	{
		// do nothing...

	}

	public void notifyElementRemoved(Ehcache cache, Element element)
			throws CacheException
	{
		cleanEntityReferences(element.getObjectKey(), element
				.getObjectValue());
	}

	public void notifyElementUpdated(Ehcache cache, Element element)
			throws CacheException
	{
		// do nothing...

	}

	public void notifyRemoveAll(Ehcache cache)
	{
		m_refsStore.clear();
	}

	/**
	 * @see CacheEventListener#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		M_log.debug("clone()");

		// Creates a clone of this listener. This method will only be called by ehcache before a cache is initialized.
		// This may not be possible for listeners after they have been initialized. Implementations should throw CloneNotSupportedException if they do not support clone.
		throw new CloneNotSupportedException(
				"CacheEventListener implementations should throw CloneNotSupportedException if they do not support clone");
	}


	protected class MultiRefCacheEntry extends SoftReference implements Serializable
	{
		private static final long serialVersionUID = -1234567890L;

		/** Set if our payload is supposed to be null. */
		protected boolean m_nullPayload = false;

		/** These are the entity reference strings that this entry is sensitive to. */
		protected Set<String> m_refs = new ConcurrentSkipListSet<String>();

		/**
		 * Construct to cache the payload for the duration.
		 *
		 * @param payload
		 *        The thing to cache.
		 * @param ref
		 *        One entity reference that, if changed, will invalidate this entry.
		 * @param dependRefs
		 *        References that, if the changed, will invalidate this entry.
		 */
		public MultiRefCacheEntry(Object payload, String ref, Collection<String> dependRefs) {
			// put the payload into the soft reference
			super(payload);
			// is it supposed to be null?
			m_nullPayload = (payload == null);
			if (ref != null) m_refs.add(ref);
			if (dependRefs != null) m_refs.addAll(dependRefs);
		}

		/**
		 * @inheritDoc
		 */
		public Set<String> getRefs()
		{
			return m_refs;
		}

		/**
		 * Get the cached object.
		 *
		 * @param key
		 *        The key for this entry (if null, we won't try to refresh if missing)
		 * @return The cached object.
		 */
		public Object getPayload(String key)
		{
			// if we hold null, this is easy
			if (m_nullPayload)
			{
				return null;
			}

			// get the payload
			Object payload = this.get();

			// if it has been garbage collected, and we can, refresh it
			if (payload == null)
			{
				if ((m_refresher != null) && (key != null))
				{
					// ask the refresher for the value
					payload = m_refresher.refresh(key, null, null);

					if (m_memoryService.getCacheLogging())
					{
						M_log.info("cache miss: refreshing: key: " + key + " new payload: " + payload);
					}

					// store this new value
					put(key, payload);
				}
				else
				{
					if (m_memoryService.getCacheLogging())
					{
						M_log.info("cache miss: no refresh: key: " + key);
					}
				}
			}

			return payload;
		}

	} // MultiRefCacheEntry

}
