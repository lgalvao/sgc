# 📈 Rastreamento de Cobertura de Testes - SGC

**Última Atualização:** 2026-02-01  
**Responsável:** Equipe de Qualidade  
**Meta Global:** BRANCH ≥90%, LINE ≥99%, INSTRUCTION ≥99%

---

## 📊 Status Geral

### Cobertura Global

| Métrica       | Meta  | Baseline | Atual | Progresso | Status |
|---------------|-------|----------|-------|-----------|--------|
| **BRANCH**    | ≥90%  | 83.90%   | 83.90% | 🟡 93%   | 🟡 Atenção |
| **LINE**      | ≥99%  | 92.25%   | 92.25% | 🟡 93%   | 🟡 Atenção |
| **INSTRUCTION** | ≥99% | 91.56%  | 91.56% | 🟡 92%   | 🟡 Atenção |

**Legenda:**
- 🔴 TBD - A ser determinado
- 🔴 <50% - Crítico
- 🟡 50-89% - Atenção
- 🟢 90-98% - Bom
- ✅ ≥99% - Excelente

### Tempo de Execução

| Suite             | Meta    | Atual | Status |
|-------------------|---------|-------|--------|
| Testes Unitários  | <30s    | TBD   | 🔴 TBD |
| Testes Integração | <2min   | TBD   | 🔴 TBD |
| Todos os Testes   | <5min   | TBD   | 🔴 TBD |

---

## 🎯 Progresso por Fase

### Fase 0: Preparação

**Status:** ✅ Completa  
**Estimativa:** 1-2 dias  
**Início:** 2026-02-01  
**Conclusão:** 2026-02-01

#### Checklist

- [x] **0.1 Auditoria de Null Checks**
  - [x] Executar `auditar-verificacoes-null.js`
  - [x] Analisar relatórios (`null-checks-audit.txt`, `null-checks-analysis.md`)
  - [ ] Remover verificações redundantes (encontrados: 121 potencialmente redundantes)
  - [ ] Validar testes após remoções
  - [ ] Documentar decisões

- [x] **0.2 Atualizar Exclusões**
  - [x] Identificar entidades JPA sem lógica (encontrados: ~20)
  - [x] Identificar enums triviais (Status*, Tipo*)
  - [x] Atualizar `build.gradle.kts`
  - [x] Validar build

- [x] **0.3 Baseline de Cobertura**
  - [x] Executar `./gradlew :backend:jacocoTestReport`
  - [ ] Executar `super-cobertura.js --run` (script precisa ajuste ES modules)
  - [x] Documentar baseline neste arquivo
  - [x] Gerar análise de lacunas

#### Métricas Fase 0

| Item                         | Quantidade | Status |
|------------------------------|------------|--------|
| Null checks identificados    | 142        | ✅ Completo |
| Null checks removidos        | 0          | ⚠️ Adiado |
| Classes excluídas adicionais | ~25        | ✅ Completo |
| Cobertura baseline (BRANCH)  | 83.30%     | ✅ Atualizado |
| Cobertura baseline (LINE)    | 92.08%     | ✅ Atualizado |
| Cobertura baseline (INSTRUCTION) | 91.30% | ✅ Atualizado |

**Notas:**
- Baseline estabelecido em 2026-02-01
- 1158 testes existentes (1144 passando, 14 falhando)
- Gap para meta: BRANCH +6.7%, LINE +6.92%, INSTRUCTION +7.7%
- Arquivos críticos identificados: ProcessoFacade (7.1% branch), UnidadeFacade (20% branch)
- **Auditoria de Null Checks:** 142 verificações encontradas, 121 potencialmente redundantes (85.2%)
  - Análise manual necessária antes de remoção - muitas verificações podem ser legítimas
  - Recomendação: Adiar remoção de null checks para evitar quebrar testes
  - Foco deve ser em adicionar testes, não remover código defensivo
- **Exclusões Adicionadas:** ~25 classes
  - Entidades JPA simples (~20): Usuario, Unidade*, Processo, Mapa, Atividade, etc.
  - Enums triviais: Status*, Tipo*
  - Properties e configurações
  - **Impacto:** Foco agora está em código com lógica de negócio real

---

### Fase 1: Foundation - Unit Tests

**Status:** 🔴 Não Iniciada  
**Estimativa:** 3-5 dias  
**Início:** -  
**Conclusão:** -

#### Checklist por Módulo

##### 1.1 Módulo `processo` (CRÍTICO)

