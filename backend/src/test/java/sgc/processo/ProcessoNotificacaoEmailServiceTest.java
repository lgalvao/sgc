package sgc.processo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoModelosService;
import sgc.processo.model.Processo;
import sgc.processo.model.UnidadeProcesso;
import sgc.processo.service.ProcessoNotificacaoService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;
import sgc.unidade.model.TipoUnidade;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoNotificacaoEmailServiceTest {

    @InjectMocks
    private ProcessoNotificacaoService service;

    @Mock
    private NotificacaoEmailService notificacaoEmailService;
    @Mock
    private NotificacaoModelosService notificacaoModelosService;
    @Mock
    private SgrhService sgrhService;

    private Processo processo;
    private UnidadeProcesso unidadeProcesso;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        unidadeProcesso = new UnidadeProcesso();
    }

    @Nested
    @DisplayName("Testes para enviarNotificacoesDeFinalizacao")
    class EnviarNotificacoesDeFinalizacaoTests {
        @Test
        @DisplayName("Deve enviar e-mail para unidade final")
        void enviarNotificacoesDeFinalizacao_UnidadeFinal_EnviaEmail() {
            unidadeProcesso.setTipo(TipoUnidade.OPERACIONAL);
            unidadeProcesso.setCodUnidade(1L);

            when(sgrhService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(1L, new ResponsavelDto(1L, "123", "Titular", "456", "Substituto")));
            when(sgrhService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", new UsuarioDto("123", "Nome", "email@test.com", "mat", "cargo")));
            when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("html");

            service.enviarNotificacoesDeFinalizacao(processo, Collections.singletonList(unidadeProcesso));

            verify(notificacaoEmailService).enviarEmailHtml(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve enviar e-mail para unidade intermediária")
        void enviarNotificacoesDeFinalizacao_UnidadeIntermediaria_EnviaEmail() {
            unidadeProcesso.setTipo(TipoUnidade.INTERMEDIARIA);
            unidadeProcesso.setCodUnidade(1L);
            UnidadeProcesso subordinada = new UnidadeProcesso();
            subordinada.setCodUnidadeSuperior(1L);

            when(sgrhService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(1L, new ResponsavelDto(1L, "123", "Titular", "456", "Substituto")));
            when(sgrhService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("123", new UsuarioDto("123", "Nome", "email@test.com", "mat", "cargo")));
            when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(any(), any(), any())).thenReturn("html");

            service.enviarNotificacoesDeFinalizacao(processo, List.of(unidadeProcesso, subordinada));

            verify(notificacaoEmailService).enviarEmailHtml(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Não deve enviar e-mail se não houver unidades participantes")
        void enviarNotificacoesDeFinalizacao_SemUnidades_NaoEnviaEmail() {
            service.enviarNotificacoesDeFinalizacao(processo, Collections.emptyList());
            verify(notificacaoEmailService, never()).enviarEmailHtml(anyString(), anyString(), anyString());
        }
    }
}
