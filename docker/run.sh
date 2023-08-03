#!/bin/bash -ex

export TOMCAT_HOME=$CATALINA_HOME

cat > ${TOMCAT_HOME}/conf/tomcat-users.xml <<-ENDL
<?xml version='1.0' encoding='utf-8'?>
<tomcat-users>
    <role rolename="admin" />
    <role rolename="admin-gui"/>
    <role rolename="manager-gui"/>
    <role rolename="${TOMCAT_USER_NAME}" />
    <user username="${TOMCAT_USER_NAME}" password="${TOMCAT_USER_PASS}" roles="tomcat,admin,${TOMCAT_USER_NAME},admin-gui,manager-gui" />
</tomcat-users>
ENDL

export JENTRATA_EBMS_DB_URL="jdbc:postgresql://$DB_EBMS_HOST:$DB_EBMS_PORT/$DB_EBMS_DATABASE"
export JENTRATA_EBMS_DB_USER="$DB_EBMS_USERNAME"
export JENTRATA_EBMS_DB_PASS="$DB_EBMS_PASSWORD"

export JENTRATA_ACTIVEMQ_MODULE="$ACTIVEMQ_MODE"
export ACTIVEMQ_HOST_NAME="localhost"
if test "$ACTIVEMQ_MODE" = "external"; then
    export ACTIVEMQ_HOST_NAME=`echo "$ACTIVEMQ_URL" | sed -n 's:^tcp\://\([^\:]*\)\:61616:\1:p'`
    export JENTRATA_ACTIVEMQ_USERNAME="$ACTIVEMQ_USER"
    export JENTRATA_ACTIVEMQ_PASSWORD="$ACTIVEMQ_PASS"
    export JENTRATA_ACTIVEMQ_CONNECTIONFACTORYURL="$ACTIVEMQ_URL"
fi

#export JAVA_OPTS="$JAVA_OPTS --add-modules java.xml.ws -Dcorvus.home=$JENTRATA_HOME -javaagent:/opt/jolokia/jolokia-jvm.jar=host=0.0.0.0"
export JAVA_OPTS="$JAVA_OPTS -Dcorvus.home=$JENTRATA_HOME -javaagent:/opt/jolokia/jolokia-jvm.jar=host=0.0.0.0"

echo waiting for postgres
/opt/wait-for-it.sh "$DB_HOST_NAME:5432"

CMD="run"
if test "$JENTRATA_DEBUG" = "true"; then
  CMD="jpda run"
fi
echo "running jentrata in $CMD mode"
exec catalina.sh $CMD