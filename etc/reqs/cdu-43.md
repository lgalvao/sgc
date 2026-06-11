# CDU-42 - Visualizar detalhes de subprocesso de diagnóstico: GESTOR e ADMIN

Ator:  GESTOR ou ADMIN

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN.
- Existência de processo de diagnóstico em andamento.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`, com uma tabela hierárquica contendo as unidades participantes e a
   situação atual do subprocesso de cada unidade, de acordo com estas restrições:

    - Para o perfil GESTOR, a tabela se limita à unidade do usuário e às suas subordinadas, recursivamente.
    - Para o perfil ADMIN, a tabela mostra todas as unidades participantes.

3. O usuário aciona uma unidade na tabela.

4. O sistema mostra a tela `Detalhes do subprocesso` da unidade selecionada. Os elementos da tela serão:

- cabeçalho com dados gerais do subprocesso e da unidade, como detalhado em [CDU-07](cdu-07.md),
- botão `Histórico de análise`; sempre habilitado;
- controle 'drop-down' `Ações`, que dá acesso a estas ações (habilitadas apenas se localização do subprocesso for a
  unidade do usuárioo)
    - `Devolver para ajustes` para GESTOR e ADMIN;
    - `Registrar aceite`, apenas para GESTOR;
    - `Homologar`, apenas para ADMIN;
    
- lista dos servidores participantes da unidade, **exceto o responsável pela unidade**;
- seção de movimentações do subprocesso;