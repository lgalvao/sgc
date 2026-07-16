# CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos

## Atores

- GESTOR
- ADMIN

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN
- Processo de revisão em andamento, que tenha a unidade como participante
- Subprocesso com revisão do cadastro disponibilizada, e com localização atual na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário clica e um processo de revisão em andamento.

2. O sistema exibe a tela `Detalhes do processo`.

3. O usuário aciona a unidade cujo cadastro de atividades deseja validar.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada.

5. O usuário aciona o card `Atividades e conhecimentos`.

6. O sistema apresenta as atividades e conhecimentos da unidade na tela `Atividades e conhecimentos`, com:
   botões:
    - `Impactos no mapa`
    - `Histórico de análise`
    - botão *dropdown* `Ações`, com os itens:
        - `Devolver para ajustes`
        - `Registrar aceite` (caso o perfil seja GESTOR)
        - `Homologar` (caso o perfil seja ADMIN)

7. O botão `Impactos no mapa` será usado pelo usuário para verificar as competências da unidade que serão impactadas
   pela alteração realizada no cadastro.

8. Se o usuário acionar `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises prévias
   registradas para o cadastro de atividades desde a última disponibilização. As análises deverão ser apresentadas em
   uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas
   informações serão usadas como subsídio para a realização da análise pela unidade atual.

9. O usuário analisa as informações obtidas ao acionar `Impactos no mapa` e `Histórico de análise` e opta por
   aceitar/homologar ou devolver o cadastro da unidade para ajustes, conforme especificado a seguir.

---

10. Se o usuário optar por **devolver para ajustes** (perfil GESTOR ou ADMIN):

    10.1. O usuário aciona `Devolver para ajustes`.

    10.2. O sistema abre uma tela modal, com título "Devolução" e texto "Confirma a devolução do cadastro para
    ajustes?", campo `Justificativa` **obrigatório** e botões `Confirmar` ou `Cancelar`.

    10.3. Caso o usuário acione `Cancelar`, o sistema interrompe a operação de devolução do cadastro, permanecendo na
    tela `Atividades e conhecimentos`.

    10.4. O usuário informa a justificativa e escolhe `Confirmar`.

    10.5. O sistema registra uma análise de cadastro para o subprocesso com:
    - `Resultado`: "Devolução"
    - `Data/hora`: Data/hora atual
    - `Unidade`: :UNIDADE_ANALISE:
    - `Observação`

    10.6. O sistema identifica a unidade de devolução (:UNIDADE_DEVOLUCAO:) como sendo a unidade de origem da última
    movimentação do subprocesso.

    10.7. O sistema registra uma movimentação para o subprocesso com:
    - `Descrição`: "Revisão do cadastro devolvida para ajustes"
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: :UNIDADE_ANALISE:
    - `Unidade destino`: :UNIDADE_DEVOLUCAO:

    10.8. O sistema envia notificação por e-mail para :UNIDADE_DEVOLUCAO:.

      ```text
      Assunto: SGC: Revisão do cadastro de atividades e conhecimentos da :SIGLA_UNIDADE_SUBPROCESSO: devolvida para ajustes
      
      Prezado(a) responsável pela :SIGLA_UNIDADE_DEVOLUCAO:,
      
      A revisão do cadastro de atividades e conhecimentos da :SIGLA_UNIDADE_SUBPROCESSO: no processo
     :DESCRICAO_PROCESSO: foi devolvida para ajustes.

      Acompanhe o processo no sistema de Gestão de Competências: :URL_SISTEMA:.
      ```

    10.9. O sistema cria um alerta:
    - `Descrição`: "Revisão do cadastro da unidade :SIGLA_UNIDADE_SUBPROCESSO: devolvida para ajustes"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: :UNIDADE_ANALISE:
    - `Unidade de destino`: :UNIDADE_DEVOLUCAO:

    10.10. Se a unidade de devolução for a unidade do subprocesso, o sistema altera a situação do subprocesso para
    'Revisão do cadastro em andamento' e apaga a data/hora de conclusão da Etapa 1 do subprocesso.

    10.11. O sistema redireciona para o `Painel`, e mostra o *toast* "Devolução realizada".

---

### 11. Se o usuário optar por **registrar aceite** (perfil GESTOR):

11.1. O usuário aciona `Registrar aceite`.

11.2. O sistema abre um diálogo modal com título "Aceite" e texto "Confirma o aceite da revisão do cadastro de
atividades?", campo de observação opcional e botões `Confirmar` ou `Cancelar`.

11.3. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de aceite, permanecendo na tela
`Atividades e conhecimentos`.

11.4. O usuário opcionalmente informa a observação e aciona `Confirmar`.

11.5. O sistema registra uma análise de cadastro para o subprocesso:

- `Resultado`: "Aceite"
- `Data/hora`: Data/hora atual
- `Unidade`: :UNIDADE_ANALISE:
- `Observação`

11.6. O sistema registra uma movimentação para o subprocesso:

- `Descrição`: "Revisão do cadastro aceita"
- `Data/hora`: Data/hora atual
- `Unidade origem`: :UNIDADE_ANALISE:
- `Unidade destino`: :UNIDADE_SUPERIOR:

11.7. O sistema envia notificação por e-mail para a :UNIDADE_SUPERIOR:, neste modelo:

  ```text
  Assunto: SGC: Revisão do cadastro de atividades e conhecimentos da :SIGLA_UNIDADE_SUBPROCESSO: submetido para análise
  
  Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,

  A revisão do cadastro de atividades e conhecimentos da :SIGLA_UNIDADE_SUBPROCESSO: no processo :DESCRICAO_PROCESSO:
  foi submetida para análise por essa unidade.

  A análise já pode ser realizada no O sistema de Gestão de Competências (:URL_SISTEMA:).
  ```

11.8. O sistema cria um alerta:

- `Descrição`: "Revisão do cadastro da unidade :SIGLA_UNIDADE_SUBPROCESSO: submetida para análise"
- `Processo`: :DESCRICAO_PROCESSO:
- `Data/hora`: :DATA_HORA_ATUAL:
- `Unidade de origem`: :UNIDADE_ANALISE:
- `Unidade de destino`: :UNIDADE_SUPERIOR:.

11.9. O sistema redireciona para o `Painel` e mostra o *toast* "Aceite registrado".

---

### 12. Se o usuário optar por **homologar** (perfil ADMIN):

12.1. O usuário aciona `Homologar`.

#### 12.2. Se o sistema **não detectar impactos** no mapa de competências da unidade:

12.2.1. O sistema abre um diálogo de confirmação (título `Homologação do mapa de competências`) com a mensagem "A
revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade. Confirma a manutenção do mapa de
competências vigente?" e os botões `Homologar` ou `Cancelar`.

12.2.2. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação de homologação, permanecendo na tela
`Atividades e conhecimentos`.

12.2.3. O usuário aciona `Homologar`.

12.2.4. O sistema altera a situação do subprocesso da unidade para 'Mapa homologado'.

#### 12.3. Se o sistema **detectar impactos**:

12.3.1. O sistema abre um diálogo de confirmação com título "Homologação do cadastro", com mensagem "Confirma a
homologação?" e botões `Homologar` e `Cancelar`.

12.3.2. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação de homologação do cadastro,
permanecendo na mesma tela.

12.3.3. O usuário aciona `Homologar`.

12.3.4. O sistema registra uma movimentação para o subprocesso:

- `Data/hora`: Data/hora atual
- `Unidade origem`: ADMIN
- `Unidade destino`: ADMIN
- `Descrição`: 'Cadastro homologado'

12.3.5. O sistema altera a situação do subprocesso para 'Revisão do cadastro homologada'. Não são gerados nem alertas
nem notificações.

12.4. O sistema redireciona para a tela `Detalhes do subprocesso` e mostra o *toast* "Homologação efetivada".