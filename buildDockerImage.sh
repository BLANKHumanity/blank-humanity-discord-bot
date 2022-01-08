rm dockerBuild/*.jar
rm dockerBuild/application.yml
rm dockerBuild/dockerbuild.dab
mvn install
cp target/BlankDiscordBot-0.0.1-SNAPSHOT.jar dockerBuild/
cp src/main/resources/application.yml dockerBuild/

cd dockerBuild

rsync -r ./ $1:BlankDiscordBot/
ssh -t $1 'cd BlankDiscordBot; docker-compose down; docker-compose build; docker-compose up -d'
