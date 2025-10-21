FROM amazoncorretto:17-alpine3.22-jdk

WORKDIR /app

COPY api/target/api-0.0.1-SNAPSHOT.jar /app/healthgoal-api.jar

COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

#used non-root user for better security
RUN addgroup --system bayer && adduser --system bayer_user --ingroup bayer
USER bayer_user

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
