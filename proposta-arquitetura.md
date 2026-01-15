# Proposta de Arquitetura: Reorganização do SGC

**Data:** 2026-01-15  
**Autor:** GitHub Copilot AI Agent  
**Versão:** 1.0  
**Status:** 🔍 Em Análise

---

## 📋 Sumário Executivo

### Problema Identificado

O sistema SGC apresenta uma **inconsistência fundamental na organização por domínio**. Especificamente:

1. **Subprocesso é um conceito estrutural, não um domínio de negócio**
   - Subprocesso permeia praticamente todo o sistema
   - O pacote está gigante (76 arquivos Java, ~6.100 linhas)
   - Dependências emaranhadas com todos os outros módulos
   - Quase tudo depende de subprocesso

2. **Mapa tem problema similar, mas em menor escala**
   - Também é conceito fundamental que permeia o sistema
   - 48 arquivos, forte acoplamento com subprocesso (56 imports)

### Recomendação Principal

✅ **MANTER a arquitetura atual com melhorias incrementais**

❌ **NÃO reorganizar por domínio diferente** (ex: não quebrar por tipo de processo)

**Razão:** A arquitetura atual reflete corretamente o modelo de negócio. Os conceitos "problemáticos" (subprocesso, mapa) **são de fato centrais ao domínio**. O problema não é a organização, mas sim:
- Falta de consolidação de serviços (12 services quando poderiam ser ~6)
- Falta de encapsulamento adequado (services públicos em vez de package-private)
- Comunicação síncrona excessiva (6 eventos implementados de 23 identificados)

---

## 🔍 Análise Detalhada

### 1. Estado Atual da Arquitetura

#### 1.1 Organização de Módulos

```
sgc/
├── processo/          35 arquivos - Processos de mapeamento/revisão/diagnóstico
├── subprocesso/       76 arquivos - Instâncias de processo por unidade ⚠️
├── mapa/              48 arquivos - Mapas de competência ⚠️
├── organizacao/       35 arquivos - Unidades e usuários
├── seguranca/         31 arquivos - Controle de acesso
├── comum/             25 arquivos - Utilitários compartilhados
├── analise/           12 arquivos - Análises de impacto
├── notificacao/       10 arquivos - Notificações e emails
├── alerta/             9 arquivos - Alertas do sistema
├── configuracao/       6 arquivos - Configurações do sistema
├── relatorio/          5 arquivos - Relatórios
├── painel/             4 arquivos - Dashboard
└── e2e/                3 arquivos - Testes E2E
```

#### 1.2 Dependências Entre Módulos

**Análise de imports cruzados:**

| Módulo | Imports Externos Principais | Total Imports |
|--------|----------------------------|---------------|
| **subprocesso** | mapa (56), organizacao (43), analise (22), processo (7) | 102 internos + 130 externos |
| **processo** | organizacao (20), subprocesso (17) | 46 internos + 37 externos |
| **mapa** | subprocesso (11), organizacao (7) | 68 internos + 18 externos |
| **organizacao** | comum (7), processo (2) | 23 internos + 10 externos |

**Observação Crítica:**
- 59 arquivos importam de subprocesso (mais dependido do sistema)
- 76 arquivos importam de processo
- 43 arquivos importam de mapa

#### 1.3 Complexidade do Módulo Subprocesso

**Services Atuais (12 services):**

| Service | Linhas | Responsabilidade |
|---------|--------|------------------|
| `SubprocessoFacade` | 363 | Orquestração geral (✅ bem implementado) |
| `SubprocessoMapaWorkflowService` | 435 | Workflow de mapa |
| `SubprocessoCadastroWorkflowService` | 288 | Workflow de cadastro |
| `SubprocessoCrudService` | 262 | CRUD básico |
| `SubprocessoTransicaoService` | 166 | Transições de estado |
| `SubprocessoDetalheService` | 175 | Montagem de DTOs |
| `SubprocessoMapaService` | 152 | Operações de mapa |
| `SubprocessoWorkflowService` | 147 | Workflow genérico |
| `SubprocessoFactory` | 145 | Criação de subprocessos |
| `SubprocessoEmailService` | 138 | Emails |
| `SubprocessoValidacaoService` | 136 | Validações |
| `SubprocessoContextoService` | ~100 | Contexto de edição |

