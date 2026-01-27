package sgc.subprocesso.service.crud;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
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
 * <p>
 * Responsável pelas operações de criação, leitura, atualização e exclusão
 * de subprocessos. Parte da decomposição arquitetural do módulo subprocesso.
 *
 * <p>
 * <b>Visibilidade:</b> Package-private - uso interno ao módulo subprocesso.
 * Acesso externo deve ser feito via
 * {@link sgc.subprocesso.service.SubprocessoFacade}.
 *
 * <p>
 * <b>Nota sobre Injeção de Dependências:</b>
 * MapaFacade é injetado com @Lazy para quebrar a dependência circular:
 * SubprocessoFacade → SubprocessoCrudService → MapaFacade →
 * MapaVisualizacaoService → SubprocessoFacade
 *
 * @since 2.0.0 - Tornado package-private na consolidação arquitetural Sprint 2
 */
@Service
@Transactional
public class SubprocessoCrudService {
    private static final String MSG_SUBPROCESSO_NAO_ENCONTRADO = "Subprocesso não encontrado";
    private final SubprocessoRepo repositorioSubprocesso;
    private final SubprocessoMapper subprocessoMapper;
    private final MapaFacade mapaFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final UsuarioFacade usuarioService;
    private final RepositorioComum repo;

    /**
     * Constructor with @Lazy injection to break circular dependency.
     *
     * @param mapaFacade injetado com @Lazy para evitar
     *                   BeanCurrentlyInCreationException
     */
    public SubprocessoCrudService(
            SubprocessoRepo repositorioSubprocesso,
            SubprocessoMapper subprocessoMapper,
            @Lazy MapaFacade mapaFacade,
            ApplicationEventPublisher eventPublisher,
            UsuarioFacade usuarioService,
            RepositorioComum repo) {
        this.repositorioSubprocesso = repositorioSubprocesso;
        this.subprocessoMapper = subprocessoMapper;
        this.mapaFacade = mapaFacade;
        this.eventPublisher = eventPublisher;
        this.usuarioService = usuarioService;
        this.repo = repo;
    }

    public Subprocesso buscarSubprocesso(Long codigo) {
        return repo.buscar(Subprocesso.class, codigo);
    }

    /**
     * Busca subprocesso e seu mapa associado.
     * <p>
     * O mapa é um invariante do subprocesso após a criação, portanto é garantido
     * que exista.
     */
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return repositorioSubprocesso.findByProcessoCodigoWithUnidade(codProcesso);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoESituacao(Long codProcesso, SituacaoSubprocesso situacao) {
        return repositorioSubprocesso.findByProcessoCodigoAndSituacaoWithUnidade(codProcesso, situacao);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long codProcesso, Long codUnidade,
            List<SituacaoSubprocesso> situacoes) {
        return repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(codProcesso,
                codUnidade, situacoes);
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
                .orElseThrow(() -> new sgc.comum.erros.ErroEntidadeNaoEncontrada(
                        "%s para o mapa com código %d".formatted(MSG_SUBPROCESSO_NAO_ENCONTRADO, codMapa)));
    }

    public SubprocessoDto criar(CriarSubprocessoRequest request) {
        return criar(request, false);
    }

    public SubprocessoDto criar(CriarSubprocessoRequest request, boolean criadoPorProcesso) {
        var entity = new Subprocesso();
        // Mapear manualmente do Request
        var processo = new sgc.processo.model.Processo();
        processo.setCodigo(request.codProcesso());
        entity.setProcesso(processo);
        var unidade = new sgc.organizacao.model.Unidade();
        unidade.setCodigo(request.codUnidade());
        entity.setUnidade(unidade);
        entity.setDataLimiteEtapa1(request.dataLimiteEtapa1());
        entity.setDataLimiteEtapa2(request.dataLimiteEtapa2());
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
                .criadoPorProcesso(true)
                .codProcesso(salvo.getProcesso().getCodigo())
                .codUnidade(salvo.getUnidade().getCodigo())
                .build();
        eventPublisher.publishEvent(evento);

        return subprocessoMapper.toDto(salvo);
    }

    public SubprocessoDto atualizar(Long codigo, AtualizarSubprocessoRequest request) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        SituacaoSubprocesso situacaoAnterior = subprocesso.getSituacao();
        Set<String> camposAlterados = processarAlteracoes(subprocesso, request);

        Subprocesso salvo = repositorioSubprocesso.save(subprocesso);

        // Publica evento de atualização se houve mudanças
        if (!camposAlterados.isEmpty()) {
            EventoSubprocessoAtualizado evento = EventoSubprocessoAtualizado.builder()
                    .subprocesso(salvo)
                    .usuario(usuarioService.obterUsuarioAutenticadoOuNull())
                    .camposAlterados(camposAlterados)
                    .dataHoraAtualizacao(LocalDateTime.now())
                    .situacaoAnterior(situacaoAnterior)
                    .build();
            eventPublisher.publishEvent(evento);
        }

        return subprocessoMapper.toDto(salvo);
    }

    private Set<String> processarAlteracoes(Subprocesso subprocesso, AtualizarSubprocessoRequest request) {
        Set<String> campos = new HashSet<>();

        java.util.Optional.ofNullable(request.codMapa()).ifPresent(cod -> {
            Mapa m = new Mapa();
            m.setCodigo(cod);
            // Compara IDs para evitar atualização desnecessária (já que Mapa não implementa
            // equals)
            Long codAtual = subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null;
            if (!Objects.equals(codAtual, cod)) {
                campos.add("mapa");
                subprocesso.setMapa(m);
            }
        });

        if (!Objects.equals(subprocesso.getDataLimiteEtapa1(), request.dataLimiteEtapa1())) {
            campos.add("dataLimiteEtapa1");
            subprocesso.setDataLimiteEtapa1(request.dataLimiteEtapa1());
        }
        if (!Objects.equals(subprocesso.getDataFimEtapa1(), request.dataFimEtapa1())) {
            campos.add("dataFimEtapa1");
            subprocesso.setDataFimEtapa1(request.dataFimEtapa1());
        }
        if (!Objects.equals(subprocesso.getDataFimEtapa2(), request.dataFimEtapa2())) {
            campos.add("dataFimEtapa2");
            subprocesso.setDataFimEtapa2(request.dataFimEtapa2());
        }

        return campos;
    }

    public void excluir(Long codigo) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);

        // Publica evento ANTES da exclusão
        EventoSubprocessoExcluido evento = EventoSubprocessoExcluido.builder()
                .codSubprocesso(codigo)
                .codProcesso(subprocesso.getProcesso().getCodigo())
                .codUnidade(subprocesso.getUnidade().getCodigo())
                .codMapa(subprocesso.getMapa().getCodigo())
                .situacao(subprocesso.getSituacao())
                .usuario(usuarioService.obterUsuarioAutenticadoOuNull())
                .dataHoraExclusao(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(evento);

        repositorioSubprocesso.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return repositorioSubprocesso.findAllComFetch().stream().map(subprocessoMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        Subprocesso sp = repositorioSubprocesso
                .findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade)
                .orElseThrow(() -> new sgc.comum.erros.ErroEntidadeNaoEncontrada(
                        "%s para o processo %s e unidade %s".formatted(MSG_SUBPROCESSO_NAO_ENCONTRADO, codProcesso,
                                codUnidade)));
        return subprocessoMapper.toDto(sp);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return repositorioSubprocesso.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }
}
