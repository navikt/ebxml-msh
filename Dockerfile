FROM tomcat:9-jdk8-corretto

ENV JDK_VERSION=8
ENV JENTRATA_VERSION 3.x-SNAPSHOT
ENV JENTRATA_HOME /opt/jentrata
ENV TOMCAT_HOME $CATALINA_HOME

ENV TOMCAT_USER_NAME jentrata
ENV TOMCAT_USER_PASS jentrata
ENV DB_USER_NAME jentrata
ENV DB_USER_PASS jentrata
ENV DB_HOST_NAME db

# From https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
COPY docker/wait-for-it.sh /opt/wait-for-it.sh
COPY docker/run.sh /opt/run.sh
COPY docker/ROOT/index.html $TOMCAT_HOME/webapps/ROOT/index.html
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

#RUN mkdir -p /etc/ssl/certs/java/ && \
#  ln -s /usr/lib/jvm/java-1.8.0-amazon-corretto/jre/lib/security/cacerts /etc/ssl/certs/java/cacerts

EXPOSE 8778 8080

WORKDIR /opt/jentrata

CMD ["/bin/sh", "/opt/run.sh"]