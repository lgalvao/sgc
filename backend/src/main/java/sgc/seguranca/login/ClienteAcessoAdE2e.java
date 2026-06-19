package sgc.seguranca.login;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import sgc.comum.util.MascaraUtil;

/**
 * Cliente de simulação de AD para habilitar a execução dos fluxos
 * autenticados durante o bypass de teste E2E, injetado dinamicamente
 * quando o profile 'e2e' está ativo. Remove o if condicional de prod.
 */
@Component
@Profile({"e2e", "test"})
@Slf4j
public class ClienteAcessoAdE2e extends ClienteAcessoAd {
    public ClienteAcessoAdE2e(@Nullable RestClient acessoAdRestClient) {
        super(acessoAdRestClient != null ? acessoAdRestClient : RestClient.create());
    }

    @Override
    public void autenticar(String titulo, String senha) {
        log.debug("Usuário autenticado sem AD (perfil e2e): {}", MascaraUtil.mascarar(titulo));
    }
}
