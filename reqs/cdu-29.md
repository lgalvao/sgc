# CDU-29 - Consultar histórico de processos

**Atores:** ADMIN/GESTOR/CHEFE

## Pré-condições

- Usuário logado com qualquer perfil, exceto SERVIDOR

## Fluxo principal

1. Em qualquer tela do sistema, na barra de navegação, usuário clica `Histórico`.

2. Sistema apresenta uma tabela com todos os processos com situação 'Finalizado', com:
   - `Processo`: Descrição do processo.
   - `Tipo`: Tipo do processo.
   - `Finalizado em`: Data de finalização do processo
   - `Unidades participantes`: Lista de unidades participantes, agregando pelas unidades que tiverem todas as subunidades participando (da mesma forma usada no Painel)

3. Usuário clica em um processo para detalhamento.

4. O sistema apresenta a página `Detalhes do processo`, sem permitir mudanças ou mostrar botões de ação.
