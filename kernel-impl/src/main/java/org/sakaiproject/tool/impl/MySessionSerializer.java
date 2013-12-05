package org.sakaiproject.tool.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.NonPortableSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionAttributeListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.SessionStore;

import java.lang.reflect.Field;
import java.util.Map;

import static org.sakaiproject.tool.impl.SessionMapSerializer.deserializeSessionMap;
import static org.sakaiproject.tool.impl.SessionMapSerializer.serializeSessionMap;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 11/22/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class MySessionSerializer extends Serializer<MySession> {
    private static Log M_log = LogFactory.getLog(MySessionSerializer.class);

    static final public String MY_SESSION_CONTEXT_KEY = "mysession.context.key";

    public MySessionSerializer() {
    }

    @Override
    public void write(Kryo kryo, Output output, MySession session) {
        output.writeString(session.getId());
        output.writeString(session.getUserEid());
        output.writeString(session.getUserId());
        output.writeInt(session.getMaxInactiveInterval());
        serializeSessionMap(kryo, output, session, "m_attributes", "session");
//        serializeSessionMap(kryo, output, session, "m_toolSessions", "toolSession");
//        serializeSessionMap(kryo, output, session, "m_contextSessions", "contextSession");

    }



    @Override
    public MySession read(Kryo kryo, Input input, Class<MySession> aClass) {
        SessionManager sessionManager = (SessionManager) ComponentManager.get(org.sakaiproject.tool.api.SessionManager.class);

        SessionStore sessionStore = (SessionStore) ComponentManager.get(org.sakaiproject.tool.api.SessionStore.class);

        ThreadLocalManager threadLocalManager = (ThreadLocalManager) ComponentManager.get(org.sakaiproject.thread_local.api.ThreadLocalManager.class);

        IdManager idManager = (IdManager) ComponentManager.get(org.sakaiproject.id.api.IdManager.class);

        SessionAttributeListener sessionListener = (SessionAttributeListener) ComponentManager.get(org.sakaiproject.tool.api.SessionBindingListener.class);

        // create a non portable session object if this is a clustered environment
        NonPortableSession nPS = new MyNonPortableSession();
        String id = input.readString();
        String eid = input.readString();
        String userId = input.readString();
        int m_defaultInactiveInterval = input.readInt();


        MySession session = new MySession(sessionManager, id, threadLocalManager, idManager,
                sessionStore, sessionListener, m_defaultInactiveInterval, nPS, new MutableLong(System.currentTimeMillis()));
        session.setUserEid(eid);
        session.setUserId(userId);
        session.setActive();

        kryo.getContext().put(MySessionSerializer.MY_SESSION_CONTEXT_KEY, session);

        deserializeSessionMap(kryo, input, session, "m_attributes", "session");
//        deserializeSessionMap(kryo, input, session, "m_toolSessions", "toolSession");
//        deserializeSessionMap(kryo, input, session, "m_contextSessions", "contextSession");


        return session;
    }


}
