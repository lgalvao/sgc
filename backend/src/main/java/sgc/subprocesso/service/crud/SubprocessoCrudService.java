package sgc.subprocesso.service.crud;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.model.Usuario;
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
import java.util.Optional;
import java.util.Set;

/**
 * Serviço especializado para operações CRUD básicas de Subprocesso.
 *
 * <p>Responsável pelas operações de criação, leitura, atualização e exclusão
 * de subprocessos. Parte da decomposição arquitetural do módulo subprocesso.
 *
 * <p><b>Visibilidade:</b> Package-private - uso interno ao módulo subprocesso.
 * Acesso externo deve ser feito via {@link sgc.subprocesso.service.SubprocessoFacade}.
 *
 * <p><b>Nota sobre Injeção de Dependências:</b> 
 * MapaFacade é injetado com @Lazy para quebrar a dependência circular:
 * SubprocessoFacade → SubprocessoCrudService → MapaFacade → MapaVisualizacaoService → SubprocessoFacade
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
    private final ApplicationEventPublisher publicadorEventos;
    private final UsuarioFacade usuarioService;
    private final RepositorioComum repo;

    /**
     * Constructor with @Lazy injection to break circular dependency.
     * 
     * @param mapaFacade injetado com @Lazy para evitar BeanCurrentlyInCreationException
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
        this.publicadorEventos = eventPublisher;
        this.usuarioService = usuarioService;
        this.repo = repo;
    }

    public Subprocesso buscarSubprocesso(Long codigo) {
        return repo.buscar(Subprocesso.class, codigo);
    }

    /**
     * Busca subprocesso e seu mapa associado.
     * <p>O mapa é um invariante do subprocesso após a criação, portanto é garantido que exista.
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
    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long codProcesso, Long codUnidade, List<SituacaoSubprocesso> situacoes) {
        return repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(codProcesso, codUnidade, situacoes);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .situacaoLabel(subprocesso.getSituacao().name())
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
        var sp = new Subprocesso();
        var processo = new sgc.processo.model.Processo();
        processo.setCodigo(request.getCodProcesso());
        sp.setProcesso(processo);

        var unidade = new sgc.organizacao.model.Unidade();
        unidade.setCodigo(request.getCodUnidade());
        sp.setUnidade(unidade);
        sp.setDataLimiteEtapa1(request.getDataLimiteEtapa1());
        sp.setDataLimiteEtapa2(request.getDataLimiteEtapa2());
        sp.setMapa(null);
        
        var subprocessoSalvo = repositorioSubprocesso.save(sp);
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaFacade.salvar(mapa);

        subprocessoSalvo.setMapa(mapaSalvo);
        var salvo = repositorioSubprocesso.save(subprocessoSalvo);

        EventoSubprocessoCriado evento = EventoSubprocessoCriado.builder()
                .subprocesso(salvo)
                .usuario(usuarioService.obterUsuarioAutenticadoOuNull())
                .dataHoraCriacao(LocalDateTime.now())
                .criadoPorProcesso(criadoPorProcesso || salvo.getProcesso() != null)
                .codProcesso(salvo.getProcesso() != null ? salvo.getProcesso().getCodigo() : null)
                .codUnidade(salvo.getUnidade().getCodigo())
                .build();
        publicadorEventos.publishEvent(evento);

        return subprocessoMapper.toDTO(salvo);
    }

    public SubprocessoDto atualizar(Long codigo, AtualizarSubprocessoRequest request) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        SituacaoSubprocesso situacaoAnterior = subprocesso.getSituacao();
        Set<String> camposAlterados = processarAlteracoes(subprocesso, request);

        Subprocesso salvo = repositorioSubprocesso.save(subprocesso);

        // Publica evento de atualização se houve mudanças
        if (!camposAlterados.isEmpty()) {
            @org.jspecify.annotations.Nullable Usuario usuario = usuarioService.obterUsuarioAutenticadoOuNull();
            EventoSubprocessoAtualizado evento = EventoSubprocessoAtualizado.builder()
                    .subprocesso(salvo)
                    .usuario(usuario)
                    .camposAlterados(camposAlterados)
                    .dataHoraAtualizacao(LocalDateTime.now())
                    .situacaoAnterior(situacaoAnterior)
                    .build();
            publicadorEventos.publishEvent(evento);
        }

        return subprocessoMapper.toDTO(salvo);
    }

    private Set<String> processarAlteracoes(Subprocesso subprocesso, AtualizarSubprocessoRequest request) {
        Set<String> campos = new HashSet<>();

        Optional.ofNullable(request.getCodMapa()).ifPresent(cod -> {
            Mapa m = new Mapa();
            m.setCodigo(cod);
            if (!Objects.equals(subprocesso.getMapa(), m)) {
                campos.add("mapa");
                subprocesso.setMapa(m);
            }
        });

        LocalDateTime dataLimiteEtapa1 = subprocesso.getDataLimiteEtapa1();
        if (!Objects.equals(dataLimiteEtapa1, request.getDataLimiteEtapa1())) {
            campos.add("dataLimiteEtapa1");
            subprocesso.setDataLimiteEtapa1(request.getDataLimiteEtapa1());
        }

        LocalDateTime dataFimEtapa1 = subprocesso.getDataFimEtapa1();
        if (!Objects.equals(dataFimEtapa1, request.getDataFimEtapa1())) {
            campos.add("dataFimEtapa1");
            subprocesso.setDataFimEtapa1(request.getDataFimEtapa1());
        }

        LocalDateTime dataFimEtapa2 = subprocesso.getDataFimEtapa2();
        if (!Objects.equals(dataFimEtapa2, request.getDataFimEtapa2())) {
            campos.add("dataFimEtapa2");
            subprocesso.setDataFimEtapa2(request.getDataFimEtapa2());
        }

        return campos;
    }

    public void excluir(Long codigo) {
        Subprocesso sp = buscarSubprocesso(codigo);

        // Publica evento ANTES da exclusão
        EventoSubprocessoExcluido evento = EventoSubprocessoExcluido.builder()
                .codSubprocesso(codigo)
                .codProcesso(sp.getProcesso() != null ? sp.getProcesso().getCodigo() : null)
                .codUnidade(sp.getUnidade() != null ? sp.getUnidade().getCodigo() : null)
                .codMapa(sp.getMapa() != null ? sp.getMapa().getCodigo() : null)
                .situacao(sp.getSituacao())
                .usuario(usuarioService.obterUsuarioAutenticadoOuNull())
                .dataHoraExclusao(LocalDateTime.now())
                .build();
        publicadorEventos.publishEvent(evento);

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
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "%s para o processo %s e unidade %s".formatted(MSG_SUBPROCESSO_NAO_ENCONTRADO, codProcesso, codUnidade)));
        return subprocessoMapper.toDTO(sp);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return repositorioSubprocesso.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }
}
