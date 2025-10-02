# Modelo de Dados do SGC

Este documento descreve a estrutura do banco de dados do sistema SGC.

## Tabelas

### `ADMINISTRADOR`

Tabela que marca usuários como administradores.

| Coluna           | Tipo         | Chave | Descrição                              |
| ---------------- | ------------ | ----- | -------------------------------------- |
| `usuario_titulo` | VARCHAR2(12) | PK,FK | Usuário administrador (ref `USUARIO`). |

---

### `ALERTA`

Alertas gerados no sistema, direcionados a unidades ou usuários específicos.

| Coluna                   | Tipo          | Chave | Descrição                                     |
| ------------------------ | ------------- | ----- | --------------------------------------------- |
| `codigo`                 | NUMBER        | PK    | Identificador único do alerta.                |
| `processo_codigo`        | NUMBER        | FK    | Processo associado (ref `PROCESSO`).          |
| `data_hora`              | TIMESTAMP     |       | Data e hora do alerta.                        |
| `unidade_origem_codigo`  | NUMBER        | FK    | Unidade de origem do alerta (ref `UNIDADE`).  |
| `unidade_destino_codigo` | NUMBER        | FK    | Unidade de destino do alerta (ref `UNIDADE`). |
| `usuario_destino_titulo` | NUMBER        | FK    | Usuário destino do alerta (ref usuario).      |
| `descricao`              | VARCHAR2(255) |       | Descrição do alerta.                          |

---

### `ALERTA_USUARIO`

Associa um alerta a um usuário específico, controlando a leitura.

| Coluna              | Tipo         | Chave  | Descrição                                               |
| ------------------- | ------------ | ------ | ------------------------------------------------------- |
| `alerta_codigo`     | NUMBER       | PK, FK | Alerta associado (ref `ALERTA`).                        |
| `usuario_titulo`    | VARCHAR2(12) | PK. FK | Usuário associado (ref `USUARIO`).                      |
| `data_hora_leitura` | TIMESTAMP    |        | Indica a data e hora de leitura do alerta pelo usuário. |

---

### `ANALISE_CADASTRO`

Registros de análise de cadastro de um subprocesso.

| Coluna               | Tipo          | Chave | Descrição                                  |
| -------------------- | ------------- | ----- | ------------------------------------------ |
| `codigo`             | NUMBER        | PK    | Identificador único da análise.            |
| `subprocesso_codigo` | NUMBER        | FK    | Subprocesso analisado (ref `SUBPROCESSO`). |
| `data_hora`          | TIMESTAMP     |       | Data e hora da análise.                    |
| `observacoes`        | VARCHAR2(500) |       | Observações da análise.                    |

---

### `ANALISE_VALIDACAO`

Registros de análise de validação de mapa de um subprocesso.

| Coluna               | Tipo          | Chave | Descrição                                  |
| -------------------- | ------------- | ----- | ------------------------------------------ |
| `codigo`             | NUMBER        | PK    | Identificador único da análise.            |
| `subprocesso_codigo` | NUMBER        | FK    | Subprocesso analisado (ref `SUBPROCESSO`). |
| `data_hora`          | TIMESTAMP     |       | Data e hora da análise.                    |
| `observacoes`        | VARCHAR2(500) |       | Observações da análise.                    |

---

### `ATRIBUICAO_TEMPORARIA`

Define atribuições temporárias de usuários a unidades.

| Coluna           | Tipo          | Chave | Descrição                              |
| ---------------- | ------------- | ----- | -------------------------------------- |
| `codigo`         | NUMBER        | PK    | Identificador único da atribuição.     |
| `unidade_codigo` | NUMBER        | FK    | Unidade da atribuição (ref `UNIDADE`). |
| `usuario_titulo` | VARCHAR2(12)  | FK    | Usuário atribuído (ref `USUARIO`).     |
| `data_inicio`    | DATE          |       | Data de início da atribuição.          |
| `data_termino`   | DATE          |       | Data de término da atribuição.         |
| `justificativa`  | VARCHAR2(500) |       | Justificativa da atribuição.           |

---

### `ATIVIDADE`

Representa uma atividade realizada em uma unidade, que compõe um mapa.

