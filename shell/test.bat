@echo on

set JAVA_HOME=C:\jdk8
set M2_HOME=C:\opt\apache-maven
set path=%path%;%JAVA_HOME%\bin;%M2_HOME%\bin

set JAVA_PROP1=-Dnet._instanceof.commons.daemon.Bootstrap.main.beanid=Bootable
set JAVA_PROP2=-Dnet._instanceof.batch.file2sqlite.config.file=csv_japanpost_config.xml
set MAIN_CLS=net._instanceof.commons.daemon.Bootstrap

set MAVEN_BATCH_PAUSE=on
set MAVEN_TERMINATE_CMD=off

cd ..\
call mvn.bat clean test
call mvn.bat exec:java %JAVA_PROP1% %JAVA_PROP2% -Dexec.classpathScope=test -Dexec.mainClass=%MAIN_CLS%

