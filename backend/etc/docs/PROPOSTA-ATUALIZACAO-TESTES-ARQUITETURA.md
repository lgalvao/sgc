# Proposta de Atualização: Testes de Arquitetura

**Data:** 15 de Fevereiro de 2026  
**Arquivo:** `backend/src/test/java/sgc/arquitetura/ArchConsistencyTest.java`  
**Status:** 🟡 Aguardando Implementação

---

## 📋 Contexto

Este documento define **como adaptar as 16 regras ArchUnit** para suportar o plano de simplificação, mantendo a qualidade arquitetural.

**Princípio:** Regras devem **facilitar** boas práticas, não **impedir** simplificação legítima.

---

## 🎯 Mudanças por Fase

### FASE 1: Generalização (BAIXO risco)

#### 1. Generalizar Regras Específicas

**Problema:** Regras #2 e #3 são muito específicas para controllers individuais.

**Regras Atuais (REMOVER):**
```java
@ArchTest
static final ArchRule mapa_controller_should_only_access_mapa_service = classes()
    .that().haveSimpleName("MapaController")
    .should().onlyAccessClassesThat().haveNameMatching("MapaFacade")
    // ...

@ArchTest
static final ArchRule processo_controller_should_only_access_processo_service = classes()
    .that().haveSimpleName("ProcessoController")
    .should().onlyAccessClassesThat().haveNameMatching("ProcessoService")
    // ...
```

**Nova Regra Genérica (ADICIONAR):**
```java
/**
 * Garante que Controllers acessem apenas Services/Facades de seu próprio módulo.
 * Isso mantém baixo acoplamento entre módulos.
 * 
 * <p>Permite tanto Services quanto Facades, mas não permite cross-module access.
 */
@ArchTest
static final ArchRule controllers_should_access_own_module_services_only = classes()
        .that()
        .haveNameMatching(".*Controller")
        .should(new ArchCondition<>("access only services from their own module") {
            @Override
            public void check(JavaClass controller, ConditionEvents events) {
                String controllerModule = extractModule(controller.getPackageName());
                if (controllerModule == null) return;

                for (Dependency dependency : controller.getDirectDependenciesFromSelf()) {
                    JavaClass targetClass = dependency.getTargetClass();
                    
                    // Verifica se é um @Service
                    boolean isService = targetClass.isAnnotatedWith(Service.class);
                    if (!isService) continue;
                    
                    String serviceModule = extractModule(targetClass.getPackageName());
                    
                    // Permite acesso apenas ao próprio módulo ou "comum"
                    if (serviceModule != null && 
                        !serviceModule.equals(controllerModule) && 
                        !serviceModule.equals("comum")) {
                        String message = String.format(
                                "Controller %s (module %s) depends on Service %s (module %s). " +
                                "Controllers should only access services from their own module.",
                                controller.getSimpleName(), controllerModule,
                                targetClass.getSimpleName(), serviceModule);
                        events.add(SimpleConditionEvent.violated(dependency, message));
                    }
                }
            }

            private String extractModule(String packageName) {
                if (!packageName.startsWith("sgc.")) return null;
                int firstDot = packageName.indexOf('.');
                if (firstDot == -1) return null;
                int secondDot = packageName.indexOf('.', firstDot + 1);
                if (secondDot == -1) return packageName.substring(firstDot + 1);
                return packageName.substring(firstDot + 1, secondDot);
            }
        })
        .because("Controllers should only access services from their own module to maintain low coupling");
```

**Impacto:**
- ✅ Remove 2 regras específicas
- ✅ Adiciona 1 regra genérica
- ✅ Cobre TODOS os controllers (não apenas Mapa e Processo)
- ✅ Permite simplificação (Facade OU Service, ambos funcionam)

---

### FASE 2: Adaptação para Facades Opcionais (MÉDIO risco)

#### 2. Adaptar Regra de Facades Obrigatórios

**Problema:** Regra #7 **força** uso de Facades, impedindo simplificação.

**Regra Atual (ADAPTAR):**
```java
@ArchTest
static final ArchRule controllers_should_only_use_facades_not_specialized_services = classes()
    .that().haveNameMatching(".*Controller")
    .should(/* força uso de Facades */)
    .because("Controllers should only use Facades (ADR-001) ...");
```

