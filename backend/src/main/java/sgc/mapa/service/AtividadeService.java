package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.usuario.UsuarioService;
import sgc.subprocesso.service.SubprocessoService;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * Serviço para gerenciar a lógica de negócios de Atividades e Conhecimentos.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AtividadeService {
    private final AtividadeRepo atividadeRepo;
    private final MapaRepo mapaRepo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoRepo conhecimentoRepo;
    private final ConhecimentoMapper conhecimentoMapper;
    @Lazy
    private final SubprocessoService subprocessoService;
    private final UsuarioService usuarioService;

    /**
     * Retorna uma lista de todas as atividades.
     *
     * @return Uma {@link List} de {@link AtividadeDto}.
     */
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
    public AtividadeDto obterPorCodigo(Long codAtividade) {
        return atividadeRepo
                .findById(codAtividade)
                .map(atividadeMapper::toDto)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
    }

    /**
     * Busca uma atividade pelo seu código e retorna a entidade.
     *
     * @param codAtividade O código da atividade.
     * @return A entidade {@link Atividade} correspondente.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public Atividade obterEntidadePorCodigo(Long codAtividade) {
        return atividadeRepo
                .findById(codAtividade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
    }

    /**
     * Cria uma nova atividade, realizando validações de segurança e de estado do subprocesso.
     *
     * @param atividadeDto  O DTO com os dados da nova atividade.
     * @param tituloUsuario O título de eleitor do usuário que está criando a atividade.
     * @return O {@link AtividadeDto} da atividade criada.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou o usuário não forem encontrados.
     * @throws ErroAccessoNegado         se o usuário não for o titular da unidade do subprocesso.
     * @throws ErroSituacaoInvalida      se o subprocesso já estiver finalizado.
     */
    public AtividadeDto criar(AtividadeDto atividadeDto, String tituloUsuario) {
        if (atividadeDto.getMapaCodigo() == null) {
            throw new ErroEntidadeNaoEncontrada("Mapa", "não informado");
        }

        var subprocesso = subprocessoService.obterEntidadePorCodigoMapa(atividadeDto.getMapaCodigo());

        var usuario = usuarioService.buscarEntidadePorId(tituloUsuario);

        // Validação defensiva: garante que apenas o titular da unidade pode criar atividades.
        if (subprocesso.getUnidade() == null) {
            throw new ErroEntidadeNaoEncontrada("Unidade não associada ao Subprocesso %d".formatted(subprocesso.getCodigo()));
        }

        // Verifica se o usuário é o titular da unidade
        if (!usuario.getTituloEleitoral().equals(subprocesso.getUnidade().getTituloTitular())) {
            throw new ErroAccessoNegado("Usuário não autorizado a criar atividades para este subprocesso.");
        }

        atualizarSituacaoSubprocessoSeNecessario(atividadeDto.getMapaCodigo());

        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada(
                    "Mapa não associado ao subprocesso %d".formatted(subprocesso.getCodigo()));
        }

        var entidade = atividadeMapper.toEntity(atividadeDto);
        entidade.setMapa(subprocesso.getMapa());

        var salvo = atividadeRepo.save(entidade);

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
        log.debug("Atualizando atividade com código: {}", codigo);
        try {
            atividadeRepo
                    .findById(codigo)
                    .map(existente -> {
                        log.debug("Atividade encontrada: {}, mapa: {}", existente.getCodigo(),
                                existente.getMapa() != null ? existente.getMapa().getCodigo() : "null");

                        if (existente.getMapa() != null) {
                            atualizarSituacaoSubprocessoSeNecessario(existente.getMapa().getCodigo());
                        }

                        var entidadeParaAtualizar = atividadeMapper.toEntity(atividadeDto);
                        existente.setDescricao(entidadeParaAtualizar.getDescricao());

                        var atualizado = atividadeRepo.save(existente);
                        log.info("Atividade {} atualizada", codigo);
                        return atividadeMapper.toDto(atualizado);
                    })
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codigo));
        } catch (Exception e) {
            log.error("Erro ao atualizar atividade {}: {}", codigo, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Exclui uma atividade e todos os seus conhecimentos associados.
     *
     * @param codAtividade O código da atividade a ser excluída.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public void excluir(Long codAtividade) {
        atividadeRepo.findById(codAtividade).ifPresentOrElse(
                atividade -> {
                    atualizarSituacaoSubprocessoSeNecessario(atividade.getMapa().getCodigo());
                    var conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
                    conhecimentoRepo.deleteAll(conhecimentos);
                    atividadeRepo.delete(atividade);
                },
                () -> {
                    throw new ErroEntidadeNaoEncontrada("Atividade", codAtividade);
                });
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade específica.
     *
     * @param codAtividade O código da atividade.
     * @return Uma {@link List} de {@link ConhecimentoDto}.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public List<ConhecimentoDto> listarConhecimentos(Long codAtividade) {
        if (!atividadeRepo.existsById(codAtividade)) {
            throw new ErroEntidadeNaoEncontrada("Atividade", codAtividade);
        }
        return conhecimentoRepo.findByAtividadeCodigo(codAtividade).stream()
                .map(conhecimentoMapper::toDto)
                .toList();
    }

    /**
     * Cria um novo conhecimento e o associa a uma atividade existente.
     *
     * @param codAtividade    O código da atividade à qual o conhecimento será associado.
     * @param conhecimentoDto O DTO com os dados do novo conhecimento.
     * @return O {@link ConhecimentoDto} do conhecimento criado.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public ConhecimentoDto criarConhecimento(Long codAtividade, ConhecimentoDto conhecimentoDto) {
        return atividadeRepo.findById(codAtividade)
                .map(atividade -> {
                    atualizarSituacaoSubprocessoSeNecessario(atividade.getMapa().getCodigo());
                    var conhecimento = conhecimentoMapper.toEntity(conhecimentoDto);
                    conhecimento.setAtividade(atividade);
                    var salvo = conhecimentoRepo.save(conhecimento);
                    return conhecimentoMapper.toDto(salvo);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
    }

    /**
     * Atualiza um conhecimento existente, verificando se ele pertence à atividade especificada.
     *
     * @param codAtividade    O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser atualizado.
     * @param conhecimentoDto O DTO com os novos dados do conhecimento.
     * @throws ErroEntidadeNaoEncontrada se o conhecimento não for encontrado ou não pertencer à
     *                                   atividade.
     */
    public void atualizarConhecimento(
            Long codAtividade, Long codConhecimento, ConhecimentoDto conhecimentoDto) {

        conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .map(existente -> {
                    atualizarSituacaoSubprocessoSeNecessario(existente.getAtividade().getMapa().getCodigo());
                    var paraAtualizar = conhecimentoMapper.toEntity(conhecimentoDto);
                    existente.setDescricao(paraAtualizar.getDescricao());
                    var atualizado = conhecimentoRepo.save(existente);
                    return conhecimentoMapper.toDto(atualizado);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento));
    }

    /**
     * Exclui um conhecimento, verificando se ele pertence à atividade especificada.
     *
     * @param codAtividade    O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser excluído.
     * @throws ErroEntidadeNaoEncontrada se o conhecimento não for encontrado ou não pertencer à
     *                                   atividade.
     */
    public void excluirConhecimento(Long codAtividade, Long codConhecimento) {
        conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .ifPresentOrElse(
                        conhecimento -> {
                            atualizarSituacaoSubprocessoSeNecessario(conhecimento.getAtividade().getMapa().getCodigo());
                            conhecimentoRepo.delete(conhecimento);
                        },
                        () -> {
                            throw new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento);
                        });
    }

    private void atualizarSituacaoSubprocessoSeNecessario(Long mapaCodigo) {
        subprocessoService.atualizarSituacaoParaEmAndamento(mapaCodigo);
    }

    /**
     * Busca atividades de um mapa.
     * Método público para uso por outros serviços (ex: SubprocessoService).
     */
    public List<Atividade> buscarPorMapaCodigo(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigo(mapaCodigo);
    }

    /**
     * Busca atividades de um mapa com conhecimentos fetchados.
     * Método público para uso por outros serviços (ex: SubprocessoService).
     */
    public List<Atividade> buscarPorMapaCodigoComConhecimentos(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigoWithConhecimentos(mapaCodigo);
    }

    /**
     * Lista conhecimentos de uma atividade (Entidades).
     */
    public List<Conhecimento> listarConhecimentosPorAtividade(Long codAtividade) {
        return conhecimentoRepo.findByAtividadeCodigo(codAtividade);
    }

    /**
     * Lista conhecimentos de um mapa (Entidades).
     */
    public List<Conhecimento> listarConhecimentosPorMapa(Long codMapa) {
        return conhecimentoRepo.findByMapaCodigo(codMapa);
    }

    /**
     * Importa atividades de um mapa para outro.
     */
    public void importarAtividadesDeOutroMapa(Long mapaOrigemId, Long mapaDestinoId) {
        List<Atividade> atividadesOrigem = atividadeRepo.findByMapaCodigo(mapaOrigemId);
        List<String> descricoesExistentes = atividadeRepo.findByMapaCodigo(mapaDestinoId).stream()
                .map(Atividade::getDescricao)
                .toList();

        Mapa mapaDestino = mapaRepo.findById(mapaDestinoId)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", mapaDestinoId));

        for (Atividade atividadeOrigem : atividadesOrigem) {
            if (descricoesExistentes.contains(atividadeOrigem.getDescricao())) {
                continue;
            }

            Atividade novaAtividade = new Atividade();
            novaAtividade.setDescricao(atividadeOrigem.getDescricao());
            novaAtividade.setMapa(mapaDestino);
            Atividade atividadeSalva = atividadeRepo.save(novaAtividade);

            List<Conhecimento> conhecimentosOrigem =
                    conhecimentoRepo.findByAtividadeCodigo(atividadeOrigem.getCodigo());
            if (conhecimentosOrigem != null) {
                for (Conhecimento conhecimentoOrigem : conhecimentosOrigem) {
                    Conhecimento novoConhecimento = new Conhecimento();
                    novoConhecimento.setDescricao(conhecimentoOrigem.getDescricao());
                    novoConhecimento.setAtividade(atividadeSalva);
                    conhecimentoRepo.save(novoConhecimento);
                }
            }
        }
    }
}
