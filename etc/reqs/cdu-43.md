# CDU-43 - Acompanhar diagnóstico da unidade

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade, com a situação dos servidores lotados na unidade. Para cada servidor, são mostrados:
   - `Nome` : nome completo do servidor
   - `Situação`: situação individual do servidor
   - `Ações`, com os itens abaixo: 
     - `Avaliação de consenso` - sempre habilitado;
     - `Indicar impossibilidade` - habilitado se o usuário já não estiver ba situação 'Avaliação impossibilitada';
    
3. O usuário analisa a situação do diagnóstico dos servidores e aciona as operações desejadas.