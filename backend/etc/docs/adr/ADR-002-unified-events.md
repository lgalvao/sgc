# ADR-002: Eventos de Domínio Unificados

**Status:** ❌ Descontinuado (Substituído por Orquestração Direta)

---

## Contexto e Problema

Subprocessos têm múltiplas transições de estado diferentes entre situações.

**Abordagem Tradicional:** Um evento por transição

- EventoCadastroDisponibilizado
- EventoCadastroDevolvido
- EventoCadastroAceito
- EventoCadastroHomologado
- EventoRevisaoCadastroDisponibilizada
- ... (muitos eventos)

**Problemas:**

1. Proliferação de classes (muitos arquivos)
2. Código duplicado entre eventos
3. Difícil adicionar novos tipos
4. Listeners precisam se registrar em múltiplos eventos

---

## Decisão (Descontinuada)

Esta ADR previa usar **Evento Unificado** para desacoplar notificações de transições.

**Motivo da Descontinuação (17/02/2026):**
Em favor da **Simplicidade**, optou-se por realizar as notificações (Alerta, Email) diretamente no Service de Transição (`SubprocessoTransicaoService`), sem a indireção de eventos.
A complexidade de manter listeners, publishers e objetos de evento não se justificou para o tamanho atual da aplicação.

**Abordagem Atual:**
Chamadas diretas e explícitas no service:
```java
// SubprocessoTransicaoService.java
public void registrar(...) {
    // ... persistência da movimentação ...

    // Notificação direta (Simples e Explícito)
    notificarTransicao(sp, tipo, origem, destino, observacoes);
}
```

---

## Decisão Original (Histórico)

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
    // ... outros tipos
}
```

---

## Vantagens ✅

1. **Menos classes**: 1 evento + 1 enum vs muitas classes separadas
2. **Consistência**: Mesma estrutura de dados
3. **Simplicidade**: Fácil adicionar novo tipo (enum entry)
4. **Listeners flexíveis**: Podem filtrar ou capturar todos

## Desvantagens ❌

1. **Type safety reduzido**: Enum vs classe específica
2. **Acoplamento ao enum**: Todos os tipos no mesmo lugar

---

## Quando Usar

✅ **Use evento unificado quando:**

- Múltiplas transições similares
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
