# https://devcenter.heroku.com/articles/java-support#default-web-process-type
web:    java -Dserver.port=$PORT $JAVA_OPTS -jar server/target/*-exec.jar
player: java                     $JAVA_OPTS -jar player/target/*-exec.jar