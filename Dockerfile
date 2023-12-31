FROM tomcat:9-jdk17-corretto

ENV JDK_VERSION=17
ENV JENTRATA_VERSION 3.x-SNAPSHOT
ENV JENTRATA_HOME /opt/jentrata
ENV TOMCAT_HOME $CATALINA_HOME

ENV TOMCAT_USER_NAME jentrata
ENV TOMCAT_USER_PASS jentrata

# From https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
COPY ContainerFiles/wait-for-it.sh /opt/wait-for-it.sh
COPY ContainerFiles/run.sh /opt/run.sh
COPY ContainerFiles/ROOT/index.html $TOMCAT_HOME/webapps/ROOT/index.html

COPY ./Dist/target/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz /opt

RUN yum install -y wget tar gzip && \
    yum clean all && \
    mkdir -p /opt/jentrata && \
    rm -rf $TOMCAT_HOME/webapps/* && \
    tar -xzvf /opt/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz -C /opt/jentrata && \
    rm /opt/jentrata-msh-$JENTRATA_VERSION-tomcat.tar.gz && \
    ln -s $JENTRATA_HOME/webapps/corvus $TOMCAT_HOME/webapps/jentrata  && \
    chmod a+x /opt/wait-for-it.sh && \
    mkdir -p /opt/jolokia && \
    wget http://search.maven.org/remotecontent?filepath=org/jolokia/jolokia-jvm/1.6.0/jolokia-jvm-1.6.0-agent.jar -O /opt/jolokia/jolokia-jvm.jar

RUN mkdir -p /etc/ssl/certs/java/ && \
  ln -s /usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts /etc/ssl/certs/java/cacerts

EXPOSE 8778 8080

WORKDIR /opt/jentrata

CMD ["/bin/sh", "/opt/run.sh"]