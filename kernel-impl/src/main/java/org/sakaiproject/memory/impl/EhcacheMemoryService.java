/******************************************************************************
 * $URL: https://source.sakaiproject.org/svn/master/trunk/header.java $
 * $Id: header.java 307632 2014-03-31 15:29:37Z azeckoski@unicon.net $
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

package org.sakaiproject.memory.impl;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.GenericMultiRefCache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.util.CacheInitializer;

import java.util.*;

/**
 * Ehcache based implementation of the MemoryService API which is meant to be friendly to distributed cache management
 *
 * This is designed to align with JSR-107
 * https://github.com/jsr107/jsr107spec/blob/master/src/main/java/javax/cache/Cache.java
 * See https://jira.sakaiproject.org/browse/KNL-1162
 * Send questions to Aaron Zeckoski
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public class EhcacheMemoryService implements MemoryService {

    final Log log = LogFactory.getLog(EhcacheMemoryService.class);

    CacheManager cacheManager;
    /**
     * MUST be lazy loaded to avoid cyclical dependency issues
     * i.e. NEVER use this variable directly, use the #getSecurityService() method instead
     */
    SecurityService securityService;
    ServerConfigurationService serverConfigurationService;

    public EhcacheMemoryService() {}

    public EhcacheMemoryService(CacheManager cacheManager, ServerConfigurationService serverConfigurationService) {
        assert cacheManager != null;
        assert serverConfigurationService != null;
        this.cacheManager = cacheManager;
        this.serverConfigurationService = serverConfigurationService;
    }

    /**
     * Service INIT
     */
    public void init() {
        if (cacheManager == null) {
            throw new IllegalStateException("init(): Ehcache CacheManager is null!");
        }
        log.info("INIT: " + cacheManager.getStatus() + ", caches: " + Arrays.asList(cacheManager.getCacheNames()));
    }

    /**
     * Service SHUTDOWN
     */
    public void destroy() {
        try {
            cacheManager.clearAll();
            cacheManager.removalAll(); // removeAllCaches()
            cacheManager.shutdown();
        } catch (CacheException e) {
            // NOTHING TO DO HERE
            log.warn("destroy() cache shutdown failure: "+e);
        }
        cacheManager = null; // release
        log.info("SHUTDOWN");
    }

    @Override
    public ClassLoader getClassLoader() {
        return EhcacheMemoryService.class.getClassLoader();
    }

    @Override
    public Properties getProperties() {
        Configuration ec = cacheManager.getConfiguration();
        Properties p = new Properties();
        p.put("name", ec.getName());
        p.put("source", ec.getConfigurationSource().toString());
        p.put("timeoutSeconds", ec.getDefaultTransactionTimeoutInSeconds());
        p.put("maxBytesDisk", ec.getMaxBytesLocalDisk());
        p.put("maxBytesHeap", ec.getMaxBytesLocalHeap());
        p.put("maxDepth", ec.getSizeOfPolicyConfiguration().getMaxDepth());
        p.put("defaultCacheMaxEntries", ec.getDefaultCacheConfiguration().getMaxEntriesLocalHeap());
        p.put("defaultCacheTimeToIdleSecs", ec.getDefaultCacheConfiguration().getTimeToIdleSeconds());
        p.put("defaultCacheTimeToLiveSecs", ec.getDefaultCacheConfiguration().getTimeToLiveSeconds());
        p.put("defaultCacheEternal", ec.getDefaultCacheConfiguration().isEternal());
        return p;
    }

    @Override
    public <C extends org.sakaiproject.memory.api.Configuration> Cache createCache(String cacheName, C configuration) {
        return new EhcacheCache(makeEhcache(cacheName, configuration));
    }

    @Override
    public Cache getCache(String cacheName) {
        return new EhcacheCache(makeEhcache(cacheName, null));
    }

    @Override
    public Iterable<String> getCacheNames() {
        if (this.cacheManager != null) {
            String[] names = cacheManager.getCacheNames();
            return Arrays.asList(names);
        } else {
            return new ArrayList<String>(0);
        }
    }

    @Override
    public void destroyCache(String cacheName) {
        if (this.cacheManager != null) {
            this.cacheManager.removeCache(cacheName);
        }
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        //noinspection unchecked
        return (T) cacheManager;
    }

    @Override
    public long getAvailableMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    @Override
    public void resetCachers() {
        if (!getSecurityService().isSuperUser()) {
            throw new SecurityException("Only super admin can reset cachers, current user not super admin");
        }
        if (this.cacheManager != null) {
            this.cacheManager.clearAll();
        }
    }

    @Override
    public void evictExpiredMembers() {
        if (!getSecurityService().isSuperUser()) {
            throw new SecurityException("Only super admin can evict caches, current user not super admin");
        }
        if (this.cacheManager != null) {
            String[] allCacheNames = cacheManager.getCacheNames();
            for (String cacheName : allCacheNames) {
                Ehcache cache = cacheManager.getCache(cacheName);
                cache.evictExpiredElements();
            }
        }
    }

    @Override
    public Cache newCache(String cacheName) {
        return getCache(cacheName);
    }

    @Override
    public String getStatus() {
        // MIRRORS the OLD status report
        final StringBuilder buf = new StringBuilder();
        buf.append("** Memory report\n");
        buf.append("freeMemory: ").append(Runtime.getRuntime().freeMemory());
        buf.append(" totalMemory: "); buf.append(Runtime.getRuntime().totalMemory());
        buf.append(" maxMemory: "); buf.append(Runtime.getRuntime().maxMemory());
        buf.append("\n\n");

        String[] allCacheNames = cacheManager.getCacheNames();
        Arrays.sort(allCacheNames);
        ArrayList<Ehcache> caches = new ArrayList<Ehcache>(allCacheNames.length);
        for (String cacheName : allCacheNames) {
            Ehcache cache = cacheManager.getCache(cacheName);
            caches.add(cache);
        }

        // summary
        for (Ehcache cache : caches) {
            final long hits = cache.getStatistics().getCacheHits();
            final long misses = cache.getStatistics().getCacheMisses();
            final long total = hits + misses;
            final long hitRatio = ((total > 0) ? ((100l * hits) / total) : 0);
            // Even when we're not collecting statistics ehcache knows how many objects are in the cache
            buf.append(cache.getName()).append(": ").append(" count:").append(cache.getStatistics().getObjectCount());
            if (cache.isStatisticsEnabled()) {
                buf.append(" hits:").append(hits).append(" misses:").append(misses).append(" hit%:").append(hitRatio);
            } else {
                buf.append(" NO statistics (not enabled for cache)");
            }
            buf.append("\n");
        }

        // extended report
        // TODO probably should remove this
        buf.append("\n** Extended Cache Report\n");
        for (Ehcache cache : caches) {
            buf.append(cache.toString());
            buf.append("\n");
        }

        // config report
        buf.append("\n** Current Cache Configurations\n");
        // determine whether to use old or new form keys
        boolean legacyKeys = true; // set true for a 2.9/BasicMemoryService compatible set of keys
        String maxKey = "maxEntries";
        String ttlKey = "timeToLive";
        String ttiKey = "timeToIdle";
        String eteKey = "eternal";
        //noinspection ConstantConditions
        if (legacyKeys) {
            maxKey = "maxElementsInMemory";
            ttlKey = "timeToLiveSeconds";
            ttiKey = "timeToIdleSeconds";
        }
        // DEFAULT cache config
        CacheConfiguration defaults = cacheManager.getConfiguration().getDefaultCacheConfiguration();
        long maxEntriesDefault = defaults.getMaxEntriesLocalHeap();
        long ttlSecsDefault = defaults.getTimeToLiveSeconds();
        long ttiSecsDefault = defaults.getTimeToIdleSeconds();
        boolean eternalDefault = defaults.isEternal();
        buf.append("# DEFAULTS: ").append(maxKey).append("=").append(maxEntriesDefault).append(",").append(ttlKey).append("=").append(ttlSecsDefault).append(",").append(ttiKey).append("=").append(ttiSecsDefault).append(",").append(eteKey).append("=").append(eternalDefault).append("\n");
        // new: timeToLive=600,timeToIdle=360,maxEntries=5000,eternal=false
        // old: timeToLiveSeconds=3600,timeToIdleSeconds=900,maxElementsInMemory=20000,eternal=false
        for (Ehcache cache : caches) {
            long maxEntries = cache.getCacheConfiguration().getMaxEntriesLocalHeap();
            long ttlSecs = cache.getCacheConfiguration().getTimeToLiveSeconds();
            long ttiSecs = cache.getCacheConfiguration().getTimeToIdleSeconds();
            boolean eternal = cache.getCacheConfiguration().isEternal();
            if (maxEntries == maxEntriesDefault && ttlSecs == ttlSecsDefault && ttiSecs == ttiSecsDefault && eternal == eternalDefault) {
                // Cache ONLY uses the defaults
                buf.append("# memory.").append(cache.getName()).append(" *ALL DEFAULTS*\n");
            } else {
                // NOT only defaults cache, show the settings that differ from the defaults
                buf.append("memory.").append(cache.getName()).append("=").append(maxKey).append("=").append(maxEntries);
                if (ttlSecs != ttlSecsDefault) {
                    buf.append(",").append(ttlKey).append("=").append(ttlSecs);
                }
                if (ttiSecs != ttiSecsDefault) {
                    buf.append(",").append(ttiKey).append("=").append(ttiSecs);
                }
                if (eternal != eternalDefault) {
                    buf.append(",").append(eteKey).append("=").append(eternal);
                }
                buf.append("\n");
                // TODO remove the overflow to disk check
                //noinspection deprecation
                if (cache.getCacheConfiguration().isOverflowToDisk()) {
                    // overflowToDisk. maxEntriesLocalDisk
                    buf.append("# NOTE: ").append(cache.getName()).append(" is configured for Overflow(disk), ").append(cache.getCacheConfiguration().getMaxEntriesLocalDisk()).append(" entries\n");
                }
            }
        }

        final String rv = buf.toString();
        log.info(rv);

        return rv;
    }

    // DEPRECATED METHODS BELOW

    @Override
    @SuppressWarnings("deprecation")
    public Cache newCache(String cacheName, CacheRefresher refresher, String pattern) {
        return getCache(cacheName);
    }

    @Override
    public Cache newCache(String cacheName, String pattern) {
        log.warn("Creating pattern Cache("+cacheName+"), pattern is not supported in the distributed MemoryService implementation, the pattern update event entry removal will not happen!");
        return getCache(cacheName);
    }

    @SuppressWarnings("deprecation")
    @Override
    public GenericMultiRefCache newGenericMultiRefCache(String cacheName) {
        log.warn("Creating MultiRefCache("+cacheName+"), GenericMultiRefCache is not supported in the distributed MemoryService implementation, the refs handling will do nothing!");
        return new EhcacheGenericMultiRefCache(makeEhcache(cacheName, null));
    }

    /**
     * @param cacheName the name of the cache
     * @param configuration [OPTIONAL] a config to use when building the cache, if null then use default methods to create cache
     * @return an Ehcache
     */
    private Ehcache makeEhcache(String cacheName, org.sakaiproject.memory.api.Configuration configuration) {
        String name = cacheName;
        if (name == null || "".equals(name)) {
            name = "DefaultCache" + UUID.randomUUID().toString();
        }

        Ehcache cache;
        // fetch an existing cache first if possible
        if ( cacheManager.cacheExists(name) ) {
            cache = cacheManager.getEhcache(name);
        } else {
            // create a new defaulted cache
            cacheManager.addCache(name);
            cache = cacheManager.getEhcache(name);
        }
        // apply config to the cache
        if (configuration != null) {
            if (configuration.getMaxEntries() >= 0) {
                cache.getCacheConfiguration().setMaxEntriesLocalHeap(configuration.getMaxEntries());
            }
            if (configuration.isEternal()) {
                cache.getCacheConfiguration().setTimeToLiveSeconds(0l);
                cache.getCacheConfiguration().setTimeToIdleSeconds(0l);
            } else {
                if (configuration.getTimeToLiveSeconds() >= 0) {
                    cache.getCacheConfiguration().setTimeToLiveSeconds(configuration.getTimeToLiveSeconds());
                }
                if (configuration.getTimeToIdleSeconds() >= 0) {
                    cache.getCacheConfiguration().setTimeToIdleSeconds(configuration.getTimeToIdleSeconds());
                }
            }
            cache.getCacheConfiguration().setEternal(configuration.isEternal());
            cache.setStatisticsEnabled(configuration.isStatisticsEnabled());
        }

        // warn people if they are using an old config style
        if (serverConfigurationService.getString(name) == null) {
            log.warn("Old cache configuration for cache ("+name+"), must be changed to memory."+name+" or it will be ignored");
        }
        // load the ehcache config from the Sakai config service
        String config = serverConfigurationService.getString("memory."+ name);
        if (StringUtils.isNotBlank(config)) {
            log.info("Configuring cache (" + name + "): " + config);
            try {
                // ehcache specific code here - no exceptions thrown
                new CacheInitializer().configure(config).initialize(cache.getCacheConfiguration());
            } catch (Exception e) {
                // nothing to do here but proceed
                log.error("Failure configuring cache (" + name + "): " + config + " :: "+e, e);
            }
        }

        /* KNL-532 - Upgraded Ehcache 2.5.1 (2.1.0+) defaults to no stats collection.
         * We may choose to allow configuration per-cache for performance tuning.
         * For now, we default everything to on, while this property allows a system-wide override.
         */
        boolean enabled = true;
        if (serverConfigurationService != null) {
            enabled = !serverConfigurationService.getBoolean("memory.cache.statistics.force.disabled", false);
        }
        cache.setStatisticsEnabled(enabled);

        return cache;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    SecurityService getSecurityService() {
        // has to be lazy
        if (securityService == null) {
            securityService = (SecurityService) ComponentManager.get(SecurityService.class);
        }
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    /**
     * Temporary only
     */
    @SuppressWarnings("deprecation")
    private class EhcacheGenericMultiRefCache extends EhcacheCache implements GenericMultiRefCache {
        public EhcacheGenericMultiRefCache(Ehcache cache) {
            super(cache);
        }
        @Override
        public void put(String key, Object payload, String ref, Collection<String> dependRefs) {
            super.put(key, payload);
        }
    }
}