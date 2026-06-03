# CDU-49 - Acompanhar diagnóstico de unidades subordinadas 

Ator: GESTOR, ADMIN

Maturidade: Média

Base principal: Respostas do usuário sobre monitoramento na tela de detalhes do processo, complementadas por modelagem de acompanhamento.

## Pré-condições

- Login realizado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. Na tela `Detalhes do processo`, o sistema apresenta uma tabela hierárquica com as unidades participantes do processo. Para cada unidade, mostra:
   - sigla e nome;
   - situação atual do subprocesso;
   - data limite da etapa atual do subprocesso;
   - localização atual do subprocesso.

   3.1. Para o perfil GESTOR, a tabela hierarquica deve se limitar à própria unidade do usuário e às unidades subordinadas a ela, recursivamente.

   3.2. Para o perfil ADMIN, a árvore exibida deve contemplar todas as unidades participantes do processo.

4. O usuário clica em uma unidade 
   
5. O sistema mostra a tela `Detalhe do subprocesso` para a unidade selecionada.
   
6. O usuário clica no card `Monitoramento`.

7. O sistema mostra a tela `Monitoramento de diagnóstico`.