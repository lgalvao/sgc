package sgc.processo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.notificacao.NotificacaoModeloEmailService;
import sgc.notificacao.NotificacaoService;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.unidade.modelo.TipoUnidade;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoNotificacaoService {

    private final NotificacaoService notificacaoService;
    private final NotificacaoModeloEmailService notificacaoModeloEmailService;
    private final SgrhService sgrhService;

    public void enviarNotificacoesDeFinalizacao(Processo processo, List<UnidadeProcesso> unidadesParticipantes) {
        log.info("Enviando notificações de finalização para o processo {}", processo.getCodigo());

        if (unidadesParticipantes.isEmpty()) {
            log.warn("Nenhuma unidade participante encontrada para notificar na finalização do processo {}", processo.getCodigo());
            return;
        }

        List<Long> todosCodigosUnidades = unidadesParticipantes.stream().map(UnidadeProcesso::getUnidadeCodigo).toList();
        Map<Long, ResponsavelDto> responsaveis = sgrhService.buscarResponsaveisUnidades(todosCodigosUnidades);
        Map<String, UsuarioDto> usuarios = sgrhService.buscarUsuariosPorTitulos(
            responsaveis.values().stream().map(ResponsavelDto::titularTitulo).distinct().toList()
        );

        for (UnidadeProcesso unidade : unidadesParticipantes) {
            try {
                ResponsavelDto responsavel = Optional.ofNullable(responsaveis.get(unidade.getUnidadeCodigo()))
                    .orElseThrow(() -> new IllegalStateException("Responsável não encontrado para a unidade " + unidade.getSigla()));

                UsuarioDto titular = Optional.ofNullable(usuarios.get(responsavel.titularTitulo()))
                    .orElseThrow(() -> new IllegalStateException("Usuário titular não encontrado: " + responsavel.titularTitulo()));

                String emailTitular = Optional.ofNullable(titular.email()).filter(e -> !e.isBlank())
                    .orElseThrow(() -> new IllegalStateException("E-mail não cadastrado para o titular " + titular.nome()));

                if (unidade.getTipo() == TipoUnidade.OPERACIONAL || unidade.getTipo() == TipoUnidade.INTEROPERACIONAL) {
                    enviarEmailUnidadeFinal(processo, unidade, emailTitular);
                } else if (unidade.getTipo() == TipoUnidade.INTERMEDIARIA) {
                    enviarEmailUnidadeIntermediaria(processo, unidade, emailTitular, unidadesParticipantes);
                }

            } catch (Exception ex) {
                log.error("Falha ao preparar notificação para unidade {} no processo {}: {}", unidade.getSigla(), processo.getCodigo(), ex.getMessage(), ex);
            }
        }
    }

    private void enviarEmailUnidadeFinal(Processo processo, UnidadeProcesso unidade, String email) {
        String assunto = String.format("SGC: Conclusão do processo %s", processo.getDescricao());
        String html = notificacaoModeloEmailService.criarEmailDeProcessoFinalizadoPorUnidade(
            unidade.getSigla(),
            processo.getDescricao()
        );
        notificacaoService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização (unidade final) enviado para {} ({})", unidade.getSigla(), email);
    }

    private void enviarEmailUnidadeIntermediaria(Processo processo, UnidadeProcesso unidadeIntermediaria, String email, List<UnidadeProcesso> todasUnidades) {
        List<String> siglasSubordinadas = todasUnidades.stream()
            .filter(u -> u.getUnidadeSuperiorCodigo() != null && u.getUnidadeSuperiorCodigo().equals(unidadeIntermediaria.getUnidadeCodigo()))
            .map(UnidadeProcesso::getSigla)
            .sorted()
            .toList();

        if (siglasSubordinadas.isEmpty()) {
            log.info("Nenhuma unidade subordinada encontrada para notificar a unidade intermediária {}", unidadeIntermediaria.getSigla());
            return;
        }

        String assunto = String.format("SGC: Conclusão do processo %s em unidades subordinadas", processo.getDescricao());
        String html = notificacaoModeloEmailService.criarEmailDeProcessoFinalizadoUnidadesSubordinadas(
            unidadeIntermediaria.getSigla(),
            processo.getDescricao(),
            siglasSubordinadas
        );

        notificacaoService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização (unidade intermediária) enviado para {} ({})", unidadeIntermediaria.getSigla(), email);
    }
}