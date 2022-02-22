PROJECT=$1
PROJECT_JAR=$2

mkdir dockerBuild/build/$PROJECT
mkdir dockerBuild/build/$PROJECT/dependency

cp $PROJECT/target/$PROJECT_JAR dockerBuild/build/$PROJECT

find dockerBuild/build/**/dependency/ -type f -printf "%P\n" | xargs -I{} rm -rf $PROJECT/target/dependency/{}
find dockerBuild/build/* -type f -printf "%P\n" | xargs -I{} rm -rf $PROJECT/target/dependency/{}

cp $PROJECT/target/dependency/* dockerBuild/build/$PROJECT/dependency/ 2>/dev/null
