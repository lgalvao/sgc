package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para SubprocessoAjusteMapaService.
 * Foca em cobrir branches e cenários de erro não cobertos.
 */
@Tag("unit")
@DisplayName("SubprocessoAjusteMapaService")
@ExtendWith(MockitoExtension.class)
class SubprocessoAjusteMapaServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    
    @Mock
    private SubprocessoCrudService crudService;
    
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    
    @Mock
    private AnaliseFacade analiseFacade;
    
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;

    @Mock
    private ComumRepo repo;

    @InjectMocks
    private SubprocessoAjusteMapaService service;

    @Nested
    @DisplayName("salvarAjustesMapa")
    class SalvarAjustesMapaTests {

        @Test
        @DisplayName("deve salvar ajustes com sucesso")
        void deveSalvarAjustesComSucesso() {
            // Arrange
            Long codSubprocesso = 1L;
            
            Subprocesso sp = Subprocesso.builder()
                    .codigo(codSubprocesso)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                    .build();
            
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
            
            Subprocesso sp = Subprocesso.builder()
                    .codigo(codSubprocesso)
                    .situacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO)
                    .build();
            
            AtividadeAjusteDto atividadeDto = new AtividadeAjusteDto(1L, "Atividade 1", Collections.emptyList());
            CompetenciaAjusteDto competenciaDto = CompetenciaAjusteDto.builder()
                    .codCompetencia(100L)
                    .nome("Competencia 1")
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
            
            Subprocesso sp = Subprocesso.builder()
                    .codigo(codSubprocesso)
                    .situacao(SituacaoSubprocesso.NAO_INICIADO)
                    .build();
            
            when(repo.buscar(Subprocesso.class, codSubprocesso)).thenReturn(sp);
            
            // Act & Assert
            List<CompetenciaAjusteDto> ajustes = Collections.emptyList();
            assertThatThrownBy(() -> service.salvarAjustesMapa(codSubprocesso, ajustes))
                    .isInstanceOf(ErroMapaEmSituacaoInvalida.class)
                    .hasMessageContaining("Ajustes no mapa só podem ser feitos em estados específicos");
        }

        @Test
        @DisplayName("deve processar competencias vazias sem erro")
        void deveProcessarCompetenciasVaziasSemErro() {
            // Arrange
            Long codSubprocesso = 1L;
            
            Subprocesso sp = Subprocesso.builder()
                    .codigo(codSubprocesso)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                    .build();
            
            when(repo.buscar(Subprocesso.class, codSubprocesso)).thenReturn(sp);
            
            // Act
            service.salvarAjustesMapa(codSubprocesso, Collections.emptyList());
            
            // Assert
            verify(subprocessoRepo).save(any(Subprocesso.class));
            verify(mapaManutencaoService, never()).atualizarDescricoesAtividadeEmLote(anyMap());
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
            Subprocesso sp = Subprocesso.builder()
                    .codigo(codSubprocesso)
                    .mapa(mapa)
                    .build();
            
            Analise analise = new Analise();
            analise.setCodigo(1L);
            
            List<Competencia> competencias = List.of(Competencia.builder().codigo(1L).build());
            List<Atividade> atividades = List.of(Atividade.builder().codigo(1L).build());
            List<Conhecimento> conhecimentos = List.of(Conhecimento.builder().codigo(1L).build());
            Map<Long, Set<Long>> associacoes = Map.of(1L, Set.of(1L));
            
            MapaAjusteDto expected = MapaAjusteDto.builder().build();
            
            when(crudService.buscarSubprocessoComMapa(codSubprocesso)).thenReturn(sp);
            when(analiseFacade.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO))
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
            Subprocesso sp = Subprocesso.builder()
                    .codigo(codSubprocesso)
                    .mapa(mapa)
                    .build();
            
            when(crudService.buscarSubprocessoComMapa(codSubprocesso)).thenReturn(sp);
            when(analiseFacade.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO))
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

    @Nested
    @DisplayName("atualizarCompetenciasEAssociacoes internal scenarios")
    class AtualizarCompetenciasInterno {
        @Test
        @DisplayName("deve ignorar competencia nao encontrada no mapa")
        void deveIgnorarCompetenciaNaoEncontrada() {
            // Arrange
            Long codSubprocesso = 1L;
            Subprocesso sp = Subprocesso.builder()
                    .codigo(codSubprocesso)
                    .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                    .build();
            
            CompetenciaAjusteDto compDto = CompetenciaAjusteDto.builder()
                    .codCompetencia(100L)
                    .atividades(Collections.emptyList())
                    .build();
            
            when(repo.buscar(Subprocesso.class, codSubprocesso)).thenReturn(sp);
            // Return empty list for competencies to trigger (competencia == null)
            when(mapaManutencaoService.buscarCompetenciasPorCodigos(anyList())).thenReturn(Collections.emptyList());
            when(mapaManutencaoService.buscarAtividadesPorCodigos(anyList())).thenReturn(Collections.emptyList());
            
            // Act
            service.salvarAjustesMapa(codSubprocesso, List.of(compDto));
            
            // Assert
            verify(mapaManutencaoService).salvarTodasCompetencias(argThat(List::isEmpty));
        }
    }
}
