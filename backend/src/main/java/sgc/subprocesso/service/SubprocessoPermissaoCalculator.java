package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.Subprocesso;

/**
 * Calcula as permissões de um usuário para um subprocesso específico.
 * Usa o {@link AccessControlService} centralizado para verificar cada permissão.
 */
@Component
@RequiredArgsConstructor
public class SubprocessoPermissaoCalculator {

    private final AccessControlService accessControlService;

    /**
     * Calcula todas as permissões do usuário para o subprocesso.
     *
     * @param subprocesso O subprocesso alvo
     * @param usuario     O usuário autenticado
     * @return DTO com todas as permissões calculadas
     */
    public SubprocessoPermissoesDto calcular(Subprocesso subprocesso, Usuario usuario) {
        return SubprocessoPermissoesDto.builder()
                .podeVerPagina(podeExecutar(usuario, Acao.VISUALIZAR_SUBPROCESSO, subprocesso))
                .podeEditarMapa(podeExecutar(usuario, Acao.EDITAR_MAPA, subprocesso))
                .podeVisualizarMapa(podeExecutar(usuario, Acao.VISUALIZAR_MAPA, subprocesso))
                .podeDisponibilizarMapa(podeExecutar(usuario, Acao.DISPONIBILIZAR_MAPA, subprocesso))
                .podeDisponibilizarCadastro(podeExecutar(usuario, Acao.DISPONIBILIZAR_CADASTRO, subprocesso))
                .podeDevolverCadastro(podeExecutar(usuario, Acao.DEVOLVER_CADASTRO, subprocesso))
                .podeAceitarCadastro(podeExecutar(usuario, Acao.ACEITAR_CADASTRO, subprocesso))
                .podeVisualizarDiagnostico(podeExecutar(usuario, Acao.VISUALIZAR_DIAGNOSTICO, subprocesso))
                .podeAlterarDataLimite(podeExecutar(usuario, Acao.ALTERAR_DATA_LIMITE, subprocesso))
                .podeVisualizarImpacto(podeExecutar(usuario, Acao.VERIFICAR_IMPACTOS, subprocesso))
                .podeRealizarAutoavaliacao(podeExecutar(usuario, Acao.REALIZAR_AUTOAVALIACAO, subprocesso))
                .podeReabrirCadastro(podeExecutar(usuario, Acao.REABRIR_CADASTRO, subprocesso))
                .podeReabrirRevisao(podeExecutar(usuario, Acao.REABRIR_REVISAO, subprocesso))
                .podeEnviarLembrete(podeExecutar(usuario, Acao.ENVIAR_LEMBRETE_PROCESSO, subprocesso))
                .build();
    }

    private boolean podeExecutar(Usuario usuario, Acao acao, Subprocesso subprocesso) {
        return accessControlService.podeExecutar(usuario, acao, subprocesso);
    }
}
