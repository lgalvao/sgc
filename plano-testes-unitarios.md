# Plano de Melhorias - Testes Unitários

## 📊 Cobertura Atual

- **Total**: 94.09% statements, 87.91% branches, 87.05% functions
- **322 testes passando** em 25 arquivos de teste
- **Melhoria recente**: +0.23% na cobertura total (utils: 90% → 100% functions)

---

## 🔍 Análise Detalhada por Área

### 1. Utils ✅ **100% Functions (Concluído)**

**Status**: ✅ **COMPLETO**
- **Statements**: 94.17% (91/103)
- **Functions**: 100% (10/10) ← **+10% melhoria implementada**
- **Branches**: 93.75% (40/43)

**Melhorias Implementadas**:
- ✅ Adicionados testes para `ensureValidDate`
- ✅ Cobertura completa de todas as 10 funções utilitárias
- ✅ 5 novos cenários de teste adicionados

---

### 2. Components 📉 **75% Functions (Prioridade ALTA)**

**Status**: 🔴 **CRÍTICO** - Maior impacto na cobertura
- **Statements**: 93.85% (1131/1205)
- **Functions**: 75% (33/44) ← **Área crítica**
- **Branches**: 86.66% (156/180)

#### Componentes com Baixa Cobertura:

| Componente | Functions | Status | Prioridade |
|------------|-----------|---------|------------|
| `Navbar.vue` | 75% (3/4) | 🔴 Crítico | **ALTA** |
| `NotificacaoContainer.vue` | 50% (3/6) | 🔴 Crítico | **ALTA** |
| `SubprocessoCards.vue` | 50% (2/4) | 🔴 Crítico | **ALTA** |
| `TabelaProcessos.vue` | 50% (3/6) | 🔴 Crítico | **ALTA** |

**Linhas não cobertas**:
- `Navbar.vue`: linhas 181-184
- `NotificacaoContainer.vue`: linhas 71-175, 178-180
- `SubprocessoCards.vue`: linha 46
- `TabelaProcessos.vue`: linhas 15, 23-48

---

### 3. Stores 📊 **91.76% Functions (Prioridade MÉDIA)**

**Status**: 🟡 **MODERADO**
- **Statements**: 93.94% (1071/1140)
- **Functions**: 91.76% (78/85)
- **Branches**: 87.53% (274/313)

#### Stores com Baixa Cobertura:

| Store | Statements | Functions | Status | Prioridade |
|-------|------------|-----------|---------|------------|
| `notificacoes.ts` | 79.36% (50/63) | 87.5% (7/8) | 🟡 Moderado | **MÉDIA** |
| `revisao.ts` | 70.45% (31/44) | 75% (3/4) | 🔴 Crítico | **MÉDIA** |
| `unidades.ts` | 76.92% (60/78) | 66.66% (4/6) | 🟡 Moderado | **MÉDIA** |
| `atribuicoes.ts` | 87.5% (21/24) | 66.66% (2/3) | 🟡 Moderado | **MÉDIA** |

**Linhas não cobertas**:
- `notificacoes.ts`: linhas 75-88
- `revisao.ts`: linhas 36-49
- `unidades.ts`: linhas 7-11, 15-27
- `atribuicoes.ts`: linhas 19-21

---

## 🎯 Plano de Melhorias Prioritárias

### Fase 1: Components (Impacto ALTO) 🚨

#### 1.1 Navbar.vue - 75% Functions
```typescript
// Função não testada: verificar qual método/função não está coberta
// Provável: métodos de navegação ou handlers de eventos
```

**Ações**:
- [ ] Identificar função não coberta (linha 181-184)
- [ ] Criar teste para cenário de navegação
- [ ] Adicionar teste de edge cases
- [ ] Meta: 100% functions coverage

#### 1.2 NotificacaoContainer.vue - 50% Functions
```typescript
// Funções não cobertas: 3/6 functions
// Provável: métodos de manipulação de notificações
```

**Ações**:
- [ ] Analisar código para identificar funções não testadas
- [ ] Criar testes para manipulação de notificações
- [ ] Testar cenários de erro (linhas 71-175, 178-180)
- [ ] Meta: 100% functions coverage

#### 1.3 SubprocessoCards.vue - 50% Functions
```typescript
// Função não coberta: linha 46
// Provável: método de manipulação de cards
```

**Ações**:
- [ ] Identificar função na linha 46
- [ ] Criar teste específico para essa funcionalidade
- [ ] Adicionar testes de edge cases
- [ ] Meta: 100% functions coverage

#### 1.4 TabelaProcessos.vue - 50% Functions
```typescript
// Funções não cobertas: linhas 15, 23-48
// Provável: métodos de manipulação de tabela
```

**Ações**:
- [ ] Identificar funções não cobertas
- [ ] Criar testes para funcionalidades de tabela
- [ ] Testar cenários de ordenação/filtragem
- [ ] Meta: 100% functions coverage

---

### Fase 2: Stores (Impacto MÉDIO) 📊

