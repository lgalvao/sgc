package sgc.processo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoModelosService;
import sgc.processo.model.Processo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoNotificacaoService {
    private final NotificacaoEmailService notificacaoEmailService;
    private final NotificacaoModelosService notificacaoModelosService;
    private final SgrhService sgrhService;

    /**
     * Orquestra o envio de emails de notificação para todas as unidades participantes na
     * finalização de um processo.
     *
     * <p>O método busca os responsáveis e seus emails em lote para otimizar a comunicação com o
     * serviço SGRH. Em seguida, itera sobre as unidades participantes, determinando o tipo de email
     * a ser enviado com base no tipo da unidade (OPERACIONAL, INTERMEDIARIA, etc.).
     *
     * @param processo O processo que foi finalizado.
     * @param unidadesParticipantes A lista de unidades que participaram do processo.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void enviarNotificacoesDeFinalizacao(
            Processo processo, List<Unidade> unidadesParticipantes) {
        log.info("Enviando notificações de finalização para o processo {}", processo.getCodigo());

        if (unidadesParticipantes.isEmpty()) {
            log.warn(
                    "Nenhuma unidade participante encontrada para notificar na finalização do"
                            + " processo {}",
                    processo.getCodigo());
            return;
        }

        List<Long> todosCodigosUnidades =
                unidadesParticipantes.stream().map(Unidade::getCodigo).toList();
        Map<Long, ResponsavelDto> responsaveis =
                sgrhService.buscarResponsaveisUnidades(todosCodigosUnidades);
        Map<String, UsuarioDto> usuarios =
                sgrhService.buscarUsuariosPorTitulos(
                        responsaveis.values().stream()
                                .map(ResponsavelDto::getTitularTitulo)
                                .distinct()
                                .toList());

        for (Unidade unidade : unidadesParticipantes) {
            try {
                ResponsavelDto responsavel =
                        Optional.ofNullable(responsaveis.get(unidade.getCodigo()))
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "Responsável não encontrado para a unidade %s"
                                                                .formatted(unidade.getSigla())));

                UsuarioDto titular =
                        Optional.ofNullable(usuarios.get(responsavel.getTitularTitulo()))
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "Usuário titular não encontrado: %s"
                                                                .formatted(
                                                                        responsavel
                                                                                .getTitularTitulo())));

                String emailTitular =
                        Optional.ofNullable(titular.getEmail())
                                .filter(e -> !e.isBlank())
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "E-mail não cadastrado para o titular %s"
                                                                .formatted(titular.getNome())));

                if (unidade.getTipo() == TipoUnidade.OPERACIONAL
                        || unidade.getTipo() == TipoUnidade.INTEROPERACIONAL) {
                    enviarEmailUnidadeFinal(processo, unidade, emailTitular);
                } else if (unidade.getTipo() == TipoUnidade.INTERMEDIARIA) {
                    enviarEmailUnidadeIntermediaria(
                            processo, unidade, emailTitular, unidadesParticipantes);
                }
            } catch (RuntimeException ex) {
                log.error(
                        "Falha ao preparar notificação para unidade {} no processo {}: {}",
                        unidade.getSigla(),
                        processo.getCodigo(),
                        ex.getMessage(),
                        ex);
            }
        }
    }

    private void enviarEmailUnidadeFinal(Processo processo, Unidade unidade, String email) {
        String assunto = String.format("SGC: Conclusão do processo %s", processo.getDescricao());
        String html =
                notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(
                        unidade.getSigla(), processo.getDescricao());
        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {}", unidade.getSigla());
    }

    private void enviarEmailUnidadeIntermediaria(
            Processo processo,
            Unidade unidadeIntermediaria,
            String email,
            List<Unidade> todasUnidades) {
        List<String> siglasSubordinadas =
                todasUnidades.stream()
                        .filter(
                                u ->
                                        u.getUnidadeSuperior() != null
                                                && u.getUnidadeSuperior()
                                                        .getCodigo()
                                                        .equals(unidadeIntermediaria.getCodigo()))
                        .map(Unidade::getSigla)
                        .sorted()
                        .toList();

        if (siglasSubordinadas.isEmpty()) {
            log.warn(
                    "Nenhuma unidade subordinada encontrada para notificar a unidade intermediária"
                            + " {}",
                    unidadeIntermediaria.getSigla());
            return;
        }

        String assunto =
                String.format(
                        "SGC: Conclusão do processo %s em unidades subordinadas",
                        processo.getDescricao());
        String html =
                notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(
                        unidadeIntermediaria.getSigla(),
                        processo.getDescricao(),
                        siglasSubordinadas);

        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {})", unidadeIntermediaria.getSigla());
    }
}
