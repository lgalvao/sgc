import { defineStore } from 'pinia';
import { useApi } from '@/composables/useApi';
import type { AlertaUsuario } from '@/types/tipos';

export const useAlertasStore = defineStore('alertas', {
    state: () => ({
        items: [] as AlertaUsuario[],
        loading: false,
        error: null as string | null,
    }),
    getters: {
        alertasNaoLidos(state): AlertaUsuario[] {
            return state.items.filter(alerta => !alerta.lido);
        },
    },
    actions: {
        async fetchAlertas() {
            this.loading = true;
            this.error = null;
            const api = useApi();
            try {
                const { data } = await api.get('/api/alertas');
                // A API deve retornar as datas como strings ISO 8601, então convertemos para objetos Date.
                this.items = (data as any[]).map(item => ({
                    ...item,
                    dataHora: new Date(item.dataHora),
                    dataLeitura: item.dataLeitura ? new Date(item.dataLeitura) : null,
                }));
            } catch (error: any) {
                this.error = 'Falha ao buscar alertas.';
            } finally {
                this.loading = false;
            }
        },

        async marcarAlertaComoLido(idAlerta: number) {
            const api = useApi();
            try {
                // Endpoint para marcar um alerta específico como lido
                await api.post(`/api/alertas/${idAlerta}/lido`, {});
                // Atualiza o estado localmente ou busca novamente
                const alerta = this.items.find(a => a.id === idAlerta);
                if (alerta) {
                    alerta.lido = true;
                    alerta.dataLeitura = new Date();
                }
            } catch (error: any) {
                // Em caso de erro, talvez seja melhor recarregar a lista para garantir a consistência
                await this.fetchAlertas();
                throw new Error('Falha ao marcar alerta como lido.');
            }
        },

        async marcarTodosAlertasComoLidos() {
            const api = useApi();
            try {
                // Endpoint para marcar todos os alertas do usuário como lidos
                await api.post('/api/alertas/marcar-todos-lidos', {});
                // Após marcar todos, busca a lista atualizada
                await this.fetchAlertas();
            } catch (error: any) {
                throw new Error('Falha ao marcar todos os alertas como lidos.');
            }
        },
    }
});