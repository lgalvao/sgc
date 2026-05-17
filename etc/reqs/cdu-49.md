# CDU-49 - Analisar diagnóstico de unidade

Ator: GESTOR, ADMIN

Maturidade: Média

Base principal: Fluxo narrado e validado na reunião, complementado por paralelismo com os fluxos hierárquicos já existentes.

## Pré-condições

- Login realizado com perfil GESTOR ou ADMIN
- Processo de diagnóstico em andamento
- Subprocesso com situação 'Concluído' e localização atual na unidade do usuário; ou
- Subprocesso com situação 'Homologado', para fins de consulta histórica do diagnóstico

## Fluxo principal

1. Na tela `Detalhes do processo`, o usuário clica em uma unidade elegível.

2. O sistema mostra a tela `Diagnóstico da unidade`.

3. O sistema apresenta:
   - dados gerais da unidade;
   - histórico de movimentações do subprocesso;
   - lista dos servidores da unidade e suas situações individuais;
   - avaliação de consenso vigente de cada servidor;
   - informações de ocupações críticas.

4. Para perfis superiores na hierarquia, o sistema apresenta apenas os dados do consenso vigente de cada servidor, e
   não os valores brutos da autoavaliação original.

5. O sistema exibe os botões:
   - `Histórico de análise`;
   - `Devolver para ajustes`;
   - `Registrar aceite`, para o perfil GESTOR;
   - `Homologar`, para o perfil ADMIN.

6. Se o usuário clicar em `Histórico de análise`, o sistema mostra, em modal, os registros prévios de análise do
   subprocesso, contendo data/hora, unidade, resultado e observação.

7. Se o usuário optar por `Devolver para ajustes`:

   7.1. O sistema abre modal com:
   - título `Devolver diagnóstico`;
   - pergunta `Confirma a devolução do diagnóstico para ajustes?`;
   - campo opcional `Observação`;
   - botões `Cancelar` e `Devolver`.

   7.2. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação.

   7.3. O usuário opcionalmente informa a observação e escolhe `Devolver`.

   7.4. O sistema registra uma análise com:
   - `Data/hora`: [Data/hora atual];
   - `Unidade`: [SIGLA_UNIDADE_ANALISE];
   - `Resultado`: 'Devolução para ajustes';
   - `Observação`: observação informada, se houver.

   7.5. O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do
   subprocesso.

   7.6. O sistema registra uma movimentação com:
   - `Descrição`: 'Diagnóstico devolvido para ajustes';
   - `Data/hora`: [Data/hora atual];
   - `Unidade origem`: [SIGLA_UNIDADE_ANALISE];
   - `Unidade destino`: [SIGLA_UNIDADE_DEVOLUCAO].

   7.7. Se a unidade de devolução for a própria unidade do subprocesso, o sistema altera a situação do subprocesso para
   'Em andamento' e apaga a data/hora de conclusão da etapa atual.

   PENDÊNCIA DE REFINAMENTO: esta especificação trata a devolução como devolução do subprocesso inteiro, sem invalidar
   automaticamente todos os consensos aprovados da unidade. Confirmar se a área de negócio deseja manter essa
   reabertura ampla com ajuste seletivo pela chefia, ou se a devolução deverá forçar reinício mais abrangente.

   7.8. O sistema envia notificação por e-mail para a unidade de devolução:

   ```text
   Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes

   Prezado(a) responsável pela [SIGLA_UNIDADE_DEVOLUCAO],

   O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi devolvido para ajustes.

   Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

   7.9. O sistema cria internamente um alerta com:
   - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes"
   - `Processo`: [DESCRICAO_PROCESSO]
   - `Data/hora`: [Data/hora atual]
   - `Unidade de origem`: [SIGLA_UNIDADE_ANALISE]
   - `Unidade de destino`: [SIGLA_UNIDADE_DEVOLUCAO]

   7.10. O sistema redireciona para o `Painel` e mostra a mensagem `Devolução realizada`.

8. Se o usuário optar por `Registrar aceite` (perfil GESTOR):

   8.1. O sistema abre modal com:
   - título `Registrar aceite do diagnóstico`;
   - texto `Confirma o aceite do diagnóstico da unidade?`;
   - campo opcional `Observação`;
   - botões `Cancelar` e `Confirmar`.

   8.2. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação.

   8.3. O usuário opcionalmente informa a observação e escolhe `Confirmar`.

   8.4. O sistema registra uma análise com:
   - `Data/hora`: [Data/hora atual];
   - `Unidade`: [SIGLA_UNIDADE_ANALISE];
   - `Resultado`: 'Aceite';
   - `Observação`: observação informada, se houver.

   8.5. O sistema registra uma movimentação com:
   - `Data/hora`: [Data/hora atual];
   - `Unidade origem`: [SIGLA_UNIDADE_ANALISE];
   - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR];
   - `Descrição`: 'Diagnóstico aceito'.

   8.6. O sistema envia notificação por e-mail para a unidade superior:

   ```text
   Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi aceito e submetido para análise por essa unidade.

   A análise já pode ser realizada no Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

   8.7. O sistema cria internamente um alerta com:
   - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
   - `Processo`: [DESCRICAO_PROCESSO]
   - `Data/hora`: [Data/hora atual]
   - `Unidade de origem`: [SIGLA_UNIDADE_ANALISE]
   - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

   8.8. O sistema redireciona para o `Painel` e mostra a mensagem `Aceite registrado`.

9. Se o usuário optar por `Homologar` (perfil ADMIN):

   9.1. O sistema mostra diálogo de confirmação com o título `Homologar diagnóstico`, a pergunta `Confirma a
   homologação do diagnóstico da unidade?` e os botões `Cancelar` e `Homologar`.

   9.2. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação.

   9.3. O usuário confirma.

   9.4. O sistema altera a situação do subprocesso para 'Homologado'.

   9.5. O sistema registra uma movimentação com:
   - `Data/hora`: [Data/hora atual];
   - `Unidade origem`: 'ADMIN';
   - `Unidade destino`: 'ADMIN';
   - `Descrição`: 'Diagnóstico homologado'.

   9.6. O sistema envia notificação por e-mail para a unidade do subprocesso:

   ```text
   Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] homologado

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

   O diagnóstico da sua unidade no processo [DESCRICAO_PROCESSO] foi homologado.

   Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

   9.7. O sistema cria internamente um alerta com:
   - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] homologado"
   - `Processo`: [DESCRICAO_PROCESSO]
   - `Data/hora`: [Data/hora atual]
   - `Unidade de origem`: ADMIN
   - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

   9.8. O sistema redireciona para o `Painel` e mostra a mensagem `Diagnóstico homologado`.

## Observação

PENDÊNCIA DE REFINAMENTO: a granularidade exata da análise hierárquica ainda pode evoluir. A versão atual assume que o
objeto formal de validação/devolução é o subprocesso da unidade, e não servidores individuais isolados.
