# CDU-35 - Gerar relatório de andamento

**Ator:** ADMIN

## Descrição
Permite a visualização e exportação do status atual de todos os subprocessos de um Processo de Gestão de Competências.

## Regras de Negócio
- O relatório deve listar todas as unidades participantes do processo selecionado.
- Para cada unidade, deve ser exibida a situação atual do seu subprocesso.
- O relatório deve permitir filtragem por unidade ou situação.

## Fluxo principal

1. O usuário acessa a área de Relatórios.
2. O usuário seleciona a opção "Relatório de Andamento".
3. O usuário seleciona o Processo desejado (ex: "Mapeamento 2023").
4. O sistema exibe o relatório em tela contendo as seguintes colunas:
    - Sigla da Unidade
    - Nome da Unidade
    - Situação Atual (ex: Cadastro em Andamento, Homologado)
    - Data da última movimentação
    - Responsável (Titular)
5. O usuário pode optar por exportar os dados (CSV, PDF).
6. O sistema gera o arquivo selecionado e o disponibiliza para download.
