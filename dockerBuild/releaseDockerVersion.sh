cd ../

mvn clean install

cp blankdiscordbot-core/target/blankdiscordbot-core-*.jar dockerBuild/BlankDiscordBot.jar
cp blankdiscordbot-core/src/main/resources/application.yml dockerBuild/

cd blankdiscordbot-core

export TAG=$(echo '${project.version}' | mvn help:evaluate | grep -v '^[[]')

cd ../dockerBuild

docker build -t zorro909/blankdiscordbot:$TAG .
docker push zorro909/blankdiscordbot:$TAG
