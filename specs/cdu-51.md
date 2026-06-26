# CDU-51 - Aceitar diagnósticos em bloco

**Ator:** GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR.
- Ao menos um processo de diagnóstico em andamento.
- Ao menos um subprocesso de unidade subordinada na situação 'Concluído', localizado na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`, especificada em [CDU-06 - Detalhar processo](cdu-06.md)

3. O usuário aciona `Aceitar em bloco`

4. O sistema mostra um modal de confirmação:
    - Título: "Aceite de diagnósticos em bloco";
    - Texto: "Selecione as unidades cujos diagnósticos devem ser aceitos";
    - Uma grade com as unidades que estejam em situação 'Concluído' e localizadas na unidade do usuário, com checkbox
      (pré-selecionado), sigla, nome e situação de cada unidade;
    - Botões `Cancelar` e `Aceitar em bloco`.

5. O usuário seleciona as unidades a serem aceitas e aciona `Aceitar em bloco`.
    - 5.1. Se o usuário desmarcar todas as unidades, o sistema mostra um alerta "Selecione ao menos uma unidade" e
      interrompe a operação.

6. O sistema atua, para cada unidade selecionada, da seguinte forma:

   6.1. Registra uma análise de validação para o subprocesso da unidade:
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ATUAL]
    - `Resultado`: "Aceite"

   6.2. Registra uma movimentação para o subprocesso da unidade:
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
    - `Descrição`: "Aceite"

   Isso muda a localização do subprocesso para a unidade superior.

7. O sistema envia notificação consolidada por e-mail para a unidade superior:
      ```text
      Assunto: SGC: Diagnósticos submetidos para análise
   
      Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
   
      Os diagnósticos das unidades [LISTA_UNIDADES_SELECIONADAS] no processo [DESCRICAO_PROCESSO]
      foram submetidos para análise por essa unidade.
   
      As análises já podem ser realizadas no Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
      ```

8. O sistema registra um alerta para a unidade superior:
    - `Descrição`: "Diagnóstico aceito para unidades subordinadas"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

9. O sistema mostra um *toast* `Diagnósticos aceitos`. 