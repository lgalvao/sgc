# CDU-13 - Analisar cadastro de atividades e conhecimentos

## Atores

- ADMIN
- GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN
- Processo de mapeamento iniciado que tenha a unidade como participante
- Subprocesso com cadastro de atividades e conhecimentos já disponibilizado, e com localização atual na unidade do
  usuário.

## Fluxo principal

1. No `Painel`, o usuário clica no processo de mapeamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. O usuário clica na unidade subordinada cujo cadastro de atividades deseja validar.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada.

5. O usuário aciona o card `Atividades e conhecimentos`.

6. O sistema mostra as atividades e conhecimentos da unidade na tela `Atividades e conhecimentos`, com os botões:
    - `Histórico de análise`
    - `Devolver para ajustes`
    - `Registrar aceite`, caso perfil seja GESTOR
    - `Homologar`, caso perfil seja ADMIN.

7. Se o usuário acionar `Histórico de análise`, o sistema mostra uma tela modal com os dados das análises prévias
   registradas para o cadastro, desde a última disponibilização, em uma tabela com:
    - data/hora,
    - sigla da unidade,
    - resultado ('Devolução' ou 'Aceite')
    - justificativa/observações.

8. O usuário analisa as informações e opta por aceitar/homologar ou devolver o cadastro para ajustes, como detalhdo a
   seguir.

---

9. Se optar por **devolver para ajustes**:

   9.1. Usuário aciona `Devolver para ajustes`.

   9.2. O sistema abre modal com título "Devolução" e texto "Confirma a devolução do cadastro para ajustes?", um campo
   `Justificativa`, obrigatório e os botões `Cancelar` e `Devolver`.

   9.3. O usuário informa a justificativa e aciona `Devolver`.

   9.4. O sistema registra uma análise de cadastro para o subprocesso:
    - `Resultado`: 'Devolução'
    - `Data/hora`: Data/hora atual
    - `Unidade`: [SIGLA_UNIDADE_ANALISE]
    - `Justificativa`: [Justificativa fornecida].

   9.5. O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do
   subprocesso, referenciada aqui como [SIGLA_UNIDADE_DEVOLUCAO].

   9.6. O sistema registra uma movimentação para o subprocesso:
    - `Descrição`: 'Devolução do cadastro para ajustes'
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: [SIGLA_UNIDADE_ANALISE]

   9.8. Se a unidade de devolução for a própria unidade do subprocesso, o sistema altera a situação do subprocesso para
   'Cadastro em andamento' e apaga a data/hora de conclusão da etapa 1 do subprocesso da unidade.

   9.9. O sistema envia notificação por e-mail para a unidade de devolução:
   ```text
      Assunto: SGC: Cadastro de atividades e conhecimentos devolvido para ajustes

      Prezado(a) responsável pela [SIGLA_UNIDADE_DEVOLUCAO],

      O cadastro de atividades e conhecimentos da sua unidade, no processo [DESCRIÇÃO_PROCESSO], foi
      devolvido para ajustes.

      Faça os ajustes no Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
   ```

   9.10. O sistema cria um alerta:
    - `Descrição`: "Cadastro da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: [SIGLA_UNIDADE_ANALISE]
    - `Unidade de destino`: [SIGLA_UNIDADE_DEVOLUCAO].

   9.11. O sistema redireciona para o `Painel`, e mostra *toast* "Devolução realizada".

--- 

10. Se optar por **aceitar** (perfil GESTOR):

10.1. Usuário clica em `Registrar aceite`.

10.2. O sistema abre um diálogo modal (título "Aceite") com a pergunta "Confirma o aceite do cadastro de atividades?",
um campo para preenchimento de uma observação opcional e os botões Confirmar ou Cancelar.

10.3. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação de aceite, permanecendo na mesma tela.

10.4. O usuário opcionalmente informa a observação e escolhe `Confirmar`.

10.5. O sistema registra uma análise de cadastro para o subprocesso:

- `Data/hora`: Data/hora atual
- `Unidade`: [SIGLA_UNIDADE_ANALISE]
- `Resultado`: 'Aceite'
- `Observação`: [Observação, caso fornecida]

10.6. O sistema registra uma movimentação para o subprocesso:

- `Data/hora`: Data/hora atual
- `Unidade origem`: [SIGLA_UNIDADE_ANALISE]
- `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
- `Descrição`: 'Cadastro aceito'

10.7. O sistema envia notificação por e-mail para a unidade superior:

   ```text
   Assunto: SGC: Cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   O cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO], no processo [DESCRICAO_PROCESSO], 
   foi submetido para análise por essa unidade.

   A análise já pode ser realizada no sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

10.8. O sistema cria um alerta:

- `Descrição`: "Cadastro da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
- `Processo`: [DESCRICA_PROCESSO]
- `Data/hora`: Data/hora atual
- `Unidade de origem`: [SIGLA_UNIDADE_ANALISE]
- `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR].

10.9. O sistema redireciona para o `Painel` e mostra o *toast* "Aceite registrado".

---

11. Se optar por **homologar** (apenas para perfil ADMIN):

11.1. O usuário aciona `Homologar`.

11.2. O sistema abre um modal de confirmação, com título "Homologação do cadastro" e texto "Confirma a homologação?"
e botões `Cancelar` e `Homologar`.

11.3. O usuário aciona `Homologar`.

11.4. O sistema registra uma movimentação para o subprocesso:

- `Descrição`: 'Cadastro homologado'
- `Data/hora`: Data/hora atual
- `Unidade origem`: 'ADMIN'
- `Unidade destino`: 'ADMIN'

11.5. O sistema altera a situação do subprocesso da unidade para 'Cadastro homologado'.

12. O sistema redireciona para o `Painel` e mostra o *toast* "Homologação efetivada".