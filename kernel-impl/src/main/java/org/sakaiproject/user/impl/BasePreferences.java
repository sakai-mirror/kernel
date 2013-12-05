package org.sakaiproject.user.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 11/21/13
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasePreferences implements PreferencesEdit, SessionBindingListener, Serializable
{
    /** Our log (commons). */
   	private static Log M_log = LogFactory.getLog(BasePreferences.class);

    private transient BasePreferencesService preferencesService;

	/** The user id. */
	protected String m_id = null;

	/** The properties. */
	protected ResourcePropertiesEdit m_properties = null;

	/** The sets of keyed ResourceProperties. */
	protected Map m_props = null;

	/**
	 * Construct.
	 *
	 * @param id
	 *        The user id.
	 */
	public BasePreferences(String id)
	{
		m_id = id;

		// setup for properties
		ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
		m_properties = props;

		m_props = new Hashtable();

		// if the id is not null (a new user, rather than a reconstruction)
		// and not the anon (id == "") user,
		// add the automatic (live) properties
		// %%% if ((m_id != null) && (m_id.length() > 0)) addLiveProperties(props);
	}

	/**
	 * Construct from another Preferences object.
	 *
	 * @param user
	 *        The user object to use for values.
	 */
	public BasePreferences(Preferences prefs)
	{
		setAll(prefs);
	}

	/**
	 * Construct from information in XML.
	 *
	 * @param el
	 *        The XML DOM Element definining the user.
	 */
	public BasePreferences(Element el)
	{
		// setup for properties
		m_properties = new BaseResourcePropertiesEdit();

		m_props = new Hashtable();

		m_id = el.getAttribute("id");

		// the children (properties)
		NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for properties
			if (element.getTagName().equals("properties"))
			{
				// re-create properties
				m_properties = new BaseResourcePropertiesEdit(element);
			}

			// look for a set of preferences
			else if (element.getTagName().equals("prefs"))
			{
				String key = element.getAttribute("key");

				// convert old pre Sakai 2.2 keys to new values (copied here to avoid build dependencies - WATCH OUT! -ggolden)
				if (key.startsWith(NotificationService.PREFS_TYPE))
				{
					if (key.endsWith("AnnouncementService"))
					{
						// matches AnnouncementService.APPLICATION_ID
						key = NotificationService.PREFS_TYPE + "sakai:announcement";
					}
					else if (key.endsWith("ContentHostingService"))
					{
						// matches ContentHostingService.APPLICATION_ID
						key = NotificationService.PREFS_TYPE + "sakai:content";
					}
					else if (key.endsWith("MailArchiveService"))
					{
						// matches MailArchiveService.APPLICATION_ID
						key = NotificationService.PREFS_TYPE + "sakai:mailarchive";
					}
					else if (key.endsWith("SyllabusService"))
					{
						// matches SyllabusService.APPLICATION_ID
						key = NotificationService.PREFS_TYPE + "sakai:syllabus";
					}
				}
				else if (key.endsWith("TimeService"))
				{
					// matches TimeService.APPLICATION_ID
					key = "sakai:time";
				}
				else if (key.endsWith("sitenav"))
				{
					// matches Charon portal's value
					key = "sakai:portal:sitenav";
				}
				else if (key.endsWith("ResourceLoader"))
				{
					// matches ResourceLoader.APPLICATION_ID
					key = BasePreferencesService.LOCALE_PREFERENCE_KEY;
				}

				BaseResourcePropertiesEdit props = null;

				// the children (properties)
				NodeList kids = element.getChildNodes();
				final int len = kids.getLength();
				for (int i2 = 0; i2 < len; i2++)
				{
					Node kid = kids.item(i2);
					if (kid.getNodeType() != Node.ELEMENT_NODE) continue;
					Element k = (Element) kid;

					// look for properties
					if (k.getTagName().equals("properties"))
					{
						props = new BaseResourcePropertiesEdit(k);
					}
				}

				if (props != null)
				{
					m_props.put(key, props);
				}
			}
		}
	}

	/**
	 * Take all values from this object.
	 *
	 * @param user
	 *        The user object to take values from.
	 */
	protected void setAll(Preferences prefs)
	{
		m_id = prefs.getId();

		m_properties = new BaseResourcePropertiesEdit();
		m_properties.addAll(prefs.getProperties());

		// %%% is this deep enough? -ggolden
		m_props = new Hashtable();
		m_props.putAll(((BasePreferences) prefs).m_props);
	}

	/**
	 * @inheritDoc
	 */
	public Element toXml(Document doc, Stack stack)
	{
		Element prefs = doc.createElement("preferences");

		if (stack.isEmpty())
		{
			doc.appendChild(prefs);
		}
		else
		{
			((Element) stack.peek()).appendChild(prefs);
		}

		stack.push(prefs);

		prefs.setAttribute("id", getId());

		// properties
		m_properties.toXml(doc, stack);

		// for each keyed property
		for (Iterator it = m_props.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			ResourceProperties properties = (ResourceProperties) entry.getValue();

			// if the props are empty, skip it
			if (properties.getPropertyNames().next() == null) continue;

			Element props = doc.createElement("prefs");
			prefs.appendChild(props);
			props.setAttribute("key", key);
			stack.push(props);
			properties.toXml(doc, stack);
			stack.pop();
		}
		stack.pop();

		return prefs;
	}

	/**
	 * @inheritDoc
	 */
	public String getId()
	{
		if (m_id == null) return "";
		return m_id;
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl()
	{
		return getPreferencesService().getAccessPoint(false) + m_id;
	}

	/**
	 * @inheritDoc
	 */
	public String getReference()
	{
		return getPreferencesService().preferencesReference(m_id);
	}

	/**
	 * @inheritDoc
	 */
	public String getReference(String rootProperty)
	{
		return getReference();
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl(String rootProperty)
	{
		return getUrl();
	}

	/**
	 * @inheritDoc
	 */
	public ResourceProperties getProperties()
	{
		return m_properties;
	}

	/**
	 * @inheritDoc
	 */
	public ResourceProperties getProperties(String key)
	{
		ResourceProperties rv = (ResourceProperties) m_props.get(key);
		if (rv == null)
		{
			// new, throwaway empty one
			rv = new BaseResourceProperties();
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public Collection getKeys()
	{
		return m_props.keySet();
	}

	/**
	 * @inheritDoc
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Preferences)) return false;
		return ((Preferences) obj).getId().equals(getId());
	}

	/**
	 * @inheritDoc
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}

	/**
	 * @inheritDoc
	 */
	public int compareTo(Object obj)
	{
		if (!(obj instanceof Preferences)) throw new ClassCastException();

		// if the object are the same, say so
		if (obj == this) return 0;

		// sort based on (unique) id
		int compare = getId().compareTo(((Preferences) obj).getId());

		return compare;
	}

	/******************************************************************************************************************************************************************************************************************************************************
	 * Edit implementation
	 *****************************************************************************************************************************************************************************************************************************************************/

	/** The event code for this edit. */
	protected String m_event = null;

	/** Active flag. */
	protected boolean m_active = false;

	/**
	 * @inheritDoc
	 */
	public ResourcePropertiesEdit getPropertiesEdit(String key)
	{
		synchronized (m_props)
		{
			ResourcePropertiesEdit rv = (ResourcePropertiesEdit) m_props.get(key);
			if (rv == null)
			{
				// new one saved in the map
				rv = new BaseResourcePropertiesEdit();
				m_props.put(key, rv);
			}

			return rv;
		}
	}

	/**
	 * Clean up.
	 */
	protected void finalize()
	{
		// catch the case where an edit was made but never resolved
		if (m_active)
		{
            getPreferencesService().cancel(this);
		}
	}

	/**
	 * Take all values from this object.
	 *
	 * @param user
	 *        The user object to take values from.
	 */
	protected void set(Preferences prefs)
	{
		setAll(prefs);
	}

	/**
	 * Access the event code for this edit.
	 *
	 * @return The event code for this edit.
	 */
	protected String getEvent()
	{
		return m_event;
	}

	/**
	 * Set the event code for this edit.
	 *
	 * @param event
	 *        The event code for this edit.
	 */
	protected void setEvent(String event)
	{
		m_event = event;
	}

	/**
	 * @inheritDoc
	 */
	public ResourcePropertiesEdit getPropertiesEdit()
	{
		return m_properties;
	}

	/**
	 * Enable editing.
	 */
	protected void activate()
	{
		m_active = true;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isActiveEdit()
	{
		return m_active;
	}

	/**
	 * Close the edit object - it cannot be used after this.
	 */
	protected void closeEdit()
	{
		m_active = false;
	}

	/******************************************************************************************************************************************************************************************************************************************************
	 * SessionBindingListener implementation
	 *****************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public void valueBound(SessionBindingEvent event)
	{
	}

	/**
	 * @inheritDoc
	 */
	public void valueUnbound(SessionBindingEvent event)
	{
		if (M_log.isDebugEnabled()) M_log.debug("valueUnbound()");

		// catch the case where an edit was made but never resolved
		if (m_active)
		{
            getPreferencesService().cancel(this);
		}
	}

    public BasePreferencesService getPreferencesService() {
        if (preferencesService == null) {
            preferencesService = (BasePreferencesService) ComponentManager.get(PreferencesService.class);
        }
        return preferencesService;
    }

}
