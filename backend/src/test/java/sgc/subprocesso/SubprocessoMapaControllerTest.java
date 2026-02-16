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
import sgc.analise.mapper.AnaliseMapper;
import sgc.analise.model.TipoAnalise;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.SubprocessoFacade;

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
        assertThat(response.getBody().mensagem()).isEqualTo("Mapa de competências disponibilizado.");
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
        Map<String, Object> dto = Map.of("sugestoes", "Texto");
        when(subprocessoFacade.obterSugestoes(codigo)).thenReturn(dto);

        Map<String, Object> result = controller.obterSugestoes(codigo);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    @DisplayName("Deve obter histórico de validação")
    void deveObterHistoricoValidacao() {
        Long codigo = 1L;
        when(analiseFacade.listarPorSubprocesso(eq(codigo), eq(TipoAnalise.VALIDACAO))).thenReturn(List.of());

        List<AnaliseValidacaoHistoricoDto> result = controller.obterHistoricoValidacao(codigo);

        assertThat(result).isEmpty();
    }
}
