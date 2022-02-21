./scripts/prepareBuild.sh

export TAG=$(git describe HEAD --tags --always)

docker build -t $DOCKER_USER/blankdiscordbot:$TAG .
docker push $DOCKER_USER/blankdiscordbot:$TAG

./scripts/cleanupBuild.sh
