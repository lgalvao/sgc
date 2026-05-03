package sgc.feedback;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.mock.web.*;
import sgc.comum.erros.*;
import sgc.feedback.dto.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import tools.jackson.databind.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackService")
class FeedbackServiceTest {

    @Mock
    private FeedbackRepo repo;

    @Mock
    private UsuarioFacade usuarioFacade;

    private FeedbackService service;
    private FeedbackPropriedades propriedades;
    private ObjectMapper objectMapper;

    private Usuario usuarioMock;

    @BeforeEach
    void configurar() throws IOException {
        Path dirTemp = Files.createTempDirectory("sgc-feedback-test-");
        propriedades = new FeedbackPropriedades(dirTemp.toString(), 5_242_880L);
        objectMapper = new ObjectMapper();
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper);

        usuarioMock = new Usuario();
        usuarioMock.setTituloEleitoral("12345");
        usuarioMock.setNome("João Testador");
    }

    private void configurarUsuarioMock() {
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuarioMock);
    }

    @Test
    @DisplayName("deve registrar feedback sem screenshot com sucesso")
    void deveRegistrarFeedbackSemScreenshot() {
        configurarUsuarioMock();
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Encontrei um problema ao salvar o formulário", null);
        var registroSalvo = FeedbackRegistro.builder()
                .id(UUID.randomUUID())
                .tipo(FeedbackTipo.BUG)
                .nota(payload.nota())
                .usuarioId(usuarioMock.getTituloEleitoral())
                .usuarioNome(usuarioMock.getNome())
                .rota("/desconhecido")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.save(any())).thenReturn(registroSalvo);

        FeedbackRespostaDto resposta = service.registrar(payload, null);

        assertThat(resposta.id()).isEqualTo(registroSalvo.getId());
        verify(repo).save(argThat(r ->
                r.getTipo() == FeedbackTipo.BUG &&
                r.getNota().equals(payload.nota()) &&
                r.getUsuarioId().equals("12345") &&
                r.getStatus() == FeedbackStatus.NOVO
        ));
    }

    @Test
    @DisplayName("deve salvar screenshot no diretório configurado")
    void deveSalvarScreenshot() throws IOException {
        configurarUsuarioMock();
        var payload = new FeedbackPayloadDto(FeedbackTipo.SUGESTAO, "Sugestão com captura de tela inclusa", null);
        byte[] imagemFake = "fake-webp-content".getBytes();
        MockMultipartFile screenshot = new MockMultipartFile("screenshot", "screenshot.webp", "image/webp", imagemFake);

        var registroSalvo = FeedbackRegistro.builder()
                .id(UUID.randomUUID())
                .tipo(FeedbackTipo.SUGESTAO)
                .nota(payload.nota())
                .usuarioId(usuarioMock.getTituloEleitoral())
                .usuarioNome(usuarioMock.getNome())
                .rota("/desconhecido")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.save(any())).thenReturn(registroSalvo);

        service.registrar(payload, screenshot);

        verify(repo).save(argThat(r -> r.getCaminhoScreenshot() != null && r.getCaminhoScreenshot().endsWith(".webp")));
    }

    @Test
    @DisplayName("deve extrair rota dos metadados quando presente")
    void deveExtrairRotaDosMetadados() throws Exception {
        configurarUsuarioMock();
        JsonNode metadados = objectMapper.readTree("{\"rotaCaminho\": \"/processos/42\"}");
        var payload = new FeedbackPayloadDto(FeedbackTipo.QUESTAO, "Dúvida sobre esta tela específica", metadados);
        var registroSalvo = FeedbackRegistro.builder()
                .id(UUID.randomUUID())
                .tipo(FeedbackTipo.QUESTAO)
                .nota(payload.nota())
                .usuarioId(usuarioMock.getTituloEleitoral())
                .usuarioNome(usuarioMock.getNome())
                .rota("/processos/42")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.save(any())).thenReturn(registroSalvo);

        service.registrar(payload, null);

        verify(repo).save(argThat(r -> "/processos/42".equals(r.getRota())));
    }

    @Test
    @DisplayName("deve lançar ErroValidacao quando nota estiver em branco")
    void deveLancarErroPorNotaEmBranco() {
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "   ", null);

        assertThatThrownBy(() -> service.registrar(payload, null))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("nota");
    }

    @Test
    @DisplayName("deve lançar ErroValidacao quando screenshot exceder tamanho máximo")
    void deveLancarErroPorScreenshotGrande() {
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Problema encontrado nesta funcionalidade", null);
        byte[] imagemGrande = new byte[6_000_000];
        MockMultipartFile screenshot = new MockMultipartFile("screenshot", "grande.webp", "image/webp", imagemGrande);

        assertThatThrownBy(() -> service.registrar(payload, screenshot))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("tamanho máximo");
    }

    @Test
    @DisplayName("deve ignorar screenshot quando screenshot-dir não está configurado")
    void deveIgnorarScreenshotSemDirConfigurado() {
        propriedades = new FeedbackPropriedades(null, 5_242_880L);
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper);
        configurarUsuarioMock();

        var payload = new FeedbackPayloadDto(FeedbackTipo.ELOGIO, "Sistema excelente, parabéns ao time!", null);
        byte[] imagemFake = "fake".getBytes();
        MockMultipartFile screenshot = new MockMultipartFile("screenshot", "s.webp", "image/webp", imagemFake);

        var registroSalvo = FeedbackRegistro.builder()
                .id(UUID.randomUUID())
                .tipo(FeedbackTipo.ELOGIO)
                .nota(payload.nota())
                .usuarioId(usuarioMock.getTituloEleitoral())
                .usuarioNome(usuarioMock.getNome())
                .rota("/desconhecido")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.save(any())).thenReturn(registroSalvo);

        service.registrar(payload, screenshot);

        verify(repo).save(argThat(r -> r.getCaminhoScreenshot() == null));
    }

    @Test
    @DisplayName("deve armazenar usuarioId do contexto de segurança, não dos metadados")
    void deveUsarUsuarioDoContextoDeSeguranca() throws Exception {
        configurarUsuarioMock();
        JsonNode metadados = objectMapper.readTree("{\"usuarioCodigo\": \"99999\", \"rotaCaminho\": \"/painel\"}");
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Bug encontrado no painel principal", metadados);

        var registroSalvo = FeedbackRegistro.builder()
                .id(UUID.randomUUID())
                .tipo(FeedbackTipo.BUG)
                .nota(payload.nota())
                .usuarioId("12345")
                .usuarioNome("João Testador")
                .rota("/painel")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.save(any())).thenReturn(registroSalvo);

        service.registrar(payload, null);

        // usuarioId deve ser o do security context (12345), não o dos metadados (99999)
        verify(repo).save(argThat(r -> "12345".equals(r.getUsuarioId())));
    }
}
