package sgc.comum.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenSimuladoUtilTest {

    @Test
    @DisplayName("Deve assinar e validar corretamente")
    void deveAssinarEValidar() {
        String conteudo = "conteudoTeste";
        String assinatura = TokenSimuladoUtil.assinar(conteudo);

        assertNotNull(assinatura);
        assertTrue(TokenSimuladoUtil.validar(conteudo, assinatura));
    }

    @Test
    @DisplayName("Deve falhar com assinatura incorreta")
    void deveFalharComAssinaturaIncorreta() {
        String conteudo = "conteudoTeste";
        String assinaturaCorreta = TokenSimuladoUtil.assinar(conteudo);
        String assinaturaFalsa = assinaturaCorreta + "x";

        assertFalse(TokenSimuladoUtil.validar(conteudo, assinaturaFalsa));
    }

    @Test
    @DisplayName("Deve falhar com conteudo alterado")
    void deveFalharComConteudoAlterado() {
        String conteudo = "conteudoTeste";
        String assinatura = TokenSimuladoUtil.assinar(conteudo);
        String conteudoAlterado = "conteudoTeste2";

        assertFalse(TokenSimuladoUtil.validar(conteudoAlterado, assinatura));
    }
}
