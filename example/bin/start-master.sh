#!/bin/sh

LANG="zh_CN.GB18030"

java -Xms1024m -Xmx2048m -Djava.ext.dirs=/usr/local/lib/dadbear/lib/ -cp /etc/dadbear/conf/ me.littlepanda.dadbear.master.Master &

#java -Xms${jvm.Xms} -Xmx${jvm.Xmx} -Djava.ext.dirs=${libpath} -cp ${confpath} ${master.main_method}
