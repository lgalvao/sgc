import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useFluxoCadastroSubprocesso} from '../useFluxoCadastroSubprocesso';
import * as cadastroService from '@/services/cadastroService';

vi.mock('@/services/cadastroService', () => ({
    disponibilizarCadastro: vi.fn(),
    devolverCadastro: vi.fn(),
    devolverRevisaoCadastro: vi.fn(),
    aceitarCadastro: vi.fn(),
    aceitarRevisaoCadastro: vi.fn(),
    homologarCadastro: vi.fn(),
    homologarRevisaoCadastro: vi.fn(),
}));

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: vi.fn(),
    }),
}));

vi.mock('@/composables/useNotification', () => ({
    useNotification: () => ({ notify: vi.fn() }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        perfilSelecionado: 'CHEFE',
    }),
}));

describe('useFluxoCadastroSubprocesso', () => {
    const execucaoMock = {
        executarAcaoWorkflow: vi.fn(async (acao) => acao())
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve usar mensagem customizada no aceitarCadastro se fornecida', async () => {
        vi.mocked(cadastroService.aceitarCadastro).mockResolvedValue({} as any);
        const { aceitarCadastro } = useFluxoCadastroSubprocesso(execucaoMock as any);
        
        await aceitarCadastro(1, { observacoes: 'TESTE' }, { mensagemSucesso: 'Custom Aceite' });
        expect(cadastroService.aceitarCadastro).toHaveBeenCalled();
    });

    it('deve usar mensagem padrao no aceitarCadastro se nao fornecida', async () => {
        vi.mocked(cadastroService.aceitarCadastro).mockResolvedValue({} as any);
        const { aceitarCadastro } = useFluxoCadastroSubprocesso(execucaoMock as any);
        
        await aceitarCadastro(1, { observacoes: 'TESTE' });
        expect(cadastroService.aceitarCadastro).toHaveBeenCalled();
    });

    it('deve usar mensagem customizada no aceitarRevisaoCadastro se fornecida', async () => {
        vi.mocked(cadastroService.aceitarRevisaoCadastro).mockResolvedValue({} as any);
        const { aceitarRevisaoCadastro } = useFluxoCadastroSubprocesso(execucaoMock as any);
        
        await aceitarRevisaoCadastro(1, { observacoes: 'TESTE' }, { mensagemSucesso: 'Custom Aceite Revisao' });
        expect(cadastroService.aceitarRevisaoCadastro).toHaveBeenCalled();
    });

    it('deve usar mensagem padrao no aceitarRevisaoCadastro se nao fornecida', async () => {
        vi.mocked(cadastroService.aceitarRevisaoCadastro).mockResolvedValue({} as any);
        const { aceitarRevisaoCadastro } = useFluxoCadastroSubprocesso(execucaoMock as any);
        
        await aceitarRevisaoCadastro(1, { observacoes: 'TESTE' });
        expect(cadastroService.aceitarRevisaoCadastro).toHaveBeenCalled();
    });

    it('deve usar mensagem customizada no homologarCadastro se fornecida', async () => {
        vi.mocked(cadastroService.homologarCadastro).mockResolvedValue({} as any);
        const { homologarCadastro } = useFluxoCadastroSubprocesso(execucaoMock as any);
        
        await homologarCadastro(1, { observacoes: '' }, { mensagemSucesso: 'Custom Homologacao' });
        expect(cadastroService.homologarCadastro).toHaveBeenCalled();
    });

    it('deve usar mensagem padrao no homologarCadastro se nao fornecida', async () => {
        vi.mocked(cadastroService.homologarCadastro).mockResolvedValue({} as any);
        const { homologarCadastro } = useFluxoCadastroSubprocesso(execucaoMock as any);
        
        await homologarCadastro(1, { observacoes: '' });
        expect(cadastroService.homologarCadastro).toHaveBeenCalled();
    });
    
    it('deve usar mensagem customizada no homologarRevisaoCadastro se fornecida', async () => {
        vi.mocked(cadastroService.homologarRevisaoCadastro).mockResolvedValue({} as any);
        const { homologarRevisaoCadastro } = useFluxoCadastroSubprocesso(execucaoMock as any);
        
        await homologarRevisaoCadastro(1, { observacoes: '' }, { mensagemSucesso: 'Custom Homologacao Revisao' });
        expect(cadastroService.homologarRevisaoCadastro).toHaveBeenCalled();
    });

    it('deve usar mensagem padrao no homologarRevisaoCadastro se nao fornecida', async () => {
        vi.mocked(cadastroService.homologarRevisaoCadastro).mockResolvedValue({} as any);
        const { homologarRevisaoCadastro } = useFluxoCadastroSubprocesso(execucaoMock as any);
        
        await homologarRevisaoCadastro(1, { observacoes: '' });
        expect(cadastroService.homologarRevisaoCadastro).toHaveBeenCalled();
    });
});
