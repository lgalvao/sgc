# Simplificação do SGC — Tracking de Progresso

**Plano:** [simplification.md](./simplification-plan.md)

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
- [ ] Verificação: testes backend + frontend

## Fase 2 — Consolidar Services do Subprocesso ✅ Concluída (2026-02-24)

- [x] Criar `SubprocessoWorkflowService` (merge de Cadastro + Admin + Factory)
- [x] Manter `SubprocessoTransicaoService` separado (compartilhado com MapaWorkflow)
- [x] Atualizar `SubprocessoFacade` (3 deps → 1 `workflowService`)
- [x] Atualizar `MapaManutencaoService` + 3 testes
- [x] Atualizar `SubprocessoFacadeTest` + 4 testes workflow
- [x] Mover `SubprocessoFactoryTest` → pacote `workflow`
- [x] Deletar services antigos + diretório `factory/`
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