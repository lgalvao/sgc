package sgc.seguranca;

import net.jqwik.api.*;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import sgc.organizacao.model.Perfil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GerenciadorJwtPropertyTest {

    private final JwtProperties jwtProperties = new JwtProperties();
    private final Environment environment = mock(Environment.class);
    private final GerenciadorJwt gerenciadorJwt;

    GerenciadorJwtPropertyTest() {
        // Configura propriedades com segredo válido (> 32 chars)
        jwtProperties.setSecret("segredo-muito-seguro-para-testes-pbt-com-mais-de-32-caracteres");
        jwtProperties.setExpiracaoMinutos(60);

        // Mock do ambiente para aceitar perfil de teste (evita logs de erro)
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        gerenciadorJwt = new GerenciadorJwt(jwtProperties, environment);
    }

    @Property
    void gerarEValidarTokenDevePreservarDados(
            @ForAll("titulosEleitorais") String titulo,
            @ForAll Perfil perfil,
            @ForAll("codigosUnidade") Long unidadeCodigo
    ) {
        // 1. Gera o token
        String token = gerenciadorJwt.gerarToken(titulo, perfil, unidadeCodigo);
        assertThat(token).isNotBlank();

        // 2. Valida o token gerado
        Optional<GerenciadorJwt.JwtClaims> claimsOpt = gerenciadorJwt.validarToken(token);

        // 3. Verifica se os dados foram preservados (Round Trip)
        assertThat(claimsOpt).isPresent();
        GerenciadorJwt.JwtClaims claims = claimsOpt.get();

        assertThat(claims.tituloEleitoral()).isEqualTo(titulo);
        assertThat(claims.perfil()).isEqualTo(perfil);
        assertThat(claims.unidadeCodigo()).isEqualTo(unidadeCodigo);
    }

    @Property
    void validarTokenInvalidoDeveRetornarVazio(@ForAll("tokensInvalidos") String tokenInvalido) {
        // Qualquer string aleatória (que não seja um JWT válido assinado com nossa chave) deve retornar vazio
        assertThat(gerenciadorJwt.validarToken(tokenInvalido)).isEmpty();
    }

    @Provide
    Arbitrary<String> titulosEleitorais() {
        // Títulos eleitorais são numéricos com 12 dígitos, mas o sistema aceita strings
        return Arbitraries.strings().numeric().ofLength(12);
    }

    @Provide
    Arbitrary<Long> codigosUnidade() {
        // IDs positivos
        return Arbitraries.longs().greaterOrEqual(1);
    }

    @Provide
    Arbitrary<String> tokensInvalidos() {
        // Strings que definitivamente não são tokens JWT válidos para esta aplicação
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(100);
    }
}
