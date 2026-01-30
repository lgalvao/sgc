# Plano de Eliminação da Palavra-Chave `new` no SGC (Fase 1: Produção)

Este documento foca na remoção do `new` em classes de produção (Entidades, DTOs e Records), onde o acoplamento é mais prejudicial à manutenção.

## ✅ Status da Análise (Atualizado em 29/01/2026)

**Entidades JPA:** Todas já possuem `@SuperBuilder` (Lombok) configurado corretamente:
- ✅ `Atividade` - linha 25 (`@SuperBuilder`)
- ✅ `Competencia` - linha 19 (`@SuperBuilder`)
- ✅ `Conhecimento` - linha 15 (`@SuperBuilder`)
- ✅ `Mapa` - linha 19 (`@SuperBuilder`)

**DTOs e Records:** Maioria já usa `@Builder` ou são `record`:
- ✅ `CompetenciaImpactadaDto` - linha 16 (`@Builder` + `record`)
- ✅ `EventoMapaAlterado` - linha 7 (`record` simples)
- ⚠️ `ContextoSalvamento` - classe interna em `MapaSalvamentoService` (linha 220) - **PODE SER CONVERTIDA PARA RECORD**

**Contagem de Ocorrências em Código de Produção:**
- `new Atividade()`: **2 ocorrências** (CopiaMapaService, AtividadeFacade)
- `new Competencia()`: **2 ocorrências** (MapaSalvamentoService, CopiaMapaService)
- `new Conhecimento()`: **1 ocorrência** (CopiaMapaService)
- `new Mapa()`: **4 ocorrências** (SubprocessoCrudService x2, SubprocessoFactory, CopiaMapaService)

## 1. Prioridade Inicial: Classes de Produção

O foco está em substituir a instanciação manual por padrões que promovam imutabilidade e fluidez.

### 1.1. Backend (Java/Kotlin)
- **Entidades JPA:** `Atividade`, `Competencia`, `Conhecimento`, `Mapa`.
- **DTOs e Records:** `CompetenciaImpactadaDto`, `ContextoSalvamento`.
- **Eventos de Domínio:** `EventoMapaAlterado`.

**Estratégia:**
1. Adicionar `@Builder` (Lombok) em todas as classes acima.
2. Substituir `new Class()` por `Class.builder()...build()` nos Services.
3. Para objetos muito simples, avaliar o uso de `Static Factory Methods` (ex: `Atividade.vazia()`).

### 1.2. Frontend (TypeScript)
- **Interfaces vs Classes:** Substituir classes de modelo por `interfaces` ou `types`.
- **Objetos Literais:** Usar a sintaxe de objeto literal `{...}` para inicialização.

**Estratégia:**
1. Varredura para converter classes que servem apenas como "containers de dados" em interfaces.
2. Usar `spread operator` para "clonagem" em vez de instanciar novas classes.

## 2. Exemplos de Refatoração

### 2.1. Backend: De Construtor para Builder
**Antes:**
```java
Atividade nova = new Atividade();
nova.setDescricao(desc);
nova.setMapa(mapa);
```
**Depois:**
```java
Atividade nova = Atividade.builder()
    .descricao(desc)
    .mapa(mapa)
    .build();
```

### 2.2. Frontend: De Classe para Interface/Literal
**Antes:**
```typescript
const dto = new UserDto();
dto.name = "Leo";
```
**Depois:**
```typescript
const dto: UserDto = {
    name: "Leo"
};
```

## 3. Arquivos Identificados para Refatoração

### 3.1. CopiaMapaService (ALTA DENSIDADE - 4 ocorrências)
**Localização:** `backend/src/main/java/sgc/mapa/service/CopiaMapaService.java`

**Ocorrências:**
1. **Linha 74:** `new Mapa()` - método `criarNovoMapa()`
   - Usa setters encadeados
   - **Substituir por:** `Mapa.builder()...build()`

2. **Linha 108:** `new Atividade()` - método `prepararCopiaAtividade()`
   - Usa setters
   - **Substituir por:** `Atividade.builder()...build()`

3. **Linha 115:** `new Conhecimento()` - dentro de loop
   - Usa setters encadeados
   - **Substituir por:** `Conhecimento.builder()...build()`

4. **Linha 133:** `new Competencia()` - método `copiarCompetencias()`
   - Usa setters
   - **Substituir por:** `Competencia.builder()...build()`

### 3.2. MapaSalvamentoService (2 ocorrências)
**Localização:** `backend/src/main/java/sgc/mapa/service/MapaSalvamentoService.java`

**Ocorrências:**
1. **Linha 141:** `new Competencia()` - método `processarCompetenciaDto()`
   - Criação condicional (quando `codigo == null`)
   - **Substituir por:** `Competencia.builder()...build()`

