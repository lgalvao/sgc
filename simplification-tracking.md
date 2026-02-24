# Simplificação do SGC — Tracking de Progresso

**Início:** 2026-02-24  
**Plano:** [simplification.md](file:///c:/sgc/simplification.md)

---

## ✅ Pré-requisito: Atualização de Documentação

- [x] ADR-001: Facade Pattern → status "Em Revisão"
- [x] ADR-003: Security Architecture → reescrito para `SgcPermissionEvaluator`
- [x] ADR-005: Controller Organization → status "Em Revisão"
- [x] ADR-008: Simplification Decisions → adicionada Fase 5
- [x] AGENTS.md → seções §2 e §5 atualizadas

## Fase 1 — Remover Código Morto (Pact) ✅ Concluída (2026-02-24)

- [x] Deletar `backend/src/test/java/sgc/pact/`
- [x] Deletar `frontend/src/services/__tests__/ProcessoService.pact.spec.ts`
- [x] Deletar `frontend/pact/`
- [x] Deletar `frontend/vitest.pact.config.ts`
- [x] Remover `@pact-foundation/pact` do `package.json`
- [x] Remover script `test:pact` do `package.json`
- [x] Remover exclusão `*.pact.spec.ts` do `vitest.config.ts`
- [ ] Verificação: testes backend + frontend (a cargo do usuário)

## Fase 2 — Consolidar Services do Subprocesso

- [ ] Criar `SubprocessoWorkflowService` (merge de Cadastro + Admin + Transição + Factory)
- [ ] Avaliar absorção de helpers (Contexto, Atividade, AjusteMapa)
- [ ] Atualizar `SubprocessoFacade` para novos services
- [ ] Atualizar testes unitários
- [ ] Limpar subdiretórios esvaziados
- [ ] Verificação: testes backend

## Fase 3 — Consolidar Módulos

- [ ] 3A: `alerta` absorve `notificacao`
- [ ] 3B: `processo` absorve `painel`
- [ ] 3C: `subprocesso` absorve `analise`
- [ ] Verificação: testes backend + frontend

## Fase 4 — Remover Mappers do Frontend

- [ ] Categorizar mappers (passthrough vs. transformação real)
- [ ] Deletar `frontend/src/mappers/`
- [ ] Atualizar imports nos services
- [ ] Verificação: typecheck + lint + testes frontend
