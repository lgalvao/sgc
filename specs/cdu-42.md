# CDU-42 - Visualizar detalhes de subprocesso de diagnóstico: CHEFE e SERVIDOR

Ator: CHEFE ou SERVIDOR

## Pré-condições

- Usuário logado com perfil CHEFE ou SERVIDOR.
- Existência de processo de diagnóstico em andamento.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema navega diretamente para a tela `Detalhes do subprocesso` da unidade do usuário, a qual mostra dados gerais
   do subprocesso/unidade, como detalhado em [CDU-07](cdu-07.md), além de cards acionáveis e outros elementos, que
   variam de acordo com o perfil.

   2.1. Para o perfil CHEFE:
    - botões `Histórico de análise` e `Concluir diagnóstico` no cabeçalho;
    - lista de servidores da unidade, contendo `Nome` e `Situação` individual de cada servidor e um botão `Ações`
      *drop-down* para cada servidor, com as ações:
        - `Manter avaliação de consenso`, habilitado apenas na situação 'Autoavaliação criada', ou posterior;
        - `Indicar impossibilidade`, habilitado sempre, exceto nas situações 'Avaliação impossibilitada' e 'Avaliação de
          consenso aprovada';
        - `Desfazer impossibilidade`, habilitado apenas quando a situação for 'Avaliação impossibilitada';
    - card acionável `Situação de capacitação`, com a descrição "Capacitação de cada servidor nas competências da
      unidade"; habilitado quando ao menos um servidor tiver situação 'Autoavaliação criada' ou posterior;
    - seção de movimentações do subprocesso (como em [CDU-07 seção 2.3](cdu-07.md));

   2.2. Para o perfil SERVIDOR:
    - card acionável `Autoavaliação`, com a descrição "Avaliar importância e domínio de cada competência'; sempre
      habilitado;
    - card acionável `Avaliação de consenso`, com a descrição "Consultar/aprovar avaliação de consenso"; habilitado
      quando a situação for 'Avaliação de consenso criada', ou posterior;
    - Para este perfil, **não serão mostrados** a lista de servidores da unidade nem as movimentações do subprocesso.