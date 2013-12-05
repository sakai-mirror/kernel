package org.sakaiproject.tool.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.sakaiproject.event.impl.BaseUsageSession;
import org.sakaiproject.event.impl.UsageSessionServiceAdaptor;


/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 12/4/13
 * Time: 8:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseUsageSessionSerializer extends Serializer<BaseUsageSession> {

    public BaseUsageSessionSerializer() {
    }

    @Override
    public void write(Kryo kryo, Output output, BaseUsageSession usageSession) {
        output.writeString(usageSession.getId());
    }

    @Override
    public BaseUsageSession read(Kryo kryo, Input input, Class<BaseUsageSession> usageSessionClass) {
		org.sakaiproject.component.api.ComponentManager compMgr =
			org.sakaiproject.component.cover.ComponentManager.getInstance();

        UsageSessionServiceAdaptor usageSessionServiceAdaptor = (UsageSessionServiceAdaptor)compMgr.get("org.sakaiproject.event.api.UsageSessionService");
        return (BaseUsageSession) usageSessionServiceAdaptor.getSession(input.readString());

    }
}
