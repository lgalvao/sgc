# E2E Test Investigation - Learnings and Fixes

## Data da Investigação
2026-02-12

## Contexto
Testes E2E (cdu-xx) estavam falhando após grandes refatorações no projeto. Esta investigação foi conduzida para identificar e corrigir os problemas.

## Descobertas Principais

### 1. Inconsistência nas Fixtures Utilizadas

**Problema Identificado:**
Os testes CDU estão usando dois padrões diferentes de fixtures, causando inconsistência no reset do banco de dados:

#### Fixtures Disponíveis:
1. **`complete-fixtures.ts`** - Completa com auto-reset de DB
   - Herda de `processo-fixtures.js` (que herda de `auth-fixtures.js`)
   - Inclui reset automático de database via `beforeEach`
   - Inclui fixture `cleanupAutomatico` para limpar processos criados
   - **Padrão recomendado** para testes E2E

2. **`auth-fixtures.ts`** - Apenas autenticação
   - Fornece apenas fixtures de login (sem reset de DB)
   - Requer `resetDatabase()` manual em `beforeAll`/`beforeEach`
   - Menos conveniente, mas pode ser útil em casos específicos

3. **`database-fixtures.ts`** - Auth + auto-reset (sem cleanup)
   - Herda de `auth-fixtures.ts`
   - Inclui reset automático de database
   - Não inclui cleanup automático de processos

#### Distribuição Atual:

**Usando `complete-fixtures` (15 testes):** ✅
- CDU-02, 03, 04, 05, 06, 07, 08, 09, 10, 13, 14, 15, 17, 18, 19

**Usando `auth-fixtures` (21 testes):** ⚠️
- CDU-01, 11, 12, 16, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36

### 2. Análise de Reset Manual de DB

Dos testes que usam `auth-fixtures`, a maioria implementa reset manual:
- CDU-20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, etc. **têm** `resetDatabase()` em `beforeAll`
- CDU-01 **não tem** reset de DB (test simples de login, não precisa)

### 3. Ambiente de Execução

**Requisitos de Sistema:**
- ✅ Java 21 (OpenJDK 21.0.10)
- ✅ Node.js (para frontend e Playwright)
- ✅ Gradle 9.3.1 (auto-download)

**Portas Utilizadas:**
- Backend: `http://localhost:10000`
- Frontend: `http://localhost:5173`

**Lifecycle (`e2e/lifecycle.js`):**
- Gerencia startup automático de backend (Gradle bootRun) e frontend (npm run dev)
- Implementa health checks antes de iniciar testes
- Filtra logs desnecessários do Gradle e Vite

### 4. Estrutura dos Helpers

Os helpers estão bem organizados e encapsulam corretamente a complexidade:

| Helper | Responsabilidade |
|--------|-----------------|
| `helpers-auth.ts` | Login, logout, credenciais (USUARIOS) |
| `helpers-processos.ts` | Criar/verificar processos, extrair IDs |
| `helpers-atividades.ts` | Adicionar atividades e conhecimentos |
| `helpers-mapas.ts` | Criar competências, disponibilizar mapas |
| `helpers-navegacao.ts` | Navegação entre páginas |
| `helpers-analise.ts` | Fluxos de análise (aceite, devolução, homologação) |

### 5. Padrão de Casos de Uso Testados

**Tipos de Funcionalidade:**
- **Autenticação e UI**: CDU-01, CDU-02
- **Workflow de Processo**: CDU-03 a CDU-10 (criação, edição, transições)
- **Análise e Validação**: CDU-20, CDU-25 (aceite, homologação)
- **Administração**: CDU-30 (gestão de admins)

**Casos de Teste Críticos:**
- **CDU-10**: Disponibilizar revisão de atividades/conhecimentos (9 cenários, 483 linhas)
  - Última falha identificada em `.last-run.json`
  - Testa histórico de análise, confirmações, múltiplas revisões
  
- **CDU-20**: Análise de mapas de competência (4 cenários, 230 linhas)
  - Diferenciação GESTOR (aceite) vs ADMIN (homologação)
  - Testa devolução, aceite, notificações
  
- **CDU-25**: Aceite em bloco (2 cenários, 159 linhas)
  - Modal com checkboxes para múltiplas unidades
  
- **CDU-30**: Manter administradores (3 cenários, 90 linhas)
  - Lista, adiciona, remove admins
  - Validações (não pode remover a si mesmo, mínimo 1 admin)

## Causas Prováveis das Falhas

### 1. Poluição de Estado entre Testes
- Testes usando `auth-fixtures` sem reset adequado podem acumular dados
- Reset em `beforeAll` não é suficiente para testes `describe.serial()`
- Processos e dados criados em um teste podem afetar os seguintes

### 2. Seletores Quebrados após Refatoração
- Refatorações podem ter alterado `data-testid` attributes
- Mudanças em estrutura de componentes (modais, formulários)
- Alterações em rotas ou padrões de URL

### 3. Mudanças em Validações de Backend
- Regras de negócio podem ter sido alteradas
- Validações mais restritivas podem bloquear ações que antes funcionavam

### 4. Inconsistência de Fixtures
- Mistura de padrões dificulta manutenção
- Alguns testes podem não ter setup adequado

## Recomendações de Correção

### 1. Padronizar Fixtures (Prioridade ALTA)
**Ação:** Migrar todos os testes para `complete-fixtures.ts` como padrão

**Benefícios:**
- Reset automático de DB garante isolamento
- Cleanup automático evita poluição
- Reduz código boilerplate
- Alinha com boas práticas já estabelecidas em CDU-02 a CDU-19

**Exceções:**
- CDU-01 (teste de login puro) pode continuar com `auth-fixtures`
- Testes que explicitamente NÃO querem reset podem usar `auth-fixtures` + comentário justificando

