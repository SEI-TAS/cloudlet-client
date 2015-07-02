# cloudlet-client
Cloudlet Client

cloudlet-client is an Android app that is used to discover cloudlets in the environment and obtain cloudlet-ready apps that can interact with cloudlets at runtime to offload computation and data.

Cloudlets are discoverable, generic, stateless servers located in single-hop proximity of mobile devices, that can operate in disconnected mode and are virtual-machine (VM) based to promote flexibility, mobility, scalability, and elasticity. In our implementation of cloudlets, applications are statically partitioned into a very thin client that runs on the mobile device and a computation-intensive Server that runs inside a Service VM. Read more about cloudlets at http://sei.cmu.edu/mobilecomputing/research/tactical-cloudlets/.

KD-Cloudlet comprises a total of 7 GitHub projects:

* pycloud (Cloudlet Server)

* cloudlet-client (Cloudlet Client)

* client-lib-android (Library for Cloudlet-Ready Apps)

* client-lib (Java REST Client Library)

* android-logger (SLF4J Logger for Android)

* speech-server (Test server: Speech Recognition Server based on Sphinx)

* speech-android (Test client: Speech Recognition Client)

Building and Installation information in https://github.com/SEI-AMS/pycloud/wiki.