**Nova Regra (SUBSTITUIR):**
```java
/**
 * Garante que Controllers usem CONSISTENTEMENTE Services OU Facades, mas não misturem.
 * 
 * <p>Após simplificação, alguns módulos usam Facades (complexos) e outros usam 
 * Services direto (simples). Esta regra garante consistência DENTRO de cada controller.
 * 
 * <p><b>Motivação:</b> Permitir simplificação sem perder consistência arquitetural.
 * Um controller que usa Facade não deve também chamar Services especializados 
 * diretamente (isso quebraria o encapsulamento da Facade).
 */
@ArchTest
static final ArchRule controllers_should_use_consistently_services_or_facades = classes()
        .that()
        .haveNameMatching(".*Controller")
        .should(new ArchCondition<>("use consistently Services OR Facades, not both") {
            @Override
            public void check(JavaClass controller, ConditionEvents events) {
                boolean usesFacade = false;
                boolean usesSpecializedService = false;

                for (Dependency dependency : controller.getDirectDependenciesFromSelf()) {
                    JavaClass targetClass = dependency.getTargetClass();
                    
                    if (!targetClass.isAnnotatedWith(Service.class)) continue;
                    
                    if (targetClass.getSimpleName().endsWith("Facade")) {
                        usesFacade = true;
                    } else {
                        usesSpecializedService = true;
                    }
                }

                // PROBLEMA: Mistura de Facade + Service especializado
                if (usesFacade && usesSpecializedService) {
                    String message = String.format(
                            "Controller %s uses BOTH Facade and specialized Services. " +
                            "Choose one pattern consistently: either use Facade (complex modules) " +
                            "or Services directly (simple modules). " +
                            "Mixing both breaks encapsulation.",
                            controller.getSimpleName());
                    events.add(SimpleConditionEvent.violated(controller, message));
                }
            }
        })
        .because("Controllers should use Services OR Facades consistently, not mix both");
```

**Impacto:**
- ✅ Permite eliminar Facades onde não são necessários
- ✅ Mantém consistência (não mistura padrões)
- ✅ Flexível para diferentes níveis de complexidade de módulos

#### 3. Remover Regra de Facades-Repositories

**Problema:** Regra #15 só faz sentido se Facades existirem.

**Ação:** **REMOVER** completamente após eliminar facades pass-through.

```java
// REMOVER ESTA REGRA (não mais necessária)
@ArchTest
static final ArchRule facades_should_not_access_repositories_directly = /* ... */
```

