# CDU-50 - Validar diagnósticos em bloco

Ator: GESTOR

Maturidade: Média

Base principal: Fluxo narrado e validado na reunião, complementado por paralelismo com as ações em bloco dos processos já existentes.

## Pré-condições

- Login realizado com perfil GESTOR
- Existência de processo de diagnóstico em andamento
- Existência de mais de uma unidade subordinada com subprocesso em situação 'Concluído' e localização atual na unidade
  do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. Havendo unidades elegíveis, o sistema habilita o botão `Validar diagnósticos em bloco`.

4. O usuário clica em `Validar diagnósticos em bloco`.

5. O sistema abre modal de confirmação contendo:
   - título `Validação de diagnósticos em bloco`;
   - texto `Selecione as unidades cujos diagnósticos deverão ser aceitos`;
   - lista das unidades elegíveis, apresentadas com checkbox selecionado por padrão, sigla e nome;
   - botões `Cancelar` e `Registrar aceite`.

6. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação.

7. O usuário escolhe as unidades desejadas e clica em `Registrar aceite`.

8. Para cada unidade selecionada, o sistema:
   - registra uma análise de resultado `Aceite`;
   - registra uma movimentação com descrição `Diagnóstico aceito`;
   - encaminha o subprocesso para a unidade superior.

9. O sistema envia uma única notificação consolidada por e-mail para a unidade superior:

    ```text
    Assunto: SGC: Diagnósticos de unidades subordinadas submetidos para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    Os diagnósticos das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] no processo [DESCRICAO_PROCESSO] foram aceitos e submetidos para análise por essa unidade.

    As análises já podem ser realizadas no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

10. O sistema cria internamente um alerta consolidado com:
    - `Descrição`: "Diagnósticos das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] submetidos para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

11. O sistema mostra a mensagem `Diagnósticos aceitos em bloco`.

## Observação

PENDÊNCIA DE REFINAMENTO: esta versão assume uma notificação e um alerta consolidados por operação em bloco, mantendo
as movimentações individualizadas por subprocesso. Confirmar depois se a área de negócio deseja também um registro
agregado adicional no histórico.
