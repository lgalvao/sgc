import {defineStore} from 'pinia';
import alertasMock from '../mocks/alertas.json';
import alertasServidorMock from '../mocks/alertas-servidor.json';
import type {Alerta, AlertaServidor} from '@/types/tipos';
import {usePerfilStore} from './perfil';

function parseAlertaDates(alerta: Omit<Alerta, 'dataHora'> & { dataHora: string }): Alerta {
    return {
        ...alerta,
        dataHora: new Date(alerta.dataHora),
    };
}

function parseAlertaServidorDates(alertaServidor: Omit<AlertaServidor, 'dataLeitura'> & {
    dataLeitura: string | null
}): AlertaServidor {
    return {
        ...alertaServidor,
        dataLeitura: alertaServidor.dataLeitura ? new Date(alertaServidor.dataLeitura) : null,
    };
}

export const useAlertasStore = defineStore('alertas', {
    state: () => ({
        alertas: alertasMock.map(parseAlertaDates) as Alerta[],
        alertasServidor: alertasServidorMock.map(parseAlertaServidorDates) as AlertaServidor[]
    }),
    getters: {
        getAlertasDoServidor: (state) => () => {
            const perfilStore = usePerfilStore();
            const servidorLogado = perfilStore.servidorId;

            return state.alertas.map(alerta => {
                const alertaServidor = state.alertasServidor.find(
                    as => as.idAlerta === alerta.id && as.idServidor === servidorLogado
                );

                return {
                    ...alerta,
                    lido: alertaServidor?.lido || false,
                    dataLeitura: alertaServidor?.dataLeitura || null
                };
            });
        },
        getAlertasNaoLidos: (state) => () => {
            const perfilStore = usePerfilStore();
            const servidorLogado = perfilStore.servidorId;

            return state.alertas.filter(alerta => {
                const alertaServidor = state.alertasServidor.find(
                    as => as.idAlerta === alerta.id && as.idServidor === servidorLogado
                );
                return !alertaServidor?.lido;
            });
        }
    },
    actions: {
        pesquisarAlertas(query?: string): Alerta[] {
            if (!query) {
                return this.alertas;
            }
            const lowerCaseQuery = query.toLowerCase();
            return this.alertas.filter(alerta =>
                alerta.descricao.toLowerCase().includes(lowerCaseQuery) ||
                alerta.unidadeOrigem.toLowerCase().includes(lowerCaseQuery) ||
                alerta.unidadeDestino.toLowerCase().includes(lowerCaseQuery)
            );
        },

        getAlertaById(id: number): Alerta | undefined {
            return this.alertas.find(alerta => alerta.id === id);
        },

        criarAlerta(novoAlerta: Omit<Alerta, 'id'>): Alerta {
            const newId = this.alertas.length > 0 ? Math.max(...this.alertas.map(a => a.id)) + 1 : 1;
            const alertaComId: Alerta = {...novoAlerta, id: newId};
            this.alertas.push(alertaComId);
            return alertaComId;
        },

        atualizarAlerta(alertaAtualizado: Alerta): boolean {
            const index = this.alertas.findIndex(alerta => alerta.id === alertaAtualizado.id);
            if (index !== -1) {
                this.alertas[index] = alertaAtualizado;
                return true;
            }
            return false;
        },

        excluirAlerta(id: number): boolean {
            const initialLength = this.alertas.length;
            this.alertas = this.alertas.filter(alerta => alerta.id !== id);
            return this.alertas.length < initialLength;
        },

        marcarAlertaComoLido(idAlerta: number): boolean {
            const perfilStore = usePerfilStore();
            const servidorLogado = perfilStore.servidorId;

            // Verificar se o alerta existe
            const alertaExiste = this.alertas.some(a => a.id === idAlerta);
            if (!alertaExiste) {
                return false;
            }

            const alertaServidorIndex = this.alertasServidor.findIndex(
                as => as.idAlerta === idAlerta && as.idServidor === servidorLogado
            );

            if (alertaServidorIndex !== -1) {
                this.alertasServidor[alertaServidorIndex] = {
                    ...this.alertasServidor[alertaServidorIndex],
                    lido: true,
                    dataLeitura: new Date()
                };
                return true;
            } else {
                // Criar novo registro se não existir
                const newId = this.alertasServidor.length > 0 ? Math.max(...this.alertasServidor.map(as => as.id)) + 1 : 1;
                this.alertasServidor.push({
                    id: newId,
                    idAlerta,
                    idServidor: servidorLogado,
                    lido: true,
                    dataLeitura: new Date()
                });
                return true;
            }
        },

        marcarTodosAlertasComoLidos(): void {
            const perfilStore = usePerfilStore();
            const servidorLogado = perfilStore.servidorId;

            this.alertas.forEach(alerta => {
                const alertaServidorIndex = this.alertasServidor.findIndex(
                    as => as.idAlerta === alerta.id && as.idServidor === servidorLogado
                );

                if (alertaServidorIndex === -1) {
                    const newId = this.alertasServidor.length > 0 ? Math.max(...this.alertasServidor.map(as => as.id)) + 1 : 1;
                    this.alertasServidor.push({
                        id: newId,
                        idAlerta: alerta.id,
                        idServidor: servidorLogado,
                        lido: true,
                        dataLeitura: new Date()
                    });
                } else if (!this.alertasServidor[alertaServidorIndex].lido) {
                    this.alertasServidor[alertaServidorIndex] = {
                        ...this.alertasServidor[alertaServidorIndex],
                        lido: true,
                        dataLeitura: new Date()
                    };
                }
            });
        }
    }
});