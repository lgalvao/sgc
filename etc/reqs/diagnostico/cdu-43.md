# CDU-43 - Acompanhar diagnóstico da unidade

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade.
   
3. O usuário clica no card `Monitoramento`.
   
4. O sistema apresenta a tela `Monitoramento de diagnóstico`, com a situação dos servidores lotados na unidade do usuário. Para cada servidor, são mostrados:
   - `Nome` : nome completo do servidor
   - `Situação`: situação atual da avaliação individual e estes valores possíveis:
     - `Autoavaliação não realizada`;
     - `Autoavaliação concluída`;
     - `Avaliação de consenso criada`;
     - `Avaliação de consenso aprovada`;
     - `Avaliação impossibilitada`.
   - `Ações`, com zero ou mais itens, que variam com a situação de cada servidor:
     - `Criar avaliação de consenso`, quando a situação for `Autoavaliação concluída`;
     - `Editar avaliação de consenso`, quando a situação for `Avaliação de consenso criado` ou `Avaliação de consenso aprovado`;
     - `Indicar impossibilidade`, enquanto a avaliação individual ainda não estiver impossibilitada;
     - `Visualizar consenso`, quando já existir avaliação de consenso criado para o servidor.
    
5. O usuário pode analisar a situação do diagnóstico dos servidores e aciona as operações necessárias.