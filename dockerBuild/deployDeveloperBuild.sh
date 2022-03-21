./scripts/prepareBuild.sh

docker build -t zorro909/blankdiscordbot:develop .
docker push zorro909/blankdiscordbot:develop

ssh -t $1 'cd BlankDiscordBot; docker-compose down; docker volume rm blankdiscordbot_blankBotConfig; docker-compose pull; docker-compose up -d'

./scripts/cleanupBuild.sh
