./scripts/prepareBuild.sh

cd ../
export TAG=$(echo '${project.version}' | mvn help:evaluate | grep -v '^[[]')
cd dockerBuild

docker build -t $DOCKER_USER/blankdiscordbot:$TAG .
docker push $DOCKER_USER/blankdiscordbot:$TAG

./scripts/cleanupBuild.sh
