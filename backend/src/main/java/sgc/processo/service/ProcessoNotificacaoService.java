package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoModelosService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final NotificacaoEmailService notificacaoEmailService;
    private final NotificacaoModelosService notificacaoModelosService;
    private final UnidadeFacade unidadeService;
    private final UsuarioFacade usuarioService;
    private final ProcessoRepo processoRepo;
    private final SubprocessoFacade subprocessoFacade;

    /**
     * Notifica sobre o início de um processo (alertas + e-mails).
     */
    @Transactional
    public void notificarInicioProcesso(Long codProcesso, List<Long> codUnidades) {
        try {
            processarInicioProcesso(codProcesso);
        } catch (Exception e) {
            log.error("Erro ao processar notificação de processo iniciado: {}", e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Notifica sobre a finalização de um processo (alertas + e-mails).
     */
    @Transactional
    public void notificarFinalizacaoProcesso(Long codProcesso) {
        try {
            processarFinalizacaoProcesso(codProcesso);
        } catch (Exception e) {
            log.error("Erro ao processar notificação de processo finalizado: {}", e.getClass().getSimpleName(), e);
        }
    }

    private void processarInicioProcesso(Long codProcesso) {
        Processo processo = processoRepo.findById(codProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codProcesso));
        List<Subprocesso> subprocessos = subprocessoFacade.listarEntidadesPorProcesso(codProcesso);

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

        Map<Long, UnidadeResponsavelDto> responsaveis = unidadeService.buscarResponsaveisUnidades(todosCodigosUnidades);

        List<String> todosTitulos = new ArrayList<>();
        responsaveis.values().forEach(r -> {
            todosTitulos.add(r.titularTitulo());
            if (r.substitutoTitulo() != null)
                todosTitulos.add(r.substitutoTitulo());
        });

        Map<String, Usuario> usuarios = usuarioService.buscarUsuariosPorTitulos(todosTitulos);

        for (Subprocesso subprocesso : subprocessos) {
            enviarEmailProcessoIniciado(processo, subprocesso, responsaveis, usuarios);
        }
    }

    private void processarFinalizacaoProcesso(Long codProcesso) {
        Processo processo = processoRepo.findById(codProcesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codProcesso));

        List<Long> codigosParticipantes = processo.getCodigosParticipantes();

        if (codigosParticipantes.isEmpty()) {
            log.warn("Nenhuma unidade participante encontrada para notificar ao finalizar processo {}",
                    processo.getCodigo());
            return;
        }

        List<Unidade> unidadesParticipantes = unidadeService.buscarEntidadesPorIds(codigosParticipantes);

        List<Long> todosCodigosUnidades = unidadesParticipantes.stream().map(Unidade::getCodigo).toList();
        Map<Long, UnidadeResponsavelDto> responsaveis = unidadeService.buscarResponsaveisUnidades(todosCodigosUnidades);

        Map<String, Usuario> usuarios = usuarioService.buscarUsuariosPorTitulos(responsaveis.values().stream()
                .map(UnidadeResponsavelDto::titularTitulo)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        for (Unidade unidade : unidadesParticipantes) {
            enviarNotificacaoFinalizacao(processo, unidade, responsaveis, usuarios, unidadesParticipantes);
        }
    }

    private void enviarNotificacaoFinalizacao(Processo processo, Unidade unidade,
            Map<Long, UnidadeResponsavelDto> responsaveis,
            Map<String, Usuario> usuarios,
            List<Unidade> subordinadas) {
        try {
            UnidadeResponsavelDto responsavel = responsaveis.get(unidade.getCodigo());
            // TODO nem responsavel nem sigla podem ser nulos nunca. É invariante do sistema!
            if (responsavel == null || unidade.getSigla() == null) {
                return;
            }

            TipoUnidade tipoUnidade = unidade.getTipo();
            String emailUnidade = String.format("%s@tre-pe.jus.br", unidade.getSigla().toLowerCase());

            if (tipoUnidade == OPERACIONAL || tipoUnidade == INTEROPERACIONAL || tipoUnidade == RAIZ) {
                enviarEmailUnidadeFinal(processo, unidade, emailUnidade);
            } else if (tipoUnidade == INTERMEDIARIA) {
                enviarEmailUnidadeIntermediaria(processo, unidade, emailUnidade, subordinadas);
            }

            // TODO estranho isso. O responsavel é será definido pela view. Ele pode ser o titular, uma pesssoa substituta ou uma pesoa designaca numa atribuição temporaria. Sempre vai ser preenchido. Entao o titulo nunca pode ser nulo!
            if (responsavel.substitutoTitulo() != null) {
                String assunto = String.format("SGC: Finalização do processo %s", processo.getDescricao());

                // Simplesmente reusa a lógica de corpo se for operacional
                String html = notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(
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

        String html = notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(
                unidade.getSigla(),
                processo.getDescricao());

        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
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

        // TODO nenhuma sentido isso: se uma unidade é intermediaria TEM que ter subordinadas! Invariante. Nao era para ter chegado inconsistente aqui!
        if (siglasSubordinadas.isEmpty()) {
            log.warn("Nenhuma unidade subordinada encontrada para notificar a unidade intermediária {}",
                    unidade.getSigla());
            return;
        }

        String assunto = String.format("SGC: Finalização do processo %s em unidades subordinadas",
                processo.getDescricao());

        String html = notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(unidade.getSigla(),
                processo.getDescricao(), siglasSubordinadas);

        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
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
                case OPERACIONAL, INTEROPERACIONAL, RAIZ -> "Processo Iniciado - %s".formatted(processo.getDescricao());
                case INTERMEDIARIA ->
                    "Processo Iniciado em Unidades Subordinadas - %s".formatted(processo.getDescricao());
                case SEM_EQUIPE -> "Notificação não enviada para unidade (N/A)";
            };

            String corpoHtml = criarCorpoEmailPorTipo(unidade.getTipo(), processo, subprocesso);

            String emailUnidade = String.format("%s@tre-pe.jus.br", unidade.getSigla().toLowerCase());
            notificacaoEmailService.enviarEmailHtml(emailUnidade, assunto, corpoHtml);
            log.info("E-mail enviado para unidade {} ({})", unidade.getSigla(), emailUnidade);

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
                    notificacaoModelosService.criarEmailProcessoIniciado(
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
                notificacaoEmailService.enviarEmailHtml(substituto.getEmail(), assunto, corpoHtml);
                log.info("E-mail enviado ao substituto da unidade {}.", nomeUnidade);
            }
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail ao substituto da unidade {}: {}", nomeUnidade,
                    e.getClass().getSimpleName());
        }
    }
}
