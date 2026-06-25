# CDU-29 - Consultar histórico de processos

**Atores:** Todos

## Pré-condições

- Usuário logado (qualquer perfil)

## Fluxo principal

1. Na barra de navegação, o usuário aciona `Histórico`.

2. O sistema apresenta uma tabela com os processos com situação 'Finalizado', com estas colunas:
    - `Processo`: Descrição do processo.
    - `Tipo`: Tipo do processo.
    - `Finalizado em`: Data de finalização do processo
    - `Unidades participantes`: Lista de unidades participantes, agregando pelas unidades que tiverem todas as
      subunidades participando (da mesma forma usada no `Painel`)

   2.1. Os seguintes procesos finalizados serão mostrados, de acordo com o perfil:
    - perfis SERVIDOR e CHEFE: apenas processos que incluem a unidade do usuário;
    - perfil GESTOR: processos que incluem a unidade do usuário ou qualquer unidade subordinada (recursivamente);
    - perfil ADMIN: todos os processos.

3. O usuário clica em um processo para detalhamento.

4. O sistema apresenta a página `Detalhes do processo`, sem permitir mudanças ou mostrar botões de ação.