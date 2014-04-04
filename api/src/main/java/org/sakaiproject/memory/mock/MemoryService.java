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

package org.sakaiproject.memory.mock;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock MemoryService for use in testing
 * Partly functional
 */
public class MemoryService implements org.sakaiproject.memory.api.MemoryService {

    ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

    @Override
    public long getAvailableMemory() {
        return 0;
    }

    @Override
    public void resetCachers() {
        for (Cache cache: caches.values()) {
            cache.clear();
        }
    }

    @Override
    public void evictExpiredMembers() {
        // just a lame implementation
        resetCachers();
    }

    @Override
    public Cache newCache(String cacheName, CacheRefresher refresher, String pattern) {
        Cache c = new org.sakaiproject.memory.mock.Cache(cacheName);
        caches.put(cacheName, c);
        return c;
    }

    @Override
    public Cache newCache(String cacheName, String pattern) {
        return newCache(cacheName, null, pattern);
    }

    @Override
    public Cache newCache(String cacheName) {
        return newCache(cacheName, null, null);
    }

    @Override
    public GenericMultiRefCache newGenericMultiRefCache(String cacheName) {
        return null;
    }

    @Override
    public String getStatus() {
        return null;
    }
}
