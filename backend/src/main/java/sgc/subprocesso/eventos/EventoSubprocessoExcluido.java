package sgc.subprocesso.eventos;

import lombok.Builder;
import lombok.Getter;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.time.LocalDateTime;

/**
 * Evento de domínio publicado quando um subprocesso é excluído.
 *
 * <p>Este evento é disparado antes da exclusão física do subprocesso no banco de dados,
 * permitindo que outros módulos realizem limpezas coordenadas.
 *
 * <p><b>Casos de uso:</b>
 * <ul>
 *   <li>Exclusão do mapa de competências associado</li>
 *   <li>Remoção de alertas relacionados</li>
 *   <li>Limpeza de histórico de movimentações</li>
 *   <li>Auditoria de exclusões</li>
 *   <li>Invalidação de cache</li>
 * </ul>
 *
 * <p><b>Importante:</b> Este evento é publicado ANTES da exclusão física, então
 * listeners ainda podem acessar dados relacionados via queries.
 *
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * EventoSubprocessoExcluido evento = EventoSubprocessoExcluido.builder()
 *     .codSubprocesso(codigo)
 *     .codProcesso(subprocesso.getProcesso().getCodigo())
 *     .codUnidade(subprocesso.getUnidade().getCodigo())
 *     .codMapa(subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null)
 *     .situacao(subprocesso.getSituacao())
 *     .usuario(usuarioAutenticado)
 *     .dataHoraExclusao(LocalDateTime.now())
 *     .build();
 * eventPublisher.publishEvent(evento);
 * subprocessoRepo.deleteById(codigo);
 * }</pre>
 *
 * @see EventoSubprocessoCriado
 * @see EventoSubprocessoAtualizado
 * @see EventoTransicaoSubprocesso
 */
@Getter
@Builder
public class EventoSubprocessoExcluido {

    /**
     * Código do subprocesso que foi excluído.
     * <p>Armazenado separadamente pois a entidade será removida do banco.
     */
    private Long codSubprocesso;

    /**
     * Código do processo pai.
     */
    private @org.jspecify.annotations.Nullable Long codProcesso;

    /**
     * Código da unidade responsável.
     */
    private @org.jspecify.annotations.Nullable Long codUnidade;

    /**
     * Código do mapa de competências associado (se existir).
     * <p>Pode ser null se o subprocesso não tinha mapa criado.
     */
    private @org.jspecify.annotations.Nullable Long codMapa;

    /**
     * Situação do subprocesso no momento da exclusão.
     */
    private SituacaoSubprocesso situacao;

    /**
     * Usuário que realizou a exclusão.
     */
    private Usuario usuario;

    /**
     * Data e hora da exclusão.
     */
    private LocalDateTime dataHoraExclusao;

    /**
     * Motivo da exclusão (opcional).
     */
    private String motivoExclusao;
}
