mkdir build

cd ../
mvn -PdockerBuild clean compile package

cp blankdiscordbot-core/target/blankdiscordbot-core-*.jar dockerBuild/build/BlankDiscordBot.jar
cp blankdiscordbot-core/src/main/resources/application.yml dockerBuild/build/
