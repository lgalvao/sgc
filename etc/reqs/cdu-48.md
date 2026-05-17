# CDU-48 - Monitorar andamento do diagnóstico

Ator: GESTOR, ADMIN

## Pré-condições

- Login realizado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. Na tela `Detalhes do processo`, o sistema apresenta uma árvore com as unidades participantes do processo dentro do
   escopo visível ao usuário.

4. Para cada unidade apresentada, o sistema mostra:
   - sigla e nome;
   - situação atual do subprocesso;
   - data limite atual da unidade;
   - localização atual do subprocesso.

5. Para o perfil GESTOR, a árvore exibida deve se limitar à própria unidade e às unidades subordinadas a ela,
   recursivamente.

6. Para o perfil ADMIN, a árvore exibida deve contemplar todas as unidades participantes do processo.

7. O usuário pode detalhar uma unidade para abrir a sua tela `Diagnóstico da unidade`.

8. O sistema pode destacar visualmente unidades com prazo vencido ou próximo do vencimento.

## Observação

PENDÊNCIA DE REFINAMENTO: os campos mostrados na árvore e o destaque visual de prazos foram definidos como conjunto
mínimo de acompanhamento. Confirmar depois se o processo exige outros indicadores, percentuais ou agregados na própria
tela de detalhes do processo.
