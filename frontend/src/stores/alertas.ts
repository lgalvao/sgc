import {defineStore} from 'pinia';
import {Alerta} from '@/mappers/alertas';
import * as painelService from '../services/painelService';
import {Page} from '@/services/painelService';
import * as alertaService from '../services/alertaService';
import {usePerfilStore} from './perfil';

export const useAlertasStore = defineStore('alertas', {
    state: () => ({
        alertas: [] as Alerta[],
        alertasPage: {} as Page<Alerta>,
    }),
    getters: {
        // Não há mais a propriedade 'lido' no DTO do backend, então este getter não é mais aplicável
        // getAlertasNaoLidos: (state) => () => {
        //     return state.alertas.filter(alerta => !alerta.lido);
        // }
    },
    actions: {
        async fetchAlertas(usuarioTitulo: string, unidade: number, page: number, size: number) {
            const response = await painelService.listarAlertas(usuarioTitulo, unidade, page, size);
            this.alertas = response.content;
            this.alertasPage = response;
        },

        async marcarAlertaComoLido(idAlerta: number): Promise<boolean> {
            try {
                await alertaService.marcarComoLido(idAlerta);
                const perfilStore = usePerfilStore();
                if (perfilStore.servidorId && perfilStore.unidadeSelecionada) {
                    await this.fetchAlertas(perfilStore.servidorId.toString(), Number(perfilStore.unidadeSelecionada), 0, 20);
                }
                return true;
            } catch (error) {
                return false;
            }
        },
    }
});