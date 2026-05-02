import {defineStore} from "pinia";
import {ref} from "vue";
import type {DiagnosticoOrganizacional} from "@/types/tipos";
import {buscarDiagnosticoOrganizacional} from "@/services/unidadeService";
import {logger} from "@/utils";

/**
 * Cache de sessão para o diagnóstico organizacional.
 * O diagnóstico é configuração administrativa — não muda durante a sessão ativa.
 * Uma única chamada ao backend é suficiente por sessão de login.
 */
export const useOrganizacaoStore = defineStore("organizacao", () => {
    const diagnostico = ref<DiagnosticoOrganizacional | null>(null);
    const erroDiagnostico = ref<string | null>(null);
    const carregado = ref(false);
    let carregamentoEmAndamento: Promise<void> | null = null;

    function dadosValidos(): boolean {
        return carregado.value;
    }

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
        try {
            diagnostico.value = await buscarDiagnosticoOrganizacional();
            erroDiagnostico.value = null;
        } catch (err) {
            diagnostico.value = null;
            erroDiagnostico.value = "Não foi possível verificar as pendências organizacionais.";
            logger.error("Erro ao carregar diagnóstico organizacional:", err);
        } finally {
            carregado.value = true;
            carregamentoEmAndamento = null;
        }
    }

    function invalidar() {
        diagnostico.value = null;
        erroDiagnostico.value = null;
        carregado.value = false;
        carregamentoEmAndamento = null;
    }

    function $reset() {
        invalidar();
    }

    return {diagnostico, erroDiagnostico, carregado, dadosValidos, garantirDiagnostico, invalidar, $reset};
});
