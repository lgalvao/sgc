import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useSubprocessosStore} from '../subprocessos';
import * as subprocessoService from '@/services/subprocessoService';
import {mockProcessoDetalhe} from "@/test-utils/mocks";

const mockProcessosStore = {
    processoDetalhe: { ...mockProcessoDetalhe, codigo: 1 },
    fetchProcessoDetalhe: vi.fn(),
};

vi.mock('@/stores/processos', () => ({
    useProcessosStore: vi.fn(() => mockProcessosStore),
}));

vi.mock('@/services/subprocessoService', async (importOriginal) => {
    const actual = await importOriginal() as object;
    return {
        ...actual,
        fetchSubprocessoDetalhe: vi.fn(),
        disponibilizarCadastro: vi.fn(),
        disponibilizarRevisaoCadastro: vi.fn(),
        devolverCadastro: vi.fn(),
        aceitarCadastro: vi.fn(),
        homologarCadastro: vi.fn(),
        devolverRevisaoCadastro: vi.fn(),
        aceitarRevisaoCadastro: vi.fn(),
        homologarRevisaoCadastro: vi.fn(),
    };
});

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
        const idSubprocesso = 123;
        const mockDetalhe = {codigo: idSubprocesso, nome: 'Detalhe Teste'};

        beforeEach(() => {
            vi.spyOn(subprocessoService, 'fetchSubprocessoDetalhe').mockResolvedValue(mockDetalhe as any);
        });



        it('disponibilizarCadastro should call service and refresh process details', async () => {
            await store.disponibilizarCadastro(idSubprocesso);
            expect(subprocessoService.disponibilizarCadastro).toHaveBeenCalledWith(idSubprocesso);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('disponibilizarRevisaoCadastro should call service and refresh process details', async () => {
            await store.disponibilizarRevisaoCadastro(idSubprocesso);
            expect(subprocessoService.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(idSubprocesso);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('devolverCadastro should call service and refresh process details', async () => {
            const req = {motivo: 'motivo de teste', observacoes: 'test'};
            await store.devolverCadastro(idSubprocesso, req);
            expect(subprocessoService.devolverCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('aceitarCadastro should call service and refresh process details', async () => {
            const req = {motivo: 'motivo de teste', observacoes: 'test'};
            await store.aceitarCadastro(idSubprocesso, req);
            expect(subprocessoService.aceitarCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('homologarCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test'};
            await store.homologarCadastro(idSubprocesso, req);
            expect(subprocessoService.homologarCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('devolverRevisaoCadastro should call service and refresh process details', async () => {
            const req = {motivo: 'motivo de teste', observacoes: 'test'}; // Adicionar motivo
            await store.devolverRevisaoCadastro(idSubprocesso, req);
            expect(subprocessoService.devolverRevisaoCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('aceitarRevisaoCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test'};
            await store.aceitarRevisaoCadastro(idSubprocesso, req);
            expect(subprocessoService.aceitarRevisaoCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('homologarRevisaoCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test'};
            await store.homologarRevisaoCadastro(idSubprocesso, req);
            expect(subprocessoService.homologarRevisaoCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });
    });
});