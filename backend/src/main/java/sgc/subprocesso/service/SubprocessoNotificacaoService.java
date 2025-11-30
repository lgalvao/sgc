package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.processo.model.Processo;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.erros.ErroUnidadeNaoEncontrada;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoNotificacaoService {
    private final NotificacaoEmailService notificacaoEmailService;
    private final AlertaRepo repositorioAlerta;
    private final UnidadeRepo unidadeRepo;
    private final TemplateEngine templateEngine;

    private String processarTemplate(String template, java.util.Map<String, Object> variaveis) {
        Context context = new Context();
        context.setVariables(variaveis);
        return templateEngine.process(template, context);
    }

    private void criarEsalvarAlerta(String descricao, Processo processo, Unidade unidadeOrigem, Unidade unidadeDestino, Usuario usuarioDestino) {
        Alerta alerta = Alerta.builder()
            .descricao(descricao)
            .processo(processo)
            .dataHora(LocalDateTime.now())
            .unidadeOrigem(unidadeOrigem)
            .unidadeDestino(unidadeDestino)
            .usuarioDestino(usuarioDestino)
            .build();
        repositorioAlerta.save(alerta);
    }

    /**
     * Envia notificações (email e alerta) sobre a disponibilização de um mapa para validação.
     * <p>
     * Corresponde aos itens 10.10, 10.11 e 10.12 do CDU-17. Notifica a própria
     * unidade, suas unidades superiores hierarquicamente e a SEDOC.
     *
     * @param sp O subprocesso cujo mapa foi disponibilizado.
     */
    public void notificarDisponibilizacaoMapa(Subprocesso sp) {
        Unidade unidade = sp.getUnidade();
        if (unidade == null) return;

        String nomeProcesso = sp.getProcesso().getDescricao();
        String siglaUnidade = unidade.getSigla();
        String dataLimite = sp.getDataLimiteEtapa2().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("siglaUnidade", siglaUnidade);
        variaveis.put("nomeProcesso", nomeProcesso);
        variaveis.put("dataLimite", dataLimite);

        String assuntoUnidade = String.format("SGC: Mapa de Competências da unidade %s disponibilizado para validação", siglaUnidade);
        String corpoUnidade = processarTemplate("mapa-disponibilizado", variaveis);
        notificacaoEmailService.enviarEmail(unidade.getSigla(), assuntoUnidade, corpoUnidade);

        String assuntoSuperior = String.format("SGC: Mapa de Competências da unidade %s disponibilizado para validação", siglaUnidade);
        String corpoSuperior = processarTemplate("mapa-disponibilizado", variaveis);
        Unidade superior = unidade.getUnidadeSuperior();
        while (superior != null) {
            notificacaoEmailService.enviarEmail(superior.getSigla(), assuntoSuperior, corpoSuperior);
            superior = superior.getUnidadeSuperior();
        }


        Unidade sedoc = unidadeRepo.findBySigla("SEDOC")
                .orElseThrow(() -> new ErroUnidadeNaoEncontrada("Unidade 'SEDOC' não encontrada."));

        criarEsalvarAlerta(
            String.format("Mapa de competências da unidade %s disponibilizado para análise", siglaUnidade),
            sp.getProcesso(),
            sedoc,
            unidade,
            null
        );
    }

    /**
     * Notifica a unidade superior sobre a disponibilização do cadastro de atividades.
     * Corresponde ao CDU-09.
     *
     * @param sp             O subprocesso cujo cadastro foi disponibilizado.
     * @param unidadeDestino A unidade que realizará a análise.
     */
    public void notificarDisponibilizacaoCadastro(Subprocesso sp, Unidade unidadeDestino) {
        if (unidadeDestino == null || sp.getUnidade() == null) return;

        String siglaUnidadeOrigem = sp.getUnidade().getSigla();
        String nomeProcesso = sp.getProcesso().getDescricao();
        String siglaUnidadeDestino = unidadeDestino.getSigla();

        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("siglaUnidadeOrigem", siglaUnidadeOrigem);
        variaveis.put("siglaUnidadeDestino", siglaUnidadeDestino);
        variaveis.put("nomeProcesso", nomeProcesso);

        String assunto = "SGC: Cadastro de atividades e conhecimentos disponibilizado: " + siglaUnidadeOrigem;
        String corpo = processarTemplate("email/disponibilizacao-cadastro.html", variaveis);
        notificacaoEmailService.enviarEmail(siglaUnidadeDestino, assunto, corpo);

        criarEsalvarAlerta(
                "Cadastro de atividades/conhecimentos da unidade " + siglaUnidadeOrigem + " disponibilizado para análise",
                sp.getProcesso(),
                sp.getUnidade(),
                unidadeDestino,
                null
        );
    }

    /**
     * Notifica a unidade superior sobre a apresentação de sugestões em um mapa.
     *
     * @param sp O subprocesso no qual as sugestões foram feitas.
     */
    public void notificarSugestoes(Subprocesso sp) {
        log.debug("Notificando sugestões para subprocesso {}", sp.getCodigo());
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            log.debug("Unidade superior existe: {}", unidadeSuperior.getSigla());
            java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
            variaveis.put("siglaUnidade", sp.getUnidade().getSigla());
            variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());

            notificacaoEmailService.enviarEmail(
                    unidadeSuperior.getSigla(),
                    "SGC: Sugestões apresentadas para o mapa de competências da %s".formatted(sp.getUnidade().getSigla()),
                    processarTemplate("email/sugestoes-mapa.html", variaveis)
            );

            log.debug("Criando e salvando alerta para sugestões. Processo: {}, Unidade Origem: {}, Unidade Destino: {}",
                sp.getProcesso().getCodigo(), sp.getUnidade().getSigla(), unidadeSuperior.getSigla());
            criarEsalvarAlerta(
                "Sugestões para o mapa de competências da " + sp.getUnidade().getSigla() + " aguardando análise",
                sp.getProcesso(),
                sp.getUnidade(),
                unidadeSuperior,
                null
            );
        } else {
            log.debug("Unidade superior não encontrada para subprocesso {}. Não será criado alerta ou enviado e-mail.", sp.getCodigo());
        }
    }

    /**
     * Notifica a unidade superior sobre a submissão de uma validação de mapa para análise.
     *
     * @param sp O subprocesso cuja validação foi submetida.
     */
    public void notificarValidacao(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
            variaveis.put("siglaUnidade", sp.getUnidade().getSigla());
            variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());

            notificacaoEmailService.enviarEmail(
                    unidadeSuperior.getSigla(),
                    "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                    processarTemplate("email/validacao-mapa.html", variaveis)
            );

            criarEsalvarAlerta(
                "Validação do mapa de competências da " + sp.getUnidade().getSigla() + " aguardando análise",
                sp.getProcesso(),
                sp.getUnidade(),
                unidadeSuperior,
                null
            );
        }
    }

    /**
     * Notifica uma unidade sobre a devolução da validação do seu mapa para ajustes.
     *
     * @param sp               O subprocesso relacionado.
     * @param unidadeDevolucao A unidade que receberá a notificação de devolução.
     */
    public void notificarDevolucao(Subprocesso sp, Unidade unidadeDevolucao) {
        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("siglaUnidade", sp.getUnidade().getSigla());
        variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());

        notificacaoEmailService.enviarEmail(
                unidadeDevolucao.getSigla(),
                "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " devolvida para ajustes",
                processarTemplate("email/devolucao-validacao.html", variaveis)
        );

        criarEsalvarAlerta(
            "Cadastro de atividades e conhecimentos da unidade " + sp.getUnidade().getSigla() + " devolvido para ajustes",
            sp.getProcesso(),
            sp.getUnidade().getUnidadeSuperior(),
            unidadeDevolucao,
            null
        );
    }

    /**
     * Notifica a unidade superior hierárquica sobre o aceite de uma validação,
     * submetendo-a para a próxima etapa de análise.
     *
     * @param sp O subprocesso cuja validação foi aceita.
     */
    public void notificarAceite(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
            variaveis.put("siglaUnidade", sp.getUnidade().getSigla());
            variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());

            notificacaoEmailService.enviarEmail(
                    unidadeSuperior.getSigla(),
                    "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                    processarTemplate("email/aceite-validacao.html", variaveis)
            );

            criarEsalvarAlerta(
                "Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                sp.getProcesso(),
                sp.getUnidade().getUnidadeSuperior(),
                unidadeSuperior,
                null
            );
        }
    }

    /**
     * Notifica a unidade responsável sobre a devolução do seu cadastro de atividades
     * para ajustes.
     *
     * @param sp               O subprocesso cujo cadastro foi devolvido.
     * @param unidadeDevolucao A unidade que receberá a notificação.
     * @param motivo           O motivo da devolução.
     */
    public void notificarDevolucaoCadastro(Subprocesso sp, Unidade unidadeDevolucao, String motivo) {
        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("siglaUnidade", sp.getUnidade().getSigla());
        variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());
        variaveis.put("motivo", motivo);

        notificacaoEmailService.enviarEmail(
                unidadeDevolucao.getSigla(),
                "SGC: Cadastro de atividades da " + sp.getUnidade().getSigla() + " devolvido para ajustes",
                processarTemplate("email/devolucao-cadastro.html", variaveis)
        );

        criarEsalvarAlerta(
            "Cadastro de atividades da unidade " + sp.getUnidade().getSigla() + " devolvido para ajustes: " + motivo,
            sp.getProcesso(),
            sp.getUnidade().getUnidadeSuperior(),
            unidadeDevolucao,
            null
        );
    }

    /**
     * Notifica a unidade de destino sobre o aceite de um cadastro, submetendo-o
     * para a próxima fase de análise.
     * <p>
     * Corresponde aos itens 10.7 e 10.8 do CDU-13.
     *
     * @param sp             O subprocesso cujo cadastro foi aceito.
     * @param unidadeDestino A unidade que realizará a próxima análise.
     */
    public void notificarAceiteCadastro(Subprocesso sp, Unidade unidadeDestino) {
        if (unidadeDestino == null || sp.getUnidade() == null) return;

        String siglaUnidadeOrigem = sp.getUnidade().getSigla();
        String nomeProcesso = sp.getProcesso().getDescricao();

        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("siglaUnidadeOrigem", siglaUnidadeOrigem);
        variaveis.put("siglaUnidadeDestino", unidadeDestino.getSigla());
        variaveis.put("nomeProcesso", nomeProcesso);

        // 1. Enviar E-mail (CDU-13 Item 10.7)
        String assunto = "SGC: Cadastro de atividades e conhecimentos da " + siglaUnidadeOrigem + " submetido para análise";
        String corpo = processarTemplate("email/aceite-cadastro.html", variaveis);
        notificacaoEmailService.enviarEmail(unidadeDestino.getSigla(), assunto, corpo);

        criarEsalvarAlerta(
            "Cadastro de atividades e conhecimentos da unidade " + siglaUnidadeOrigem + " submetido para análise",
            sp.getProcesso(),
            sp.getUnidade(),
            unidadeDestino,
            null
        );
    }

    /**
     * Notifica a unidade superior sobre a disponibilização da revisão do cadastro de atividades.
     * <p>
     * Corresponde ao CDU-10.
     *
     * @param sp             O subprocesso cuja revisão de cadastro foi disponibilizada.
     * @param unidadeDestino A unidade que realizará a análise.
     */
    public void notificarDisponibilizacaoRevisaoCadastro(Subprocesso sp, Unidade unidadeDestino) {
        if (unidadeDestino == null || sp.getUnidade() == null) return;

        String siglaUnidadeSubprocesso = sp.getUnidade().getSigla();
        String descricaoProcesso = sp.getProcesso().getDescricao();
        String siglaUnidadeSuperior = unidadeDestino.getSigla();

        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("siglaUnidadeOrigem", siglaUnidadeSubprocesso);
        variaveis.put("siglaUnidadeDestino", siglaUnidadeSuperior);
        variaveis.put("nomeProcesso", descricaoProcesso);

        // CDU-10 Item 12: Notificação por e-mail
        String assunto = String.format("SGC: Revisão do cadastro de atividades e conhecimentos disponibilizada: %s", siglaUnidadeSubprocesso);
        String corpo = processarTemplate("email/disponibilizacao-revisao-cadastro.html", variaveis);
        notificacaoEmailService.enviarEmail(siglaUnidadeSuperior, assunto, corpo);

        // CDU-10 Item 13: Alerta interno
        criarEsalvarAlerta(
            String.format("Cadastro de atividades e conhecimentos da unidade %s disponibilizado para análise", siglaUnidadeSubprocesso),
            sp.getProcesso(),
            sp.getUnidade(),
            unidadeDestino,
            null
        );
    }

    /**
     * Notifica a unidade responsável sobre a devolução da revisão de seu cadastro
     * para ajustes.
     * <p>
     * Corresponde aos itens 10.9 e 10.10 do CDU-14.
     *
     * @param sp               O subprocesso cuja revisão foi devolvida.
     * @param unidadeAnalise   A unidade que realizou a análise e devolução.
     * @param unidadeDevolucao A unidade que receberá a notificação para ajustar o cadastro.
     */
    public void notificarDevolucaoRevisaoCadastro(Subprocesso sp, Unidade unidadeAnalise, Unidade unidadeDevolucao) {
        String siglaUnidadeSubprocesso = sp.getUnidade().getSigla();
        String descricaoProcesso = sp.getProcesso().getDescricao();

        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("siglaUnidadeOrigem", siglaUnidadeSubprocesso);
        variaveis.put("siglaUnidadeDestino", unidadeDevolucao.getSigla());
        variaveis.put("nomeProcesso", descricaoProcesso);

        String assunto = String.format("SGC: Cadastro de atividades e conhecimentos da %s devolvido para ajustes", siglaUnidadeSubprocesso);
        String corpo = processarTemplate("email/devolucao-revisao-cadastro.html", variaveis);
        notificacaoEmailService.enviarEmail(unidadeDevolucao.getSigla(), assunto, corpo);

        criarEsalvarAlerta(
            String.format("Cadastro de atividades e conhecimentos da unidade %s devolvido para ajustes", siglaUnidadeSubprocesso),
            sp.getProcesso(),
            unidadeAnalise,
            unidadeDevolucao,
            null
        );
    }

    /**
     * Notifica a unidade de destino sobre o aceite da revisão de um cadastro,
     * submetendo-o para a próxima fase de análise.
     * <p>
     * Corresponde aos itens 11.7 e 11.8 do CDU-14.
     *
     * @param sp             O subprocesso cuja revisão de cadastro foi aceita.
     * @param unidadeDestino A unidade que realizará a próxima análise.
     */
    public void notificarAceiteRevisaoCadastro(Subprocesso sp, Unidade unidadeDestino) {
        if (unidadeDestino == null || sp.getUnidade() == null) return;

        String siglaUnidadeSubprocesso = sp.getUnidade().getSigla();
        String descricaoProcesso = sp.getProcesso().getDescricao();
        String siglaUnidadeSuperior = unidadeDestino.getSigla();

        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("siglaUnidadeOrigem", siglaUnidadeSubprocesso);
        variaveis.put("siglaUnidadeDestino", siglaUnidadeSuperior);
        variaveis.put("nomeProcesso", descricaoProcesso);

        // CDU-14 Item 11.7: Notificação por e-mail
        String assunto = String.format("SGC: Revisão do cadastro de atividades e conhecimentos da %s submetido para análise", siglaUnidadeSubprocesso);
        String corpo = processarTemplate("email/aceite-revisao-cadastro.html", variaveis);
        notificacaoEmailService.enviarEmail(siglaUnidadeSuperior, assunto, corpo);

        criarEsalvarAlerta(
            String.format("Revisão do cadastro de atividades e conhecimentos da unidade %s submetida para análise", siglaUnidadeSubprocesso),
            sp.getProcesso(),
            sp.getUnidade(),
            unidadeDestino,
            null
        );
    }

    public void notificarHomologacaoMapa(Subprocesso sp) {
        Unidade sedoc = unidadeRepo.findBySigla("SEDOC")
                .orElseThrow(() -> new ErroUnidadeNaoEncontrada("Unidade 'SEDOC' não encontrada."));

        String descricaoProcesso = sp.getProcesso().getDescricao();

        java.util.Map<String, Object> variaveis = new java.util.HashMap<>();
        variaveis.put("nomeProcesso", descricaoProcesso);

        // Notificação por e-mail
        String assunto = String.format("SGC: Mapa de competências do processo %s homologado", descricaoProcesso);
        String corpo = processarTemplate("email/homologacao-mapa.html", variaveis);
        notificacaoEmailService.enviarEmail(sedoc.getSigla(), assunto, corpo);

        // Alerta interno
        criarEsalvarAlerta(
            String.format("Mapa de competências do processo %s homologado", descricaoProcesso),
            sp.getProcesso(),
            sedoc,
            sedoc,
            null
        );
    }
}