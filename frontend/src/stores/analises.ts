import {listarAnalisesCadastro, listarAnalisesValidacao} from "@/services/subprocessoService";
import type {AnaliseCadastro, AnaliseValidacao} from "@/types/tipos";
import {defineStore} from "pinia";
import {ref} from "vue";

export const useAnalisesStore = defineStore("analises", () => {
    const analisesCadastro = ref<AnaliseCadastro[]>([]);
    const analisesValidacao = ref<AnaliseValidacao[]>([]);
    const carregando = ref(false);
    const erro = ref<string | null>(null);

    async function buscarAnalisesCadastro(codSubprocesso: number) {
        carregando.value = true;
        erro.value = null;
        analisesCadastro.value = [];

        try {
            if (codSubprocesso) {
                const analises = await listarAnalisesCadastro(codSubprocesso);
                analisesCadastro.value = analises;
            }
        } catch (e: any) {
            erro.value = e.message || "Erro ao carregar histórico de análises de cadastro.";
        } finally {
            carregando.value = false;
        }
    }

    async function buscarAnalisesValidacao(codSubprocesso: number) {
        carregando.value = true;
        erro.value = null;
        analisesValidacao.value = [];
        try {
            if (codSubprocesso) {
                const validacoes = await listarAnalisesValidacao(codSubprocesso);
                analisesValidacao.value = validacoes;
            }
        } catch (e: any) {
            erro.value = e.message || "Erro ao carregar histórico de análises de validação.";
        } finally {
            carregando.value = false;
        }
    }

    async function carregarHistorico(codSubprocesso: number) {
        await buscarAnalisesCadastro(codSubprocesso);
        await buscarAnalisesValidacao(codSubprocesso);
    }

    function obterAnalisesPorSubprocesso(): (AnaliseCadastro | AnaliseValidacao)[] {
        // Simplificação: Retorna a concatenação das listas carregadas.
        // Assume-se que o contexto do subprocesso é mantido pelo componente que chama buscarAnalisesCadastro/Validacao.
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
