./scripts/prepareBuild.sh

TAG=$1

docker build -t $DOCKER_USER/blankdiscordbot:$TAG .
docker push $DOCKER_USER/blankdiscordbot:$TAG

./scripts/cleanupBuild.sh
