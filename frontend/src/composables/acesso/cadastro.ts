import {computed, type ComputedRef} from 'vue';
import type {PermissoesSubprocesso} from '@/types/tipos';
import {TEXTOS} from '@/constants/textos';
import {TEXTOS_SUCESSO_SUBPROCESSO} from '@/constants/textos-subprocesso';
import type {AcaoPrincipalCadastro} from './tipos';

export function usarAcessoCadastro(
    permissoes: ComputedRef<PermissoesSubprocesso>,
    ehRevisao: ComputedRef<boolean>
) {
    const podeAnalisarCadastro = computed(() =>
        permissoes.value.podeDevolverCadastro ||
        permissoes.value.podeAceitarCadastro ||
        permissoes.value.podeHomologarCadastro
    );

    const mostrarImportarAtividades = computed(() => permissoes.value.podeEditarCadastro);
    const mostrarDisponibilizarCadastro = computed(() =>
        permissoes.value.podeDisponibilizarCadastro || permissoes.value.podeEditarCadastro
    );
    const mostrarDevolverCadastro = computed(() => permissoes.value.podeDevolverCadastro);

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
                mensagemSucesso: TEXTOS_SUCESSO_SUBPROCESSO.HOMOLOGACAO_EFETIVADA,
                redirecionarParaPainel: false,
            };
        }

        if (permissoes.value.podeAceitarCadastro) {
            return {
                codigo: 'ACEITAR',
                mostrar: true,
                habilitar: permissoes.value.podeAceitarCadastro && permissoes.value.habilitarAceitarCadastro,
                tituloModal: ehRevisao.value
                    ? TEXTOS.atividades.MODAL_ACEITE_REVISAO_TITULO
                    : TEXTOS.atividades.MODAL_VALIDAR_TITULO,
                textoModal: ehRevisao.value
                    ? TEXTOS.atividades.MODAL_ACEITE_REVISAO_TEXTO
                    : TEXTOS.atividades.MODAL_VALIDAR_TEXTO,
                rotuloBotao: TEXTOS.comum.BOTAO_REGISTRAR_ACEITE,
                rotuloConfirmacao: TEXTOS.comum.BOTAO_REGISTRAR_ACEITE,
                mensagemSucesso: TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO,
                redirecionarParaPainel: true,
            };
        }

        return null;
    });

    return {
        podeAnalisarCadastro,
        mostrarImportarAtividades,
        mostrarDisponibilizarCadastro,
        mostrarDevolverCadastro,
        acaoPrincipalCadastro
    };
}
