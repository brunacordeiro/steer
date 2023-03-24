# STEER
STEER (**S**DN-based In**TE**nt-Driv**E**n IoT Netwo**R**ks)

<p align="center">
  <img src="https://github.com/brunacordeiro/steer/blob/main/IMG/arqSTEER-english.jpg" width="700" title="hover text">
</p>

Figure presents the STEER architecture, composed by three layers:
* the **SDN fo IoT Layer**, which consists of an existing SDN solution for IoT and is further subdivided into two sub-layers: infrastructure and control;
* the **IDN for IoT Layer**, composed by a **Manager**, which maintains a set of templates for intent definition, along with the set of available behaviors, a **Mediator**, which is responsible for interpreting intents and deploying the most appropriate behavior on the network, and a **Communication** module, responsible for the interaction with the SDN layer; and
* the **Application Layer**, which is composed by the user applications and tools to create intents and behaviors.


--------------------------------
In this repository you can find:

* Tutorial for installing the STEER solution: **Installation Tutorial**
* Source code project running on Netbeans: **SDN_IDN-IoT**
* Example of IoT network simulation that can be emulated in cooja: **Simulation - SDN-IDN-IoT.csc**
* Dataset of the real data used by the sensor nodes: **dataset_AirPure.csv**

In addition to the files mentioned, it is possible to access the **sdn-wise_java** directory that contains the source code of the infrastructure based on the SDN-WISE solution. Directory organization is described in the directory organization.md document.

The steps for installing and configuring the STEER solution present in the **Installation Tutorial** presents all the installation details of the base solution (SDN-WISE) together with the Cooja emulator, where an example of STEER simulation - **Simulation - SDN-IDN-IoT.csc** can be executed.

The **SDN_IDN-IoT** project source code is extracted from the Netbeans IDE, so it can be opened using this IDE <https://netbeans.apache.org/download/index.html>. The project uses Maven which consist of a Java project automation and management tool.

The dataset used is composed of real data extracted from an IoT solution capable of monitoring indoor air quality. Data were collected using sensors and stored on a web platform for analysis and processing purposes.
