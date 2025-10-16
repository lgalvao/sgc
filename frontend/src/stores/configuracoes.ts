import { defineStore } from 'pinia';
import { useApi } from '@/composables/useApi';

interface ConfiguracoesState {
    diasInativacaoProcesso: number;
    diasAlertaNovo: number;
    loading: boolean;
    error: string | null;
}

export const useConfiguracoesStore = defineStore('configuracoes', {
    state: (): ConfiguracoesState => ({
        diasInativacaoProcesso: 10, // Valor padrão
        diasAlertaNovo: 7,       // Valor padrão
        loading: false,
        error: null,
    }),
    actions: {
        async fetchConfiguracoes() {
            this.loading = true;
            this.error = null;
            const api = useApi();
            try {
                const { data } = await api.get('/api/configuracoes');
                this.diasInativacaoProcesso = data.diasInativacaoProcesso;
                this.diasAlertaNovo = data.diasAlertaNovo;
            } catch (error) {
                this.error = 'Falha ao carregar configurações.';
            } finally {
                this.loading = false;
            }
        },
        async saveConfiguracoes() {
            this.loading = true;
            this.error = null;
            const api = useApi();
            try {
                const configToSave = {
                    diasInativacaoProcesso: this.diasInativacaoProcesso,
                    diasAlertaNovo: this.diasAlertaNovo,
                };
                await api.put('/api/configuracoes', configToSave);
            } catch (error) {
                this.error = 'Falha ao salvar configurações.';
                throw new Error('Falha ao salvar configurações.');
            } finally {
                this.loading = false;
            }
        },
        setDiasInativacaoProcesso(dias: number) {
            if (dias >= 1) this.diasInativacaoProcesso = dias;
        },
        setDiasAlertaNovo(dias: number) {
            if (dias >= 1) this.diasAlertaNovo = dias;
        },
    },
});