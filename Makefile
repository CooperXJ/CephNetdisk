#clean:
#	rm -rf ./build
#prod: build
#	TPS_HOST=172.23.27.32 java -jar build/libs/benchmark-0.5.0.jar
#test: build
#	java -jar build/libs/benchmark-0.5.0.jar
preview:
	scp target/demo-0.0.1-SNAPSHOT.jar root@172.23.27.118:/usr/local/service/netdisk
	ssh root@172.23.27.118 "nohup java -jar /usr/local/service/netdisk/demo-0.0.1-SNAPSHOT.jar &"

local_image:
	docker build -f Dockerfile -t file-center-server:latest .
	docker save -o file-center-server.tar file-center-server

default: build
build:
	mvn -DskipTests clean package
image: build
	docker build -f Dockerfile -t file-center-server:latest .



