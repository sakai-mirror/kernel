package org.sakaiproject.tool.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.sakaiproject.time.impl.BasicTimeService;
import org.sakaiproject.time.impl.MyTime;


/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 12/4/13
 * Time: 8:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyTimeSerializer extends Serializer<MyTime> {

    public MyTimeSerializer() {
    }

    @Override
    public void write(Kryo kryo, Output output, MyTime myTime) {
        output.writeLong(myTime.getTime());
    }

    @Override
    public MyTime read(Kryo kryo, Input input, Class<MyTime> myTimeClass) {
        org.sakaiproject.component.api.ComponentManager compMgr =
      			org.sakaiproject.component.cover.ComponentManager.getInstance();
        BasicTimeService timeService = (BasicTimeService)compMgr.get("org.sakaiproject.time.api.TimeService");

        MyTime time = new MyTime(timeService, input.readLong());
        time.resolveTransientFields();
        return time;
    }
}
