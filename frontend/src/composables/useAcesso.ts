import {computed, type Ref, unref} from 'vue';
import {type PermissoesSubprocesso, type SubprocessoDetalhe, Perfil, TipoProcesso} from '@/types/tipos';
import {TEXTOS} from '@/constants/textos';
import {usePerfil} from '@/composables/usePerfil';
import {PERMISSOES_SUBPROCESSO_VAZIAS} from '@/utils/permissoesSubprocesso';

type AcaoPrincipalCadastro = {
    codigo: 'ACEITAR' | 'HOMOLOGAR';
    mostrar: boolean;
    habilitar: boolean;
    tituloModal: string;
    textoModal: string;
    rotuloBotao: string;
    rotuloConfirmacao: string;
    mensagemSucesso: string;
    redirecionarParaPainel: boolean;
};

type AcaoPrincipalMapa = {
    codigo: 'ACEITAR' | 'HOMOLOGAR';
    mostrar: boolean;
    habilitar: boolean;
    rotuloBotao: string;
    mensagemSucesso: string;
};

/**
 * Hook to access permissions calculated by the backend.
 *
 * The backend is the single source of truth for security and workflow rules.
 * This hook simplifies access to those rules in Vue components.
 */
export function useAcesso(subprocessoRef: Ref<SubprocessoDetalhe | null> | SubprocessoDetalhe) {
    const getSubprocesso = () => unref(subprocessoRef);
    const getPermissoes = () => getSubprocesso()?.permissoes;
    const isRevisao = computed(() => getSubprocesso()?.tipoProcesso === TipoProcesso.REVISAO);
    const {perfilSelecionado} = usePerfil();
    const isAdmin = computed(() => perfilSelecionado.value === Perfil.ADMIN);
    const isGestor = computed(() => perfilSelecionado.value === Perfil.GESTOR);
    const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);

    // Dynamic permission computeds to reduce boilerplate
    const permissoes = computed<PermissoesSubprocesso>(() => getPermissoes() ?? PERMISSOES_SUBPROCESSO_VAZIAS);
    
    const podeAnalisarCadastro = computed(() => 
        permissoes.value.podeDevolverCadastro || 
        permissoes.value.podeAceitarCadastro || 
        permissoes.value.podeHomologarCadastro
    );

    const podeAnalisarMapa = computed(() => 
        permissoes.value.podeDevolverMapa || 
        permissoes.value.podeAceitarMapa || 
        permissoes.value.podeHomologarMapa
    );

    const podeVisualizarImpacto = computed(() => 
        isRevisao.value && (permissoes.value.podeVisualizarImpacto ?? false)
    );

    const mostrarAlterarDataLimite = computed(() => isAdmin.value);
    const mostrarReabrirCadastro = computed(() => isAdmin.value);
    const mostrarReabrirRevisao = computed(() => isAdmin.value);
    const mostrarEnviarLembrete = computed(() => isAdmin.value);

    const mostrarImportarAtividades = computed(() => isChefe.value);
    const mostrarDisponibilizarCadastro = computed(() => isChefe.value);
    const mostrarDevolverCadastro = computed(() => isAdmin.value || isGestor.value);

    const mostrarApresentarSugestoes = computed(() => isChefe.value);
    const mostrarValidarMapa = computed(() => isChefe.value);
    const mostrarDisponibilizarMapa = computed(() => isAdmin.value);
    const mostrarDevolverMapa = computed(() => isAdmin.value || isGestor.value);

    const acaoPrincipalCadastro = computed<AcaoPrincipalCadastro | null>(() => {
        if (isAdmin.value) {
            return {
                codigo: 'HOMOLOGAR',
                mostrar: true,
                habilitar: permissoes.value.podeHomologarCadastro && permissoes.value.habilitarHomologarCadastro,
                tituloModal: TEXTOS.atividades.MODAL_HOMOLOGAR_TITULO,
                textoModal: TEXTOS.atividades.MODAL_HOMOLOGAR_TEXTO,
                rotuloBotao: TEXTOS.atividades.BOTAO_HOMOLOGAR,
                rotuloConfirmacao: TEXTOS.comum.BOTAO_HOMOLOGAR,
                mensagemSucesso: TEXTOS.sucesso.HOMOLOGACAO_EFETIVADA,
                redirecionarParaPainel: false,
            };
        }

        if (isGestor.value) {
            return {
                codigo: 'ACEITAR',
                mostrar: true,
                habilitar: permissoes.value.podeAceitarCadastro && permissoes.value.habilitarAceitarCadastro,
                tituloModal: isRevisao.value
                    ? TEXTOS.atividades.MODAL_ACEITE_REVISAO_TITULO
                    : TEXTOS.atividades.MODAL_VALIDAR_TITULO,
                textoModal: isRevisao.value
                    ? TEXTOS.atividades.MODAL_ACEITE_REVISAO_TEXTO
                    : TEXTOS.atividades.MODAL_VALIDAR_TEXTO,
                rotuloBotao: TEXTOS.comum.BOTAO_REGISTRAR_ACEITE,
                rotuloConfirmacao: TEXTOS.comum.BOTAO_REGISTRAR_ACEITE,
                mensagemSucesso: TEXTOS.sucesso.ACEITE_REGISTRADO,
                redirecionarParaPainel: true,
            };
        }

        return null;
    });

    const acaoPrincipalMapa = computed<AcaoPrincipalMapa | null>(() => {
        if (isAdmin.value) {
            return {
                codigo: 'HOMOLOGAR',
                mostrar: true,
                habilitar: permissoes.value.podeHomologarMapa && permissoes.value.habilitarHomologarMapa,
                rotuloBotao: TEXTOS.mapa.LABEL_HOMOLOGAR,
                mensagemSucesso: TEXTOS.mapa.SUCESSO_HOMOLOGACAO,
            };
        }

        if (isGestor.value) {
            return {
                codigo: 'ACEITAR',
                mostrar: true,
                habilitar: permissoes.value.podeAceitarMapa && permissoes.value.habilitarAceitarMapa,
                rotuloBotao: TEXTOS.mapa.LABEL_REGISTRAR_ACEITE,
                mensagemSucesso: TEXTOS.sucesso.ACEITE_REGISTRADO,
            };
        }

        return null;
    });

    return {
        // Pass-through permissions
        podeEditarCadastro: computed(() => permissoes.value.podeEditarCadastro ?? false),
        podeDisponibilizarCadastro: computed(() => permissoes.value.podeDisponibilizarCadastro ?? false),
        podeDevolverCadastro: computed(() => permissoes.value.podeDevolverCadastro ?? false),
        podeAceitarCadastro: computed(() => permissoes.value.podeAceitarCadastro ?? false),
        podeHomologarCadastro: computed(() => permissoes.value.podeHomologarCadastro ?? false),
        podeEditarMapa: computed(() => permissoes.value.podeEditarMapa ?? false),
        podeDisponibilizarMapa: computed(() => permissoes.value.podeDisponibilizarMapa ?? false),
        podeValidarMapa: computed(() => permissoes.value.podeValidarMapa ?? false),
        podeApresentarSugestoes: computed(() => permissoes.value.podeApresentarSugestoes ?? false),
        podeVerSugestoes: computed(() => permissoes.value.podeVerSugestoes ?? false),
        podeDevolverMapa: computed(() => permissoes.value.podeDevolverMapa ?? false),
        podeAceitarMapa: computed(() => permissoes.value.podeAceitarMapa ?? false),
        podeHomologarMapa: computed(() => permissoes.value.podeHomologarMapa ?? false),
        podeAlterarDataLimite: computed(() => permissoes.value.podeAlterarDataLimite ?? false),
        podeReabrirCadastro: computed(() => permissoes.value.podeReabrirCadastro ?? false),
        podeReabrirRevisao: computed(() => permissoes.value.podeReabrirRevisao ?? false),
        podeEnviarLembrete: computed(() => permissoes.value.podeEnviarLembrete ?? false),
        mesmaUnidade: computed(() => permissoes.value.mesmaUnidade ?? false),
        habilitarAcessoCadastro: computed(() => permissoes.value.habilitarAcessoCadastro ?? false),
        habilitarAcessoMapa: computed(() => permissoes.value.habilitarAcessoMapa ?? false),
        habilitarEditarCadastro: computed(() => permissoes.value.habilitarEditarCadastro ?? false),
        habilitarDisponibilizarCadastro: computed(() => permissoes.value.habilitarDisponibilizarCadastro ?? false),
        habilitarDevolverCadastro: computed(() => permissoes.value.habilitarDevolverCadastro ?? false),
        habilitarAceitarCadastro: computed(() => permissoes.value.habilitarAceitarCadastro ?? false),
        habilitarHomologarCadastro: computed(() => permissoes.value.habilitarHomologarCadastro ?? false),
        habilitarEditarMapa: computed(() => permissoes.value.habilitarEditarMapa ?? false),
        habilitarDisponibilizarMapa: computed(() => permissoes.value.habilitarDisponibilizarMapa ?? false),
        habilitarValidarMapa: computed(() => permissoes.value.habilitarValidarMapa ?? false),
        habilitarApresentarSugestoes: computed(() => permissoes.value.habilitarApresentarSugestoes ?? false),
        habilitarDevolverMapa: computed(() => permissoes.value.habilitarDevolverMapa ?? false),
        habilitarAceitarMapa: computed(() => permissoes.value.habilitarAceitarMapa ?? false),
        habilitarHomologarMapa: computed(() => permissoes.value.habilitarHomologarMapa ?? false),

        // Custom logic
        podeAnalisarCadastro,
        podeAnalisarMapa,
        podeVisualizarImpacto,
        mostrarAlterarDataLimite,
        mostrarReabrirCadastro,
        mostrarReabrirRevisao,
        mostrarEnviarLembrete,
        mostrarImportarAtividades,
        mostrarDisponibilizarCadastro,
        mostrarDevolverCadastro,
        mostrarApresentarSugestoes,
        mostrarValidarMapa,
        mostrarDisponibilizarMapa,
        mostrarDevolverMapa,
        acaoPrincipalCadastro,
        acaoPrincipalMapa
    };
}
