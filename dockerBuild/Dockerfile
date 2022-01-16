FROM openjdk:17
COPY wait-for-it.sh /blankbot/wait-for-it.sh
RUN chmod +x /blankbot/wait-for-it.sh
ADD application.yml /blankbot/config/
ADD BlankDiscordBot.jar /blankbot/
 
WORKDIR /blankbot
CMD ["sh", "-c", "./wait-for-it.sh $MYSQL_HOSTNAME -- java -jar BlankDiscordBot.jar -Dspring.config.location config/application.yml"]
