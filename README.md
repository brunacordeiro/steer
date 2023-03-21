# STEER
STEER (**S**DN-based In**TE**nt-Driv**E**n IoT Netwo**R**ks)

<p align="center">
  <img src="https://github.com/brunacordeiro/steer/blob/main/IMG/arqSTEER-english.jpg" width="700" title="hover text">
</p>

Figure presents the STEER architecture, composed by three layers:
* the **SDN fo IoT Layer**, which consists of an existing SDN solution for IoT and is further subdivided into two sub-layers: infrastructure and control;
* the **IDN for IoT Layer**, composed by a **Manager**, which maintains a set of templates for intent definition, along with the set of available behaviors, a **Mediator**, which is responsible for interpreting intents and deploying the most appropriate behavior on the network, and a **Communication** module, responsible for the interaction with the SDN layer; and
* the **Application Layer**, which is composed by the user applications and tools to create intents and behaviors.
