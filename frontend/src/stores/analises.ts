import {listarAnalisesCadastro, listarAnalisesValidacao} from "@/services/subprocessoService";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";
import {defineStore} from "pinia";
import {ref} from "vue";
import {useAsyncAction} from "@/composables/useAsyncAction";

export const useAnalisesStore = defineStore("analises", () => {
    const analisesCadastro = ref<AnaliseCadastro[]>([]);
    const analisesValidacao = ref<AnaliseValidacao[]>([]);
    const {carregando, erro, executarSilencioso} = useAsyncAction();

    async function buscarAnalisesCadastro(codSubprocesso: number) {
        analisesCadastro.value = [];
        await executarSilencioso(async () => {
            if (codSubprocesso) {
                analisesCadastro.value = await listarAnalisesCadastro(codSubprocesso);
            }
        }, "Erro ao carregar histórico de análises de cadastro.");
    }

    async function buscarAnalisesValidacao(codSubprocesso: number) {
        analisesValidacao.value = [];
        await executarSilencioso(async () => {
            if (codSubprocesso) {
                analisesValidacao.value = await listarAnalisesValidacao(codSubprocesso);
            }
        }, "Erro ao carregar histórico de análises de validação.");
    }

    async function carregarHistorico(codSubprocesso: number) {
        await buscarAnalisesCadastro(codSubprocesso);
        await buscarAnalisesValidacao(codSubprocesso);
    }

    function obterAnalisesPorSubprocesso(): (AnaliseCadastro | AnaliseValidacao)[] {
        return [...analisesCadastro.value, ...analisesValidacao.value];
    }

    return {
        analisesCadastro,
        analisesValidacao,
        carregando,
        erro,
        buscarAnalisesCadastro,
        buscarAnalisesValidacao,
        carregarHistorico,
        obterAnalisesPorSubprocesso
    };
});
