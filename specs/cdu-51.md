# CDU-51 - Aceitar diagnósticos em bloco

**Ator:** GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR.
- Ao menos um processo de diagnóstico em andamento.
- Ao menos um subprocesso de unidade subordinada na situação 'Concluído', localizado na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo` como especificado em [CDU-06 - Detalhar processo](cdu-06.md)

3. O usuário aciona `Aceitar diagnósticos em bloco`

4. O sistema mostra um modal de confirmação, com os elementos a seguir:
    - título: "Aceite de diagnósticos em bloco";
    - texto: "Selecione as unidades cujos diagnósticos deverão ser aceitos";
    - grade com as unidades na situação 'Concluído' e localizado na unidade do usuário, com um checkbox (selecionado),
      sigla, nome e situação de cada unidade;
    - botões `Cancelar` e `Aceitar em bloco`.

6. O usuário determina quais unidades serão aceitas, marcando ou desmarcando as checkboxes, e aciona `Aceitar em bloco`.

7. O sistema atua, para cada unidade marcada, da seguinte forma:

   7.1. Registra uma análise de validação para o subprocesso:
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ATUAL]
    - `Resultado`: "Aceite"

   7.2. Registra uma movimentação para o subprocesso:
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]
    - `Descrição`: "Aceite"

   7.3. Registra um alerta:
    - `Data/hora`: [Data/hora atual]
    - `Descrição`: "Diagnóstico aceito"
    - `Processo`: [DESCRIÇÃO_PROCESSO]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

8. O sistema gera uma notificação e um alerta consolidados para a unidade superior,

   8.1. Envia uma única notificação consolidada por e-mail para a unidade superior, com o modelo a seguir:
      ```text
      Assunto: SGC: Diagnósticos submetidos para análise
   
      Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
   
      Os diagnósticos das unidades [LISTA_UNIDADES_MARCADAS] no processo [DESCRICAO_PROCESSO]
      foram submetidos para análise por essa unidade.
   
      As análises já podem ser realizadas no Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
      ```

   8.2. Registra um único alerta para a unidade superior:
    - `Descrição`: "Diagnóstico aceito para unidades subordinadas"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

9. O sistema mostra um *toast* `Diagnósticos aceitos em bloco` e permanece na mesma tela. 