package sgc.seguranca.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Cliente de simulação de AD para habilitar a execução dos fluxos
 * autenticados durante o bypass de teste E2E, injetado dinamicamente
 * quando o profile 'e2e' está ativo. Remove o if condicional de prod.
 */
@Component
@Profile({"e2e", "test"})
@Slf4j
public class ClienteAcessoAdE2e extends ClienteAcessoAd {

    public ClienteAcessoAdE2e() {
        super(null);
    }

    @Override
    public boolean autenticar(String titulo, String senha) {
        log.debug("Usuário autenticado via bypass de AD (Profile E2E): {}", mascarar(titulo));
        return true;
    }

    private String mascarar(String valor) {
        if (valor.length() <= 4) return "***";
        return "***" + valor.substring(valor.length() - 4);
    }
}
