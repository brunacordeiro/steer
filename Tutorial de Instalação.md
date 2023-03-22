In this tutorial we will perform the installation of the SDN layer for IoT and the IDN layer for IoT.
It is indicated that the solution is executed in a linux environment, however the installation in virtual machines can also be performed.

**Installation and configuration of SDN and IDN for IoT Layer**

Initially, it is necessary to install SDN-WISE, which can be done directly through the installation tutorial available at: https://sdnwiselab.github.io/docs/guides/GetStarted

All steps present in the SDN-WISE installation tutorial must be followed so that the STEER solution can be executed.

After SDN-WISE has been installed, it is recommended that the sdn-wise_java directory available at: https://github.com/brunacordeiro/steer/tree/main/sdn-wise_java be replaced.

*To replace the directory, go to: /home/contiki/tools/cooja/examples and replace the original SDN-WISE directory with the directory available here

The SDN Controller and IDN Mediator are available in a project: https://github.com/brunacordeiro/steer/blob/main/SDN_IDN-IoT

The SDN-IDN-IoT project can be run with Netbeans IDE, it is coded in Java. This project can be run on a different machine than where SDN-WISE was installed, but it is necessary to have the IP address so that the IoT network can communicate with the SDN controller.

**Executing STEER**

In order to be able to visualize the execution of the STEER solution it is necessary:

1 - in the environment where SDN-WISE was installed, it is necessary to run the cooja emulator. When opening the emulator, it is necessary to identify the IP address of the machine where the SDN controller was installed.

<p align="center">
  <img src="https://github.com/brunacordeiro/steer/blob/main/IMG/Execute-cooja.png" width="300" title="hover text">
</p>

2 - with the cooja emulator open, the simulation can be selected: Simulation - SDN-IDN-IoT.csc available at: https://github.com/brunacordeiro/steer/blob/main/Simulation%20-%20SDN-IDN -IoT.csc

<p align="center">
  <img src="https://github.com/brunacordeiro/steer/blob/main/IMG/simulation.png" width="600" title="hover text">
</p>

3 - Before running the simulation, the SDN controller must be running. So, you need to enter the Netbeans SDN-IDN-IoT project and run the project.

<p align="center">
  <img src="https://github.com/brunacordeiro/steer/blob/main/IMG/ExecuteControllerSDN.png" width="700" title="hover text">
</p>

4 - At this moment, the controller is waiting for the connection of the sensor nodes, just start the execution of the simulation in cooja and follow the print messages.
