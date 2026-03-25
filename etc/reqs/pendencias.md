# Pendências consolidadas da reanálise CDU x E2E

Documento criado na rodada de reanálise dos 36 pares `cdu-xx.md` -> `cdu-xx.spec.ts`.

## Critérios de priorização
- **P0**: quebra de regra de negócio, autorização, ou fluxo crítico fim-a-fim.
- **P1**: cobertura funcional parcial com risco médio (falta de cenário de perfil/unidade/situação).
- **P2**: melhoria de robustez/manutenibilidade (massa de dados, asserts complementares, estabilidade).

## Principais mudanças necessárias nos testes E2E
- **P0** Cobrir sistematicamente variações por perfil (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`) quando o requisito diferencia permissões/visibilidade.
- **P0** Validar regras de autorização e escopo por unidade ativa/hierarquia com massa controlada (cenários positivo e negativo).
- **P1** Aumentar asserts de efeito colateral: criação/edição/exclusão refletindo na tela e em consultas subsequentes.
- **P1** Para tabelas, validar conteúdo + ordenação real (não só atributo visual de sorting).
- **P1** Validar fluxos de navegação clicável entre páginas conforme cada CDU (entrada, retorno e redirecionamentos esperados).
- **P2** Reduzir fragilidade com helpers específicos por domínio e massa de dados explícita por cenário.

## Principais mudanças necessárias no sistema (backend/frontend)
- **P0** Revisar aderência entre regra de negócio documentada e comportamento entregue quando houver divergência recorrente nos alinhamentos.
- **P0** Garantir rastreabilidade de permissões no backend (anotação de autorização + testes de integração por ação).
- **P1** Melhorar observabilidade funcional para E2E: estados e mensagens determinísticas, `data-testid` estáveis e eventos de feedback claros.
- **P1** Uniformizar nomenclaturas de estados/campos entre requisito, API e UI para reduzir ambiguidades de validação.
- **P2** Documentar massas mínimas por CDU para execução reprodutível local/CI.

## Temas com maior recorrência na varredura automatizada
- Revisar regra: data, hora: 12 ocorrência(s).
- Criar massa/asserts de alertas: 10 ocorrência(s).
- Cobrir perfil SERVIDOR: 4 ocorrência(s).
- Revisar regra: registra, internamente: 4 ocorrência(s).
- Revisar regra: unidades, superiores: 3 ocorrência(s).
- Cobrir perfil CHEFE: 2 ocorrência(s).
- Revisar regra: lista, unidades: 2 ocorrência(s).
- Revisar regra: unidades, participantes: 2 ocorrência(s).
- Revisar regra: salvar, salva: 2 ocorrência(s).
- Revisar regra: cancelar, salva: 2 ocorrência(s).
- Revisar regra: escolha, cancelar: 2 ocorrência(s).
- Revisar regra: opcionalmente, informa: 2 ocorrência(s).

## Próximos passos sugeridos
1. Executar triagem rápida por `P0` com responsáveis de produto + QA + backend.
2. Abrir issues filhas por CDU com checklist: regra, massa, cenário e evidência esperada.
3. Implementar correções em lotes pequenos (4 a 6 CDUs) e reexecutar `npx playwright test` por arquivo impactado.
4. Atualizar este documento ao final de cada lote com status: `Aberta`, `Em andamento`, `Concluída`.
