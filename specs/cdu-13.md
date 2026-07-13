# CDU-13 - Analisar cadastro de atividades e conhecimentos

## Atores

- ADMIN
- GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN
- Processo de mapeamento em andamento
- Subprocesso com cadastro já disponibilizado, localizado na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de mapeamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. O usuário clica na unidade cujo cadastro de atividades deseja validar.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada.

5. O usuário aciona o card `Atividades e conhecimentos`.

6. O sistema mostra as atividades e conhecimentos da unidade na tela `Atividades e conhecimentos`, com o botão
   `Histórico de análise`, além de um botão *dropdown* `Ações` com os itens:
    - `Devolver para ajustes`
    - `Registrar aceite`, caso perfil seja GESTOR
    - `Homologar`, caso perfil seja ADMIN

7. Se o usuário acionar `Histórico de análise`, o sistema mostra um modal com os dados das análises prévias registradas
   para o cadastro, desde a última disponibilização, em uma tabela com:
    - data/hora,
    - sigla da unidade,
    - resultado ('Devolução' ou 'Aceite')
    - justificativa/observações.

8. O usuário analisa as informações e opta por aceitar/homologar ou devolver o para ajustes, como detalhdo a seguir.

---

### 9. Se usuário optar por **devolver para ajustes** (perfil GESTOR ou ADMIN):

9.1. O usuário aciona `Devolver para ajustes`.

9.2. O sistema abre um modal com título "Devolução" e texto "Confirma a devolução do cadastro para ajustes?", além de um
campo`Justificativa` obrigatório e botões `Cancelar` e `Devolver`.

9.3. O usuário informa a justificativa e aciona `Devolver`.

9.4. O sistema registra uma análise de cadastro para o subprocesso:

- `Resultado`: 'Devolução'
- `Data/hora`: :DATA_HORA_ATUAL:
- `Unidade`: :UNIDADE_ANALISE:
- `Justificativa`

9.5. O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do subprocesso --
referenciada aqui como :UNIDADE_DEVOLUCAO:.

9.6. O sistema registra uma movimentação para o subprocesso:

- `Descrição`: "Cadastro devolvido para ajustes"
- `Data/hora`: :DATA_HORA_ATUAL:
- `Unidade origem`: :UNIDADE_ANALISE:
- `Unidade destino`: :UNIDADE_DEVOLUCAO:

9.8. Se a unidade de devolução for a própria unidade do subprocesso, o sistema altera a situação do subprocesso para
'Cadastro em andamento' e apaga a data/hora de conclusão da Etapa 1 do subprocesso.

9.9. O sistema envia notificação por e-mail para a unidade de devolução:

   ```text
      Assunto: SGC: Cadastro de atividades e conhecimentos devolvido para ajustes

      Prezado(a) responsável pela :SIGLA_UNIDADE_DEVOLUCAO:,

      O cadastro de atividades e conhecimentos da sua unidade, no processo :DESCRICAO_PROCESSO:, foi
      devolvido para ajustes.

      Faça os ajustes no Sistema de Gestão de Competências (SGC): :URL_SISTEMA:.
   ```

9.10. O sistema cria um alerta:

- `Descrição`: "Cadastro da unidade :SIGLA_UNIDADE_SUBPROCESSO: devolvido para ajustes"
- `Processo`: :DESCRICAO_PROCESSO:
- `Data/hora`: :DATA_HORA_ATUAL:
- `Unidade de origem`: :UNIDADE_ANALISE:
- `Unidade de destino`: :UNIDADE_DEVOLUCAO:.

9.11. O sistema redireciona para o `Painel` e mostra o *toast* "Devolução realizada".

--- 

### 10. Se usuário optar por **aceitar** (perfil GESTOR):

10.1. O usuário aciona `Registrar aceite`.

10.2. O sistema abre um modal com título "Aceite" e texto "Confirma o aceite do cadastro de atividades?", campo
`Observação` (opcional) e botões `Aceitar` e `Cancelar`.

10.3. Caso o usuário acione `Cancelar`, o sistema interrompe a operação de aceite, permanecendo na mesma tela.

10.4. O usuário opcionalmente informa a observação e aciona `Aceitar`.

10.5. O sistema registra uma análise de cadastro para o subprocesso:

- `Resultado`: 'Aceite'
- `Data/hora`: :DATA_HORA_ATUAL:
- `Unidade`: :UNIDADE_ANALISE:
- `Observação`

10.6. O sistema registra uma movimentação para o subprocesso:

- `Descrição`: "Cadastro aceito"
- `Data/hora`: :DATA_HORA_ATUAL:
- `Unidade origem`: :UNIDADE_ANALISE:
- `Unidade destino`: :UNIDADE_SUPERIOR:

10.7. O sistema envia notificação por e-mail para a unidade superior:

   ```text
   Assunto: SGC: Cadastro de atividades e conhecimentos da :SIGLA_UNIDADE_SUBPROCESSO: submetido para análise

   Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,

   O cadastro de atividades e conhecimentos da :SIGLA_UNIDADE_SUBPROCESSO:, no processo :DESCRICAO_PROCESSO:, 
   foi submetido para análise por essa unidade.

   A análise já pode ser realizada no sistema de Gestão de Competências (:URL_SISTEMA:).
   ```

10.8. O sistema cria um alerta:

- `Descrição`: "Cadastro da unidade :SIGLA_UNIDADE_SUBPROCESSO: submetido para análise"
- `Processo`: :DESCRICAO_PROCESSO:
- `Data/hora`: :DATA_HORA_ATUAL:
- `Unidade de origem`: :UNIDADE_ANALISE:
- `Unidade de destino`: :UNIDADE_SUPERIOR:

10.9. O sistema redireciona para o `Painel` e mostra o *toast* "Aceite registrado".

---

### 11. Se o usuário optar por **homologar** (apenas perfil ADMIN):

11.1. O usuário aciona `Homologar`.

11.2. O sistema abre um modal de confirmação, com título "Homologação do cadastro", texto "Confirma a homologação?"
e botões `Cancelar` e `Homologar`.

11.3. O usuário aciona `Homologar`.

11.4. O sistema registra uma movimentação para o subprocesso:

- `Descrição`: "Cadastro homologado"
- `Data/hora`: :DATA_HORA_ATUAL:
- `Unidade origem`: ADMIN
- `Unidade destino`: ADMIN

11.5. O sistema altera a situação do subprocesso para 'Cadastro homologado'.

11.6. O sistema redireciona para o `Painel` e mostra o *toast* "Homologação efetivada".