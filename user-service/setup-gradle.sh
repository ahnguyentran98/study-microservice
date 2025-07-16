echo "Building all microservices..."

echo "Config gradle version to 7.6.1"
sudo apt update

sudo apt install zip unzip curl

curl -s "https://get.sdkman.io" | bash

source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install gradle 7.6.1

sdk use gradle 7.6.1

echo "Config gradle version to 7.6.1 done"