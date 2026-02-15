# 📚 Índice Geral: Redução de Complexidade SGC

**Data:** 15 de Fevereiro de 2026  
**Propósito:** Guia de navegação para toda a documentação de redução de complexidade

---

## 🚀 Por Onde Começar?

### Para Stakeholders e Tomadores de Decisão

1. **[RESUMO-EXECUTIVO-REDUCAO-COMPLEXIDADE.md](../RESUMO-EXECUTIVO-REDUCAO-COMPLEXIDADE.md)** ⭐
   - Resumo em 1 minuto
   - Métricas antes vs depois
   - Custo-benefício
   - Decisões requeridas
   - **Tempo de leitura:** 5 minutos

### Para Tech Lead e Arquitetos

1. **[PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md](../PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md)** ⭐
   - Análise técnica completa
   - Impacto em testes, documentação e regras arquiteturais
   - Roadmap detalhado (3 fases)
   - Métricas de sucesso
   - **Tempo de leitura:** 30-40 minutos

2. **[PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md](PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md)**
   - Mudanças específicas em 16 regras ArchUnit
   - Código de exemplo para novas regras
   - Checklist de implementação
   - **Tempo de leitura:** 20 minutos

### Para Desenvolvedores (Implementação)

1. **[guia-implementacao-simplificacao-v2.md](../../guia-implementacao-simplificacao-v2.md)**
   - Passo a passo para Fase 1 e Fase 2
   - Exemplos de código completos
   - Scripts e comandos prontos
   - **Uso:** Consulta durante implementação

2. **[PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md](PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md)**
   - Como adaptar ArchConsistencyTest.java
   - Código das novas regras
   - **Uso:** Ao implementar Fase 2

---

## 📁 Estrutura de Documentos

### Documentos Ativos (Use Estes)

```
/
├── RESUMO-EXECUTIVO-REDUCAO-COMPLEXIDADE.md          # Para decisão rápida
├── PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md         # Análise completa
├── LEIA-ME-COMPLEXIDADE-V2.md                        # Base da análise (dados)
├── complexity-summary-v2.txt                          # Sumário técnico
├── guia-implementacao-simplificacao-v2.md             # Guia passo a passo
│
└── backend/etc/docs/
    └── PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md    # Mudanças em ArchUnit
```

### Documentos Arquivados (Apenas Referência Histórica)

```
backend/etc/docs/archive/complexity-v1/
├── README.md                           # Por que foram arquivados
├── LEIA-ME-COMPLEXIDADE.md            # Análise v1 original
├── complexity-report.md                # Relatório detalhado v1
└── complexity-v1-vs-v2-comparison.md  # Comparação v1 vs v2
```

**⚠️ NÃO USE** os documentos arquivados para implementação!

---

## 🗺️ Fluxo de Leitura Recomendado

### Cenário 1: "Preciso aprovar/rejeitar a proposta"

1. [RESUMO-EXECUTIVO-REDUCAO-COMPLEXIDADE.md](../RESUMO-EXECUTIVO-REDUCAO-COMPLEXIDADE.md) (5 min)
2. Se quiser mais detalhes → [PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md](../PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md), seção "Resumo Executivo" (10 min)
3. **Decisão:** Aprovar Fase 1+2, postergar Fase 3

### Cenário 2: "Preciso entender o problema técnico"

1. [LEIA-ME-COMPLEXIDADE-V2.md](../../LEIA-ME-COMPLEXIDADE-V2.md) - Base de dados (30 min)
2. [PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md](../PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md) - Análise completa (40 min)
3. [complexity-summary-v2.txt](../../complexity-summary-v2.txt) - Sumário técnico (10 min)

### Cenário 3: "Vou implementar a simplificação"

1. [PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md](../PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md), seção "Plano de Execução" (20 min)
2. [guia-implementacao-simplificacao-v2.md](../../guia-implementacao-simplificacao-v2.md) - Guia prático (consulta)
3. [PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md](PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md) - Para Fase 2 (20 min)

### Cenário 4: "Por que a v1 foi descartada?"

1. [archive/complexity-v1/README.md](archive/complexity-v1/README.md) (5 min)
2. Se realmente curioso → [archive/complexity-v1/complexity-v1-vs-v2-comparison.md](archive/complexity-v1/complexity-v1-vs-v2-comparison.md) (15 min)

---

## 📊 Documentos por Fase de Implementação

### Antes da Implementação (Planejamento)

| Documento | Propósito | Público |
|-----------|-----------|---------|
| RESUMO-EXECUTIVO | Decisão de aprovação | Stakeholders |
| PLANO-CONSOLIDADO | Entender escopo completo | Tech Lead, Arquitetos |
| LEIA-ME-V2 | Base de dados e análise | Desenvolvedores |

### Durante Fase 1 (Consolidação de Services/Stores)

| Documento | Propósito | Uso |
|-----------|-----------|-----|
| guia-implementacao-v2.md | Passo a passo | Consulta constante |
| PLANO-CONSOLIDADO, seção "Fase 1" | Checklist | Daily standup |

### Durante Fase 2 (Facades + @JsonView)

| Documento | Propósito | Uso |
|-----------|-----------|-----|
| guia-implementacao-v2.md | Passo a passo | Consulta constante |
| PROPOSTA-TESTES-ARQUITETURA | Adaptar ArchUnit | Ao modificar testes |
| PLANO-CONSOLIDADO, seção "Fase 2" | Checklist | Daily standup |