2. **Linha 220-240:** `class ContextoSalvamento` - classe interna privada
   - Atualmente usa construtor explícito
   - Tem `@SuppressWarnings("ClassCanBeRecord")`
   - **Substituir por:** `record ContextoSalvamento(...)`

### 3.3. AtividadeFacade (1 ocorrência)
**Localização:** `backend/src/main/java/sgc/mapa/service/AtividadeFacade.java`

**Ocorrências:**
1. **Linha 95-96:** `new Atividade()` - método `criarAtividade()`
   - Cria objeto temporário apenas para verificação de acesso
   - **Substituir por:** `Atividade.builder()...build()`

### 3.4. SubprocessoCrudService (2 ocorrências)
**Localização:** `backend/src/main/java/sgc/subprocesso/service/crud/SubprocessoCrudService.java`

**Ocorrências:**
1. Criação de `Mapa` para novo subprocesso
   - **Substituir por:** `Mapa.builder()...build()`

2. Atualização com referência de `Mapa` em `processarAlteracoes()`
   - **Substituir por:** `Mapa.builder()...build()`

### 3.5. SubprocessoFactory (1 ocorrência)
**Localização:** `backend/src/main/java/sgc/subprocesso/service/factory/SubprocessoFactory.java`

**Ocorrências:**
1. Criação de `Mapa` em stream
   - **Substituir por:** `Mapa.builder()...build()`

### 3.6. ImpactoMapaService (✅ SEM OCORRÊNCIAS)
**Localização:** `backend/src/main/java/sgc/mapa/service/ImpactoMapaService.java`

**Análise:** Este serviço já utiliza builders corretamente:
- Linha 131-137: `AtividadeImpactadaDto.builder()...build()`
- Linha 159-165: `AtividadeImpactadaDto.builder()...build()`
- Linha 193-200: `AtividadeImpactadaDto.builder()...build()`
- Linha 356-358: Usa construtor de classe interna `CompetenciaImpactoAcumulador`, mas não é problema pois é classe auxiliar privada

**Status:** ✅ Nenhuma refatoração necessária

## 4. Próximos Passos (Priorizado)

### Fase 1: Backend - Refatoração de Código de Produção ✅ CONCLUÍDO
1. ✅ **Análise Completa** - Confirmar achados e adicionar contexto
2. ✅ **CopiaMapaService** - Refatoradas 4 ocorrências (CONCLUÍDO)
3. ✅ **MapaSalvamentoService** - Refatoradas 2 ocorrências incluindo conversão de ContextoSalvamento para record (CONCLUÍDO)
4. ✅ **AtividadeFacade** - Refatorada 1 ocorrência (CONCLUÍDO)
5. ✅ **SubprocessoCrudService** - Refatoradas 2 ocorrências (CONCLUÍDO)
6. ✅ **SubprocessoFactory** - Refatorada 1 ocorrência (CONCLUÍDO)
7. ✅ **Executar Testes** - Todos os 1414 testes do backend passaram com sucesso (CONCLUÍDO)

**RESULTADO FINAL:**
- ✅ **0 ocorrências** de `new Atividade()` em código de produção
- ✅ **0 ocorrências** de `new Competencia()` em código de produção
- ✅ **0 ocorrências** de `new Conhecimento()` em código de produção
- ✅ **0 ocorrências** de `new Mapa()` em código de produção
- ✅ Todos os testes passaram (1414 testes, 100% de sucesso)
- ✅ Compilação sem erros ou warnings

### Fase 2: Frontend ✅ CONCLUÍDO
- ✅ Padronizar DTOs TypeScript como interfaces
- ✅ Substituir classes de modelo por interfaces/types quando apropriado

**RESULTADO FINAL:**
- ✅ **0 classes customizadas** encontradas no código TypeScript
- ✅ Frontend já utiliza **interfaces e types** exclusivamente
- ✅ Único uso de `new` é com `Modal` do Bootstrap (biblioteca externa)
- ✅ Padrão de objeto literal `{...}` já é consistentemente utilizado

## 5. Lições Aprendidas

1. **Type Hints em Streams:** Quando usando builders com `@SuperBuilder` em streams, pode ser necessário adicionar type hints explícitos (ex: `.<Mapa>map(...)`) para ajudar o compilador Java.

2. **Records vs Classes:** A conversão de classes internas privadas para `records` elimina boilerplate significativo e melhora a clareza do código.

3. **Builders Fluentes:** O uso consistente de builders torna o código mais legível e menos propenso a erros de configuração de objetos.

4. **Testes como Garantia:** A execução completa da suite de testes após refatorações garante que o comportamento foi preservado.