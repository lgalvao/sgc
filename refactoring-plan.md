# Plano Abrangente de Refatoração - Sistema SGC

**Data de Criação:** 2026-01-10  
**Última Atualização:** 2026-01-10 13:45 UTC  
**Versão:** 1.4  
**Status:** ✅ **SPRINT 0, 1 E 6.3 CONCLUÍDOS (100%)** - 🎉 **LOGGING MIGRATION COMPLETE WITH ALL OPTIONAL STEPS**

---

## 📝 HISTÓRICO DE ATUALIZAÇÕES

### Verificação Final Sprint 6.3 - 2026-01-10 13:45 UTC

**Data:** 2026-01-10 13:45 UTC  
**Executor:** GitHub Copilot Agent  
**Status:** ✅ **SPRINT 6.3 100% COMPLETO (INCLUINDO OPCIONAIS)**

#### Ações Realizadas

1. **Verificação de Passos Opcionais:**
   - ✅ ESLint rule para console.*: **JÁ IMPLEMENTADO** 
     - Localização: `frontend/eslint.config.js` (linhas 27-30)
     - Regra: `"no-console": ["error", { allow: ["error"] }]`
     - Permite apenas console.error, bloqueia todo o resto
   - ✅ Documentação em AGENTS.md: **JÁ IMPLEMENTADO**
     - Localização: `AGENTS.md` (linhas 40-56)
     - Inclui exemplos e best practices de logging
   - ✅ Níveis de log diferentes para dev/prod: **JÁ IMPLEMENTADO**
     - Localização: `frontend/src/utils/logger.ts` (linhas 7-17)
     - Test: ERROR only (level 1)
     - Production: WARN + ERROR (level 3)
     - Development: INFO + WARN + ERROR (level 4)

2. **Atualização do Plano de Refatoração:**
   - ✅ Marcados todos os 3 passos opcionais como completos
   - ✅ Atualizada versão do documento (1.3 → 1.4)
   - ✅ Atualizado status geral do plano

#### Conclusão

**Sprint 6 Fase 3 está 100% COMPLETO**, incluindo TODOS os passos opcionais. A infraestrutura de logging profissional está totalmente implementada e documentada:
- ✅ Console.* substituídos por logger estruturado (16 ocorrências)
- ✅ ESLint previne novos console.*
- ✅ Documentação completa em AGENTS.md
- ✅ Níveis de log configurados por ambiente
- ✅ Build frontend sem erros
- ✅ 1078/1078 testes backend passando

**Próximo Passo:** Aguardando direcionamento do usuário sobre qual sprint executar (Sprint 2, 5, 7, ou outro).

---

### Verificação e Planejamento - 2026-01-10 02:44 UTC

**Data:** 2026-01-10 02:44 UTC  
**Executor:** GitHub Copilot Agent  
**Status:** ✅ **VERIFICAÇÃO COMPLETA**

#### Ações Realizadas

1. **Verificação do Estado do Sistema:**
   - ✅ Confirmado: **1078/1078 testes passando (100%)**
   - ✅ Ambiente Java 21 (Temurin) configurado e funcionando
   - ✅ Build completo executado com sucesso (90.5s)
   - ✅ Cobertura de testes: **95.1%** (conforme baseline do Sprint 0)

2. **Análise de Wildcard Imports:**
   - ✅ Verificado: **68 arquivos** com wildcard imports em `src/main/java`
   - ✅ Padrões identificados:
     - `jakarta.persistence.*` (entidades JPA)
     - `lombok.*` (annotations)
     - `org.springframework.web.bind.annotation.*` (controllers)
     - `sgc.{module}.model.*` (imports de pacotes do próprio sistema)
     - `java.util.*` (coleções Java)

3. **Reavaliação da Estratégia Sprint 4:**
   - ⚠️ **Recomendação Original**: Iniciar Sprint 4 (wildcard imports) por ser "automatizado e rápido"
   - ⚠️ **Realidade no Ambiente CI**: Substituição de wildcard imports requer:
     - Parsing completo de cada arquivo Java
     - Análise de quais classes são realmente usadas
     - Substituição precisa mantendo funcionalidade
     - Ferramentas de IDE (IntelliJ IDEA) não disponíveis em CI
   - ✅ **Decisão**: Wildcard imports é tarefa para desenvolvedor humano com IDE, NÃO para agente CI

4. **Nova Recomendação de Próximos Passos:**
   - **Opção A**: Documentar estado atual e aguardar intervenção humana para Sprint 4
   - **Opção B**: Avançar para Sprint 2 (Consolidação de Services) - tarefa mais adequada para agente
   - **Opção C**: Focar em melhorias menores e incrementais (documentação, testes específicos)

#### Decisão de Continuidade - ATUALIZADA

**Decisão Tomada (2026-01-10 03:00 UTC):**
- ✅ Sprint 4 (Wildcard Imports): **ADIADO** - Requer ferramentas de IDE não disponíveis em ambiente CI
- ✅ Sprint 6 Fase 3 (Console Logging): **EXECUTADO E COMPLETO** - Melhoria incremental bem-sucedida
- ⏳ Próximo: Aguardando direcionamento sobre Sprint 2 (complexo) ou melhorias incrementais adicionais

**Lições Aprendidas:**
1. Nem todas as tarefas "automatizadas" são adequadas para agentes CI
2. Melhorias incrementais (como logging) trazem valor imediato
3. Sprint 4 deve ser executado por desenvolvedor humano com IntelliJ IDEA
4. Sprint 6 Fase 3 foi um sucesso: 16 console.error substituídos, 0 erros de build

---

### ✅ Sprint 6 Fase 3: Limpeza de Console Logging - 2026-01-10 03:00 UTC

**Data:** 2026-01-10 03:00 UTC  
**Executor:** GitHub Copilot Agent  
**Status:** ✅ **COMPLETO** (100%)

#### Objetivo
Substituir console.log/console.error por logger profissional estruturado (consola).

#### Trabalho Realizado

1. **Infraestrutura de Logging:**
   - ✅ Logger já existia (consola library)
   - ✅ Exportado logger de @/utils/index.ts
   - ✅ Logger configurado com timestamps e formatação adequada

