package sgc.auth;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sgc.dto.PerfilUnidadeDTO;

import java.util.Collections;
import java.util.List;

/**
 * Mock simples do SistemaAcessoClient usado quando o profile "test" está ativo.
 * Comportamento previsível para testes: autentica apenas "validUser"/"senha"
 * e retorna um perfil exemplo para esse usuário.
 */
@Component
@Profile("test")
public class MockSistemaAcessoClient implements SistemaAcessoClient {

    @Override
    public boolean authenticate(String titulo, String senha) {
        return "validUser".equals(titulo) && "senha".equals(senha);
    }

    @Override
    public List<PerfilUnidadeDTO> fetchPerfis(String titulo) {
        if ("validUser".equals(titulo)) {
            return Collections.singletonList(new PerfilUnidadeDTO("CHEFE", 1L, "SESEL"));
        }
        return Collections.emptyList();
    }
}