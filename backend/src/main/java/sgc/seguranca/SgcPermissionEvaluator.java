package sgc.seguranca;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.security.access.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.io.*;
import java.util.*;

import static sgc.organizacao.model.Perfil.*;

/**
 * Avaliador central de permissões do SGC.
 * Implementa a interface padrão do Spring Security para uso em expressões @PreAuthorize.
 * Consolida as regras de acesso baseadas na "Regra de Ouro":
 * 1. Visualização (Leitura): Hierarquia da Unidade Responsável.
 * 2. Execução (Escrita): Localização Atual do Subprocesso (Unidade do Usuário == Localização).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SgcPermissionEvaluator implements PermissionEvaluator {

    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final HierarquiaService hierarquiaService;
    private final ProcessoRepo processoRepo;

    // Ações de Escrita que exigem Localização
    private static final Set<String> ACOES_ESCRITA = Set.of(
            "EDITAR_CADASTRO", "DISPONIBILIZAR_CADASTRO", "DEVOLVER_CADASTRO", "ACEITAR_CADASTRO", "HOMOLOGAR_CADASTRO",
            "EDITAR_REVISAO_CADASTRO", "DISPONIBILIZAR_REVISAO_CADASTRO", "DEVOLVER_REVISAO_CADASTRO", "ACEITAR_REVISAO_CADASTRO", "HOMOLOGAR_REVISAO_CADASTRO",
            "EDITAR_MAPA", "DISPONIBILIZAR_MAPA", "APRESENTAR_SUGESTOES", "VALIDAR_MAPA", "DEVOLVER_MAPA", "ACEITAR_MAPA", "HOMOLOGAR_MAPA", "AJUSTAR_MAPA",
            "REALIZAR_AUTOAVALIACAO", "IMPORTAR_ATIVIDADES", "ALTERAR_DATA_LIMITE", "REABRIR_CADASTRO", "REABRIR_REVISAO", "ENVIAR_LEMBRETE_PROCESSO"
    );

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (!(authentication.getPrincipal() instanceof Usuario usuario)) {
            return false;
        }

        if (targetDomainObject instanceof Collection<?> collection) {
            return collection.stream().allMatch(item -> hasPermission(authentication, item, permission));
        }

        String acao = (String) permission;

        if (targetDomainObject instanceof Subprocesso sp) {
            return checkSubprocesso(usuario, sp, acao);
        } else if (targetDomainObject instanceof Processo p) {
            return checkProcesso(usuario, p, acao);
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (!(authentication.getPrincipal() instanceof Usuario usuario)) {
            return false;
        }

        if (targetId instanceof Collection<?> collection) {
             return collection.stream().allMatch(id -> hasPermission(authentication, (Serializable) id, targetType, permission));
        }

        String acao = (String) permission;

        if ("Subprocesso".equals(targetType)) {
            return subprocessoRepo.findById((Long) targetId)
                    .map(sp -> checkSubprocesso(usuario, sp, acao))
                    .orElse(false);
        } else if ("Processo".equals(targetType)) {
             return processoRepo.findById((Long) targetId)
                    .map(p -> checkProcesso(usuario, p, acao))
                    .orElse(false);
        }

        return false;
    }

    public boolean checkPermission(@Nullable Usuario usuario, @Nullable Object targetDomainObject, String permission) {
        if (usuario == null) return false;

        return switch (targetDomainObject) {
            case Collection<?> collection ->
                    collection.stream().allMatch(item -> checkPermission(usuario, item, permission));
            case Subprocesso sp -> checkSubprocesso(usuario, sp, permission);
            case Processo processo -> checkProcesso(usuario, processo, permission);
            case null, default -> false;
        };

    }

    private boolean checkSubprocesso(Usuario usuario, Subprocesso sp, String acao) {
        if (sp.getProcesso() != null && sp.getProcesso().getSituacao() == sgc.processo.model.SituacaoProcesso.FINALIZADO) {
            return !isAcaoEscrita(acao);
        }

        Perfil perfil = usuario.getPerfilAtivo();
        boolean isAdmin = perfil == ADMIN;
        boolean isEscrita = isAcaoEscrita(acao);

        // Visualização (Leitura) - Baseada na Hierarquia
        if (!isEscrita) {
            if (isAdmin) return true; // Admin vê tudo

            // Exceção: Importação permite ler de outras unidades para obter atividades/conhecimentos
            if ("CONSULTAR_PARA_IMPORTACAO".equals(acao) && (usuario.getPerfilAtivo() == CHEFE || usuario.getPerfilAtivo() == GESTOR)) {
                return true;
            }

            // CDU-12: Exceção à regra de visualização baseada apenas em hierarquia
            if ("VERIFICAR_IMPACTOS".equals(acao)) {
                return true; // Permitir visualização se for verificação de impactos (controle de estado feito no serviço)
            }

            return checkHierarquia(usuario, sp.getUnidade());
        }

        // 2. Validação de Perfil (RBAC) para ações de escrita
        if (!checkPerfil(usuario, acao)) {
            return false;
        }

        // 3. Execução (Escrita) - Baseada na Localização Atual (Regra de Ouro)
        // O usuário só pode executar se o subprocesso estiver na sua unidade
        Unidade localizacao = obterUnidadeLocalizacao(sp);
        boolean permitido = Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacao.getCodigo());
        if (!permitido) {
            log.debug("Acesso de ESCRITA negado para a ação '{}'. Usuário: {} (Unidade Ativa: {}). Subprocesso {} localizado em {}.",
                    acao, usuario.getTituloEleitoral(), usuario.getUnidadeAtivaCodigo(), sp.getCodigo(), localizacao.getCodigo());
        }
        return permitido;
    }

    private boolean checkPerfil(Usuario usuario, String acao) {
        Perfil perfil = usuario.getPerfilAtivo();

        if (acao.startsWith("HOMOLOGAR")) {
            return perfil == ADMIN;
        }
        if (acao.startsWith("ACEITAR")) {
            return perfil == GESTOR;
        }
        if (acao.startsWith("DEVOLVER")) {
            return perfil == ADMIN || perfil == GESTOR;
        }

        // Ações de Mapa exclusivas de ADMIN
        if (Set.of("EDITAR_MAPA", "DISPONIBILIZAR_MAPA", "AJUSTAR_MAPA").contains(acao)) {
            return perfil == ADMIN;
        }

        // Ações de validação de mapa (Chefe)
        if (Set.of("APRESENTAR_SUGESTOES", "VALIDAR_MAPA").contains(acao)) {
            return perfil == CHEFE;
        }

        // Ações de Cadastro (Chefe)
        if (Set.of("EDITAR_CADASTRO", "DISPONIBILIZAR_CADASTRO", "EDITAR_REVISAO_CADASTRO", "DISPONIBILIZAR_REVISAO_CADASTRO", "IMPORTAR_ATIVIDADES").contains(acao)) {
            return perfil == CHEFE;
        }

        // Verificar impactos (Todos, mas depende da localização)
        if ("VERIFICAR_IMPACTOS".equals(acao)) {
            return perfil == CHEFE || perfil == GESTOR || perfil == ADMIN;
        }

        return true;
    }

    private boolean checkProcesso(Usuario usuario, @Nullable Processo processo, String acao) {
        if (processo != null && processo.getSituacao() == sgc.processo.model.SituacaoProcesso.FINALIZADO) {
            return "VISUALIZAR_PROCESSO".equals(acao);
        }

        Perfil perfil = usuario.getPerfilAtivo();
        // Regras para Processo (baseadas em Perfil)
        boolean isAdmin = perfil == ADMIN;
        if (isAdmin) return true; // Admin faz tudo no Processo

        // Gestor e Chefe podem ver e realizar ações em bloco
        if (perfil == GESTOR || perfil == CHEFE) {
            return Set.of(
                    "VISUALIZAR_PROCESSO",
                    "HOMOLOGAR_CADASTRO_EM_BLOCO",
                    "HOMOLOGAR_MAPA_EM_BLOCO",
                    "ACEITAR_CADASTRO_EM_BLOCO",
                    "DISPONIBILIZAR_MAPA_EM_BLOCO" 
            ).contains(acao);
        }

        return false;
    }

    private boolean isAcaoEscrita(String acao) {
        return ACOES_ESCRITA.contains(acao);
    }

    private boolean checkHierarquia(Usuario usuario, Unidade unidadeAlvo) {
        // Chefe e Servidor veem apenas sua unidade
        if (usuario.getPerfilAtivo() == CHEFE || usuario.getPerfilAtivo() == SERVIDOR) {
            return Objects.equals(usuario.getUnidadeAtivaCodigo(), unidadeAlvo.getCodigo());
        }

        // Gestor vê sua unidade e subordinadas
        if (usuario.getPerfilAtivo() == GESTOR) {
            // Assume-se que o usuário logado tem sua unidade ativa carregada ou acessível via código
            Unidade unidadeUsuario = Unidade.builder().codigo(usuario.getUnidadeAtivaCodigo()).build();
            return hierarquiaService.isMesmaOuSubordinada(unidadeAlvo, unidadeUsuario);
        }

        log.info("Acesso negado por hierarquia para {} (Perfil: {}, Unidade Ativa: {}). Unidade alvo: {}.",
                usuario.getTituloEleitoral(), usuario.getPerfilAtivo(), usuario.getUnidadeAtivaCodigo(), unidadeAlvo.getCodigo());
        return false;
    }

    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtual() != null) return sp.getLocalizacaoAtual();

        if (sp.getCodigo() == null) return sp.getUnidade();

        Unidade localizacao = movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo()).filter(m -> m.getUnidadeDestino() != null).map(Movimentacao::getUnidadeDestino).orElse(sp.getUnidade());

        sp.setLocalizacaoAtual(localizacao);
        return localizacao;
    }
}
