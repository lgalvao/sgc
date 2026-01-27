# ADR-002: Eventos de Domínio Unificados

**Data**: 2026-01-10  
**Status**: ✅ Aceito e Implementado (Subprocesso)  
**Decisores**: Equipe de Arquitetura SGC

---

## Contexto e Problema

Subprocessos têm múltiplas transições de estado (~15 transições diferentes entre situações).

**Abordagem Tradicional:** Um evento por transição

- EventoCadastroDisponibilizado
- EventoCadastroDevolvido
- EventoCadastroAceito
- EventoCadastroHomologado
- EventoRevisaoCadastroDisponibilizada
- ... (15+ eventos)

**Problemas:**

1. Proliferação de classes (15+ arquivos)
2. Código duplicado entre eventos
3. Difícil adicionar novos tipos
4. Listeners precisam se registrar em múltiplos eventos

---

## Decisão

Usar **Evento Unificado** com enum de tipos para transições similares.

**EventoTransicaoSubprocesso:**

```java
public record EventoTransicaoSubprocesso(
    Long codigoSubprocesso,
    SituacaoSubprocesso novaSituacao,
    TipoTransicao tipoTransicao,
    String tituloEleitoral
) {}

public enum TipoTransicao {
    CADASTRO_DISPONIBILIZADO,
    CADASTRO_DEVOLVIDO,
    CADASTRO_ACEITO,
    CADASTRO_HOMOLOGADO,
    REVISAO_CADASTRO_DISPONIBILIZADA,
    // ... 15 tipos
}
```

---

## Vantagens ✅

1. **Menos classes**: 1 evento + 1 enum vs 15+ classes
2. **Consistência**: Mesma estrutura de dados
3. **Simplicidade**: Fácil adicionar novo tipo (enum entry)
4. **Listeners flexíveis**: Podem filtrar ou capturar todos

## Desvantagens ❌

1. **Type safety reduzido**: Enum vs classe específica
2. **Acoplamento ao enum**: Todos os tipos no mesmo lugar

---

## Quando Usar

✅ **Use evento unificado quando:**

- Múltiplas transições similares (>5)
- Mesma estrutura de dados
- Ações subsequentes similares

❌ **Use eventos separados quando:**

- Eventos conceitualmente diferentes
- Estruturas de dados diferentes
- Processamento completamente distinto

---

## Exemplos

**Unificado** (Subprocesso): EventoTransicaoSubprocesso + TipoTransicao  
**Separado** (Processo): EventoProcessoCriado, EventoProcessoIniciado, EventoProcessoFinalizado

---

**Autor**: GitHub Copilot AI Agent  
**Revisão**: 2026-07-10
