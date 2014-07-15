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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

import me.littlepanda.dadbear.core.config.Configured;

/**
 * Base class for providing serialization to Avro types.
 */
public abstract class AvroSerialization<T> extends Configured 
	implements Serialization<T>{
  
  public static final String AVRO_SCHEMA_KEY = "Avro-Schema";

  @Override
  public Deserializer<T> getDeserializer(Class<T> c) {
    return new AvroDeserializer(c);
  }

  @Override
  public Serializer<T> getSerializer(Class<T> c) {
    return new AvroSerializer(c);
  }

  /**
   * Return an Avro Schema instance for the given class.
   */
  public abstract Schema getSchema(T t);

  /**
   * Create and return Avro DatumWriter for the given class.
   */
  public abstract DatumWriter<T> getWriter(Class<T> clazz);

  /**
   * Create and return Avro DatumReader for the given class.
   */
  public abstract DatumReader<T> getReader(Class<T> clazz);

  class AvroSerializer implements Serializer<T> {

    private DatumWriter<T> writer;
    private DataFileWriter<T> dataFileWriter;
    private OutputStream outStream;

    AvroSerializer(Class<T> clazz) {
      this.writer = getWriter(clazz);
    }

    @Override
    public void close() throws IOException {
	  dataFileWriter.close();
      outStream.close();
    }

    @Override
    public void open(OutputStream out) throws IOException {
      outStream = out;
    }

    @Override
    public void serialize(T t) throws IOException {
      dataFileWriter = new DataFileWriter<T>(this.writer).create(getSchema(t), outStream);
      dataFileWriter.append(t);
    }

  }

  class AvroDeserializer implements Deserializer<T> {

    private DatumReader<T> reader;
    private DataFileStream<T> stream;
    private InputStream inStream;

    AvroDeserializer(Class<T> clazz) {
      this.reader = getReader(clazz);
    }

    @Override
    public void close() throws IOException {
      inStream.close();
      stream.close();
    }

    @Override
    public T deserialize(T t) throws IOException {
      return stream.next();
    }

    @Override
    public void open(InputStream in) throws IOException {
    	inStream = in;
    	stream = new DataFileStream<T>(inStream, reader);
    }

  }

}
