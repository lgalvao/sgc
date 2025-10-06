package sgc.processo;

/**
 * Evento simples indicando que um Subprocesso foi disponibilizado.
 * Usado para publicação via ApplicationEventPublisher.
 */
public record EventoSubprocessoDisponibilizado(Long subprocessoCodigo) {
}