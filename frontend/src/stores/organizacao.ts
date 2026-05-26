import {defineStore} from "pinia";
import {ref} from "vue";
import type {DiagnosticoOrganizacional} from "@/types/tipos";
import {buscarDiagnosticoOrganizacional} from "@/services/unidadeService";
import {logger} from "@/utils";

function limparCarregamento(
    carregando: {value: boolean},
    carregado: {value: boolean},
    definirCarregamentoEmAndamento: (carregamento: Promise<void> | null) => void,
) {
    carregando.value = false;
    carregado.value = true;
    definirCarregamentoEmAndamento(null);
}

function registrarErroDiagnostico(
    diagnostico: {value: DiagnosticoOrganizacional | null},
    erroDiagnostico: {value: string | null},
    erro: unknown,
) {
    diagnostico.value = null;
    erroDiagnostico.value = "Não foi possível verificar as pendências organizacionais.";
    logger.error("Erro ao carregar diagnóstico organizacional:", erro);
}

/**
 * Cache de sessão para o diagnóstico organizacional.
 * O diagnóstico é configuração administrativa — não muda durante a sessão ativa.
 * Uma única chamada ao backend é suficiente por sessão de login.
 */
export const useOrganizacaoStore = defineStore("organizacao", () => {
    const diagnostico = ref<DiagnosticoOrganizacional | null>(null);
    const erroDiagnostico = ref<string | null>(null);
    const carregado = ref(false);
    const carregando = ref(false);
    let carregamentoEmAndamento: Promise<void> | null = null;

    const dadosValidos = (): boolean => carregado.value;

    /**
     * Carrega o diagnóstico organizacional somente se ainda estiver válido nesta sessão.
     * @param deveExibir - resultado de `usePerfil().mostrarDiagnosticoOrganizacional`; se falso, não busca.
     */
    async function garantirDiagnostico(deveExibir: boolean): Promise<void> {
        if (!deveExibir || dadosValidos()) {
            return;
        }

        if (!carregamentoEmAndamento) {
            carregamentoEmAndamento = carregarDiagnostico();
        }

        await carregamentoEmAndamento;
    }

    async function carregarDiagnostico(): Promise<void> {
        carregando.value = true;
        try {
            diagnostico.value = await buscarDiagnosticoOrganizacional();
            erroDiagnostico.value = null;
        } catch (erro) {
            registrarErroDiagnostico(diagnostico, erroDiagnostico, erro);
        } finally {
            limparCarregamento(carregando, carregado, (carregamento) => {
                carregamentoEmAndamento = carregamento;
            });
        }
    }

    async function recarregarDiagnostico(deveExibir: boolean): Promise<void> {
        invalidar();
        await garantirDiagnostico(deveExibir);
    }

    function invalidar() {
        carregado.value = false;
        carregamentoEmAndamento = null;
    }

    function resetar() {
        diagnostico.value = null;
        erroDiagnostico.value = null;
        carregado.value = false;
        carregando.value = false;
        carregamentoEmAndamento = null;
    }

    return {diagnostico, erroDiagnostico, carregado, carregando, dadosValidos, garantirDiagnostico, recarregarDiagnostico, invalidar, resetar};
});
