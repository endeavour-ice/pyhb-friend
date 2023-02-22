FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the container image.
# 指定镜像所在的目录文件
WORKDIR /app
# 复制本地的pom文件
COPY ../user/pyhb-friend/pom.xml .
# 将本地的源码文件复制
COPY ../user/pyhb-friend/src ./src

# Build a release artifact.
# 执行打包文件
RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/pyhb-friend-1.0-SNAPSHOT.jar","--spring.profiles.active=prod"]