#### 2.1 notificacoes.ts - 79.36% Statements
```typescript
// Linhas não cobertas: 75-88
// Provável: métodos de manipulação de notificações
```

**Ações**:
- [ ] Identificar código não coberto (linhas 75-88)
- [ ] Criar testes para cenários de notificação
- [ ] Adicionar testes de error handling
- [ ] Meta: 90%+ statements coverage

#### 2.2 revisao.ts - 70.45% Statements
```typescript
// Linhas não cobertas: 36-49
// Provável: métodos de revisão/análise
```

**Ações**:
- [ ] Identificar funcionalidades não testadas
- [ ] Criar testes para lógica de revisão
- [ ] Adicionar testes de validação
- [ ] Meta: 85%+ statements coverage

#### 2.3 unidades.ts - 76.92% Statements
```typescript
// Linhas não cobertas: 7-11, 15-27
// Provável: métodos de manipulação de unidades
```

**Ações**:
- [ ] Identificar código não coberto
- [ ] Criar testes para funcionalidades de unidade
- [ ] Adicionar testes de validação
- [ ] Meta: 90%+ statements coverage

#### 2.4 atribuicoes.ts - 87.5% Statements
```typescript
// Linhas não cobertas: 19-21
// Funções: 66.66% (precisa melhorar)
```

**Ações**:
- [ ] Identificar função não coberta
- [ ] Criar teste específico
- [ ] Melhorar cobertura de functions para 100%
- [ ] Meta: 95%+ statements coverage

---

### Fase 3: Utils (Manutenção) ✅

**Status**: ✅ **CONCLUÍDO**
- **Statements**: 94.17% (91/103)
- **Functions**: 100% (10/10)
- **Branches**: 93.75% (40/43)

**Melhorias Implementadas**:
- ✅ `ensureValidDate`: 5 novos cenários de teste
- ✅ Cobertura completa de todas as funções
- ✅ Validação de edge cases

---

## 📈 Métricas de Sucesso

### Metas de Cobertura:
- **Meta Total**: 95%+ statements coverage
- **Meta Components**: 90%+ functions coverage
- **Meta Stores**: 90%+ statements coverage
- **Meta Utils**: Manter 95%+ coverage

### Critérios de Aceitação:
- [ ] Todos os testes passando (322+)
- [ ] Cobertura de functions > 90% em components
- [ ] Cobertura de statements > 90% em stores
- [ ] Cobertura de branches > 85% em todas as áreas

---

## 🚀 Estratégia de Implementação

### 1. Abordagem por Prioridade:
1. **Components** (maior impacto na cobertura)
2. **Stores críticos** (< 80% coverage)
3. **Stores moderados** (80-90% coverage)
4. **Manutenção** (utils já completo)

### 2. Técnica de Testes:
- **Unit Tests**: Funções isoladas
- **Edge Cases**: Cenários extremos
- **Error Handling**: Tratamento de erros
- **Integration**: Fluxos completos

### 3. Ferramentas:
- **Vitest**: Framework de testes
- **Coverage v8**: Provedor de cobertura
- **jsdom**: Ambiente de teste
- **Vue Test Utils**: Utilitários Vue

---

## 📋 Checklist de Execução

### Para cada melhoria:
- [ ] Identificar código não coberto
- [ ] Criar testes unitários
- [ ] Executar testes (deve passar)
- [ ] Verificar melhoria na cobertura
- [ ] Documentar mudanças
- [ ] Commit e push

### Validação Final:
- [ ] Executar `npm run coverage:unit`
- [ ] Verificar relatório HTML
- [ ] Confirmar melhoria nas métricas
- [ ] Validar que não quebrou testes existentes

---

## 📅 Cronograma Sugerido

### Semana 1: Components (Prioridade ALTA)
- [ ] Navbar.vue - 100% functions
- [ ] NotificacaoContainer.vue - 100% functions
- [ ] SubprocessoCards.vue - 100% functions
- [ ] TabelaProcessos.vue - 100% functions

### Semana 2: Stores Críticos (Prioridade MÉDIA)
- [ ] revisao.ts - 85%+ statements
- [ ] notificacoes.ts - 90%+ statements
- [ ] unidades.ts - 90%+ statements

### Semana 3: Stores Moderados (Prioridade BAIXA)
- [ ] atribuicoes.ts - 95%+ statements
- [ ] Refatoração e otimização

### Semana 4: Validação e Documentação
- [ ] Revisão completa da cobertura
- [ ] Documentação das melhorias
- [ ] Validação final dos testes

---

## 🎯 Resultado Esperado

Após implementação completa:
- **Cobertura Total**: 96%+ statements
- **Components**: 90%+ functions coverage
- **Stores**: 92%+ statements coverage
- **Utils**: 95%+ coverage (mantido)
- **Total de Testes**: 350+ testes passando

**Benefícios**:
- ✅ Confiabilidade do código aumentada
- ✅ Manutenção mais segura
- ✅ Refatoração com confiança
- ✅ Detecção precoce de bugs
- ✅ Documentação viva do código