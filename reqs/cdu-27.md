# CDU-27 - Consultar histórico de processos

**Ator:** ADMIN/GESTOR/CHEFE

**Pré-condições:**

Usuário logado com qualquer perfil, exceto SERVIDOR

**Fluxo principal:**

- Em qualquer tela do sistema, na barra de navegação, usuário clica 'Histórico'.
- Sistema apresenta uma tabela com todos os processos com situação 'Finalizado', com:
- Processo: Descrição do processo.
- Tipo: Tipo do processo.
- Finalizado em: Data de finalização do processo
- Unidades participantes: Lista de unidades participantes, agregando pelas unidades que tiverem todas as subunidades participando.
- Usuário clica em um processo para detalhamento.
- O sistema apresenta a página Detalhes do processo, sem permitir mudanças ou mostrar botões de ação.
