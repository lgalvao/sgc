import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useSubprocessosStore} from '../subprocessos';
import * as subprocessoService from '@/services/subprocessoService';
import {useProcessosStore} from "@/stores/processos";
import {mockProcessoDetalhe} from "@/test-utils/mocks";

// Mock a store completa
vi.mock('@/stores/processos', () => ({
    useProcessosStore: vi.fn(() => ({
        processoDetalhe: { ...mockProcessoDetalhe, codigo: 1 },
        fetchProcessoDetalhe: vi.fn(),
    })),
}));

vi.mock('@/services/subprocessoService', () => ({
    obterSubprocessoDetalhe: vi.fn(),
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
    let processosStore: ReturnType<typeof useProcessosStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useSubprocessosStore();
        processosStore = useProcessosStore();
        vi.clearAllMocks();
    });

    it('should initialize with a null subprocessoDetalhe', () => {
        expect(store.subprocessoDetalhe).toBeNull();
    });

    it('reset should clear the subprocessoDetalhe', () => {
        store.subprocessoDetalhe = {} as any; // mock state
        store.reset();
        expect(store.subprocessoDetalhe).toBeNull();
    });

    describe('actions', () => {
        const idSubprocesso = 123;

        it('fetchSubprocessoDetalhe should call service and update state', async () => {
            const mockDetalhe = {codigo: idSubprocesso, nome: 'Detalhe Teste'};
            vi.mocked(subprocessoService.obterSubprocessoDetalhe).mockResolvedValue(mockDetalhe as any);

            await store.fetchSubprocessoDetalhe(idSubprocesso);

            expect(subprocessoService.obterSubprocessoDetalhe).toHaveBeenCalledWith(idSubprocesso);
            expect(store.subprocessoDetalhe).toEqual(mockDetalhe);
        });

        it('disponibilizarCadastro should call service and refresh process details', async () => {
            await store.disponibilizarCadastro(idSubprocesso);
            expect(subprocessoService.disponibilizarCadastro).toHaveBeenCalledWith(idSubprocesso);
            expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('disponibilizarRevisaoCadastro should call service and refresh process details', async () => {
            await store.disponibilizarRevisaoCadastro(idSubprocesso);
            expect(subprocessoService.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(idSubprocesso);
            expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('devolverCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test', analista: 'GESTOR'};
            await store.devolverCadastro(idSubprocesso, req);
            expect(subprocessoService.devolverCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('aceitarCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test', analista: 'GESTOR'};
            await store.aceitarCadastro(idSubprocesso, req);
            expect(subprocessoService.aceitarCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('homologarCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test', analista: 'ADMIN'};
            await store.homologarCadastro(idSubprocesso, req);
            expect(subprocessoService.homologarCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('devolverRevisaoCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test', analista: 'GESTOR'};
            await store.devolverRevisaoCadastro(idSubprocesso, req);
            expect(subprocessoService.devolverRevisaoCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('aceitarRevisaoCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test', analista: 'GESTOR'};
            await store.aceitarRevisaoCadastro(idSubprocesso, req);
            expect(subprocessoService.aceitarRevisaoCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });

        it('homologarRevisaoCadastro should call service and refresh process details', async () => {
            const req = {observacoes: 'test', analista: 'ADMIN'};
            await store.homologarRevisaoCadastro(idSubprocesso, req);
            expect(subprocessoService.homologarRevisaoCadastro).toHaveBeenCalledWith(idSubprocesso, req);
            expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
        });
    });
});