### 2. Verificar Seletores após Refatoração
**Ação:** Executar testes e verificar logs de erro para identificar seletores quebrados

**Método:**
```bash
npx playwright test cdu-XX.spec.ts --reporter=list > resultado.txt 2>&1
grep -i "error\|failed\|timeout" resultado.txt
```

### 3. Revisar Error Context Files
**Ação:** Examinar arquivos `error-context.md` gerados em `test-results/`

**Último erro encontrado:**
- Arquivo: `test-results/cdu-10-CDU-10---Disponibil-e43ac-es-e-disponibiliza-cadastro-chromium/error-context.md`
- Mostra snapshot YAML da página no momento da falha
- Usuário estava logado como CHEFE_SECAO_221
- Painel vazio (sem processos)

### 4. Executar Testes Incrementalmente
**Nunca executar cenários isolados!** Muitos testes usam `test.describe.serial()`, onde cenários dependem de execução sequencial.

**Abordagem recomendada:**
1. Executar arquivo completo: `npx playwright test cdu-XX.spec.ts`
2. Capturar saída: `> resultado.txt 2>&1`
3. Analisar com grep: `grep -A 10 "FAILED" resultado.txt`

## Próximos Passos

### Fase 1: Padronização (IMEDIATO) ✅ CONCLUÍDA
- [x] Criar lista completa de testes a migrar
- [x] Migrar testes CDU-11, 12, 16, 20-36 para `complete-fixtures`
- [x] Remover imports e chamadas manuais de `resetDatabase()`
- [x] Adicionar `cleanupAutomatico` onde apropriado

**Resultado:** 19 arquivos migrados (CDU-11, 12, 16, 20-36) + 1 já migrado anteriormente (CDU-11)
**Status Final:** 35/36 testes CDU usando `complete-fixtures` (apenas CDU-01 mantém `auth-fixtures` por ser teste de login puro)

### Fase 2: Validação (CURTO PRAZO)
- [ ] Executar cada teste CDU individualmente
- [ ] Documentar falhas específicas em arquivo separado
- [ ] Identificar seletores quebrados
- [ ] Verificar mudanças em requisitos (comparar com /etc/reqs)

### Fase 3: Correção (MÉDIO PRAZO)
- [ ] Corrigir seletores quebrados
- [ ] Ajustar expectativas de testes se requisitos mudaram
- [ ] Adicionar testes para novos comportamentos
- [ ] Executar suite completa de E2E

### Fase 4: Documentação (FINAL)
- [ ] Atualizar guia-correcao-e2e.md se necessário
- [ ] Documentar padrões finais em e2e/README.md
- [ ] Criar exemplos de "antes/depois" para futuras migrações

## Comandos Úteis

### Executar Testes
```bash
# Teste único (arquivo completo)
npx playwright test e2e/cdu-XX.spec.ts --reporter=list

# Com saída capturada
npx playwright test e2e/cdu-XX.spec.ts --reporter=list > resultado.txt 2>&1

# UI Mode (interativo)
npx playwright test --ui
```

### Analisar Resultados
```bash
# Ver apenas falhas
grep -A 20 "FAILED" resultado.txt

# Ver logs do backend
grep "BACKEND" e2e/server.log

# Ver logs do frontend
grep "FRONTEND" e2e/server.log
```

### Ambiente
```bash
# Verificar Java
java -version  # deve ser 21.x

# Setar Java 21
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

## Observações Importantes

1. **Nunca aumentar timeouts**: Se elemento não aparece, é porque:
   - Dados não estão no banco
   - Validação backend falhou
   - Seletor está errado
   - Feature não implementada

2. **Sempre usar helpers centralizados**: Não criar funções locais nos testes

3. **Error context é seu amigo**: Sempre examinar `error-context.md` antes de debugar

4. **Testes são seriais em muitos casos**: Verificar `describe.serial()` antes de executar

## Conclusão

A principal causa das falhas é **inconsistência no uso de fixtures**, com 21 testes usando `auth-fixtures` quando deveriam usar `complete-fixtures`. A migração para `complete-fixtures` deve resolver a maioria dos problemas de poluição de estado.

Após a padronização, uma execução completa dos testes revelará falhas reais causadas pela refatoração (seletores, mudanças de comportamento), que poderão ser corrigidas uma a uma.

## Correções Implementadas

### Migração de Fixtures (2026-02-12)

**Problema:** 21 testes CDU usavam `auth-fixtures` sem reset automático de banco de dados, causando poluição de estado entre testes.

**Solução Implementada:**
1. Migrados 20 arquivos de `auth-fixtures` para `complete-fixtures`
2. Removidos blocos manuais de `beforeAll`/`afterAll` com `resetDatabase`
3. Substituído `cleanup.registrar()` por `cleanupAutomatico.registrar()`
4. Simplificados imports (apenas `type {useProcessoCleanup}`)

**Arquivos Migrados:**
- CDU-11, 12, 16, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36

**Estatísticas:**
- 20 arquivos modificados
- +66 linhas (imports e parâmetros)
- -236 linhas (código boilerplate removido)
- **Redução líquida: 170 linhas de código**

**Status Atual:**
- ✅ 35 testes CDU usam `complete-fixtures` (com auto-reset)
- ✅ 1 teste CDU usa `auth-fixtures` (CDU-01 - teste de login puro, correto)
- ✅ 100% dos testes que precisam de DB reset agora o têm automaticamente

---
**Responsável:** Jules (Agente Copilot)  
**Status:** Migração de Fixtures Concluída - Aguardando Validação  
**Última Atualização:** 2026-02-12 (15:33 UTC)
