## Client-Server-TCP: Uma Exploração Prática do Protocolo TCP

Este projeto visa aprofundar o conhecimento sobre o protocolo TCP e a camada de rede do modelo OSI, utilizando uma aplicação prática de comunicação cliente-servidor. Desenvolvido para a disciplina de Redes de Computadores, o projeto simula um sistema de comunicação via TCP com duas funcionalidades principais:

- **Chat:** Permite a troca de mensagens em tempo real entre o cliente e o servidor.
- **Transferência de arquivos:** O cliente pode solicitar arquivos ao servidor, que os envia de forma segura.

Para garantir a comunicação simultânea com múltiplos clientes, o servidor utiliza threads separadas para cada conexão.

**Arquitetura da aplicação:**

O projeto é estruturado em duas classes principais:

- **TCPClient:** Implementa a funcionalidade do cliente, responsável por iniciar conexões, enviar e receber mensagens, e solicitar arquivos.
- **TCPServer:** Implementa a funcionalidade do servidor, responsável por ouvir por conexões, gerenciar threads de comunicação, receber mensagens, e enviar arquivos.

**Estrutura dos pacotes:**

Os pacotes de comunicação possuem um tamanho fixo de 1034 bytes e são estruturados da seguinte forma:

- **4 bytes:** Sequence Number (número de sequência)
- **4 bytes:** ACK (número de confirmação)
- **2 bytes:** rcvWindow (tamanho da janela de recepção, em bytes)
- **2 bytes:** checksum (soma de verificação)
- **1024 bytes:** payload (dados da mensagem ou arquivo)

**Funcionalidades implementadas:**

- **Confirmação de recebimento (ACK):** O TCP garante a entrega confiável de dados através do mecanismo de ACK. O cliente envia um ACK para cada pacote recebido pelo servidor, confirmando o recebimento. O servidor, por sua vez, mantém um registro dos pacotes recebidos e retransmite os pacotes não confirmados.
- **Controle de Congestionamento:** O TCP implementa mecanismos para evitar o congestionamento da rede, evitando o envio de pacotes em excesso que podem sobrecarregar o sistema. Este projeto utiliza o mecanismo de **janela deslizante** para controlar a quantidade de dados enviados antes de receber uma confirmação. O cliente mantém uma janela de pacotes que podem ser enviados simultaneamente, ajustando seu tamanho com base no feedback recebido do servidor. Caso haja perda de pacotes, a janela deslizante diminui para descongestionar o tráfego da rede, evitando o envio excessivo de dados. Quando a rede está menos congestionada, a janela deslizante aumenta gradualmente, permitindo um envio mais eficiente de dados.
- **Ordenação de pacotes:** O TCP garante que os pacotes cheguem na ordem correta, mesmo que sejam recebidos fora de ordem. O projeto implementa mecanismos para detectar e ordenar pacotes recebidos em ordem diferente, garantindo a entrega correta dos dados.

**Segurança:**

A integridade dos dados é garantida utilizando o algoritmo MD5 para a geração de tokens de verificação de integridade de arquivos e para o cálculo do checksum. A classe `MD5Encryption` é responsável por essas operações.

**Recursos adicionais:**

- **PacketTransmitter:** Classe responsável por encapsular e formatar dados em pacotes.
- **PacketReceiver:** Classe responsável por descapsular os dados recebidos.

**Conclusão:**

Este projeto é uma ótima maneira de aprender na prática sobre o funcionamento do protocolo TCP e explorar diferentes aspectos da comunicação cliente-servidor. O código demonstra a implementação de um sistema de chat e transferência de arquivos, utilizando threads para multitarefa e mecanismos de segurança para garantir a integridade dos dados. Ele também ilustra a implementação de mecanismos importantes do TCP como a confirmação de recebimento, controle de congestionamento e ordenação de pacotes, tornando-o um recurso valioso para o aprendizado de redes de computadores.
