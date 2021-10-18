FROM openjdk:11-jre-slim
MAINTAINER Nils Hoffmann &lt;nils.hoffmann@isas.de&gt;
VOLUME /tmp
# receive jar file to host via argument
ARG JAR_FILE
ARG APP_NAME
ARG APP_PORT
ARG APP_VERSION

LABEL base.image="openjdk:11-jre-slim"
LABEL version="${APP_VERSION}"
LABEL software="jgoslin-cli"
LABEL software.version="${APP_VERSION}"
LABEL description="A command line interface for the jgoslin library for lipid name translation"
LABEL website="https://github.com/lifs-tools/jgoslin"
LABEL documentation="https://github.com/lifs-tools/jgoslin"
LABEL license="https://github.com/lifs-tools/jgoslin/blob/master/LICENSE"
LABEL tags="Metabolomics Lipidomics"

RUN mkdir /home/data && chmod 755 /home/data

ENV AB_OFF true
EXPOSE ${APP_PORT}
ENV JAVA_OPTIONS="-Djava.security.egd=file:/dev/./urandom"
ENV JAVA_APP_JAR=${JAR_FILE}
ENV JAVA_APP_NAME=${APP_NAME}
ENV JAR_FILE=${JAR_FILE}
ADD target/${JAR_FILE} /deployments/
ADD parseNames.sh /tmp/parseNames.sh
RUN cp /tmp/parseNames.sh /bin/parseNames && chmod 755 /bin/parseNames
ENTRYPOINT ["sh","/bin/parseNames"]
