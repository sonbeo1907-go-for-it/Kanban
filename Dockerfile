FROM tomcat:10-jdk17-openjdk-slim

USER root
RUN apt-get update && apt-get upgrade -y && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN rm -rf /usr/local/tomcat/webapps/*

RUN groupadd -r kanban && useradd -r -g kanban kanban

RUN mkdir -p /usr/local/tomcat/webapps /usr/local/tomcat/logs /usr/local/tomcat/temp /usr/local/tomcat/work \
    && chown -R kanban:kanban /usr/local/tomcat/webapps /usr/local/tomcat/logs /usr/local/tomcat/temp /usr/local/tomcat/work

USER kanban

COPY --chown=kanban:kanban target/kanban.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

CMD ["catalina.sh", "run"]