| Coluna        | Tipo          | Chave | Descrição                         |
| ------------- | ------------- | ----- | --------------------------------- |
| `codigo`      | NUMBER        | PK    | Identificador único da atividade. |
| `mapa_codigo` | NUMBER        | FK    | Referência para `MAPA`.           |
| `descricao`   | VARCHAR2(255) |       | Descrição da atividade.           |

---

### `COMPETENCIA`

Representa uma competência necessária para realizar atividades.

| Coluna        | Tipo          | Chave | Descrição                           |
| ------------- | ------------- | ----- | ----------------------------------- |
| `codigo`      | NUMBER        | PK    | Identificador único da competência. |
| `mapa_codigo` | NUMBER        | FK    | Referência para `MAPA`.             |
| `descricao`   | VARCHAR2(255) |       | Descrição da competência.           |

---

### `COMPETENCIA_ATIVIDADE`

Tabela de associação que relaciona competências a atividades (relação N-N).

| Coluna               | Tipo   | Chave  | Descrição                      |
| -------------------- | ------ | ------ | ------------------------------ |
| `atividade_codigo`   | NUMBER | PK, FK | Referência para `ATIVIDADE`.   |
| `competencia_codigo` | NUMBER | PK, FK | Referência para `COMPETENCIA`. |

---

### `CONHECIMENTO`

Representa um conhecimento específico associado a uma atividade.

| Coluna             | Tipo          | Chave | Descrição                            |
| ------------------ | ------------- | ----- | ------------------------------------ |
| `codigo`           | NUMBER        | PK    | Identificador único do conhecimento. |
| `atividade_codigo` | NUMBER        | FK    | Referência para `ATIVIDADE`.         |
| `descricao`        | VARCHAR2(255) |       | Descrição do conhecimento.           |

---

### `MAPA`

Representa um mapa de competências.

| Coluna                         | Tipo           | Chave | Descrição                                                  |
| ------------------------------ | -------------- | ----- | ---------------------------------------------------------- |
| `codigo`                       | NUMBER         | PK    | Identificador único do mapa.                               |
| `data_hora_disponibilizado`    | TIMESTAMP      |       | Data e hora em que o mapa foi disponibilizado.             |
| `observacoes_disponibilizacao` | VARCHAR2(1000) |       | Observacoes fornecidas durante a disponibilização do mapa. |
| `sugestoes_apresentadas`       | VARCHAR2(1000) |       | Sugestões apresentadas durante a validação do mapa.        |
| `data_hora_homologado`         | TIMESTAMP      |       | Data e hora em que o mapa foi homologado.                  |

---

### `MOVIMENTACAO`

Histórico de movimentação de um subprocesso entre unidades.

| Coluna                   | Tipo          | Chave | Descrição                                    |
| ------------------------ | ------------- | ----- | -------------------------------------------- |
| `codigo`                 | NUMBER        | PK    | Identificador único da movimentação.         |
| `subprocesso_codigo`     | NUMBER        | FK    | Subprocesso movimentado (ref `SUBPROCESSO`). |
| `data_hora`              | TIMESTAMP     |       | Data e hora da movimentação.                 |
| `unidade_origem_codigo`  | NUMBER        | FK    | Unidade de origem (ref `UNIDADE`).           |
| `unidade_destino_codigo` | NUMBER        | FK    | Unidade de destino (ref `UNIDADE`).          |
| `descricao`              | VARCHAR2(255) |       | Descrição da movimentação.                   |

---

### `NOTIFICACAO`

Notificações enviadas entre unidades sobre um subprocesso.

| Coluna                   | Tipo          | Chave | Descrição                                  |
| ------------------------ | ------------- | ----- | ------------------------------------------ |
| `codigo`                 | NUMBER        | PK    | Identificador único da notificação.        |
| `subprocesso_codigo`     | NUMBER        | FK    | Subprocesso associado (ref `SUBPROCESSO`). |
| `data_hora`              | TIMESTAMP     |       | Data e hora da notificação.                |
| `unidade_origem_codigo`  | NUMBER        | FK    | Unidade de origem (ref `UNIDADE`).         |
| `unidade_destino_codigo` | NUMBER        | FK    | Unidade de destino (ref `UNIDADE`).        |
| `conteudo`               | VARCHAR2(500) |       | Conteúdo da notificação.                   |

---

### `PARAMETRO`

Armazena parâmetros de configuração do sistema.

