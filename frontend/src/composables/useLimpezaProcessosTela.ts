import {computed, ref} from 'vue';
import {TEXTOS} from '@/constants/textos';
import {useNotification} from '@/composables/useNotification';
import {useToast} from '@/composables/useToast';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';
import {normalizarErro} from '@/utils/apiError';
import {excluirProcessoCompleto} from '@/services/processo';
import {useAsyncAction} from '@/composables/useAsyncAction';

export function useLimpezaProcessosTela() {
    const {notificacao, notify, clear} = useNotification();
    const {exibirSucesso} = useToast();
    const {validarSubmissao, deveExibirErro, focarPrimeiroErroInvalido} = useValidacaoFormulario();
    const acaoExclusao = useAsyncAction();

    const codigoProcesso = ref('');
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

        await acaoExclusao.executar(
            () => excluirProcessoCompleto(codigoConfirmacao.value!),
            TEXTOS.administracao.LIMPEZA_ERRO_CODIGO,
            {
                relancarErro: false,
                aoSucesso: () => {
                    mostrarConfirmacao.value = false;
                    codigoProcesso.value = '';
                    clear();
                    exibirSucesso(TEXTOS.administracao.LIMPEZA_SUCESSO);
                },
                aoOcorrerErro: (_erro, causa) => {
                    notify(normalizarErro(causa).mensagem, 'danger');
                },
            },
        );
    }

    return {
        codigoProcesso,
        codigoConfirmacao,
        excluindo: acaoExclusao.carregando,
        mensagemErroCodigo,
        mostrarConfirmacao,
        notificacao,
        clear,
        abrirConfirmacao,
        confirmarExclusao,
    };
}
