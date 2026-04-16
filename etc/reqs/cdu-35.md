# CDU-35 - Gerar relatório de andamento

**Ator:** ADMIN ou GESTOR

## Fluxo principal

1. O usuário acessa o menu `Relatórios` na barra de navegação principal.

2. O sistema exibe cards com os relatórios disponíveis. O usuário clica no card `Andamento de processo`.

3. O sistema exibe a tela de geração do relatório. 
   - Se o perfil for GESTOR, a lista de processos para seleção é filtrada para exibir apenas aqueles que envolvem a sua unidade ou subordinadas. 
   - Para o perfil ADMIN, todos os processos ativos são exibidos.

4. O usuário seleciona o processo desejado no campo de seleção e clica no botão `Gerar relatório`.

5. O sistema processa os dados e exibe o relatório em tela, organizado em cartões individuais para cada unidade participante, contendo:
   - **Cabeçalho do relatório:** Título, Processo selecionado, Tipo do processo, Quantidade total de unidades e Data limite geral do processo.
   - **Cartão da Unidade (Hierarquia Visual):** Cada unidade é apresentada em um bloco com borda lateral distintiva.
     - **Título:** Sigla e Nome da unidade.
     - **Informações Gerais:** Situação atual do subprocesso, Localização (Unidade Superior) e Data/hora da última movimentação.
     - **Etapa 1 (Cadastro):** Data limite e Data de conclusão.
     - **Etapa 2 (Mapa):** Data limite (com indicação de "Prazo ajustado" se divergir da etapa 1) e Data de conclusão.
     - **Responsáveis:** Nome do titular da unidade e, se houver substituição vigente, o nome do responsável atual com a indicação "(Substituição)".

6. O usuário pode clicar no botão `PDF` para exportar o relatório.

7. O sistema gera um arquivo PDF seguindo o mesmo layout visual (otimizado para impressão em preto e branco) e inicia o download automaticamente com o padrão de nome `relatorio-andamento-[id-processo].pdf`.
