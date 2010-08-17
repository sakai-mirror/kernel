package org.sakaiproject.messagebundle.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 16, 2010
 * Time: 12:37:31 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MessageBundleService {
    public int SORT_ORDER_ASCENDING = 1;
    public int SORT_ORDER_DESCENDING = 2;
    public int SORT_FIELD_MODULE = 1;
    public int SORT_FIELD_PROPERTY = 2;
    public int SORT_FIELD_ID = 3;
    public int SORT_FIELD_LOCALE = 4;
    public int SORT_FIELD_BASENAME = 5;


    public List search(String search, String module, String baseName, String locale);
    public MessageBundleProperty getMessageBundleProperty(long id);
    public void updateMessageBundleProperty(MessageBundleProperty mbp);

    public List getModifiedProperties(int sortOrder, int sortField, int startingIndex, int pageSize);

    public List getLocales();

    public int getModifiedPropertiesCount();

    public List getAllProperties(String locale, String module);

    public int revertAll(String locale);

    public int importProperties(List<MessageBundleProperty> properties);

    public List getAllModuleNames();

    public List getAllBaseNames();

    public void revert(MessageBundleProperty mbp);

    public int getSearchCount(String searchQuery, String module, String baseName, String locale);

    public void saveOrUpdate(String baseName, String moduleName, ResourceBundle newBundle, Locale loc);

    public Map getBundle(String baseName, String moduleName, Locale loc);
}
