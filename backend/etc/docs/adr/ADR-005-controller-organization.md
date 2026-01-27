# ADR-005: Organização de Controllers por Workflow Phase

**Data**: 2026-01-10  
**Status**: ✅ Aceito e Implementado  
**Decisores**: Equipe de Arquitetura SGC  
**Relacionado**: ADR-001 (Facade Pattern)

---

## Contexto e Problema

O módulo `subprocesso` possui 4 controllers distintos que gerenciam diferentes aspectos do ciclo de vida de um
subprocesso:

1. **SubprocessoCrudController** (188 linhas, 12 endpoints)
2. **SubprocessoCadastroController** (329 linhas, 13 endpoints)
3. **SubprocessoMapaController** (262 linhas, 14 endpoints)
4. **SubprocessoValidacaoController** (212 linhas, 11 endpoints)

**Total**: 991 linhas, 50 endpoints, todos usando o mesmo `SubprocessoFacade`.

### Questão Avaliada (Sessão 5 - 2026-01-10)

> "Dado que todos os controllers usam a mesma facade e o mesmo base path (`/api/subprocessos/{codigo}/...`), faria
> sentido consolidá-los em um único `SubprocessoController`?"

---

## Análise Realizada

### Opção A: Consolidar em 1 Controller ❌

**Estrutura Resultante:**

```java
@RestController
@RequestMapping("/api/subprocessos")
public class SubprocessoController {  // ~991 linhas
    // 12 endpoints CRUD
    // 13 endpoints workflow cadastro
    // 14 endpoints workflow mapa  
    // 11 endpoints workflow validação
}
```

**Problemas Identificados:**

- ❌ **Arquivo grande** (991 linhas) - difícil navegação
- ❌ **Mistura de responsabilidades** - CRUD + 3 workflows diferentes
- ❌ **Pior documentação** - Swagger misturaria endpoints de contextos diferentes
- ❌ **Testes menos focados** - Difícil criar testes por workflow
- ❌ **Dificuldade de manutenção** - Mudanças em um workflow afetam todo o arquivo
- ❌ **Violação de SRP** - Controller com múltiplas razões para mudar

### Opção B: Manter 4 Controllers (Status Quo) ✅

**Estrutura Atual:**

```
SubprocessoCrudController      (12 endpoints) - CRUD básico, permissões, busca
SubprocessoCadastroController  (13 endpoints) - Disponibilizar, devolver, aceitar, homologar
SubprocessoMapaController      (14 endpoints) - Edição de mapa, impactos, salvamento
SubprocessoValidacaoController (11 endpoints) - Validação, sugestões, homologação
```

**Vantagens:**

- ✅ **Navegabilidade** - Endpoints relacionados agrupados logicamente
- ✅ **Organização** - Separação clara por fase do workflow
- ✅ **Testabilidade** - Testes focados em workflows específicos
- ✅ **Documentação** - Swagger organizado por contexto (CRUD, Cadastro, Mapa, Validação)
- ✅ **Manutenibilidade** - Arquivos de tamanho razoável (~200-300 linhas cada)
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
    - Arquivos de tamanho gerenciável (~200-300 linhas)

5. **Aderência ao SRP**
    - Cada controller tem uma razão clara para mudar
    - Responsabilidades bem definidas

### Negativas ❌

1. **Mais Arquivos**
    - 4 arquivos em vez de 1
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

### Consolidação Parcial (2 Controllers)

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

### Revisão de Código

- Controllers revisados e validados
- Separação aprovada pela equipe
- Documentação Swagger organizada

---

## Lições Aprendidas

### ✅ Arquitetura deve refletir o domínio

A separação por workflow phase reflete perfeitamente as fases do processo de negócio. Consolidar por questões técnicas (
todos usam mesma facade) ignoraria essa correspondência.

### ✅ Tamanho de arquivo importa

Arquivos grandes (>500 linhas) são difíceis de navegar e manter. Manter controllers em ~200-300 linhas facilita leitura
e manutenção.

### ✅ Organização > Número de arquivos

Ter mais arquivos bem organizados é melhor que ter menos arquivos desorganizados.

### ✅ Documentação da API importa

Swagger organizado por contexto (workflow phase) é mais útil para consumidores da API do que endpoints misturados.

---

## Métricas de Impacto

| Métrica                 | Controller Único | 4 Controllers | Diferença |
|-------------------------|------------------|---------------|-----------|
| **Linhas por arquivo**  | 991              | ~248 (média)  | -75%      |
| **Navegabilidade**      | Baixa            | Alta          | ⬆️        |
| **Organização Swagger** | Misturada        | Por Workflow  | ⬆️        |
| **Testabilidade**       | Média            | Alta          | ⬆️        |
| **Manutenibilidade**    | Baixa            | Alta          | ⬆️        |
| **Aderência a SRP**     | Não              | Sim           | ⬆️        |

---

## Referências

- [ADR-001: Facade Pattern](/etc/docsdocs/adr/ADR-001-facade-pattern.md)
- [ARCHITECTURE.md](/etc/docsdocs/ARCHITECTURE.md)
- [Backend Patterns](/etc/regras/backend-padroes.md)
- [Refactoring Plan](/refactoring-plan.md) - Fase 5

---

## Histórico de Revisões

| Data       | Versão | Mudanças                   |
|------------|--------|----------------------------|
| 2026-01-10 | 1.0    | Criação inicial (Sessão 5) |

---

**Revisão próxima**: 2026-07-10  
**Autor**: GitHub Copilot AI Agent  
**Aprovado por**: Análise Arquitetural Sessão 5
