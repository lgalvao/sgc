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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // 1. Identificar quem são os participantes (12.1)
        Map<Long, Subprocesso> participantesMap = subprocessos.stream()
                .collect(Collectors.toMap(s -> s.getUnidade().getCodigo(), s -> s));

        // 2. Identificar quem são os gestores de participantes (12.2) e coletar subordinadas imediatas
        Map<Long, Set<String>> gestoresSubordinadasMap = new HashMap<>();
        for (Subprocesso sp : subprocessos) {
            Unidade superior = sp.getUnidade().getUnidadeSuperior();
            while (superior != null) {
                Long codSuperior = superior.getCodigo();
                gestoresSubordinadasMap.computeIfAbsent(codSuperior, k -> new HashSet<>())
                        .add(sp.getUnidade().getSigla());
                superior = superior.getUnidadeSuperior();
            }
        }

        // 3. Unir todas as unidades que precisam ser notificadas
        Set<Long> todosCodigosNotificar = new HashSet<>(participantesMap.keySet());
        todosCodigosNotificar.addAll(gestoresSubordinadasMap.keySet());

        // 4. Buscar dados auxiliares em lote
        Map<Long, UnidadeResponsavelDto> responsaveisMap = organizacaoFacade.buscarResponsaveisUnidades(new ArrayList<>(todosCodigosNotificar));
        List<String> todosTitulosUsuarios = responsaveisMap.values().stream()
                .flatMap(r -> Stream.of(r.titularTitulo(), r.substitutoTitulo()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, Usuario> usuariosMap = usuarioService.buscarUsuariosPorTitulos(todosTitulosUsuarios);

        // 5. Enviar e-mails consolidados
        for (Long codUnidade : todosCodigosNotificar) {
            Unidade unidade = organizacaoFacade.unidadePorCodigo(codUnidade);
            Subprocesso sp = participantesMap.get(codUnidade);
            Set<String> subordinadasNoProcesso = gestoresSubordinadasMap.get(codUnidade);

            enviarEmailConsolidado(processo, unidade, sp, subordinadasNoProcesso, responsaveisMap.get(codUnidade), usuariosMap);
        }

        // 6. Criar alertas conforme 13.1, 13.2 e 13.3 (mantendo a lógica especificada)
        List<Unidade> unidadesParticipantes = subprocessos.stream().map(Subprocesso::getUnidade).toList();
        servicoAlertas.criarAlertasProcessoIniciado(processo, unidadesParticipantes);
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

            if (responsavel != null && responsavel.substitutoTitulo() != null) {
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

    private void enviarEmailConsolidado(
            Processo processo,
            Unidade unidade,
            Subprocesso sp,
            Set<String> subordinadas,
            UnidadeResponsavelDto responsavel,
            Map<String, Usuario> usuarios) {

        boolean isParticipante = sp != null;
        boolean isGestor = subordinadas != null && !subordinadas.isEmpty();

        if (!isParticipante && !isGestor) return;

        try {
            List<String> siglasSubordinadas = isGestor ? subordinadas.stream().sorted().toList() : Collections.emptyList();
            LocalDateTime dataLimite = sp != null ? sp.getDataLimiteEtapa1() : processo.getDataLimite();

            String assunto = isParticipante 
                    ? "SGC: Início de processo de mapeamento de competências"
                    : "SGC: Início de processo de mapeamento de competências em unidades subordinadas";

            String corpoHtml = emailModelosService.criarEmailInicioProcessoConsolidado(
                    unidade.getSigla(), processo.getDescricao(), dataLimite, isParticipante, siglasSubordinadas);

            enviarEmailEParaSubstituto(unidade, responsavel, usuarios, assunto, corpoHtml, unidade.getNome());
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail consolidado para {}: {}", unidade.getSigla(), e.getMessage(), e);
        }
    }

    private void enviarEmailEParaSubstituto(Unidade unidade, UnidadeResponsavelDto responsavel, 
                                            Map<String, Usuario> usuarios, String assunto, 
                                            String corpoHtml, String nomeUnidade) {
        String emailUnidade = String.format("%s@tre-pe.jus.br", unidade.getSigla().toLowerCase());
        emailService.enviarEmailHtml(emailUnidade, assunto, corpoHtml);
        log.info("E-mail enviado para {}", unidade.getSigla());

        if (responsavel != null && responsavel.substitutoTitulo() != null) {
            enviarEmailParaSubstituto(responsavel.substitutoTitulo(), usuarios, assunto, corpoHtml, nomeUnidade);
        }
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