### Após Implementação (Documentação)

| Documento | Ação | Responsável |
|-----------|------|-------------|
| ADR-001 (Facade Pattern) | Atualizar | Arquiteto |
| ADR-004 (DTO Pattern) | Atualizar | Arquiteto |
| ADR-006 (Domain Aggregates) | Atualizar | Arquiteto |
| ADR-008 (Simplification) | Criar novo | Arquiteto |
| PLANO-CONSOLIDADO | Adicionar seção "Resultados" | Tech Lead |

---

## 🔗 Links para Documentação Relacionada

### Arquitetura e Padrões

- [backend/etc/docs/adr/](adr/) - Architectural Decision Records (7 ADRs)
- [backend/etc/docs/backend-padroes.md](backend-padroes.md) - Padrões de código backend
- [frontend/etc/docs/frontend-padroes.md](../../frontend/etc/docs/frontend-padroes.md) - Padrões de código frontend
- [AGENTS.md](../../AGENTS.md) - Convenções e regras fundamentais

### Testes

- [guia-testes-junit.md](guia-testes-junit.md) - Guia de testes JUnit
- [GUIA-MELHORIAS-TESTES.md](GUIA-MELHORIAS-TESTES.md) - Melhorias de qualidade de testes
- [ArchConsistencyTest.java](../../backend/src/test/java/sgc/arquitetura/ArchConsistencyTest.java) - Testes de arquitetura

### Requisitos

- [etc/reqs/](../../etc/reqs/) - 48 documentos de casos de uso (CDUs)
- [etc/regras-acesso.md](../../etc/regras-acesso.md) - Regras de controle de acesso

---

## 📌 Changelog de Documentação

### 15/02/2026 - Consolidação v3

**Adicionados:**
- ✅ PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md
- ✅ RESUMO-EXECUTIVO-REDUCAO-COMPLEXIDADE.md
- ✅ PROPOSTA-ATUALIZACAO-TESTES-ARQUITETURA.md
- ✅ Este índice (INDICE-REDUCAO-COMPLEXIDADE.md)

**Arquivados:**
- 📦 LEIA-ME-COMPLEXIDADE.md → archive/complexity-v1/
- 📦 complexity-report.md → archive/complexity-v1/
- 📦 complexity-v1-vs-v2-comparison.md → archive/complexity-v1/

**Removidos:**
- ❌ INDICE-DOCUMENTACAO-COMPLEXIDADE.md (substituído por este)
- ❌ complexity-summary.txt (obsoleto)

**Mantidos:**
- ✅ LEIA-ME-COMPLEXIDADE-V2.md (base de dados)
- ✅ complexity-summary-v2.txt (sumário técnico)
- ✅ guia-implementacao-simplificacao-v2.md (guia prático)

---

## ❓ FAQ

### Qual documento devo ler primeiro?

**Se você é:**
- **Stakeholder/PM:** RESUMO-EXECUTIVO (5 min)
- **Tech Lead/Arquiteto:** PLANO-CONSOLIDADO (40 min)
- **Desenvolvedor:** guia-implementacao-v2.md (consulta)

### Por que tantos documentos?

**Diferentes públicos, diferentes necessidades:**
- Stakeholders precisam de resumo executivo
- Arquitetos precisam de análise técnica completa
- Desenvolvedores precisam de guias práticos

### Posso ignorar os documentos arquivados?

**Sim!** Use apenas documentos ativos. Documentos arquivados são apenas para:
- Referência histórica
- Auditoria de decisões
- Entender evolução do pensamento

### Qual a diferença entre PLANO-CONSOLIDADO e LEIA-ME-V2?

- **LEIA-ME-V2:** Análise de complexidade (DADOS)
- **PLANO-CONSOLIDADO:** Análise + Impacto + Decisões (AÇÃO)

O PLANO-CONSOLIDADO **integra** LEIA-ME-V2 e adiciona análise de:
- Impacto em testes (100-125 testes afetados)
- Impacto em documentação (13 docs arquivados)
- Impacto em regras ArchUnit (4 regras adaptadas)
- Decisões finais sobre cada ponto

---

## 🎯 Mapa Mental

```
Redução de Complexidade SGC
│
├── 📊 Análise (Por quê?)
│   ├── LEIA-ME-COMPLEXIDADE-V2.md ← Dados e métricas
│   └── complexity-summary-v2.txt ← Sumário técnico
│
├── 📋 Planejamento (O quê?)
│   ├── PLANO-CONSOLIDADO.md ← Análise completa + Impacto
│   └── RESUMO-EXECUTIVO.md ← Para aprovação
│
├── 🔧 Implementação (Como?)
│   ├── guia-implementacao-v2.md ← Passo a passo
│   └── PROPOSTA-TESTES-ARQUITETURA.md ← Mudanças ArchUnit
│
└── 📦 Histórico (Referência)
    └── archive/complexity-v1/ ← Versões antigas
```

---

## 📞 Contato e Suporte

**Dúvidas sobre:**
- **Aprovação/Decisões:** Abrir issue com label `decision-required`
- **Implementação técnica:** Abrir issue com label `implementation`
- **Arquitetura:** Mencionar @arquiteto na issue
- **Documentação:** Abrir PR com correções

---

**Última atualização:** 15 de Fevereiro de 2026  
**Responsável:** Agente de Consolidação de Complexidade  
**Status:** ✅ Completo e Atualizado
