package sgc.processo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.jqwik.api.*;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.mappers.ProcessoDetalheMapper;
import sgc.processo.dto.mappers.ProcessoMapper;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.dto.SubprocessoMapper;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

class ProcessoServicePropertyTest {

    // Mocks
    private ProcessoRepo processoRepo = mock(ProcessoRepo.class);
    private UnidadeRepo unidadeRepo = mock(UnidadeRepo.class);
    private SubprocessoRepo subprocessoRepo = mock(SubprocessoRepo.class);
    private ApplicationEventPublisher publicadorEventos = mock(ApplicationEventPublisher.class);
    private ProcessoMapper processoMapper = mock(ProcessoMapper.class);
    private ProcessoDetalheMapper processoDetalheMapper = mock(ProcessoDetalheMapper.class);
    private SubprocessoMapper subprocessoMapper = mock(SubprocessoMapper.class);
    private MapaRepo mapaRepo = mock(MapaRepo.class);
    private SubprocessoMovimentacaoRepo movimentacaoRepo = mock(SubprocessoMovimentacaoRepo.class);
    private CopiaMapaService copiaMapaService = mock(CopiaMapaService.class);
    private ProcessoNotificacaoService processoNotificacaoService =
            mock(ProcessoNotificacaoService.class);
    private SgrhService sgrhService = mock(SgrhService.class);

    private ProcessoService service =
            new ProcessoService(
                    processoRepo,
                    unidadeRepo,
                    subprocessoRepo,
                    publicadorEventos,
                    processoMapper,
                    processoDetalheMapper,
                    subprocessoMapper,
                    mapaRepo,
                    movimentacaoRepo,
                    copiaMapaService,
                    processoNotificacaoService,
                    sgrhService);

    @Property
    void criarDeveLancarErroSeDescricaoForVazia(
            @ForAll("descricaoInvalida") String descricaoInvalida, @ForAll TipoProcesso tipo) {
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao(descricaoInvalida);
        req.setTipo(tipo);
        req.setUnidades(List.of(1L));

        Throwable thrown = catchThrowable(() -> service.criar(req));
        assertThat(thrown)
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("A descrição do processo é obrigatória");
    }

    @Property
    void criarDeveLancarErroSeUnidadesVazias(
            @ForAll("descricaoValida") String descricao, @ForAll TipoProcesso tipo) {
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao(descricao);
        req.setTipo(tipo);
        req.setUnidades(new ArrayList<>()); // Empty list

        Throwable thrown = catchThrowable(() -> service.criar(req));
        assertThat(thrown)
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Pelo menos uma unidade participante deve ser selecionada");
    }

    @Property
    void criarMapeamentoDeveSucesso(
            @ForAll("descricaoValida") String descricao,
            @ForAll("listaUnidades") List<Long> unidadesIds) {
        // Setup
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao(descricao);
        req.setTipo(TipoProcesso.MAPEAMENTO);
        req.setUnidades(unidadesIds);

        // Mock UnidadeRepo
        for (Long id : unidadesIds) {
            Unidade u = new Unidade();
            u.setCodigo(id);
            when(unidadeRepo.findById(id)).thenReturn(Optional.of(u));
        }

        // Mock ProcessoRepo save
        when(processoRepo.saveAndFlush(any(Processo.class)))
                .thenAnswer(
                        i -> {
                            Processo p = i.getArgument(0);
                            p.setCodigo(100L); // simulate DB id
                            return p;
                        });

        when(processoMapper.toDto(any(Processo.class))).thenReturn(ProcessoDto.builder().build());

        ProcessoDto result = service.criar(req);
        assertThat(result).isNotNull();
    }

    @Property
    void criarRevisaoDeveFalharSeUnidadeSemMapa(
            @ForAll("descricaoValida") String descricao,
            @ForAll("listaUnidades") List<Long> unidadesIds) {
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao(descricao);
        req.setTipo(TipoProcesso.REVISAO);
        req.setUnidades(unidadesIds);

        // Mock Units WITHOUT Map
        List<Unidade> unidades = new ArrayList<>();
        for (Long id : unidadesIds) {
            Unidade u = new Unidade();
            u.setCodigo(id);
            u.setSigla("U" + id);
            // No MapaVigente set -> null
            unidades.add(u);
            when(unidadeRepo.findById(id)).thenReturn(Optional.of(u));
        }
        // Needed for the bulk check inside service
        when(unidadeRepo.findAllById(anyList())).thenReturn(unidades);
        when(unidadeRepo.findSiglasByCodigos(anyList()))
                .thenReturn(unidadesIds.stream().map(id -> "U" + id).toList());

        Throwable thrown = catchThrowable(() -> service.criar(req));
        assertThat(thrown)
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("não possuem mapa vigente");
    }

    @Provide
    Arbitrary<String> descricaoInvalida() {
        return Arbitraries.of(null, "", "   ", "\t", "\n");
    }

    @Provide
    Arbitrary<String> descricaoValida() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(100);
    }

    @Provide
    Arbitrary<List<Long>> listaUnidades() {
        return Arbitraries.longs().between(1, 1000).list().ofMinSize(1).ofMaxSize(10);
    }
}
