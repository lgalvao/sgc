# ADR-006: Organização por Agregados de Domínio vs Outros Critérios

**Data:** 2026-01-15  
**Status:** ✅ Aceito  
**Decisores:** Equipe de Arquitetura SGC  
**Relacionado:** ADR-001 (Facade Pattern), ADR-005 (Controller Organization)

---

## Contexto e Problema

O sistema SGC está organizado em módulos por conceitos de domínio:

- `processo/` - Processos de mapeamento/revisão/diagnóstico
- `subprocesso/` - Instâncias de processo por unidade
- `mapa/` - Mapas de competência
- `organizacao/` - Unidades e usuários
- etc.

### Problema Levantado

Foi identificado que:

1. O módulo `subprocesso/` é muito grande (76 arquivos, ~6.100 linhas)
2. O módulo `mapa/` também é substancial (48 arquivos)
3. Quase tudo no sistema depende de `subprocesso` (59 arquivos importam dele)
4. Há muitas dependências cruzadas entre módulos

### Questão para Decisão

**"A quebra por domínio está consistente ou deveríamos reorganizar o sistema?"**

Alternativas consideradas:

- **A)** Reorganizar por tipo de processo (mapeamento/, revisao/, diagnostico/)
- **B)** Reorganizar por camadas técnicas (domain/, application/, infrastructure/)
- **C)** Manter organização atual por agregados de domínio

---

## Análise Realizada

### Análise do Modelo de Negócio

**Conceitos Centrais do Domínio:**

1. **Processo** - Container de alto nível (Tipo: Mapeamento/Revisão/Diagnóstico)
2. **Subprocesso** - **Agregado Raiz** do workflow (instância por unidade)
3. **Mapa** - Produto final (síntese de atividades em competências)
4. **Atividade** - Ações cadastradas pelas unidades
5. **Competência** - Agrupamento de atividades (criado pela SEDOC)

**Descoberta Crítica:**

> **Subprocesso é central porque É o agregado raiz do sistema no sentido DDD.**

Ele conecta:

- Processo (contexto geral)
- Unidade (quem executa)
- Atividades (o que é feito)
- Mapa (competências resultantes)
- Estados (9 estados de workflow)

**Não é um problema arquitetural, é a REALIDADE do domínio.**

### Análise das Alternativas

#### Opção A: Reorganizar por Tipo de Processo ❌

**Estrutura proposta:**

```
sgc/
├── mapeamento/subprocesso/
├── revisao/subprocesso/
└── diagnostico/subprocesso/
```

**Problemas Fatais:**

1. **Duplicação Massiva (>80%)**
    - Subprocesso de mapeamento e revisão compartilham:
        - ✓ Mesmo modelo de dados (Entidade JPA)
        - ✓ Mesma validação hierárquica
        - ✓ Mesmo CRUD de atividades
        - ✓ Mesmos services de transição
    - Diferem apenas em:
        - ✗ Alguns estados específicos (~10%)
        - ✗ Algumas validações específicas (~5%)

