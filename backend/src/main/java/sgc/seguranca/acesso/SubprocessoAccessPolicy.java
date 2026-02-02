package sgc.seguranca.acesso;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.service.HierarquiaService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.EnumSet;
import java.util.Map;

import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Política de acesso para operações em subprocessos.
 * Implementa as regras de autorização baseadas em:
 * - Perfil do usuário (ADMIN, GESTOR, CHEFE, SERVIDOR)
 * - Situação do subprocesso
 * - Hierarquia de unidades
 * - Regras específica de cada CDU
 */
@Component
@Slf4j
public class SubprocessoAccessPolicy extends AbstractAccessPolicy<Subprocesso> {
    /**
     * Ações que ADMIN pode executar sem restrição de hierarquia.
     */
    private static final EnumSet<Acao> ACOES_ADMIN_GLOBAIS = EnumSet.of(
            VISUALIZAR_SUBPROCESSO, VISUALIZAR_MAPA, VISUALIZAR_DIAGNOSTICO,
            EDITAR_MAPA, DEVOLVER_CADASTRO, ACEITAR_CADASTRO, HOMOLOGAR_CADASTRO,
            DEVOLVER_REVISAO_CADASTRO, ACEITAR_REVISAO_CADASTRO, HOMOLOGAR_REVISAO_CADASTRO,
            DISPONIBILIZAR_MAPA, VALIDAR_MAPA, HOMOLOGAR_MAPA, DEVOLVER_MAPA,
            ALTERAR_DATA_LIMITE, REABRIR_CADASTRO, REABRIR_REVISAO
    );
    /**
     * Situações permitidas para ADMIN em VERIFICAR_IMPACTOS.
     */
    private static final EnumSet<SituacaoSubprocesso> SITUACOES_VERIFICAR_IMPACTOS_ADMIN = EnumSet.of(
            REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO
    );
    /**
     * Situações permitidas para CHEFE em VERIFICAR_IMPACTOS.
     */
    private static final EnumSet<SituacaoSubprocesso> SITUACOES_VERIFICAR_IMPACTOS_CHEFE = EnumSet.of(
            NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO
    );
    /**
     * Mapeamento de ações para regras de acesso
     */
    private static final Map<Acao, RegrasAcao> REGRAS = Map.ofEntries(
            // ========== CRUD ==========
            Map.entry(LISTAR_SUBPROCESSOS, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.NENHUM)),

