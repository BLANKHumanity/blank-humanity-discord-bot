mkdir build
mkdir build/funplace

cd ../
mvn -PdockerBuild compile package

cp blankdiscordbot-core/target/blankdiscordbot-core-*.jar.original dockerBuild/build/BlankDiscordBot.jar
cp -r blankdiscordbot-core/target/dependency dockerBuild/build/
cp blankdiscordbot-core/src/main/resources/application.yml dockerBuild/build/

cp funplace/target/funplace-*.jar dockerBuild/build/funplace/funplace.jar
cp -r funplace/target/dependency dockerBuild/build/funplace

cd dockerBuild

cd build/dependency
find . -exec rm -rf ../funplace/dependency/{} \;
touch ../funplace/dependency/placeholder.txt
