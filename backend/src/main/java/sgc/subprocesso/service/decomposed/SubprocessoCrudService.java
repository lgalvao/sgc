package sgc.subprocesso.service.decomposed;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioService;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.eventos.EventoSubprocessoAtualizado;
import sgc.subprocesso.eventos.EventoSubprocessoCriado;
import sgc.subprocesso.eventos.EventoSubprocessoExcluido;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Serviço especializado para operações CRUD básicas de Subprocesso.
 *
 * <p>Responsável pelas operações de criação, leitura, atualização e exclusão
 * de subprocessos. Parte da decomposição arquitetural do módulo subprocesso.
 *
 * <p><b>Visibilidade:</b> Package-private - uso interno ao módulo subprocesso.
 * Acesso externo deve ser feito via {@link sgc.subprocesso.service.SubprocessoService}
 * ou {@link sgc.subprocesso.service.SubprocessoFacade}.
 *
 * @since 2.0.0 - Tornado package-private na consolidação arquitetural Sprint 2
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubprocessoCrudService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final SubprocessoMapper subprocessoMapper;
    private final MapaFacade mapaFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final UsuarioService usuarioService;

    public Subprocesso buscarSubprocesso(Long codigo) {
        return repositorioSubprocesso
                .findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado", codigo);
        }
        return subprocesso;
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return repositorioSubprocesso.findByProcessoCodigoWithUnidade(codProcesso);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .situacaoLabel(subprocesso.getSituacao() != null ? subprocesso.getSituacao().name() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return repositorioSubprocesso
                .findByMapaCodigo(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Subprocesso não encontrado para o mapa com código %d".formatted(codMapa)));
    }

    public SubprocessoDto criar(SubprocessoDto subprocessoDto) {
        var entity = subprocessoMapper.toEntity(subprocessoDto);
        entity.setMapa(null);
        var subprocessoSalvo = repositorioSubprocesso.save(entity);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaFacade.salvar(mapa);

        subprocessoSalvo.setMapa(mapaSalvo);
        var salvo = repositorioSubprocesso.save(subprocessoSalvo);

        // Publica evento de criação
        EventoSubprocessoCriado evento = EventoSubprocessoCriado.builder()
                .subprocesso(salvo)
                .usuario(usuarioService.obterUsuarioAutenticadoOuNull())
                .dataHoraCriacao(LocalDateTime.now())
                .criadoPorProcesso(false)  // TODO: detectar se foi criado automaticamente
                .codProcesso(salvo.getProcesso() != null ? salvo.getProcesso().getCodigo() : null)
                .codUnidade(salvo.getUnidade() != null ? salvo.getUnidade().getCodigo() : null)
                .build();
        eventPublisher.publishEvent(evento);

        return subprocessoMapper.toDTO(salvo);
    }

    public SubprocessoDto atualizar(Long codigo, SubprocessoDto subprocessoDto) {
        return repositorioSubprocesso.findById(codigo)
                .map(subprocesso -> {
                    // Captura estado anterior para o evento
                    Set<String> camposAlterados = new HashSet<>();
                    SituacaoSubprocesso situacaoAnterior = subprocesso.getSituacao();

                    if (subprocessoDto.getCodMapa() != null) {
                        Mapa mapa = new Mapa();
                        mapa.setCodigo(subprocessoDto.getCodMapa());
                        if (!Objects.equals(subprocesso.getMapa(), mapa)) {
                            camposAlterados.add("mapa");
                        }
                        subprocesso.setMapa(mapa);
                    } else {
                        if (subprocesso.getMapa() != null) {
                            camposAlterados.add("mapa");
                        }
                        subprocesso.setMapa(null);
                    }

                    if (!Objects.equals(subprocesso.getDataLimiteEtapa1(), subprocessoDto.getDataLimiteEtapa1())) {
                        camposAlterados.add("dataLimiteEtapa1");
                    }
                    if (!Objects.equals(subprocesso.getDataFimEtapa1(), subprocessoDto.getDataFimEtapa1())) {
                        camposAlterados.add("dataFimEtapa1");
                    }
                    if (!Objects.equals(subprocesso.getDataFimEtapa2(), subprocessoDto.getDataFimEtapa2())) {
                        camposAlterados.add("dataFimEtapa2");
                    }
                    if (!Objects.equals(subprocesso.getSituacao(), subprocessoDto.getSituacao())) {
                        camposAlterados.add("situacao");
                    }

                    subprocesso.setDataLimiteEtapa1(subprocessoDto.getDataLimiteEtapa1());
                    subprocesso.setDataFimEtapa1(subprocessoDto.getDataFimEtapa1());
                    subprocesso.setDataFimEtapa2(subprocessoDto.getDataFimEtapa2());
                    subprocesso.setSituacao(subprocessoDto.getSituacao());

                    Subprocesso salvo = repositorioSubprocesso.save(subprocesso);

                    // Publica evento de atualização se houve mudanças
                    if (!camposAlterados.isEmpty()) {
                        EventoSubprocessoAtualizado evento = EventoSubprocessoAtualizado.builder()
                                .subprocesso(salvo)
                                .usuario(usuarioService.obterUsuarioAutenticadoOuNull())
                                .camposAlterados(camposAlterados)
                                .dataHoraAtualizacao(LocalDateTime.now())
                                .situacaoAnterior(situacaoAnterior != subprocessoDto.getSituacao() ? situacaoAnterior : null)
                                .build();
                        eventPublisher.publishEvent(evento);
                    }

                    return subprocessoMapper.toDTO(salvo);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    public void excluir(Long codigo) {
        Subprocesso subprocesso = repositorioSubprocesso.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));

        // Publica evento ANTES da exclusão
        EventoSubprocessoExcluido evento = EventoSubprocessoExcluido.builder()
                .codSubprocesso(codigo)
                .codProcesso(subprocesso.getProcesso() != null ? subprocesso.getProcesso().getCodigo() : null)
                .codUnidade(subprocesso.getUnidade() != null ? subprocesso.getUnidade().getCodigo() : null)
                .codMapa(subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null)
                .situacao(subprocesso.getSituacao())
                .usuario(usuarioService.obterUsuarioAutenticadoOuNull())
                .dataHoraExclusao(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(evento);

        repositorioSubprocesso.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return repositorioSubprocesso.findAllComFetch().stream().map(subprocessoMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        Subprocesso sp = repositorioSubprocesso
                .findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para o processo %d e unidade %d".formatted(codProcesso, codUnidade)));
        return subprocessoMapper.toDTO(sp);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return repositorioSubprocesso.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }
}
