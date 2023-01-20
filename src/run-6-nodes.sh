#!/bin/bash

start java DatabaseNode -tcpport 2000 -record 29:78 &
timeout 1 > NUL
start java DatabaseNode -tcpport 2701 -connect localhost:2000 -record 20:7 &
timeout 1 > NUL
start java DatabaseNode -tcpport 2002 -connect localhost:2701 -record 11:89 &
timeout 1 > NUL


java DatabaseClient -gateway localhost:2002 -operation get-max &
java DatabaseClient -gateway localhost:2701 -operation set-value 12:77 &
java DatabaseClient -gateway localhost:2701 -operation set-value 11:77 &
java DatabaseClient -gateway localhost:2701 -operation get-max &

start java DatabaseNode -tcpport 3000 -connect localhost:2000 -record -29:-1 &
timeout 1 > NUL
start java DatabaseNode -tcpport 7751 -connect localhost:2000 -record 0:93 &
timeout 1 > NUL
start java DatabaseNode -tcpport 992 -connect localhost:2002 -record 56:2005 &
timeout 1 > NUL

java DatabaseClient -gateway localhost:2701 -operation find-key -29 &
java DatabaseClient -gateway localhost:7751 -operation get-value 11 &
java DatabaseClient -gateway localhost:992 -operation get-value 0 &


java DatabaseClient -gateway localhost:2000 -operation terminate
java DatabaseClient -gateway localhost:2002 -operation terminate
java DatabaseClient -gateway localhost:2701 -operation terminate
java DatabaseClient -gateway localhost:3000 -operation terminate
java DatabaseClient -gateway localhost:7751 -operation terminate
java DatabaseClient -gateway localhost:992 -operation terminate