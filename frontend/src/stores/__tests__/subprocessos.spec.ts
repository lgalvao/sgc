import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useSubprocessosStore} from '../subprocessos';
import * as subprocessoService from '@/services/subprocessoService';
import * as cadastroService from '@/services/cadastroService';
import {mockProcessoDetalhe} from "@/test-utils/mocks";

const mockProcessosStore = {
    processoDetalhe: { ...mockProcessoDetalhe, codigo: 1 },
    fetchProcessoDetalhe: vi.fn(),
};

vi.mock('@/stores/processos', () => ({
    useProcessosStore: vi.fn(() => mockProcessosStore),
}));

vi.mock('@/services/subprocessoService', () => ({
    fetchSubprocessoDetalhe: vi.fn(),
}));

vi.mock('@/services/cadastroService', () => ({
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
    devolverCadastro: vi.fn(),
    aceitarCadastro: vi.fn(),
    homologarCadastro: vi.fn(),
    devolverRevisaoCadastro: vi.fn(),
    aceitarRevisaoCadastro: vi.fn(),
    homologarRevisaoCadastro: vi.fn(),
}));

describe('useSubprocessosStore', () => {
    let store: ReturnType<typeof useSubprocessosStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useSubprocessosStore();
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('should initialize with a null subprocessoDetalhe', () => {
        expect(store.subprocessoDetalhe).toBeNull();
    });

    it('reset should clear the subprocessoDetalhe', () => {
        store.subprocessoDetalhe = {} as any;
        store.$reset();
        expect(store.subprocessoDetalhe).toBeNull();
    });

    describe('actions', () => {
        const codSubrocesso = 123;
        const mockDetalhe = {codigo: codSubrocesso, nome: 'Detalhe Teste'};

        beforeEach(() => {
            vi.spyOn(subprocessoService, 'fetchSubprocessoDetalhe').mockResolvedValue(mockDetalhe as any);
        });



        it('disponibilizarCadastro should call service and refresh process details', async () => {
            await store.disponibilizarCadastro(codSubrocesso);
            expect(cadastroService.disponibilizarCadastro).toHaveBeenCalledWith(codSubrocesso);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('disponibilizarRevisaoCadastro should call service and refresh process details', async () => {
            await store.disponibilizarRevisaoCadastro(codSubrocesso);
            expect(cadastroService.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(codSubrocesso);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('devolverCadastro should call service and refresh process details', async () => {
            const req = {motivo: 'motivo de teste', observacoes: 'test'};
            await store.devolverCadastro(codSubrocesso, req);
            expect(cadastroService.devolverCadastro).toHaveBeenCalledWith(codSubrocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('aceitarCadastro should call service and refresh process details', async () => {
            const req = {motivo: 'motivo de teste', observacoes: 'test'};
            await store.aceitarCadastro(codSubrocesso, req);
            expect(cadastroService.aceitarCadastro).toHaveBeenCalledWith(codSubrocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('homologarCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test'};
            await store.homologarCadastro(codSubrocesso, req);
            expect(cadastroService.homologarCadastro).toHaveBeenCalledWith(codSubrocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('devolverRevisaoCadastro should call service and refresh process details', async () => {
            const req = {motivo: 'motivo de teste', observacoes: 'test'}; // Adicionar motivo
            await store.devolverRevisaoCadastro(codSubrocesso, req);
            expect(cadastroService.devolverRevisaoCadastro).toHaveBeenCalledWith(codSubrocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('aceitarRevisaoCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test'};
            await store.aceitarRevisaoCadastro(codSubrocesso, req);
            expect(cadastroService.aceitarRevisaoCadastro).toHaveBeenCalledWith(codSubrocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('homologarRevisaoCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test'};
            await store.homologarRevisaoCadastro(codSubrocesso, req);
            expect(cadastroService.homologarRevisaoCadastro).toHaveBeenCalledWith(codSubrocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });
    });
});