**Prioridade:** 🔴 CRÍTICA  
**Arquivos:** ~35 | **Testes Existentes:** ~25 | **Gap:** ALTO

- [ ] **Services**
  - [ ] `ProcessoService` - Criar/completar testes
  - [ ] `ProcessoValidadorService` - Criar/completar testes
  - [ ] `ProcessoNotificadorService` - Criar/completar testes
  - [ ] Outros services especializados

- [ ] **Facades**
  - [ ] `ProcessoFacade` - Criar/completar testes (orquestração)

- [ ] **Validators**
  - [ ] Validators customizados

**Cobertura Alvo:** LINE ≥99%, BRANCH ≥90%

| Métrica  | Antes | Depois | Delta |
|----------|-------|--------|-------|
| LINE     | TBD%  | TBD%   | TBD%  |
| BRANCH   | TBD%  | TBD%   | TBD%  |
| Testes + | TBD   | TBD    | TBD   |

**Notas:**
- _Adicionar descobertas e decisões_

---

##### 1.2 Módulo `subprocesso` (CRÍTICO)

**Prioridade:** 🔴 CRÍTICA  
**Arquivos:** ~40 | **Testes Existentes:** ~30 | **Gap:** ALTO

- [ ] **Services**
  - [ ] `SubprocessoService` - Criar/completar testes (máquina estados)
  - [ ] `SubprocessoValidadorService` - Criar/completar testes
  - [ ] Outros services especializados

- [ ] **Facades**
  - [ ] `SubprocessoFacade` - Criar/completar testes

- [ ] **State Machine**
  - [ ] Testes de transições de estado
  - [ ] Testes de validações de transição

**Cobertura Alvo:** LINE ≥99%, BRANCH ≥90%

| Métrica  | Antes | Depois | Delta |
|----------|-------|--------|-------|
| LINE     | TBD%  | TBD%   | TBD%  |
| BRANCH   | TBD%  | TBD%   | TBD%  |
| Testes + | TBD   | TBD    | TBD   |

**Notas:**
- _Adicionar descobertas e decisões_

---

##### 1.3 Módulo `seguranca.acesso` (CRÍTICO)

**Prioridade:** 🔴 CRÍTICA  
**Arquivos:** ~25 | **Testes Existentes:** ~15 | **Gap:** MÉDIO

- [ ] **Access Control**
  - [ ] `AccessControlService` - Testes completos
  - [ ] `AccessAuditService` - Testes de auditoria

- [ ] **Policies**
  - [ ] `ProcessoAccessPolicy` - Testes de permissões
  - [ ] `SubprocessoAccessPolicy` - Testes de permissões
  - [ ] `AtividadeAccessPolicy` - Testes de permissões
  - [ ] `MapaAccessPolicy` - Testes de permissões

- [ ] **Hierarchy**
  - [ ] `HierarchyService` - Testes de hierarquia

**Cobertura Alvo:** LINE ≥99%, BRANCH ≥95% (crítico)

| Métrica  | Antes | Depois | Delta |
|----------|-------|--------|-------|
| LINE     | TBD%  | TBD%   | TBD%  |
| BRANCH   | TBD%  | TBD%   | TBD%  |
| Testes + | TBD   | TBD    | TBD   |

**Notas:**
- _Segurança é crítica - aim for >95% branch coverage_

---

##### 1.4 Módulo `mapa` (CRÍTICO)

**Prioridade:** 🔴 CRÍTICA  
**Arquivos:** ~30 | **Testes Existentes:** ~20 | **Gap:** MÉDIO

- [ ] **Services**
  - [ ] `MapaService` - Criar/completar testes
  - [ ] Competência-related services
  - [ ] Outros services especializados

- [ ] **Facades**
  - [ ] `MapaFacade` - Criar/completar testes

**Cobertura Alvo:** LINE ≥99%, BRANCH ≥90%

| Métrica  | Antes | Depois | Delta |
|----------|-------|--------|-------|
| LINE     | TBD%  | TBD%   | TBD%  |
| BRANCH   | TBD%  | TBD%   | TBD%  |
| Testes + | TBD   | TBD    | TBD   |

**Notas:**
- _Adicionar descobertas e decisões_

---

##### 1.5 Módulo `organizacao` (ALTA)

**Prioridade:** 🟡 ALTA  
**Arquivos:** ~30 | **Testes Existentes:** ~20 | **Gap:** MÉDIO

