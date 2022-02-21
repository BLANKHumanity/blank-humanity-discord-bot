FROM openjdk:17
COPY scripts/wait-for-it.sh /blankbot/wait-for-it.sh
RUN chmod +x /blankbot/wait-for-it.sh
COPY build/dependency/* /blankbot/libraries/
COPY build/BlankDiscordBot.jar /blankbot/
ADD build/funplace/dependency/ /blankbot/libraries/
COPY build/funplace/funplace.jar /blankbot/libraries/

COPY build/application.yml /blankbot/config/
 
WORKDIR /blankbot
CMD ["sh", "-c", "./wait-for-it.sh $MYSQL_HOSTNAME -- java -cp BlankDiscordBot.jar:libraries/* -Dspring.config.location=config/application.yml com.blank.humanity.discordbot.BlankDiscordBotApplication"]
