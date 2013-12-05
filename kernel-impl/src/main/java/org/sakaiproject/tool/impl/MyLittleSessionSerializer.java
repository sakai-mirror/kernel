package org.sakaiproject.tool.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.NonPortableSession;
import org.sakaiproject.tool.api.SessionAttributeListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.SessionStore;

import static org.sakaiproject.tool.impl.SessionMapSerializer.SESSION_MAP_CONTEXT_KEY;
import static org.sakaiproject.tool.impl.SessionMapSerializer.deserializeSessionMap;
import static org.sakaiproject.tool.impl.SessionMapSerializer.serializeSessionMap;
import static org.sakaiproject.tool.impl.MySessionSerializer.MY_SESSION_CONTEXT_KEY;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 12/5/13
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyLittleSessionSerializer extends Serializer<MyLittleSession> {
    private static Log M_log = LogFactory.getLog(MyLittleSessionSerializer.class);

    @Override
    public void write(Kryo kryo, Output output, MyLittleSession myLittleSession) {
        output.writeString(myLittleSession.getId());
        output.writeString(myLittleSession.getContextId());
        String type = (String) kryo.getContext().get(SESSION_MAP_CONTEXT_KEY);

        serializeSessionMap(kryo, output, myLittleSession, "m_attributes", type);

    }

    @Override
    public MyLittleSession read(Kryo kryo, Input input, Class<MyLittleSession> myLittleSessionClass) {
        SessionManager sessionManager = (SessionManager) ComponentManager.get(org.sakaiproject.tool.api.SessionManager.class);

        SessionStore sessionStore = (SessionStore) ComponentManager.get(org.sakaiproject.tool.api.SessionStore.class);

        ThreadLocalManager threadLocalManager = (ThreadLocalManager) ComponentManager.get(org.sakaiproject.thread_local.api.ThreadLocalManager.class);

        SessionAttributeListener sessionListener = (SessionAttributeListener) ComponentManager.get(org.sakaiproject.tool.api.SessionBindingListener.class);

        // create a non portable session object if this is a clustered environment
        NonPortableSession nPS = new MyNonPortableSession();


        String id = input.readString();
        String contextId = input.readString();

        MySession session = (MySession) kryo.getContext().get(MY_SESSION_CONTEXT_KEY);

        MyLittleSession myLittleSession = new MyLittleSession(sessionManager, id, session, contextId, threadLocalManager, sessionListener, sessionStore, nPS );

        String type = (String) kryo.getContext().get(SESSION_MAP_CONTEXT_KEY);

        deserializeSessionMap(kryo, input, myLittleSession, "m_attributes", type);

        return null;
    }
}
