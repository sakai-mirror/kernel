package org.sakaiproject.tool.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 12/5/13
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class SessionMapSerializer {
    private static Log M_log = LogFactory.getLog(SessionMapSerializer.class);
    static final public String SESSION_MAP_CONTEXT_KEY = "session.map.context.key";

    static public void serializeSessionMap(Kryo kryo, Output output, Object session, String name, String type) {
        Field attributesField = null;
        kryo.getContext().put(SESSION_MAP_CONTEXT_KEY, type);

        try {
            attributesField = session.getClass().getDeclaredField(name);
            attributesField.setAccessible(true);
            Map<String, Object> attributes = (Map<String, Object>) attributesField.get(session);
            output.writeInt(attributes.size());

            M_log.info("found " + attributes.size() + " " + type + " attributes to serialize");

            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                output.writeString(entry.getKey());
                try {
                    kryo.writeClassAndObject(output, entry.getValue());
                    M_log.info("serialized " + type + " attribute with name:" + entry.getKey() +
                            " type:" + entry.getValue().getClass().getName());

                } catch (Exception e) {
                    M_log.error("couldn't serialize " + type + " attribute with name:" + entry.getKey() +
                            " and type:" + entry.getValue().getClass().getName() + " error:" + e.getMessage(), e);
                    //kryo.writeObjectOrNull(output, null, entry.getValue().getClass());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    static public void deserializeSessionMap(Kryo kryo, Input input, Object session, String name, String type) {
        int attributesLength = input.readInt();
        kryo.getContext().put(SESSION_MAP_CONTEXT_KEY, type);
        for (int i = 0; i < attributesLength; i++) {
            String paramName = input.readString();
            M_log.info("deserializing " + type + " attribute with name:" + paramName);
            //ClassLoader originalClassloader = this.getClass().getClassLoader();

            try {
    /*
                    if ("org.sakaiproject.portal.pluto.PORTLET_STATE".equals(paramName)) {
                        Thread.currentThread().setContextClassLoader(
                                ComponentManager.get("org.sakaiproject.portal.api.PortalService").getClass().getClassLoader());
                    }
    */
                Object value = kryo.readClassAndObject(input);
                M_log.info(type + " attribute type:" + value.getClass().getName());
                Field attributesField = MySession.class.getDeclaredField(name);
                attributesField.setAccessible(true);
                Map<String, Object> attributes = (Map<String, Object>) attributesField.get(session);

                attributes.put(paramName, value);

            } catch (Exception e) {
                // rather than just blow up lets continue on our merry way and log the issue
                // there are bound to be things put into the session that cause issues
                // we have both classloading and serialization issues to contend with here
                M_log.error("Failed to deserialize " + type + " attribute: " + paramName + " error: " + e.getMessage(), e);
            } finally {
                //Thread.currentThread().setContextClassLoader(originalClassloader);
            }
        }
    }
}
