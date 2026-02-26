package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.erros.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SubprocessoService - Ajuste Mapa")
@ExtendWith(MockitoExtension.class)
class SubprocessoServiceAjusteTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private CopiaMapaService copiaMapaService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;
    @InjectMocks
    private SubprocessoService service;

    @BeforeEach
    void setup() {
        service.setMapaManutencaoService(mapaManutencaoService);
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMovimentacaoRepo(movimentacaoRepo);
        service.setCopiaMapaService(copiaMapaService);
    }

    private Subprocesso criarSubprocesso(Long codigo) {
        return Subprocesso.builder()
                .codigo(codigo)
                .processo(sgc.processo.model.Processo.builder().codigo(10L).build())
                .unidade(sgc.organizacao.model.Unidade.builder().codigo(20L).build())
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                .build();
    }

    @Nested
    @DisplayName("salvarAjustesMapa")
    class SalvarAjustesMapaTests {
        @Test
        @DisplayName("deve salvar ajustes com sucesso")
        void deveSalvarAjustesComSucesso() {
            // Arrange
            Long codSubprocesso = 1L;
            Subprocesso sp = criarSubprocesso(codSubprocesso);

            AtividadeAjusteDto atividadeDto = new AtividadeAjusteDto(1L, "Atividade 1", Collections.emptyList());
            CompetenciaAjusteDto competenciaDto = CompetenciaAjusteDto.builder()
                    .codCompetencia(100L)
                    .nome("Competencia 1")
                    .atividades(List.of(atividadeDto))
                    .build();

            List<CompetenciaAjusteDto> competencias = List.of(competenciaDto);

            Competencia competenciaEntity = Competencia.builder()
                    .codigo(100L)
                    .descricao("Competencia 1")
                    .build();

            Atividade atividadeEntity = Atividade.builder()
                    .codigo(1L)
                    .descricao("Atividade 1")
                    .build();

            when(repo.buscar(Subprocesso.class, codSubprocesso)).thenReturn(sp);
            when(mapaManutencaoService.buscarCompetenciasPorCodigos(anyList()))
                    .thenReturn(List.of(competenciaEntity));
            when(mapaManutencaoService.buscarAtividadesPorCodigos(anyList()))
                    .thenReturn(List.of(atividadeEntity));
            when(subprocessoRepo.save(any(Subprocesso.class))).thenReturn(sp);

            // Act
            service.salvarAjustesMapa(codSubprocesso, competencias);

            // Assert
            verify(repo).buscar(Subprocesso.class, codSubprocesso);
            verify(mapaManutencaoService).atualizarDescricoesAtividadeEmLote(anyMap());
            verify(mapaManutencaoService).salvarTodasCompetencias(anyList());
            verify(subprocessoRepo).save(argThat(s -> s.getSituacao() == SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO));
        }

        @Test
        @DisplayName("deve permitir ajuste quando situacao eh REVISAO_MAPA_AJUSTADO")
        void devePermitirAjusteQuandoSituacaoRevisaoMapaAjustado() {
            // Arrange
            Long codSubprocesso = 1L;
            Subprocesso sp = criarSubprocesso(codSubprocesso);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

            AtividadeAjusteDto atividadeDto = new AtividadeAjusteDto(1L, "Atividade 1", Collections.emptyList());
            CompetenciaAjusteDto competenciaDto = CompetenciaAjusteDto.builder()
                    .codCompetencia(100L)
                    .atividades(List.of(atividadeDto))
                    .build();

            Competencia competenciaEntity = Competencia.builder().codigo(100L).build();
            Atividade atividadeEntity = Atividade.builder().codigo(1L).build();

            when(repo.buscar(Subprocesso.class, codSubprocesso)).thenReturn(sp);
            when(mapaManutencaoService.buscarCompetenciasPorCodigos(anyList())).thenReturn(List.of(competenciaEntity));
            when(mapaManutencaoService.buscarAtividadesPorCodigos(anyList())).thenReturn(List.of(atividadeEntity));

            // Act
            service.salvarAjustesMapa(codSubprocesso, List.of(competenciaDto));

            // Assert
            verify(subprocessoRepo).save(any(Subprocesso.class));
        }

        @Test
        @DisplayName("deve lancar erro quando subprocesso nao encontrado")
        void deveLancarErroQuandoSubprocessoNaoEncontrado() {
            // Arrange
            Long codSubprocesso = 999L;
            when(repo.buscar(Subprocesso.class, codSubprocesso))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

            // Act & Assert
            List<CompetenciaAjusteDto> ajustes = Collections.emptyList();
            assertThatThrownBy(() -> service.salvarAjustesMapa(codSubprocesso, ajustes))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("deve lancar erro quando situacao invalida para ajuste")
        void deveLancarErroQuandoSituacaoInvalidaParaAjuste() {
            // Arrange
            Long codSubprocesso = 1L;
            Subprocesso sp = criarSubprocesso(codSubprocesso);
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

            when(repo.buscar(Subprocesso.class, codSubprocesso)).thenReturn(sp);

            // Act & Assert
            List<CompetenciaAjusteDto> ajustes = Collections.emptyList();
            assertThatThrownBy(() -> service.salvarAjustesMapa(codSubprocesso, ajustes))
                    .isInstanceOf(ErroMapaEmSituacaoInvalida.class)
                    .hasMessageContaining("Ajustes no mapa só podem ser feitos em estados específicos");
        }
    }

    @Nested
    @DisplayName("obterMapaParaAjuste")
    class ObterMapaParaAjusteTests {
        @Test
        @DisplayName("deve obter mapa para ajuste com analise")
        void deveObterMapaParaAjusteComAnalise() {
            // Arrange
            Long codSubprocesso = 1L;
            Long codMapa = 100L;

            Mapa mapa = Mapa.builder().codigo(codMapa).build();
            Subprocesso sp = criarSubprocesso(codSubprocesso);
            sp.setMapa(mapa);

            Analise analise = new Analise();
            analise.setCodigo(1L);
            analise.setTipo(TipoAnalise.VALIDACAO); // Ensure type matches what SubprocessoService looks for

            List<Competencia> competencias = List.of(Competencia.builder().codigo(1L).build());
            List<Atividade> atividades = List.of(Atividade.builder().codigo(1L).build());
            List<Conhecimento> conhecimentos = List.of(Conhecimento.builder().codigo(1L).build());
            Map<Long, Set<Long>> associacoes = Map.of(1L, Set.of(1L));

            MapaAjusteDto expected = MapaAjusteDto.builder().build();

            when(subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso)).thenReturn(Optional.of(sp));
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso))
                    .thenReturn(List.of(analise));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapaSemRelacionamentos(codMapa))
                    .thenReturn(competencias);
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoSemRelacionamentos(codMapa))
                    .thenReturn(atividades);
            when(mapaManutencaoService.listarConhecimentosPorMapa(codMapa))
                    .thenReturn(conhecimentos);
            when(mapaManutencaoService.buscarIdsAssociacoesCompetenciaAtividade(codMapa))
                    .thenReturn(associacoes);
            when(mapaAjusteMapper.toDto(sp, analise, competencias, atividades, conhecimentos, associacoes))
                    .thenReturn(expected);

            // Act
            MapaAjusteDto result = service.obterMapaParaAjuste(codSubprocesso);

            // Assert
            assertThat(result).isEqualTo(expected);
            verify(mapaAjusteMapper).toDto(sp, analise, competencias, atividades, conhecimentos, associacoes);
        }

        @Test
        @DisplayName("deve obter mapa para ajuste sem analise")
        void deveObterMapaParaAjusteSemAnalise() {
            // Arrange
            Long codSubprocesso = 1L;
            Long codMapa = 100L;

            Mapa mapa = Mapa.builder().codigo(codMapa).build();
            Subprocesso sp = criarSubprocesso(codSubprocesso);
            sp.setMapa(mapa);

            when(subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso)).thenReturn(Optional.of(sp));
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso))
                    .thenReturn(Collections.emptyList());
            when(mapaManutencaoService.buscarCompetenciasPorCodMapaSemRelacionamentos(codMapa))
                    .thenReturn(Collections.emptyList());
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoSemRelacionamentos(codMapa))
                    .thenReturn(Collections.emptyList());
            when(mapaManutencaoService.listarConhecimentosPorMapa(codMapa))
                    .thenReturn(Collections.emptyList());
            when(mapaManutencaoService.buscarIdsAssociacoesCompetenciaAtividade(codMapa))
                    .thenReturn(Collections.emptyMap());
            when(mapaAjusteMapper.toDto(eq(sp), isNull(), anyList(), anyList(), anyList(), anyMap()))
                    .thenReturn(MapaAjusteDto.builder().build());

            // Act
            MapaAjusteDto result = service.obterMapaParaAjuste(codSubprocesso);

            // Assert
            assertThat(result).isNotNull();
            verify(mapaAjusteMapper).toDto(eq(sp), isNull(), anyList(), anyList(), anyList(), anyMap());
        }
    }
}
