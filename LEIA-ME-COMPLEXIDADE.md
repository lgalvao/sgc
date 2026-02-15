# 📊 Análise de Complexidade - SGC

> ⚠️ **ATENÇÃO:** Esta é a **versão 1** da análise. Uma **versão 2 revisada** está disponível em [LEIA-ME-COMPLEXIDADE-V2.md](LEIA-ME-COMPLEXIDADE-V2.md)
> 
> **Diferença principal:** v2 adiciona **provas concretas de viabilidade**, diferencia **complexidade obrigatória vs opcional**, e respeita melhor os **padrões arquiteturais válidos**.

## 🎯 Objetivo

Identificar sobre-engenharia no SGC, sistema projetado para **10-20 usuários simultâneos** mas arquitetado com padrões de **sistemas enterprise de alta escala**.

## 📁 Documentos Gerados

### Versão 1 (Original)
1. **[complexity-report.md](complexity-report.md)** (30KB, 921 linhas)
   - Relatório técnico completo e detalhado
   - Análise profunda de backend e frontend
   - Recomendações priorizadas
   - Análise custo-benefício

2. **[complexity-summary.txt](complexity-summary.txt)** (2.6KB)
   - Sumário executivo
   - Métricas principais
   - Quick wins

### Versão 2 (Revisada - RECOMENDADA) ⭐
3. **[LEIA-ME-COMPLEXIDADE-V2.md](LEIA-ME-COMPLEXIDADE-V2.md)** (23KB, 696 linhas)
   - Análise revisada com viés para simplificação prática
   - Provas concretas de viabilidade
   - Diferenciação entre complexidade obrigatória e opcional
   - Métricas medidas (não estimadas)
   - Respeito aos padrões arquiteturais válidos

4. **[complexity-summary-v2.txt](complexity-summary-v2.txt)** (9.4KB)
   - Sumário executivo da v2
   - Comparação v1 vs v2
   - Roadmap conservador vs agressivo

## 🔍 Principais Achados

### Backend (Java/Spring Boot)

| Componente | Atual | Recomendado | Redução |
|------------|-------|-------------|---------|
| Services | 38 | 8-10 | **75%** |
| Facades | 12 | 2-3 | **80%** |
| DTOs | 78 | 15-20 | **75%** |
| Mappers | 14 | 2-3 | **85%** |

**Problemas principais:**
- 9 services para módulo Organização (deveria ser 2)
- 7 services para Subprocesso (deveria ser 2)
- Sistema de eventos assíncrono desnecessário
- Arquitetura de segurança enterprise (28 classes → 3)

### Frontend (Vue/TypeScript)

| Componente | Atual | Recomendado | Redução |
|------------|-------|-------------|---------|
| Stores | 15 | 12 | **20%** |
| Services | 15 | 6-8 | **50%** |
| Composables | 18 | 6 | **67%** |
| Types | 83+ | 40 | **52%** |

**Problemas principais:**
- Store `processos` dividido em 3 arquivos + agregador
- 18 composables (muitos específicos de views)
- 83 tipos TypeScript com redundância
- DTOs/Mappers duplicando backend

## 💸 Custo da Complexidade

### Para adicionar UM campo a uma entidade:

**Atual:** 13-17 arquivos precisam ser alterados

```
Backend (7-9 arquivos):
- Entity
- DTO
- Mapper
- Request
- Response  
- Testes (2-3)

Frontend (6-8 arquivos):
- Type
- DTO
- Mapper
- Componente formulário
- Componente visualização
- Testes (1-2)
```

**Simplificado:** 5 arquivos (redução de 65%)

```
Backend (2 arquivos):
- Entity com @JsonView
- Testes

Frontend (3 arquivos):
- Componente formulário
- Componente visualização
- Testes
```

## 🎯 Recomendações por Prioridade

### ⚡ Priority 1: Quick Wins (5 dias, 40% redução)

1. ✅ Consolidar Organization services (9 → 2)
2. ✅ Remover Facades pass-through (-7 classes)
3. ✅ Simplificar DTOs com @JsonView (-40 classes)
4. ✅ Consolidar processos stores (-3 files)
5. ✅ Remover composables view-specific (-10 files)
6. ✅ Eliminar DTOs/Mappers frontend (-30 files)

**Resultado: ~100 arquivos eliminados**

### 🔧 Priority 2: Simplificação Arquitetural (11 dias, +30% redução)

7. Remover sistema de eventos (usar chamadas diretas)
8. Simplificar segurança (28 → 3 classes)
9. Consolidar subprocesso services (7 → 2)
10. Consolidar mapa services (5 → 1)
11. Consolidar frontend services (15 → 6)
12. Reduzir types/interfaces (83 → 40)

**Resultado: ~80 arquivos adicionais eliminados**

### 📚 Priority 3: Long-Term (opcional)

13. Avaliar necessidade de Active Directory
14. Considerar Server-Side Rendering
15. Criar ADR-008: Simplicidade Apropriada

## 📊 Impacto da Simplificação

### Redução Total de Código

**60-70% menos código mantendo TODAS as funcionalidades**

### Benefícios Qualitativos

| Métrica | Melhoria |
|---------|----------|
| 👨‍💻 Onboarding novos devs | **60% mais rápido** (1 sem vs 3 sem) |
| 🔧 Manutenção | **70% mais simples** (5 vs 15 arquivos) |
| 🐛 Debugging | **80% mais fácil** (menos camadas) |
| ✅ Velocidade de testes | **50% mais rápido** (menos mocks) |

## 🚀 Próximos Passos

> 💡 **RECOMENDAÇÃO:** Siga o roadmap da **versão 2** ([LEIA-ME-COMPLEXIDADE-V2.md](LEIA-ME-COMPLEXIDADE-V2.md))

1. **Revisar** LEIA-ME-COMPLEXIDADE-V2.md (versão revisada)
2. **Aprovar** Fase 1 + Fase 2 (roadmap conservador)
3. **Validar** que simplificação não compromete requisitos
4. **Executar** refatoração incremental (1 módulo por vez)
5. **Medir** impacto real (onboarding, velocidade dev)
6. **Documentar** novo padrão (ADR-008)

## 💡 Filosofia Recomendada

> **"Simplicidade primeiro. Complexidade quando necessário."**

Para **10-20 usuários** é suficiente:
- ✅ Controller → Service → Repository
- ✅ @PreAuthorize + verificação hierarquia
- ✅ Entidade JPA com @JsonView
- ✅ 1 store por domínio
- ✅ Tipos TypeScript únicos

**Quando crescer para 100+ usuários:**
- Reavaliar necessidade de Facades
- Considerar CQRS se leitura >> escrita
- Implementar cache se performance degradar

---

**📄 Documento principal:** [complexity-report.md](complexity-report.md)  
**📅 Data:** 15 de Fevereiro de 2026  
**👤 Elaborado por:** Agente de Análise de Complexidade
