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
        isRevisao.value && permissoes.value.podeVisualizarImpacto
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
        podeEditarCadastro: computed(() => permissoes.value.podeEditarCadastro),
        podeDisponibilizarCadastro: computed(() => permissoes.value.podeDisponibilizarCadastro),
        podeDevolverCadastro: computed(() => permissoes.value.podeDevolverCadastro),
        podeAceitarCadastro: computed(() => permissoes.value.podeAceitarCadastro),
        podeHomologarCadastro: computed(() => permissoes.value.podeHomologarCadastro),
        podeEditarMapa: computed(() => permissoes.value.podeEditarMapa),
        podeDisponibilizarMapa: computed(() => permissoes.value.podeDisponibilizarMapa),
        podeValidarMapa: computed(() => permissoes.value.podeValidarMapa),
        podeApresentarSugestoes: computed(() => permissoes.value.podeApresentarSugestoes),
        podeVerSugestoes: computed(() => permissoes.value.podeVerSugestoes),
        podeDevolverMapa: computed(() => permissoes.value.podeDevolverMapa),
        podeAceitarMapa: computed(() => permissoes.value.podeAceitarMapa),
        podeHomologarMapa: computed(() => permissoes.value.podeHomologarMapa),
        podeAlterarDataLimite: computed(() => permissoes.value.podeAlterarDataLimite),
        podeReabrirCadastro: computed(() => permissoes.value.podeReabrirCadastro),
        podeReabrirRevisao: computed(() => permissoes.value.podeReabrirRevisao),
        podeEnviarLembrete: computed(() => permissoes.value.podeEnviarLembrete),
        mesmaUnidade: computed(() => permissoes.value.mesmaUnidade),
        habilitarAcessoCadastro: computed(() => permissoes.value.habilitarAcessoCadastro),
        habilitarAcessoMapa: computed(() => permissoes.value.habilitarAcessoMapa),
        habilitarEditarCadastro: computed(() => permissoes.value.habilitarEditarCadastro),
        habilitarDisponibilizarCadastro: computed(() => permissoes.value.habilitarDisponibilizarCadastro),
        habilitarDevolverCadastro: computed(() => permissoes.value.habilitarDevolverCadastro),
        habilitarAceitarCadastro: computed(() => permissoes.value.habilitarAceitarCadastro),
        habilitarHomologarCadastro: computed(() => permissoes.value.habilitarHomologarCadastro),
        habilitarEditarMapa: computed(() => permissoes.value.habilitarEditarMapa),
        habilitarDisponibilizarMapa: computed(() => permissoes.value.habilitarDisponibilizarMapa),
        habilitarValidarMapa: computed(() => permissoes.value.habilitarValidarMapa),
        habilitarApresentarSugestoes: computed(() => permissoes.value.habilitarApresentarSugestoes),
        habilitarDevolverMapa: computed(() => permissoes.value.habilitarDevolverMapa),
        habilitarAceitarMapa: computed(() => permissoes.value.habilitarAceitarMapa),
        habilitarHomologarMapa: computed(() => permissoes.value.habilitarHomologarMapa),

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
