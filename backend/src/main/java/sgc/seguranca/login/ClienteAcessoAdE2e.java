package sgc.seguranca.login;

import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import org.jspecify.annotations.Nullable;
import org.springframework.web.client.RestClient;

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
        super(acessoAdRestClient);
    }

    @Override
    public void autenticar(String titulo, String senha) {
        log.info("Usuário autenticado via bypass de AD (Profile E2E): {}", mascarar(titulo));
    }

    private String mascarar(String valor) {
        if (valor.length() <= 4) return "***";
        return "***" + valor.substring(valor.length() - 4);
    }
}
