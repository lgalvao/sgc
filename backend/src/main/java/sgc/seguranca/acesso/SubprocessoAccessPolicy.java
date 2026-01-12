package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sgc.organizacao.ServicoHierarquia;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Política de acesso para operações em subprocessos.
 * Implementa as regras de autorização baseadas em:
 * - Perfil do usuário (ADMIN, GESTOR, CHEFE, SERVIDOR)
 * - Situação do subprocesso
 * - Hierarquia de unidades
 * - Regras específicas de cada CDU
 */
@Component
@RequiredArgsConstructor
public class SubprocessoAccessPolicy implements AccessPolicy<Subprocesso> {

        private final ServicoHierarquia servicoHierarquia;

        private String ultimoMotivoNegacao = "";

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

        @Override
        public boolean canExecute(Usuario usuario, Acao acao, Subprocesso subprocesso) {
                ultimoMotivoNegacao = "";
                // Caso especial: VERIFICAR_IMPACTOS tem regras diferentes por perfil
                if (acao == VERIFICAR_IMPACTOS) {
                        return canExecuteVerificarImpactos(usuario, subprocesso);
                }

                RegrasAcao regras = REGRAS.get(acao);
                if (regras == null) {
                        ultimoMotivoNegacao = "Ação não reconhecida: " + acao;
                        return false;
                }

                // 1. Verifica perfil
                if (!temPerfilPermitido(usuario, regras.perfisPermitidos)) {
                        ultimoMotivoNegacao = String.format(
                                        "Usuário '%s' não possui um dos perfis necessários: %s",
                                        usuario.getTituloEleitoral(),
                                        formatarPerfis(regras.perfisPermitidos));
                        return false;
                }

                // 2. Verifica situação do subprocesso
                if (!regras.situacoesPermitidas.contains(subprocesso.getSituacao())) {
                        ultimoMotivoNegacao = String.format(
                                        "Ação '%s' não pode ser executada com o subprocesso na situação '%s'. Situações permitidas: %s",
                                        acao.getDescricao(),
                                        subprocesso.getSituacao().getDescricao(),
                                        formatarSituacoes(regras.situacoesPermitidas));
                        return false;
                }

                // 3. Verifica hierarquia
                // Caso especial: ADMIN é global e não precisa verificar hierarquia para ações
                // administrativas
                boolean isAdmin = usuario.getTodasAtribuicoes().stream()
                                .anyMatch(a -> a.getPerfil() == ADMIN);

                // ADMIN pode executar ações administrativas e de edição de mapa sem restrição
                // de hierarquia
                if (isAdmin && isAcaoAdministrativaOuEdicaoMapa(acao)) {
                        return true;
                }

                if (!verificaHierarquia(usuario, subprocesso, regras.requisitoHierarquia)) {
                        ultimoMotivoNegacao = obterMotivoNegacaoHierarquia(
                                        usuario, subprocesso, regras.requisitoHierarquia);
                        return false;
                }

                return true;
        }

        /**
         * Regras específicas para VERIFICAR_IMPACTOS baseadas na implementação original
         * do MapaAcessoService.
         * Cada perfil tem situações diferentes permitidas:
         * - CHEFE: NAO_INICIADO ou REVISAO_CADASTRO_EM_ANDAMENTO (e deve estar na mesma
         * unidade)
         * - GESTOR: REVISAO_CADASTRO_DISPONIBILIZADA (sem verificação de unidade)
         * - ADMIN: REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA,
         * REVISAO_MAPA_AJUSTADO (sem verificação de unidade)
         */
        private boolean canExecuteVerificarImpactos(Usuario usuario, Subprocesso subprocesso) {
                SituacaoSubprocesso situacao = subprocesso.getSituacao();
                Set<Perfil> perfisUsuario = usuario.getTodasAtribuicoes().stream()
                                .map(UsuarioPerfil::getPerfil)
                                .collect(java.util.stream.Collectors.toSet());

                if (isChefeVerificarImpactosPermitido(perfisUsuario, situacao, usuario, subprocesso)) {
                        return true;
                }

                if (perfisUsuario.contains(GESTOR) && situacao == REVISAO_CADASTRO_DISPONIBILIZADA) {
                        return true;
                }

                if (perfisUsuario.contains(ADMIN) &&
                                (situacao == REVISAO_CADASTRO_DISPONIBILIZADA
                                                || situacao == REVISAO_CADASTRO_HOMOLOGADA
                                                || situacao == REVISAO_MAPA_AJUSTADO)) {
                        return true;
                }

                if (!ultimoMotivoNegacao.isEmpty()) {
                        return false;
                }

                definirMotivoNegacaoVerificarImpactos(perfisUsuario, subprocesso);
                return false;
        }

