# 📝 Resumo Executivo: Redução de Complexidade SGC

**Data:** 15 de Fevereiro de 2026  
**Para:** Liderança Técnica e Stakeholders  
**Assunto:** Proposta Consolidada de Simplificação

---

## 🎯 Resumo em 1 Minuto

Propomos **simplificar o SGC** através de 2 fases com risco controlado:

- **Fase 1** (7 dias, BAIXO risco): Consolidar services e stores → **-19 arquivos**
- **Fase 2** (12 dias, MÉDIO risco): Remover facades pass-through, introduzir @JsonView → **-23 arquivos**
- **Resultado:** **-65% de arquivos alterados** por mudança típica, sem perda funcional

**Fase 3** (simplificação de segurança) está **POSTERADA** devido ao alto risco.

---

## 📊 Situação Atual vs Proposta

| Métrica | Atual | Após Fases 1+2 | Melhoria |
|---------|-------|----------------|----------|
| Arquivos Java | 250 | ~210 | **-16%** |
| Arquivos TS/Vue | 180 | ~160 | **-11%** |
| Arquivos para adicionar 1 campo | 15-17 | 5-7 | **-65%** ⭐ |
| Tempo onboarding | 2-3 semanas | 1 semana | **-60%** ⭐ |
| Stack trace depth | 7 camadas | 4 camadas | **-43%** |
| Regras ArchUnit | 16 | 14 | -12.5% |
| Documentos MD | 128 | ~115 | -10% |

---

## 🔍 Principais Mudanças

### Backend

1. **Consolidar Services** (9 → 3 em Organização, 8 → 3 em Subprocesso)
   - Eliminar wrappers puros e services com < 3 métodos
   - **Impacto:** ~30 testes ajustados

2. **Remover Facades Pass-Through** (12 → 4)
   - Manter apenas facades complexos (Processo, Subprocesso, Mapa, Atividade)
   - Controllers chamam Services diretamente quando apropriado
   - **Impacto:** ~20 testes ajustados, 2 regras ArchUnit adaptadas

3. **Introduzir @JsonView** (78 DTOs → ~25)
   - Usar @JsonView do Jackson para controle de serialização
   - Manter DTOs apenas para transformações reais
   - **Impacto:** ~25 testes ajustados, 1 regra ArchUnit adaptada

### Frontend

1. **Consolidar Stores** (processos fragmentado → único)
   - Mesclar core + workflow + context
   - **Impacto:** ~8 testes ajustados

2. **Eliminar Composables View-Specific** (18 → 6)
   - Mover lógica view-specific para Views
   - Manter apenas composables genéricos
   - **Impacto:** ~10 testes ajustados

### Documentação

1. **Consolidar Análises de Complexidade** (8 docs → 1)
   - Arquivar versões v1
   - Único documento consolidado com decisões finais
   
2. **Atualizar ADRs** (4 ADRs afetados)
   - ADR-001 (Facades), ADR-004 (DTOs), ADR-006 (Aggregates)
   - Criar ADR-008 (Simplification Decisions)

### Testes

1. **Adaptar Testes de Arquitetura** (16 regras)
   - Generalizar 2 regras específicas
   - Adaptar 4 regras para permitir simplificação
   - Remover 2 regras obsoletas
   - **Total:** 100-125 testes ajustados (3-4% do total)

---

## ⚠️ Riscos e Mitigação

| Risco | Probabilidade | Impacto | Mitigação |
|-------|--------------|---------|-----------|
| Bugs funcionais | BAIXA | ALTO | Suite completa de testes + E2E |
| Degradação performance | BAIXA | MÉDIO | @JsonView tem overhead mínimo (<1ms) |
| Vazamento de dados (@JsonView) | MÉDIA | ALTO | Testes de serialização obrigatórios |
| Quebra de contratos API | BAIXA | ALTO | Testes de integração + versionamento |
| Resistência do time | MÉDIA | MÉDIO | Documentação + treinamento |

**Estratégia:** Deploy gradual (dev → staging → prod) com rollback preparado.

---

## 💰 Custo-Benefício

### Custo

- **Esforço:** 19 dias de desenvolvimento (Fases 1+2)
- **Testes afetados:** 100-125 (~3-4% do total)
- **Risco:** MÉDIO (gerenciável, reversível)

### Benefício

- **Velocidade:** +50% em mudanças típicas (65% menos arquivos)
- **Onboarding:** -60% de tempo (2-3 semanas → 1 semana)
- **Manutenção:** Código mais simples, menos camadas
- **Qualidade:** Menos indireção = menos bugs

**ROI:** Paga-se em **2-3 meses** de desenvolvimento normal.

---

## 📅 Cronograma Proposto

### Fase 1: Quick Wins (7 dias)
- **Semana 1:** Consolidar services + stores + documentação
- **Risco:** 🟢 BAIXO
- **Aprovação necessária:** Tech Lead

### Fase 2: Estrutural (12 dias)
- **Semanas 2-3:** Facades + @JsonView + ADRs
- **Risco:** 🟡 MÉDIO
- **Aprovação necessária:** Arquiteto + Tech Lead

### Fase 3: Segurança (POSTERGAR)
- **Status:** ⏸️ Aguardar evidência de necessidade
- **Risco:** 🔴 ALTO
- **Aprovação necessária:** CISO + CTO

**Total (Fases 1+2):** 3-4 semanas

---

## ✅ Critérios de Sucesso

### Obrigatórios (Gate)
- [ ] Todos os testes passam (backend + frontend + E2E)
- [ ] Todas as regras ArchUnit passam
- [ ] Cobertura mantém ≥70%
- [ ] Zero vulnerabilidades de segurança novas
- [ ] Performance não degrada (±5%)

### Desejados (KPIs)
- [ ] Velocidade de desenvolvimento +50%
- [ ] Onboarding -50%
- [ ] Feedback positivo do time

---

## 🚦 Decisão Requerida

**Solicitamos aprovação para:**

1. ✅ **Iniciar Fase 1** (7 dias, BAIXO risco)
   - Consolidar services e stores
   - Limpar documentação
   
2. ✅ **Planejar Fase 2** (12 dias, MÉDIO risco)
   - Remover facades pass-through
   - Introduzir @JsonView
   - Atualizar ADRs

3. ⏸️ **Postergar Fase 3** (alto risco)
   - Aguardar evidência de necessidade
   - Reavaliar em 6 meses

---

## 📚 Documentação Completa

Para detalhes técnicos completos, consulte:

- **[PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md](PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md)** - Análise completa (50+ páginas)
- **[backend/etc/docs/PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md](backend/etc/docs/PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md)** - Mudanças em regras ArchUnit

---

## 👥 Próximos Passos

1. **Revisão** (2 dias): Tech Lead + Arquiteto revisam este documento
2. **Aprovação** (1 dia): Decisão sobre Fases 1 e 2
3. **Kickoff** (1 dia): Brief para o time, criar branch
4. **Execução** (19 dias): Implementar Fases 1+2
5. **Review** (2 dias): Validação final e deploy

---

**Preparado por:** Agente de Consolidação de Complexidade  
**Data:** 15 de Fevereiro de 2026  
**Status:** 🟡 Aguardando Aprovação  
**Contato:** Abrir issue no GitHub para discussão
