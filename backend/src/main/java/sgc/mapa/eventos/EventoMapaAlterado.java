package sgc.mapa.eventos;

/**
 * Evento disparado quando ocorrem alterações em atividades ou conhecimentos
 * de um mapa que podem afetar o status do subprocesso.
 */
public record EventoMapaAlterado(Long mapaCodigo) {
}
