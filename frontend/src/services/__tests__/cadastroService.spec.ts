import { beforeEach, describe, expect, it, vi, type Mocked } from 'vitest';
import * as cadastroService from '@/services/cadastroService';
import apiClient from '@/axios-setup';

vi.mock('@/axios-setup');

describe('cadastroService', () => {
    const mockedApiClient = apiClient as Mocked<typeof apiClient>;

    beforeEach(() => {
        vi.clearAllMocks();
    });

    const testEndpoint = async (serviceFn: (id: number, data?: any) => Promise<void>, endpoint: string, withData = false) => {
        mockedApiClient.post.mockResolvedValue({});
        const data = withData ? { motivo: 'teste', observacoes: 'teste' } : undefined;
        await serviceFn(1, data);
        if (withData) {
            expect(mockedApiClient.post).toHaveBeenCalledWith(endpoint, data);
        } else {
            expect(mockedApiClient.post).toHaveBeenCalledWith(endpoint);
        }
    };

    it('disponibilizarCadastro deve chamar o endpoint correto', async () => {
        await testEndpoint(cadastroService.disponibilizarCadastro, '/subprocessos/1/disponibilizar');
    });

    it('disponibilizarRevisaoCadastro deve chamar o endpoint correto', async () => {
        await testEndpoint(cadastroService.disponibilizarRevisaoCadastro, '/subprocessos/1/disponibilizar-revisao');
    });

    it('devolverCadastro deve chamar o endpoint correto', async () => {
        await testEndpoint(cadastroService.devolverCadastro, '/subprocessos/1/devolver-cadastro', true);
    });

    it('aceitarCadastro deve chamar o endpoint correto', async () => {
        await testEndpoint(cadastroService.aceitarCadastro, '/subprocessos/1/aceitar-cadastro', true);
    });

    it('homologarCadastro deve chamar o endpoint correto', async () => {
        await testEndpoint(cadastroService.homologarCadastro, '/subprocessos/1/homologar-cadastro', true);
    });

    it('devolverRevisaoCadastro deve chamar o endpoint correto', async () => {
        await testEndpoint(cadastroService.devolverRevisaoCadastro, '/subprocessos/1/devolver-revisao-cadastro', true);
    });

    it('aceitarRevisaoCadastro deve chamar o endpoint correto', async () => {
        await testEndpoint(cadastroService.aceitarRevisaoCadastro, '/subprocessos/1/aceitar-revisao-cadastro', true);
    });

    it('homologarRevisaoCadastro deve chamar o endpoint correto', async () => {
        await testEndpoint(cadastroService.homologarRevisaoCadastro, '/subprocessos/1/homologar-revisao-cadastro', true);
    });
});
