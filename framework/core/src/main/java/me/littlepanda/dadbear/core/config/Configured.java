package me.littlepanda.dadbear.core.config;

import org.apache.commons.configuration.Configuration;

public class Configured implements Configurable {

  private Configuration conf;

  public Configured() {
    this(null);
  }
  
  public Configured(Configuration conf) {
    setConf(conf);
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public Configuration getConf() {
    return conf;
  }

}
