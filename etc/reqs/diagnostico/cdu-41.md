# CDU-41 - Acompanhar diagnóstico da equipe

Ator: CHEFE

Maturidade: Média

Base principal: Fluxo narrado e validado na reunião, com complementos mínimos de modelagem da tela.

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` da unidade.

3. O usuário clica no card `Diagnóstico da equipe`.

4. O sistema apresenta a tela `Diagnóstico da equipe`, listando os servidores participantes do snapshot da unidade no
   processo.

5. Para cada servidor, o sistema mostra:
   - nome;
   - situação atual da avaliação individual, com um dos valores:
     - `Autoavaliação não iniciada`;
     - `Autoavaliação concluída`;
     - `Avaliação de consenso criado`;
     - `Avaliação de Consenso aprovado`;
     - `Avaliação impossibilitada`.

6. Conforme a situação do servidor, o sistema pode exibir as seguintes ações:
   - `Criar avaliação de consenso`, quando a situação for `Autoavaliação concluída`;
   - `Editar avaliação de consenso`, quando a situação for `Consenso criado` ou `Consenso aprovado`;
   - `Indicar impossibilidade`, enquanto a avaliação individual ainda não estiver impossibilitada;
   - `Visualizar consenso`, quando já existir consenso criado para o servidor.

   PENDÊNCIA DE REFINAMENTO: a lista acima representa as ações mínimas inferidas a partir do fluxo. Confirmar depois se
   haverá ações adicionais de acompanhamento, filtros ou destaques específicos na tela.

7. O usuário acompanha as pendências da equipe, identifica os servidores ainda não concluídos e aciona as operações
   desejadas.
