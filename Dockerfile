# 基础镜像
FROM openjdk:17-jdk-slim

# 设置时区为上海
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# 复制 jar 包
COPY target/upc-1.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8069

# 启动命令优化 (关键！)
# 服务器只有 4G，这里限制堆内存为 512M，防止 Java 进程把服务器内存吃光导致死机
# -Djava.security.egd... 是为了加快启动速度
ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]