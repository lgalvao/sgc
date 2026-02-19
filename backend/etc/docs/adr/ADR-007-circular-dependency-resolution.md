# ADR-007: Resolução de Dependências Circulares

**Status:** ✅ Ativo

---

## Contexto e Problema

Durante análise arquitetural foram identificados ciclos de dependência entre módulos:

### Ciclos Identificados

**1. Mapa ↔ Subprocesso (Service Layer)**
- `AtividadeFacade` → `SubprocessoFacade`
- `SubprocessoFacade` → `MapaFacade` / `MapaManutencaoService`

**2. Processo ↔ Subprocesso (Service Layer)**
- `ProcessoFacade` → `SubprocessoFacade`
- Consultas legítimas de dados relacionados

**3. Model Layer (Entidades JPA)**
- Relacionamentos bidirecionais entre `Mapa` ↔ `Subprocesso`
- Relacionamentos bidirecionais entre `Processo` ↔ `Unidade`

### Problema
Ciclos de dependência podem causar:
1. Falhas de inicialização do Spring
2. Dificuldade de manutenção
3. Acoplamento excessivo

---

## Decisão

### Estratégia 1: @Lazy Injection

**Quando usar:**
- Consultas síncronas de dados relacionados
- Relacionamentos legítimos entre agregados
- Não há como eliminar a dependência via eventos

**Implementação:**
```java
// AtividadeFacade
public AtividadeFacade(
        MapaManutencaoService mapaManutencaoService,
        @Lazy SubprocessoFacade subprocessoFacade,  // ← @Lazy quebra ciclo
        AccessControlService accessControlService,
        UsuarioFacade usuarioService,
        MapaFacade mapaFacade,
        ApplicationEventPublisher eventPublisher) {
    // ...
}
```

**Classes com @Lazy implementado:**
- `AtividadeFacade` → `SubprocessoFacade`
- `ProcessoConsultaService` → `SubprocessoFacade`
- `ProcessoAcessoService` → `SubprocessoFacade`
- `ProcessoFinalizador` → `SubprocessoFacade`
- `ProcessoValidador` → `SubprocessoFacade`
- `SubprocessoCrudService` → `MapaFacade`

### Estratégia 2: Spring Events

**Quando usar:**
- Operações assíncronas e workflows
- Comunicação entre agregados de domínios diferentes
- Necessidade de desacoplamento total

**Eventos já implementados:**
- `EventoTransicaoSubprocesso` - padrão unificado ⭐
- `EventoMapaAlterado`
- `EventoProcessoIniciado`
- `EventoProcessoFinalizado`

### Estratégia 3: Ciclos Aceitáveis

**Model Layer (Entidades JPA):**
- Relacionamentos bidirecionais são esperados e legítimos
- Exemplo: `Mapa.subprocesso` ↔ `Subprocesso.mapa`
- Não requer resolução

---

## Análise de Impacto

### Consultas vs Operações

**Consultas (OK com @Lazy):**
- `subprocessoFacade.listarEntidadesPorProcesso()` ✅
- `subprocessoFacade.obterSituacao()` ✅
- `subprocessoFacade.obterEntidadePorCodigoMapa()` ✅

**Operações (candidatas a eventos):**
- `ProcessoInicializador.iniciar()` → cria subprocessos (operação síncrona dentro do mesmo contexto transacional)

### Decisões Técnicas

1. **@Lazy é preferível a eventos** quando:
   - A operação é síncrona
   - Os dados são consultados, não modificados
   - Não há benefício em processamento assíncrono

2. **Eventos são preferíveis a @Lazy** quando:
   - Operação pode ser assíncrona
   - Desacoplamento total é necessário
   - Múltiplos listeners podem reagir ao mesmo evento

---

## Validação

### Testes ArchUnit

Criado `CyclicDependencyTest.java` com:
```java
@ArchTest
static final ArchRule no_cycles_within_service_packages = slices()
        .matching("sgc.(*).service.(**)")
        .should()
        .beFreeOfCycles();
```

**Resultado:** ✅ Todos os testes passam

### Compilação e Execução

✅ Backend compila sem erros  
✅ Aplicação inicia corretamente  
✅ Testes unitários passam

---

## Consequências

### Positivas ✅

1. **Inicialização Spring bem-sucedida** - Não há mais ciclos bloqueantes
2. **Código limpo** - @Lazy documenta explicitamente dependências circulares
3. **Manutenibilidade** - Ciclos documentados e gerenciáveis
4. **Testabilidade** - Testes ArchUnit garantem não introdução de novos ciclos

### Negativas ❌

1. **Overhead de proxy** - @Lazy cria proxy CGLIB (impacto negligível)
2. **Inicialização tardia** - Bean é inicializado no primeiro uso
3. **Documentação necessária** - Deve explicar por que @Lazy foi usado

---

## Métricas

**Ciclos Resolvidos:**
- Service Layer: 2 ciclos (Mapa ↔ Subprocesso; Processo ↔ Subprocesso)
- Facades com @Lazy: 1 (AtividadeFacade)
- Services com @Lazy: 5 (Processo*)

**Ciclos Aceitáveis:**
- Model Layer: 4 ciclos (relacionamentos JPA bidirecionais)

---

## Referências

- ADR-001: Facade Pattern
- ADR-002: Unified Events Pattern
- [Spring @Lazy Documentation](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-lazy-init.html)
- [Circular Dependencies in Spring](https://www.baeldung.com/circular-dependencies-in-spring)
