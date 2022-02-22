./scripts/cleanupBuild.sh

mkdir build

cd ../
mvn -PdockerBuild clean compile package

./dockerBuild/scripts/copyProject.sh discord-library "discord-library-*.jar"
./dockerBuild/scripts/copyProject.sh blankdiscordbot-core "blankdiscordbot-core-*.jar.original"
./dockerBuild/scripts/copyProject.sh funplace "funplace-*.jar"

cp blankdiscordbot-core/src/main/resources/application.yml dockerBuild/build/

cd dockerBuild

./createDockerfile.sh > DockerfileTmp

