# TodoTxt2Mqtt

Suitable for using a [todo.txt](https://github.com/todotxt/todo.txt) file with Home Assistant or any other MQTT-compatible software

## Docker
- On production machine
    - make a conf/private.conf
    - sbt dist
    - sbt docker:publishLocal
    - docker run -d --name todotxt2mqtt -p 1111:9000 -v /home/snuffy/todo:/todo -e PUID=1000 -e PGID=1000 todotxt2mqtt:1.0-SNAPSHOT (where 1111 is the host port desired and snuffy is you)