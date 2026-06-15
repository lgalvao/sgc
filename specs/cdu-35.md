# CDU-35 - Gerar relatório de andamento

**Ator:** ADMIN, GESTOR

## Fluxo principal

1. O usuário acessa o menu `Relatórios` na barra de navegação principal.

2. O sistema exibe cards com os relatórios disponíveis. 

3. O usuário clica no card `Andamento de processo`.

4. O sistema mostra a tela `Relatório de andamento` permitindo escolher o processo.
   - Se o perfil for GESTOR, a lista de processos para seleção é filtrada para exibir apenas aqueles que envolvem a sua unidade ou subordinadas, recursivamente.
   - Para o perfil ADMIN, todos os processos ativos são exibidos.

5. O usuário seleciona um processo e aciona o botão `Gerar`.

6. O sistema processa os dados e mostra o conteúdo do relatório em tela, organizado em grupos individuais para cada unidade participante, contendo:
    - **Cabeçalho:** Título, Processo, Tipo do processo, Quantidade total de unidades, Data limite da etapa 1 e Data limite da etapa 2.
    - **Cartão da unidade (Hierarquia visual):** Cada unidade é apresentada em um bloco com indentação.
        - **Título:** Sigla e nome da unidade.
        - **Informações gerais:** Situação atual do subprocesso, Localização e Data/hora da última movimentação.
        - **Etapa 1 (Cadastro):** Data limite e Data de conclusão.
        - **Etapa 2 (Mapa):** Data limite (com indicação de "Prazo ajustado" se divergir da Etapa 1) e Data de conclusão.
        - **Responsáveis:** Nome do titular da unidade e, se houver substituição ou atribuição temporária vigente, o nome do responsável atual com a indicação "(Substituição até `data`)", onde `data` é a data final da vigência da atribuição ou a da substituição.

7. O usuário clica no botão `PDF` para exportar o relatório.

8. O sistema gera um arquivo PDF seguindo o mesmo layout visual, com cabeçalho contendo brasão, nome do sistema, data/hora de geração e descrição do processo. O download é iniciado automaticamente. O padrão de nome deve ser `sgc-rel-andamento-YYYY-MM-DD.pdf`.
