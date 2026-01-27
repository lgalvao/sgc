# CDU-36 - Gerar relatório de mapas

**Ator:** ADMIN

## Descrição

Permite a extração consolidada dos mapas de competências (Atividades, Conhecimentos, Competências) das unidades.

## Fluxo principal

1. O usuário acessa Relatórios na barra de navegacao.

2. O usuário seleciona a opção "Mapas".

3. O usuário define os filtros:

    - Processo (Obrigatório)
    - Unidade (Opcional - se vazio, considera todas as unidades do processo)

4. O usuário aciona a opção "Gerar".

5. O sistema processa os dados e gera um arquivo PDF, contendo, para cada mapa, as seguintes informações:

    - Unidade (Sigla e Nome)
    - Para cada competencia:
        - Descricao da competência
            - Atividades da competencia
                - Conhecimentos da atividade

6. O sistema disponibiliza o arquivo para download.
