# Programming Plan

## What we need
- Multiple clients
- Multiple routers

## Structure
Client A
/ \
 |
 |
 |
\ /
Router A
/ \
 |
 |
 |
\ /
Router B
/ \
 |
 |
 |
\ /
Client B

## Logic
- Client A asks for a socket with Client B (neither know each other's IP)
- Client A sends this request, along with its IP, to Router A
- Router A does not know Client B, so sends the request to Router B
- Router B knows Client B, so sends request there
- CLient B decodes request as a request to connect to Client A at IP xxx.xxx.xxx.xxx
- Client B creates a socket connection to Client A's IP

# Client Capabilities
- Send request
- Receive request
- Create socket
- Accept incoming socket

# Router Capabilities
- Receive request from client
- Connect to other router
- Forward request to other router
- Forward request to client


# Benchmarks
- filesize
- read time
- write time
- send time
- receive time



# CURSOR BOX
----------
|        |
|        |
|        |
|        |
----------