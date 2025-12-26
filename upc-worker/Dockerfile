# 1. 基础镜像：继续使用你熟悉的 eclipse-temurin
FROM eclipse-temurin:17-jre-jammy

# 2. 设置时区（保持你的逻辑）
ENV TZ=Asia/Shanghai
RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 3. 关键点：匹配多模块的 Jar 包名称
# 因为你父 POM 的 version 是 1.0-SNAPSHOT，所以打包出来的名字通常是 模块名-1.0-SNAPSHOT.jar
# 我们用通配符，这样所有子模块可以共用这套逻辑
COPY target/*.jar app.jar

# 4. 启动命令（保留你的 JAVA_OPTS 注入逻辑）
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]