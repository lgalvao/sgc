import {computed, type Ref, unref} from 'vue';
import {type SubprocessoDetalhe, TipoProcesso} from '@/types/tipos';
import {TEXTOS} from '@/constants/textos';

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

    const podeEditarCadastro = computed(() => getPermissoes()?.podeEditarCadastro ?? false);
    const podeDisponibilizarCadastro = computed(() => getPermissoes()?.podeDisponibilizarCadastro ?? false);
    const podeDevolverCadastro = computed(() => getPermissoes()?.podeDevolverCadastro ?? false);
    const podeAceitarCadastro = computed(() => getPermissoes()?.podeAceitarCadastro ?? false);
    const podeHomologarCadastro = computed(() => getPermissoes()?.podeHomologarCadastro ?? false);

    const podeAnalisarCadastro = computed(() => podeDevolverCadastro.value || podeAceitarCadastro.value || podeHomologarCadastro.value);

    const podeEditarMapa = computed(() => getPermissoes()?.podeEditarMapa ?? false);
    const podeDisponibilizarMapa = computed(() => getPermissoes()?.podeDisponibilizarMapa ?? false);
    const podeValidarMapa = computed(() => getPermissoes()?.podeValidarMapa ?? false);
    const podeApresentarSugestoes = computed(() => getPermissoes()?.podeApresentarSugestoes ?? false);
    const podeVerSugestoes = computed(() => getPermissoes()?.podeVerSugestoes ?? false);
    const podeDevolverMapa = computed(() => getPermissoes()?.podeDevolverMapa ?? false);
    const podeAceitarMapa = computed(() => getPermissoes()?.podeAceitarMapa ?? false);
    const podeHomologarMapa = computed(() => getPermissoes()?.podeHomologarMapa ?? false);

    const podeAnalisarMapa = computed(() => podeDevolverMapa.value || podeAceitarMapa.value || podeHomologarMapa.value);

    const podeVisualizarImpacto = computed(() => getPermissoes()?.podeVisualizarImpacto ?? false);

    const podeAlterarDataLimite = computed(() => getPermissoes()?.podeAlterarDataLimite ?? false);
    const podeReabrirCadastro = computed(() => getPermissoes()?.podeReabrirCadastro ?? false);
    const podeReabrirRevisao = computed(() => getPermissoes()?.podeReabrirRevisao ?? false);
    const podeEnviarLembrete = computed(() => getPermissoes()?.podeEnviarLembrete ?? false);

    const mesmaUnidade = computed(() => getPermissoes()?.mesmaUnidade ?? false);
    const habilitarAcessoCadastro = computed(() => getPermissoes()?.habilitarAcessoCadastro ?? false);
    const habilitarAcessoMapa = computed(() => getPermissoes()?.habilitarAcessoMapa ?? false);

    const habilitarEditarCadastro = computed(() => getPermissoes()?.habilitarEditarCadastro ?? false);
    const habilitarDisponibilizarCadastro = computed(() => getPermissoes()?.habilitarDisponibilizarCadastro ?? false);
    const habilitarDevolverCadastro = computed(() => getPermissoes()?.habilitarDevolverCadastro ?? false);
    const habilitarAceitarCadastro = computed(() => getPermissoes()?.habilitarAceitarCadastro ?? false);
    const habilitarHomologarCadastro = computed(() => getPermissoes()?.habilitarHomologarCadastro ?? false);

    const habilitarEditarMapa = computed(() => getPermissoes()?.habilitarEditarMapa ?? false);
    const habilitarDisponibilizarMapa = computed(() => getPermissoes()?.habilitarDisponibilizarMapa ?? false);
    const habilitarValidarMapa = computed(() => getPermissoes()?.habilitarValidarMapa ?? false);
    const habilitarApresentarSugestoes = computed(() => getPermissoes()?.habilitarApresentarSugestoes ?? false);
    const habilitarDevolverMapa = computed(() => getPermissoes()?.habilitarDevolverMapa ?? false);
    const habilitarAceitarMapa = computed(() => getPermissoes()?.habilitarAceitarMapa ?? false);
    const habilitarHomologarMapa = computed(() => getPermissoes()?.habilitarHomologarMapa ?? false);

    const acaoPrincipalCadastro = computed<AcaoPrincipalCadastro | null>(() => {
        if (podeHomologarCadastro.value) {
            return {
                codigo: 'HOMOLOGAR',
                mostrar: true,
                habilitar: habilitarHomologarCadastro.value,
                tituloModal: TEXTOS.atividades.MODAL_HOMOLOGAR_TITULO,
                textoModal: TEXTOS.atividades.MODAL_HOMOLOGAR_TEXTO,
                rotuloBotao: TEXTOS.atividades.BOTAO_HOMOLOGAR,
                rotuloConfirmacao: TEXTOS.comum.BOTAO_HOMOLOGAR,
                mensagemSucesso: TEXTOS.sucesso.HOMOLOGACAO_EFETIVADA,
                redirecionarParaPainel: false,
            };
        }

        if (podeAceitarCadastro.value) {
            return {
                codigo: 'ACEITAR',
                mostrar: true,
                habilitar: habilitarAceitarCadastro.value,
                tituloModal: isRevisao.value
                    ? TEXTOS.atividades.MODAL_ACEITE_REVISAO_TITULO
                    : TEXTOS.atividades.MODAL_VALIDAR_TITULO,
                textoModal: isRevisao.value
                    ? TEXTOS.atividades.MODAL_ACEITE_REVISAO_TEXTO
                    : TEXTOS.atividades.MODAL_VALIDAR_TEXTO,
                rotuloBotao: TEXTOS.atividades.BOTAO_ACEITAR,
                rotuloConfirmacao: isRevisao.value
                    ? TEXTOS.comum.BOTAO_ACEITAR
                    : TEXTOS.comum.BOTAO_VALIDAR,
                mensagemSucesso: TEXTOS.sucesso.ACEITE_REGISTRADO,
                redirecionarParaPainel: true,
            };
        }

        return null;
    });

    const acaoPrincipalMapa = computed<AcaoPrincipalMapa | null>(() => {
        if (podeHomologarMapa.value) {
            return {
                codigo: 'HOMOLOGAR',
                mostrar: true,
                habilitar: habilitarHomologarMapa.value,
                rotuloBotao: TEXTOS.mapa.LABEL_HOMOLOGAR,
                mensagemSucesso: TEXTOS.mapa.SUCESSO_HOMOLOGACAO,
            };
        }

        if (podeAceitarMapa.value) {
            return {
                codigo: 'ACEITAR',
                mostrar: true,
                habilitar: habilitarAceitarMapa.value,
                rotuloBotao: TEXTOS.mapa.LABEL_REGISTRAR_ACEITE,
                mensagemSucesso: TEXTOS.sucesso.ACEITE_REGISTRADO,
            };
        }

        return null;
    });

    return {
        mesmaUnidade,
        podeEditarCadastro,
        podeDisponibilizarCadastro,
        podeDevolverCadastro,
        podeAceitarCadastro,
        podeHomologarCadastro,
        podeAnalisarCadastro,
        podeEditarMapa,
        podeDisponibilizarMapa,
        podeValidarMapa,
        podeApresentarSugestoes,
        podeVerSugestoes,
        podeDevolverMapa,
        podeAceitarMapa,
        podeHomologarMapa,
        podeAnalisarMapa,
        podeVisualizarImpacto,
        podeAlterarDataLimite,
        podeReabrirCadastro,
        podeReabrirRevisao,
        podeEnviarLembrete,
        habilitarAcessoCadastro,
        habilitarAcessoMapa,
        habilitarEditarCadastro,
        habilitarDisponibilizarCadastro,
        habilitarDevolverCadastro,
        habilitarAceitarCadastro,
        habilitarHomologarCadastro,
        habilitarEditarMapa,
        habilitarDisponibilizarMapa,
        habilitarValidarMapa,
        habilitarApresentarSugestoes,
        habilitarDevolverMapa,
        habilitarAceitarMapa,
        habilitarHomologarMapa,
        acaoPrincipalCadastro,
        acaoPrincipalMapa
    };
}
