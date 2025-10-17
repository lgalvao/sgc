import {beforeEach, describe, expect, it, vi, Mocked} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {useAlertasStore} from '../alertas';
import {usePerfilStore} from '../perfil';
import type {Alerta} from '@/types/tipos';

// Mock dos serviços
vi.mock('@/services/painelService');
vi.mock('@/services/alertaService');

// Mock do perfilStore
vi.mock('../perfil', () => ({
    usePerfilStore: vi.fn(() => ({
        servidorId: 123, // Mock de um ID de servidor
        unidadeSelecionada: '456', // Mock de uma unidade selecionada
    })),
}));

describe('useAlertasStore', () => {
    let alertasStore: ReturnType<typeof useAlertasStore>;
    let painelService: Mocked<typeof import('@/services/painelService')>;
    let alertaService: Mocked<typeof import('@/services/alertaService')>;
    let perfilStore: ReturnType<typeof usePerfilStore>;

    beforeEach(async () => {
        initPinia();
        alertasStore = useAlertasStore();
        perfilStore = usePerfilStore(); // Obter a instância mockada

        painelService = (await import('@/services/painelService')) as Mocked<typeof import('@/services/painelService')>;
        alertaService = (await import('@/services/alertaService')) as Mocked<typeof import('@/services/alertaService')>;

        vi.clearAllMocks();
        alertasStore.$reset();
    });

    it('should initialize with mock alerts and parsed dates', () => {
        expect(alertasStore.alertas).toEqual([]);
        expect(alertasStore.alertasPage).toEqual({});
    });

    describe('actions', () => {
        it('fetchAlertas should call painelService and update state', async () => {
            const mockPage = {
                content: [{codigo: 1, descricao: 'Alerta Teste', dataHora: '2025-01-01T10:00:00', processoCodigo: 1, unidadeOrigemCodigo: 1, usuarioDestinoTitulo: '123'}],
                totalPages: 1,
                totalElements: 1,
                number: 0,
                size: 10,
                first: true,
                last: true,
                empty: false
            };
            painelService.listarAlertas.mockResolvedValue(mockPage);

            await alertasStore.fetchAlertas('123', 456, 0, 10);

            expect(painelService.listarAlertas).toHaveBeenCalledWith('123', 456, 0, 10);
            expect(alertasStore.alertas).toEqual(mockPage.content);
            expect(alertasStore.alertasPage).toEqual(mockPage);
        });

        describe('marcarAlertaComoLido', () => {
            it('should call alertaService and reload alerts on success', async () => {
                alertaService.marcarComoLido.mockResolvedValue();
                // Mock para o fetchAlerts que é chamado internamente
                const mockReloadPage = {
                    content: [{codigo: 2, descricao: 'Alerta Recarregado', dataHora: '2025-01-01T10:00:00', processoCodigo: 1, unidadeOrigemCodigo: 1, usuarioDestinoTitulo: '123'}],
                    totalPages: 1,
                    totalElements: 1,
                    number: 0,
                    size: 10,
                    first: true,
                    last: true,
                    empty: false
                };
                painelService.listarAlertas.mockResolvedValue(mockReloadPage);

                const result = await alertasStore.marcarAlertaComoLido(1);

                expect(result).toBe(true);
                expect(alertaService.marcarComoLido).toHaveBeenCalledWith(1);
                expect(painelService.listarAlertas).toHaveBeenCalledWith('123', 456, 0, 20);
                expect(alertasStore.alertas).toEqual(mockReloadPage.content);
            });

            it('should return false on service failure', async () => {
                alertaService.marcarComoLido.mockRejectedValue(new Error('Falha no serviço'));

                const result = await alertasStore.marcarAlertaComoLido(1);

                expect(result).toBe(false);
                expect(painelService.listarAlertas).not.toHaveBeenCalled();
            });

            it('should handle missing perfilStore data', async () => {
                // Configurar o mock para retornar null
                (perfilStore as any).servidorId = null;
                (perfilStore as any).unidadeSelecionada = null;

                alertaService.marcarComoLido.mockResolvedValue();

                const result = await alertasStore.marcarAlertaComoLido(1);

                expect(result).toBe(true);
                expect(alertaService.marcarComoLido).toHaveBeenCalledWith(1);
                // Não deve tentar recarregar os alertas se os dados do perfil não estiverem disponíveis
                expect(painelService.listarAlertas).not.toHaveBeenCalled();
            });
        });
    });
});