package sgc.subprocesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.model.TipoAnalise;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.model.Usuario;
import sgc.comum.dto.ComumDtos.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para SubprocessoMapaController")
class SubprocessoMapaControllerTest {

    @InjectMocks
    private SubprocessoMapaController controller;

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private MapaFacade mapaFacade;

    @Mock
    private AnaliseFacade analiseFacade;

    @Test
    @DisplayName("Deve verificar impactos")
    void deveVerificarImpactos() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();
        Subprocesso sp = new Subprocesso();
        ImpactoMapaResponse impacto = ImpactoMapaResponse.builder().build();
        
        when(subprocessoFacade.buscarSubprocesso(codigo)).thenReturn(sp);
        when(mapaFacade.verificarImpactos(sp, usuario)).thenReturn(impacto);

        ImpactoMapaResponse result = controller.verificarImpactos(codigo, usuario);

        assertThat(result).isEqualTo(impacto);
    }

    @Test
    @DisplayName("Deve obter mapa")
    void deveObterMapa() {
        Long codigo = 1L;
        Mapa mapa = new Mapa();
        Subprocesso sp = Subprocesso.builder().mapa(mapa).build();
        
        when(subprocessoFacade.buscarSubprocessoComMapa(codigo)).thenReturn(sp);

        Mapa result = controller.obterMapa(codigo);

        assertThat(result).isEqualTo(mapa);
    }

    @Test
    @DisplayName("Deve disponibilizar mapa")
    void deveDisponibilizarMapa() {
        Long codigo = 1L;
        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(null, "Obs");
        Usuario usuario = new Usuario();

        ResponseEntity<MensagemResponse> response = controller.disponibilizarMapa(codigo, req, usuario);

        verify(subprocessoFacade).disponibilizarMapa(eq(codigo), any(DisponibilizarMapaRequest.class), eq(usuario));
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().mensagem()).isEqualTo("Mapa de competências disponibilizado.");
    }

    @Test
    @DisplayName("Deve obter mapa para visualização")
    void deveObterMapaParaVisualizacao() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        MapaVisualizacaoResponse vis = MapaVisualizacaoResponse.builder().build();
        
        when(subprocessoFacade.buscarSubprocesso(codigo)).thenReturn(sp);
        when(mapaFacade.obterMapaParaVisualizacao(sp)).thenReturn(vis);

        MapaVisualizacaoResponse result = controller.obterMapaParaVisualizacao(codigo);

        assertThat(result).isEqualTo(vis);
    }

    @Test
    @DisplayName("Deve salvar mapa")
    void deveSalvarMapa() {
        Long codigo = 1L;
        SalvarMapaRequest req = SalvarMapaRequest.builder().build();
        Mapa mapa = new Mapa();
        
        when(subprocessoFacade.salvarMapaSubprocesso(codigo, req)).thenReturn(mapa);

        Mapa result = controller.salvarMapa(codigo, req);

        assertThat(result).isEqualTo(mapa);
    }

    @Test
    @DisplayName("Deve obter mapa completo")
    void deveObterMapaCompleto() {
        Long codigo = 1L;
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        Subprocesso sp = Subprocesso.builder().mapa(mapa).build();
        
        when(subprocessoFacade.buscarSubprocessoComMapa(codigo)).thenReturn(sp);
        when(mapaFacade.obterPorCodigo(100L)).thenReturn(mapa);

        ResponseEntity<Mapa> response = controller.obterMapaCompleto(codigo);

        assertThat(response.getBody()).isEqualTo(mapa);
    }

    @Test
    @DisplayName("Deve salvar mapa completo")
    void deveSalvarMapaCompleto() {
        Long codigo = 1L;
        SalvarMapaRequest req = SalvarMapaRequest.builder().build();
        Mapa mapa = new Mapa();
        
        when(subprocessoFacade.salvarMapaSubprocesso(codigo, req)).thenReturn(mapa);

        ResponseEntity<Mapa> response = controller.salvarMapaCompleto(codigo, req);

        assertThat(response.getBody()).isEqualTo(mapa);
    }

    @Test
    @DisplayName("Deve apresentar sugestões")
    void deveApresentarSugestoes() {
        Long codigo = 1L;
        TextoRequest req = new TextoRequest("Sugestão");
        Usuario usuario = new Usuario();

        controller.apresentarSugestoes(codigo, req, usuario);

        verify(subprocessoFacade).apresentarSugestoes(codigo, "Sugestão", usuario);
    }

    @Test
    @DisplayName("Deve obter sugestões")
    void deveObterSugestoes() {
        Long codigo = 1L;
        Map<String, Object> dto = Map.of("sugestoes", "Texto");
        when(subprocessoFacade.obterSugestoes(codigo)).thenReturn(dto);

        Map<String, Object> result = controller.obterSugestoes(codigo);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Deve obter histórico de validação")
    void deveObterHistoricoValidacao() {
        Long codigo = 1L;
        when(analiseFacade.listarPorSubprocesso(codigo, TipoAnalise.VALIDACAO)).thenReturn(List.of());

        List<AnaliseValidacaoHistoricoDto> result = controller.obterHistoricoValidacao(codigo);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve validar mapa")
    void deveValidarMapa() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        ResponseEntity<Void> response = controller.validarMapa(codigo, usuario);

        verify(subprocessoFacade).validarMapa(codigo, usuario);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("Deve devolver validação")
    void deveDevolverValidacao() {
        Long codigo = 1L;
        JustificativaRequest req = new JustificativaRequest("Justificativa");
        Usuario usuario = new Usuario();

        ResponseEntity<Void> response = controller.devolverValidacao(codigo, req, usuario);

        verify(subprocessoFacade).devolverValidacao(codigo, "Justificativa", usuario);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("Deve aceitar validação")
    void deveAceitarValidacao() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        ResponseEntity<Void> response = controller.aceitarValidacao(codigo, usuario);

        verify(subprocessoFacade).aceitarValidacao(codigo, usuario);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("Deve homologar validação")
    void deveHomologarValidacao() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        ResponseEntity<Void> response = controller.homologarValidacao(codigo, usuario);

        verify(subprocessoFacade).homologarValidacao(codigo, usuario);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("Deve submeter mapa ajustado")
    void deveSubmeterMapaAjustado() {
        Long codigo = 1L;
        SubmeterMapaAjustadoRequest req = SubmeterMapaAjustadoRequest.builder().build();
        Usuario usuario = new Usuario();

        ResponseEntity<Void> response = controller.submeterMapaAjustado(codigo, req, usuario);

        verify(subprocessoFacade).submeterMapaAjustado(codigo, req, usuario);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("Deve aceitar validação em bloco")
    void deveAceitarValidacaoEmBloco() {
        Long codigo = 100L;
        List<Long> subprocessos = List.of(1L, 2L);
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("ACEITAR", subprocessos, null);
        Usuario usuario = new Usuario();

        controller.aceitarValidacaoEmBloco(codigo, req, usuario);

        verify(subprocessoFacade).aceitarValidacaoEmBloco(subprocessos, codigo, usuario);
    }

    @Test
    @DisplayName("Deve homologar validação em bloco")
    void deveHomologarValidacaoEmBloco() {
        Long codigo = 100L;
        List<Long> subprocessos = List.of(1L, 2L);
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("HOMOLOGAR", subprocessos, null);
        Usuario usuario = new Usuario();

        controller.homologarValidacaoEmBloco(codigo, req, usuario);

        verify(subprocessoFacade).homologarValidacaoEmBloco(subprocessos, codigo, usuario);
    }

    @Test
    @DisplayName("Deve disponibilizar mapa em bloco")
    void deveDisponibilizarMapaEmBloco() {
        Long codigo = 100L;
        List<Long> subprocessos = List.of(1L, 2L);
        LocalDate data = LocalDate.now().plusDays(10);
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("DISPONIBILIZAR", subprocessos, data);
        Usuario usuario = new Usuario();

        controller.disponibilizarMapaEmBloco(codigo, req, usuario);

        verify(subprocessoFacade).disponibilizarMapaEmBloco(subprocessos, codigo, any(DisponibilizarMapaRequest.class), usuario);
    }

    @Test
    @DisplayName("Deve obter mapa para ajuste")
    void deveObterMapaParaAjuste() {
        Long codigo = 1L;
        MapaAjusteDto dto = MapaAjusteDto.builder().build();
        when(subprocessoFacade.obterMapaParaAjuste(codigo)).thenReturn(dto);

        MapaAjusteDto result = controller.obterMapaParaAjuste(codigo);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Deve salvar ajustes mapa")
    void deveSalvarAjustesMapa() {
        Long codigo = 1L;
        SalvarAjustesRequest req = new SalvarAjustesRequest(List.of());

        controller.salvarAjustesMapa(codigo, req);

        verify(subprocessoFacade).salvarAjustesMapa(codigo, List.of());
    }

    @Test
    @DisplayName("Deve adicionar competência")
    void deveAdicionarCompetencia() {
        Long codigo = 1L;
        CompetenciaRequest req = CompetenciaRequest.builder().build();
        Mapa mapa = new Mapa();
        when(subprocessoFacade.adicionarCompetencia(codigo, req)).thenReturn(mapa);

        ResponseEntity<Mapa> response = controller.adicionarCompetencia(codigo, req);

        assertThat(response.getBody()).isEqualTo(mapa);
    }

    @Test
    @DisplayName("Deve atualizar competência")
    void deveAtualizarCompetencia() {
        Long codigo = 1L;
        Long codComp = 10L;
        CompetenciaRequest req = CompetenciaRequest.builder().build();
        Mapa mapa = new Mapa();
        when(subprocessoFacade.atualizarCompetencia(codigo, codComp, req)).thenReturn(mapa);

        ResponseEntity<Mapa> response = controller.atualizarCompetencia(codigo, codComp, req);

        assertThat(response.getBody()).isEqualTo(mapa);
    }

    @Test
    @DisplayName("Deve remover competência")
    void deveRemoverCompetencia() {
        Long codigo = 1L;
        Long codComp = 10L;
        Mapa mapa = new Mapa();
        when(subprocessoFacade.removerCompetencia(codigo, codComp)).thenReturn(mapa);

        ResponseEntity<Mapa> response = controller.removerCompetencia(codigo, codComp);

        assertThat(response.getBody()).isEqualTo(mapa);
    }
}
