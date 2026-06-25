O CDU-51 está próximo do implementável, mas hoje ele ainda tem algumas ambiguidades e pequenos conflitos com o restante
da especificação e com a semântica já existente no sistema. Os principais ajustes que eu faria no texto antes de
implementar são estes:

1. Em /abs/path/C:/sgc/specs/cdu-51.md, o passo 7 diz “para cada unidade marcada”, mas o passo 8 já fala de notificação
   consolidada para a unidade superior. Falta deixar explícito que:
    - os passos 8.1 a 8.4 são por unidade selecionada;
    - os passos 9.1 e 9.2 acontecem uma única vez por execução. Hoje a numeração também está inconsistente (7, 8.1,
      depois 8., 9.1, 9.2).

3. O efeito funcional precisa ser alinhado com o fluxo individual do aceite em /abs/ path/C:/sgc/specs/cdu-50.md. No
   individual, o aceite:
    - registra análise;
    - move para a unidade superior;
    - cria alerta;
    - envia e-mail. No bloco, a spec mudou o destino do alerta/e-mail:

    - por unidade: alerta e e-mail para a unidade do subprocesso;
    - consolidado: alerta e e-mail para a unidade superior. Isso é possível, mas é diferente do comportamento individual
      atual. Vale confirmar que essa divergência é intencional, porque ela muda a regra de negócio, não só a UI.

4. O texto do passo 8.3 está ambiguo
   Pelo restante do fluxo, parece que esse alerta é para a unidade subordinada informando que o aceite foi feito e o
   subprocesso seguiu para cima. Se for isso, eu deixaria explícito: “alerta para a unidade subordinada informando que
   seu diagnóstico foi aceito”.

5. Falta explicitar o comportamento quando a seleção ficar vazia. Como o modal começa com todos selecionados, a pessoa
   pode desmarcar tudo. A spec precisa dizer se:
    - o botão continua habilitado e o sistema mostra erro inline/global “Selecione ao menos uma unidade”; ou

    - o botão fica desabilitado. Pelo padrão de UX do projeto, eu recomendaria permitir o clique e mostrar validação
      contextual no modal.

6. Falta explicitar se existe campo Observação no aceite em bloco. No CDU-50 individual existe observação opcional. No
   CDU-51 não existe. Isso é ok, mas precisa ser uma decisão consciente, porque o backend atual grava "Avaliação em
   bloco" como observação fixa em lote em /abs/path/C:/sgc/backend/src/main/java/sgc/
   diagnostico/service/DiagnosticoFluxoService.java:327.

7. O critério de elegibilidade deveria repetir a situação técnica usada no sistema. Em /abs/path/C:/sgc/specs/cdu-51.md
   está “situação ‘Concluído’”, o que bate com o rótulo de UI. Isso está coerente com a situação técnica
   DIAGNOSTICO_CONCLUIDO, então aqui está claro; eu só manteria o texto exatamente assim para evitar reabrir a
   nomenclatura antiga “Avaliação de consenso concluída” que ainda aparece em /abs/ path/C:/sgc/specs/cdu-06.md.

Conclusão: a intenção do caso de uso está clara, mas eu pediria ajuste textual antes de implementar, principalmente para
fechar:

- terminologia aceitar vs validar,
- diferença exata entre o aceite individual e o em bloco,
- regra quando nenhuma unidade estiver selecionada,
- numeração/escopo dos passos por unidade vs consolidados.
