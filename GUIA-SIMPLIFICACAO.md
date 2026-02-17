# 📘 Guia de Simplificação SGC

**Versão:** 2.0  
**Data:** 17 de Fevereiro de 2026  
**Público-Alvo:** Desenvolvedores do SGC

Este guia define os novos padrões arquiteturais do SGC após o processo de simplificação. Siga estas diretrizes ao criar novo código ou refatorar o existente.

---

## 🎯 Princípios Fundamentais

1.  **Sem Indireção Desnecessária:** Se um serviço ou facade apenas delega chamadas, elimine-o.
2.  **Dados Brutos no Backend:** O Backend envia dados (ISO dates, Enums); o Frontend formata.
3.  **Lógica na View:** Lógica específica de uma tela fica no componente `.vue`. Composables são apenas para lógica realmente reutilizável.
4.  **@JsonView para Leitura:** Use `@JsonView` para evitar a criação de DTOs de resposta que apenas repetem a estrutura da entidade.

---

## 🔧 Backend: Novo Padrão de DTOs e Mappers

### 1. Fim dos campos *Formatada e *Label

**Proibido** adicionar campos de formatação no backend.

#### ❌ Incorreto (Boilerplate)
```java
public record AlertaDto(
    LocalDateTime dataHora,
    String dataHoraFormatada, // ❌ Remova
    String situacaoLabel         // ❌ Remova
) {}
```

#### ✅ Correto (Dados Brutos)
```java
public record AlertaDto(
    LocalDateTime dataHora,
    SituacaoAlerta situacao
) {}
```

### 2. Uso de @JsonView
Para respostas simples, anote a entidade e use `@JsonView` no Controller em vez de criar um DTO.

```java
@JsonView(MapaViews.Publica.class)
@GetMapping("/{id}")
public Atividade buscar(@PathVariable Long id) {
    return service.buscar(id);
}
```

---

## 🎨 Frontend: Formatação e Tipos

### 1. Utilização de Formatadores
Sempre utilize os utilitários de `src/utils/formatters.ts` nos templates Vue.

```html
<!-- Exemplo de uso -->
<template>
  <span>{{ formatDate(alerta.dataHora) }}</span>
  <span>{{ formatSituacaoProcesso(processo.situacao) }}</span>
</template>

<script setup>
import { formatDate, formatSituacaoProcesso } from '@/utils/formatters';
</script>
```

### 2. Unificação de Interfaces
Não duplique interfaces entre `dtos.ts` e `tipos.ts`. Use interfaces únicas que representem o modelo de domínio.

---

## 📐 Padrões Arquiteturais (ADRs)

Para detalhes profundos, consulte a documentação técnica:
- **[ADR-001: Facade Pattern](backend/etc/docs/adr/ADR-001-facade-pattern.md)** - Uso criterioso de Facades.
- **[ADR-004: DTO Pattern](backend/etc/docs/adr/ADR-004-dto-pattern.md)** - Regras de DTO vs @JsonView e formatação.
- **[ADR-008: Simplification Decisions](backend/etc/docs/adr/ADR-008-simplification-decisions.md)** - Histórico de todas as mudanças.

---

## 📞 Suporte
Em caso de dúvidas sobre onde colocar uma nova lógica, consulte a equipe de arquitetura ou abra uma Issue de "Dúvida Arquitetural".
