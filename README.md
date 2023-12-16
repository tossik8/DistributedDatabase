# Distributed Database

## Table of Content

- Project Description
- How to Compile/Install
- How to Run
  - Node Execution
  - Client Execution
- How to Use
- Project Description
  - Available Operations
  - Description of operations
- Author

## Project Description
The database allows you to store "key:value" pairs of data on each running node across many devices.
The code is written in Java 1.8 because to connect devices within 1 network, it is enough to utilise
the basics of Java API.

## How to Compile/Install
- In order to install the application
    - For Windows `tar -xf DistributedDatabase.zip`
    - For Linux `unzip DistributedDatabase.zip`
- In order to compile the program execute the following command: `javac DatabaseNode.java DatabaseClient.java Node.java ClientServerThread.java`

## How to Run

### Node Execution
- General format: `java DatabaseNode -tcpport <TCP port number> [ -connect <adrress>:<port> ] -record <key>:<value>`
- Concrete example: `java DatabaseNode -tcpport 9991 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989 -record 17:256`

### Client Execution
- General format: `java DatabaseClient -gateway <address>:<TCP port number> -operation <operation with parameters>`
- Concrete example: `java DatabaseClient -gateway localhost:9991 -operation get-value 17`

## How to Use

1. Run a DatabaseNode in the terminal.
2. Having successfully executed the node, the terminal window will not accept any
   new inputs from the user, because it will listen for connections to it.
3. Execute the DatabaseClient or run another DatabaseNode.

## Protocol Description

The network is created by executing DatabaseNodes and connecting them with each other.

Communication with both clients and nodes is executed using the TCP protocol.

Before execution, each node is provided with a port number. During the creation of a node, a setPort function
is invoked, and unless the port number is available, it will increase the port number by 1 and check again
until a free a port is found. As a consequence, the node might run on a port different from the provided one.
In addition, port numbers cannot be negative or 0, so the node will not be started if either condition is met.

### Available operations

- `get-value <key>`
  - If key is present, sends key:value to the client. Otherwise, ERROR.
- `set-value <key value>`
  - If key is present, sets the new value, sends key:value to the client. Otherwise, ERROR.
- `find-key <key>`
  - If key is present, sends ip:port of a node with this key to the client. Otherwise, ERROR.
- `get-max`
  - Sends the greatest value to the client in the following way: key:value.
- `get-min`
  - Sends the smallest value to the client in the following way: key:value.
- `new-record <key value>`
  - Updates the key-value pair of a node. Nodes with the same key are updated as well. Sends OK to the client.
- `terminate`
  - Terminates a node. Neighbours forget that node. Sends OK to the client.


### Description of operations

Operations "get-value", "set-value", "find-key", "get-max", "get-min" utilise
the recursive version of Depth-First Search (DFS) to traverse through the graph.

An example with "get-value" command:
1. A client sends the request.
2. The node checks if it contains the key which is being looked for. 
    - If so, send the result  to the client and finish execution.
    - Else, add the address to the list of visited addresses and start iterating over the neighbours.
3. Check if there is a connected node.
    - If so, check if a neighbour has been visited.
      - If so, skip.
      - Else, go back to 1, and, additionally, pass the list of visited nodes.
        - If response is <i>ERROR</i>, go back to 3.
        - Else, send the result back to the client that contacted this node and finish execution.
    - Else, finish iterating.
4. Send ERROR to the client that contacted this node and finish execution.

For examples with different commands, refer to the PDF file.

Operation "new-record" sets a new value associated with the provided key to the node,
and executes "set-value" operation to update the value in the entire network for the new key with the value
provided as arguments to the "new-record" function.

Operation "terminate" contacts all neighbours of a node being terminated and removes its address
from their lists of connected nodes.

## Author

Mykyta Toropov
