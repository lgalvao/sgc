package sgc.feedback;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import org.springframework.mock.web.*;
import sgc.comum.erros.*;
import sgc.comum.model.ComumRepo;
import sgc.feedback.dto.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import tools.jackson.databind.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
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

    @Mock
    private ComumRepo comumRepo;

    private FeedbackService service;
    private FeedbackPropriedades propriedades;
    private ObjectMapper objectMapper;

    private Usuario usuarioMock;

    @BeforeEach
    void configurar() throws IOException {
        Path dirTemp = Files.createTempDirectory("sgc-feedback-test-");
        propriedades = new FeedbackPropriedades(dirTemp.toString(), 5_242_880L);
        objectMapper = new ObjectMapper();
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper, comumRepo);

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
                .codigo(UUID.randomUUID())
                .tipo(FeedbackTipo.BUG)
                .nota(payload.nota())
                .usuarioCodigo(usuarioMock.getTituloEleitoral())
                .usuarioNome(usuarioMock.getNome())
                .rota("/desconhecido")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.save(any())).thenReturn(registroSalvo);

        FeedbackRespostaDto resposta = service.registrar(payload, null);

        assertThat(resposta.codigo()).isEqualTo(registroSalvo.getCodigo());
        verify(repo).save(argThat(r ->
                r.getTipo() == FeedbackTipo.BUG &&
                        r.getNota().equals(payload.nota()) &&
                        r.getUsuarioCodigo().equals("12345") &&
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
                .codigo(UUID.randomUUID())
                .tipo(FeedbackTipo.SUGESTAO)
                .nota(payload.nota())
                .usuarioCodigo(usuarioMock.getTituloEleitoral())
                .usuarioNome(usuarioMock.getNome())
                .rota("/desconhecido")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.save(any())).thenAnswer(invocation -> {
            FeedbackRegistro r = invocation.getArgument(0);
            registroSalvo.setCaminhoScreenshot(r.getCaminhoScreenshot());
            return registroSalvo;
        });

        service.registrar(payload, screenshot);

        ArgumentCaptor<FeedbackRegistro> captor = ArgumentCaptor.forClass(FeedbackRegistro.class);
        verify(repo).save(captor.capture());
        String nomeArquivo = captor.getValue().getCaminhoScreenshot();
        
        assertThat(nomeArquivo).isNotNull().endsWith(".webp");
        Path caminhoCompleto = Path.of(propriedades.screenshotDir()).resolve(nomeArquivo);
        assertThat(caminhoCompleto).exists();
        assertThat(Files.readAllBytes(caminhoCompleto)).isEqualTo(imagemFake);
    }

    @Test
    @DisplayName("deve extrair rota dos metadados quando presente")
    void deveExtrairRotaDosMetadados() throws Exception {
        configurarUsuarioMock();
        JsonNode metadados = objectMapper.readTree("{\"rotaCaminho\": \"/processos/42\"}");
        var payload = new FeedbackPayloadDto(FeedbackTipo.QUESTAO, "Dúvida sobre esta tela específica", metadados);
        var registroSalvo = FeedbackRegistro.builder()
                .codigo(UUID.randomUUID())
                .tipo(FeedbackTipo.QUESTAO)
                .nota(payload.nota())
                .usuarioCodigo(usuarioMock.getTituloEleitoral())
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
    @DisplayName("deve lançar ErroValidacao quando nota exceder o limite")
    void deveLancarErroPorNotaMuitoLonga() {
        String notaLonga = "a".repeat(2001);
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, notaLonga, null);

        assertThatThrownBy(() -> service.registrar(payload, null))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("2000");
    }

    @Test
    @DisplayName("deve lançar ErroValidacao quando nota for nula")
    void deveLancarErroPorNotaNula() {
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, null, null);

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
    @DisplayName("deve registrar feedback mesmo quando falhar serialização dos metadados")
    void deveRegistrarFeedbackComFalhaDeSerializacao() throws Exception {
        configurarUsuarioMock();
        ObjectMapper objectMapperComFalha = mock(ObjectMapper.class);
        when(objectMapperComFalha.writeValueAsString(any())).thenThrow(new RuntimeException("falha serializacao"));
        FeedbackService servicoComFalhaNaSerializacao = new FeedbackService(
                repo, propriedades, usuarioFacade, objectMapperComFalha, comumRepo);

        JsonNode metadados = objectMapper.createObjectNode().put("rotaCaminho", "/painel");
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Bug no painel", metadados);
        when(repo.save(any())).thenAnswer(invocation -> {
            FeedbackRegistro registro = invocation.getArgument(0);
            registro.setCodigo(UUID.randomUUID());
            registro.setEnviadoEm(OffsetDateTime.now());
            return registro;
        });

        servicoComFalhaNaSerializacao.registrar(payload, null);

        verify(repo).save(argThat(registro ->
                registro.getMetadataJson() == null
                        && "/painel".equals(registro.getRota())
                        && "12345".equals(registro.getUsuarioCodigo())
        ));
    }

    @Test
    @DisplayName("deve ignorar screenshot quando screenshot-dir não está configurado")
    void deveIgnorarScreenshotSemDirConfigurado() {
        propriedades = new FeedbackPropriedades(null, 5_242_880L);
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper, comumRepo);
        configurarUsuarioMock();

        var payload = new FeedbackPayloadDto(FeedbackTipo.ELOGIO, "Sistema excelente, parabéns ao time!", null);
        byte[] imagemFake = "fake".getBytes();
        MockMultipartFile screenshot = new MockMultipartFile("screenshot", "s.webp", "image/webp", imagemFake);

        var registroSalvo = FeedbackRegistro.builder()
                .codigo(UUID.randomUUID())
                .tipo(FeedbackTipo.ELOGIO)
                .nota(payload.nota())
                .usuarioCodigo(usuarioMock.getTituloEleitoral())
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
    @DisplayName("deve ignorar screenshot vazio")
    void deveIgnorarScreenshotVazio() {
        configurarUsuarioMock();
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Bug sem imagem válida", null);
        MockMultipartFile screenshotVazio = new MockMultipartFile("screenshot", "s.webp", "image/webp", new byte[0]);
        when(repo.save(any())).thenAnswer(invocation -> {
            FeedbackRegistro registro = invocation.getArgument(0);
            registro.setCodigo(UUID.randomUUID());
            registro.setEnviadoEm(OffsetDateTime.now());
            return registro;
        });

        service.registrar(payload, screenshotVazio);

        verify(repo).save(argThat(registro -> registro.getCaminhoScreenshot() == null));
    }

    @Test
    @DisplayName("deve armazenar usuarioCodigo do contexto de segurança, não dos metadados")
    void deveUsarUsuarioDoContextoDeSeguranca() throws Exception {
        configurarUsuarioMock();
        JsonNode metadados = objectMapper.readTree("{\"usuarioCodigo\": \"99999\", \"rotaCaminho\": \"/painel\"}");
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Bug encontrado no painel principal", metadados);

        var registroSalvo = FeedbackRegistro.builder()
                .codigo(UUID.randomUUID())
                .tipo(FeedbackTipo.BUG)
                .nota(payload.nota())
                .usuarioCodigo("12345")
                .usuarioNome("João Testador")
                .rota("/painel")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.save(any())).thenReturn(registroSalvo);

        service.registrar(payload, null);

        // usuarioCodigo deve ser o do security context (12345), não o dos metadados (99999)
        verify(repo).save(argThat(r -> "12345".equals(r.getUsuarioCodigo())));
    }

    @Test
    @DisplayName("deve listar feedbacks mais recentes em ordem decrescente")
    void deveListarFeedbacksMaisRecentes() {
        var maisRecente = FeedbackRegistro.builder()
                .codigo(UUID.randomUUID())
                .tipo(FeedbackTipo.BUG)
                .nota("Mais recente")
                .usuarioCodigo("1")
                .usuarioNome("A")
                .rota("/painel")
                .status(FeedbackStatus.NOVO)
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        var maisAntigo = FeedbackRegistro.builder()
                .codigo(UUID.randomUUID())
                .tipo(FeedbackTipo.SUGESTAO)
                .nota("Mais antigo")
                .usuarioCodigo("2")
                .usuarioNome("B")
                .rota("/historico")
                .status(FeedbackStatus.REVISADO)
                .enviadoEm(java.time.OffsetDateTime.now().minusDays(1))
                .build();
        when(repo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(maisRecente, maisAntigo)));

        var resposta = service.listarRecentes(50);

        assertThat(resposta).hasSize(2);
        assertThat(resposta.getFirst().nota()).isEqualTo("Mais recente");
        verify(repo).findAll(argThat((Pageable pageable) ->
                pageable.getPageSize() == 50
                        && pageable.getSort().getOrderFor("enviadoEm") != null
                        && pageable.getSort().getOrderFor("enviadoEm").isDescending()
        ));
    }

    @Test
    @DisplayName("deve retornar bytes da screenshot quando existe")
    void deveRetornarBytesDaScreenshot() throws IOException {
        UUID codigo = UUID.randomUUID();
        Path tempFile = Files.createTempFile("screenshot-", ".webp");
        Files.write(tempFile, "fake-image-bytes".getBytes());

        var registro = FeedbackRegistro.builder()
                .codigo(codigo)
                .caminhoScreenshot(tempFile.toString())
                .build();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenReturn(registro);

        byte[] resultado = service.obterScreenshot(codigo);

        assertThat(resultado).isEqualTo("fake-image-bytes".getBytes());
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("deve lançar ErroEntidadeNaoEncontrada quando feedback não existe")
    void deveLancarErroQuandoFeedbackNaoExiste() {
        UUID codigo = UUID.randomUUID();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenThrow(new ErroEntidadeNaoEncontrada("Feedback", codigo));

        assertThatThrownBy(() -> service.obterScreenshot(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("deve lançar ErroEntidadeNaoEncontrada quando screenshot não está disponível")
    void deveLancarErroQuandoScreenshotNaoDisponivel() {
        UUID codigo = UUID.randomUUID();
        var registro = FeedbackRegistro.builder()
                .codigo(codigo)
                .caminhoScreenshot(null)
                .build();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenReturn(registro);

        assertThatThrownBy(() -> service.obterScreenshot(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Screenshot não disponível");
    }

    @Test
    @DisplayName("deve lançar ErroEntidadeNaoEncontrada quando arquivo de screenshot não existe")
    void deveLancarErroQuandoArquivoDeScreenshotNaoExiste() {
        UUID codigo = UUID.randomUUID();
        var registro = FeedbackRegistro.builder()
                .codigo(codigo)
                .caminhoScreenshot("arquivo-inexistente.webp")
                .build();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenReturn(registro);

        assertThatThrownBy(() -> service.obterScreenshot(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Arquivo de screenshot não encontrado no servidor");
    }

    @Test
    @DisplayName("deve tratar caminho legado com separador invertido na resolução de screenshot")
    void deveTratarCaminhoLegadoComSeparadorInvertido() {
        UUID codigo = UUID.randomUUID();
        var registro = FeedbackRegistro.builder()
                .codigo(codigo)
                .caminhoScreenshot("pasta\\arquivo.webp")
                .build();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenReturn(registro);

        assertThatThrownBy(() -> service.obterScreenshot(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Arquivo de screenshot não encontrado no servidor");
    }

    @Test
    @DisplayName("deve lançar ErroInconsistenciaInterna quando falhar leitura do arquivo")
    void deveLancarErroInconsistenciaAoLerDiretorioComoArquivo() throws IOException {
        UUID codigo = UUID.randomUUID();
        Path diretorio = Files.createTempDirectory("feedback-screenshot-dir");
        var registro = FeedbackRegistro.builder()
                .codigo(codigo)
                .caminhoScreenshot(diretorio.toString())
                .build();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenReturn(registro);

        assertThatThrownBy(() -> service.obterScreenshot(codigo))
                .isInstanceOf(ErroInconsistenciaInterna.class)
                .hasMessageContaining("Erro ao ler arquivo de screenshot");
    }

    @Test
    @DisplayName("deve usar diretório padrão quando screenshot-dir estiver em branco")
    void deveUsarDiretorioPadraoQuandoConfiguracaoEmBranco() {
        propriedades = new FeedbackPropriedades("   ", 5_242_880L);
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper, comumRepo);
        UUID codigo = UUID.randomUUID();
        var registro = FeedbackRegistro.builder()
                .codigo(codigo)
                .caminhoScreenshot("arquivo-inexistente.webp")
                .build();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenReturn(registro);

        assertThatThrownBy(() -> service.obterScreenshot(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Arquivo de screenshot não encontrado no servidor");
    }

    @Test
    @DisplayName("deve normalizar limite de listagem para o intervalo permitido")
    void deveNormalizarLimiteDeListagem() {
        when(repo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // Teste com valor abaixo do mínimo (1)
        service.listarRecentes(0);
        verify(repo, atLeastOnce()).findAll(argThat((Pageable p) -> p.getPageSize() == 1));

        // Teste com valor acima do máximo (200)
        service.listarRecentes(250);
        verify(repo, atLeastOnce()).findAll(argThat((Pageable p) -> p.getPageSize() == 200));
    }

    @Test
    @DisplayName("deve usar rota /desconhecido quando não informada nos metadados")
    void deveUsarRotaDesconhecida() {
        configurarUsuarioMock();
        // Metadados sem rotaCaminho
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Nota", objectMapper.createObjectNode());
        when(repo.save(any())).thenReturn(FeedbackRegistro.builder().codigo(UUID.randomUUID()).enviadoEm(OffsetDateTime.now()).build());

        service.registrar(payload, null);

        verify(repo).save(argThat(r -> "/desconhecido".equals(r.getRota())));
    }

    @Test
    @DisplayName("deve usar rota /desconhecido quando rotaCaminho for nulo nos metadados JSON")
    void deveUsarRotaDesconhecidaQuandoRotaCaminhoForNuloNoJson() {
        configurarUsuarioMock();
        // Metadados com rotaCaminho explicitamente nulo (JSON null node)
        tools.jackson.databind.node.ObjectNode metadados = objectMapper.createObjectNode();
        metadados.putNull("rotaCaminho");
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Nota", metadados);
        when(repo.save(any())).thenReturn(FeedbackRegistro.builder().codigo(UUID.randomUUID()).enviadoEm(OffsetDateTime.now()).build());

        service.registrar(payload, null);

        verify(repo).save(argThat(r -> "/desconhecido".equals(r.getRota())));
    }

    @Test
    @DisplayName("deve marcar screenshot indisponível quando caminhoScreenshot estiver em branco")
    void deveMarcarScreenshotIndisponivelQuandoCaminhoEstiverEmBranco() {
        var registro = FeedbackRegistro.builder()
                .codigo(UUID.randomUUID())
                .tipo(FeedbackTipo.BUG)
                .nota("Nota")
                .usuarioCodigo("1")
                .usuarioNome("Usuário")
                .rota("/rota")
                .status(FeedbackStatus.NOVO)
                .caminhoScreenshot("  ")
                .enviadoEm(java.time.OffsetDateTime.now())
                .build();
        when(repo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(registro)));

        var resposta = service.listarRecentes(1);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.getFirst().screenshotDisponivel()).isFalse();
    }

    @Test
    @DisplayName("deve lançar ErroEntidadeNaoEncontrada quando caminhoScreenshot estiver em branco")
    void deveLancarErroQuandoCaminhoScreenshotEstiverEmBranco() {
        UUID codigo = UUID.randomUUID();
        var registro = FeedbackRegistro.builder()
                .codigo(codigo)
                .caminhoScreenshot("  ")
                .build();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenReturn(registro);

        assertThatThrownBy(() -> service.obterScreenshot(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Screenshot não disponível");
    }

    @Test
    @DisplayName("deve descartar screenshot quando screenshotDir for nulo")
    void deveDescartarScreenshotQuandoScreenshotDirForNulo() {
        propriedades = new FeedbackPropriedades(null, 5_242_880L);
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper, comumRepo);
        configurarUsuarioMock();
        var screenshot = new MockMultipartFile("screenshot", "img.webp", "image/webp", "bytes".getBytes());
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Nota", null);
        when(repo.save(any())).thenReturn(FeedbackRegistro.builder().codigo(UUID.randomUUID()).enviadoEm(OffsetDateTime.now()).build());

        service.registrar(payload, screenshot);

        verify(repo).save(argThat(r -> r.getCaminhoScreenshot() == null));
    }

    @Test
    @DisplayName("deve ignorar screenshot quando screenshotDir estiver em branco")
    void deveIgnorarScreenshotComDirEmBranco() {
        propriedades = new FeedbackPropriedades("   ", 5_242_880L);
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper, comumRepo);
        configurarUsuarioMock();
        var screenshot = new MockMultipartFile("screenshot", "img.webp", "image/webp", "bytes".getBytes());
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Nota", null);
        when(repo.save(any())).thenReturn(FeedbackRegistro.builder().codigo(UUID.randomUUID()).enviadoEm(OffsetDateTime.now()).build());

        service.registrar(payload, screenshot);

        verify(repo).save(argThat(r -> r.getCaminhoScreenshot() == null));
    }

    @Test
    @DisplayName("deve ignorar screenshot quando falhar a gravação no diretório configurado")
    void deveIgnorarScreenshotQuandoFalharAGravacaoNoDiretorioConfigurado() throws IOException {
        configurarUsuarioMock();
        Path arquivoNoLugarDoDiretorio = Path.of("backend", "build", "feedback-screenshot-arquivo-" + UUID.randomUUID() + ".bin");
        Files.createDirectories(arquivoNoLugarDoDiretorio.getParent());
        Files.writeString(arquivoNoLugarDoDiretorio, "arquivo");
        propriedades = new FeedbackPropriedades(arquivoNoLugarDoDiretorio.toString(), 5_242_880L);
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper, comumRepo);
        var payload = new FeedbackPayloadDto(FeedbackTipo.BUG, "Nota com screenshot", null);
        var screenshot = new MockMultipartFile("screenshot", "img.webp", "image/webp", "bytes".getBytes());
        when(repo.save(any())).thenReturn(FeedbackRegistro.builder().codigo(UUID.randomUUID()).enviadoEm(OffsetDateTime.now()).build());

        try {
            service.registrar(payload, screenshot);
        } finally {
            Files.deleteIfExists(arquivoNoLugarDoDiretorio);
        }

        verify(repo).save(argThat(r -> r.getCaminhoScreenshot() == null));
    }

    @Test
    @DisplayName("deve usar caminho padrão quando screenshotDir for nulo na resolução de caminho")
    void deveUsarCaminhosPadraoQuandoScreenshotDirForNuloNaResolucao() {
        propriedades = new FeedbackPropriedades(null, 5_242_880L);
        service = new FeedbackService(repo, propriedades, usuarioFacade, objectMapper, comumRepo);
        UUID codigo = UUID.randomUUID();
        var registro = FeedbackRegistro.builder()
                .codigo(codigo)
                .caminhoScreenshot("arquivo-inexistente.webp")
                .build();
        when(comumRepo.buscar(FeedbackRegistro.class, codigo)).thenReturn(registro);

        assertThatThrownBy(() -> service.obterScreenshot(codigo))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Arquivo de screenshot não encontrado no servidor");
    }
}