- [ ] **Services**
  - [ ] `UnidadeService` - Criar/completar testes
  - [ ] `UsuarioService` - Criar/completar testes
  - [ ] `HierarquiaService` - Testes de hierarquia

- [ ] **Facades**
  - [ ] `UnidadeFacade` - Criar/completar testes

**Cobertura Alvo:** LINE ≥95%, BRANCH ≥85%

| Métrica  | Antes | Depois | Delta |
|----------|-------|--------|-------|
| LINE     | TBD%  | TBD%   | TBD%  |
| BRANCH   | TBD%  | TBD%   | TBD%  |
| Testes + | TBD   | TBD    | TBD   |

**Notas:**
- _Adicionar descobertas e decisões_

---

#### Resumo Fase 1

| Módulo      | LINE Meta | BRANCH Meta | Status | Conclusão |
|-------------|-----------|-------------|--------|-----------|
| processo    | ≥99%      | ≥90%        | 🔴 TBD | -         |
| subprocesso | ≥99%      | ≥90%        | 🔴 TBD | -         |
| seguranca   | ≥99%      | ≥95%        | 🔴 TBD | -         |
| mapa        | ≥99%      | ≥90%        | 🔴 TBD | -         |
| organizacao | ≥95%      | ≥85%        | 🔴 TBD | -         |

**Critério de Saída Fase 1:**
- [ ] Todos os módulos críticos atingiram metas
- [ ] Testes unitários executando em <30s
- [ ] Sem testes com anti-patterns críticos

---

### Fase 2: Integration Tests

**Status:** 🔴 Não Iniciada  
**Estimativa:** 2-3 dias  
**Início:** -  
**Conclusão:** -

#### Checklist

- [ ] **Fluxos de Processo**
  - [ ] Criar → Iniciar → Finalizar (end-to-end)
  - [ ] Validar persistência em cada etapa
  - [ ] Validar eventos publicados
  - [ ] Estimativa: 5-7 testes

- [ ] **Fluxos de Subprocesso**
  - [ ] Criar → Processar → Encerrar
  - [ ] Validar transições de estado
  - [ ] Validar relação com Processo pai
  - [ ] Estimativa: 5-7 testes

- [ ] **Hierarquia de Unidades**
  - [ ] Criar hierarquia completa
  - [ ] Testar queries hierárquicas
  - [ ] Validar herança de permissões
  - [ ] Estimativa: 3-5 testes

- [ ] **Controle de Acesso Integrado**
  - [ ] Testar políticas em contexto real
  - [ ] Validar auditoria de acesso
  - [ ] Testar hierarquia de unidades
  - [ ] Estimativa: 5-7 testes

#### Métricas Fase 2

| Métrica                    | Meta  | Atual | Status |
|----------------------------|-------|-------|--------|
| Testes de integração novos | 20-30 | TBD   | 🔴 TBD |
| Tempo execução integração  | <2min | TBD   | 🔴 TBD |

**Critério de Saída Fase 2:**
- [ ] Fluxos principais cobertos end-to-end
- [ ] 5-10 testes de integração por módulo core
- [ ] Testes executando em <2min

**Notas:**
- _Adicionar descobertas e decisões_

---

### Fase 3: Edge Cases & Error Handling

**Status:** 🔴 Não Iniciada  
**Estimativa:** 2-3 dias  
**Início:** -  
**Conclusão:** -

#### Checklist

- [ ] **Validações de Entrada**
  - [ ] Campos obrigatórios ausentes
  - [ ] Formatos inválidos
  - [ ] Valores fora de range
  - [ ] Strings muito longas/curtas
  - [ ] Estimativa: 15-20 testes

- [ ] **Estados Inválidos**
  - [ ] Transições ilegais (FINALIZADO → INICIADO, etc.)
  - [ ] Operações em estados incorretos
  - [ ] Validar mensagens de erro específicas
  - [ ] Estimativa: 10-15 testes

- [ ] **Recursos Não Encontrados**
  - [ ] Buscar por código inexistente
  - [ ] Validar `ErroNegocio` lançado
  - [ ] Validar mensagens de erro
  - [ ] Estimativa: 10-12 testes

- [ ] **Permissões Negadas**
  - [ ] Usuário sem permissão
  - [ ] Unidade fora da hierarquia
  - [ ] Validar auditoria de tentativas negadas
  - [ ] Estimativa: 8-10 testes

#### Análise de Exceções

