import {beforeEach, describe, expect, it, vi} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {useAlertasStore} from '../alertas';
import {usePerfilStore} from '../perfil';
import type {Alerta} from '@/types/tipos';

// Mock the alertas.json import
vi.mock('../../mocks/alertas.json', () => ({
    default: [
        {
            "id": 1,
            "unidadeOrigem": "COSIS",
            "unidadeDestino": "SESEL",
            "dataHora": "2025-07-02T10:00:00",
            "idProcesso": 1,
            "descricao": "Cadastro devolvido para ajustes"
        },
        {
            "id": 2,
            "unidadeOrigem": "SEDOC",
            "unidadeDestino": "SEDESENV",
            "dataHora": "2025-07-03T14:30:00",
            "idProcesso": 1,
            "descricao": "Prazo próximo para validação do mapa de competências"
        },
        {
            "id": 3,
            "unidadeOrigem": "SEDOC",
            "unidadeDestino": "SEDESENV",
            "dataHora": "2025-07-04T09:15:00",
            "idProcesso": 2,
            "descricao": "Nova atribuição temporária: Bruno Silva (10/10/2025 a 10/11/2025)"
        }
    ]
}));

// Mock the alertas-servidor.json import
vi.mock('../../mocks/alertas-servidor.json', () => ({
    default: [
        {
            "id": 1,
            "idAlerta": 1,
            "idServidor": 1,
            "lido": false,
            "dataLeitura": null
        },
        {
            "id": 2,
            "idAlerta": 2,
            "idServidor": 1,
            "lido": true,
            "dataLeitura": "2025-07-03T15:00:00"
        },
        {
            "id": 3,
            "idAlerta": 3,
            "idServidor": 1,
            "lido": false,
            "dataLeitura": null
        }
    ]
}));

