# 基础镜像改为 eclipse-temurin
FROM eclipse-temurin:17-jre-jammy

# 置时区为上海
ENV TZ=Asia/Shanghai
# eclipse-temurin 基于 Ubuntu，设置时区命令稍有不同，用下面这行最稳：
RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制 jar 包 (这行不用动)
COPY target/upc-1.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8069

# 启动命令
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]