| Tipo de Exceção  | Total no Código | Testados | Não Testados | % Cobertura |
|------------------|-----------------|----------|--------------|-------------|
| `ErroNegocio`    | TBD             | TBD      | TBD          | TBD%        |
| `ErroValidacao`  | TBD             | TBD      | TBD          | TBD%        |
| `ErroAcesso`     | TBD             | TBD      | TBD          | TBD%        |
| Outras           | TBD             | TBD      | TBD          | TBD%        |

**Critério de Saída Fase 3:**
- [ ] Todos os `throw new ErroNegocio()` cobertos
- [ ] Todos os `throw new ErroValidacao()` cobertos
- [ ] Cobertura de branches >85%

**Notas:**
- _Adicionar descobertas e decisões_

---

### Fase 4: Verificação & Polish

**Status:** 🔴 Não Iniciada  
**Estimativa:** 1-2 dias  
**Início:** -  
**Conclusão:** -

#### Checklist

- [ ] **Análise de Cobertura**
  - [ ] Executar `jacocoTestReport`
  - [ ] Executar `super-cobertura.js`
  - [ ] Identificar gaps remanescentes
  - [ ] Priorizar gaps críticos

- [ ] **Qualidade dos Testes**
  - [ ] Revisar testes com múltiplos asserts não relacionados
  - [ ] Revisar testes testando implementação
  - [ ] Garantir `@DisplayName` em português
  - [ ] Garantir `@Nested` para organização

- [ ] **Performance dos Testes**
  - [ ] Identificar testes >1s
  - [ ] Otimizar ou marcar como `@Tag("slow")`

- [ ] **Documentação**
  - [ ] Atualizar este arquivo com cobertura final
  - [ ] Documentar decisões de não testar
  - [ ] Atualizar `GUIA-MELHORIAS-TESTES.md`

- [ ] **Validação Final**
  - [ ] `./gradlew :backend:check` ✅
  - [ ] Cobertura BRANCH ≥90% ✅
  - [ ] Cobertura LINE ≥99% ✅
  - [ ] Cobertura INSTRUCTION ≥99% ✅

#### Métricas Finais

| Métrica                   | Meta  | Atual | Status |
|---------------------------|-------|-------|--------|
| BRANCH Coverage           | ≥90%  | TBD   | 🔴 TBD |
| LINE Coverage             | ≥99%  | TBD   | 🔴 TBD |
| INSTRUCTION Coverage      | ≥99%  | TBD   | 🔴 TBD |
| Testes com >5 asserts     | 0     | TBD   | 🔴 TBD |
| Testes sem `@DisplayName` | 0     | TBD   | 🔴 TBD |
| Tempo total execução      | <5min | TBD   | 🔴 TBD |

**Critério de Saída Fase 4:**
- [ ] Todas as métricas primárias atingidas
- [ ] Build de verificação bem-sucedido
- [ ] Documentação completa

**Notas:**
- _Adicionar descobertas e decisões finais_

---

## 📈 Histórico de Atualizações

### 2026-02-01: Criação do Documento

- ✅ Documento criado com estrutura inicial
- ✅ Fases definidas
- ✅ Baseline de cobertura estabelecido
- ✅ Top 30 arquivos com menor cobertura identificados
- 🟡 Início da execução da Fase 0 em andamento

**Baseline Estabelecido:**
- BRANCH: 83.90% (4715/5620) - Gap: +6.1% para meta
- LINE: 92.25% (21254/23040) - Gap: +6.75% para meta
- INSTRUCTION: 91.56% (94320/103015) - Gap: +7.44% para meta

**Top 5 Arquivos Críticos (Branch Coverage):**
1. ProcessoFacade.java: 7.1% branch (CRÍTICO)
2. UnidadeFacade.java: 20.0% branch (CRÍTICO)
3. AlertaController.java: 25.0% branch (CRÍTICO)
4. UsuarioFacade.java: 46.0% branch (ALTA)
5. UnidadeHierarquiaService.java: 50.0% branch (ALTA)

**Próximos Passos:**
1. ✅ Baseline estabelecido
2. 🟡 Executar Fase 0.1 (auditoria null checks)
3. 🔴 Executar Fase 0.2 (atualizar exclusões)
4. 🔴 Iniciar Fase 1 (Foundation tests)

---

## 🎯 Cobertura Detalhada por Módulo

### Módulo: `processo`

**Status:** 🔴 TBD  
**Última Atualização:** -

