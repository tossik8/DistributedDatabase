start java DatabaseNode -tcpport 9000 -record 1:8
timeout 1 > NUL
start java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:7
timeout 1 > NUL
start java DatabaseNode -tcpport 9002 -connect localhost:9001 -record 3:6
timeout 1 > NUL
start java DatabaseNode -tcpport 9003 -connect localhost:9002 -record 4:5
timeout 1 > NUL
start java DatabaseNode -tcpport 9004 -connect localhost:9003 -record 5:4
timeout 1 > NUL
start java DatabaseNode -tcpport 9005 -connect localhost:9004 -record 6:3
timeout 1 > NUL
start java DatabaseNode -tcpport 9006 -connect localhost:9005 -connect localhost:9000 -record 7:1
timeout 1 > NUL
start java DatabaseNode -tcpport 9005 -connect localhost:9004 -record 11:0
timeout 1 > NUL
java DatabaseNode -tcppor 9005 -connect localhost:9004 -record 81:90
timeout 1 > NUL
start java DatabaseNode -tcpport 9005 -connec localhost:9004 -connect localhost:9005 -record 0:3
timeout 1 > NUL
java DatabaseNode -tcpport 9005 -connect localhost:9004 -recor 6:3
timeout 1 > NUL
java DatabaseNode -tcpport 0 -record 99:43
timeout 1 > NUL


java DatabaseClient -gateway localhost:9001 -operation new-record 20:-7
java DatabaseClient -gateway localhost:9003 -operation get-value 20
java DatabaseClient -gateway localhost:9007 -operation get-value 2
java DatabaseClient -gateway localhost:9006 -operation set-value 5:-2
java DatabaseClient -gateway localhost:9004 -operation get-value 5
java DatabaseClient -gateway localhost:9000 -operation get-min
java DatabaseClient -gateway localhost:9002 -operation find-key 78
java DatabaseClient -gateway localhost:9002 -operation find-key 0
java DatabaseClient -gateway localhost:9008 -operation get-max
java DatabaseClient -gateway localhost:9000 -operation terminate
java DatabaseClient -gateway localhost:9008 -operation get-max
java DatabaseClient -gateway localhost:9008 -operation get-value 1
java DatabaseClient -gateway localhost:9001 -operation terminate
java DatabaseClient -gateway localhost:9006 -operation get-min
timeout 5 > NUL

java DatabaseClient -gateway localhost:9000 -operation terminate
java DatabaseClient -gateway localhost:9001 -operation terminate
java DatabaseClient -gateway localhost:9002 -operation terminate
java DatabaseClient -gateway localhost:9003 -operation terminate
java DatabaseClient -gateway localhost:9004 -operation terminate
java DatabaseClient -gateway localhost:9005 -operation terminate
java DatabaseClient -gateway localhost:9006 -operation terminate
java DatabaseClient -gateway localhost:9007 -operation terminate
java DatabaseClient -gateway localhost:9008 -operation terminate

