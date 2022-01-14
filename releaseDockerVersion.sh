rm dockerBuild/*.jar
rm dockerBuild/application.yml
rm dockerBuild/dockerbuild.dab
mvn clean install
cp target/BlankDiscordBot-*.jar dockerBuild/BlankDiscordBot.jar
cp src/main/resources/application.yml dockerBuild/

export TAG=$(echo '${project.version}' | mvn help:evaluate | grep -v '^[[]')

cd dockerBuild

docker build -t zorro909/blankdiscordbot:$TAG .
docker push zorro909/blankdiscordbot:$TAG
