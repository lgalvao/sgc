# CDU-36 - Gerar relatório de mapas

**Ator:** ADMIN

## Descrição
Permite a extração consolidada dos mapas de competências (Atividades, Conhecimentos, Competências) das unidades.

## Regras de Negócio
- O relatório deve consolidar os dados dos mapas de competências vigentes (homologados) ou em elaboração, dependendo do filtro.
- O relatório deve ser estruturado de forma a permitir a análise de competências transversais (comuns a várias unidades).

## Fluxo principal

1. O usuário acessa a área de Relatórios.
2. O usuário seleciona a opção "Relatório de Mapas".
3. O usuário define os filtros:
    - Processo (Obrigatório)
    - Unidade (Opcional - se vazio, considera todas as unidades do processo)
    - Situação do Mapa (Opcional - ex: Apenas Homologados)
4. O usuário aciona a opção "Gerar Relatório".
5. O sistema processa os dados e gera um arquivo (ex: planilha Excel ou CSV) contendo as seguintes informações:
    - Unidade (Sigla e Nome)
    - Atividade (Descrição)
    - Conhecimentos associados (Lista concatenada ou linhas separadas)
    - Competências associadas (Lista concatenada ou linhas separadas)
6. O sistema disponibiliza o arquivo para download.
