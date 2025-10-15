package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.notificacao.NotificacaoService;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

@Service
@RequiredArgsConstructor
public class SubprocessoNotificacaoService {

    private final NotificacaoService notificacaoService;
    private final AlertaRepo repositorioAlerta;
    private final UnidadeRepo unidadeRepo;

    public void notificarDisponibilizacaoMapa(Subprocesso sp) {
        Unidade unidade = sp.getUnidade();
        if (unidade == null) return;

        String nomeProcesso = sp.getProcesso().getDescricao();
        String siglaUnidade = unidade.getSigla();
        String dataLimite = sp.getDataLimiteEtapa2().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // E-mail para a unidade do subprocesso (Item 10.11 do CDU)
        String assuntoUnidade = String.format("SGC: Mapa de Competências da unidade %s disponibilizado para validação", siglaUnidade);
        String corpoUnidade = String.format(
                "Prezado(a) Chefe da unidade %s,%n%n" +
                        "O mapa de competências da sua unidade, referente ao processo '%s', foi disponibilizado para validação.%n" +
                        "O prazo para conclusão desta etapa é %s.%n%n" +
                        "Acesse o SGC para realizar a validação.",
                siglaUnidade, nomeProcesso, dataLimite);
        notificacaoService.enviarEmail(unidade.getSigla(), assuntoUnidade, corpoUnidade);

        // E-mail para as unidades superiores (Item 10.12 do CDU)
        String assuntoSuperior = String.format("SGC: Mapa de Competências da unidade %s disponibilizado para validação", siglaUnidade);
        String corpoSuperior = String.format(
                "Prezado(a) Chefe,%n%n" +
                        "O mapa de competências da unidade %s, referente ao processo '%s', foi disponibilizado para validação.%n" +
                        "O prazo para conclusão desta etapa é %s.%n%n" +
                        "Acompanhe o processo no SGC.",
                siglaUnidade, nomeProcesso, dataLimite);
        Unidade superior = unidade.getUnidadeSuperior();
        while (superior != null) {
            notificacaoService.enviarEmail(superior.getSigla(), assuntoSuperior, corpoSuperior);
            superior = superior.getUnidadeSuperior();
        }

        // Alerta (Item 10.10 do CDU)
        Unidade sedoc = unidadeRepo.findBySigla("SEDOC")
                .orElseThrow(() -> new IllegalStateException("Unidade 'SEDOC' não encontrada."));
        Alerta alerta = new Alerta();
        alerta.setDescricao(String.format("Mapa de competências da unidade %s disponibilizado para análise", siglaUnidade));
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sedoc);
        alerta.setUnidadeDestino(unidade);
        repositorioAlerta.save(alerta);
    }

    public void notificarSugestoes(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            notificacaoService.enviarEmail(
                    unidadeSuperior.getSigla(),
                    "SGC: Sugestões apresentadas para o mapa de competências da " + sp.getUnidade().getSigla(),
                    "A unidade " + sp.getUnidade().getSigla() + " apresentou sugestões para o mapa de competências elaborado no processo " + sp.getProcesso().getDescricao() + ". A análise dessas sugestões já pode ser realizada no sistema."
            );

            Alerta alerta = new Alerta();
            alerta.setDescricao("Sugestões para o mapa de competências da " + sp.getUnidade().getSigla() + " aguardando análise");
            alerta.setProcesso(sp.getProcesso());
            alerta.setDataHora(java.time.LocalDateTime.now());
            alerta.setUnidadeOrigem(sp.getUnidade());
            alerta.setUnidadeDestino(unidadeSuperior);
            repositorioAlerta.save(alerta);
        }
    }

    public void notificarValidacao(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            notificacaoService.enviarEmail(
                    unidadeSuperior.getSigla(),
                    "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                    "A unidade " + sp.getUnidade().getSigla() + " validou o mapa de competências elaborado no processo " + sp.getProcesso().getDescricao() + ". A análise dessa validação já pode ser realizada no sistema."
            );

            Alerta alerta = new Alerta();
            alerta.setDescricao("Validação do mapa de competências da " + sp.getUnidade().getSigla() + " aguardando análise");
            alerta.setProcesso(sp.getProcesso());
            alerta.setDataHora(java.time.LocalDateTime.now());
            alerta.setUnidadeOrigem(sp.getUnidade());
            alerta.setUnidadeDestino(unidadeSuperior);
            repositorioAlerta.save(alerta);
        }
    }

    public void notificarDevolucao(Subprocesso sp, Unidade unidadeDevolucao) {
        notificacaoService.enviarEmail(
                unidadeDevolucao.getSigla(),
                "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " devolvida para ajustes",
                "A validação do mapa de competências da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi devolvida para ajustes. Acompanhe o processo no sistema."
        );

        Alerta alerta = new Alerta();
        alerta.setDescricao("Cadastro de atividades e conhecimentos da unidade " + sp.getUnidade().getSigla() + " devolvido para ajustes");
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        alerta.setUnidadeDestino(unidadeDevolucao);
        repositorioAlerta.save(alerta);
    }

    public void notificarAceite(Subprocesso sp) {
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior().getUnidadeSuperior();
        if (unidadeSuperior != null) {
            notificacaoService.enviarEmail(
                    unidadeSuperior.getSigla(),
                    "SGC: Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise",
                    "A validação do mapa de competências da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi submetida para análise por essa unidade. A análise já pode ser realizada no sistema."
            );

            Alerta alerta = new Alerta();
            alerta.setDescricao("Validação do mapa de competências da " + sp.getUnidade().getSigla() + " submetida para análise");
            alerta.setProcesso(sp.getProcesso());
            alerta.setDataHora(java.time.LocalDateTime.now());
            alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
            alerta.setUnidadeDestino(unidadeSuperior);
            repositorioAlerta.save(alerta);
        }
    }

    public void notificarDevolucaoCadastro(Subprocesso sp, Unidade unidadeDevolucao, String motivo) {
        notificacaoService.enviarEmail(
                unidadeDevolucao.getSigla(),
                "SGC: Cadastro de atividades da " + sp.getUnidade().getSigla() + " devolvido para ajustes",
                "O cadastro de atividades da " + sp.getUnidade().getSigla() + " no processo " + sp.getProcesso().getDescricao() + " foi devolvido para ajustes com o motivo: " + motivo + ". Acompanhe o processo no sistema."
        );

        Alerta alerta = new Alerta();
        alerta.setDescricao("Cadastro de atividades da unidade " + sp.getUnidade().getSigla() + " devolvido para ajustes: " + motivo);
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sp.getUnidade().getUnidadeSuperior());
        alerta.setUnidadeDestino(unidadeDevolucao);
        repositorioAlerta.save(alerta);
    }

    public void notificarAceiteCadastro(Subprocesso sp, Unidade unidadeDestino) {
        if (unidadeDestino == null || sp.getUnidade() == null) return;

        String siglaUnidadeOrigem = sp.getUnidade().getSigla();
        String nomeProcesso = sp.getProcesso().getDescricao();

        // 1. Enviar E-mail (CDU-13 Item 10.7)
        String assunto = "SGC: Cadastro de atividades e conhecimentos da " + siglaUnidadeOrigem + " submetido para análise";
        String corpo = String.format(
                "Prezado(a) responsável pela %s,%n" +
                        "O cadastro de atividades e conhecimentos da %s no processo %s foi submetido para análise por essa unidade.%n" +
                        "A análise já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).",
                unidadeDestino.getSigla(),
                siglaUnidadeOrigem,
                nomeProcesso
        );
        notificacaoService.enviarEmail(unidadeDestino.getSigla(), assunto, corpo);

        // 2. Criar Alerta (CDU-13 Item 10.8)
        Alerta alerta = new Alerta();
        alerta.setDescricao("Cadastro de atividades e conhecimentos da unidade " + siglaUnidadeOrigem + " submetido para análise");
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sp.getUnidade());
        alerta.setUnidadeDestino(unidadeDestino);
        repositorioAlerta.save(alerta);
    }

    public void notificarDevolucaoRevisaoCadastro(Subprocesso sp, Unidade unidadeAnalise, Unidade unidadeDevolucao) {
        String siglaUnidadeSubprocesso = sp.getUnidade().getSigla();
        String descricaoProcesso = sp.getProcesso().getDescricao();

        // CDU-14 Item 10.9: Notificação por e-mail
        String assunto = String.format("SGC: Cadastro de atividades e conhecimentos da %s devolvido para ajustes", siglaUnidadeSubprocesso);
        String corpo = String.format(
                "Prezado(a) responsável pela %s,%n" +
                        "O cadastro de atividades e conhecimentos da %s no processo %s foi devolvido para ajustes.%n" +
                        "Acompanhe o processo no O sistema de Gestão de Competências: [URL_SISTEMA].",
                unidadeDevolucao.getSigla(),
                siglaUnidadeSubprocesso,
                descricaoProcesso
        );
        notificacaoService.enviarEmail(unidadeDevolucao.getSigla(), assunto, corpo);

        // CDU-14 Item 10.10: Alerta interno
        Alerta alerta = new Alerta();
        alerta.setDescricao(String.format("Cadastro de atividades e conhecimentos da unidade %s devolvido para ajustes", siglaUnidadeSubprocesso));
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(unidadeAnalise);
        alerta.setUnidadeDestino(unidadeDevolucao);
        repositorioAlerta.save(alerta);
    }

    public void notificarAceiteRevisaoCadastro(Subprocesso sp, Unidade unidadeDestino) {
        if (unidadeDestino == null || sp.getUnidade() == null) return;

        String siglaUnidadeSubprocesso = sp.getUnidade().getSigla();
        String descricaoProcesso = sp.getProcesso().getDescricao();
        String siglaUnidadeSuperior = unidadeDestino.getSigla();

        // CDU-14 Item 11.7: Notificação por e-mail
        String assunto = String.format("SGC: Revisão do cadastro de atividades e conhecimentos da %s submetido para análise", siglaUnidadeSubprocesso);
        String corpo = String.format(
                "Prezado(a) responsável pela %s,%n" +
                        "A revisão do cadastro de atividades e conhecimentos da %s no processo %s foi submetida para análise por essa unidade.%n" +
                        "A análise já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).",
                siglaUnidadeSuperior,
                siglaUnidadeSubprocesso,
                descricaoProcesso
        );
        notificacaoService.enviarEmail(siglaUnidadeSuperior, assunto, corpo);

        // CDU-14 Item 11.8: Alerta interno
        Alerta alerta = new Alerta();
        alerta.setDescricao(String.format("Revisão do cadastro de atividades e conhecimentos da unidade %s submetida para análise", siglaUnidadeSubprocesso));
        alerta.setProcesso(sp.getProcesso());
        alerta.setDataHora(java.time.LocalDateTime.now());
        alerta.setUnidadeOrigem(sp.getUnidade());
        alerta.setUnidadeDestino(unidadeDestino);
        repositorioAlerta.save(alerta);
    }
}