describe('useAlertasStore', () => {
    let alertasStore: ReturnType<typeof useAlertasStore>;

    beforeEach(() => {
        initPinia();
        alertasStore = useAlertasStore();
        // Reset the store state to the mock data before each test
        alertasStore.$reset(); // Pinia's $reset() method
    });

    it('should initialize with mock alerts and parsed dates', () => {
        expect(alertasStore.alertas.length).toBe(3); // Directly use the expected length
        expect(alertasStore.alertas[0].dataHora).toBeInstanceOf(Date);
        expect(alertasStore.alertas[0].id).toBe(1);
    });

    describe('pesquisarAlertas', () => {
        it('should return all alerts when no query is provided', () => {
            const result = alertasStore.pesquisarAlertas();
            expect(result.length).toBe(3); // Directly use the expected length
            expect(result[0].id).toBe(1);
        });

        it('should filter alerts by descricao (case-insensitive)', () => {
            const result = alertasStore.pesquisarAlertas('cadastro');
            expect(result.length).toBe(1);
            expect(result[0].id).toBe(1);
        });

        it('should filter alerts by unidadeOrigem (case-insensitive)', () => {
            const result = alertasStore.pesquisarAlertas('SEDOC');
            expect(result.length).toBe(2);
            expect(result[0].id).toBe(2);
            expect(result[1].id).toBe(3);
        });

        it('should filter alerts by unidadeDestino (case-insensitive)', () => {
            const result = alertasStore.pesquisarAlertas('SEDESENV');
            expect(result.length).toBe(2);
            expect(result[0].id).toBe(2);
            expect(result[1].id).toBe(3);
        });

        it('should return an empty array if no matching alerts are found', () => {
            const result = alertasStore.pesquisarAlertas('nonexistent');
            expect(result.length).toBe(0);
        });
    });

    describe('getAlertaById', () => {
        it('should return the correct alert when a valid ID is provided', () => {
            const result = alertasStore.getAlertaById(2);
            expect(result).toBeDefined();
            expect(result?.id).toBe(2);
            expect(result?.descricao).toBe('Prazo próximo para validação do mapa de competências');
        });

        it('should return undefined when an invalid ID is provided', () => {
            const result = alertasStore.getAlertaById(999);
            expect(result).toBeUndefined();
        });
    });

    describe('criarAlerta', () => {
        it('should add a new alert to the store', () => {
            const newAlertaData = {
                unidadeOrigem: 'NOVA',
                unidadeDestino: 'DEST',
                dataHora: new Date('2025-08-01T12:00:00'),
                idProcesso: 3,
                descricao: 'Novo alerta de teste'
            };
            const initialLength = alertasStore.alertas.length;
            const createdAlerta = alertasStore.criarAlerta(newAlertaData);

            expect(alertasStore.alertas.length).toBe(initialLength + 1);
            expect(createdAlerta).toBeDefined();
            expect(createdAlerta.id).toBe(initialLength + 1); // Assuming sequential IDs
            expect(createdAlerta.descricao).toBe('Novo alerta de teste');
            expect(alertasStore.alertas.some(a => a.id === createdAlerta.id)).toBe(true);
        });

        it('should assign a unique ID to the new alert', () => {
            const newAlertaData1 = {
                unidadeOrigem: 'A', unidadeDestino: 'B', dataHora: new Date(), idProcesso: 1, descricao: 'Test 1'
            };
            const newAlertaData2 = {
                unidadeOrigem: 'C', unidadeDestino: 'D', dataHora: new Date(), idProcesso: 2, descricao: 'Test 2'
            };
            const createdAlerta1 = alertasStore.criarAlerta(newAlertaData1);
            const createdAlerta2 = alertasStore.criarAlerta(newAlertaData2);

            expect(createdAlerta1.id).not.toBe(createdAlerta2.id);
        });
    });

    describe('atualizarAlerta', () => {
        it('should update an existing alert', () => {
            const updatedAlerta: Alerta = {
                id: 1,
                unidadeOrigem: 'UPDATED',
                unidadeDestino: 'UPDATED',
                dataHora: new Date('2025-07-02T10:00:00'),
                idProcesso: 1,
                descricao: 'Descrição atualizada'
            };
            const result = alertasStore.atualizarAlerta(updatedAlerta);
            expect(result).toBe(true);
            const foundAlerta = alertasStore.getAlertaById(1);
            expect(foundAlerta?.descricao).toBe('Descrição atualizada');
            expect(foundAlerta?.unidadeOrigem).toBe('UPDATED');
        });

        it('should return false if the alert was not found', () => {
            const nonExistentAlerta: Alerta = {
                id: 999,
                unidadeOrigem: 'X',
                unidadeDestino: 'Y',
                dataHora: new Date(),
                idProcesso: 99,
                descricao: 'Non existent'
            };
            const result = alertasStore.atualizarAlerta(nonExistentAlerta);
            expect(result).toBe(false);
        });
    });

    describe('excluirAlerta', () => {
        it('should remove an alert from the store', () => {
            const initialLength = alertasStore.alertas.length;
            const result = alertasStore.excluirAlerta(1);
            expect(result).toBe(true);
            expect(alertasStore.alertas.length).toBe(initialLength - 1);
            expect(alertasStore.getAlertaById(1)).toBeUndefined();
        });

        it('should return false if the alert was not found', () => {
            const initialLength = alertasStore.alertas.length;
            const result = alertasStore.excluirAlerta(999);
            expect(result).toBe(false);
            expect(alertasStore.alertas.length).toBe(initialLength);
        });
    });

    describe('Sistema de Alertas por Servidor', () => {
        let perfilStore: ReturnType<typeof usePerfilStore>;

        beforeEach(() => {
            perfilStore = usePerfilStore();
            // Mock servidor logado como ID 1
            perfilStore.setServidorId(1);
        });

        describe('getAlertasDoServidor', () => {
            it('should return alerts with read status for the logged-in server', () => {
                const alertasComStatus = alertasStore.getAlertasDoServidor();

                expect(alertasComStatus).toHaveLength(3);
                expect(alertasComStatus[0]).toHaveProperty('lido', false);
                expect(alertasComStatus[1]).toHaveProperty('lido', true);
                expect(alertasComStatus[2]).toHaveProperty('lido', false);
            });

            it('should include dataLeitura for read alerts', () => {
                const alertasComStatus = alertasStore.getAlertasDoServidor();

                const alertaLido = alertasComStatus.find(a => a.id === 2);
                expect(alertaLido?.dataLeitura).toBeInstanceOf(Date);
                // Verificar apenas que a data existe, sem comparar exatamente devido ao timezone
                expect(alertaLido?.dataLeitura).toBeDefined();
            });
        });

        describe('getAlertasNaoLidos', () => {
            it('should return only unread alerts for the logged-in server', () => {
                const alertasNaoLidos = alertasStore.getAlertasNaoLidos();

                expect(alertasNaoLidos).toHaveLength(2);
                expect(alertasNaoLidos[0].id).toBe(1);
                expect(alertasNaoLidos[1].id).toBe(3);
            });

            it('should return empty array when all alerts are read', () => {
                // Marcar todos os alertas como lidos
                alertasStore.marcarTodosAlertasComoLidos();

                const alertasNaoLidos = alertasStore.getAlertasNaoLidos();
                expect(alertasNaoLidos).toHaveLength(0);
            });
        });

        describe('marcarAlertaComoLido', () => {
            it('should mark a specific alert as read for the logged-in server', () => {
                const result = alertasStore.marcarAlertaComoLido(1);
                expect(result).toBe(true);

                const alertasComStatus = alertasStore.getAlertasDoServidor();
                const alertaMarcado = alertasComStatus.find(a => a.id === 1);
                expect(alertaMarcado?.lido).toBe(true);
                expect(alertaMarcado?.dataLeitura).toBeInstanceOf(Date);
            });

            it('should create new AlertaServidor record if it does not exist', () => {
                // Simular alerta sem registro de servidor
                const initialLength = alertasStore.alertasServidor.length;

                const result = alertasStore.marcarAlertaComoLido(999); // ID que não existe
                expect(result).toBe(false); // Deve falhar pois alerta não existe

                // Testar com alerta existente mas sem registro de servidor
                // Remover o registro existente do alerta 3 para testar criação
                alertasStore.alertasServidor = alertasStore.alertasServidor.filter(as => as.idAlerta !== 3);

                const result2 = alertasStore.marcarAlertaComoLido(3); // Existe mas sem registro inicial
                expect(result2).toBe(true);
                expect(alertasStore.alertasServidor.length).toBe(initialLength); // Mesmo tamanho pois removemos 1 e adicionamos 1
            });
        });

        describe('marcarTodosAlertasComoLidos', () => {
            it('should mark all alerts as read for the logged-in server', () => {
                alertasStore.marcarTodosAlertasComoLidos();

                const alertasComStatus = alertasStore.getAlertasDoServidor();
                alertasComStatus.forEach(alerta => {
                    expect(alerta.lido).toBe(true);
                    expect(alerta.dataLeitura).toBeInstanceOf(Date);
                });
            });

            it('should create AlertaServidor records for alerts without existing records', () => {
                const initialLength = alertasStore.alertasServidor.length;

                // Simular cenário onde alguns alertas não têm registros de servidor
                alertasStore.alertasServidor = alertasStore.alertasServidor.filter(as => as.idAlerta !== 3);

                alertasStore.marcarTodosAlertasComoLidos();

                expect(alertasStore.alertasServidor.length).toBe(initialLength); // Deve manter o mesmo tamanho pois recriou o registro
            });
        });
    });
});