cd ../
mvn clean install
cp blankdiscordbot-core/target/blankdiscordbot-core-*.jar dockerBuild/BlankDiscordBot.jar
cp blankdiscordbot-core/src/main/resources/application.yml dockerBuild/

cd dockerBuild

docker build -t zorro909/blankdiscordbot:develop .
docker push zorro909/blankdiscordbot:develop

ssh -t $1 'cd BlankDiscordBot; docker-compose down; docker volume rm blankdiscordbot_blankBotConfig; docker-compose pull; docker-compose up -d'

rm BlankDiscordBot.jar
rm application.yml
