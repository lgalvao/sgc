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
        alertas: JSON.parse(JSON.stringify(alertasMock)).map(parseAlertaDates) as Alerta[],
        alertasServidor: JSON.parse(JSON.stringify(alertasServidorMock)).map(parseAlertaServidorDates) as AlertaServidor[]
    }),
    getters: {
        getAlertasDoServidor(state) {
            const perfilStore = usePerfilStore();
            const servidorLogadoIdStr = perfilStore.usuario?.tituloEleitoral;
            if (!servidorLogadoIdStr) return [];
            const servidorLogadoId = parseInt(servidorLogadoIdStr, 10);

            return state.alertas.map(alerta => {
                const alertaServidor = state.alertasServidor.find(
                    as => as.idAlerta === alerta.id && as.idServidor === servidorLogadoId
                );
                return {
                    ...alerta,
                    lido: !!alertaServidor?.lido,
                    dataLeitura: alertaServidor?.dataLeitura || null,
                };
            });
        },
        getAlertasNaoLidos(): Alerta[] {
            return this.getAlertasDoServidor.filter(alerta => !alerta.lido);
        }
    },
    actions: {
        marcarAlertaComoLido(idAlerta: number): boolean {
            const perfilStore = usePerfilStore();
            const servidorLogadoIdStr = perfilStore.usuario?.tituloEleitoral;
            if (!servidorLogadoIdStr) return false;
            const servidorLogadoId = parseInt(servidorLogadoIdStr, 10);

            if (!this.alertas.some(a => a.id === idAlerta)) return false;

            const registroExistente = this.alertasServidor.find(
                as => as.idAlerta === idAlerta && as.idServidor === servidorLogadoId
            );

            if (registroExistente) {
                this.alertasServidor = this.alertasServidor.map(as =>
                    as.id === registroExistente.id ? { ...as, lido: true, dataLeitura: new Date() } : as
                );
            } else {
                const newId = (this.alertasServidor.length > 0 ? Math.max(...this.alertasServidor.map(as => as.id)) : 0) + 1;
                this.alertasServidor.push({
                    id: newId,
                    idAlerta,
                    idServidor: servidorLogadoId,
                    lido: true,
                    dataLeitura: new Date(),
                });
            }
            return true;
        },

        marcarTodosAlertasComoLidos(): void {
            this.getAlertasDoServidor.forEach(alerta => {
                if (!alerta.lido) {
                    this.marcarAlertaComoLido(alerta.id);
                }
            });
        },

        reset() {
            this.alertas = JSON.parse(JSON.stringify(alertasMock)).map(parseAlertaDates);
            this.alertasServidor = JSON.parse(JSON.stringify(alertasServidorMock)).map(parseAlertaServidorDates);
        }
    }
});