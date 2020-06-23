# Layer RBA provider MSA

## Prerequisites

1. Java JDK 11 or later
2. Docker and Compose

## How to build and run

Postgres backend emulation and initialization scripts are in the `docker` folder 

```$bash
$ cd ./docker 
$ docker-compose up -d

$ docker ps
```

should show 
```
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS                 PORTS                    NAMES
f01293fca592        layer/postgres      "docker-entrypoint.s…"   43 hours ago        Up 2 hours (healthy)   0.0.0.0:5432->5432/tcp   docker_postgres_1
```

then `$ cd ..` and `$ ./gradlew bootRun' should output:
```
2020-06-23 13:54:23.704  INFO 7435 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port(s): 8080
2020-06-23 13:54:23.718  INFO 7435 --- [           main] i.l.s.s.SharingMicroservice$Companion    : Started SharingMicroservice.Companion in 1.664 seconds (JVM running for 1.933)


```