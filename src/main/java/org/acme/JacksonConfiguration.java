package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import net.fortuna.ical4j.model.Calendar;
import org.mnode.ical4j.serializer.JCalMapper;
import org.mnode.ical4j.serializer.JCalSerializer;

@Singleton
public class JacksonConfiguration implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper mapper) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Calendar.class, new CalendarStringArraySerializer());
        simpleModule.addDeserializer(Calendar.class, new CalendarStringArrayDeserializer());
//aus der lib
//        simpleModule.addSerializer(Calendar.class, new  JCalSerializer(Calendar.class));
//        simpleModule.addDeserializer(Calendar.class, new JCalMapper(Calendar.class));
        mapper.registerModule(simpleModule);
    }


}
