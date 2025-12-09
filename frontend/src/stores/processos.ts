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

    const obterUnidadesProcesso = computed(
        () =>
            (idProcesso: number): ProcessoResumo[] => {
                if (processoDetalhe.value && processoDetalhe.value.codigo === idProcesso) {
                    return processoDetalhe.value.resumoSubprocessos;
                }
                return [];
            },
    );

    async function buscarProcessosPainel(
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

    async function buscarProcessosFinalizados() {
        processosFinalizados.value = await processoService.buscarProcessosFinalizados();
    }

    async function buscarProcessoDetalhe(idProcesso: number) {
        processoDetalhe.value = await processoService.obterDetalhesProcesso(idProcesso);
    }

    async function buscarSubprocessosElegiveis(idProcesso: number) {
        subprocessosElegiveis.value =
            await processoService.buscarSubprocessosElegiveis(idProcesso);
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
    }

    async function finalizarProcesso(idProcesso: number) {
        await processoService.finalizarProcesso(idProcesso);
        await buscarProcessoDetalhe(idProcesso);
    }

    async function processarCadastroBloco(payload: {
        codProcesso: number;
        unidades: string[];
        tipoAcao: "aceitar" | "homologar";
        unidadeUsuario: string;
    }) {
        await processoService.processarAcaoEmBloco(payload);
        // Após a ação em bloco, recarregar os detalhes do processo para refletir as mudanças
        await buscarProcessoDetalhe(payload.codProcesso);
    }

    async function alterarDataLimiteSubprocesso(id: number, dados: { novaData: string }) {
        await processoService.alterarDataLimiteSubprocesso(id, dados);
        if (processoDetalhe.value) {
            await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        }
    }

    async function apresentarSugestoes(id: number, dados: { sugestoes: string }) {
        await processoService.apresentarSugestoes(id, dados);
        if (processoDetalhe.value) {
            await buscarProcessoDetalhe(processoDetalhe.value.codigo);
        }
    }

    async function validarMapa(id: number) {
        await processoService.validarMapa(id);
        if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
    }

    async function homologarValidacao(id: number) {
        await processoService.homologarValidacao(id);
        if (processoDetalhe.value) await buscarProcessoDetalhe(processoDetalhe.value.codigo);
    }

    function atualizarStatusSubprocesso(codSubprocesso: number, dados: { situacao: any, situacaoLabel: string }) {
        if (processoDetalhe.value) {
            const unidade = processoDetalhe.value.unidades.find(u => u.codSubprocesso === codSubprocesso);
            if (unidade) {
                unidade.situacaoSubprocesso = dados.situacao;
                unidade.situacaoLabel = dados.situacaoLabel;
            }
        }
    }

    return {
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        subprocessosElegiveis,
        processosFinalizados,
        obterUnidadesDoProcesso: obterUnidadesProcesso,
        buscarProcessosPainel,
        buscarProcessosFinalizados,
        buscarProcessoDetalhe,
        buscarSubprocessosElegiveis,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,
        iniciarProcesso,
        finalizarProcesso,
        processarCadastroBloco,
        alterarDataLimiteSubprocesso,
        apresentarSugestoes,
        validarMapa,
        homologarValidacao,
        atualizarStatusSubprocesso,
    };
});
