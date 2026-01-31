# ADR-005: Organização de Controllers por Workflow Phase

---

## Contexto e Problema

O módulo `subprocesso` possui 4 controllers distintos que gerenciam diferentes aspectos do ciclo de vida de um subprocesso:

1. **SubprocessoCrudController** - CRUD básico, permissões, busca
2. **SubprocessoCadastroController** - Disponibilizar, devolver, aceitar, homologar
3. **SubprocessoMapaController** - Edição de mapa, impactos, salvamento
4. **SubprocessoValidacaoController** - Validação, sugestões, homologação

Todos usam o mesmo `SubprocessoFacade`.

### Questão Avaliada

> "Dado que todos os controllers usam a mesma facade e o mesmo base path (`/api/subprocessos/{codigo}/...`), faria
> sentido consolidá-los em um único `SubprocessoController`?"

---

## Análise Realizada

### Opção A: Consolidar em 1 Controller ❌

**Estrutura Resultante:**

```java
@RestController
@RequestMapping("/api/subprocessos")
public class SubprocessoController {
    // Todos endpoints CRUD, cadastro, mapa e validação em um único arquivo
}
```

**Problemas Identificados:**

- ❌ **Arquivo muito grande** - difícil navegação
- ❌ **Mistura de responsabilidades** - CRUD + 3 workflows diferentes
- ❌ **Pior documentação** - Swagger misturaria endpoints de contextos diferentes
- ❌ **Testes menos focados** - Difícil criar testes por workflow
- ❌ **Dificuldade de manutenção** - Mudanças em um workflow afetam todo o arquivo
- ❌ **Violação de SRP** - Controller com múltiplas razões para mudar

### Opção B: Manter 4 Controllers (Status Quo) ✅

**Estrutura Atual:**

```
SubprocessoCrudController      - CRUD básico, permissões, busca
SubprocessoCadastroController  - Disponibilizar, devolver, aceitar, homologar
SubprocessoMapaController      - Edição de mapa, impactos, salvamento
SubprocessoValidacaoController - Validação, sugestões, homologação
```

**Vantagens:**

- ✅ **Navegabilidade** - Endpoints relacionados agrupados logicamente
- ✅ **Organização** - Separação clara por fase do workflow
- ✅ **Testabilidade** - Testes focados em workflows específicos
- ✅ **Documentação** - Swagger organizado por contexto (CRUD, Cadastro, Mapa, Validação)
- ✅ **Manutenibilidade** - Arquivos de tamanho razoável
- ✅ **Coesão** - Cada controller tem responsabilidade bem definida
- ✅ **Aderência a SRP** - Cada controller muda por uma razão específica

---

## Decisão

✅ **MANTER 4 controllers separados por workflow phase.**

A separação atual reflete perfeitamente as diferentes fases do ciclo de vida de um subprocesso:

1. **CRUD** - Operações básicas de criação, leitura, atualização, exclusão
2. **Cadastro** - Workflow de cadastro de atividades (CDU-08, CDU-12)
3. **Mapa** - Workflow de edição de mapa de competências (CDU-10, CDU-12, CDU-16)
4. **Validação** - Workflow de validação pelas unidades (CDU-09, CDU-10)

Esta organização:

- **Alinha com o domínio** - Cada controller mapeia uma fase do processo de negócio
- **Facilita onboarding** - Novos desenvolvedores entendem rapidamente a organização
- **Suporta evolução** - Fases podem evoluir independentemente
- **Melhora documentação** - Swagger fica organizado por contexto de uso

---

## Consequências

### Positivas ✅

1. **Navegabilidade Superior**
    - Desenvolvedores encontram endpoints rapidamente
    - Nome do controller indica claramente o contexto

2. **Documentação Swagger Organizada**
    - Endpoints agrupados por fase de workflow
    - Facilita uso por frontend/consumidores da API

3. **Testes Mais Focados**
    - Testes de CRUD separados de testes de workflow
    - Menor acoplamento entre testes

4. **Manutenibilidade**
    - Mudanças em workflow de cadastro não afetam outros controllers
    - Arquivos de tamanho gerenciável

5. **Aderência ao SRP**
    - Cada controller tem uma razão clara para mudar
    - Responsabilidades bem definidas

### Negativas ❌

1. **Mais Arquivos**
    - Múltiplos arquivos em vez de um único
    - *Mitigação*: Organização compensa o número de arquivos

2. **Possível Duplicação de Código**
    - Validações ou lógica comum podem se repetir
    - *Mitigação*: Facade centraliza lógica; controllers são thin

---

## Princípios Aplicados

### Single Responsibility Principle (SRP)

Cada controller tem UMA responsabilidade clara:

- CRUD: Gerenciar operações básicas
- Cadastro: Orquestrar workflow de cadastro
- Mapa: Orquestrar workflow de mapa
- Validação: Orquestrar workflow de validação

### Interface Segregation Principle (ISP)

Consumidores da API (frontend) usam apenas os endpoints necessários para o contexto:

- Tela de edição de cadastro → SubprocessoCadastroController
- Tela de edição de mapa → SubprocessoMapaController
- Tela de validação → SubprocessoValidacaoController

### Open/Closed Principle (OCP)

Controllers podem evoluir independentemente sem afetar os demais.

---

## Alternativas Consideradas

### Consolidação Parcial

```
SubprocessoController        (CRUD básico)
SubprocessoWorkflowController (Cadastro + Mapa + Validação)
```

**Razão para Rejeitar:**

- Ainda misturaria 3 workflows diferentes em um controller
- Perderia benefícios de separação por fase
- Pior que status quo

### Consolidação por Tipo de Operação

```
SubprocessoQueryController   (GET endpoints)
SubprocessoCommandController (POST endpoints)
```

**Razão para Rejeitar:**

- Separação artificial (CQRS não aplicado no projeto)
- Não alinha com domínio de negócio
- Dificulta navegação (GET de diferentes contextos misturados)

---

## Conformidade

### Testes Arquiteturais (ArchUnit)

Controllers seguem padrões:

- ✅ Nomeação: `*Controller`
- ✅ Uso de Facades: Apenas `SubprocessoFacade`
- ✅ Localização: `sgc.subprocesso` package
- ✅ Anotações: `@RestController`, `@RequestMapping`


---

## Referências

- [ADR-001: Facade Pattern](/etc/docs/adr/ADR-001-facade-pattern.md)
- [ARCHITECTURE.md](/etc/docsdocs/ARCHITECTURE.md)
- [Backend Patterns](/etc/docs/backend-padroes.md)
- [Refactoring Plan](/refactoring-plan.md) - Fase 5
