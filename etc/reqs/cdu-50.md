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
   - logo abaixo dos dados gerais do subprocesso/unidade: uma lista com todos os servidores lotados na unidade do subprocesso -- exceto o responsável pela unidade. 
   Um botão `Histórico de análise`
   Um botão 'dropdown' `Ações` que abre subitens:
   - `Devolver para ajustes`;
   - `Registrar aceite`, para GESTOR;
   - `Homologar`, para ADMIN.
   **NOTA**: Os botões de ação serão habilitados/desabilidatos conforme as regras gerais acesso, na forma especificada em [design/acesso.md]

9. Se o usuário clicar em `Histórico de análise`, o sistema mostra os registros prévios de análise do subprocesso, contendo data/hora, unidade, resultado e observação.

---

Se o usuário optar por `Devolver para ajustes`:

10. O sistema solicita confirmação em um modal, com um campo `Justificativa`, de preenchimento obrigatório.

11. Caso o usuário confirme, o sistema:
    - registra análise com resultado `Devolução para ajustes`;
    - e muda a localização do subprocesso para a unidade imediatamente inferior.

12. O sistema envia uma notificação por e-mail para a unidade inferior que foi a origem da última movimentação.
    
13. O sistema cria internamente um alerta com estes campos:

    Descrição: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes"
    Processo: [DESCRICAO_PROCESSO]
    Data/hora: [Data/hora atual]
    Unidade de origem: [SIGLA_UNIDADE_ANALISE]
    Unidade de destino: [SIGLA_UNIDADE_DEVOLUCAO]

14. O Sistema mostra a mensagem `Devolução realizada`

---

Se o usuário optar por `Registrar aceite`:

15. O sistema solicita confirmação em um modal, com campo `Observação` opcional.

16. Caso o usuário confirme, o sistema registra análise com resultado `Aceite`.

17. O sistema muda a localização do subprocesso para a unidade imediatamente superior.

18. O sistema cria internamente um alerta com estes campos:

    Descrição: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] aceito"
    Processo: [DESCRICAO_PROCESSO]
    Data/hora: [Data/hora atual]
    Unidade de origem: [SIGLA_UNIDADE_ANALISE]
    Unidade de destino: [SIGLA_UNIDADE_SUPERIOR]

19. O sistema envia uma notificação por e-mail para a unidade imediatamente superior.

20. O sistema mostra a mensagem `Aceite registrado`.

---

Se o usuário optar por `Homologar`:

21. O sistema solicita confirmação em um modal, com texto simples, sem campo adicional.

22. Caso o usuário confirme, o sistema altera a situação do subprocesso para `Homologado`.

23. O sistema registra movimentação e análise de homologação.

24. O sistema mostra a mensagem `Diagnóstico homologado`.