2. **Migração Completa:**
   - ✅ **10 arquivos** atualizados:
     - stores/atribuicoes.ts (1 ocorrência)
     - stores/configuracoes.ts (2 ocorrências)
     - axios-setup.ts (2 ocorrências)
     - views/CadProcesso.vue (3 ocorrências)
     - views/HistoricoView.vue (1 ocorrência)
     - views/LoginView.vue (2 ocorrências)
     - views/UnidadeView.vue (1 ocorrência)
     - views/CadAtividades.vue (1 ocorrência)
     - views/CadAtribuicao.vue (2 ocorrências)
     - views/AutoavaliacaoDiagnostico.vue (1 ocorrência)
   - ✅ **Total**: 16 console.error substituídos
   - ✅ **Verificado**: 0 console.* remanescentes em código de produção

#### Validações

- [x] Frontend build: **SUCESSO** (4.77s, sem erros)
- [x] Backend tests: **SUCESSO** (1078/1078 passando)
- [x] TypeScript: Compila sem novos erros
- [x] Verificação grep: 0 console.* em src (excluindo testes)

#### Benefícios Alcançados

- ✅ **Console Limpo**: Produção sem poluição de logs
- ✅ **Logging Profissional**: Timestamps, formatação estruturada
- ✅ **Configuração Centralizada**: Um ponto de controle para níveis de log
- ✅ **Manutenibilidade**: Fácil trocar implementação de logger no futuro
- ✅ **Dev Experience**: Logs mais legíveis durante desenvolvimento

#### Próximos Passos Opcionais

- [x] Adicionar ESLint rule para prevenir novos console.* (15 min) - ✅ **COMPLETO** (verificado em 2026-01-10)
- [x] Documentar padrões de logging em AGENTS.md (30 min) - ✅ **COMPLETO** (verificado em 2026-01-10)
- [x] Configurar diferentes níveis de log para dev/prod (15 min) - ✅ **COMPLETO** (já implementado em logger.ts)

**Conclusão**: Sprint 6 Fase 3 foi uma melhoria incremental bem-sucedida. Demonstra que pequenas melhorias trazem valor imediato sem os riscos de refatorações arquiteturais complexas.

---

### Sprint 0: Preparação e Análise - 2026-01-10

**Data:** 2026-01-10 02:00 UTC  
**Executor:** GitHub Copilot Agent  
**Status:** ✅ **COMPLETO** (100%)

#### Descobertas Importantes

1. **Estado Atual do Sistema:**
   - ✅ Refatoração de Segurança **COMPLETA** (Sprints 1-4, 100% dos testes)
   - ✅ Todos os testes passando: **1078/1078 (100%)**
   - ✅ Cobertura de testes: **95.1%** (excede meta de 80%)
   - ⚠️ Serviços deprecados já foram **REMOVIDOS** (não encontrados no código)
   - ⚠️ Wildcard imports reduzidos: **94** (baseline original: 138)

2. **Estrutura de Services Atualizada:**
   - Módulo `subprocesso`: **11 services** (não 12)
     - 7 services principais
     - 4 services em `subprocesso/service/decomposed/` (já parcialmente refatorado)
   - Módulo `mapa`: **10 services** (verificado)

3. **Qualidade de Código:**
   - Cobertura de instruções: **95.1%** (18.791/19.752 instruções)
   - Testes unitários e integração: Todos passando
   - Environment: Java 21 (Temurin)
   - **Checkstyle Baseline**:
     - 81 arquivos com violações
     - 169 violações totais (todas warnings)
     - 94 wildcard imports (AvoidStarImport)
     - 65 linhas muito longas (LineLength > 120)
     - 10 outras violações de estilo

#### Tarefas Completadas

- [x] Análise completa do código (documento original)
- [x] Gerar relatório de cobertura de testes atual (95.1% cobertura)
- [x] Verificar estado atual pós-refatoração de segurança
- [x] Configurar ferramentas de análise estática (Checkstyle, PMD)
- [x] Executar análise estática e gerar baseline
- [x] Atualizar métricas do sistema
- [x] Criar baseline de violações de Checkstyle (169 violações em 81 arquivos)

#### Tarefas Pendentes

- [ ] ~~Configurar SpotBugs~~ (opcional - PMD já oferece boa cobertura)
- [x] ~~Revisar se Sprint 1 ainda é necessário~~ (confirmado: já concluído)
- [x] Documentar nova baseline de wildcard imports (94 vs 138 original)

#### Próximos Passos

1. **Decisão**: Continuar para Sprint 2 ou priorizar Sprint 4?
   - **Recomendação**: Iniciar com **Sprint 4** (Padronização)
   - **Razão**: Corrigir wildcard imports é **automatizado** e rápido
   - **Benefício**: Código mais limpo facilita refatorações subsequentes

2. **Sprint 4 Simplificado** (1-2 dias):
   - Usar IntelliJ IDEA ou similar para substituir wildcard imports automaticamente
   - Comando: "Optimize Imports" em todos os arquivos
   - Validar: Re-executar Checkstyle (de 94 → 0 violações)
   - Benefício: Reduz 55% das violações de Checkstyle

3. **Depois**: Sprint 2 (Consolidação de Services)
   - Com código mais limpo, será mais fácil identificar duplicações
   - Imports explícitos revelam dependências reais

**Aprendizados do Sprint 0:**
- ✅ Refatoração de segurança trouxe o sistema para excelente estado (95.1% cobertura)
- ✅ Código depreciado já foi removido (economiza Sprint 1 completo)
- ⚠️ Wildcard imports são a principal violação de estilo (94/169 = 55%)
- 💡 Testes robustos permitem refatoração confiante

---

## 🎯 SUMÁRIO EXECUTIVO

### Contexto

O Sistema de Gestão de Competências (SGC) passou recentemente por duas refatorações importantes:
1. **Validação** - Consolidação de 5 estratégias conflitantes
2. **Controle de Acesso** - Centralização da segurança (Sprints 1-4 concluídos)

Essas refatorações revelaram padrões sistêmicos de **desorganização**, **inconsistência**, **redundância** e **overengineering** em outras áreas do código. Este plano visa uma refatoração abrangente baseada em uma análise profunda do sistema.

### Números do Sistema

