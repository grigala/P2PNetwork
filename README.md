# P2P Network

###Abstract
A fundamental problem that confronts peer-to-peer applications is
to efficiently locate the node that stores a particular data item. This
project presents Chord, a distributed lookup protocol that addresses
this problem. Chord provides support for just one operation: given
a key, it maps the key onto a node. Data location can be easily
implemented on top of Chord by associating a key with each data
item, and storing the key/data item pair at the node to which the
key maps. Chord adapts efficiently as nodes join and leave the
system, and can answer queries even if the system is continuously
changing. Results from theoretical analysis, simulations, and experiments
show that Chord is scalable, with communication cost
and the state maintained by each node scaling logarithmically with
the number of Chord nodes.

In order to access project Agile/Scrum board and issue tracking interface please follow [`this link`](https://grigala.myjetbrains.com/youtrack/) and sign in with your [Jetbrains](https://myjetbrains.com/) account.

Notes for the code:

- For adding and removing nodes, enter the key into the textfield next to the button and then press add/remove
- when removing the node from the network, the gui doesn't fully update,
    which is why there still are green lines and old entries in the dropdown list
- stabilize gets called periodically via the fixfingers function
--> needs to be run a while until the finger tables are correct
