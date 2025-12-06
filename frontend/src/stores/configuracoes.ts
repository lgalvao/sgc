import {defineStore} from "pinia";
import {ref} from "vue";

export const useConfiguracoesStore = defineStore("configuracoes", () => {
    const diasInativacaoProcesso = ref(10);
    const diasAlertaNovo = ref(7);

    function carregarConfiguracoes() {
        try {
            const savedConfig = localStorage.getItem("appConfiguracoes");
            if (savedConfig) {
                const parsedConfig = JSON.parse(savedConfig);
                diasInativacaoProcesso.value = parsedConfig.diasInativacaoProcesso || diasInativacaoProcesso.value;
                diasAlertaNovo.value = parsedConfig.diasAlertaNovo || diasAlertaNovo.value;
            }
        } catch (e) {
            console.error("Erro ao carregar configurações do localStorage:", e);
        }
    }

    function salvarConfiguracoes() {
        try {
            const configToSave = {
                diasInativacaoProcesso: diasInativacaoProcesso.value,
                diasAlertaNovo: diasAlertaNovo.value,
            };
            localStorage.setItem("appConfiguracoes", JSON.stringify(configToSave));
            return true;
        } catch (e) {
            console.error("Erro ao salvar configurações no localStorage:", e);
            return false;
        }
    }

    function definirDiasInativacaoProcesso(dias: number) {
        if (dias >= 1) diasInativacaoProcesso.value = dias;
    }

    function definirDiasAlertaNovo(dias: number) {
        if (dias >= 1) diasAlertaNovo.value = dias;
    }

    return {
        diasInativacaoProcesso,
        diasAlertaNovo,
        carregarConfiguracoes,
        salvarConfiguracoes,
        definirDiasInativacaoProcesso,
        definirDiasAlertaNovo,
    };
});