| Coluna      | Tipo          | Chave | Descrição                         |
| ----------- | ------------- | ----- | --------------------------------- |
| `codigo`    | NUMBER        | PK    | Identificador único do parâmetro. |
| `chave`     | VARCHAR2(50)  |       | Chave do parâmetro.               |
| `descricao` | VARCHAR2(255) |       | Descrição do parâmetro.           |
| `valor`     | VARCHAR2(255) |       | Valor do parâmetro.               |

---

### `PROCESSO`

Representa um processo de negócio (Mapeamento, Revisão, Diagnóstico).

| Coluna             | Tipo          | Chave | Descrição                                                  |
| ------------------ | ------------- | ----- | ---------------------------------------------------------- |
| `codigo`           | NUMBER        | PK    | Identificador único do processo.                           |
| `data_criacao`     | TIMESTAMP     |       | Data de criação do processo.                               |
| `data_finalizacao` | TIMESTAMP     |       | Data de finalização do processo.                           |
| `data_limite`      | DATE          |       | Data limite para conclusão.                                |
| `descricao`        | VARCHAR2(255) |       | Descrição do processo.                                     |
| `situacao`         | VARCHAR2(20)  |       | Situação atual (`CRIADO`, `EM_ANDAMENTO`, `FINALIZADO`).   |
| `tipo`             | VARCHAR2(20)  |       | Tipo do processo (`MAPEAMENTO`, `REVISAO`, `DIAGNOSTICO`). |

---

### `SITUACAO_SUBPROCESSO`

Representa uma situação de um subprocesso.

| Coluna      | Tipo         | Chave | Descrição                           |
| ----------- | ------------ | ----- | ----------------------------------- |
| `id`        | VARCHAR2(50) | PK    | Identificador único do subprocesso. |
| `descricao` | VARCHAR2(50) |       | Descrição da situação processo.     |

---

### `SUBPROCESSO`

Representa uma etapa de um processo principal, associada a uma unidade.

| Coluna               | Tipo         | Chave | Descrição                                                   |
| -------------------- | ------------ | ----- | ----------------------------------------------------------- |
| `codigo`             | NUMBER       | PK    | Identificador único do subprocesso.                         |
| `processo_codigo`    | NUMBER       | FK    | Processo pai (ref `PROCESSO`).                              |
| `unidade_codigo`     | NUMBER       | FK    | Unidade do subprocesso (ref `UNIDADE`).                     |
| `mapa_codigo`        | NUMBER       | FK    | Mapa associado (ref `MAPA`).                                |
| `data_limite_etapa1` | DATE         |       | Data limite da etapa 1.                                     |
| `data_fim_etapa1`    | TIMESTAMP    |       | Data de fim da etapa 1.                                     |
| `data_limite_etapa2` | DATE         |       | Data limite da etapa 2.                                     |
| `data_fim_etapa2`    | TIMESTAMP    |       | Data de fim da etapa 2.                                     |
| `situacao_id`        | VARCHAR2(50) | FK    | Situação atual do subprocesso (ref `SITUACAO_SUBPROCESSO`). |

---

### `UNIDADE_MAPA`

Armazena o mapa vigente de uma unidade operacional ou interoperacional.

| Coluna                | Tipo   | Chave  | Descrição                                 |
| --------------------- | ------ | ------ | ----------------------------------------- |
| `unidade_codigo`      | NUMBER | PK. FK | Unidade organizacional (ref `UNIDADE`).   |
| `mapa_vigente_codigo` | NUMBER | PK, FK | Mapa vigente para a unidade (ref `MAPA`). |

---

### `UNIDADE_PROCESSO`

Armazena uma cópia (SNAPTSHOT) das unidades participantes do processo.

| Coluna                    | Tipo          | Chave | Descrição                                                             |
| ------------------------- | ------------- | ----- | --------------------------------------------------------------------- |
| `processo_codigo`         | NUMBER        | PK    | Processo pai (ref `PROCESSO`).                                        |
| `codigo`                  | NUMBER        | PK    | Identificador único da unidade.                                       |
| `nome`                    | VARCHAR2(255) |       | Nome da unidade.                                                      |
| `sigla`                   | VARCHAR2(20)  |       | Sigla da unidade.                                                     |
| `titular_titulo`          | VARCHAR2(12)  | FK    | Titular da unidade (ref `USUARIO`).                                   |
| `tipo`                    | VARCHAR2(20)  |       | Tipo da unidade (`OPERACIONAL`, `INTEROPERACIONAL`, `INTERMEDIARIA`). |
| `situacao`                | VARCHAR2(20)  |       | Situação da unidade `ATIVA`, `INATIVA`).                              |
| `unidade_superior_codigo` | NUMBER        | FK    | Unidade pai na hierarquia (ref `UNIDADE`).                            |

