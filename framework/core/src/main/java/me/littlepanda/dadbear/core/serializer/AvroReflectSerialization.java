package me.littlepanda.dadbear.core.serializer;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class AvroReflectSerialization<T> extends AvroSerialization<T> {
	
	private static ConcurrentHashMap<Class, Schema> schemaHolder = new ConcurrentHashMap<Class, Schema>();

	@Override
	public boolean accept(Class<?> c) {
		return true;
	}

	@Override
	public Schema getSchema(T t) {
		if(schemaHolder.contains(t.getClass())){
			return schemaHolder.get(t.getClass());
		}
		Schema s = ReflectData.get().getSchema(t.getClass());
		schemaHolder.put(t.getClass(), s);
		return s;
	}

	@Override
	public DatumWriter<T> getWriter(Class<T> clazz) {
		return new ReflectDatumWriter<T>(clazz);
	}

	@Override
	public DatumReader<T> getReader(Class<T> clazz) {
		return new ReflectDatumReader<T>(clazz);
	}
	
}
