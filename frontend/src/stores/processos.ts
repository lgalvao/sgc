import {defineStore} from "pinia";
import {computed, ref} from "vue";
import type {Page} from "@/services/painelService";
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel,
    TipoProcesso,
} from "@/types/tipos";
import * as painelService from "../services/painelService";
import * as processoService from "../services/processoService";

export const useProcessosStore = defineStore("processos", () => {
    const processosPainel = ref<ProcessoResumo[]>([]);
    const processosPainelPage = ref<Page<ProcessoResumo>>({} as Page<ProcessoResumo>);
    const processoDetalhe = ref<Processo | null>(null);
    const subprocessosElegiveis = ref<SubprocessoElegivel[]>([]);
    const processosFinalizados = ref<ProcessoResumo[]>([]);

    const getUnidadesProcesso = computed(
        () =>
            (idProcesso: number): ProcessoResumo[] => {
                if (processoDetalhe.value && processoDetalhe.value.codigo === idProcesso) {
                    return processoDetalhe.value.resumoSubprocessos;
                }
                return [];
            },
    );

    async function fetchProcessosPainel(
        perfil: string,
        unidade: number,
        page: number,
        size: number,
        sort?: keyof ProcessoResumo,
        order?: "asc" | "desc",
    ) {
        const response = await painelService.listarProcessos(
            perfil,
            unidade,
            page,
            size,
            sort,
            order,
        );
        processosPainel.value = response.content;
        processosPainelPage.value = response;
    }

    async function fetchProcessosFinalizados() {
        processosFinalizados.value = await processoService.fetchProcessosFinalizados();
    }

    async function fetchProcessoDetalhe(idProcesso: number) {
        processoDetalhe.value = await processoService.obterDetalhesProcesso(idProcesso);
    }

    async function fetchSubprocessosElegiveis(idProcesso: number) {
        subprocessosElegiveis.value =
            await processoService.fetchSubprocessosElegiveis(idProcesso);
    }

    async function criarProcesso(payload: CriarProcessoRequest) {
        return await processoService.criarProcesso(payload);
    }

    async function atualizarProcesso(idProcesso: number, payload: AtualizarProcessoRequest) {
        await processoService.atualizarProcesso(idProcesso, payload);
    }

    async function removerProcesso(idProcesso: number) {
        await processoService.excluirProcesso(idProcesso);
    }

    async function iniciarProcesso(idProcesso: number, tipo: TipoProcesso, unidadesIds: number[]) {
        await processoService.iniciarProcesso(idProcesso, tipo, unidadesIds);
        await fetchProcessoDetalhe(idProcesso);
    }

    async function finalizarProcesso(idProcesso: number) {
        await processoService.finalizarProcesso(idProcesso);
        await fetchProcessoDetalhe(idProcesso);
    }

    async function processarCadastroBloco(payload: {
        codProcesso: number;
        unidades: string[];
        tipoAcao: "aceitar" | "homologar";
        unidadeUsuario: string;
    }) {
        await processoService.processarAcaoEmBloco(payload);
        // Após a ação em bloco, recarregar os detalhes do processo para refletir as mudanças
        await fetchProcessoDetalhe(payload.codProcesso);
    }

    async function alterarDataLimiteSubprocesso(id: number, dados: { novaData: string }) {
        await processoService.alterarDataLimiteSubprocesso(id, dados);
        if (processoDetalhe.value) {
            await fetchProcessoDetalhe(processoDetalhe.value.codigo);
        }
    }

    async function apresentarSugestoes(id: number, dados: { sugestoes: string }) {
        await processoService.apresentarSugestoes(id, dados);
        if (processoDetalhe.value) {
            await fetchProcessoDetalhe(processoDetalhe.value.codigo);
        }
    }

    async function validarMapa(id: number) {
        await processoService.validarMapa(id);
        if (processoDetalhe.value) await fetchProcessoDetalhe(processoDetalhe.value.codigo);
    }

    return {
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        subprocessosElegiveis,
        processosFinalizados,
        getUnidadesDoProcesso: getUnidadesProcesso,
        fetchProcessosPainel,
        fetchProcessosFinalizados,
        fetchProcessoDetalhe,
        fetchSubprocessosElegiveis,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,
        iniciarProcesso,
        finalizarProcesso,
        processarCadastroBloco,
        alterarDataLimiteSubprocesso,
        apresentarSugestoes,
        validarMapa,
    };
});