---

### `VINCULACAO_UNIDADE`

Registra o histórico de vinculação/subordinação entre unidades.

| Coluna                    | Tipo   | Chave | Descrição                                       |
| ------------------------- | ------ | ----- | ----------------------------------------------- |
| `codigo`                  | NUMBER | PK    | Identificador único.                            |
| `unidade_anterior_codigo` | NUMBER | FK    | Unidade anterior na vinculação (ref `UNIDADE`). |
| `unidade_atual_codigo`    | NUMBER | FK    | Unidade atual na vinculação (ref `UNIDADE`).    |

---

## Visões (integração com o SGRH)

### `VW_RESPONSABILIDADE`

Define a responsabilidade de um usuário sobre uma unidade.

| Coluna           | Tipo         | Chave | Descrição                                                |
| ---------------- | ------------ | ----- | -------------------------------------------------------- |
| `codigo`         | NUMBER       | PK    | Identificador único da responsabilidade.                 |
| `unidade_codigo` | NUMBER       | FK    | Unidade da responsabilidade (ref `UNIDADE`).             |
| `usuario_titulo` | VARCHAR2(12) | FK    | Usuário responsável `USUARIO`.                           |
| `tipo`           | VARCHAR2(20) |       | Tipo (`TITULAR`, `SUBSTITUTO`, `ATRIBUICAO_TEMPORARIA`). |
| `data_inicio`    | DATE         |       | Data de início da responsabilidade.                      |
| `data_fim`       | DATE         |       | Data de término da responsabilidade.                     |

---

### `VW_UNIDADE`

Representa uma unidade organizacional.

| Coluna                    | Tipo          | Chave | Descrição                                                             |
| ------------------------- | ------------- | ----- | --------------------------------------------------------------------- |
| `codigo`                  | NUMBER        | PK    | Identificador único da unidade.                                       |
| `nome`                    | VARCHAR2(255) |       | Nome da unidade.                                                      |
| `sigla`                   | VARCHAR2(20)  |       | Sigla da unidade.                                                     |
| `titular_titulo`          | VARCHAR2(12)  | FK    | Titular da unidade (ref `USUARIO`).                                   |
| `tipo`                    | VARCHAR2(20)  |       | Tipo da unidade (`OPERACIONAL`, `INTEROPERACIONAL`, `INTERMEDIARIA`). |
| `situacao`                | VARCHAR2(20)  |       | Situação da unidade `ATIVA`, `INATIVA`).                              |
| `unidade_superior_codigo` | NUMBER        | FK    | Unidade pai na hierarquia (ref `UNIDADE`).                            |

---

### `VW_USUARIO`

Representa um usuário do sistema.

| Coluna           | Tipo          | Chave | Descrição                                      |
| ---------------- | ------------- | ----- | ---------------------------------------------- |
| `titulo`         | VARCHAR2(12)  | PK    | Título funcional do usuário (chave primária).  |
| `nome`           | VARCHAR2(255) |       | Nome do usuário.                               |
| `email`          | VARCHAR2(255) |       | E-mail do usuário.                             |
| `ramal`          | VARCHAR2(20)  |       | Ramal do usuário.                              |
| `unidade_codigo` | NUMBER        | FK    | Unidade de lotação do usuário (ref `UNIDADE`). |

---

### `VW_USUARIO_PERFIL_UNIDADE`

Define os perfis de um usuário em uma unidade.

| Coluna           | Tipo         | Chave  | Descrição                                                   |
| ---------------- | ------------ | ------ | ----------------------------------------------------------- |
| `usuario_titulo` | VARCHAR2(12) | PK, FK | Usuário associado (ref `USUARIO`).                          |
| `perfil`         | VARCHAR2(10) | PK     | Perfil do usuário (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`). |
| `unidade_codigo` | NUMBER       | PK, FK | Unidade associada (ref `UNIDADE`).                          |
