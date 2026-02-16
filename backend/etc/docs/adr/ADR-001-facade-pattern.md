# ADR-001: Uso do Padrão Facade para Orquestração de Serviços

**Status:** ✅ Ativo (Atualizado 2026-02-16)

---

## Contexto e Problema

O sistema SGC possui múltiplos módulos (processo, subprocesso, mapa, atividade) com lógica de negócio complexa. Cada
módulo tem vários services especializados que implementam funcionalidades específicas.

**Problemas Identificados:**

1. Controllers acessavam diretamente múltiplos services
2. Lógica de orquestração espalhada nos controllers
3. Difícil testar fluxos completos
4. Violação do Single Responsibility Principle nos controllers
5. Acoplamento excessivo entre controllers e services

---

## Decisão

Adotar o **Padrão Facade** para encapsular a orquestração de services especializados.

**Princípios:**

1. Um Facade por módulo de domínio (ou submódulo coeso)
2. Controllers usam APENAS Facades (exceto casos simples CRUD)
3. Facades orquestram services especializados
4. Services especializados focam em responsabilidade única

### Exceções à Regra (Atualizado 2026-02-16)

Após revisão arquitetural, identificamos que facades "pass-through" (que apenas delegam sem orquestração) adicionam indireção desnecessária.

**Casos onde Facade NÃO é necessário:**

1. **CRUD Simples:** Controllers de configuração que apenas fazem operações básicas podem acessar Service diretamente
   ```java
   // ✅ PERMITIDO: CRUD simples sem orquestração
   @RestController
   public class ConfiguracaoController {
       private final ConfiguracaoService service;  // Acesso direto OK
   }
   ```

2. **Facades Wrapper Puros:** Eliminadas se não agregam valor
   - **Exemplo Removido:** `AcompanhamentoFacade` (apenas delegava para AlertaFacade, AnaliseFacade, PainelFacade)
   - **Decisão:** Controllers acessam facades específicas diretamente

**Casos onde Facade É necessário:**

1. **Orquestração Complexa:** Coordena múltiplos services
   ```java
   // ✅ NECESSÁRIO: Orquestração de workflow
   @Service
   public class SubprocessoFacade {
       private final SubprocessoWorkflowService workflowService;
       private final SubprocessoAtividadeService atividadeService;
       private final SubprocessoContextoService contextoService;
       private final NotificacaoService notificacaoService;
       
       public void aceitarCadastro(Long codigo, AceitarCadastroRequest req) {
           // Orquestra múltiplos services + validações
       }
   }
   ```

2. **Agregação de Módulos:** Combina funcionalidades de módulos relacionados
   ```java
   // ✅ NECESSÁRIO: Agregação
   public class ProcessoFacade {
       // Coordena Processo + Subprocesso + Mapa
   }
   ```

---


## Consequências

### Positivas ✅

- Controllers mais simples e focados
- Lógica de orquestração centralizada e reutilizável
- Services mais coesos com responsabilidade única
- Melhor testabilidade (testes de orquestração isolados)
- Facilita evolução e manutenção

### Negativas ❌

- Camada adicional de abstração
- Possível God Class se mal gerenciada (mitigado por services especializados)
- ⚠️ **Atualização 2026-02-16:** Facades sem orquestração real adicionam indireção desnecessária

### Lições Aprendidas (2026-02-16)

Durante revisão arquitetural identificamos:

1. **Facades Pass-Through São Anti-Padrão:**
   - `AcompanhamentoFacade` apenas delegava → **eliminada**
   - `ConfiguracaoFacade` apenas delegava → **eliminada**
   - **Resultado:** -2 facades (-117 LOC de indireção)

2. **Critérios para Justificar Facade:**
   - Orquestra 3+ services
   - Possui lógica de coordenação complexa
   - Gerencia transações cross-cutting
   - Agrega funcionalidades de múltiplos módulos

3. **Exceções Documentadas:**
   - Controllers CRUD simples podem usar Service diretamente
   - Documentado em ArchConsistencyTest com exceções explícitas
   - Code review valida justificativa

**Métricas:**
- Facades totais: 14 → 12 (-14%)
- Facades com orquestração real: 12/12 (100%)
- Indireção desnecessária eliminada: 117 LOC

---

## Conformidade

Testes ArchUnit garantem que controllers usam apenas Facades:

- Ver `sgc.arquitetura.ArchConsistencyTest`
