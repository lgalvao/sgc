import {computed, type Ref, unref} from 'vue';
import {type PermissoesSubprocesso, type SubprocessoDetalhe, TipoProcesso} from '@/types/tipos';
import {TEXTOS} from '@/constants/textos';
import {criarAcessosPermissao} from '@/composables/acessoPermissoes';
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
    const permissoes = computed<PermissoesSubprocesso>(() => getPermissoes() ?? PERMISSOES_SUBPROCESSO_VAZIAS);
    const acessosPermissao = criarAcessosPermissao(permissoes);

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

    const mostrarAlterarDataLimite = computed(() => permissoes.value.podeAlterarDataLimite);
    const mostrarReabrirCadastro = computed(() => permissoes.value.podeReabrirCadastro);
    const mostrarReabrirRevisao = computed(() => permissoes.value.podeReabrirRevisao);
    const mostrarEnviarLembrete = computed(() => permissoes.value.podeEnviarLembrete);

    const mostrarImportarAtividades = computed(() => permissoes.value.podeEditarCadastro);
    const mostrarDisponibilizarCadastro = computed(() => permissoes.value.podeDisponibilizarCadastro);
    const mostrarDevolverCadastro = computed(() => permissoes.value.podeDevolverCadastro);

    const mostrarApresentarSugestoes = computed(() => permissoes.value.podeApresentarSugestoes);
    const mostrarValidarMapa = computed(() => permissoes.value.podeValidarMapa);
    const mostrarDisponibilizarMapa = computed(() => permissoes.value.podeDisponibilizarMapa);
    const mostrarDevolverMapa = computed(() => permissoes.value.podeDevolverMapa);

    const acaoPrincipalCadastro = computed<AcaoPrincipalCadastro | null>(() => {
        if (permissoes.value.podeHomologarCadastro) {
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

        if (permissoes.value.podeAceitarCadastro) {
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
        if (permissoes.value.podeHomologarMapa) {
            return {
                codigo: 'HOMOLOGAR',
                mostrar: true,
                habilitar: permissoes.value.podeHomologarMapa && permissoes.value.habilitarHomologarMapa,
                rotuloBotao: TEXTOS.mapa.LABEL_HOMOLOGAR,
                mensagemSucesso: TEXTOS.mapa.SUCESSO_HOMOLOGACAO,
            };
        }

        if (permissoes.value.podeAceitarMapa) {
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
        ...acessosPermissao,
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