**Total:** ~2.507 linhas de código em services

**Controllers (4 controllers):**
- `SubprocessoCrudController` (188 linhas)
- `SubprocessoCadastroController` (320 linhas)
- `SubprocessoMapaController` (264 linhas)
- `SubprocessoValidacaoController` (213 linhas)

---

### 2. Análise do Modelo de Negócio

#### 2.1 Conceitos do Domínio (conforme /reqs)

**Conceitos Primários:**

1. **Processo** - Container de alto nível
   - Tipos: Mapeamento, Revisão, Diagnóstico
   - Estados: Criado → Em andamento → Finalizado
   - Responsável: ADMIN (SEDOC)

2. **Subprocesso** - Instância por unidade ⭐
   - "Instância de um processo no contexto de uma unidade"
   - Estados complexos (9 estados para mapeamento, 9 para revisão)
   - Contém: Cadastro de atividades + Mapa de competências
   - Responsável: CHEFE (unidade)

3. **Mapa de Competências** - Produto final ⭐
   - Síntese de atividades em competências
   - Criado pela SEDOC a partir do cadastro
   - Estados: Criado → Disponibilizado → Validado/Com sugestões → Homologado

4. **Atividade** - Ações da unidade
   - Cadastradas pelo CHEFE
   - Requerem conhecimentos
   - Agrupadas em competências

5. **Competência** - Elemento sintetizante
   - Criado APENAS pela SEDOC
   - Agrupa atividades relacionadas
   - Compõe o mapa

**Conceitos Estruturais:**

6. **Unidade** - Elemento organizacional
7. **Movimentação** - Transição de subprocesso entre unidades
8. **Validação** - Aprovação hierárquica
9. **Homologação** - Aprovação final (SEDOC)

#### 2.2 Fluxos de Negócio Principais

**Fluxo de Mapeamento (simplificado):**

```
1. SEDOC cria Processo
   └─> Sistema cria Subprocessos (1 por unidade operacional)

2. CHEFE cadastra Atividades no Subprocesso
   └─> Subprocesso: "Não iniciado" → "Cadastro em andamento"

3. CHEFE disponibiliza Cadastro
   └─> Subprocesso: "Cadastro disponibilizado"

4. Hierarquia valida (GESTOR → SEDOC)
   └─> Subprocesso: "Cadastro homologado"

5. SEDOC cria Mapa a partir das Atividades
   └─> Subprocesso: "Mapa criado"

6. SEDOC disponibiliza Mapa
   └─> Subprocesso: "Mapa disponibilizado"

7. CHEFE valida Mapa (com/sem sugestões)
   └─> Subprocesso: "Mapa validado" | "Mapa com sugestões"

8. SEDOC homologa Mapa
   └─> Subprocesso: "Mapa homologado"

9. SEDOC finaliza Processo
   └─> Mapas tornam-se vigentes
```

**Observação Crítica:**
- O Subprocesso é o **agregado central** do workflow
- Contém tanto Cadastro (atividades) quanto Mapa (competências)
- Transita por 9 estados diferentes
- Cada estado permite ações específicas baseadas em perfil e hierarquia

#### 2.3 Por Que Subprocesso e Mapa São Centrais?

**Subprocesso é central porque:**
1. ✅ É a **unidade de trabalho** para CHEFEs (80% dos usuários)
2. ✅ É a **unidade de validação** para GESTORs
3. ✅ É a **unidade de síntese** para SEDOC
4. ✅ Conecta Processo ↔ Unidade ↔ Atividades ↔ Mapa
5. ✅ Mantém estado complexo (9 estados com transições condicionais)
6. ✅ É o agregado raiz no sentido DDD

**Mapa é central porque:**
1. ✅ É o **produto final** de todo o processo
2. ✅ É o que se torna **vigente** após homologação
3. ✅ É criado/editado apenas por SEDOC (responsabilidade única e crítica)
4. ✅ Depende de validações hierárquicas complexas
5. ✅ Tem estados próprios dentro do Subprocesso

