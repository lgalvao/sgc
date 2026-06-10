# CDU-50 - Analisar diagnóstico

Ator: GESTOR, ADMIN

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação `Em andamento`.

2. O sistema mostra a tela `Detalhes do processo` com uma tabela hierárquica contendo as unidades participantes; mostra
   para cada unidade:
    - sigla da unidade;
    - nome da unidade;
    - situação atual do diagnóstico da unidade.

   2.1. Para o perfil GESTOR, o conteúdo da tabela se limita à unidade e suas unidades subordinadas a ela,
   recursivamente.

   2.2. Para o perfil ADMIN, a tabela mostra todas as unidades participantes.

3. O usuário aciona uma unidade com diagnóstico concluído.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada, conforme o caso de
   uso [CDU-42.md](cdu-42.md)`.

5. Se o usuário clicar em `Histórico de análise`, o sistema mostra os registros prévios de análise do subprocesso,
   contendo data/hora, unidade, resultado e observação.

---

Se o usuário optar por `Devolver para ajustes`:

6. O sistema solicita confirmação em um modal, com um campo `Justificativa`, de preenchimento obrigatório.

7. Caso o usuário confirme, o sistema:
    - registra análise com resultado `Devolução para ajustes`;
    - muda a localização do subprocesso para a unidade imediatamente inferior.

8. O sistema envia uma notificação por e-mail para a unidade inferior que foi a origem da última movimentação.

9. O sistema cria internamente um alerta com estes campos:

   Descrição: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes"
   Processo: [DESCRICAO_PROCESSO]
   Data/hora: [Data/hora atual]
   Unidade de origem: [SIGLA_UNIDADE_ANALISE]
   Unidade de destino: [SIGLA_UNIDADE_DEVOLUCAO]

10. O sistema mostra a mensagem `Devolução realizada`.

---

Se o usuário optar por `Registrar aceite`:

11. O sistema solicita confirmação em um modal, com campo `Observação` opcional.

12. Caso o usuário confirme, o sistema registra análise com resultado `Aceite`.

13. O sistema muda a localização do subprocesso para a unidade imediatamente superior.

14. O sistema cria internamente um alerta com estes campos:

Descrição: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] aceito"
Processo: [DESCRICAO_PROCESSO]
Data/hora: [Data/hora atual]
Unidade de origem: [SIGLA_UNIDADE_ANALISE]
Unidade de destino: [SIGLA_UNIDADE_SUPERIOR]

15. O sistema envia uma notificação por e-mail para a unidade imediatamente superior.

16. O sistema mostra a mensagem `Aceite registrado`.

---

Se o usuário optar por `Homologar`:

17. O sistema solicita confirmação em um modal, com texto simples, sem campo adicional.

18. Caso o usuário confirme, o sistema altera a situação do subprocesso para `Homologado`.

19. O sistema registra movimentação e análise de homologação.

20. O sistema mostra a mensagem `Diagnóstico homologado`.
