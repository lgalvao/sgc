package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.HierarquiaService;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.AnaliseRepo;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.organizacao.service.UnidadeService;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.subprocesso.model.MovimentacaoRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoConsultaService - Cobertura de Testes")
class SubprocessoConsultaServiceCoverageTest {

    @InjectMocks
    private SubprocessoConsultaService target;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private AnaliseRepo analiseRepo;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private ImpactoMapaService impactoMapaService;

    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private HierarquiaService hierarquiaService;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;

    @Test
    @DisplayName("Deve converter análise para DTO de histórico chamando carregarUnidades")
    void deveConverterParaHistoricoDto() {
        Long codUnidade = 1L;
        Analise analise = Analise.builder()
                .codigo(10L)
                .unidadeCodigo(codUnidade)
                .dataHora(LocalDateTime.now())
                .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .tipo(TipoAnalise.VALIDACAO)
                .usuarioTitulo("123456789012") // TituloEleitoral (12 chars)
                .build();

        Unidade unidade = Unidade.builder()
                .codigo(codUnidade)
                .sigla("SIGLA")
                .nome("Nome da Unidade")
                .build();

        when(unidadeService.buscarPorCodigos(List.of(codUnidade))).thenReturn(List.of(unidade));

        AnaliseHistoricoDto dto = target.paraHistoricoDto(analise);

        assertThat(dto).isNotNull();
        assertThat(dto.unidadeSigla()).isEqualTo("SIGLA");
        verify(unidadeService).buscarPorCodigos(List.of(codUnidade));
    }

    @Test
    @DisplayName("Deve lançar exceção quando unidade está ausente no histórico")
    void deveLancaoExcecaoUnidadeAusente() {
        Long codUnidade = 1L;
        Analise analise = Analise.builder()
                .unidadeCodigo(codUnidade)
                .build();

        // Fazemos unidadeService retornar lista vazia para simular ausência
        when(unidadeService.buscarPorCodigos(List.of(codUnidade))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> target.paraHistoricoDto(analise))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unidade 1 ausente no histórico de análises");
    }

    @Test
    @DisplayName("Deve retornar mapa vazio quando lista de análises para carregar unidades for vazia")
    void deveRetornarMapaVazioParaAnalisesVazias() {
        Long codSubprocesso = 100L;
        when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso))
                .thenReturn(Collections.emptyList());

        List<AnaliseHistoricoDto> resultado = target.listarHistoricoValidacao(codSubprocesso);

        assertThat(resultado).isEmpty();
        verify(unidadeService, never()).buscarPorCodigos(anyList());
    }
}
