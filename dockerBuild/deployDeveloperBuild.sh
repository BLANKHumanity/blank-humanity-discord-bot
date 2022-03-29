./scripts/prepareBuild.sh

docker build -t zorro909/blankdiscordbot:develop .
docker push zorro909/blankdiscordbot:develop

if [ $1 == 'localhost' ]; then
  docker-compose down
  docker volume rm blankdiscordbot_blankBotConfig
  docker-compose pull
  docker-compose up -d
else
  ssh -t $1 'cd BlankDiscordBot; docker-compose down; docker volume rm blankdiscordbot_blankBotConfig; docker-compose pull; docker-compose up -d'
fi

./scripts/cleanupBuild.sh
