Message Headers for Types:

Flag means the first data(int) included by message.

Heartbeats Server -> Peers: Flag 100
Heartbeats Peers  -> Peers: Flag 101
Heartbeats response: Flag 102 

Communication through a heartbeat port is invalid if 
a message contains another header. 

