## **UC-001 - Iniciar Processo de Mapeamento**

**Ator principal**: SEDOC

**Pré-condições**: Usuário autenticado com perfil SEDOC.

**Fluxo principal:**

1. Usuário abre a página inicial do sistema: Painel.
2. O Sistema mostra painel listando os processos de mapeamento em andamento e estatísticas gerais sobre os processos.
3. SEDOC clica em Criar Processo de Mapeamento.
4. Sistema mostra:
   4.1. Campo de descrição do processo
   4.2. Campo de data limite para término do processo
   4.3. Arvore de unidades organizacionais atualizadas (lidas do SGRH) com checkboxes para cada uma.
   4.4. Botões Salvar e Iniciar processo.
5. SEDOC fornece os dados solicitados e seleciona unidades participantes.
6. Se SEDOC clicar em Salvar.
   6.1. Sistema verifica se já há algum processo em andamento para alguma das unidades selecionadas. Se houver, informa a impossibilidade de continuar. Caso contrário, cria o processo internamente, colocando-o na situação 'Não iniciado' e redireciona para o Painel.
7. Se SEDOC clicar em 'Iniciar processo':
   7.1. Sistema envia notificações para as unidades participantes. As notificações são enviadas para os e-mails das unidades e também visualizadas pelas responsáveis pelas unidades como alertas na tela inicial do sistema.
8. O Sistema envia notificações consolidadas para as unidades superiores informando o início do processo de mapeamento das unidades subordinadas a elas.
9. Sistema muda a a situação do processo para 'Iniciado' e volta para a página Painel.