        private boolean isChefeVerificarImpactosPermitido(Set<Perfil> perfis, SituacaoSubprocesso situacao,
                        Usuario usuario, Subprocesso subprocesso) {
                if (perfis.contains(CHEFE) && (situacao == NAO_INICIADO || situacao == REVISAO_CADASTRO_EM_ANDAMENTO)) {
                        if (verificaHierarquia(usuario, subprocesso, RequisitoHierarquia.MESMA_UNIDADE)) {
                                return true;
                        }
                        ultimoMotivoNegacao = obterMotivoNegacaoHierarquia(
                                        usuario, subprocesso, RequisitoHierarquia.MESMA_UNIDADE);
                        return false;
                }
                return false;
        }

        private void definirMotivoNegacaoVerificarImpactos(Set<Perfil> perfis, Subprocesso subprocesso) {
                if (!perfis.contains(CHEFE) && !perfis.contains(GESTOR) && !perfis.contains(ADMIN)) {
                        ultimoMotivoNegacao = "O usuário não possui um dos perfis necessários: ADMIN, GESTOR, CHEFE";
                        return;
                }

                String situacoesPermitidas = switch (perfis.stream().findFirst().orElse(null)) {
                        case ADMIN ->
                                "REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO";
                        case GESTOR -> "REVISAO_CADASTRO_DISPONIBILIZADA";
                        default -> "NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO";
                };

                if (perfis.contains(ADMIN)) {
                        situacoesPermitidas = "REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO";
                } else if (perfis.contains(GESTOR)) {
                        situacoesPermitidas = "REVISAO_CADASTRO_DISPONIBILIZADA";
                }

                ultimoMotivoNegacao = String.format(
                                "Ação 'VERIFICAR_IMPACTOS' não pode ser executada com o subprocesso na situação '%s'. "
                                                +
                                                "Situações permitidas para o perfil do usuário: %s",
                                subprocesso.getSituacao().getDescricao(),
                                situacoesPermitidas);
        }

        @Override
        public String getMotivoNegacao() {
                return ultimoMotivoNegacao;
        }

        private boolean temPerfilPermitido(Usuario usuario, EnumSet<Perfil> perfisPermitidos) {
                return usuario.getTodasAtribuicoes().stream()
                                .anyMatch(a -> perfisPermitidos.contains(a.getPerfil()));
        }

        private boolean verificaHierarquia(
                        Usuario usuario, Subprocesso subprocesso, RequisitoHierarquia requisito) {

                Unidade unidadeSubprocesso = subprocesso.getUnidade();
                if (unidadeSubprocesso == null) {
                        return requisito == RequisitoHierarquia.NENHUM;
                }

                return switch (requisito) {
                        case NENHUM -> true;

                        case MESMA_UNIDADE -> usuario.getTodasAtribuicoes().stream()
                                        .anyMatch(a -> a.getUnidade() != null
                                                        && Objects.equals(a.getUnidade().getCodigo(),
                                                                        unidadeSubprocesso.getCodigo()));

                        case MESMA_OU_SUBORDINADA -> usuario.getTodasAtribuicoes().stream()
                                        .anyMatch(a -> a.getUnidade() != null
                                                        && (Objects.equals(a.getUnidade().getCodigo(),
                                                                        unidadeSubprocesso.getCodigo())
                                                                        || servicoHierarquia.isSubordinada(
                                                                                        unidadeSubprocesso,
                                                                                        a.getUnidade())));

                        case SUPERIOR_IMEDIATA -> usuario.getTodasAtribuicoes().stream()
                                        .anyMatch(a -> a.getUnidade() != null
                                                        && servicoHierarquia.isSuperiorImediata(unidadeSubprocesso,
                                                                        a.getUnidade()));

                        case TITULAR_UNIDADE -> {
                                String tituloTitular = unidadeSubprocesso.getTituloTitular();
                                yield tituloTitular.equals(usuario.getTituloEleitoral());
                        }
                };
        }

