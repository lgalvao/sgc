# CDU-42 - Visualizar detalhes de subprocesso de diagnóstico: CHEFE e SERVIDOR

Ator: SERVIDOR ou CHEFE

## Pré-condições

- Usuário logado com perfil CHEFE ou SERVIDOR.
- Existência de processo de diagnóstico em andamento.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema navega diretamente para a tela `Detalhes do subprocesso` da unidade do usuário, a qual mostra dados gerais
   do subprocesso e da unidade, como detalhado em [CDU-07](cdu-07.md), além de cards acionáveis que variam de acordo com
   o perfil.

2.1. Para o perfil CHEFE, a tela apresenta:

- grade de servidores da unidade, contendo `Nome` e `Situação` individual de cada servidor;
- controle de `Ações` para cada servidor, que dá acesso às ações:
    - `Avaliação de consenso`, com a descrição "Manter avaliações de consenso das competências'
    - `Impossibilidade`, habilitado quando a situação individual não for 'Avaliação impossibilitada';
- card `Situação de capacitação`, sempre habilitado;
- botão `Concluir diagnóstico`;
- seção de movimentações do subprocesso (como em [CDU-07 seção 2.3](cdu-07.md);

2.2. Para o perfil SERVIDOR, a tela apresenta:

- card `Autoavaliação`, com a descrição "Avaliar Importância e Domínio de cada competência'; sempre habilitado,
- card `Avaliação de consenso`, com a descrição "Consultar/aprovar avaliações de consenso"; habilitado quando a situação
  do servidor for 'Avaliação de consenso criada'.
- Para o perfil SERVIDOR, **não serão mostrados** a grade de servidores da unidade nem as movimentações do subprocesso.