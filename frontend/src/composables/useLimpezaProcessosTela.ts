import {computed, ref} from 'vue';
import {TEXTOS} from '@/constants/textos';
import {useNotification} from '@/composables/useNotification';
import {useToast} from '@/composables/useToast';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';
import {normalizarErro} from '@/utils/apiError';
import {buscarProcessosAtivos, buscarProcessosFinalizados, excluirProcessoCompleto} from '@/services/processo';
import {useAsyncAction} from '@/composables/useAsyncAction';
import type {ProcessoResumo} from '@/types/tipos';

export function useLimpezaProcessosTela() {
    const {notificacao, notify, clear} = useNotification();
    const {exibirSucesso} = useToast();
    const {validarSubmissao, resetarValidacao, deveExibirErro, focarPrimeiroErroInvalido} = useValidacaoFormulario();
    const acaoExclusao = useAsyncAction();

    const processos = ref<ProcessoResumo[]>([]);
    const carregandoProcessos = ref(false);
    const codigoProcessoSelecionado = ref<number | null>(null);
    const mostrarConfirmacao = ref(false);

    const processoSelecionado = computed<ProcessoResumo | null>(
        () => processos.value.find((processo) => processo.codigo === codigoProcessoSelecionado.value) ?? null,
    );

    const codigoConfirmacao = computed<number | undefined>(() => processoSelecionado.value?.codigo);
    const descricaoConfirmacao = computed<string | null>(() => processoSelecionado.value?.descricao ?? null);

    const mensagemErroProcesso = computed(() =>
        deveExibirErro(!codigoConfirmacao.value) ? TEXTOS.administracao.LIMPEZA_ERRO_PROCESSO : ''
    );

    async function carregarProcessos() {
        carregandoProcessos.value = true;
        try {
            const [ativos, finalizados] = await Promise.all([
                buscarProcessosAtivos(),
                buscarProcessosFinalizados(),
            ]);
            const processosUnicos = new Map<number, ProcessoResumo>();
            [...ativos, ...finalizados].forEach((processo) => {
                processosUnicos.set(processo.codigo, processo);
            });
            processos.value = Array.from(processosUnicos.values())
                .toSorted((a, b) => a.descricao.localeCompare(b.descricao, 'pt-BR', {sensitivity: 'base'}));
        } catch (causa) {
            notify(normalizarErro(causa).mensagem, 'danger');
        } finally {
            carregandoProcessos.value = false;
        }
    }

    async function abrirConfirmacao() {
        if (!validarSubmissao(!!codigoConfirmacao.value)) {
            await focarPrimeiroErroInvalido();
            return;
        }
        mostrarConfirmacao.value = true;
    }

    async function confirmarExclusao() {
        if (!codigoConfirmacao.value) return;

        const codigo = codigoConfirmacao.value;
        await acaoExclusao.executar(
            () => excluirProcessoCompleto(codigo),
            TEXTOS.administracao.LIMPEZA_ERRO_PROCESSO,
            {
                relancarErro: false,
                aoSucesso: () => {
                    mostrarConfirmacao.value = false;
                    processos.value = processos.value.filter((processo) => processo.codigo !== codigo);
                    codigoProcessoSelecionado.value = null;
                    resetarValidacao();
                    clear();
                    exibirSucesso(TEXTOS.administracao.LIMPEZA_SUCESSO);
                },
                aoOcorrerErro: (_erro, causa) => {
                    notify(normalizarErro(causa).mensagem, 'danger');
                },
            },
        );
    }

    void carregarProcessos();

    return {
        processos,
        carregandoProcessos,
        codigoProcessoSelecionado,
        processoSelecionado,
        codigoConfirmacao,
        descricaoConfirmacao,
        excluindo: acaoExclusao.carregando,
        mensagemErroProcesso,
        mostrarConfirmacao,
        notificacao,
        clear,
        carregarProcessos,
        abrirConfirmacao,
        confirmarExclusao,
    };
}
