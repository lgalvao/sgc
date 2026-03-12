package sgc.seguranca;

import lombok.*;
import sgc.organizacao.model.*;

import java.util.*;

import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.AcaoPermissao.Tipo.*;

/**
 * Define todas as ações de permissão do sistema e seus requisitos de acesso.
 * Cada ação carrega: quais perfis podem executá-la e se depende da localização do subprocesso.
 *
 * <p>Usado pelo {@link SgcPermissionEvaluator} para validar permissões.
 * Os nomes dos valores correspondem exatamente às strings usadas nas anotações {@code @PreAuthorize}.
 */
@Getter
@RequiredArgsConstructor
public enum AcaoPermissao {

    // ── Cadastro de atividades ──────────────────────────────────────
    EDITAR_CADASTRO(ESCRITA, CHEFE),
    DISPONIBILIZAR_CADASTRO(ESCRITA, CHEFE),
    DEVOLVER_CADASTRO(ESCRITA, ADMIN, GESTOR),
    ACEITAR_CADASTRO(ESCRITA, GESTOR),
    HOMOLOGAR_CADASTRO(ESCRITA, ADMIN),

    // ── Revisão de cadastro ─────────────────────────────────────────
    EDITAR_REVISAO_CADASTRO(ESCRITA, CHEFE),
    DISPONIBILIZAR_REVISAO_CADASTRO(ESCRITA, CHEFE),
    DEVOLVER_REVISAO_CADASTRO(ESCRITA, ADMIN, GESTOR),
    ACEITAR_REVISAO_CADASTRO(ESCRITA, GESTOR),
    HOMOLOGAR_REVISAO_CADASTRO(ESCRITA, ADMIN),

    // ── Mapa de competências ────────────────────────────────────────
    EDITAR_MAPA(ESCRITA, ADMIN),
    DISPONIBILIZAR_MAPA(ESCRITA, ADMIN),
    AJUSTAR_MAPA(ESCRITA, ADMIN),
    APRESENTAR_SUGESTOES(ESCRITA, CHEFE),
    VALIDAR_MAPA(ESCRITA, CHEFE),
    DEVOLVER_MAPA(ESCRITA, ADMIN, GESTOR),
    ACEITAR_MAPA(ESCRITA, GESTOR),
    HOMOLOGAR_MAPA(ESCRITA, ADMIN),

    // ── Importação ──────────────────────────────────────────────────
    IMPORTAR_ATIVIDADES(ESCRITA, CHEFE),
    CONSULTAR_PARA_IMPORTACAO(LEITURA, CHEFE),

    // ── Visualização ────────────────────────────────────────────────
    VISUALIZAR_SUBPROCESSO(LEITURA),
    VERIFICAR_IMPACTOS(LEITURA, CHEFE, GESTOR, ADMIN),

    // ── Ações de processo ───────────────────────────────────────────
    VISUALIZAR_PROCESSO(LEITURA),
    FINALIZAR_PROCESSO(LEITURA, ADMIN),
    ACEITAR_CADASTRO_EM_BLOCO(LEITURA, GESTOR),
    HOMOLOGAR_CADASTRO_EM_BLOCO(LEITURA, ADMIN),
    HOMOLOGAR_MAPA_EM_BLOCO(LEITURA, ADMIN),
    DISPONIBILIZAR_MAPA_EM_BLOCO(LEITURA, ADMIN);

    private final Tipo tipo;
    private final Set<Perfil> perfisPermitidos;

    AcaoPermissao(Tipo tipo, Perfil... perfis) {
        this.tipo = tipo;
        this.perfisPermitidos = perfis.length == 0 ? Set.of() : Set.of(perfis);
    }

    /** Indica se a ação exige que a unidade do usuário seja igual à localização do subprocesso. */
    public boolean dependeLocalizacao() {
        return tipo == ESCRITA;
    }

    /**
     * Verifica se o perfil informado tem permissão para executar esta ação.
     * Se {@code perfisPermitidos} estiver vazio, qualquer perfil é aceito.
     */
    public boolean permitePerfil(Perfil perfil) {
        return perfisPermitidos.isEmpty() || perfisPermitidos.contains(perfil);
    }

    /** Classifica a ação quanto à dependência de localização. */
    public enum Tipo {
        /** Ações que alteram dados — exigem localização do subprocesso na unidade do usuário. */
        ESCRITA,
        /** Ações de consulta — verificam apenas hierarquia organizacional. */
        LEITURA
    }
}