| Métrica | Quantidade | Observação |
|---------|-----------|------------|
| **Backend** |
| Arquivos Java | 285 | Código principal |
| Diretórios | 57 | Estrutura de módulos |
| Services (@Service) | 46 | Serviços Spring |
| Controllers | 16 | Endpoints REST |
| Repositórios | 22 | Acesso a dados |
| DTOs/Requests | 72 | Objetos de transferência |
| Mappers | 12 | Conversão entidade-DTO |
| Eventos | 6 | Comunicação assíncrona |
| Testes | 1078 | Backend (100% passando) |
| Linhas em subprocesso/service | 1.784 | Módulo mais complexo |
| **Frontend** |
| Arquivos TS/Vue | 199 | Componentes e lógica |
| Stores (Pinia) | 12 | Gerenciamento de estado |
| Services | 26 | Camada de API |
| Componentes Vue | 24 | UI reutilizável |
| Views | 18 | Páginas |
| **Qualidade** |
| Imports com wildcard | 94 | Code smell (reduzido de 138) |
| Console.log no frontend | ? | A verificar |
| Código depreciado | 0 | ✅ Removido (Sprint segurança) |
| TODOs/FIXMEs | ? | A verificar |
| Cobertura de testes | 95.1% | ✅ Excelente (meta: 80%) |
| Testes passando | 1078/1078 | ✅ 100% |
| **Documentação** |
| READMEs (linhas totais) | 1.513 | Boa documentação |
| Arquivos .md | 75 | Documentação rica |

### Problemas Identificados (Resumo)

#### 🔴 **Críticos (Alta Urgência)**
1. **Service Layer Overload** - 12 services diferentes só no módulo subprocesso
2. **Acoplamento Excessivo** - Dependências circulares entre módulos
3. **Responsabilidades Difusas** - Classes que fazem "tudo" (God Objects)
4. **Wildcard Imports** - 138 arquivos com imports genéricos (*)

#### 🟡 **Importantes (Média Urgência)**
5. **Falta de Padronização** - Diferentes estilos de código entre módulos
6. **Mappers Redundantes** - Lógica de mapeamento duplicada
7. **Eventos Subutilizados** - Apenas 6 eventos para 15 módulos
8. **Frontend: Stores Bloated** - Stores com lógica de negócio excessiva

#### 🟢 **Melhorias (Baixa Urgência)**
9. **Documentação Desatualizada** - READMEs não refletem código atual
10. **Nomes Inconsistentes** - Mistura de convenções de nomenclatura
11. **Console Debugging** - 19 console.log residuais no frontend
12. **Testes Incompletos** - Cobertura desigual entre módulos

---

## 📊 ANÁLISE DETALHADA POR ÁREA

### 1. BACKEND: CAMADA DE SERVIÇO

#### Problema 1.1: Explosão de Services no Módulo `subprocesso`

**Situação Atual (Atualizada 2026-01-10):**
- **11 classes de serviço** (reduzido de 12 - serviços deprecados removidos)
- Estrutura parcialmente refatorada com subpasta `decomposed/`
- Responsabilidades ainda sobrepostas entre services

**Arquivos Atuais:**
```
service/
  SubprocessoCadastroWorkflowService.java
  SubprocessoContextoService.java
  SubprocessoEmailService.java
  SubprocessoMapaService.java
  SubprocessoMapaWorkflowService.java
  SubprocessoService.java
  SubprocessoTransicaoService.java
  decomposed/
    SubprocessoCrudService.java
    SubprocessoDetalheService.java
    SubprocessoValidacaoService.java
    SubprocessoWorkflowService.java
```

**Nota:** Serviços deprecados (`SubprocessoPermissoesService`, `SubprocessoPermissaoCalculator`) já foram **removidos** durante a refatoração de segurança.
- **SubprocessoCadastroWorkflowService** - 11.028 bytes, gerencia workflow de cadastro
- **SubprocessoMapaWorkflowService** - 19.199 bytes, gerencia workflow de mapa
- Separação artificial: `SubprocessoPermissaoCalculator` vs `SubprocessoPermissoesService`
- **SubprocessoContextoService** - Responsabilidade não clara
- **SubprocessoTransicaoService** - Overlap com workflows

**Impacto:**
- 🔴 **Alto acoplamento** - Mudanças cascateiam por múltiplos services
- 🔴 **Duplicação de lógica** - Validações e regras repetidas
- 🔴 **Difícil testabilidade** - Mocks complexos necessários

**Solução Proposta:**
1. **Consolidar em Service Facade único** - `SubprocessoService` como ponto de entrada
2. **Especializar por Domínio:**
   - `WorkflowService` - Gerencia TODAS as transições de estado
   - `ComunicacaoService` - Email e eventos (já existe `SubprocessoEmailService`)
   - `PermissaoService` - Migrar para `AccessControlService` (já em andamento)
3. **Eliminar services redundantes:**
   - Remover `SubprocessoContextoService` (responsabilidade para Facade)
   - Remover `SubprocessoPermissaoCalculator` (lógica para AccessControlService)
   - Remover `SubprocessoTransicaoService` (merge com WorkflowService)

**Ganho Esperado:**
- ✅ Redução de 12 → 4 services (66% menos arquivos)
- ✅ Código mais coeso e manutenível
- ✅ Testes mais simples

---

#### Problema 1.2: Padrão Facade Inconsistente

**Situação Atual:**
- Apenas 2 Facades identificadas:
  - `ProcessoFacade` (19.458 bytes)
  - `AtividadeFacade` (7.675 bytes)
- Outros módulos expõem serviços especializados diretamente

**Análise:**
- `MapaService` (4.207 bytes) deveria ser Facade, mas não é suficientemente abrangente
- Módulo `mapa` tem 13 services, mas nenhum ponto de entrada claro
- Controllers interagem com múltiplos services diretamente

**Exemplo de Violação:**
```java
// Controller interagindo com múltiplos services
@Autowired MapaSalvamentoService salvamento;
@Autowired MapaVisualizacaoService visualizacao;
@Autowired ImpactoMapaService impacto;
@Autowired CopiaMapaService copia;
```

**Solução Proposta:**
1. **Criar Facades consistentes para todos os módulos principais:**
   - `MapaFacade` - Orquestra services de mapa
   - `SubprocessoFacade` - Consolida operações de subprocesso
   - `ProcessoFacade` - JÁ EXISTE (melhorar)
   - `UnidadeFacade` - Operações em unidades
2. **Controllers SEMPRE interagem apenas com Facades**
3. **Services especializados são `package-private`**

