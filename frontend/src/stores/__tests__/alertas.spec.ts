import {beforeEach, describe, expect, it, vi, Mocked} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {useAlertasStore} from '../alertas';

// Mock dos serviços
vi.mock('@/services/painelService');
vi.mock('@/services/alertaService');

// Mock do perfilStore para garantir que a mesma instância seja usada
const mockPerfilStoreValues = {
    servidorId: 123 as number | null,
    unidadeSelecionada: '456' as string | null,
};
vi.mock('../perfil', () => ({
    usePerfilStore: vi.fn(() => mockPerfilStoreValues),
}));

describe('useAlertasStore', () => {
    let alertasStore: ReturnType<typeof useAlertasStore>;
    let painelService: Mocked<typeof import('@/services/painelService')>;
    let alertaService: Mocked<typeof import('@/services/alertaService')>;

    beforeEach(async () => {
        initPinia();
        alertasStore = useAlertasStore();

        // Resetar o estado do mock antes de cada teste
        mockPerfilStoreValues.servidorId = 123;
        mockPerfilStoreValues.unidadeSelecionada = '456';

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
                content: [{codigo: 1, descricao: 'Alerta Teste', dataHora: '2025-01-01T10:00:00', processoCodigo: 1, unidadeOrigemCodigo: 1, unidadeDestinoCodigo: 2, usuarioDestinoTitulo: '123'}],
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
                    content: [{codigo: 2, descricao: 'Alerta Recarregado', dataHora: '2025-01-01T10:00:00', processoCodigo: 1, unidadeOrigemCodigo: 1, unidadeDestinoCodigo: 2, usuarioDestinoTitulo: '123'}],
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
                mockPerfilStoreValues.servidorId = null;
                mockPerfilStoreValues.unidadeSelecionada = null;

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