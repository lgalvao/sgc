package sgc.subprocesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.mapper.AnaliseMapper;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para SubprocessoValidacaoController")
class SubprocessoValidacaoControllerTest {

    @InjectMocks
    private SubprocessoValidacaoController controller;

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private sgc.analise.AnaliseFacade analiseFacade;

    @Mock
    private AnaliseMapper analiseMapper;

    @Test
    @DisplayName("Deve disponibilizar mapa")
    void deveDisponibilizarMapa() {
        Long codigo = 1L;
        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(null, "Obs");
        Usuario usuario = new Usuario();

        ResponseEntity<MensagemResponse> response = controller.disponibilizarMapa(codigo, req, usuario);

        verify(subprocessoFacade).disponibilizarMapa(eq(codigo), any(DisponibilizarMapaRequest.class), eq(usuario));
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Mapa de competências disponibilizado.");
    }

    @Test
    @DisplayName("Deve apresentar sugestões")
    void deveApresentarSugestoes() {
        Long codigo = 1L;
        ApresentarSugestoesRequest req = new ApresentarSugestoesRequest("Sugestão");
        Usuario usuario = new Usuario();

        controller.apresentarSugestoes(codigo, req, usuario);

        verify(subprocessoFacade).apresentarSugestoes(codigo, "Sugestão", usuario);
    }

    @Test
    @DisplayName("Deve validar mapa")
    void deveValidarMapa() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        controller.validarMapa(codigo, usuario);

        verify(subprocessoFacade).validarMapa(codigo, usuario);
    }

    @Test
    @DisplayName("Deve obter sugestões")
    void deveObterSugestoes() {
        Long codigo = 1L;
        SugestoesDto dto = new SugestoesDto("Texto", true, "Unidade");
        when(subprocessoFacade.obterSugestoes(codigo)).thenReturn(dto);

        SugestoesDto result = controller.obterSugestoes(codigo);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Deve obter histórico de validação")
    void deveObterHistoricoValidacao() {
        Long codigo = 1L;
        Analise analise = new Analise();
        AnaliseValidacaoHistoricoDto dto = new AnaliseValidacaoHistoricoDto(null, null, null, null, null, null, null);

        when(analiseFacade.listarPorSubprocesso(codigo, TipoAnalise.VALIDACAO)).thenReturn(List.of(analise));
        when(analiseMapper.toAnaliseValidacaoHistoricoDto(analise)).thenReturn(dto);

        List<AnaliseValidacaoHistoricoDto> result = controller.obterHistoricoValidacao(codigo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    @DisplayName("Deve devolver validação")
    void deveDevolverValidacao() {
        Long codigo = 1L;
        DevolverValidacaoRequest req = new DevolverValidacaoRequest("Justificativa");
        Usuario usuario = new Usuario();

        controller.devolverValidacao(codigo, req, usuario);

        verify(subprocessoFacade).devolverValidacao(codigo, "Justificativa", usuario);
    }

    @Test
    @DisplayName("Deve aceitar validação")
    void deveAceitarValidacao() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        controller.aceitarValidacao(codigo, usuario);

        verify(subprocessoFacade).aceitarValidacao(codigo, usuario);
    }

    @Test
    @DisplayName("Deve homologar validação")
    void deveHomologarValidacao() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        controller.homologarValidacao(codigo, usuario);

        verify(subprocessoFacade).homologarValidacao(codigo, usuario);
    }

    @Test
    @DisplayName("Deve submeter mapa ajustado")
    void deveSubmeterMapaAjustado() {
        Long codigo = 1L;
        SubmeterMapaAjustadoRequest req = new SubmeterMapaAjustadoRequest("Obs", null);
        Usuario usuario = new Usuario();

        controller.submeterMapaAjustado(codigo, req, usuario);

        verify(subprocessoFacade).submeterMapaAjustado(codigo, req, usuario);
    }

    @Test
    @DisplayName("Deve aceitar validação em bloco")
    void deveAceitarValidacaoEmBloco() {
        Long codigo = 1L;
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest(List.of(10L, 20L), null);
        Usuario usuario = new Usuario();

        controller.aceitarValidacaoEmBloco(codigo, req, usuario);

        verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(10L, 20L), codigo, usuario);
    }

    @Test
    @DisplayName("Deve homologar validação em bloco")
    void deveHomologarValidacaoEmBloco() {
        Long codigo = 1L;
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest(List.of(10L, 20L), null);
        Usuario usuario = new Usuario();

        controller.homologarValidacaoEmBloco(codigo, req, usuario);

        verify(subprocessoFacade).homologarValidacaoEmBloco(List.of(10L, 20L), codigo, usuario);
    }
}
