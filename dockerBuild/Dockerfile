FROM openjdk:17
COPY scripts/wait-for-it.sh /blankbot/wait-for-it.sh
RUN chmod +x /blankbot/wait-for-it.sh

COPY build/BlankDiscordBot.jar /blankbot/
COPY build/application.yml /blankbot/config/
COPY build/events.properties /blankbot/config/
 
WORKDIR /blankbot
CMD ["sh", "-c", "./wait-for-it.sh $MYSQL_HOSTNAME -- java -jar BlankDiscordBot.jar -Dspring.config.location=config/application.yml"]
