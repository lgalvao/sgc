package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static sgc.organizacao.model.TipoUnidade.*;

/**
 * Serviço de notificação para eventos de processo.
 * Chamado diretamente por ProcessoInicializador e ProcessoFinalizador.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoNotificacaoService {
    private final AlertaFacade servicoAlertas;
    private final EmailService emailService;
    private final EmailModelosService emailModelosService;
    private final OrganizacaoFacade organizacaoFacade;
    private final UsuarioFacade usuarioService;
    private final ProcessoRepo processoRepo;
    private final SubprocessoService subprocessoService;

    /**
     * Notifica sobre o início de um processo (alertas + e-mails).
     */
    @Transactional
    public void emailInicioProcesso(Long codProcesso) {
        processarInicioProcesso(codProcesso);
    }

    /**
     * Notifica sobre a finalização de um processo (alertas + e-mails).
     */
    @Transactional
    public void emailFinalizacaoProcesso(Long codProcesso) {
        processarFinalizacaoProcesso(codProcesso);
    }

    private void processarInicioProcesso(Long codProcesso) {
        Processo processo = processoRepo.findByIdComParticipantes(codProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codProcesso));

        List<Subprocesso> subprocessos = subprocessoService.listarEntidadesPorProcesso(codProcesso);
        if (subprocessos.isEmpty()) {
            log.warn("Nenhum subprocesso encontrado para o processo {}", codProcesso);
            return;
        }

        List<Unidade> unidadesParticipantes = subprocessos.stream()
                .map(Subprocesso::getUnidade)
                .toList();
        servicoAlertas.criarAlertasProcessoIniciado(processo, unidadesParticipantes);

        List<Long> todosCodigosUnidades = subprocessos.stream()
                .map(s -> s.getUnidade().getCodigo())
                .toList();

        Map<Long, UnidadeResponsavelDto> responsaveis = organizacaoFacade.buscarResponsaveisUnidades(todosCodigosUnidades);

        List<String> todosTitulos = new ArrayList<>();
        responsaveis.values().forEach(r -> {
            todosTitulos.add(r.titularTitulo());
            if (r.substitutoTitulo() != null)
                todosTitulos.add(r.substitutoTitulo());
        });

        Map<String, Usuario> usuarios = usuarioService.buscarUsuariosPorTitulos(todosTitulos);
        for (Subprocesso sp : subprocessos) {
            enviarEmailProcessoIniciado(processo, sp, responsaveis, usuarios);
        }
    }

    private void processarFinalizacaoProcesso(Long codProcesso) {
        Processo processo = processoRepo.findByIdComParticipantes(codProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codProcesso));

        List<Long> codigosParticipantes = processo.getCodigosParticipantes();
        if (codigosParticipantes.isEmpty()) {
            log.warn("Nenhuma unidade participante encontrada para notificar ao finalizar processo {}",
                    processo.getCodigo());
            return;
        }

        List<Unidade> unidadesParticipantes = organizacaoFacade.unidadesPorCodigos(codigosParticipantes);

        List<Long> todosCodigosUnidades = unidadesParticipantes.stream().map(Unidade::getCodigo).toList();
        Map<Long, UnidadeResponsavelDto> responsaveis = organizacaoFacade.buscarResponsaveisUnidades(todosCodigosUnidades);

        Map<String, Usuario> usuarios = usuarioService.buscarUsuariosPorTitulos(responsaveis.values().stream()
                .map(UnidadeResponsavelDto::titularTitulo)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        for (Unidade unidade : unidadesParticipantes) {
            enviarEmailFinalizacao(processo, unidade, responsaveis, usuarios, unidadesParticipantes);
        }
    }

    private void enviarEmailFinalizacao(Processo processo, Unidade unidade,
                                        Map<Long, UnidadeResponsavelDto> responsaveis,
                                        Map<String, Usuario> usuarios,
                                        List<Unidade> subordinadas) {
        try {
            UnidadeResponsavelDto responsavel = responsaveis.get(unidade.getCodigo());
            TipoUnidade tipoUnidade = unidade.getTipo();
            String emailUnidade = String.format("%s@tre-pe.jus.br", unidade.getSigla().toLowerCase());

            if (tipoUnidade == OPERACIONAL || tipoUnidade == INTEROPERACIONAL || tipoUnidade == RAIZ) {
                enviarEmailUnidadeFinal(processo, unidade, emailUnidade);
            } else if (tipoUnidade == INTERMEDIARIA) {
                enviarEmailUnidadeIntermediaria(processo, unidade, emailUnidade, subordinadas);
            }

            if (responsavel.substitutoTitulo() != null) {
                String assunto = String.format("SGC: Finalização do processo %s", processo.getDescricao());
                String html = emailModelosService.criarEmailProcessoFinalizadoPorUnidade(
                        unidade.getSigla(),
                        processo.getDescricao());
                enviarEmailParaSubstituto(responsavel.substitutoTitulo(), usuarios, assunto, html, unidade.getNome());
            }
        } catch (Exception e) {
            log.error("Falha ao preparar notificação para unidade {} no processo {}: {}",
                    unidade.getSigla(), processo.getCodigo(), e.getMessage(), e);
        }
    }

    private void enviarEmailUnidadeFinal(Processo processo, Unidade unidade, String email) {
        String assunto = String.format("SGC: Finalização do processo %s", processo.getDescricao());

        String html = emailModelosService.criarEmailProcessoFinalizadoPorUnidade(
                unidade.getSigla(),
                processo.getDescricao());

        emailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {}", unidade.getSigla());
    }

    private void enviarEmailUnidadeIntermediaria(
            Processo processo,
            Unidade unidade,
            String email,
            List<Unidade> subordinadas) {

        List<String> siglasSubordinadas = subordinadas.stream()
                .filter(u -> u.getUnidadeSuperior() != null
                        && u.getUnidadeSuperior().getCodigo().equals(unidade.getCodigo()))
                .map(Unidade::getSigla)
                .sorted()
                .toList();

        if (siglasSubordinadas.isEmpty()) {
            log.warn("Unidade intermediária {} sem unidades subordinadas participantes para notificar na finalização do processo {}.", 
                    unidade.getSigla(), processo.getCodigo());
            return;
        }

        String assunto = String.format("SGC: Finalização do processo %s em unidades subordinadas",
                processo.getDescricao());

        String html = emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(unidade.getSigla(),
                processo.getDescricao(), siglasSubordinadas);

        emailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização (unidade intermediaria) enviado para {}", unidade.getSigla());
    }

    private void enviarEmailProcessoIniciado(
            Processo processo,
            Subprocesso subprocesso,
            Map<Long, UnidadeResponsavelDto> responsaveis,
            Map<String, Usuario> usuarios) {

        Unidade unidade = subprocesso.getUnidade();
        Long codigoUnidade = unidade.getCodigo();

        try {
            UnidadeResponsavelDto responsavel = responsaveis.get(codigoUnidade);
            String nomeUnidade = unidade.getNome();
            String assunto = switch (unidade.getTipo()) {
                case OPERACIONAL, INTEROPERACIONAL, RAIZ -> "Processo iniciado - %s".formatted(processo.getDescricao());
                case INTERMEDIARIA ->
                        "Processo iniciado em unidades subordinadas - %s".formatted(processo.getDescricao());
                case SEM_EQUIPE -> "Notificação não enviada para unidade (N/A)";
            };

            String corpoHtml = criarCorpoEmailPorTipo(unidade.getTipo(), processo, subprocesso);
            String emailUnidade = String.format("%s@tre-pe.jus.br", unidade.getSigla().toLowerCase());
            emailService.enviarEmailHtml(emailUnidade, assunto, corpoHtml);
            log.info("E-mail enviado para {}", unidade.getSigla());

            if (responsavel.substitutoTitulo() != null) {
                enviarEmailParaSubstituto(responsavel.substitutoTitulo(), usuarios, assunto, corpoHtml, nomeUnidade);
            }
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para {}: {}", codigoUnidade, e.getClass().getSimpleName(), e);
        }
    }

    String criarCorpoEmailPorTipo(TipoUnidade tipoUnidade, Processo processo, Subprocesso subprocesso) {
        return switch (tipoUnidade) {
            case OPERACIONAL, INTEROPERACIONAL, INTERMEDIARIA, RAIZ ->
                    emailModelosService.criarEmailProcessoIniciado(
                            subprocesso.getUnidade().getNome(),
                            processo.getDescricao(),
                            processo.getTipo().name(),
                            subprocesso.getDataLimiteEtapa1());
            case SEM_EQUIPE ->
                    throw new IllegalArgumentException("Tipo de unidade não suportado para geração de e-mail: " + tipoUnidade);
        };
    }

    void enviarEmailParaSubstituto(String tituloSubstituto, Map<String, Usuario> usuarios, String assunto,
                                   String corpoHtml, String nomeUnidade) {
        try {
            Usuario substituto = usuarios.get(tituloSubstituto);
            if (substituto != null && !substituto.getEmail().isBlank()) {
                emailService.enviarEmailHtml(substituto.getEmail(), assunto, corpoHtml);
                log.info("E-mail enviado ao substituto da unidade {}.", nomeUnidade);
            }
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail ao substituto da unidade {}: {}", nomeUnidade, e.getClass().getSimpleName());
        }
    }
}
