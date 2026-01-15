# Tracking: Implementação da Proposta de Arquitetura

**Documento:** proposta-arquitetura.md  
**Início:** 2026-01-15  
**Status:** 🚧 Em Progresso

---

## 📊 Resumo Executivo

Implementação das Fases 1 e 2 da proposta de reorganização arquitetural do SGC, focando em melhorias incrementais sem reestruturação radical.

**Abordagem:** Manter arquitetura por agregados de domínio + melhorias de encapsulamento e documentação.

---

## ✅ Fase 1: Análise e Documentação

**Objetivo:** Documentar estado atual e criar ADRs

**Status:** ✅ Concluída em 2026-01-15

### Entregáveis

- ✅ **Proposta de Arquitetura** (`proposta-arquitetura.md`)
  - Análise completa de 76 arquivos do módulo subprocesso
  - Identificação de 10 services atuais
  - Mapeamento de dependências entre módulos
  - Recomendação: manter organização por domínio

- ✅ **ADR-006: Organização por Agregados de Domínio** (`docs/adr/ADR-006-domain-aggregates-organization.md`)
  - Já existe e documenta a decisão
  - Status: Aprovado

- ✅ **Tracking Document** (`tracking-arquitetura.md`)
  - Este documento
  - Acompanhamento conciso do progresso

- ⏳ **Diagrama de Dependências**
  - A criar: diagrama Mermaid mostrando dependências entre services

- ⏳ **Tabela de Consolidação**
  - A criar: mapeamento detalhado de services atuais → futuros

### Services Atuais Identificados

| # | Service | LoC (aprox) | Modificador | Responsabilidade |
|---|---------|-------------|-------------|------------------|
| 1 | `SubprocessoFacade` | ~360 | `public` | Orquestração geral ✅ |
| 2 | `SubprocessoMapaWorkflowService` | ~520 | `public` | Workflow de mapa |
| 3 | `SubprocessoCadastroWorkflowService` | ~350 | `public` | Workflow de cadastro |
| 4 | `SubprocessoTransicaoService` | ~165 | `public` | Transições de estado |
| 5 | `SubprocessoMapaService` | ~180 | `public` | Operações de mapa |
| 6 | `SubprocessoFactory` | ~160 | `public` | Criação de subprocessos |
| 7 | `SubprocessoEmailService` | ~158 | `public` | Envio de emails |
| 8 | `SubprocessoContextoService` | ~65 | `public` | Contexto de edição |
| 9 | `SubprocessoComunicacaoListener` | ~37 | package-private | Listener de eventos ✅ |

**Total:** 9 services principais + 1 listener (10 classes de serviço)

**Observações:**
- Apenas `SubprocessoFacade` deveria ser `public`
- `SubprocessoComunicacaoListener` já está package-private ✅
- 8 services especializados devem ser alterados para package-private

---

## 🔒 Fase 2: Package-Private Services

**Objetivo:** Encapsular services especializados, expondo apenas Facades

**Status:** ⏳ A Iniciar

### Plano de Execução

#### 2.1. Identificação ✅

Services a alterar para package-private:
1. ✅ `SubprocessoMapaWorkflowService`
2. ✅ `SubprocessoCadastroWorkflowService`
3. ✅ `SubprocessoTransicaoService`
4. ✅ `SubprocessoMapaService`
5. ✅ `SubprocessoFactory`
6. ✅ `SubprocessoEmailService`
7. ✅ `SubprocessoContextoService`

**Critério:** Todo `@Service` que não termina com `Facade`

#### 2.2. Alteração de Modificadores ⏳

**Padrão:**
```java
// ANTES
@Service
public class SubprocessoMapaWorkflowService { ... }

// DEPOIS
@Service
class SubprocessoMapaWorkflowService { ... }
```

**Arquivos a alterar:** 7 arquivos

#### 2.3. Regras ArchUnit ⏳

Criar regra para validar encapsulamento:
```java
@ArchTest
static final ArchRule specialized_services_should_be_package_private =
    classes()
        .that().resideInAPackage("..service..")
        .and().areAnnotatedWith(Service.class)
        .and().haveSimpleNameNotEndingWith("Facade")
        .should().bePackagePrivate();
```

**Local:** `backend/src/test/java/sgc/comum/test/ArchitectureTest.java`

#### 2.4. Testes ⏳

- [ ] Executar testes unitários dos services alterados
- [ ] Executar testes de integração
- [ ] Validar que não há compilação falhando
- [ ] Verificar cobertura de código mantida (≥95%)

### Métricas de Sucesso

| Métrica | Antes | Meta | Status |
|---------|-------|------|--------|
| Services públicos em subprocesso | 9 | 1 (Facade) | ⏳ 9 |
| Services package-private | 1 | 8 | ⏳ 1 |
| Regras ArchUnit | 0 | 1 | ⏳ 0 |
| Testes passando | ✅ | ✅ | ⏳ |

---

## 📈 Próximas Fases (Futuro)

### Fase 3: Implementar Eventos Prioritários
- EventoCadastroDisponibilizado
- EventoCadastroHomologado
- EventoMapaCriado
- EventoMapaDisponibilizado
- EventoMapaHomologado
- Listeners assíncronos

### Fase 4: Organização de Sub-pacotes
- Criar sub-pacotes em subprocesso/service/
- Mover services para sub-pacotes apropriados

### Fase 5: Consolidar Services (12 → 6-7)
- SubprocessoWorkflowService unificado
- Eliminar services redundantes

### Fase 6: Documentação Final
- package-info.java completos
- ARCHITECTURE.md atualizado

---

## 🎯 Status Geral

**Progresso Total:** 25% (Fase 1 completa, Fase 2 iniciada)

**Próximo Passo:** Implementar alterações de modificadores de acesso na Fase 2

**Bloqueios:** Nenhum

**Riscos:** Nenhum identificado

---

## 📝 Log de Mudanças

### 2026-01-15
- ✅ Criado tracking-arquitetura.md
- ✅ Fase 1 concluída: análise e documentação inicial
- ✅ Identificados 9 services + 1 listener
- ⏳ Iniciando Fase 2: package-private services
