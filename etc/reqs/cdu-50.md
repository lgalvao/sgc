# CDU-50 - Analisar diagnóstico

Ator: GESTOR, ADMIN

## Pré-condições
- Usuário logado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação `Em andamento`.

2. O sistema mostra a tela `Detalhes do processo` com uma tabela hierárquica contendo as unidades participantes; mostra para cada unidade:
   - sigla da unidade;
   - nome da unidade;
   - situação atual do diagnóstico da unidade.

   2.1. Para o perfil GESTOR, o conteúdo da tabela se limita à unidade e suas unidades subordinadas a ela, recursivamente.

   2.2. Para o perfil ADMIN, a tabela mostra todas as unidades participantes.

5. O usuário aciona uma unidade com diagnóstico concluído.

6. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada, contendo:
   - dados gerais do subprocesso: desc. subprocesso, desc. processo pai, localização atual, situação do subprocesso
   - dados gerais da unidade: sigla, nome, responsável
   - histórico de movimentações
     *NOTA*: Os dados acima são exatamente análogos aos mostrados em subprocessos de mapeamento e de revisão
   - logo abaixo dos dados gerais do subprocesso/unidade, é mostrada uma lista com todos os servidores lotados na unidade do subprocesso -- exceto o responsável pela unidade. 
   Um botão `Histórico de análise`
   Um botão 'dropdown' `Ações` que abre subitens:
   - `Devolver para ajustes`;
   - `Registrar aceite`, para GESTOR;
   - `Homologar`, para ADMIN.
   **NOTA**: Os botões de ação serão habilitados/desabilidatos conforme as regras gerais acesso, na forma especificada em [design/acesso.md]

9. Se o usuário clicar em `Histórico de análise`, o sistema mostra os registros prévios de análise do subprocesso, contendo data/hora, unidade, resultado e observação.

10. Se o usuário optar por `Devolver para ajustes`:
    10.1. O sistema solicita confirmação e permite informar observação.
    10.2. Caso o usuário cancele, o sistema interrompe a operação.
    10.3. Caso o usuário confirme, o sistema registra análise com resultado `Devolução para ajustes`.
    10.4. O sistema devolve o diagnóstico à unidade imediatamente inferior.
    10.6. O sistema notifica a unidade responsável pela retificação.
    10.7. O sistema mostra a mensagem `Devolução realizada`.

11. Se o usuário optar por `Registrar aceite`:
    11.1. O sistema solicita confirmação e permite informar observação.
    11.2. Caso o usuário cancele, o sistema interrompe a operação.
    11.3. Caso o usuário confirme, o sistema registra análise com resultado `Aceite`.
    11.4. O sistema encaminha o subprocesso para análise da unidade hierarquicamente superior.
    11.5. O sistema notifica a unidade superior.
    11.6. O sistema mostra a mensagem `Aceite registrado`.

12. Se o usuário optar por `Homologar`:
    12.1. O sistema solicita confirmação.
    12.2. Caso o usuário cancele, o sistema interrompe a operação.
    12.3. Caso o usuário confirme, o sistema altera a situação do subprocesso para `Homologado`.
    12.4. O sistema registra movimentação e análise de homologação.
    12.5. O sistema notifica a unidade do subprocesso.
    12.6. O sistema mostra a mensagem `Diagnóstico homologado`.