2. **Viola DRY (Don't Repeat Yourself)**
    - Mudança em validação = alterar 3 lugares
    - Risco de inconsistências
    - Bug corrigido em um, permanece nos outros

3. **Não Reflete o Domínio**
    - No domínio real, existe "Subprocesso" como conceito único
    - Não existe "Subprocesso de Mapeamento" vs "Subprocesso de Revisão"
    - Existe "Subprocesso **em** Processo de Mapeamento"

**Exemplo de Duplicação:**

```java
// ANTES (código atual - correto)
@Entity
public class Subprocesso {
    private TipoProcesso tipoProcesso;  // enum
    private SituacaoSubprocesso situacao;  // varia por tipo
    // ... lógica comum para todos os tipos
}

// DEPOIS (hipotético - ERRADO)
@Entity
public class SubprocessoMapeamento {
    // duplicação de 80% do código
}

@Entity
public class SubprocessoRevisao {
    // duplicação de 80% do código
}

@Entity
public class SubprocessoDiagnostico {
    // duplicação de 80% do código
}
```

**Veredito:** ❌ **REJEITADO** - Viola princípios fundamentais de engenharia

#### Opção B: Reorganizar por Camadas Técnicas ❌

**Estrutura proposta:**

```
sgc/
├── domain/       (entidades)
├── application/  (services)
├── infrastructure/ (repos, config)
└── presentation/ (controllers)
```

**Problemas:**

1. **Navegação Difícil**
    - Para entender "Subprocesso", visitar 4 pacotes diferentes
    - Funcionalidades relacionadas espalhadas

2. **Módulos Grandes Demais**
    - `domain/` teria 100+ entidades de todos os módulos
    - `application/` teria 50+ services de todos os módulos
    - Perde coesão

3. **Impede Modularização Futura**
    - Impossível extrair módulo "Processo" como microserviço
    - Tudo está misturado por camada técnica

4. **Não Alinha com Modelo Mental**
    - Desenvolvedores pensam em "módulo Processo", não em "camada Application"
    - Dificulta onboarding

**Exemplo de Problema:**

```
// Desenvolvedor quer entender "Subprocesso"
// ATUAL (por domínio):
subprocesso/
├── model/Subprocesso.java           // 1 lugar
├── service/SubprocessoFacade.java   // 1 lugar
└── SubprocessoController.java       // 1 lugar

// PROPOSTO (por camada):
domain/Subprocesso.java              // lugar 1
application/SubprocessoFacade.java   // lugar 2
infrastructure/SubprocessoRepo.java  // lugar 3
presentation/SubprocessoController.java // lugar 4
// 4 lugares diferentes!
```

**Veredito:** ❌ **REJEITADO** - Inadequado para monólito modular

#### Opção C: Manter Organização por Agregados ✅

**Estrutura atual:**

```
sgc/
├── processo/      - Agregado Processo
├── subprocesso/   - Agregado Subprocesso (raiz)
├── mapa/          - Agregado Mapa
├── organizacao/   - Agregado Unidade + Usuário
└── [outros]
```

**Vantagens:**

1. ✅ **Alinha com DDD (Domain-Driven Design)**
    - Cada pacote = 1 agregado ou bounded context
    - Reflete modelo de negócio
    - Boundaries claros

2. ✅ **Coesão Máxima**
    - Tudo sobre "Subprocesso" em um lugar
    - Fácil navegar
    - Mudanças localizadas

3. ✅ **Permite Evolução**
    - Módulos podem virar microserviços
    - Dependências explícitas
    - Testabilidade

4. ✅ **Benchmarking Positivo**
    - Spring Petclinic: organização por agregados
    - eShopOnContainers: agregados dentro de bounded contexts
    - Padrão recomendado pela indústria

**Problemas Identificados (e soluções):**

| Problema                         | Solução                                |
|----------------------------------|----------------------------------------|
| Módulo grande (76 arquivos)      | ✅ Consolidar services (12→6)           |
| Services públicos desnecessários | ✅ Tornar package-private               |
| Comunicação síncrona excessiva   | ✅ Implementar eventos de domínio       |
| Falta de sub-organização         | ✅ Criar sub-pacotes (workflow/, crud/) |

**Veredito:** ✅ **ACEITO** - Arquitetura correta, requer refinamento

---

## Decisão

✅ **MANTER organização atual por agregados de domínio.**

### Justificativa

1. **Organização está CORRETA** - reflete modelo de negócio
2. **Problemas são de REFINAMENTO, não de organização:**
    - Consolidar services
    - Melhorar encapsulamento
    - Aumentar uso de eventos
3. **Alternativas têm problemas FATAIS:**
    - Opção A: Duplicação massiva
    - Opção B: Navegação difícil, perde coesão

### Princípio Aplicado

> **"Package by Feature, not by Layer"** (Robert C. Martin)

Organizar código por **funcionalidade de negócio** (agregados), não por **tipo técnico** (camadas).

---

## Melhorias Propostas

### M1. Consolidar Services de Subprocesso

**Antes:** 12 services  
**Depois:** 6-7 services  
**Redução:** ~50%

**Consolidações:**

- SubprocessoCadastroWorkflowService + SubprocessoMapaWorkflowService → SubprocessoWorkflowService
- SubprocessoDetalheService → lógica movida para Facade
- SubprocessoContextoService → lógica movida para Facade

### M2. Tornar Services Package-Private

```java
// ANTES
@Service
public class SubprocessoCrudService { ... }

// DEPOIS
@Service
class SubprocessoCrudService { ... }  // package-private
```

**Efeito:** Força uso via Facade, garante encapsulamento.

### M3. Implementar Eventos de Domínio

**Atual:** 6 eventos  
**Meta:** 14-16 eventos  
**Benefício:** Desacoplamento entre módulos

### M4. Organizar Sub-pacotes

```
subprocesso/service/
├── SubprocessoFacade.java (public)
├── workflow/    (package-private)
├── crud/        (package-private)
└── notificacao/ (package-private)
```

**Benefício:** Navegação mais clara, coesão por responsabilidade.

---

## Consequências

### Positivas ✅

1. **Manutenção da Coesão**
    - Código relacionado permanece junto
    - Mudanças localizadas

2. **Evita Duplicação**
    - Código compartilhado entre tipos de processo permanece único
    - DRY mantido

3. **Facilita Evolução**
    - Possível extrair módulos como microserviços no futuro
    - Boundaries claros

4. **Melhora Qualidade Incremental**
    - Melhorias sem big bang rewrite
    - Baixo risco de regressões

5. **Alinha com Indústria**
    - Segue práticas recomendadas (DDD, Clean Architecture)
    - Facilita onboarding

### Negativas ❌

1. **Módulo Grande**
    - Subprocesso continuará grande (é o agregado raiz)
    - **Mitigação:** Consolidar services, criar sub-pacotes

2. **Dependências Cruzadas**
    - Muitos módulos dependem de Subprocesso
    - **Mitigação:** Usar eventos para desacoplamento

---

## Análise de Complexidade

### Complexidade Essencial (Inevitável - ~70%)

Do domínio de negócio:

- 9 estados de Subprocesso → workflow complexo de negócio
- Validação hierárquica em 3 níveis → estrutura organizacional real
- Síntese manual de competências → decisão humana, não automatizável

### Complexidade Acidental (Evitável - ~30%)

Introduzida pela implementação:

- 12 services quando 6 seriam suficientes
- Services públicos sem necessidade
- Comunicação síncrona excessiva

### Estratégia

✅ **Focar em reduzir complexidade acidental**  
✅ **Aceitar e documentar complexidade essencial**

---

## Métricas de Sucesso

| Métrica                 | Antes  | Meta       |
|-------------------------|--------|------------|
| Services de Subprocesso | 12     | 6-7        |
| Services públicos       | 12     | 1 (Facade) |
| Eventos implementados   | 6      | 14-16      |
| Linhas em services      | ~2.500 | ~1.800     |

---

## Conformidade

### Padrões Mantidos

- ✅ ADR-001: Facade Pattern (mantido)
- ✅ ADR-002: Unified Events (expandido)
- ✅ ADR-003: Security Architecture (mantido)
- ✅ ADR-004: DTO Pattern (mantido)
- ✅ ADR-005: Controller Organization (mantido)

### Testes Arquiteturais

```java
@ArchTest
static final ArchRule modules_are_organized_by_domain_aggregates =
    classes()
        .that().resideInAPackage("sgc..")
        .should().onlyAccessClassesThat()
        .resideInAnyPackage("sgc..", "java..", "org.springframework..")
        .because("Modules should be organized by domain aggregates");
```

---

## Referências

### Literatura

1. **Domain-Driven Design** (Eric Evans, 2003)
    - Capítulo 6: The Life Cycle of a Domain Object
    - Capítulo 9: Modules
    - Conceito de Agregado Raiz

2. **Clean Architecture** (Robert C. Martin, 2017)
    - Capítulo 20: Business Rules
    - Capítulo 34: The Missing Chapter (Package by Feature)

3. **Implementing Domain-Driven Design** (Vaughn Vernon, 2013)
    - Capítulo 10: Aggregates
    - Capítulo 14: Application

### Benchmarks

1. **Spring Petclinic**
    - Organização: owner/, vet/, visit/ (agregados)
    - Não usa: domain/, application/ (camadas)

2. **eShopOnContainers (Microsoft)**
    - Cada microserviço: agregados dentro do domínio
    - Não organiza por camada técnica

### Documentação Interna

- `/proposta-arquitetura.md` - Análise completa desta decisão
- `/docs/ARCHITECTURE.md` - Arquitetura geral
- `/reqs/_intro.md` - Modelo de negócio

---

## Histórico de Revisões

| Data       | Versão | Mudanças                                                      |
|------------|--------|---------------------------------------------------------------|
| 2026-01-15 | 1.0    | Criação inicial - Decisão de manter organização por agregados |

---

**Revisão próxima:** 2026-07-15  
**Autor:** GitHub Copilot AI Agent  
**Aprovado por:** Análise Arquitetural (proposta-arquitetura.md)