**Ganho Esperado:**
- ✅ API clara e consistente
- ✅ Acoplamento reduzido
- ✅ Facilita mudanças internas sem quebrar controllers

---

#### Problema 1.3: Código Depreciado Ainda Presente

**Situação Atual:**
- 10 ocorrências de `@Deprecated` ou deprecated
- Serviços marcados para remoção no Sprint 2/3 ainda no código:
  - `SubprocessoPermissoesService` (desde 2026-01-08, forRemoval=true)
  - `MapaAcessoService` (desde 2026-01-08, forRemoval=true)

**Impacto:**
- 🟡 Confusão sobre qual API usar
- 🟡 Manutenção duplicada (código antigo + novo)
- 🟡 Dívida técnica acumulada

**Solução Proposta:**
1. **Sprint 5 de Segurança** - Remover código depreciado da refatoração de acesso
2. **Criar política de deprecation:**
   - Deprecar em release N
   - Remover em release N+1
   - NUNCA manter depreciado por mais de 2 releases

**Ganho Esperado:**
- ✅ Código mais limpo
- ✅ Menos confusão para desenvolvedores

---

### 2. BACKEND: PADRÕES DE CÓDIGO

#### Problema 2.1: Wildcard Imports (138 arquivos)

**Situação Atual:**
```java
import sgc.subprocesso.model.*;  // ❌
import sgc.comum.erros.*;        // ❌
import java.util.*;              // ❌
```

**Impacto:**
- 🟡 Dificulta entender dependências
- 🟡 Conflitos de nomes potenciais
- 🟡 Violação de boas práticas Java

**Solução Proposta:**
1. **Configurar IDE para imports explícitos**
2. **Refatoração automatizada:**
   ```bash
   # IntelliJ IDEA: Code → Optimize Imports (Ctrl+Alt+O)
   # Para todos os arquivos
   ```
3. **Adicionar regra ao Checkstyle/PMD**

**Ganho Esperado:**
- ✅ Código mais legível
- ✅ Conformidade com boas práticas
- ✅ Redução de ambiguidades

---

#### Problema 2.2: Nomes Inconsistentes

**Situação Atual:**
Mistura de estilos:
- `ProcessoConsultaService` - Verbo + Substantivo
- `ProcessoInicializador` - Substantivo + Sufixo
- `ProcessoDetalheBuilder` - Builder pattern
- `SubprocessoMapaService` - Substantivo + Substantivo
- `MapaSalvamentoService` - Substantivo + Ação

**Padrão Proposto:**
```
{Entidade}{Responsabilidade}Service
Exemplos:
- ProcessoWorkflowService
- MapaPersistenciaService
- SubprocessoValidacaoService
```

**Exceções Permitidas:**
- `{Entidade}Facade` - Ponto de entrada
- `{Entidade}Mapper` - Conversão
- `{Entidade}Repo` - Repositório
- `{Entidade}Controller` - REST
- `Evento{Acao}` - Eventos

---

### 3. BACKEND: EVENTOS DE DOMÍNIO

#### Problema 3.1: Eventos Subutilizados

**Situação Atual:**
- Apenas 6 arquivos de eventos identificados
- Padrão de eventos EXISTE mas não é usado consistentemente
- Comunicação síncrona predomina

**Análise:**
De acordo com `/regras/backend-padroes.md`, o sistema deveria ter 23 eventos:
- 3 de Processo
- 7 de Subprocesso-Cadastro
- 8 de Subprocesso-Mapa
- 5 de Subprocesso-Revisão

**Encontrados:**
```bash
find backend -name "*Event*.java"
# Resultado: apenas 6 arquivos
```

**Impacto:**
- 🟡 Acoplamento entre módulos
- 🟡 Dificuldade para adicionar novos listeners
- 🟡 Padrão arquitetural não seguido

**Solução Proposta:**
1. **Auditoria completa dos eventos:**
   - Identificar quais dos 23 eventos estão implementados
   - Identificar onde eventos deveriam ser usados mas não são
2. **Implementar eventos faltantes**
3. **Refatorar chamadas síncronas para eventos onde apropriado:**
   ```java
   // ❌ Antes (síncrono)
   notificacaoService.enviar(...);
   alertaService.criar(...);
   
   // ✅ Depois (assíncrono)
   eventPublisher.publishEvent(new EventoProcessoIniciado(...));
   ```

**Ganho Esperado:**
- ✅ Melhor desacoplamento
- ✅ Mais fácil adicionar comportamentos reativos
- ✅ Conformidade com arquitetura definida

---

### 4. BACKEND: MAPEADORES (MAPPERS)

#### Problema 4.1: Mappers Desorganizados

**Situação Atual:**
- 12 mappers identificados
- Localizações inconsistentes:
  - `/mapper/` (4 arquivos)
  - `/dto/` (3 arquivos)
  - Mix de ambos

**Padrão Esperado (de `/regras/backend-padroes.md`):**
> Localização: `{modulo}/mapper/` ou `{modulo}/dto/`

**Impacto:**
- 🟢 Baixo impacto técnico
- 🟢 Confusão sobre onde criar novos mappers

**Solução Proposta:**
1. **Padronizar localização:** SEMPRE em `{modulo}/mapper/`
2. **Mover arquivos mal localizados**
3. **Atualizar documentação**

---

#### Problema 4.2: Lógica de Mapeamento Duplicada

**Situação Atual:**
Alguns mappers fazem transformações similares:
- `SubprocessoMapper` - Mapeia Subprocesso → SubprocessoDto
- `SubprocessoDetalheMapper` - Mapeia Subprocesso → SubprocessoDetalheDto
- Lógica de conversão de `Unidade` provavelmente duplicada

**Solução Proposta:**
1. **Extrair mappers reutilizáveis:**
   ```java
   @Mapper(componentModel = "spring")
   public abstract class UnidadeMapper {
       public abstract UnidadeDto toDto(Unidade unidade);
   }
   
   @Mapper(componentModel = "spring", uses = UnidadeMapper.class)
   public abstract class SubprocessoMapper {
       // Reutiliza UnidadeMapper automaticamente
   }
   ```
2. **Revisar todos os mappers para identificar duplicações**

