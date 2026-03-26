package sgc.organizacao.service;

import lombok.*;
import org.springframework.stereotype.*;
import sgc.alerta.*;
import sgc.comum.config.*;
import sgc.organizacao.model.*;

import java.time.format.*;

@Service
@RequiredArgsConstructor
public class AtribuicaoTemporariaNotificacaoService {
    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlertaFacade alertaFacade;
    private final EmailService emailService;
    private final ConfigAplicacao configAplicacao;

    public void notificarCriacao(AtribuicaoTemporaria atribuicao, Usuario usuario) {
        Unidade unidade = atribuicao.getUnidade();
        String siglaUnidade = unidade.getSigla();
        String assunto = "SGC: Atribuição de perfil CHEFE na unidade %s".formatted(siglaUnidade);
        String descricaoAlerta = "Atribuição temporária de perfil de CHEFE na unidade %s".formatted(siglaUnidade);
        String corpo = """
                Prezado(a) %s,

                Foi registrada uma atribuição temporária de perfil de CHEFE para você na unidade %s.
                Período: %s a %s.
                Justificativa: %s.

                Acesse o sistema em: %s.
                """.formatted(
                usuario.getNome(),
                siglaUnidade,
                atribuicao.getDataInicio().format(FORMATADOR_DATA),
                atribuicao.getDataTermino().format(FORMATADOR_DATA),
                atribuicao.getJustificativa(),
                obterUrlSistema()
        );

        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            emailService.enviarEmail(usuario.getEmail(), assunto, corpo);
        }

        alertaFacade.criarAlertaPessoalSemProcesso(unidade, usuario.getTituloEleitoral(), descricaoAlerta);
    }

    private String obterUrlSistema() {
        if (configAplicacao.getUrlAcessoHom() != null && !configAplicacao.getUrlAcessoHom().isBlank()) {
            return configAplicacao.getUrlAcessoHom();
        }
        if (configAplicacao.getUrlAcessoProd() != null && !configAplicacao.getUrlAcessoProd().isBlank()) {
            return configAplicacao.getUrlAcessoProd();
        }
        if (configAplicacao.isAmbienteTestes()) {
            return "http://localhost:5173/login";
        }
        return "";
    }
}
