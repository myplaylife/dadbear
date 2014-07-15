package me.littlepanda.dadbear.core.config;

import org.apache.commons.configuration.Configuration;

public interface Configurable {

  void setConf(Configuration conf);

  Configuration getConf();
}
