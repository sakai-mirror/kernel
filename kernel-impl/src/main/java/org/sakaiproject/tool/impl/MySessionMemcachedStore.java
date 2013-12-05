package org.sakaiproject.tool.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.minlog.Log;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.impl.BaseUsageSession;
import org.sakaiproject.time.impl.MyTime;
import org.sakaiproject.user.impl.BasePreferences;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;


/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 12/3/13
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class MySessionMemcachedStore {
    private static org.apache.commons.logging.Log M_log = LogFactory.getLog(MySessionMemcachedStore.class);
    private MemcachedClient memcachedClient;
    private ServerConfigurationService serverConfigurationService;

    /**
     * kryo is not thread safe, a new instance is needed per thread.
     * may want to consider pushing into threadlocal if instantiating this thing
     * over and over proofs to be slow.
     * @return
     */
    protected Kryo getSerializer() {
        Kryo kryo = new Kryo();
        kryo.register(ArrayList.class);
        kryo.register(LinkedList.class);
        kryo.register(HashSet.class);
        kryo.register(HashMap.class);
        kryo.register(Currency.class, new DefaultSerializers.CurrencySerializer());
        kryo.register(StringBuffer.class, new DefaultSerializers.StringBufferSerializer());
        kryo.register(StringBuilder.class, new DefaultSerializers.StringBuilderSerializer());
        kryo.register(Collections.EMPTY_LIST.getClass(), new DefaultSerializers.CollectionsEmptyListSerializer());
        kryo.register(Collections.EMPTY_MAP.getClass(), new DefaultSerializers.CollectionsEmptyMapSerializer());
        kryo.register(Collections.EMPTY_SET.getClass(), new DefaultSerializers.CollectionsEmptySetSerializer());
        kryo.register(Collections.singletonList("").getClass(), new DefaultSerializers.CollectionsSingletonListSerializer());
        kryo.register(Collections.singleton("").getClass(), new DefaultSerializers.CollectionsSingletonSetSerializer());
        kryo.register(Collections.singletonMap("", "").getClass(), new DefaultSerializers.CollectionsSingletonMapSerializer());
        kryo.register(Class.class, new DefaultSerializers.ClassSerializer());
        kryo.register(BigDecimal.class, new DefaultSerializers.BigDecimalSerializer());
        kryo.register(BigInteger.class, new DefaultSerializers.BigIntegerSerializer());
        kryo.register(MyTime.class, new MyTimeSerializer());
        kryo.register(BaseUsageSession.class, new BaseUsageSessionSerializer());
        kryo.addDefaultSerializer(Locale.class, JavaSerializer.class);
        kryo.register(MySession.class, new MySessionSerializer());
        kryo.addDefaultSerializer(BasePreferences.class, JavaSerializer.class);
        kryo.register(MyLittleSession.class, new MyLittleSessionSerializer());
        return kryo;
    }

    public void init() {
        if (M_log.isDebugEnabled()) {
            Log.DEBUG();
        }
    }

    public MySession findSession(final String sessionId) {
        long start = System.currentTimeMillis();

        Object object = memcachedClient.get(sessionId);
        if (object != null) {
            M_log.info("retrieved session:" + sessionId + " from memcached in " + (System.currentTimeMillis() -  start) + " ms, attempting to deserialize");
            try {
                return deserialize((byte[]) object);
            } catch (Exception e) {
                M_log.error("Failure deserializing session: " + sessionId + " error: " + e.getMessage(), e);
            }
        }
        return null;

    }

    public MySession deserialize(final byte[] data) {
        long start = System.currentTimeMillis();
        MySession session =  getSerializer().readObject(new Input(data), MySession.class);
        M_log.debug("serialized session in " + (System.currentTimeMillis() -  start) + " ms");
        return session;
    }


    public byte[] serialize(final MySession object) {
        long start = System.currentTimeMillis();
        Output output = new Output(4096);
        getSerializer().writeObject(output, object, new MySessionSerializer());
        output.flush();
        M_log.debug("serialized session in " + (System.currentTimeMillis() -  start) + " ms");
        return output.getBuffer();
    }

    public void storeSession(final MySession session) {
        M_log.info("storing session:" + session.getId() + " in memcached.");
        long start = System.currentTimeMillis();

        memcachedClient.delete(session.getId());

        memcachedClient.add(session.getId(),
                serverConfigurationService.getInt("session.memcached.expiration", 0),
                serialize(session));
        M_log.debug("stored session to memcached in " + (System.currentTimeMillis() -  start) + " ms");

    }


    public void setMemcachedClient(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }
}
