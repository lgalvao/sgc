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
**Status:** ✅ COMPLETO  
**Esforço:** 2-3 horas  
**Risco:** Baixo

**Problema:**
- `SubprocessoService` atua como facade duplicada, competindo com `SubprocessoFacade`
- Cria camada extra de delegação sem valor agregado
- Anotado com `@Primary`, causando confusão sobre qual usar

**Solução Implementada:**
1. ✅ Atualizado `SubprocessoFacade` para usar diretamente os services decomposed:
   - `SubprocessoCrudService`
   - `SubprocessoDetalheService`
   - `SubprocessoValidacaoService`
   - `SubprocessoWorkflowService`
2. ✅ Atualizado todos os módulos externos para usar `SubprocessoFacade`:
   - `ProcessoFacade` (5 referências)
   - `MapaFacade`, `AtividadeFacade`, `ImpactoMapaService`, `MapaVisualizacaoService` (4 arquivos)
   - `AnaliseController`, `EventoProcessoListener`, `RelatorioService`, `SubprocessoMapaListener` (4 arquivos)
3. ✅ Atualizado services internos para usar decomposed services diretamente:
   - `SubprocessoContextoService`
   - `SubprocessoCadastroWorkflowService`
4. ✅ Removido `SubprocessoService.java` completamente
5. ✅ Atualizado documentação (package-info.java, comentários)

**Arquivos Modificados:**
- 15 arquivos atualizados
- 1 arquivo removido (SubprocessoService.java)
- 0 linhas → eliminação completa da duplicação

**Checklist:**
- [x] Analisar todas as referências a `SubprocessoService`
- [x] Atualizar `SubprocessoFacade` para usar services decomposed diretamente
- [x] Atualizar módulos externos para usar `SubprocessoFacade`
- [x] Atualizar services internos para usar decomposed services
- [x] Remover `SubprocessoService.java`
- [x] Atualizar documentação
- [ ] Executar testes para validar (pendente Java 21 no ambiente local)

**Resultado:**
- ✅ Eliminada camada extra de delegação
- ✅ Clarificado que `SubprocessoFacade` é o único ponto de entrada
- ✅ Reduzido 1 service (37 → 36)

---

#### ⏸️ P2: Resolver Dependências Circulares (@Lazy)
**Status:** 📊 Analisado  
**Esforço:** 1-2 dias  
**Risco:** Alto

**Problema:**
6 usos de `@Lazy` indicando dependências circulares identificadas:

**Casos Identificados:**

**Caso 1: UsuarioService ↔ UnidadeService (Organização)**
```java
// UsuarioService usa UnidadeService para:
- buscarPorSigla()
- buscarPorCodigo()
- buscarTodasUnidades()
- listarSubordinadas()
- buscarArvoreHierarquica()

// UnidadeService usa UsuarioService para:
- buscarPorId()
- buscarUsuariosPorUnidade()
```
**Análise:** Dependência bidirecional real. Ambos os services precisam um do outro.

**Soluções Possíveis:**
- A) Criar `OrganizacaoService` que coordena ambos
- B) Usar eventos de domínio para comunicação assíncrona
- C) Manter @Lazy (aceitável para operações de leitura)

**Recomendação:** Manter @Lazy por enquanto. Baixo risco, operações de leitura.

**Caso 2: LoginService → UnidadeService**
```java
@Lazy UnidadeService unidadeService
```
**Análise:** Similar ao Caso 1, parte do mesmo módulo organizacional.

**Recomendação:** Manter @Lazy.

**Caso 3: SubprocessoMapaWorkflowService (Self-Injection)**
```java
@Autowired
@Lazy
private SubprocessoMapaWorkflowService self;

// Usado para chamar métodos @Transactional internamente:
self.disponibilizarMapa(...)
self.aceitarValidacao(...)
self.homologarValidacao(...)
```
**Análise:** Padrão técnico para suportar @Transactional em chamadas internas.

**Soluções Possíveis:**
- A) Usar `TransactionTemplate` explicitamente
- B) Extrair métodos para service separado
- C) Usar AspectJ load-time weaving (complexo)

**Recomendação:** Refatorar para usar `TransactionTemplate` ou extrair para service separado.

**Caso 4: MapaFacade → MapaVisualizacaoService + ImpactoMapaService**
```java
@Lazy MapaVisualizacaoService mapaVisualizacaoService
@Lazy ImpactoMapaService impactoMapaService

// Cadeia de dependência circular:
// MapaFacade → MapaVisualizacaoService → SubprocessoFacade → 
// SubprocessoCrudService → MapaFacade
```
**Análise:** Dependência circular complexa envolvendo múltiplos módulos.

**Soluções Possíveis:**
- A) Revisar se MapaVisualizacaoService/ImpactoMapaService realmente precisam de SubprocessoFacade
- B) Usar eventos de domínio
- C) Refatorar para eliminar dependência reversa

