# CDU-41 - Acompanhar diagnóstico da equipe
[REVISADO]

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade.
   
3. O usuário clica no card `Diagnóstico da equipe`.
   
4. O sistema apresenta a tela `Diagnóstico da equipe`, listando os servidores lotados na unidade do subprocesso.

5. Para cada servidor, o sistema mostra:
   - `Nome`;
   - `Situação`, com a situação atual da avaliação individual e estes valores possíveis:
     - `Autoavaliação não iniciada`;
     - `Autoavaliação concluída`;
     - `Avaliação de consenso criada`;
     - `Avaliação de consenso aprovada`;
     - `Avaliação impossibilitada`.

6. Conforme a situação, o sistema mostra as seguintes ações, condicionalmente:
   - `Criar avaliação de consenso`, quando a situação for `Autoavaliação concluída`;
   - `Editar avaliação de consenso`, quando a situação for `Consenso criado` ou `Consenso aprovado`;
   - `Indicar impossibilidade`, enquanto a avaliação individual ainda não estiver impossibilitada;
   - `Visualizar consenso`, quando já existir consenso criado para o servidor.

7. O usuário acompanha as pendências da equipe, identifica os servidores ainda não concluídos e aciona as operações desejadas.