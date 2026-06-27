import {computed, ref} from 'vue';
import {TEXTOS} from '@/constants/textos';
import {useNotification} from '@/composables/useNotification';
import {useToast} from '@/composables/useToast';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';
import {normalizarErro} from '@/utils/apiError';
import {excluirProcessoCompleto} from '@/services/processo';

export function useLimpezaProcessosTela() {
    const {notificacao, notify, clear} = useNotification();
    const {exibirSucesso} = useToast();
    const {validarSubmissao, deveExibirErro, focarPrimeiroErroInvalido} = useValidacaoFormulario();

    const codigoProcesso = ref('');
    const excluindo = ref(false);
    const mostrarConfirmacao = ref(false);

    const codigoConfirmacao = computed<number | undefined>(() => {
        const codigo = Number(codigoProcesso.value);
        return Number.isInteger(codigo) && codigo > 0 ? codigo : undefined;
    });

    const mensagemErroCodigo = computed(() =>
        deveExibirErro(!codigoConfirmacao.value) ? TEXTOS.administracao.LIMPEZA_ERRO_CODIGO : ''
    );

    async function abrirConfirmacao() {
        if (!validarSubmissao(!!codigoConfirmacao.value)) {
            await focarPrimeiroErroInvalido();
            return;
        }
        mostrarConfirmacao.value = true;
    }

    async function confirmarExclusao() {
        if (!codigoConfirmacao.value) return;

        excluindo.value = true;
        try {
            await excluirProcessoCompleto(codigoConfirmacao.value);
            mostrarConfirmacao.value = false;
            codigoProcesso.value = '';
            clear();
            exibirSucesso(TEXTOS.administracao.LIMPEZA_SUCESSO);
        } catch (error) {
            notify(normalizarErro(error).mensagem, 'danger');
        } finally {
            excluindo.value = false;
        }
    }

    return {
        codigoProcesso,
        codigoConfirmacao,
        excluindo,
        mensagemErroCodigo,
        mostrarConfirmacao,
        notificacao,
        clear,
        abrirConfirmacao,
        confirmarExclusao,
    };
}