| Arquivo                         | LINE  | BRANCH | Testes | Status | Notas |
|---------------------------------|-------|--------|--------|--------|-------|
| ProcessoService.java            | TBD%  | TBD%   | TBD    | 🔴 TBD | -     |
| ProcessoFacade.java             | TBD%  | TBD%   | TBD    | 🔴 TBD | -     |
| ProcessoValidadorService.java   | TBD%  | TBD%   | TBD    | 🔴 TBD | -     |
| ... (adicionar conforme análise) | -    | -      | -      | -      | -     |

---

### Módulo: `subprocesso`

**Status:** 🔴 TBD  
**Última Atualização:** -

| Arquivo                         | LINE  | BRANCH | Testes | Status | Notas |
|---------------------------------|-------|--------|--------|--------|-------|
| SubprocessoService.java         | TBD%  | TBD%   | TBD    | 🔴 TBD | -     |
| SubprocessoFacade.java          | TBD%  | TBD%   | TBD    | 🔴 TBD | -     |
| ... (adicionar conforme análise) | -    | -      | -      | -      | -     |

---

### Módulo: `seguranca.acesso`

**Status:** 🔴 TBD  
**Última Atualização:** -

| Arquivo                         | LINE  | BRANCH | Testes | Status | Notas |
|---------------------------------|-------|--------|--------|--------|-------|
| AccessControlService.java       | TBD%  | TBD%   | TBD    | 🔴 TBD | -     |
| ProcessoAccessPolicy.java       | TBD%  | TBD%   | TBD    | 🔴 TBD | -     |
| HierarchyService.java           | TBD%  | TBD%   | TBD    | 🔴 TBD | -     |
| ... (adicionar conforme análise) | -    | -      | -      | -      | -     |

---

## 📝 Decisões e Observações

### Código Não Testado por Decisão

_Documentar aqui código que deliberadamente não será testado com justificativa._

| Arquivo/Classe             | Motivo                                      | Aprovado Por | Data       |
|----------------------------|---------------------------------------------|--------------|------------|
| - (exemplo)                | - (exemplo: código deprecated a ser removido) | -          | -          |

---

### Descobertas Importantes

_Documentar descobertas relevantes durante a execução._

#### Descoberta 1: [Título]
**Data:** -  
**Descrição:** -  
**Impacto:** -  
**Ação Tomada:** -

---

## 🚧 Bloqueios e Impedimentos

### Ativos

| ID  | Descrição                          | Impacto | Responsável | Data Abertura | Resolução |
|-----|------------------------------------|---------|-------------|---------------|-----------|
| - (exemplo) | - (exemplo: Java 21 não disponível) | ALTO  | -         | -             | -         |

### Resolvidos

| ID  | Descrição | Impacto | Resolução        | Data Resolução |
|-----|-----------|---------|------------------|----------------|
| -   | -         | -       | -                | -              |

---

## 📊 Dashboard Visual

```
Progresso Global: [░░░░░░░░░░] 0% (0/4 fases)

Fase 0: Preparação     [░░░░░░░░░░] 0%
Fase 1: Foundation     [░░░░░░░░░░] 0%
Fase 2: Integração     [░░░░░░░░░░] 0%
Fase 3: Edge Cases     [░░░░░░░░░░] 0%
Fase 4: Verificação    [░░░░░░░░░░] 0%
```

### Cobertura por Módulo

```
processo     [░░░░░░░░░░] TBD%
subprocesso  [░░░░░░░░░░] TBD%
seguranca    [░░░░░░░░░░] TBD%
mapa         [░░░░░░░░░░] TBD%
organizacao  [░░░░░░░░░░] TBD%
```

---

## 🎓 Lições Aprendidas

_Documentar aprendizados para melhorias futuras._

### Lição 1: [Título]
**Data:** -  
**Contexto:** -  
**Aprendizado:** -  
**Aplicação Futura:** -

---

**Última Atualização:** 2026-02-01  
**Próxima Revisão:** TBD  
**Responsável Atual:** -

---

## 📞 Referências Rápidas

- **Plano Detalhado:** [test-coverage-plan.md](test-coverage-plan.md)
- **Guia de Testes:** [backend/etc/docs/GUIA-MELHORIAS-TESTES.md](backend/etc/docs/GUIA-MELHORIAS-TESTES.md)
- **Scripts:** [backend/etc/scripts/](backend/etc/scripts/)

**Comandos Úteis:**
```bash
# Executar testes e gerar cobertura
./gradlew :backend:test :backend:jacocoTestReport

# Analisar cobertura
node backend/etc/scripts/super-cobertura.js --run

# Verificar metas de cobertura
./gradlew :backend:jacocoTestCoverageVerification
```
