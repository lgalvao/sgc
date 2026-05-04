import {computed, type ComputedRef, ref, watch} from "vue";
import {SituacaoSubprocesso} from "@/types/tipos";
import logger from "@/utils/logger";

interface FluxoRevisaoCadastro {
    iniciarRevisaoCadastro(codigoSubprocesso: number): Promise<boolean>;

    cancelarInicioRevisaoCadastro(codigoSubprocesso: number): Promise<boolean>;
}

interface UseCadastroRevisaoSemMudancasParams {
    codigoSubprocesso: ComputedRef<number | null> | { value: number | null };
    isRevisao: ComputedRef<boolean>;
    situacaoAtual: ComputedRef<SituacaoSubprocesso | string | null | undefined>;
    houveAlteracaoCadastro: ComputedRef<boolean>;
    fluxoSubprocesso: FluxoRevisaoCadastro;
}

export function useCadastroRevisaoSemMudancas({
                                                  codigoSubprocesso,
                                                  isRevisao,
                                                  situacaoAtual,
                                                  houveAlteracaoCadastro,
                                                  fluxoSubprocesso,
                                              }: UseCadastroRevisaoSemMudancasParams) {
    const disponibilizacaoSemMudancas = ref(false);
    const loadingInicioRevisao = ref(false);

    const checkboxSemMudancasDesabilitado = computed(() =>
        loadingInicioRevisao.value || houveAlteracaoCadastro.value
    );

    const precisaIniciarRevisao = computed(() =>
        isRevisao.value && situacaoAtual.value === SituacaoSubprocesso.NAO_INICIADO
    );

    async function iniciarRevisaoSeNecessario() {
        if (!precisaIniciarRevisao.value || !codigoSubprocesso.value || loadingInicioRevisao.value) return;

        loadingInicioRevisao.value = true;
        try {
            const sucesso = await fluxoSubprocesso.iniciarRevisaoCadastro(codigoSubprocesso.value);
            if (!sucesso) {
                logger.error("Falha ao iniciar revisão do cadastro");
            }
        } finally {
            loadingInicioRevisao.value = false;
        }
    }

    async function cancelarInicioRevisaoSeNecessario() {
        if (situacaoAtual.value !== SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
            || !codigoSubprocesso.value
            || loadingInicioRevisao.value
            || houveAlteracaoCadastro.value) return;

        loadingInicioRevisao.value = true;
        try {
            const sucesso = await fluxoSubprocesso.cancelarInicioRevisaoCadastro(codigoSubprocesso.value);
            if (!sucesso) {
                logger.error("Falha ao cancelar início da revisão do cadastro");
            }
        } finally {
            loadingInicioRevisao.value = false;
        }
    }

    let ignorarAlteracaoCheckboxSemMudancas = false;

    function atualizarCheckboxSemMudancasSilenciosamente(valor: boolean) {
        ignorarAlteracaoCheckboxSemMudancas = true;
        disponibilizacaoSemMudancas.value = valor;
        queueMicrotask(() => {
            ignorarAlteracaoCheckboxSemMudancas = false;
        });
    }

    watch(disponibilizacaoSemMudancas, async (marcado) => {
        if (ignorarAlteracaoCheckboxSemMudancas) {
            return;
        }

        if (marcado) {
            if (!precisaIniciarRevisao.value) return;

            await iniciarRevisaoSeNecessario();
            if (situacaoAtual.value !== SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO) {
                atualizarCheckboxSemMudancasSilenciosamente(false);
            }
            return;
        }

        if (houveAlteracaoCadastro.value || situacaoAtual.value !== SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO) {
            return;
        }

        await cancelarInicioRevisaoSeNecessario();
        const situacaoAposCancelamento = situacaoAtual.value as SituacaoSubprocesso | string | null | undefined;
        if (situacaoAposCancelamento !== SituacaoSubprocesso.NAO_INICIADO) {
            atualizarCheckboxSemMudancasSilenciosamente(true);
        }
    });

    watch(houveAlteracaoCadastro, (alterou) => {
        if (alterou && disponibilizacaoSemMudancas.value) {
            atualizarCheckboxSemMudancasSilenciosamente(false);
        }
    });

    function sincronizarDisponibilizacaoSemMudancasInicial() {
        if (
            isRevisao.value
            && situacaoAtual.value === SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
            && !houveAlteracaoCadastro.value
        ) {
            atualizarCheckboxSemMudancasSilenciosamente(true);
        }
    }

    return {
        disponibilizacaoSemMudancas,
        checkboxSemMudancasDesabilitado,
        precisaIniciarRevisao,
        loadingInicioRevisao,
        iniciarRevisaoSeNecessario,
        cancelarInicioRevisaoSeNecessario,
        atualizarCheckboxSemMudancasSilenciosamente,
        sincronizarDisponibilizacaoSemMudancasInicial,
    };
}