**Justificativa:**
- ❌ Se não há Facade, regra não se aplica
- ✅ Regra já existente (#1) garante Controllers não acessam Repos
- ✅ Simplifica suite de testes

#### 4. Adaptar Regra de Entities em Controllers

**Problema:** Regra #10 proíbe retornar Entities, mas @JsonView **permite** de forma segura.

**Regra Atual (ADAPTAR):**
```java
@ArchTest
static final ArchRule controllers_should_not_return_jpa_entities = methods()
    .that().arePublic()
    .and().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
    .should().notHaveRawReturnType(annotatedWith(Entity.class))
    .because("JPA entities should never be exposed directly - use DTOs instead");
```

**Nova Regra (SUBSTITUIR):**
```java
/**
 * Garante que Entities retornadas por Controllers tenham @JsonView para proteger dados sensíveis.
 * 
 * <p>Permite @JsonView como alternativa a DTOs, mas exige que views estejam definidas
 * para prevenir vazamento de dados (campos lazy, relações, dados sensíveis).
 * 
 * <p><b>Padrão aceito:</b>
 * <pre>
 * &#64;GetMapping("/{id}")
 * &#64;JsonView(Processo.Public.class)
 * public Processo buscar(@PathVariable Long id) { ... }
 * </pre>
 */
@ArchTest
static final ArchRule controllers_returning_entities_must_use_jsonview = methods()
        .that()
        .arePublic()
        .and()
        .areDeclaredInClassesThat()
        .areAnnotatedWith(RestController.class)
        .and()
        .haveRawReturnType(annotatedWith(Entity.class))
        .should(new ArchCondition<>("use @JsonView annotation for entity serialization") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                boolean hasJsonView = method.isAnnotatedWith("com.fasterxml.jackson.annotation.JsonView");
                
                if (!hasJsonView) {
                    String message = String.format(
                            "Controller method %s.%s returns Entity %s without @JsonView. " +
                            "Add @JsonView to control which fields are serialized and prevent data leaks. " +
                            "Example: @JsonView(YourEntity.Public.class)",
                            method.getOwner().getSimpleName(),
                            method.getName(),
                            method.getRawReturnType().getSimpleName());
                    events.add(SimpleConditionEvent.violated(method, message));
                }
            }
        })
        .because("Entities must use @JsonView to prevent sensitive data exposure");
```

**Impacto:**
- ✅ Permite @JsonView (simplificação de DTOs)
- ✅ Mantém segurança (exige declaração de view)
- ✅ Força boas práticas (@JsonView obrigatório)

---

### FASE 3: Segurança (OPCIONAL - ALTO risco)

#### 5. Revisar Regra de AccessControl

**Problema:** Se AccessPolicies forem simplificadas, regra #11 pode precisar ajuste.

**⚠️ DECISÃO PENDENTE:** Aguardar aprovação da Fase 3 antes de definir mudança.

**Opções:**
1. **Manter regra atual** se AccessControlService for mantido
2. **Adaptar para @PreAuthorize** se migrarmos para anotações
3. **Remover** se simplificação for completa (NÃO RECOMENDADO)

---

## 📊 Resumo de Mudanças

| Fase | Regras Afetadas | Ação | Risco |
|------|----------------|------|-------|
| **Fase 1** | #2, #3 | Generalizar → 1 nova regra | BAIXO |
| **Fase 2** | #7 | Adaptar (Facades opcionais) | MÉDIO |
| **Fase 2** | #15 | Remover (desnecessária) | BAIXO |
| **Fase 2** | #10 | Adaptar (@JsonView permitido) | MÉDIO |
| **Fase 3** | #11 | Revisar (TBD) | ALTO |

**Total:**
- **Antes:** 16 regras
- **Depois (Fase 2):** 14 regras (2 removidas, 1 adicionada, 2 adaptadas)
- **Complexidade:** Reduzida (regras mais genéricas e flexíveis)

---

## ✅ Checklist de Implementação

### Fase 1 (Generalização)
- [ ] Remover regras #2 e #3 (específicas)
- [ ] Adicionar nova regra genérica (módulos)
- [ ] Rodar suite completa de testes ArchUnit
- [ ] Validar que todas as 15 regras passam
- [ ] Commit com mensagem: "chore(arch): generalizar regras de controllers"

### Fase 2 (Facades Opcionais)
- [ ] Adaptar regra #7 (Facades não obrigatórios)
- [ ] Remover regra #15 (Facades-Repos)
- [ ] Adaptar regra #10 (@JsonView permitido)
- [ ] Rodar suite completa de testes ArchUnit
- [ ] Validar que todas as 14 regras passam
- [ ] Adicionar testes de @JsonView (serialização)
- [ ] Commit com mensagem: "chore(arch): adaptar regras para Facades opcionais e @JsonView"

### Fase 3 (Segurança - TBD)
- [ ] Decidir sobre manutenção/adaptação da regra #11
- [ ] Implementar mudança aprovada
- [ ] Testes extensivos de segurança
- [ ] Code review com foco em segurança
- [ ] Aprovação de security officer

---

## 🔗 Referências

- [ArchConsistencyTest.java](../../../../../backend/src/test/java/sgc/arquitetura/ArchConsistencyTest.java) - Arquivo atual
- [PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md](../../../../../PLANO-REDUCAO-COMPLEXIDADE-CONSOLIDADO.md) - Plano geral
- [ADR-001: Facade Pattern](../adr/ADR-001-facade-pattern.md) - Será atualizado
- [ArchUnit Documentation](https://www.archunit.org/userguide/html/000_Index.html)

---

**Elaborado por:** Agente de Consolidação de Complexidade  
**Status:** 🟡 Aguardando Implementação (Fase 1)
