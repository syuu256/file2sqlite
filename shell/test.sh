#!/bin/bash

#JAVA_HOME=/usr/java/default
#M2_HOME=/opt/apache-maven
#PATH=${PATH}:${JAVA_HOME}/bin:${M2_HOME}/bin

CONFIG_XML=csv_japanpost_config.xml
JAVA_PROP1=-Dnet._instanceof.commons.daemon.Bootstrap.main.beanid=Bootable
JAVA_PROP2=-Dnet._instanceof.batch.file2sqlite.config.file=${CONFIG_XML}
MAIN_CLS=net._instanceof.commons.daemon.Bootstrap

cd ../
mvn clean test
mvn exec:java ${JAVA_PROP1} ${JAVA_PROP2} -Dexec.classpathScope=test -Dexec.mainClass=${MAIN_CLS}
