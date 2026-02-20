package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

/**
 * Service responsável por calcular permissões de acesso a subprocessos. Responsabilidades:
 * <ul>
 *   <li>Calcular todas as permissões de um usuário em um subprocesso</li>
 *   <li>Verificar se usuário pode executar uma ação específica</li>
 *   <li>Obter permissões de um subprocesso</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
class SubprocessoPermissaoCalculator {

    private final AccessControlService accessControlService;
    private final SubprocessoCrudService crudService;

    /**
     * Verifica se usuário pode executar uma ação em um subprocesso.
     */
    public boolean podeExecutar(Usuario usuario, Acao acao, Subprocesso subprocesso) {
        return accessControlService.podeExecutar(usuario, acao, subprocesso);
    }

    /**
     * Obtém permissões de um subprocesso para um usuário.
     */
    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto obterPermissoes(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        return calcularPermissoes(sp, usuario);
    }

    /**
     * Calcula todas as permissões de um usuário em um subprocesso.
     */
    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto calcularPermissoes(Subprocesso subprocesso, Usuario usuario) {
        boolean isRevisao = subprocesso.getProcesso().getTipo() == TipoProcesso.REVISAO;

        Acao acaoEditarCadastro = isRevisao
                ? Acao.EDITAR_REVISAO_CADASTRO
                : Acao.EDITAR_CADASTRO;

        Acao acaoDisponibilizarCadastro = isRevisao
                ? Acao.DISPONIBILIZAR_REVISAO_CADASTRO
                : Acao.DISPONIBILIZAR_CADASTRO;

        Acao acaoDevolverCadastro = isRevisao
                ? Acao.DEVOLVER_REVISAO_CADASTRO
                : Acao.DEVOLVER_CADASTRO;

        Acao acaoAceitarCadastro = isRevisao
                ? Acao.ACEITAR_REVISAO_CADASTRO
                : Acao.ACEITAR_CADASTRO;

        Acao acaoHomologarCadastro = isRevisao
                ? Acao.HOMOLOGAR_REVISAO_CADASTRO
                : Acao.HOMOLOGAR_CADASTRO;

        return SubprocessoPermissoesDto.builder()
                .podeVerPagina(podeExecutar(usuario, Acao.VISUALIZAR_SUBPROCESSO, subprocesso))
                .podeEditarMapa(podeExecutar(usuario, Acao.EDITAR_MAPA, subprocesso))
                .podeEditarCadastro(podeExecutar(usuario, acaoEditarCadastro, subprocesso))
                .podeVisualizarMapa(podeExecutar(usuario, Acao.VISUALIZAR_MAPA, subprocesso))
                .podeDisponibilizarMapa(podeExecutar(usuario, Acao.DISPONIBILIZAR_MAPA, subprocesso))
                .podeDisponibilizarCadastro(podeExecutar(usuario, acaoDisponibilizarCadastro, subprocesso))
                .podeDevolverCadastro(podeExecutar(usuario, acaoDevolverCadastro, subprocesso))
                .podeAceitarCadastro(podeExecutar(usuario, acaoAceitarCadastro, subprocesso))
                .podeHomologarCadastro(podeExecutar(usuario, acaoHomologarCadastro, subprocesso))
                .podeVisualizarDiagnostico(podeExecutar(usuario, Acao.VISUALIZAR_DIAGNOSTICO, subprocesso))
                .podeAlterarDataLimite(podeExecutar(usuario, Acao.ALTERAR_DATA_LIMITE, subprocesso))
                .podeVisualizarImpacto(podeExecutar(usuario, Acao.VERIFICAR_IMPACTOS, subprocesso))
                .podeRealizarAutoavaliacao(podeExecutar(usuario, Acao.REALIZAR_AUTOAVALIACAO, subprocesso))
                .podeReabrirCadastro(podeExecutar(usuario, Acao.REABRIR_CADASTRO, subprocesso))
                .podeReabrirRevisao(podeExecutar(usuario, Acao.REABRIR_REVISAO, subprocesso))
                .podeEnviarLembrete(podeExecutar(usuario, Acao.ENVIAR_LEMBRETE_PROCESSO, subprocesso))
                .podeApresentarSugestoes(podeExecutar(usuario, Acao.APRESENTAR_SUGESTOES, subprocesso))
                .podeValidarMapa(podeExecutar(usuario, Acao.VALIDAR_MAPA, subprocesso))
                .podeAceitarMapa(podeExecutar(usuario, Acao.ACEITAR_MAPA, subprocesso))
                .podeDevolverMapa(podeExecutar(usuario, Acao.DEVOLVER_MAPA, subprocesso))
                .podeHomologarMapa(podeExecutar(usuario, Acao.HOMOLOGAR_MAPA, subprocesso))
                .build();
    }
}
