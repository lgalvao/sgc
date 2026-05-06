package sgc.feedback;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sgc.comum.erros.*;
import sgc.feedback.dto.*;
import sgc.organizacao.*;
import tools.jackson.databind.*;
import sgc.organizacao.model.Usuario;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceExtraTest {

    @Mock
    private FeedbackRepo repo;
    @Mock
    private FeedbackPropriedades propriedades;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FeedbackService service;

    @Test
    @DisplayName("resolverCaminho - deve suportar caminhos absolutos legados")
    void resolverCaminho_CaminhoAbsolutoLegado() {
        String pathLegado = "C:\\temp\\foto.webp";
        // Uso de reflexão para acessar método privado
        Path path = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "resolverCaminho", pathLegado);
        assertEquals(Path.of(pathLegado), path);
    }

    @Test
    @DisplayName("salvarScreenshot - deve retornar nulo se diretório não configurado")
    void salvarScreenshot_DiretorioNulo() {
        when(propriedades.screenshotDir()).thenReturn(null);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "bytes".getBytes());
        
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "salvarScreenshot", file);
        assertNull(result);
    }



    @Test
    @DisplayName("serializarMetadados - deve retornar nulo em caso de erro no Jackson")
    void serializarMetadados_ErroJackson() throws Exception {
        Object meta = new Object();
        when(objectMapper.writeValueAsString(meta)).thenThrow(new RuntimeException("Erro"));
        
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "serializarMetadados", meta);
        assertNull(result);
    }

    @Test
    @DisplayName("extrairRota - deve extrair de JsonNode")
    void extrairRota_JsonNode() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode().put("rotaCaminho", "/teste");
        
        String rota = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "extrairRota", node);
        assertEquals("/teste", rota);
    }

    @Test
    @DisplayName("extrairRota - deve retornar desconhecido para metadados nulos")
    void extrairRota_Null() {
        String rota = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "extrairRota", (Object)null);
        assertEquals("/desconhecido", rota);
    }

    @Test
    @DisplayName("obterScreenshot - deve falhar se arquivo não existir no disco")
    void obterScreenshot_ArquivoNaoExiste() {
        UUID id = UUID.randomUUID();
        FeedbackRegistro reg = FeedbackRegistro.builder()
                .caminhoScreenshot("inexistente.webp")
                .build();
        when(repo.findById(id)).thenReturn(Optional.of(reg));
        when(propriedades.screenshotDir()).thenReturn("temp_feedbacks");
        
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.obterScreenshot(id));
    }
}
