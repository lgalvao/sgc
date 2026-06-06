# CDU-50 - Analisar diagnóstico

Ator: GESTOR, ADMIN

Maturidade: Média

Base principal: fluxo negocial acordado no PDF e revisão temática da reunião de diagnóstico.

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento
- Existência de unidade acessível ao usuário com diagnóstico concluído e pendente de análise hierárquica ou homologação

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação `Em andamento`.

2. O sistema mostra a tela `Detalhes do processo` com uma árvore das unidades participantes. Para cada unidade, mostra:
   - sigla da unidade;
   - nome da unidade;
   - situação atual do diagnóstico da unidade.

3. Para o perfil GESTOR, a árvore exibida se limita à própria unidade do usuário e às unidades subordinadas a ela, recursivamente.

4. Para o perfil ADMIN, a árvore exibida inclui todas as unidades participantes do processo.

5. O usuário seleciona uma unidade com diagnóstico concluído.

6. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada, contendo:
   - dados gerais da unidade;
   - situação atual do diagnóstico da unidade;
   - lista dos servidores da unidade e suas situações individuais;
   - avaliação de consenso vigente de cada servidor;
   - situação de capacitação registrada por competência;
   - histórico de movimentações e análises do subprocesso.

7. O sistema respeita as regras de visibilidade aplicáveis ao perfil do usuário.

   7.1. O servidor consulta apenas a própria avaliação de consenso.

   7.2. A chefia da unidade consulta as informações necessárias para manter o consenso e concluir o diagnóstico da própria unidade.

   7.3. Perfis superiores consultam, no sistema, as informações necessárias para analisar ou homologar o diagnóstico das unidades sob seu escopo.

   7.4. Relatórios oficiais exportáveis não devem ser confundidos com essa consulta operacional: relatórios institucionais devem ser agregados e sem identificação nominal de servidores.

8. O sistema exibe as ações disponíveis conforme o perfil e a situação do subprocesso:
   - `Histórico de análise`;
   - `Devolver para ajustes`;
   - `Registrar aceite`, para GESTOR;
   - `Homologar`, para ADMIN.

9. Se o usuário clicar em `Histórico de análise`, o sistema mostra os registros prévios de análise do subprocesso, contendo data/hora, unidade, resultado e observação.

10. Se o usuário optar por `Devolver para ajustes`:

    10.1. O sistema solicita confirmação e permite informar observação.

    10.2. Caso o usuário cancele, o sistema interrompe a operação.

    10.3. Caso o usuário confirme, o sistema registra análise com resultado `Devolução para ajustes`.

    10.4. O sistema devolve o diagnóstico à unidade responsável pela retificação.

    10.5. O CHEFE da unidade passa a poder realizar os ajustes necessários, atuar novamente junto aos SERVIDORES para nova aprovação de consenso quando necessário e concluir novamente o diagnóstico.

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

## Observação

- A análise formal continua tendo como objeto o diagnóstico da unidade, não servidores isolados.
- O efeito exato da devolução sobre consensos já aprovados precisa ser mantido explícito na implementação: nova edição de consenso exige nova aprovação do servidor.
- A regra de visibilidade detalhada precisa ser validada como decisão de produto, porque a reunião registrou tensão entre privacidade, transparência e necessidade de análise hierárquica.
