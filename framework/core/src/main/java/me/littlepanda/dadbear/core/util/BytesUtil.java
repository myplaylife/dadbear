package me.littlepanda.dadbear.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import me.littlepanda.dadbear.core.serializer.Deserializer;
import me.littlepanda.dadbear.core.serializer.SerializationFactory;
import me.littlepanda.dadbear.core.serializer.Serializer;

/**
 * @author 张静波 myplaylife@icloud.com
 *
 */
public class BytesUtil {
	
	private static Log log = LogFactory.getLog(BytesUtil.class);
	
	/**
	 * 将对象实例化为二进制
	 * @param t
	 * @param conf
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static<T> byte[] classToBytes(T t, Configuration conf){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Serializer<T> serializer = (Serializer<T>) SerializationFactory.getFactory(conf).getSerializer(t.getClass());
		try {
			serializer.open(out);
			serializer.serialize(t);
		} catch (IOException e) {
			log.error("error when serialize " + t.getClass().getName() + " to bytes.", e); 
			throw new RuntimeException("error when serialize " + t.getClass().getName() + " to bytes.", e);
		} finally {
			try {
				serializer.close();
			} catch (IOException e) {
				log.error("error when close serializer with " + t.getClass().getName(), e);
				throw new RuntimeException("error when close serializer with " + t.getClass().getName(), e);
			}
		}
		return out.toByteArray();
	}
	
	/**
	 * 将字节数组反序列化为对象
	 * @param bytes
	 * @param conf
	 * @return
	 */
	public static<T> T bytesToClass(byte[] bytes, Configuration conf, Class<T> c){
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		Deserializer<T> deserializer = (Deserializer<T>) SerializationFactory.getFactory(conf).getDeserializer(c);
		try {
			deserializer.open(in);
			T t =  deserializer.deserialize(ReflectionUtils.newInstance(c, conf));
			return t;
		} catch (IOException e) {
			log.error("error when deserialize " + c.getClass().getName() + "from bytes.", e);
			throw new RuntimeException("error when deserialize " + c.getClass().getName() + "from bytes.", e);
		} finally {
			try {
				deserializer.close();
			} catch (IOException e) {
				log.error("error when close deserializer with " + c.getClass().getName(), e);
				throw new RuntimeException("error when close deserializer with " + c.getClass().getName(), e);
			}
		}
	}
	
}
