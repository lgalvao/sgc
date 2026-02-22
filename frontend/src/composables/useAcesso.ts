import { computed, type Ref, unref } from 'vue';
import { usePerfilStore } from '@/stores/perfil';
import { SituacaoSubprocesso, TipoProcesso } from '@/types/tipos';
import type { SubprocessoDetalhe } from '@/types/tipos';

/**
 * Hook to calculate access permissions on the frontend based strictly on the business rules.
 * 
 * CORE RULES (from acesso.md):
 * 1. VISUALIZATION (Read): Based on Ownership (Unidade Responsável) hierarchy.
 *    - Handled mostly by backend filtering, but frontend should assume if it has the DTO, it can view.
 * 2. EXECUTION (Write/Move): Based strictly on Current Location (Localização Atual).
 *    - A user CANNOT execute a workflow action unless the subprocess is physically located at their current unit.
 *    - This applies to ALL profiles, including ADMIN.
 */
export function useAcesso(subprocessoRef: Ref<SubprocessoDetalhe | null> | SubprocessoDetalhe) {
    const perfilStore = usePerfilStore();

    // Helper to unwrap refs
    const getSubprocesso = () => unref(subprocessoRef);

    /**
     * Is the user currently at the same unit where the subprocess is parked?
     * This is the "Golden Rule" for execution.
     */
    const isAtLocation = computed(() => {
        const sp = getSubprocesso();
        if (!sp) return false;
        
        // Ensure both are compared identically (uppercase/trim if necessary, though sigla usually is)
        const localizacaoAtual = sp.localizacaoAtual?.toUpperCase() || '';
        
        // ADMIN is always at 'ADMIN' unit. 
        // Other profiles use their selected unit sigla.
        const unidadeAtual = perfilStore.perfilSelecionado === 'ADMIN' 
            ? 'ADMIN' 
            : perfilStore.unidadeSelecionadaSigla?.toUpperCase() || '';

        return localizacaoAtual === unidadeAtual;
    });

    const isGestorOuAdmin = computed(() => {
        return perfilStore.perfilSelecionado === 'ADMIN' || perfilStore.perfilSelecionado === 'GESTOR';
    });

    const isAdmin = computed(() => perfilStore.perfilSelecionado === 'ADMIN');
    const isGestor = computed(() => perfilStore.perfilSelecionado === 'GESTOR');
    const isChefe = computed(() => perfilStore.perfilSelecionado === 'CHEFE');

    // ==========================================
    // ACTIONS LOGIC
    // ==========================================

    const podeEditarCadastro = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isChefe.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.NAO_INICIADO || 
               sit === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO ||
               sit === SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
    });

    const podeDisponibilizarCadastro = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isChefe.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO ||
               sit === SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
    });

    const podeAceitarCadastro = computed(() => {
        const sp = getSubprocesso();
        // Gestor accepts, Admin doesn't "accept" (Admin homologates)
        if (!sp || !isAtLocation.value || !isGestor.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
               sit === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
    });

    const podeHomologarCadastro = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isAdmin.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
               sit === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
    });

    const podeDevolverCadastro = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isGestorOuAdmin.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
               sit === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
    });

    const podeEditarMapa = computed(() => {
        const sp = getSubprocesso();
        // Admin creates/edits the map before making it available
        if (!sp || !isAtLocation.value || !isAdmin.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO ||
               sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO ||
               sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES ||
               sit === SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES;
    });

    const podeDisponibilizarMapa = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isAdmin.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO;
    });

    const podeValidarMapa = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isChefe.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO;
    });

    const podeApresentarSugestoes = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isChefe.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO;
    });

    const podeAceitarMapa = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isGestor.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
               sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES;
    });

    const podeHomologarMapa = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isAdmin.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
               sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES;
    });

    const podeDevolverMapa = computed(() => {
        const sp = getSubprocesso();
        if (!sp || !isAtLocation.value || !isGestorOuAdmin.value) return false;

        const sit = sp.situacao;
        return sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO ||
               sit === SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_VALIDADO ||
               sit === SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES;
    });

    const podeVisualizarImpacto = computed(() => {
        const sp = getSubprocesso();
        // Visualizar Impactos is an analysis tool, so it's less strictly tied to location for Admin
        if (sp?.tipoProcesso !== TipoProcesso.REVISAO) return false;

        const sit = sp.situacao;
        
        if (isAdmin.value) {
            // ADMIN can verify impacts across the board for revisions
            return sit === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
                   sit === SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA ||
                   sit === SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO;
        }

        if (isGestor.value && isAtLocation.value) {
            return sit === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
        }

        if (isChefe.value && isAtLocation.value) {
            return sit === SituacaoSubprocesso.NAO_INICIADO || 
                   sit === SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
        }

        return false;
    });

    // GLOBAL ADMIN ACTIONS (Not restricted by location)
    const podeAlterarDataLimite = computed(() => {
        const sp = getSubprocesso();
        // ADMIN only, workflow must not be completely finished
        return isAdmin.value && !!sp && sp.isEmAndamento !== false; // Usually true if not finished
    });

    const podeReabrirCadastro = computed(() => {
        const sp = getSubprocesso();
        return isAdmin.value && !!sp; // Usually handled carefully via backend logic
    });

    const podeReabrirRevisao = computed(() => {
        const sp = getSubprocesso();
        return isAdmin.value && !!sp;
    });

    const podeEnviarLembrete = computed(() => {
        const sp = getSubprocesso();
        return isAdmin.value && !!sp && sp.isEmAndamento !== false;
    });

    return {
        isAtLocation,
        podeEditarCadastro,
        podeDisponibilizarCadastro,
        podeDevolverCadastro,
        podeAceitarCadastro,
        podeHomologarCadastro,
        podeEditarMapa,
        podeDisponibilizarMapa,
        podeValidarMapa,
        podeApresentarSugestoes,
        podeDevolverMapa,
        podeAceitarMapa,
        podeHomologarMapa,
        podeVisualizarImpacto,
        podeAlterarDataLimite,
        podeReabrirCadastro,
        podeReabrirRevisao,
        podeEnviarLembrete
    };
}
