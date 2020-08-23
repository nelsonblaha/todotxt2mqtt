# TodoTxt2Mqtt

## Docker
- On production machine
    - make a conf/private.conf
    - sbt dist
    - sbt docker:publishLocal
    - docker run -p 1111:9000 -v /home/snuffy/todo:/todo todotxt2mqtt:1.0-SNAPSHOT (where 1111 is the host port desired and snuffy is you)