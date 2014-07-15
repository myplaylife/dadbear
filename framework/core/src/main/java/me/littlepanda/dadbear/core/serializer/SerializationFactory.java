/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.littlepanda.dadbear.core.serializer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import me.littlepanda.dadbear.core.config.CommonConfigurationKeys;
import me.littlepanda.dadbear.core.config.Configured;
import me.littlepanda.dadbear.core.util.ReflectionUtils;

/**
 * <p>
 * A factory for {@link Serialization}s.
 * </p>
 */
public class SerializationFactory extends Configured {
  
  private static final Log LOG = LogFactory.getLog(SerializationFactory.class.getName());

  private List<Serialization<?>> serializations = new ArrayList<Serialization<?>>();
  
  volatile private static SerializationFactory serialFactory = null;
  
  /**
   * <p>
   * Serializations are found by reading the <code>io.serializations</code>
   * property from <code>conf</code>, which is a comma-delimited list of
   * classnames.
   * </p>
   */
  public SerializationFactory(Configuration conf) {
    super(conf);
    for (String serializerName : conf.getStringArray(CommonConfigurationKeys.IO_SERIALIZATIONS_KEY)) {
      add(conf, serializerName);
    }
  }
  
  @SuppressWarnings("unchecked")
  private void add(Configuration conf, String serializationName) {
    Class serializionClass = ReflectionUtils.getClass(serializationName);
    serializations.add((Serialization)ReflectionUtils.newInstance(serializionClass, getConf()));
  }

  public <T> Serializer<T> getSerializer(Class<T> c) {
    Serialization<T> serializer = getSerialization(c);
    if (serializer != null) {
      return serializer.getSerializer(c);
    }
    return null;
  }

  public <T> Deserializer<T> getDeserializer(Class<T> c) {
    Serialization<T> serializer = getSerialization(c);
    if (serializer != null) {
      return serializer.getDeserializer(c);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T> Serialization<T> getSerialization(Class<T> c) {
    for (Serialization serialization : serializations) {
      if (serialization.accept(c)) {
        return (Serialization<T>) serialization;
      }
    }
    return null;
  }

  public static SerializationFactory getFactory(Configuration conf) {
    if (serialFactory == null) {
      serialFactory = new SerializationFactory(conf);
    }
    return serialFactory;
  }
  
}
