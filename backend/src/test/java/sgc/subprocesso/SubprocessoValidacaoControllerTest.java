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
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.comum.ComumDtos.JustificativaRequest;
import sgc.comum.ComumDtos.TextoRequest;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para SubprocessoValidacaoController")
class SubprocessoValidacaoControllerTest {

    @InjectMocks
    private SubprocessoValidacaoController controller;

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private AnaliseFacade analiseFacade;

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
        when(analiseFacade.listarHistoricoValidacao(codigo)).thenReturn(List.of());

        List<AnaliseHistoricoDto> result = controller.obterHistoricoValidacao(codigo);

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

        verify(subprocessoFacade).aceitarValidacaoEmBloco(subprocessos, usuario);
    }

    @Test
    @DisplayName("Deve homologar validação em bloco")
    void deveHomologarValidacaoEmBloco() {
        Long codigo = 100L;
        List<Long> subprocessos = List.of(1L, 2L);
        ProcessarEmBlocoRequest req = new ProcessarEmBlocoRequest("HOMOLOGAR", subprocessos, null);
        Usuario usuario = new Usuario();

        controller.homologarValidacaoEmBloco(codigo, req, usuario);

        verify(subprocessoFacade).homologarValidacaoEmBloco(subprocessos, usuario);
    }
}
