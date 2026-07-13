# CDU-51 - Aceitar diagnósticos em bloco

## Atores

- GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR.
- Ao menos um processo de diagnóstico em andamento.
- Ao menos um subprocesso de unidade subordinada na situação 'Concluído', localizado na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`, especificada em [CDU-06 - Detalhar processo](cdu-06.md)

3. O usuário aciona `Aceitar em bloco`

4. O sistema mostra um modal de confirmação, com título "Aceite de diagnósticos em bloco",texto: "Selecione as unidades
   cujos diagnósticos devem ser aceitos" e estes elementos:
    - Uma grade com os subprocessos em situação 'Concluído' e localizadas na unidade do usuário, com checkbox
      pré-selecionado, sigla, nome e situação de cada unidade;
    - Botões `Cancelar` e `Aceitar em bloco`.

5. O usuário seleciona as unidades a serem aceitas e aciona `Aceitar em bloco`.
    - Se o usuário desmarcar todas as unidades, o sistema mostra um alerta "Selecione ao menos uma unidade" e interrompe
      a operação.

6. O sistema atua, para cada unidade selecionada, da seguinte forma:

   6.1. Registra uma análise de validação para o subprocesso da unidade:
    - `Resultado`: 'Aceite'
    - `Data/hora`: :DATA_HORA:
    - `Unidade`: :UNIDADE_ATUAL:
   
   6.2. Registra uma movimentação para o subprocesso da unidade:
    - `Descrição`: "Aceite"
    - `Data/hora`: :DATA_HORA:
    - `Unidade origem`: :UNIDADE_ATUAL:
    - `Unidade destino`: :UNIDADE_SUPERIOR:

   Isso muda a localização do subprocesso para a unidade superior.

7. O sistema envia notificação consolidada por e-mail para a unidade superior:
      ```text
      Assunto: SGC: Diagnósticos submetidos para análise
   
      Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,
   
      Os diagnósticos das unidades :LISTA_UNIDADES_SELECIONADAS: no processo :DESCRICAO_PROCESSO:
      foram submetidos para análise por essa unidade.
   
      As análises já podem ser realizadas no Sistema de Gestão de Competências (SGC): :URL_SISTEMA:.
      ```

8. O sistema registra um alerta para a unidade superior:
    - `Descrição`: "Diagnóstico aceito para unidades subordinadas"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: :DATA_HORA_ATUAL:
    - `Unidade de origem`: :UNIDADE_ANALISE:
    - `Unidade de destino`: :UNIDADE_SUPERIOR:

9. O sistema mostra um *toast* `Diagnósticos aceitos`. 
