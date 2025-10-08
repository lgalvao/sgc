package sgc.processo.eventos;

/**
 * Evento de domínio publicado quando um Subprocesso é disponibilizado para a próxima
 * etapa do fluxo (ex: disponibilizar cadastro, disponibilizar mapa).
 * <p>
 * Este evento é usado para notificar outros sistemas ou módulos, como o de alertas,
 * que uma ação foi concluída e uma nova etapa está pronta para começar.
 *
 * @param idSubprocesso O ID do subprocesso que foi disponibilizado.
 */
public record SubprocessoDisponibilizadoEvento(Long idSubprocesso) {
}