#!/bin/bash

start java DatabaseNode -tcpport 2000 -record 29:78 &
sleep 1
start java DatabaseNode -tcpport 2701 -connect localhost:2000 -record 20:7 &
sleep 1
start java DatabaseNode -tcpport 2002 -connect localhost:2701 -record 11:89 &
sleep 1


java DatabaseClient -gateway localhost:2701 -operation get-max &
java DatabaseClient -gateway localhost:2701 -operation new-record 11:77 &
java DatabaseClient -gateway localhost:2701 -operation get-max &

java DatabaseClient -gateway localhost:2000 -operation terminate
java DatabaseClient -gateway localhost:2002 -operation terminate
java DatabaseClient -gateway localhost:2701 -operation terminate