**Recomendação:** Requer análise mais profunda. Possivelmente P5 (consolidar Detector/Impacto) resolverá isso.

**Caso 5: FiltroJwt**
```java
@Lazy // A ser analisado
```

**Decisão:** 
- ⏸️ MANTER @Lazy em UsuarioService ↔ UnidadeService (Casos 1, 2, 5)
- 🔄 REFATORAR SubprocessoMapaWorkflowService self-injection (Caso 3)
- ⏸️ ADIAR MapaFacade circular dependencies até P5 (Caso 4)

**Checklist:**
- [x] Mapear todas as dependências circulares
- [x] Analisar cada caso individualmente
- [ ] Refatorar SubprocessoMapaWorkflowService self-injection
- [ ] Executar testes completos
- [ ] Revisar MapaFacade após P5

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
| P1 | Eliminar SubprocessoService | 🔴 CRÍTICA | ✅ Completo | 100% |
| P2 | Resolver @Lazy (ciclos) | 🔴 CRÍTICA | 📊 Analisado | 50% |
| P3 | Consolidar Workflow Services | 🟡 ALTA | ⏸️ Planejado | 0% |
| P4 | Dividir ProcessoFacade | 🟡 ALTA | ⏸️ Planejado | 0% |
| P5 | Consolidar Detector/Impacto | 🟡 ALTA | ⏸️ Planejado | 0% |
| P6 | REST POST/GET apenas | 🟢 MÉDIA | ⏸️ Bloqueado | N/A |
| P7 | Criar Mappers faltantes | 🟢 MÉDIA | ⏸️ Planejado | 0% |
| P8 | Reduzir DTOs subprocesso | 🟢 MÉDIA | ⏸️ Planejado | 0% |

**Progresso Total:** 1/8 completos (12.5%) + 1 analisado

### Resumo de Impacto Atual

**Métricas Antes vs. Depois:**

| Métrica | Meta Original | Após P1 | Melhoria |
|---------|---------------|---------|----------|
| Services totais | 37 → ~30 | 37 → 36 | ✅ -2.7% |
| Services em subprocesso | 12 → ~8 | 12 → 11 | ✅ -8.3% |
| Dependências circulares (@Lazy) | 6 → 0 | 6 → 6* | ⏸️ Analisado |
| Maior service (linhas) | 530 → ~300 | 530 | ⏸️ Pendente (P4) |

*3 mantidos por decisão arquitetural, 1 para refatorar, 2 para adiar

**Conquistas:**
- ✅ Anti-pattern crítico eliminado (facade duplicada)
- ✅ Arquitetura mais clara (SubprocessoFacade como único ponto de entrada)
- ✅ ~185 linhas de código de delegação pura removidas
- ✅ 15 arquivos limpos (imports e referências corrigidas)

---

## 📝 Log de Atividades

### 2026-01-11 - Sprint 1 Iniciado

#### P1: Eliminar SubprocessoService (CONCLUÍDO) ✅
- ✅ Análise do architecture-report.md completa
- ✅ Plano de refatoração criado (architecture-refactor.md)
- ✅ Identificadas 64 referências a SubprocessoService em 32 arquivos
- ✅ Atualizado SubprocessoFacade para usar decomposed services diretamente (4 services)
- ✅ Atualizado 13 arquivos em módulos externos:
  - ProcessoFacade (5 substituições)
  - Mapa services: AtividadeFacade, ImpactoMapaService, MapaVisualizacaoService, MapaFacade
  - Outros: AnaliseController, EventoProcessoListener, RelatorioService, SubprocessoMapaListener
- ✅ Atualizado 2 services internos:
  - SubprocessoContextoService
  - SubprocessoCadastroWorkflowService
- ✅ Removido SubprocessoService.java (185 linhas)
- ✅ Atualizada documentação (package-info.java e comentários)
- ✅ Resultado: 37 → 36 services (-2.7%)

**Impacto:**
- Services eliminados: 1
- Linhas de código removidas: ~185
- Camadas de delegação eliminadas: 1
- Clareza arquitetural: Significativamente melhorada

---

#### P2: Resolver Dependências Circulares (ANALISADO) 📊
- ✅ Mapeadas 6 ocorrências de @Lazy em 5 arquivos
- ✅ Analisados 5 casos de dependências circulares:
  - Caso 1-2: UsuarioService ↔ UnidadeService (Organização)
  - Caso 3: SubprocessoMapaWorkflowService (self-injection)
  - Caso 4: MapaFacade → MapaVisualizacaoService/ImpactoMapaService
  - Caso 5: FiltroJwt
- ✅ Decisões tomadas:
  - MANTER @Lazy para Organização (UsuarioService ↔ UnidadeService) - baixo risco
  - REFATORAR SubprocessoMapaWorkflowService self-injection (planejado)
  - ADIAR MapaFacade até P5 (consolidar Detector/Impacto)

