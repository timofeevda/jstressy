### Docker-compose bundle

Docker-compose bundle for running in Docker and demonstrating Prometheus metrics exporting functionality and showing exported data in Grafana. JStessy runs example scenarios against local HTTP or WebSocket servers from `jstressy-echo-servers` module

```sh
cd target
unzip stressy-docker-compose-springboot
docker-compose -f ./stressy-bundle/docker-compose.yml up -d
```