# Plano de Refatoração Arquitetural - Backend SGC

**Data de Início:** 2026-01-11  
**Versão:** 1.0  
**Status:** Em Andamento  
**Baseado em:** architecture-report.md

---

## 📋 Objetivos da Refatoração

### Foco Principal
- ✅ **Redução de Fragmentação**: Consolidar services fragmentados (37 → ~30)
- ✅ **Redução de Redundância**: Eliminar duplicação e overlaps
- ⚠️ **Restrição**: Manter POST/GET apenas nos controllers (restrição do ambiente)

### Métricas Alvo

| Métrica | Antes | Meta | Melhoria |
|---------|-------|------|----------|
| Services totais | 37 | ~30 | -19% |
| Services em subprocesso | 12 | ~8 | -33% |
| Services em mapa | 11 | ~8 | -27% |
| Dependências circulares (@Lazy) | 6 | 0 | -100% |
| Maior service (linhas) | 530 | ~300 | -43% |

---

## 🎯 Plano de Execução

### Sprint 1: Limpeza Crítica (Prioridade CRÍTICA 🔴)

#### ✅ P1: Eliminar SubprocessoService (Anti-Pattern)
**Status:** 🔄 Em Progresso  
**Esforço:** 2-3 horas  
**Risco:** Baixo

**Problema:**
- `SubprocessoService` atua como facade duplicada, competindo com `SubprocessoFacade`
- Cria camada extra de delegação sem valor agregado
- Anotado com `@Primary`, causando confusão sobre qual usar

**Solução:**
1. Fazer `SubprocessoFacade` usar diretamente os services decomposed:
   - `SubprocessoCrudService`
   - `SubprocessoDetalheService`
   - `SubprocessoValidacaoService`
   - `SubprocessoWorkflowService`
2. Remover `SubprocessoService` completamente
3. Atualizar imports e referências

**Checklist:**
- [ ] Analisar todas as referências a `SubprocessoService`
- [ ] Atualizar `SubprocessoFacade` para usar services decomposed diretamente
- [ ] Remover `SubprocessoService.java`
- [ ] Executar testes para validar
- [ ] Verificar se nenhum outro código depende de `SubprocessoService`

---

#### ⏸️ P2: Resolver Dependências Circulares (@Lazy)
**Status:** ⏸️ Planejado  
**Esforço:** 1 dia  
**Risco:** Médio

**Problema:**
6 usos de `@Lazy` indicando dependências circulares:
1. `UsuarioService` ↔ `UnidadeService`
2. `SubprocessoMapaWorkflowService` → self (auto-injeção)
3. `MapaFacade` → `MapaVisualizacaoService` + `ImpactoMapaService`

**Soluções Planejadas:**

**Caso 1: UsuarioService ↔ UnidadeService**
- Extrair lógica compartilhada para `OrganizacaoService`
- OU usar eventos de domínio para comunicação assíncrona

**Caso 2: Self-injection em SubprocessoMapaWorkflowService**
- Mover lógica transacional para método separado
- OU usar `TransactionTemplate` explicitamente

**Caso 3: MapaFacade circulares**
- Revisar se services realmente precisam de Facade
- Refatorar para eliminar dependência reversa

**Checklist:**
- [ ] Mapear todas as dependências circulares
- [ ] Analisar cada caso individualmente
- [ ] Implementar solução apropriada para cada caso
- [ ] Remover todos os `@Lazy`
- [ ] Executar testes completos

---

### Sprint 2: Consolidação de Services (Prioridade ALTA 🟡)

#### ⏸️ P3: Consolidar Workflow Services
**Status:** ⏸️ Planejado  
**Esforço:** 4-6 horas  
**Risco:** Médio

**Problema:**
- `SubprocessoWorkflowService` genérico não é usado pelos específicos
- Duplicação de lógica entre services de workflow

**Análise Necessária:**
```bash
# Verificar uso do SubprocessoWorkflowService genérico
grep -r "SubprocessoWorkflowService" --include="*.java" | grep -v "class SubprocessoWorkflowService"
```

**Opções:**
- A: Eliminar o genérico se não usado
- B: Fazer específicos usarem o genérico (composição)

**Checklist:**
- [ ] Analisar uso de `SubprocessoWorkflowService`
- [ ] Decidir entre Opção A ou B
- [ ] Implementar solução escolhida
- [ ] Atualizar testes

---

#### ⏸️ P4: Dividir ProcessoFacade (530 → ~250 linhas)
**Status:** ⏸️ Planejado  
**Esforço:** 1 dia  
**Risco:** Baixo

**Problema:**
- `ProcessoFacade` muito grande (530 linhas)
- Múltiplas responsabilidades (CRUD, Consultas, Inicialização, Validações)

**Solução:**
1. Extrair validações → `ProcessoValidador`
2. Extrair consultas complexas → `ProcessoConsultaService`
3. Manter `ProcessoInicializador` (já existe)
4. `ProcessoFacade` fica apenas com orquestração (~200-250 linhas)

