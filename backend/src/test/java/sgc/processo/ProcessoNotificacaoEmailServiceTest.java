package sgc.processo;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import sgc.processo.service.ProcessoNotificacaoService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;

@ExtendWith(MockitoExtension.class)
class ProcessoNotificacaoEmailServiceTest {
    @InjectMocks private ProcessoNotificacaoService service;

    @Mock private NotificacaoEmailService notificacaoEmailService;

    @Mock private NotificacaoModelosService notificacaoModelosService;

    @Mock private SgrhService sgrhService;

    private Processo processo;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        unidade = new Unidade();
    }

    @Nested
    @DisplayName("Testes para enviarNotificacoesDeFinalizacao")
    class EnviarNotificacoesDeFinalizacaoTests {
        @Test
        @DisplayName("Deve enviar e-mail para unidade final")
        void enviarNotificacoesDeFinalizacao_UnidadeFinal_EnviaEmail() {
            unidade.setTipo(TipoUnidade.OPERACIONAL);
            unidade.setCodigo(1L);

            when(sgrhService.buscarResponsaveisUnidades(any()))
                    .thenReturn(
                            Map.of(
                                    1L,
                                    new ResponsavelDto(1L, "123", "Titular", "456", "Substituto")));
            when(sgrhService.buscarUsuariosPorTitulos(any()))
                    .thenReturn(
                            Map.of(
                                    "123",
                                    new UsuarioDto(
                                            "123", "Nome", "email@test.com", "mat")));
            when(notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any()))
                    .thenReturn("html");

            service.enviarNotificacoesDeFinalizacao(processo, Collections.singletonList(unidade));

            verify(notificacaoEmailService).enviarEmailHtml(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve enviar e-mail para unidade intermediária")
        void enviarNotificacoesDeFinalizacao_UnidadeIntermediaria_EnviaEmail() {
            unidade.setTipo(TipoUnidade.INTERMEDIARIA);
            unidade.setCodigo(1L);
            Unidade subordinada = new Unidade();
            subordinada.setUnidadeSuperior(unidade);

            when(sgrhService.buscarResponsaveisUnidades(any()))
                    .thenReturn(
                            Map.of(
                                    1L,
                                    new ResponsavelDto(1L, "123", "Titular", "456", "Substituto")));
            when(sgrhService.buscarUsuariosPorTitulos(any()))
                    .thenReturn(
                            Map.of(
                                    "123",
                                    new UsuarioDto(
                                            "123", "Nome", "email@test.com", "mat")));
            when(notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(
                            any(), any(), any()))
                    .thenReturn("html");

            service.enviarNotificacoesDeFinalizacao(processo, List.of(unidade, subordinada));

            verify(notificacaoEmailService).enviarEmailHtml(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Não deve enviar e-mail se não houver unidades participantes")
        void enviarNotificacoesDeFinalizacao_SemUnidades_NaoEnviaEmail() {
            service.enviarNotificacoesDeFinalizacao(processo, Collections.emptyList());
            verify(notificacaoEmailService, never())
                    .enviarEmailHtml(anyString(), anyString(), anyString());
        }
    }
}
