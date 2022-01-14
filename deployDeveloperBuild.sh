rm dockerBuild/*.jar
rm dockerBuild/application.yml
rm dockerBuild/dockerbuild.dab
mvn clean install
cp target/BlankDiscordBot-*.jar dockerBuild/BlankDiscordBot.jar
cp src/main/resources/application.yml dockerBuild/

cd dockerBuild

rsync -r ./ $1:BlankDiscordBot/
ssh -t $1 'cd BlankDiscordBot; docker-compose down; docker-compose build; docker-compose up -d'
