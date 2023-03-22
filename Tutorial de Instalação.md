Neste tutorial vamos realizar a instalação da camada de SDN para IoT e da camada de IDN para IoT.
É indicado que a solução seja executada em um ambiente linux, porém a instalação em máquinas virtuais também pode ser realizada.

**Instalação e configuração da SDN and IDN for IoT Layer**

Inicialmente é necessário a instalação do SDN-WISE que pode ser feita diretamento pelo tutorial de instalação disponibilizado em: https://sdnwiselab.github.io/docs/guides/GetStarted

Todos os passos presentes no tutorial de instalação do SDN-WISE devem ser seguidos para que a solução STEER possa ser executada.

Após o SDN-WISE ter sido instalado, é indicado que a diretorio sdn-wise_java disponível em: https://github.com/brunacordeiro/steer/tree/main/sdn-wise_java seja substituido.

*Para substituir o diretório é necessário entrar em: /home/contiki/tools/cooja/examples e substituir o diretorio original do SDN-WISE pelo diretório disponível aqui

O Controlador SDN e o Mediador IDN estão disponíveis em um projeto: https://github.com/brunacordeiro/steer/blob/main/SDN_IDN-IoT 

O projeto SDN-IDN-IoT pode ser executado com o Netbeans IDE, está codificado em Java. Este projeto pode ser executado em uma máquina diferente de onde o SDN-WISE foi instalado, mas é necessário ter o endereço de IP para que a rede IoT possa realizar a comunicação com o controlador SDN.

**Executando STEER**

Para que seja possível visualizar a execução da solucão STEER é necessário:

1 - no ambiente onde foi instalado o SDN-WISE é necessário executar o emulador cooja. Ao abrir o emulador é necessário identificar o endereço de IP da máquina onde o controlador SDN foi instalado.

<p align="center">
  <img src="https://github.com/brunacordeiro/steer/blob/main/IMG/Execute-cooja.png" width="700" title="hover text">
</p>

2 - com o emulador cooja aberto, a simulação pode ser selecionada: Simulation - SDN-IDN-IoT.csc disponível em: https://github.com/brunacordeiro/steer/blob/main/Simulation%20-%20SDN-IDN-IoT.csc 

<p align="center">
  <img src="https://github.com/brunacordeiro/steer/blob/main/IMG/simulation.png" width="700" title="hover text">
</p>

3 - Antes de executar a simulação é necessário que o controlador SDN esteja executando. Dessa forma, é necessário entrar no projeto SDN-IDN-IoT do Netbeans e executar o projeto.

<p align="center">
  <img src="https://github.com/brunacordeiro/steer/blob/main/IMG/ExecuteControllerSDN.png" width="700" title="hover text">
</p>

4 - Neste momento o controlador está aguardano a conexão dos nós sensores, basta iniciar a execução da simulação no cooja e acompanhar as mensagens de impressão.
