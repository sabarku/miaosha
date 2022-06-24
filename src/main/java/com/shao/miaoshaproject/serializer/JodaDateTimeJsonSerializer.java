package com.shao.miaoshaproject.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: SJP
 * Date: 2022/6/5
 * Time: 19:52
 *
 * @author sjp
 * Description: * 将String类型的DateTime序列化为json存入redis
 */
public class JodaDateTimeJsonSerializer extends JsonSerializer<DateTime> {
    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toString("yyyy-MM-dd hh:mm:ss"));
    }
}
