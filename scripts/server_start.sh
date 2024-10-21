#!/usr/bin/env bash
cd /home/ec2-user/server
sudo java -war -Dserver.port=8081 \
    *.war > /dev/null 2> /dev/null < /dev/null &