            Map.entry(VISUALIZAR_SUBPROCESSO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR, CHEFE, SERVIDOR),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.MESMA_OU_SUBORDINADA)),

            Map.entry(CRIAR_SUBPROCESSO, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.NENHUM)),

            Map.entry(EDITAR_SUBPROCESSO, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.NENHUM)),

            Map.entry(EXCLUIR_SUBPROCESSO, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.NENHUM)),

            Map.entry(ALTERAR_DATA_LIMITE, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.NENHUM)),

            Map.entry(REABRIR_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.NENHUM)),

            Map.entry(REABRIR_REVISAO, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.NENHUM)),

            // ========== CADASTRO ==========
            Map.entry(EDITAR_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    EnumSet.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO),
                    RequisitoHierarquia.MESMA_UNIDADE)),

            Map.entry(DISPONIBILIZAR_CADASTRO, new RegrasAcao(
                    EnumSet.of(CHEFE),
                    EnumSet.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO),
                    RequisitoHierarquia.TITULAR_UNIDADE)),

            Map.entry(DEVOLVER_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR),
                    EnumSet.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO),
                    RequisitoHierarquia.SUPERIOR_IMEDIATA)),

            Map.entry(ACEITAR_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR),
                    EnumSet.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO),
                    RequisitoHierarquia.SUPERIOR_IMEDIATA)),

            Map.entry(HOMOLOGAR_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO),
                    RequisitoHierarquia.NENHUM)),

            // ========== REVISÃO CADASTRO ==========
            Map.entry(EDITAR_REVISAO_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    EnumSet.of(REVISAO_CADASTRO_EM_ANDAMENTO),
                    RequisitoHierarquia.MESMA_UNIDADE)),

            Map.entry(DISPONIBILIZAR_REVISAO_CADASTRO, new RegrasAcao(
                    EnumSet.of(CHEFE),
                    EnumSet.of(REVISAO_CADASTRO_EM_ANDAMENTO),
                    RequisitoHierarquia.TITULAR_UNIDADE)),

            Map.entry(DEVOLVER_REVISAO_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR),
                    EnumSet.of(REVISAO_CADASTRO_DISPONIBILIZADA),
                    RequisitoHierarquia.SUPERIOR_IMEDIATA)),

            Map.entry(ACEITAR_REVISAO_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR),
                    EnumSet.of(REVISAO_CADASTRO_DISPONIBILIZADA),
                    RequisitoHierarquia.SUPERIOR_IMEDIATA)),

            Map.entry(HOMOLOGAR_REVISAO_CADASTRO, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.of(REVISAO_CADASTRO_DISPONIBILIZADA),
                    RequisitoHierarquia.NENHUM)),

            // ========== MAPA ==========
            Map.entry(VISUALIZAR_MAPA, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR, CHEFE, SERVIDOR),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.MESMA_OU_SUBORDINADA)),

            Map.entry(EDITAR_MAPA, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    EnumSet.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                            MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO,
                            MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_CADASTRO_EM_ANDAMENTO,
                            REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO,
                            REVISAO_MAPA_COM_SUGESTOES,
                            DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO),
                    RequisitoHierarquia.MESMA_UNIDADE)),

            Map.entry(DISPONIBILIZAR_MAPA, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.of(MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO,
                            MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_CADASTRO_HOMOLOGADA,
                            REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES),
                    RequisitoHierarquia.NENHUM)),

            Map.entry(VERIFICAR_IMPACTOS, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    EnumSet.of(NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO,
                            REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA,
                            REVISAO_MAPA_AJUSTADO),
                    RequisitoHierarquia.MESMA_UNIDADE)),

            Map.entry(APRESENTAR_SUGESTOES, new RegrasAcao(
                    EnumSet.of(CHEFE),
                    EnumSet.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO),
                    RequisitoHierarquia.MESMA_UNIDADE)),

            Map.entry(VALIDAR_MAPA, new RegrasAcao(
                    EnumSet.of(CHEFE),
                    EnumSet.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO),
                    RequisitoHierarquia.MESMA_UNIDADE)),

            Map.entry(DEVOLVER_MAPA, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR),
                    EnumSet.of(MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                            REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO),
                    RequisitoHierarquia.SUPERIOR_IMEDIATA)),

            Map.entry(ACEITAR_MAPA, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR),
                    EnumSet.of(MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                            REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO),
                    RequisitoHierarquia.SUPERIOR_IMEDIATA)),

            Map.entry(HOMOLOGAR_MAPA, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.of(MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                            REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO),
                    RequisitoHierarquia.NENHUM)),

            Map.entry(AJUSTAR_MAPA, new RegrasAcao(
                    EnumSet.of(ADMIN),
                    EnumSet.of(REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO),
                    RequisitoHierarquia.NENHUM)),

            // ========== DIAGNÓSTICO ==========
            Map.entry(VISUALIZAR_DIAGNOSTICO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR, CHEFE, SERVIDOR),
                    EnumSet.allOf(SituacaoSubprocesso.class),
                    RequisitoHierarquia.MESMA_OU_SUBORDINADA)),

            Map.entry(REALIZAR_AUTOAVALIACAO, new RegrasAcao(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    EnumSet.of(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO),
                    RequisitoHierarquia.MESMA_UNIDADE)));

    public SubprocessoAccessPolicy(UsuarioPerfilRepo usuarioPerfilRepo, HierarquiaService hierarquiaService) {
        super(usuarioPerfilRepo, hierarquiaService);
    }

    @Override
    public boolean canExecute(Usuario usuario, Acao acao, Subprocesso sp) {
        // Caso especial: VERIFICAR_IMPACTOS tem regras diferentes por perfil
        if (acao == VERIFICAR_IMPACTOS) {
            return canExecuteVerificarImpactos(usuario, sp);
        }

        RegrasAcao regras = REGRAS.get(acao);
        if (regras == null) {
            definirMotivoNegacao("Ação não reconhecida: " + acao);
            return false;
        }

        // 1. Verifica perfil
        if (!temPerfilPermitido(usuario, regras.perfisPermitidos)) {
            log.debug("Permissão negada: Usuário {} não tem perfil permitido {} para ação {}", usuario.getTituloEleitoral(), regras.perfisPermitidos, acao);
            definirMotivoNegacao(usuario, regras.perfisPermitidos, acao);
            return false;
        }

        // 2. Verifica situação do subprocesso
        if (!regras.situacoesPermitidas.contains(sp.getSituacao())) {
            log.debug("Permissão negada: Situação {} não permitida para ação {} (Permitidas: {})", sp.getSituacao(), acao, regras.situacoesPermitidas);
            definirMotivoNegacao(String.format(
                    "Ação '%s' não pode ser executada com o sp na situação '%s'. Situações permitidas: %s",
                    acao.getDescricao(),
                    sp.getSituacao().getDescricao(),
                    formatarSituacoes(regras.situacoesPermitidas)));
            return false;
        }

        // 3. Verifica hierarquia (ADMIN é global para ações administrativas)
        if (temPerfil(usuario, ADMIN) && ACOES_ADMIN_GLOBAIS.contains(acao)) {
            return true;
        }

        if (!verificaHierarquia(usuario, sp.getUnidade(), regras.requisitoHierarquia)) {
            log.debug("Permissão negada: Falha no requisito de hierarquia {} para usuário {} na unidade {} para ação {}", 
                regras.requisitoHierarquia, usuario.getTituloEleitoral(), sp.getUnidade().getSigla(), acao);
            definirMotivoNegacao(obterMotivoNegacaoHierarquia(usuario, sp.getUnidade(), regras.requisitoHierarquia));
            return false;
        }

        return true;
    }

    /**
     * Verifica se o usuário pode executar VERIFICAR_IMPACTOS.
     * Esta ação tem regras especiais por perfil:
     * - ADMIN: situações de revisão (DISPONIBILIZADA, HOMOLOGADA, MAPA_AJUSTADO)
     * - GESTOR: apenas REVISAO_CADASTRO_DISPONIBILIZADA
     * - CHEFE: NAO_INICIADO e REVISAO_CADASTRO_EM_ANDAMENTO (mesma unidade)
     */
    private boolean canExecuteVerificarImpactos(Usuario usuario, Subprocesso sp) {
        SituacaoSubprocesso situacao = sp.getSituacao();

        // ADMIN: pode em revisões avançadas
        if (temPerfil(usuario, ADMIN) && SITUACOES_VERIFICAR_IMPACTOS_ADMIN.contains(situacao)) {
            return true;
        }

        // GESTOR: apenas quando revisão está disponibilizada
        if (temPerfil(usuario, GESTOR) && situacao == REVISAO_CADASTRO_DISPONIBILIZADA) {
            return true;
        }

        // CHEFE: situações iniciais, mas precisa estar na mesma unidade
        if (temPerfil(usuario, CHEFE) && SITUACOES_VERIFICAR_IMPACTOS_CHEFE.contains(situacao)) {
            if (verificaHierarquia(usuario, sp.getUnidade(), RequisitoHierarquia.MESMA_UNIDADE)) {
                return true;
            }
            definirMotivoNegacao(obterMotivoNegacaoHierarquia(usuario, sp.getUnidade(), RequisitoHierarquia.MESMA_UNIDADE));
            return false;
        }

        definirMotivoNegacaoVerificarImpactos(usuario, sp);
        return false;
    }

    /**
     * Define a mensagem de erro apropriada para VERIFICAR_IMPACTOS.
     */
    private void definirMotivoNegacaoVerificarImpactos(Usuario usuario, Subprocesso sp) {
        if (!temPerfil(usuario, CHEFE) && !temPerfil(usuario, GESTOR) && !temPerfil(usuario, ADMIN)) {
            definirMotivoNegacao("O usuário não possui um dos perfis necessários: ADMIN, GESTOR, CHEFE");
            return;
        }

        String situacoesPermitidas;
        if (temPerfil(usuario, ADMIN)) {
            situacoesPermitidas = "REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO";
        } else if (temPerfil(usuario, GESTOR)) {
            situacoesPermitidas = "REVISAO_CADASTRO_DISPONIBILIZADA";
        } else {
            situacoesPermitidas = "NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO";
        }

        definirMotivoNegacao(String.format(
                "Ação 'VERIFICAR_IMPACTOS' não pode ser executada com o sp na situação '%s'. "
                        + "Situações permitidas para o perfil do usuário: %s",
                sp.getSituacao().getDescricao(),
                situacoesPermitidas));
    }

    private String formatarSituacoes(EnumSet<SituacaoSubprocesso> situacoes) {
        if (situacoes.size() > 5) {
            return "%d situações".formatted(situacoes.size());
        }
        return situacoes.stream()
                .map(SituacaoSubprocesso::getDescricao)
                .reduce((a, b) -> a + ", " + b)
                .orElse("nenhuma");
    }

    /**
     * Record para armazenar regras de uma ação
     */
    private record RegrasAcao(
            EnumSet<Perfil> perfisPermitidos,
            EnumSet<SituacaoSubprocesso> situacoesPermitidas,
            RequisitoHierarquia requisitoHierarquia) {
    }
}