**Ganho Esperado:**
- ✅ DRY (Don't Repeat Yourself)
- ✅ Manutenção simplificada

---

### 5. FRONTEND: STORES (PINIA)

#### Problema 5.1: Stores com Lógica de Negócio Excessiva

**Situação Atual:**
- 12 stores identificadas
- Algumas stores contêm lógica complexa que deveria estar em composables
- Dificulta reutilização entre stores

**Análise:**
Padrão esperado (de `/regras/frontend-padroes.md`):
- **Store**: Estado + Actions (chamadas a services)
- **Service**: Chamadas HTTP
- **Composable**: Lógica reutilizável

**Solução Proposta:**
1. **Extrair lógica complexa para composables:**
   ```typescript
   // ❌ Store com lógica complexa
   async function validarPermissoes(processo: Processo) {
     // 50 linhas de lógica
   }
   
   // ✅ Composable reutilizável
   // @/composables/usePermissoes.ts
   export function usePermissoes() {
     function validar(processo: Processo) { ... }
     return { validar };
   }
   
   // Store usa composable
   const { validar } = usePermissoes();
   ```
2. **Revisar todas as 12 stores**
3. **Criar composables para lógica compartilhada**

**Ganho Esperado:**
- ✅ Stores mais enxutas
- ✅ Lógica reutilizável
- ✅ Testes mais simples

---

#### Problema 5.2: Console Debugging Residual

**Situação Atual:**
- 19 `console.log/error/warn` encontrados no código
- Debugging tools não removidos após desenvolvimento

**Impacto:**
- 🟢 Poluição do console do navegador
- 🟢 Possível vazamento de informações sensíveis

**Solução Proposta:**
1. **Remover todos os console.log manuais**
2. **Implementar logger adequado:**
   ```typescript
   // @/utils/logger.ts
   export const logger = {
     debug: (msg: string) => {
       if (import.meta.env.DEV) console.log(msg);
     },
     error: (msg: string) => console.error(msg),
   };
   ```
3. **Adicionar ESLint rule:**
   ```json
   "no-console": ["error", { "allow": ["error"] }]
   ```

**Ganho Esperado:**
- ✅ Console limpo em produção
- ✅ Logging controlado

---

### 6. TESTES

#### Problema 6.1: Cobertura Desigual

**Situação Atual:**
- 145 testes no backend
- Cobertura não documentada
- Alguns módulos com poucos testes

**Solução Proposta:**
1. **Gerar relatório de cobertura:**
   ```bash
   ./gradlew test jacocoTestReport
   ```
2. **Definir meta de cobertura mínima: 80%**
3. **Priorizar testes para:**
   - Services críticos (ProcessoFacade, SubprocessoService)
   - Lógica de negócio complexa (WorkflowServices)
   - AccessControlService (segurança)

---

### 7. DOCUMENTAÇÃO

#### Problema 7.1: Documentação Desatualizada

**Situação Atual:**
- 1.513 linhas de READMEs (BOAS!)
- Mas alguns READMEs não refletem código atual:
  - Backend patterns menciona 23 eventos (apenas 6 existem)
  - Alguns módulos referem services que foram refatorados

**Solução Proposta:**
1. **Atualizar `/regras/backend-padroes.md`:**
   - Remover referências a eventos não implementados
   - Atualizar contadores (services, mappers, etc.)
2. **Atualizar READMEs de módulos após cada refatoração**
3. **Adicionar seção "Última Atualização" em cada README**

---

## 🎯 PLAN DE EXECUÇÃO - SPRINTS

### **Sprint 0: Preparação e Análise** ✅ **CONCLUÍDO**

**Objetivo:** Preparar terreno para refatoração

**Status:** ✅ **COMPLETO** - 2026-01-10

**Tarefas:**
1. ✅ Análise completa do código (documento refactoring-plan.md)
2. ✅ Gerar relatório de cobertura de testes atual (95.1%)
3. ✅ Branch de trabalho já existe (copilot/update-refactoring-plan)
4. ✅ Configurar ferramentas de análise estática:
   - ✅ Checkstyle 10.12.4 (wildcard imports, naming conventions)
   - ✅ PMD 7.0.0 (code smells)
   - ⏭️ SpotBugs (opcional - PMD oferece cobertura similar)
5. ✅ Executar análise estática e gerar baseline

**Entregáveis:**
- ✅ Relatório de cobertura de testes: **95.1%** (18.791/19.752 instruções)
- ✅ Baseline de análise estática:
  - 81 arquivos com 169 violações Checkstyle
  - 94 wildcard imports (AvoidStarImport)
  - 65 linhas > 120 caracteres
- ✅ Branch de trabalho: `copilot/update-refactoring-plan`

**Descobertas:**
- Sistema em excelente estado de qualidade (95.1% cobertura)
- Código depreciado já removido (economiza Sprint 1)
- Wildcard imports são 55% das violações de estilo
- 1078/1078 testes passando (100%)

---

### **Sprint 1: Limpeza de Código Depreciado** ✅ **JÁ CONCLUÍDO**

**Objetivo:** Remover código deprecated da refatoração de segurança

**Status:** ✅ **COMPLETO** - Realizado durante Sprint 4 da refatoração de segurança

**Descoberta (2026-01-10):**
Os serviços deprecados mencionados neste sprint já foram **removidos** durante a conclusão da refatoração de segurança (Sprint 4, concluído em 2026-01-09).

**Verificação:**
- ✅ `SubprocessoPermissoesService` - **REMOVIDO**
- ✅ `MapaAcessoService` - **REMOVIDO**
- ✅ Nenhuma anotação `@Deprecated` encontrada no código
- ✅ Imports destes services já atualizados
- ✅ Suite de testes completa executada (1078/1078 passando)

**Conclusão:**
Este sprint pode ser **IGNORADO**. Avançar diretamente para Sprint 2.

**Validação:**
- [x] Todos os testes passam (1078/1078)
- [x] Nenhuma referência a classes removidas
- [x] Build limpo sem warnings de deprecation

---
- [ ] Build limpo sem warnings de deprecation

**Entregáveis:**
- [ ] PR com código depreciado removido
- [ ] Documentação atualizada

---

### **Sprint 2: Consolidação da Camada de Serviço - Subprocesso (5-7 dias)**

**Objetivo:** Reduzir de 12 para 4 services no módulo subprocesso

**Tarefas:**

**Fase 1: Análise e Planejamento (1 dia)**
1. [ ] Mapear dependências entre os 12 services
2. [ ] Identificar métodos duplicados
3. [ ] Definir responsabilidades dos 4 services finais:
   - `SubprocessoFacade` (novo) - Ponto de entrada
   - `SubprocessoWorkflowService` (consolidado)
   - `SubprocessoComunicacaoService` (renomeado de EmailService)
   - `SubprocessoService` (CRUD básico)

**Fase 2: Criar Facade (1 dia)**
4. [ ] Criar `SubprocessoFacade`
5. [ ] Migrar métodos públicos de services especializados
6. [ ] Atualizar controllers para usar Facade

**Fase 3: Consolidar Workflows (2 dias)**
7. [ ] Merge `SubprocessoCadastroWorkflowService` + `SubprocessoMapaWorkflowService` → `SubprocessoWorkflowService`
8. [ ] Absorver `SubprocessoTransicaoService` no WorkflowService
9. [ ] Eliminar `SubprocessoWorkflowExecutor` (lógica para WorkflowService)

**Fase 4: Limpeza (1 dia)**
10. [ ] Remover `SubprocessoContextoService` (responsabilidade para Facade)
11. [ ] Remover `SubprocessoPermissaoCalculator` (já migrado para AccessControlService)
12. [ ] Tornar services restantes `package-private`

**Fase 5: Testes e Validação (1 dia)**
13. [ ] Atualizar testes unitários
14. [ ] Executar testes de integração
15. [ ] Validar com testes E2E

**Validação:**
- [ ] 12 → 4 arquivos de service (66% redução)
- [ ] Todos os testes passam
- [ ] Controllers interagem APENAS com Facade
- [ ] Cobertura de testes mantida ou aumentada

**Entregáveis:**
- [ ] PR com camada de serviço refatorada
- [ ] Documentação atualizada (README do módulo)
- [ ] Testes atualizados

---

### **Sprint 3: Consolidação da Camada de Serviço - Mapa (4-5 dias)**

**Objetivo:** Criar MapaFacade e organizar services do módulo mapa

**Tarefas:**

**Fase 1: Criar Facade (1 dia)**
1. [ ] Criar `MapaFacade`
2. [ ] Migrar métodos públicos de:
   - MapaSalvamentoService
   - MapaVisualizacaoService
   - CopiaMapaService
   - ImpactoMapaService
3. [ ] Manter `AtividadeFacade` separada (domínio diferente)

**Fase 2: Revisar Services Especializados (2 dias)**
4. [ ] Identificar duplicações entre services
5. [ ] Consolidar lógica similar
6. [ ] Tornar services `package-private`

**Fase 3: Atualizar Controllers (1 dia)**
7. [ ] `MapaController` usa `MapaFacade`
8. [ ] `AtividadeController` continua usando `AtividadeFacade`
9. [ ] Remover injeções diretas de services especializados

**Validação:**
- [ ] Todos os testes passam
- [ ] Controllers usam Facades
- [ ] Arquitetura consistente com outros módulos

**Entregáveis:**
- [ ] MapaFacade implementada
- [ ] Controllers refatorados
- [ ] Testes atualizados

---

### **Sprint 4: Padronização de Imports e Nomenclatura (2-3 dias)**

**Objetivo:** Eliminar wildcard imports e padronizar nomes

**Tarefas:**

**Fase 1: Wildcard Imports (1 dia - AUTOMATIZADA)**
1. [ ] Configurar IDE para imports explícitos
2. [ ] Executar refatoração automática em todos os 138 arquivos:
   ```bash
   # IntelliJ IDEA batch mode
   idea.sh format -r -s backend/src/main/java/sgc
   ```
3. [ ] Configurar Checkstyle para prevenir novos wildcards

**Fase 2: Nomenclatura de Classes (2 dias)**
4. [ ] Criar guia de nomenclatura (baseado em análise)
5. [ ] Identificar classes com nomes inconsistentes
6. [ ] Renomear classes (usar refactoring do IDE):
   - Exemplos:
     - `ProcessoConsultaService` → `ProcessoQueryService`
     - `ProcessoInicializador` → `ProcessoBootstrapService`
7. [ ] Atualizar todos os imports
8. [ ] Executar testes

**Validação:**
- [ ] 0 wildcard imports
- [ ] Nomenclatura consistente em 100% das classes
- [ ] Todos os testes passam

**Entregáveis:**
- [ ] Código com imports explícitos
- [ ] Guia de nomenclatura documentado
- [ ] Classes renomeadas

---

### **Sprint 5: Eventos de Domínio (3-4 dias)**

**Objetivo:** Implementar eventos faltantes e refatorar comunicação síncrona

**Tarefas:**

**Fase 1: Auditoria (1 dia)**
1. [ ] Listar os 23 eventos esperados (de backend-padroes.md)
2. [ ] Identificar os 6 existentes
3. [ ] Identificar 17 eventos faltantes
4. [ ] Mapear onde eventos deveriam ser usados

**Fase 2: Implementação (2 dias)**
5. [ ] Criar classes de evento faltantes
6. [ ] Implementar listeners correspondentes
7. [ ] Refatorar chamadas síncronas para publicação de eventos:
   - NotificacaoService
   - AlertaService
   - Outros módulos reativos

**Fase 3: Testes (1 dia)**
8. [ ] Criar testes para novos eventos e listeners
9. [ ] Validar que comportamento não mudou
10. [ ] Teste E2E para garantir emails/alertas continuam funcionando

**Validação:**
- [ ] 23 eventos implementados
- [ ] Módulos desacoplados via eventos
- [ ] Todos os testes passam

**Entregáveis:**
- [ ] Eventos de domínio implementados
- [ ] Listeners criados
- [ ] Testes de eventos

---

### **Sprint 6: Frontend - Stores e Composables (3-4 dias)**

**Objetivo:** Extrair lógica de stores para composables reutilizáveis

**Tarefas:**

**Fase 1: Análise (1 dia)**
1. [ ] Revisar as 12 stores
2. [ ] Identificar lógica que deveria estar em composables:
   - Validações complexas
   - Cálculos de estado
   - Lógica compartilhada entre stores

**Fase 2: Extração (2 dias)**
3. [ ] Criar composables para lógica identificada:
   - `usePermissoes` - Lógica de permissões
   - `useValidacao` - Validações complexas
   - `useFormatacao` - Formatação de dados
4. [ ] Refatorar stores para usar composables
5. [ ] Atualizar testes

**Fase 3: Limpeza (1 dia)**
6. [ ] Remover 19 console.log/error/warn
7. [ ] Implementar logger adequado
8. [ ] Configurar ESLint rule

**Validação:**
- [ ] Stores mais enxutas
- [ ] Composables reutilizáveis criados
- [ ] Console limpo
- [ ] Todos os testes passam

**Entregáveis:**
- [ ] Composables criados
- [ ] Stores refatoradas
- [ ] Logger implementado

---

### **Sprint 7: Mappers e Padronização (2 dias)**

**Objetivo:** Padronizar localização e eliminar duplicação em mappers

**Tarefas:**

**Fase 1: Reorganização (1 dia)**
1. [ ] Mover todos os mappers para `{modulo}/mapper/`
2. [ ] Atualizar imports
3. [ ] Executar testes

**Fase 2: Deduplicate (1 dia)**
4. [ ] Identificar lógica duplicada entre mappers
5. [ ] Extrair mappers reutilizáveis (ex: UnidadeMapper)
6. [ ] Configurar MapStruct para usar mappers compartilhados
7. [ ] Executar testes

**Validação:**
- [ ] Todos os mappers em `{modulo}/mapper/`
- [ ] Nenhuma lógica duplicada
- [ ] Testes passam

**Entregáveis:**
- [ ] Mappers reorganizados
- [ ] Lógica deduplicate

---

### **Sprint 8: Testes e Cobertura (3-4 dias)**

**Objetivo:** Aumentar cobertura de testes para 80%

**Tarefas:**

**Fase 1: Análise (1 dia)**
1. [ ] Gerar relatório de cobertura atualizado
2. [ ] Identificar módulos/classes com <80% cobertura
3. [ ] Priorizar por criticidade:
   - Facades
   - Workflows
   - Segurança (AccessControlService)

**Fase 2: Implementação (2 dias)**
4. [ ] Criar testes faltantes para módulos prioritários
5. [ ] Aumentar cobertura gradualmente

**Fase 3: Validação (1 dia)**
6. [ ] Executar suite completa
7. [ ] Gerar relatório final
8. [ ] Documentar áreas que ficaram abaixo de 80% com justificativa

**Validação:**
- [ ] Cobertura global ≥ 80%
- [ ] Módulos críticos com ≥ 90% cobertura
- [ ] Todos os testes passam

**Entregáveis:**
- [ ] Testes adicionados
- [ ] Relatório de cobertura
- [ ] Documentação de cobertura

---

### **Sprint 9: Documentação e Finalização (2-3 dias)**

**Objetivo:** Atualizar documentação e preparar para merge

**Tarefas:**

**Fase 1: Atualizar Documentação (2 dias)**
1. [ ] Atualizar `/regras/backend-padroes.md`:
   - Contadores atualizados (services, events, etc.)
   - Remover referências a código depreciado
   - Adicionar padrão Facade documentado
2. [ ] Atualizar `/regras/frontend-padroes.md`:
   - Documentar composables
   - Atualizar contadores
3. [ ] Atualizar READMEs dos módulos:
   - subprocesso
   - mapa
   - processo
   - organizacao
4. [ ] Atualizar AGENTS.md com novos padrões
5. [ ] Criar CHANGELOG.md com resumo de mudanças

**Fase 2: Validação Final (1 dia)**
6. [ ] Executar análise estática completa
7. [ ] Comparar com baseline do Sprint 0
8. [ ] Executar suite completa de testes:
   - Backend: `./gradlew test` (1149 testes)
   - Frontend: `npm run test:unit`
   - E2E: `npm run test:e2e`
9. [ ] Code review final
10. [ ] Preparar PR para merge

**Validação:**
- [ ] Documentação completa e atualizada
- [ ] Análise estática: 0 violações críticas
- [ ] Todos os testes passam
- [ ] Code review aprovado

**Entregáveis:**
- [ ] Documentação atualizada
- [ ] CHANGELOG.md
- [ ] PR para merge
- [ ] Aprovação de code review

---

## 📈 MÉTRICAS DE SUCESSO

### Métricas Quantitativas

| Métrica | Baseline | Meta | Medição |
|---------|----------|------|---------|
| **Backend** |
| Services em subprocesso | 12 | 4 | Sprint 2 |
| Services em mapa | 13 | ~6 | Sprint 3 |
| Wildcard imports | 138 | 0 | Sprint 4 |
| Eventos implementados | 6 | 23 | Sprint 5 |
| Código depreciado | 10 | 0 | Sprint 1 |
| Cobertura de testes | ? | ≥80% | Sprint 8 |
| **Frontend** |
| Console.log residuais | 19 | 0 | Sprint 6 |
| Stores com lógica pesada | ? | 0 | Sprint 6 |
| **Qualidade** |
| TODOs/FIXMEs | 2 | 0 | Sprints 1-9 |
| Complexidade ciclomática (avg) | ? | <10 | Sprint 9 |
| Duplicação de código (%) | ? | <5% | Sprint 9 |
| **Documentação** |
| READMEs desatualizados | ? | 0 | Sprint 9 |

### Métricas Qualitativas

- [ ] **Arquitetura Consistente** - Todos os módulos seguem padrão Facade
- [ ] **Separação de Responsabilidades** - Services com uma única responsabilidade clara
- [ ] **Desacoplamento** - Módulos comunicam via eventos onde apropriado
- [ ] **Testabilidade** - Código facilmente testável, mocks simples
- [ ] **Manutenibilidade** - Novos desenvolvedores conseguem entender código rapidamente
- [ ] **Documentação** - Documentação reflete código atual

---

## ⚠️ RISCOS E MITIGAÇÕES

| Risco | Probabilidade | Impacto | Mitigação |
|-------|--------------|---------|-----------|
| **Quebrar funcionalidade existente** | Média | 🔴 Alto | - Testes E2E abrangentes<br>- Refatoração incremental<br>- Code review rigoroso |
| **Aumentar tempo de build** | Baixa | 🟡 Médio | - Monitorar performance<br>- Otimizar se necessário |
| **Refatoração muito ambiciosa** | Média | 🟡 Médio | - Sprints bem definidos<br>- Validação entre sprints<br>- Pode pausar/ajustar |
| **Conflitos de merge** | Alta | 🟡 Médio | - Branch de longa duração<br>- Rebase frequente<br>- Comunicação com time |
| **Fadiga da equipe** | Baixa | 🟡 Médio | - Sprints de 2-7 dias<br>- Pausas entre sprints<br>- Celebrar conquistas |

---

## 🎯 CRITÉRIOS DE ACEITAÇÃO

### Por Sprint

Cada sprint deve:
- [ ] Ter todos os testes passando (1149 backend + frontend + E2E)
- [ ] Não introduzir novas violações de análise estática
- [ ] Ter code review aprovado
- [ ] Ter documentação atualizada
- [ ] Ser deployável (não quebrar build)

### Projeto Completo

- [ ] **Arquitetura:**
  - Padrão Facade implementado em todos os módulos principais
  - Services organizados por responsabilidade
  - Eventos de domínio completos (23)
  - Dependências claras e documentadas
- [ ] **Qualidade de Código:**
  - 0 wildcard imports
  - 0 código depreciado
  - Nomenclatura 100% consistente
  - Cobertura ≥80%
- [ ] **Frontend:**
  - Stores enxutas
  - Composables reutilizáveis
  - Console limpo
- [ ] **Documentação:**
  - Toda documentação reflete código atual
  - Guias de padrões atualizados
  - CHANGELOG completo
- [ ] **Validação:**
  - 100% dos testes passando
  - Análise estática sem violações críticas
  - Code review aprovado
  - Validação E2E completa

---

## 📅 CRONOGRAMA ESTIMADO

| Sprint | Duração | Esforço (dev-days) |
|--------|---------|-------------------|
| Sprint 0 | 2 dias | 2 |
| Sprint 1 | 2-3 dias | 2.5 |
| Sprint 2 | 5-7 dias | 6 |
| Sprint 3 | 4-5 dias | 4.5 |
| Sprint 4 | 2-3 dias | 2.5 |
| Sprint 5 | 3-4 dias | 3.5 |
| Sprint 6 | 3-4 dias | 3.5 |
| Sprint 7 | 2 dias | 2 |
| Sprint 8 | 3-4 dias | 3.5 |
| Sprint 9 | 2-3 dias | 2.5 |
| **TOTAL** | **28-39 dias** | **32.5 dev-days** |

**Nota:** Estimativas assumem 1 desenvolvedor full-time. Com 2 desenvolvedores, pode ser reduzido para ~20 dias corridos.

---

## 🚀 COMO EXECUTAR

### Pré-requisitos

1. [ ] Aprovar este plano com stakeholders
2. [ ] Alocar recursos (1-2 desenvolvedores)
3. [ ] Reservar tempo no roadmap (~5-6 semanas)
4. [ ] Configurar ferramentas de análise estática

### Execução

1. **Executar Sprint 0** (preparação)
2. **Para cada Sprint 1-9:**
   - Executar tarefas definidas
   - Executar validação
   - Code review
   - Merge para branch principal de refatoração
   - Celebrar conquista! 🎉
3. **Após Sprint 9:**
   - Merge final para `main`
   - Deploy
   - Retrospectiva

### Comandos de Validação

```bash
# Backend - Testes
cd backend && ./gradlew test

# Backend - Cobertura
cd backend && ./gradlew jacocoTestReport

# Backend - Análise estática
cd backend && ./gradlew check

# Frontend - Type check
cd frontend && npm run typecheck

# Frontend - Lint
cd frontend && npm run lint

# Frontend - Testes
cd frontend && npm run test:unit

# E2E
npm run test:e2e
```

---

## 📚 REFERÊNCIAS

### Documentos Existentes
- `/regras/backend-padroes.md` - Padrões de backend
- `/regras/frontend-padroes.md` - Padrões de frontend
- `/regras/guia-validacao.md` - Guia de validação
- `/regras/guia-excecoes.md` - Guia de exceções
- `/regras/guia-testes-junit.md` - Guia de testes
- `/SECURITY-REFACTORING.md` - Refatoração de segurança (concluída)
- `/security-refactoring-plan.md` - Plano de segurança

### Requisitos
- `/reqs/_intro.md` - Introdução ao sistema
- `/reqs/cdu-*.md` - Casos de uso (36 CDUs)

### Arquivos Chave Analisados
- `backend/src/main/java/sgc/subprocesso/service/` - 12 services
- `backend/src/main/java/sgc/mapa/service/` - 13 services
- `backend/src/main/java/sgc/processo/service/ProcessoFacade.java`
- `backend/src/main/java/sgc/mapa/service/AtividadeFacade.java`
- `frontend/src/stores/` - 12 stores

---

## 💡 PRINCÍPIOS NORTEADORES

Este plano de refatoração segue os seguintes princípios:

1. **Simplicidade sobre Complexidade** - Reduzir, não adicionar
2. **Consistência sobre Flexibilidade** - Padrões claros e seguidos
3. **Pragmatismo sobre Purismo** - Soluções práticas, não teóricas
4. **Incremental sobre Big Bang** - Mudanças graduais e validadas
5. **Medido sobre Assumido** - Métricas objetivas de sucesso
6. **Documentado sobre Implícito** - Decisões arquiteturais documentadas
7. **Testado sobre Confiado** - Validação rigorosa em cada etapa

---

## ✅ PRÓXIMOS PASSOS IMEDIATOS

1. [ ] **Revisão deste documento** com time de desenvolvimento
2. [ ] **Aprovação** por stakeholders (PO, Tech Lead)
3. [ ] **Criação de issues/tasks** no sistema de gestão de projeto
4. [ ] **Alocação de recursos** (desenvolvedores)
5. [ ] **Início do Sprint 0** (análise e preparação)

---

**FIM DO DOCUMENTO**

---

**Versão:** 1.0  
**Criado em:** 2026-01-10  
**Autor:** AI Agent (GitHub Copilot)  
**Aprovação Pendente:** Sim

**Nota para Agente AI Executor:**

Este plano foi desenhado para execução incremental e iterativa por um agente AI como você. Cada sprint:
- Está bem definido e auto-contido
- Tem validação clara
- Pode ser executado independentemente (com dependências explícitas)
- Possui critérios objetivos de sucesso

**Recomendações:**
- Execute um sprint por vez
- SEMPRE valide com testes antes de prosseguir
- Documente desvios e decisões
- Use `report_progress` frequentemente
- Se encontrar bloqueios, sinalize e peça orientação

Boa sorte! 🚀
