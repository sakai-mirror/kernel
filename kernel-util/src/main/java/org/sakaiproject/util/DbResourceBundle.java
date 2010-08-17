package org.sakaiproject.util;

import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.component.cover.ComponentManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: May 7, 2009
 * Time: 11:03:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class DbResourceBundle extends ResourceBundle {
    protected Locale locale;
    protected Map entries = new HashMap();
    protected String baseName;

    protected static Log log = LogFactory.getLog(DbResourceBundle.class);

    private DbResourceBundle(String baseName, Locale locale) {
        this.baseName = baseName;
        this.locale = locale;
    }

    static public ResourceBundle addResourceBundle(String baseName, Locale locale, ClassLoader classLoader) {
        DbResourceBundle newBundle = new DbResourceBundle(baseName, locale);
        String context =  (String)ThreadLocalManager.get(org.sakaiproject.util.RequestFilter.CURRENT_CONTEXT);
		try
		{
            if (context != null) {
                Map bundleValues = getMessageBundleService().getBundle(baseName, context, locale);
                Iterator bundleValuesIter = bundleValues.keySet().iterator();
                while (bundleValuesIter.hasNext()) {
                    String key = (String) bundleValuesIter.next();
                    String value = (String) bundleValues.get(key);
                    newBundle.addProperty(key, value);
                }
            }
            ResourceBundle loadedBundle = null;
            try
            {
                if ( classLoader == null )
                    loadedBundle = ResourceBundle.getBundle(baseName, locale);
                else
                    loadedBundle = ResourceBundle.getBundle(baseName, locale, classLoader);
            }
            catch (NullPointerException e)
            {
            } // ignore


            Enumeration<String> keys = loadedBundle.getKeys();
            while (keys.hasMoreElements()){
                String key = keys.nextElement();
                if (newBundle.handleGetObject(key) == null) {
                    newBundle.addProperty(key, loadedBundle.getString(key));
                }
            }
        }
		catch (Exception e)
		{
            log.error("problem loading bundle: " +baseName + " locale: " + locale.toString() + " " + e.getMessage());
		}

		return newBundle;
    }

    protected void addProperty(String name, String value) {
        entries.put(name, value);
    }

    public Locale getLocale() {
        return locale;
    }

    protected Object handleGetObject(String key) {
        return entries.get(key);
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(entries.keySet());
    }

    public static void indexResourceBundle(String baseName, ResourceBundle newBundle, Locale loc, ClassLoader classLoader) {
        String context =  (String)ThreadLocalManager.get(org.sakaiproject.util.RequestFilter.CURRENT_CONTEXT);
        if (context == null) return;
        MessageBundleService messageBundleService = getMessageBundleService();
        messageBundleService.saveOrUpdate(baseName, context, newBundle, loc);

    }

    private static MessageBundleService getMessageBundleService() {
        return (MessageBundleService) ComponentManager.get(MessageBundleService.class.getName());
    }


}
