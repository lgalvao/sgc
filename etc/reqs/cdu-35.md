# CDU-35 - Gerar relatório de andamento

**Ator:** ADMIN ou GESTOR

## Fluxo principal

1. O usuário acessa `Relatórios` na barra principal.

2. O usuário seleciona a opção `Andamento de processo`.

3. O sistema mostra uma tela com uma lista de processos. Se o perfil for GESTOR, os processos serão filtrados para apenas os envolvendo a sua unidade ou subordinadas. Para perfil ADMIN, todos os processos são exibidos.

4. O usuário seleciona o processo desejado e clica em `Gerar` 

4. O sistema exibe o relatório em tela contendo as seguintes informações:
   - Título do relatório e data/hora de geração 
   - Detalhes do processo selecionado: Descrição, Data limite,  
   - Para cada unidade participante do processo:
         - Sigla da unidade
         - Nome da unidade
         - Situação atual do subprocesso da unidade, para o processo selecionado
         - Data/hora da última movimentação
         - Localização
         - Data limite da etapa 1
         - Data de conclusão da etapa 1
         - Data limite da etapa 2
         - Data de conclusão da etapa 2
         - Nome do titular da unidade
         - Nome do responsável atual pela unidade (se não for o titular)

5. O usuário pode optar por exportar os dados para PDF clicando no botao `PDF`.

6. O sistema gera o arquivo selecionado e o disponibiliza para download.