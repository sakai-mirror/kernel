/******************************************************************************
 * $URL: $
 * $Id: $
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/

package org.sakaiproject.config.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.config.api.HibernateConfigItem;
import org.sakaiproject.config.api.HibernateConfigItemDao;
import org.sakaiproject.db.api.SqlService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * KNL-1063
 * HibernateConfigItemDaoImpl
 * Implementation for HibernateConfigItemDao
 *
 * @author Earle Nietzel
 *         Created on Mar 8, 2013
 */
public class HibernateConfigItemDaoImpl extends HibernateDaoSupport implements HibernateConfigItemDao {
    private final Log log = LogFactory.getLog(HibernateConfigItemDaoImpl.class);

    private static String SAKAI_CONFIG_ITEM_SQL = "sakai_config_item";
    private SqlService sqlService;

    private boolean autoDdl;

    public void init() {
        if (autoDdl) {
            log.info("init: autoDDL " + SAKAI_CONFIG_ITEM_SQL);
            sqlService.ddl(this.getClass().getClassLoader(), SAKAI_CONFIG_ITEM_SQL);
        }
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void setAutoDdl(boolean autoDdl) {
        this.autoDdl = autoDdl;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#create(org.sakaiproject.config.api.HibernateConfigItem)
     */
    @Override
    public void create(HibernateConfigItem item) {
        if (item != null) {
            getSession().save(item);
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#read(java.lang.Long)
     */
    @Override
    public HibernateConfigItem read(Long id) {
        if (id == null) {
            return null;
        }

        return (HibernateConfigItem) getSession().get(HibernateConfigItem.class, id);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#update(org.sakaiproject.config.api.HibernateConfigItem)
     */
    @Override
    public void update(HibernateConfigItem item) {
        if (item == null) {
            return;
        }

        getSession().update(item);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#delete(org.sakaiproject.config.api.HibernateConfigItem)
     */
    @Override
    public void delete(HibernateConfigItem item) {
        if (item == null) {
            return;
        }

        getSession().delete(item);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#countByNode(java.lang.String)
     */
    public int countByNode(String node) {
        if (node == null) {
            return -1;
        }
        Criteria criteria = getSession().createCriteria(HibernateConfigItem.class)
                                    .setProjection(Projections.rowCount())
                                    .add(Restrictions.eq("node", node));

        return (Integer) criteria.uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#countByNodeAndName(java.lang.String, java.lang.String)
     */
    @Override
    public int countByNodeAndName(String node, String name) {
        if (node == null || name == null) {
            return -1;
        }
        Criteria criteria = getSession().createCriteria(HibernateConfigItem.class)
                                    .setProjection(Projections.rowCount())
                                    .add(Restrictions.eq("node", node))
                                    .add(Restrictions.eq("name", name));

        return (Integer) criteria.uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#saveOrUpdateAll(java.util.List)
     */
    @Override
    public void saveOrUpdateAll(List<HibernateConfigItem> items) {
        if (items == null) {
            return;
        }

        for (HibernateConfigItem item : items) {
            if (item != null) {
                saveOrUpdate(item);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#saveOrUpdate(org.sakaiproject.config.api.HibernateConfigItem)
     */
    @Override
    public void saveOrUpdate(HibernateConfigItem item) {
        if (item == null) {
            return;
        }

        getSession().saveOrUpdate(item);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#findAllByCriteriaByNode(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<HibernateConfigItem> findAllByCriteriaByNode(String node, String name, Boolean defaulted, Boolean registered, Boolean dynamic, Boolean secured) {
        if (node == null) {
            return Collections.emptyList();
        }

        Criteria criteria = getSession().createCriteria(HibernateConfigItem.class);
        criteria.add(Restrictions.eq("node", node));
        if (name != null && name.length() > 0) {
            criteria.add(Restrictions.eq("name", name));
        }
        if (defaulted != null) {
            criteria.add(Restrictions.eq("defaulted", defaulted));
        }
        if (registered != null) {
            criteria.add(Restrictions.eq("registered", registered));
        }
        if (dynamic != null) {
            criteria.add(Restrictions.eq("dynamic", dynamic));
        }
        if (secured != null) {
            criteria.add(Restrictions.eq("secured", secured));
        }
        return criteria.list();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.config.api.HibernateConfigItemDao#findPollOnByNode(java.lang.String, java.util.Date, java.util.Date)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<HibernateConfigItem> findPollOnByNode(String node, Date onOrAfter, Date before) {
        Criteria criteria = getSession().createCriteria(HibernateConfigItem.class)
                                    .add(Restrictions.eq("node", node));
        if (onOrAfter == null && before == null) {
            criteria.add(Restrictions.isNotNull("pollOn"));
        } else {
            if (onOrAfter != null) {
                criteria.add(Restrictions.ge("pollOn", onOrAfter));
            }
            if (before != null) {
                criteria.add(Restrictions.lt("pollOn", before));
            }
        }
        return criteria.list();
    }
}

