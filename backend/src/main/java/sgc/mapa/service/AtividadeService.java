package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.model.*;

import java.util.List;

/**
 * Serviço para gerenciar a lógica de negócios de Atividades.
 *
 * <p>Os métodos de Conhecimentos foram migrados para {@link ConhecimentoService}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AtividadeService {
    private static final String ENTIDADE_ATIVIDADE = "Atividade";
    private static final String ENTIDADE_MAPA = "Mapa";

    private final AtividadeRepo atividadeRepo;
    private final MapaRepo mapaRepo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoService conhecimentoService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Retorna uma lista de todas as atividades.
     *
     * @return Uma {@link List} de {@link AtividadeDto}.
     */
    @Transactional(readOnly = true)
    public List<AtividadeDto> listar() {
        return atividadeRepo.findAll().stream().map(atividadeMapper::toDto).toList();
    }

    /**
     * Busca uma atividade pelo seu código.
     *
     * @param codAtividade O código da atividade.
     * @return O {@link AtividadeDto} correspondente.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    @Transactional(readOnly = true)
    public AtividadeDto obterDto(Long codAtividade) {
        return atividadeRepo.findById(codAtividade)
                .map(atividadeMapper::toDto)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_ATIVIDADE, codAtividade));
    }

    /**
     * Busca uma atividade pelo seu código e retorna a entidade.
     *
     * @param codAtividade O código da atividade.
     * @return A entidade {@link Atividade} correspondente.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    @Transactional(readOnly = true)
    public Atividade obterPorCodigo(Long codAtividade) {
        return atividadeRepo.findById(codAtividade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_ATIVIDADE, codAtividade));
    }

    /**
     * Cria uma nova atividade, realizando validações de segurança e de estado do subprocesso.
     *
     * @param atividadeDto  O DTO com os dados da nova atividade.
     * @param tituloUsuario O título de eleitor do usuário que está criando a atividade.
     * @return O {@link AtividadeDto} da atividade criada.
     * @throws ErroEntidadeNaoEncontrada            se o subprocesso ou o usuário não forem encontrados.
     * @throws sgc.comum.erros.ErroSituacaoInvalida se o subprocesso já estiver finalizado.
     */
    public AtividadeDto criar(AtividadeDto atividadeDto, String tituloUsuario) {
        if (atividadeDto.getMapaCodigo() == null) {
            throw new ErroEntidadeNaoEncontrada(ENTIDADE_MAPA, "não informado");
        }

        Mapa mapa = mapaRepo.findById(atividadeDto.getMapaCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_MAPA, atividadeDto.getMapaCodigo()));

        var subprocesso = mapa.getSubprocesso();
        if (subprocesso == null) {
            throw new ErroEntidadeNaoEncontrada(
                    "Subprocesso não encontrado para o mapa com código %d".formatted(atividadeDto.getMapaCodigo()));
        }

        notificarAlteracaoMapa(atividadeDto.getMapaCodigo());

        Atividade entidade = atividadeMapper.toEntity(atividadeDto);
        entidade.setMapa(mapa);

        Atividade salvo = atividadeRepo.save(entidade);
        return atividadeMapper.toDto(salvo);
    }

    /**
     * Atualiza uma atividade existente.
     *
     * @param codigo       O código da atividade a ser atualizada.
     * @param atividadeDto O DTO com os novos dados da atividade.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public void atualizar(Long codigo, AtividadeDto atividadeDto) {
        try {
            atividadeRepo.findById(codigo)
                    .map(existente -> atualizarAtividadeExistente(codigo, atividadeDto, existente))
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_ATIVIDADE, codigo));
        } catch (Exception e) {
            log.error("Erro ao atualizar atividade {}: {}", codigo, e.getMessage(), e);
            throw e;
        }
    }

    private AtividadeDto atualizarAtividadeExistente(Long codigo, AtividadeDto atividadeDto, Atividade existente) {
        if (existente.getMapa() != null) {
            notificarAlteracaoMapa(existente.getMapa().getCodigo());
        }

        var entidadeParaAtualizar = atividadeMapper.toEntity(atividadeDto);
        existente.setDescricao(entidadeParaAtualizar.getDescricao());

        var atualizado = atividadeRepo.save(existente);
        log.info("Atividade {} atualizada", codigo);
        return atividadeMapper.toDto(atualizado);
    }

    /**
     * Exclui uma atividade e todos os seus conhecimentos associados.
     *
     * @param codAtividade O código da atividade a ser excluída.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public void excluir(Long codAtividade) {
        atividadeRepo.findById(codAtividade).ifPresentOrElse(
                this::excluirAtividadeEConhecimentos,
                () -> {
                    throw new ErroEntidadeNaoEncontrada(ENTIDADE_ATIVIDADE, codAtividade);
                });
    }

    private void excluirAtividadeEConhecimentos(Atividade atividade) {
        notificarAlteracaoMapa(atividade.getMapa().getCodigo());
        conhecimentoService.excluirTodosDaAtividade(atividade);
        atividadeRepo.delete(atividade);
    }

    // ===================================================================================
    // Métodos de consulta para outros serviços
    // ===================================================================================

    /**
     * Busca atividades de um mapa.
     * Método público para uso por outros serviços (ex: SubprocessoService).
     */
    @Transactional(readOnly = true)
    public List<Atividade> buscarPorMapaCodigo(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigo(mapaCodigo);
    }

    /**
     * Busca atividades de um mapa com conhecimentos fetchados.
     * Método público para uso por outros serviços (ex: SubprocessoService).
     */
    @Transactional(readOnly = true)
    public List<Atividade> buscarPorMapaCodigoComConhecimentos(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigoWithConhecimentos(mapaCodigo);
    }

    // ===================================================================================
    // Métodos auxiliares
    // ===================================================================================

    private void notificarAlteracaoMapa(Long mapaCodigo) {
        eventPublisher.publishEvent(new EventoMapaAlterado(mapaCodigo));
    }
}
