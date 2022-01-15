rm dockerBuild/*.jar
rm dockerBuild/application.yml
rm dockerBuild/dockerbuild.dab
mvn clean install
cp target/BlankDiscordBot-*.jar dockerBuild/BlankDiscordBot.jar
cp src/main/resources/application.yml dockerBuild/

cd dockerBuild

docker build -t zorro909/blankdiscordbot:develop .
docker push zorro909/blankdiscordbot:develop

ssh -t $1 'cd BlankDiscordBot; docker-compose down; docker volume rm blankdiscordbot_blankBotConfig; docker-compose pull; docker-compose up -d'
