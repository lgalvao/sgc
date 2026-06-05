# CDU-49 - Acompanhar diagnóstico de unidades subordinadas 

Ator: GESTOR, ADMIN

## Pré-condições

- Login realizado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo` com uma tabela hierárquica contendo as unidades participantes do processo. Para cada unidade, mostra:
   - sigla da unidade 
   - nome de unidade;
   - situação atual do subprocesso da unidade.

   3.1. Para o perfil GESTOR, a tabela hierarquica deve se limitar à própria unidade do usuário e às unidades subordinadas a ela, recursivamente.
   
   3.2. Para o perfil ADMIN, a árvore exibida deve incluir todas as unidades participantes do processo.

4. O usuário clica em uma unidade na tabela 
   
5. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada.
   
6. O sistema exibe inline, na própria tela `Detalhes do subprocesso`, o monitoramento de diagnóstico com detalhes sobre as situações de cada servidor.
