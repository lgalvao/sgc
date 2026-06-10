# CDU-42 - Visualizar detalhes de subprocesso de diagnóstico

Atores: SERVIDOR, CHEFE, GESTOR, ADMIN

## Pré-condições

- Usuário logado.
- Existência de processo de diagnóstico em andamento.
- Existência de subprocesso de diagnóstico acessível ao perfil logado.

## Fluxo principal

1. O usuário acessa um processo de diagnóstico em andamento.

2. Se o perfil for SERVIDOR ou CHEFE, o sistema mostra a tela `Detalhes do subprocesso` da unidade do usuário.

3. Se o perfil for GESTOR ou ADMIN:

   3.1. O sistema mostra a tela `Detalhes do processo`, com uma tabela hierárquica contendo as unidades participantes e
   a situação atual do subprocesso de cada unidade.

   3.2. Para o perfil GESTOR, a tabela se limita à unidade do usuário e às suas subordinadas, recursivamente.

   3.3. Para o perfil ADMIN, a tabela mostra todas as unidades participantes.

   3.4. O usuário aciona uma unidade na tabela.

   3.5. O sistema mostra a tela `Detalhes do subprocesso` da unidade selecionada.

4. A tela `Detalhes do subprocesso` apresenta, para todos os perfis, dados gerais do subprocesso e dados gerais da
   unidade. O cabeçalho dessa página é idêntico ao mostrado para os outros tipos de processos; veja detalhes no caso de
   uso [CDU-07 - Detalhar subprocesso de mapeamento ou revisão])

5. Para o perfil SERVIDOR, a tela apresenta:
    - card `Autoavaliação`, sempre habilitado;
    - card `Avaliação de consenso`, habilitado apenas quando a situação do servidor for 'Avaliação de consenso criada'.

  Para este perfil, **não serão mostrados** a grade de servidores da unidade nem as movimentações;

6. Para o perfil CHEFE, a tela apresenta:
    - grade de servidores da unidade, contendo `Nome` e `Situação` individual de cada servidor;
    - controle de `Ações` para cada servidor, que dá acesso às ações:
        - `Avaliação de consenso`;
        - `Impossibilidade`, habilitado quando a situação individual não for 'Avaliação impossibilitada';
    - card `Situação de capacitação`, sempre habilitado;
    - botão `Concluir diagnóstico`;
   - seção de movimentações do subprocesso; 
   
7. Para os perfis GESTOR e ADMIN, a tela apresenta:
    - lista dos servidores participantes da unidade, *exceto o responsável pela unidade*;
    - botão `Ações` no cabeçalho, que dá acesso a estas ações, habilitadas ou não de acordo com a situação/localização
      do subprocesso
        - `Devolver para ajustes` para GESTOR e ADMIN; ;
        - `Registrar aceite`, apenas para GESTOR; 
        - `Homologar`, apenas para ADMIN; 
    - botão `Histórico de análise`; sempre habilitado;
   - seção de movimentações do subprocesso;
