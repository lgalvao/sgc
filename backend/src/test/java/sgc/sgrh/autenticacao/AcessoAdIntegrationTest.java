package sgc.sgrh.autenticacao;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sgc.comum.erros.ErroAutenticacao;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requer ambiente com AD configurado")
@SpringBootTest
@DisplayName("Integração com Acesso AD de desenvolvimento")
public class AcessoAdIntegrationTest {
    @Autowired
    private AcessoAdClient acessoAdClient;

    @Autowired
    private AcessoAdProperties acessoAdProperties;

    @Test
    @DisplayName("Deve autenticar com sucesso usando credenciais reais válidas")
    void testAutenticar_sucessoCredenciaisValidas() {
        String tituloEleitoral = "039703250884";
        String senha = "12345678";

        boolean resultado = acessoAdClient.autenticar(tituloEleitoral, senha);
        assertTrue(resultado, "Autenticação deve retornar true para credenciais válidas");
    }

    @Test
    @DisplayName("Deve falhar ao tentar autenticar com senha inválida")
    void testAutenticar_falhaSenhaInvalida() {
        String tituloEleitoral = "039703250884";
        String senhaInvalida = "senhaErrada123";

        ErroAutenticacao exception = assertThrows(
            ErroAutenticacao.class,
            () -> acessoAdClient.autenticar(tituloEleitoral, senhaInvalida),
            "Deve lançar ErroAutenticacao para senha inválida"
        );

        assertTrue(
            exception.getMessage().contains("Falha na autenticação") || 
            exception.getMessage().contains("Erro na autenticação"),
            "Mensagem de erro deve indicar falha na autenticação"
        );
    }

    @Test
    @DisplayName("Deve falhar ao tentar autenticar com título inexistente")
    void testAutenticar_falhaTituloInexistente() {
        String tituloInexistente = "000000000000";
        String senha = "qualquersenha";

        ErroAutenticacao exception = assertThrows(
            ErroAutenticacao.class,
            () -> acessoAdClient.autenticar(tituloInexistente, senha),
            "Deve lançar ErroAutenticacao para título inexistente"
        );

        assertTrue(
            exception.getMessage().contains("Falha na autenticação") || 
            exception.getMessage().contains("Erro na autenticação"),
            "Mensagem de erro deve indicar falha na autenticação"
        );
    }

    @Test
    @DisplayName("Deve verificar configuração do cliente AD")
    void testConfiguracao() {
        assertNotNull(acessoAdProperties, "Properties do Acesso AD devem estar configuradas");
        assertNotNull(acessoAdProperties.getBaseUrl(), "Base URL não deve ser nula");
        assertNotNull(acessoAdProperties.getCodigoSistema(), "Código do sistema não deve ser nulo");
    }
}