        /**
         * Verifica se a ação é administrativa ou de edição de mapa.
         * ADMIN possui privilégios globais e pode executar estas ações sem restrição de
         * hierarquia.
         */
        private boolean isAcaoAdministrativaOuEdicaoMapa(Acao acao) {
                return acao == VISUALIZAR_SUBPROCESSO
                                || acao == VISUALIZAR_MAPA
                                || acao == VISUALIZAR_DIAGNOSTICO
                                || acao == EDITAR_MAPA
                                || acao == DEVOLVER_CADASTRO
                                || acao == ACEITAR_CADASTRO
                                || acao == HOMOLOGAR_CADASTRO
                                || acao == DEVOLVER_REVISAO_CADASTRO
                                || acao == ACEITAR_REVISAO_CADASTRO
                                || acao == HOMOLOGAR_REVISAO_CADASTRO
                                || acao == DISPONIBILIZAR_MAPA
                                || acao == VALIDAR_MAPA
                                || acao == HOMOLOGAR_MAPA
                                || acao == DEVOLVER_MAPA
                                || acao == ALTERAR_DATA_LIMITE
                                || acao == REABRIR_CADASTRO
                                || acao == REABRIR_REVISAO;
        }

        private String obterMotivoNegacaoHierarquia(
                        Usuario usuario, Subprocesso subprocesso, RequisitoHierarquia requisito) {

                Unidade unidadeSubprocesso = subprocesso.getUnidade();
                String siglaUnidade = unidadeSubprocesso != null ? unidadeSubprocesso.getSigla() : "não definida";

                return switch (requisito) {
                        case MESMA_UNIDADE -> String.format(
                                        "Usuário '%s' não pertence à unidade '%s' do subprocesso",
                                        usuario.getTituloEleitoral(), siglaUnidade);
                        case MESMA_OU_SUBORDINADA -> String.format(
                                        "Usuário '%s' não pertence à unidade '%s' nem a uma unidade superior na hierarquia",
                                        usuario.getTituloEleitoral(), siglaUnidade);
                        case SUPERIOR_IMEDIATA -> String.format(
                                        "Usuário '%s' não pertence à unidade superior imediata da unidade '%s'",
                                        usuario.getTituloEleitoral(), siglaUnidade);
                        case TITULAR_UNIDADE -> {
                                String tituloTitular = unidadeSubprocesso != null
                                                ? unidadeSubprocesso.getTituloTitular()
                                                : "não definido";
                                yield String.format(
                                                "Usuário '%s' não é o titular da unidade '%s'. Titular: %s",
                                                usuario.getTituloEleitoral(), siglaUnidade, tituloTitular);
                        }
                        case NENHUM -> "Erro inesperado na verificação de hierarquia";
                };
        }

        private String formatarPerfis(EnumSet<Perfil> perfis) {
                return perfis.stream()
                                .map(Perfil::name)
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("nenhum");
        }

        private String formatarSituacoes(EnumSet<SituacaoSubprocesso> situacoes) {
                if (situacoes.size() > 5) {
                        return situacoes.size() + " situações";
                }
                return situacoes.stream()
                                .map(SituacaoSubprocesso::getDescricao)
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("nenhuma");
        }

        /**
         * Enum para requisitos de hierarquia de unidades
         */
        private enum RequisitoHierarquia {
                NENHUM, // Sem verificação de hierarquia
                MESMA_UNIDADE, // Usuário deve estar na mesma unidade
                MESMA_OU_SUBORDINADA, // Usuário deve estar na mesma unidade ou em unidade superior
                SUPERIOR_IMEDIATA, // Usuário deve estar na unidade superior imediata
                TITULAR_UNIDADE // Usuário deve ser o titular da unidade
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
