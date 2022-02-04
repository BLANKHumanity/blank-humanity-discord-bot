# Blank Humanity's Discord Bot

## Project Structure
- [discord-library](discord-library/)
  Main Interfaces/Services and abstract Classes, to be used by other modules
- [blankdiscordbot-core](blankdiscordbot-core)
  Contains core Implementations of Interfaces/Services (currently also contains most commands)

### How to Build

Run ´mvn compile` in the root folder. The main jar file will then be located under blankdiscordbot-core/target/

### How to publish new Version

You need to make sure you have docker installed and running. You might also need to edit the user used to publish the container in the last 2 lines of this [script](dockerBuild/releaseDockerVersion.sh).

- Navigate to the dockerBuild Folder
- Run `./releaseDockerVersion.sh`
- The Script will now build and publish a container to dockerhub, with the version specified in the [core pom.xml](blankdiscordbot-core/pom.xml)

### How to deploy on Server

This bot comes with a preconfigured [docker-compose.yml](docker-build/docker-compose.yml) that can be used to setup and update the bot easily on any machine.
A [.env](docker-build/.env.example) file must also be created and configured. Afterwards the bot can be started by running `docker-compose up -d` in the folder containing the docker-compose.yml and .env
The bot can then be stopped by running `docker-compose down`
Updates can be pulled by running `docker-compose pull`, if you have selected a new version tag or use latest as your deployment tag.