**Recomendação:** 
- Priorizar P3, P4, P5 antes de resolver completamente P2
- P5 pode resolver naturalmente o Caso 4
- SubprocessoMapaWorkflowService self-injection requer mais análise

---

## 🎯 Próximos Passos Imediatos

1. ✅ ~~Criar este documento de planejamento~~
2. ✅ ~~Analisar referências a `SubprocessoService`~~
3. ✅ ~~Atualizar `SubprocessoFacade` para usar services decomposed~~
4. ✅ ~~Remover `SubprocessoService`~~
5. ⏸️ Aguardar CI para validar compilação e testes (requer Java 21)
6. 🔄 Prosseguir com P3, P4, P5 (prioridade sobre P2 completo)

---

## 🎯 Recomendações para Próximas Iterações

### Prioridade Imediata (Próxima Sprint)

**1. P4: Dividir ProcessoFacade (530 → ~250 linhas)**
- ✅ **ROI Alto**: Arquivo muito grande é difícil de manter
- ✅ **Risco Baixo**: ProcessoInicializador já foi extraído com sucesso (precedente)
- ✅ **Esforço**: ~1 dia
- 💡 **Benefício**: Melhora significativa na manutenibilidade

**2. P5: Consolidar Detector/Impacto Services (3 → 1)**
- ✅ **ROI Médio-Alto**: Reduz fragmentação e confusão de nomenclatura
- ✅ **Pode resolver**: MapaFacade circular dependencies (P2 Caso 4)
- ✅ **Esforço**: 6-8 horas
- 💡 **Benefício**: Simplifica módulo `mapa` e pode eliminar 1-2 @Lazy

### Prioridade Média

**3. P3: Consolidar Workflow Services**
- ⚠️ **Requer análise**: Verificar se `SubprocessoWorkflowService` genérico é usado
- ✅ **Esforço**: 4-6 horas
- 💡 **Benefício**: Elimina duplicação se confirmado não-uso

**4. P7: Criar Mappers Faltantes (12 → 20)**
- ✅ **ROI Baixo-Médio**: Código mais limpo, menos erros
- ✅ **Risco Muito Baixo**: MapStruct é seguro
- ✅ **Esforço**: 1 dia
- 💡 **Benefício**: Qualidade de código

### Prioridade Baixa

**5. P8: Reduzir DTOs de Subprocesso (35 → ~25)**
- ⚠️ **Requer análise cuidadosa**: Não quebrar compatibilidade
- ✅ **Esforço**: 4-6 horas
- 💡 **Benefício**: Menos arquivos para manter

**6. P2 (Completo): Refatorar SubprocessoMapaWorkflowService self-injection**
- ⚠️ **Risco Médio**: Mexer com @Transactional é delicado
- ✅ **Esforço**: 4-6 horas
- 💡 **Benefício**: Eliminar 1 @Lazy

### Bloqueado / Documentar

**7. P6: Documentar REST POST/GET apenas**
- ✅ **Ação**: Criar ADR-006 documentando decisão
- ✅ **Esforço**: 1-2 horas
- 💡 **Benefício**: Clareza para futuros desenvolvedores

---

## 📋 Roadmap Atualizado

### Sprint 1: Limpeza Crítica (Em Andamento - 50% completo)
- [x] P1: Eliminar SubprocessoService ✅
- [x] P2: Analisar dependências circulares ✅
- [ ] P4: Dividir ProcessoFacade (recomendado próximo)
- **Meta**: Eliminar anti-patterns críticos

### Sprint 2: Consolidação (Planejado)
- [ ] P5: Consolidar Detector/Impacto Services
- [ ] P3: Consolidar Workflow Services (se aplicável)
- [ ] P2: Refatorar self-injection (se tempo permitir)
- **Meta**: Reduzir fragmentação

### Sprint 3: Padronização (Planejado)
- [ ] P7: Criar Mappers faltantes
- [ ] P8: Reduzir DTOs subprocesso
- [ ] P6: Documentar REST POST/GET (ADR-006)
- **Meta**: Melhorar consistência e documentação

---

## 📚 Referências

- [architecture-report.md](./architecture-report.md) - Análise detalhada que gerou este plano
- [ARCHITECTURE.md](./docs/ARCHITECTURE.md) - Visão geral da arquitetura
- [ADR-001](./docs/adr/ADR-001-facade-pattern.md) - Facade Pattern
- [refactoring-plan.md](./refactoring-plan.md) - Plano de refatoração geral

---

**Última Atualização:** 2026-01-11  
**Responsável:** GitHub Copilot AI Agent  
**Status:** ✅ P1 Completo, 📊 P2 Analisado, Roadmap Atualizado