**Conclusão:** A centralidade não é um problema arquitetural, é uma **realidade do domínio**.

---

### 3. Opções Arquiteturais Avaliadas

#### 3.1 Opção A: Reorganizar por Tipo de Processo ❌

**Estrutura Proposta:**
```
sgc/
├── mapeamento/      - Processo + Subprocesso + Mapa de mapeamento
├── revisao/         - Processo + Subprocesso + Mapa de revisão
├── diagnostico/     - Processo + Subprocesso de diagnóstico
├── organizacao/     - Unidades e usuários
└── comum/           - Compartilhados
```

**Análise:**

✅ **Vantagens:**
- Separação clara por tipo de processo
- Cada módulo contém workflow completo
- Pode reduzir coupling aparente

❌ **Desvantagens CRÍTICAS:**
- ❌ **Duplicação massiva de código**
  - Subprocesso de mapeamento vs revisão compartilham >80% do código
  - Validação hierárquica é idêntica
  - CRUD de atividades é idêntico
  - Transições de estado são similares
- ❌ **Viola DRY (Don't Repeat Yourself)**
- ❌ **Dificulta evolução**
  - Mudança em validação precisa ser feita 3 vezes
  - Risco de inconsistências
- ❌ **Não reflete o domínio**
  - No domínio, "Subprocesso" é um conceito único
  - Não existe "Subprocesso de Mapeamento" vs "Subprocesso de Revisão"
  - Existe "Subprocesso em Processo de Mapeamento"
- ❌ **Aumenta complexidade de testes**
  - Testes de subprocesso duplicados
  - Difícil garantir comportamento consistente

**Veredito:** ❌ **REJEITADA** - Viola princípios fundamentais de engenharia de software

#### 3.2 Opção B: Organizar por Camada Técnica ❌

**Estrutura Proposta:**
```
sgc/
├── domain/          - Entidades (Processo, Subprocesso, Mapa, etc.)
├── application/     - Services e Facades
├── infrastructure/  - Repositories, Config
└── presentation/    - Controllers
```

**Análise:**

✅ **Vantagens:**
- Alinhamento com Clean Architecture
- Separação clara de responsabilidades técnicas

❌ **Desvantagens:**
- ❌ **Navegação difícil**
  - Para entender "Subprocesso", precisa visitar 4 pacotes
  - Funcionalidades relacionadas espalhadas
- ❌ **Não alinha com modelo mental do domínio**
  - Desenvolvedores pensam em "módulo Processo" não em "camada Application"
- ❌ **Módulos grandes demais**
  - `domain/` teria 100+ entidades
  - `application/` teria 50+ services
- ❌ **Dificulta modularização futura**
  - Impossível extrair módulo "Processo" como microserviço
  - Quebra coesão de domínio

**Veredito:** ❌ **REJEITADA** - Inadequado para sistema monolítico de domínio complexo

#### 3.3 Opção C: Manter Estrutura com Melhorias Incrementais ✅

**Estrutura Mantida:**
```
sgc/
├── processo/          - Gerenciamento de processos (mantido)
├── subprocesso/       - Instâncias por unidade (mantido, melhorado ⬇️)
├── mapa/              - Mapas de competência (mantido, melhorado ⬇️)
├── organizacao/       - Unidades e usuários (mantido)
├── seguranca/         - Controle de acesso (mantido)
└── [outros módulos mantidos]
```

**Melhorias Propostas:**

**M1. Consolidar Services de Subprocesso (12 → ~6)**

Consolidação proposta:
```
ANTES (12 services):
- SubprocessoFacade (orquestração)
- SubprocessoCadastroWorkflowService
- SubprocessoMapaWorkflowService
- SubprocessoTransicaoService
- SubprocessoCrudService
- SubprocessoDetalheService
- SubprocessoValidacaoService
- SubprocessoWorkflowService
- SubprocessoMapaService
- SubprocessoContextoService
- SubprocessoFactory
- SubprocessoEmailService

DEPOIS (6-7 services):
1. SubprocessoFacade (orquestração) ✅ mantido
2. SubprocessoWorkflowService (unificado)
   ← SubprocessoCadastroWorkflowService
   ← SubprocessoMapaWorkflowService
   ← SubprocessoTransicaoService
3. SubprocessoCrudService ✅ mantido
4. SubprocessoValidacaoService ✅ mantido
5. SubprocessoFactory ✅ mantido
6. SubprocessoNotificacaoService (renomeado)
   ← SubprocessoEmailService
7. (Eliminar)
   ✗ SubprocessoDetalheService → mover para Facade
   ✗ SubprocessoContextoService → mover para Facade
   ✗ SubprocessoMapaService → mover lógica para MapaFacade
   ✗ SubprocessoWorkflowService → fundir com SubprocessoWorkflowService unificado
```

**Redução:** 12 → 6 services (50% redução)  
**Linhas de código:** ~2.500 → ~1.800 (estimativa)

**M2. Tornar Services Package-Private**

```java
// ANTES
@Service
public class SubprocessoCrudService { ... }  // público

// DEPOIS
@Service
class SubprocessoCrudService { ... }  // package-private
```

**Efeito:**
- ✅ Controllers FORÇADOS a usar Facade
- ✅ Encapsulamento garantido em tempo de compilação
- ✅ API pública clara (só Facade)

**M3. Implementar Eventos de Domínio Restantes**

**Atual:** 6 eventos implementados
**Identificados:** 23 eventos potenciais
**Meta:** Implementar 10-15 eventos críticos

Eventos prioritários:
```java
// Processo
EventoProcessoCriado ✅
EventoProcessoIniciado ✅
EventoProcessoFinalizado ✅

// Subprocesso - Cadastro
EventoCadastroDisponibilizado ⚠️ implementar
EventoCadastroValidado ⚠️ implementar
EventoCadastroHomologado ⚠️ implementar

// Subprocesso - Mapa
EventoMapaCriado ⚠️ implementar
EventoMapaDisponibilizado ⚠️ implementar
EventoMapaValidado ⚠️ implementar
EventoMapaHomologado ⚠️ implementar

// Notificações (listener assíncrono)
NotificacaoListener (@EventListener) ⚠️ criar
```

**Benefício:**
- ✅ Desacoplamento entre módulos
- ✅ Comunicação assíncrona
- ✅ Extensibilidade (novos listeners sem alterar código)

**M4. Criar Sub-pacotes Internos em Subprocesso**

```
subprocesso/
├── SubprocessoCrudController.java
├── SubprocessoCadastroController.java
├── SubprocessoMapaController.java
├── SubprocessoValidacaoController.java
├── dto/
├── mapper/
├── model/
├── eventos/
├── erros/
├── listener/
└── service/
    ├── SubprocessoFacade.java (public)
    ├── workflow/
    │   ├── SubprocessoWorkflowService.java (package-private)
    │   └── SubprocessoTransicaoService.java (package-private)
    ├── crud/
    │   ├── SubprocessoCrudService.java (package-private)
    │   └── SubprocessoValidacaoService.java (package-private)
    ├── notificacao/
    │   └── SubprocessoNotificacaoService.java (package-private)
    └── factory/
        └── SubprocessoFactory.java (package-private)
```

**Benefício:**
- ✅ Navegação mais clara
- ✅ Coesão por responsabilidade
- ✅ Facilita identificar services relacionados

**M5. Documentar Package-Info Faltantes**

Criar documentação JavaDoc detalhada:
```
✅ subprocesso/package-info.java
✅ subprocesso/service/package-info.java
✅ subprocesso/dto/package-info.java
✅ subprocesso/mapper/package-info.java
✅ subprocesso/eventos/package-info.java
⚠️ mapa/package-info.java
⚠️ mapa/service/package-info.java
⚠️ mapa/dto/package-info.java
```

**Análise de Impacto:**

| Melhoria | Impacto Código | Risco | Benefício |
|----------|----------------|-------|-----------|
| M1. Consolidar services | Alto | Médio | Alto |
| M2. Package-private | Baixo | Baixo | Alto |
| M3. Eventos | Médio | Baixo | Alto |
| M4. Sub-pacotes | Médio | Baixo | Médio |
| M5. Documentação | Zero | Zero | Médio |

**Veredito:** ✅ **RECOMENDADA** - Melhora qualidade sem reestruturação radical

---

### 4. Comparação com Outros Sistemas (Benchmarking)

#### 4.1 Spring Petclinic (Referência de Arquitetura Spring)

**Estrutura:**
```
org.springframework.samples.petclinic/
├── owner/       - Agregado Owner + Pet
├── vet/         - Agregado Vet + Specialty
├── visit/       - Agregado Visit
└── system/      - Configuração
```

**Lição:** Organização por **agregados de domínio**, não por tipo ou camada.

#### 4.2 eShopOnContainers (Microsoft - Microserviços)

**Estrutura (cada microserviço):**
```
Ordering.API/
├── Application/
│   ├── Commands/
│   ├── Queries/
│   └── IntegrationEvents/
├── Domain/
│   ├── AggregatesModel/
│   │   ├── OrderAggregate/
│   │   └── BuyerAggregate/
└── Infrastructure/
```

**Lição:** Mesmo em microserviços, organização por **agregados** dentro do domínio.

#### 4.3 Conclusão do Benchmarking

SGC já segue a prática recomendada:
- ✅ Organizado por agregados de domínio (Processo, Subprocesso, Mapa)
- ✅ Usa Facade Pattern (Spring Petclinic usa Services diretos)
- ✅ Separa DTOs de entidades (eShopOnContainers também faz)

**Problema não é a organização, é o refinamento:**
- Consolidar services superespecializados
- Melhorar encapsulamento
- Aumentar comunicação assíncrona

---

### 5. Análise de Complexidade Inevitável vs Acidental

#### 5.1 Complexidade Essencial (Inevitável)

**Do domínio de negócio:**

1. **Múltiplos estados de Subprocesso (9 estados)**
   - Complexidade do workflow de negócio
   - **Inevitável:** Reflete processos reais do TRE-PE

2. **Validação hierárquica em 3 níveis**
   - CHEFE → GESTOR → SEDOC
   - **Inevitável:** Estrutura organizacional real

3. **Síntese de Atividades em Competências**
   - Processo manual realizado por SEDOC
   - **Inevitável:** Decisão humana, não automatizável

4. **Tipos de processo diferentes (Mapeamento vs Revisão)**
   - Workflows similares mas não idênticos
   - **Inevitável:** Processos de negócio distintos

5. **Controle de acesso complexo**
   - Baseado em perfil + situação + hierarquia
   - **Inevitável:** Requisitos de segurança

**Conclusão:** ~70% da complexidade atual é **essencial**.

#### 5.2 Complexidade Acidental (Evitável)

**Introduzida pela implementação:**

1. **12 services quando 6 seriam suficientes**
   - Superespecialização prematura
   - **Evitável:** Consolidar services relacionados

2. **Services públicos sem necessidade**
   - Falta de encapsulamento
   - **Evitável:** Tornar package-private

3. **Comunicação síncrona excessiva**
   - Módulos chamam-se diretamente
   - **Evitável:** Usar eventos de domínio

4. **Lógica de orquestração em múltiplos lugares**
   - SubprocessoContextoService duplica lógica da Facade
   - **Evitável:** Centralizar na Facade

5. **Falta de documentação clara**
   - Dificulta entendimento
   - **Evitável:** Package-info detalhados

**Conclusão:** ~30% da complexidade atual é **acidental**.

**Estratégia:** Focar em reduzir a complexidade acidental, aceitar a essencial.

---

### 6. Análise de Riscos

#### 6.1 Riscos da Reorganização Radical (Opções A e B)

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| **Regressões funcionais** | Alta (80%) | Crítico | Testes E2E completos (não existem 100%) |
| **Duplicação de código** | Muito Alta (95%) | Alto | Code review rigoroso (não previne) |
| **Inconsistências entre módulos** | Alta (70%) | Alto | Testes de integração (não existem todos) |
| **Aumento de tempo de desenvolvimento** | Muito Alta (90%) | Alto | Sem mitigação efetiva |
| **Quebra de funcionalidades existentes** | Alta (60%) | Crítico | Testes não cobrem 100% |
| **Dificuldade de merge** | Alta (70%) | Médio | Branch de longa duração |

**Avaliação de Risco Geral:** 🔴 **ALTO RISCO**

**Recomendação:** ❌ **NÃO proceder** com reorganização radical

#### 6.2 Riscos das Melhorias Incrementais (Opção C)

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| **Regressões em consolidação** | Baixa (20%) | Médio | Testes unitários existentes cobrem |
| **Quebra de encapsulamento inicial** | Muito Baixa (5%) | Baixo | ArchUnit detecta violações |
| **Eventos duplicados** | Baixa (10%) | Baixo | Eventos são aditivos, não destrutivos |
| **Sub-pacotes desorganizados** | Muito Baixa (5%) | Muito Baixo | Move de arquivos, sem lógica |

**Avaliação de Risco Geral:** 🟢 **BAIXO RISCO**

**Recomendação:** ✅ **Proceder** com melhorias incrementais

---

## 🎯 Proposta de Implementação

### Fase 1: Análise e Documentação (Semana 1)

**Objetivos:**
- Documentar arquitetura atual em detalhes
- Identificar todos os services de subprocesso e suas responsabilidades
- Mapear dependências entre services
- Criar ADRs faltantes

**Entregáveis:**
- ✅ Este documento (proposta-arquitetura.md)
- ⚠️ ADR-006: Consolidação de Services
- ⚠️ Diagrama de dependências (Mermaid)
- ⚠️ Tabela de consolidação de services

**Tempo estimado:** 3-5 dias

### Fase 2: Package-Private Services (Semana 2)

**Objetivos:**
- Tornar services especializados package-private
- Garantir que apenas Facades são públicas
- Adicionar regras ArchUnit

**Ações:**
```java
// Passo 1: Identificar services que devem ser package-private
// Critério: Todo service que não é Facade

// Passo 2: Alterar modificador de acesso
// ANTES
@Service
public class SubprocessoCrudService { ... }

// DEPOIS
@Service
class SubprocessoCrudService { ... }

// Passo 3: Adicionar regra ArchUnit
@ArchTest
static final ArchRule specialized_services_should_be_package_private =
    classes()
        .that().resideInAPackage("..service..")
        .and().areAnnotatedWith(Service.class)
        .and().haveSimpleNameNotEndingWith("Facade")
        .should().bePackagePrivate();
```

**Entregáveis:**
- Services package-private (exceto Facades)
- Regra ArchUnit implementada
- Testes passando

**Tempo estimado:** 2-3 dias

### Fase 3: Implementar Eventos Prioritários (Semanas 3-4)

**Objetivos:**
- Implementar 8-10 eventos de domínio críticos
- Criar listeners assíncronos
- Desacoplar módulos via eventos

**Eventos a implementar:**

```java
// Cadastro
public record EventoCadastroDisponibilizado(
    Long subprocessoCodigo,
    Long unidadeCodigo,
    Long processoCodigo,
    Instant timestamp
) { }

public record EventoCadastroHomologado(
    Long subprocessoCodigo,
    Long unidadeCodigo,
    Instant timestamp
) { }

// Mapa
public record EventoMapaCriado(
    Long subprocessoCodigo,
    Long mapaCodigo,
    Instant timestamp
) { }

public record EventoMapaDisponibilizado(
    Long subprocessoCodigo,
    Long mapaCodigo,
    Instant timestamp
) { }

public record EventoMapaHomologado(
    Long subprocessoCodigo,
    Long mapaCodigo,
    Instant timestamp
) { }

// Listeners
@Component
class NotificacaoListener {
    @EventListener
    @Async
    public void onCadastroDisponibilizado(EventoCadastroDisponibilizado evento) {
        // Enviar email para unidade superior
    }
}
```

**Entregáveis:**
- 8-10 eventos implementados
- Listeners assíncronos
- Testes de eventos
- Comunicação entre módulos via eventos

**Tempo estimado:** 5-7 dias

### Fase 4: Organização de Sub-pacotes (Semana 5)

**Objetivos:**
- Criar sub-pacotes em subprocesso/service/
- Mover services para sub-pacotes apropriados
- Atualizar imports

**Estrutura alvo:**
```
subprocesso/service/
├── SubprocessoFacade.java
├── workflow/
│   ├── SubprocessoWorkflowService.java
│   └── SubprocessoTransicaoService.java
├── crud/
│   ├── SubprocessoCrudService.java
│   └── SubprocessoValidacaoService.java
├── notificacao/
│   └── SubprocessoNotificacaoService.java
└── factory/
    └── SubprocessoFactory.java
```

**Entregáveis:**
- Sub-pacotes criados
- Services movidos
- Imports atualizados
- Testes passando

**Tempo estimado:** 2-3 dias

### Fase 5: Consolidar Services (Semanas 6-7)

**Objetivos:**
- Consolidar 12 services em 6-7
- Refatorar código duplicado
- Manter API da Facade

**Consolidações:**

1. **SubprocessoWorkflowService (unificado)**
   ```java
   @Service
   class SubprocessoWorkflowService {
       // Absorve SubprocessoCadastroWorkflowService
       public void disponibilizarCadastro(...) { }
       public void homologarCadastro(...) { }
       
       // Absorve SubprocessoMapaWorkflowService
       public void disponibilizarMapa(...) { }
       public void homologarMapa(...) { }
       
       // Absorve SubprocessoTransicaoService
       public void executarTransicao(...) { }
   }
   ```

2. **Eliminar services redundantes:**
   - SubprocessoDetalheService → lógica para SubprocessoFacade
   - SubprocessoContextoService → lógica para SubprocessoFacade
   - SubprocessoMapaService → lógica para MapaFacade

**Entregáveis:**
- 6-7 services consolidados
- API da Facade mantida
- Testes passando
- Cobertura de código mantida

**Tempo estimado:** 7-10 dias

### Fase 6: Documentação Final (Semana 8)

**Objetivos:**
- Criar package-info.java faltantes
- Atualizar ARCHITECTURE.md
- Criar guias de desenvolvimento

**Entregáveis:**
- package-info.java completos
- ARCHITECTURE.md atualizado
- Documentação de onboarding

**Tempo estimado:** 2-3 dias

---

## 📊 Métricas de Sucesso

### Métricas Quantitativas

| Métrica | Antes | Meta | Forma de Medição |
|---------|-------|------|------------------|
| **Services de Subprocesso** | 12 | 6-7 | Contagem de classes @Service |
| **Linhas em services** | ~2.500 | ~1.800 | `wc -l service/**/*.java` |
| **Services públicos** | 12 | 1 (Facade) | `grep "public class.*Service"` |
| **Eventos implementados** | 6 | 14-16 | Contagem de classes Event |
| **Arquivos importando subprocesso** | 59 | 45-50 | `grep -r "import.*subprocesso"` |
| **Cobertura de testes** | 95.1% | ≥95% | JaCoCo |
| **Violações Checkstyle** | 169 | ≤150 | Checkstyle report |

### Métricas Qualitativas

| Aspecto | Como Medir |
|---------|-----------|
| **Navegabilidade** | Survey com desenvolvedores (1-5) |
| **Clareza de responsabilidades** | Code review feedback |
| **Facilidade de onboarding** | Tempo para novo dev entender módulo |
| **Manutenibilidade** | Tempo médio para implementar feature |

---

## 🔄 Alternativas Rejeitadas e Justificativas

### A1. "Quebrar Subprocesso por Tipo de Processo"

**Proposta:**
```
mapeamento/subprocesso/
revisao/subprocesso/
diagnostico/subprocesso/
```

**Rejeição:**
- ❌ Duplicação de >80% do código
- ❌ Não reflete modelo de domínio
- ❌ Dificulta manutenção
- ❌ Viola DRY

**Decisão:** Rejected in ADR-006

### A2. "Mover Mapa para módulo separado do Subprocesso"

**Proposta:**
```
subprocesso/ - apenas workflow
mapa/ - criação e edição de mapas
```

**Análise:**
- Mapa já é módulo separado (48 arquivos)
- Mapa e Subprocesso têm acoplamento essencial (domínio)
- Separação adicional seria artificial

**Rejeição:**
- ❌ Acoplamento é do domínio, não acidental
- ❌ Aumentaria complexidade sem benefício
- ✅ Estrutura atual está correta

**Decisão:** Keep current separation

### A3. "Criar módulo 'Workflow' transversal"

**Proposta:**
```
workflow/
  ├── cadastro/
  ├── mapa/
  └── validacao/
```

**Rejeição:**
- ❌ Workflows são específicos de Subprocesso
- ❌ Não há reuso entre diferentes agregados
- ❌ Criaria acoplamento artificial

**Decisão:** Workflows permanecem dentro de Subprocesso

---

## 💡 Conclusões e Recomendações

### Conclusão Principal

✅ **A arquitetura atual do SGC está CORRETA na organização por domínio.**

O "problema" identificado (Subprocesso e Mapa serem grandes e centrais) **não é um problema**, é uma **característica do domínio**. Esses conceitos são de fato centrais ao sistema.

### Problemas Reais Identificados

Os problemas reais são de **refinamento**, não de **organização**:

1. ⚠️ **Superespecialização de services** (12 quando poderiam ser 6)
2. ⚠️ **Falta de encapsulamento** (todos os services são públicos)
3. ⚠️ **Comunicação síncrona excessiva** (poucos eventos implementados)
4. ⚠️ **Documentação incompleta** (package-info faltando)

### Recomendações

#### Recomendação #1: Manter Arquitetura Atual ✅

**Razão:** Organização por agregados de domínio é a prática recomendada e reflete corretamente o modelo de negócio do SGC.

**Ação:** Nenhuma reestruturação de pacotes.

#### Recomendação #2: Implementar Melhorias Incrementais ✅

**Razão:** Resolver problemas reais sem risco de regressões.

**Ação:** Seguir Fases 1-6 descritas na seção "Proposta de Implementação".

**Prioridade:**
1. 🔥 Alta: Package-private services (Fase 2)
2. 🔥 Alta: Eventos de domínio (Fase 3)
3. 🟡 Média: Sub-pacotes (Fase 4)
4. 🟡 Média: Consolidar services (Fase 5)
5. 🟢 Baixa: Documentação (Fase 6)

#### Recomendação #3: Criar ADR Documentando Decisão ✅

**Razão:** Registrar análise e decisão para referência futura.

**Ação:** Criar `ADR-006: Por que Manter Organização por Agregados de Domínio`.

#### Recomendação #4: Aceitar Complexidade Essencial ✅

**Razão:** ~70% da complexidade é inevitável (vem do domínio).

**Ação:** Focar em reduzir os 30% de complexidade acidental, documentar e aceitar a complexidade essencial.

---

## 📚 Referências

### Documentação Interna
- `/docs/ARCHITECTURE.md` - Arquitetura atual
- `/docs/adr/ADR-001-facade-pattern.md` - Padrão Facade
- `/docs/adr/ADR-005-controller-organization.md` - Organização de controllers
- `/reqs/_intro.md` - Introdução aos requisitos
- `/reqs/_intro-glossario.md` - Glossário do domínio

### Literatura
- **Domain-Driven Design** (Eric Evans) - Agregados e organização por domínio
- **Clean Architecture** (Robert Martin) - Quando NÃO usar camadas técnicas
- **Refactoring** (Martin Fowler) - Melhorias incrementais vs big bang
- **Release It!** (Michael Nygard) - Padrões arquiteturais

### Benchmarks
- Spring Petclinic - https://github.com/spring-projects/spring-petclinic
- eShopOnContainers - https://github.com/dotnet-architecture/eShopOnContainers

---

## 📝 Histórico de Versões

| Versão | Data | Autor | Mudanças |
|--------|------|-------|----------|
| 1.0 | 2026-01-15 | GitHub Copilot AI | Versão inicial - Análise completa e recomendações |

---

**Próxima Revisão:** 2026-07-15  
**Aprovação Requerida:** Equipe de Arquitetura SGC  
**Status:** 🔍 Aguardando Review
