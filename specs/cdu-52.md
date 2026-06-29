# CDU-52 - Homologar diagnósticos em bloco

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Ao menos um processo de diagnóstico em andamento.
- Ao menos um subprocesso de unidade subordinada na situação 'Concluído' e localizado na unidade do usuário.

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`, especificada em [CDU-06 - Detalhar processo](cdu-06.md)

3. O usuário aciona `Homologar em bloco`

4. O sistema mostra um modal de confirmação:
    - Título: "Homologação de diagnósticos em bloco";
    - Texto: "Selecione as unidades cujos diagnósticos devem ser homologados";
    - Uma grade com as unidades na situação 'Concluído' e com subprocesso localizado na unidade do usuário, com um
      checkbox (pré-selecionado), sigla, nome e situação de cada unidade;
    - Botões `Cancelar` e `Homologar em bloco`.

5. O usuário seleciona as unidades a serem homologadas e aciona `Homologar em bloco`.
    - 5.1. Se o usuário desmarcar todas as unidades, o sistema mostra um alerta "Selecione ao menos uma unidade" e
      interrompe a operação.

6. O sistema atua, para cada unidade selecionada, da seguinte forma:

   6.1. Registra uma análise de validação para o subprocesso da unidade:
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ATUAL]
    - `Resultado`: "Homologação"

   6.2. Registra uma movimentação para o subprocesso da unidade:
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: ADMIN
    - `Unidade destino`: ADMIN
    - `Descrição`: "Homologação"

   A localização do subprocesso não é alterada.

7. O sistema mostra um *toast* `Diagnósticos homologados`. 
