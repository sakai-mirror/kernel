/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/branches/SAK-18678/api/src/main/java/org/sakaiproject/site/api/Site.java $
 * $Id: Site.java 81275 2010-08-14 09:24:56Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

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
