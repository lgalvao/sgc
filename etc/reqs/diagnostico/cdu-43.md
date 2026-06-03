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
   - `Ações`, com os itens abaixo: 
     - `Manter avaliação de consenso` - sempre habilitado;
     - `Indicar impossibilidade` - habilitado se o usuário não estiver ba situação `Avaliação impossibilitada`;
    
5. O usuário pode analisar a situação do diagnóstico dos servidores e acionar as operações desejadas.