# Plano de Eliminação da Palavra-Chave `new` no SGC (Fase 1: Produção)

Este documento foca na remoção do `new` em classes de produção (Entidades, DTOs e Records), onde o acoplamento é mais prejudicial à manutenção.

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

## 3. Próximos Passos
1. **Refatorar `CopiaMapaService`:** É o arquivo com maior densidade de `new` para entidades de produção.
2. **Refatorar `ImpactoMapaService`:** Foco em DTOs de retorno.
3. **Padronizar DTOs do Frontend:** Garantir que sejam interfaces.