**Checklist:**
- [ ] Criar `ProcessoValidador`
- [ ] Criar `ProcessoConsultaService`
- [ ] Mover métodos apropriados
- [ ] Atualizar `ProcessoFacade`
- [ ] Executar testes

---

#### ⏸️ P5: Consolidar Detector/Impacto Services (3 → 1)
**Status:** ⏸️ Planejado  
**Esforço:** 6-8 horas  
**Risco:** Médio

**Problema:**
3 services com responsabilidades sobrepostas:
- `DetectorMudancasAtividadeService` (182 linhas)
- `DetectorImpactoCompetenciaService` (159 linhas)
- `ImpactoMapaService` (118 linhas)

**Solução:**
Consolidar em um único `MapaImpactoService` com seções claras:
1. Detecção de mudanças
2. Cálculo de impactos
3. Análise de mapa

**Checklist:**
- [ ] Criar novo `MapaImpactoService`
- [ ] Migrar lógica dos 3 services
- [ ] Atualizar referências
- [ ] Remover services antigos
- [ ] Executar testes

---

### Sprint 3: Melhorias Complementares (Prioridade MÉDIA 🟢)

#### ⏸️ P6: REST - Manter POST/GET apenas
**Status:** ⏸️ Bloqueado (Restrição do Ambiente)  
**Esforço:** N/A  
**Decisão:** MANTER estado atual

**Justificativa:**
- Problema statement indica: "Pode deixar os controles usando post e get apenas. É uma restrição do ambiente."
- Manter POST para: create, update, delete, actions
- Manter GET para: consultas
- Não migrar para PUT/DELETE/PATCH

**Ação:**
- [ ] Documentar decisão em ADR-006 (REST Non-Standard Approach)

---

#### ⏸️ P7: Criar Mappers Faltantes (12 → 20)
**Status:** ⏸️ Planejado  
**Esforço:** 1 dia  
**Risco:** Muito Baixo

**Mappers a Criar:**
- `UsuarioMapper`
- `UnidadeMapper`
- `AlertaMapper`
- `PainelMapper`
- Outros conforme necessário

**Checklist:**
- [ ] Identificar services com mapeamento manual
- [ ] Criar Mappers com MapStruct
- [ ] Substituir mapeamento manual
- [ ] Executar testes

---

#### ⏸️ P8: Reduzir DTOs de Subprocesso (35 → ~25)
**Status:** ⏸️ Planejado  
**Esforço:** 4-6 horas  
**Risco:** Baixo

**Análise Necessária:**
- Identificar DTOs com apenas 1-2 campos diferentes
- Verificar possibilidade de consolidação via herança
- Validar uso de cada DTO

**Checklist:**
- [ ] Analisar todos os 35 DTOs
- [ ] Identificar candidatos para consolidação
- [ ] Refatorar usando herança quando apropriado
- [ ] Atualizar código dependente
- [ ] Executar testes

---

## 📊 Progresso Geral

### Status dos Problemas

| ID | Problema | Prioridade | Status | Progresso |
|----|----------|------------|--------|-----------|
| P1 | Eliminar SubprocessoService | 🔴 CRÍTICA | 🔄 Em Progresso | 0% |
| P2 | Resolver @Lazy (ciclos) | 🔴 CRÍTICA | ⏸️ Planejado | 0% |
| P3 | Consolidar Workflow Services | 🟡 ALTA | ⏸️ Planejado | 0% |
| P4 | Dividir ProcessoFacade | 🟡 ALTA | ⏸️ Planejado | 0% |
| P5 | Consolidar Detector/Impacto | 🟡 ALTA | ⏸️ Planejado | 0% |
| P6 | REST POST/GET apenas | 🟢 MÉDIA | ⏸️ Bloqueado | N/A |
| P7 | Criar Mappers faltantes | 🟢 MÉDIA | ⏸️ Planejado | 0% |
| P8 | Reduzir DTOs subprocesso | 🟢 MÉDIA | ⏸️ Planejado | 0% |

**Progresso Total:** 0/8 completos (0%)

---

## 📝 Log de Atividades

### 2026-01-11
- ✅ Análise do architecture-report.md completa
- ✅ Plano de refatoração criado
- ✅ Documento architecture-refactor.md iniciado
- 🔄 Iniciando P1: Eliminação de SubprocessoService

---

## 🎯 Próximos Passos Imediatos

1. ✅ Criar este documento de planejamento
2. 🔄 Analisar referências a `SubprocessoService`
3. ⏸️ Atualizar `SubprocessoFacade` para usar services decomposed
4. ⏸️ Remover `SubprocessoService`
5. ⏸️ Executar testes

---

## 📚 Referências

- [architecture-report.md](./architecture-report.md) - Análise detalhada que gerou este plano
- [ARCHITECTURE.md](./docs/ARCHITECTURE.md) - Visão geral da arquitetura
- [ADR-001](./docs/adr/ADR-001-facade-pattern.md) - Facade Pattern
- [refactoring-plan.md](./refactoring-plan.md) - Plano de refatoração geral

---

**Última Atualização:** 2026-01-11  
**Responsável:** GitHub Copilot AI Agent
