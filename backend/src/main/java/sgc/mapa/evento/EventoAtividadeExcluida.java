package sgc.mapa.evento;

import lombok.Builder;
import lombok.Getter;
import sgc.organizacao.model.Usuario;

import java.time.LocalDateTime;

/**
 * Evento de domínio publicado quando uma atividade é excluída de um mapa de competências.
 *
 * <p>Este evento é disparado antes da exclusão física da atividade no banco de dados,
 * permitindo que outros módulos verifiquem impactos e realizem limpezas.
 *
 * <p><b>Casos de uso:</b>
 * <ul>
 *   <li>Verificação de competências órfãs (sem atividades associadas)</li>
 *   <li>Recálculo de validação do mapa</li>
 *   <li>Limpeza de conhecimentos associados</li>
 *   <li>Invalidação de cache</li>
 *   <li>Auditoria de exclusões</li>
 * </ul>
 *
 * <p><b>Importante:</b> Este evento é publicado ANTES da exclusão física, então
 * listeners ainda podem acessar a entidade Atividade e seus relacionamentos.
 *
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * EventoAtividadeExcluida evento = EventoAtividadeExcluida.builder()
 *     .codAtividade(codigo)
 *     .descricao(atividade.getDescricao())
 *     .codMapa(mapa.getCodigo())
 *     .codSubprocesso(subprocesso.getCodigo())
 *     .usuario(usuarioAutenticado)
 *     .quantidadeConhecimentos(atividade.getConhecimentos().size())
 *     .dataHoraExclusao(LocalDateTime.now())
 *     .build();
 * eventPublisher.publishEvent(evento);
 * atividadeRepo.deleteById(codigo);
 * }</pre>
 *
 * @see EventoAtividadeCriada
 * @see EventoAtividadeAtualizada
 * @see EventoMapaAlterado
 */
@Getter
@Builder
public class EventoAtividadeExcluida {

    /**
     * Código da atividade que foi excluída.
     * <p>Armazenado separadamente pois a entidade será removida do banco.
     */
    private Long codAtividade;

    /**
     * Descrição da atividade excluída.
     * <p>Para auditoria e logs.
     */
    private String descricao;

    /**
     * Código do mapa de competências.
     */
    private Long codMapa;

    /**
     * Código do subprocesso proprietário do mapa.
     */
    private Long codSubprocesso;

    /**
     * Usuário que realizou a exclusão.
     */
    private Usuario usuario;

    /**
     * Quantidade de conhecimentos que a atividade possuía.
     * <p>Para auditoria e validação de limpeza.
     */
    private int quantidadeConhecimentos;

    /**
     * Data e hora da exclusão.
     */
    private LocalDateTime dataHoraExclusao;

    /**
     * Número total de atividades no mapa após a exclusão.
     * <p>Útil para validações (ex: mapa vazio).
     */
    private int totalAtividadesRestantes;
}
