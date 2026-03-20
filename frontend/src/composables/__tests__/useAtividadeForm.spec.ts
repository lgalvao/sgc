import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useAtividadeForm} from '../useAtividadeForm';
import * as atividadeService from '@/services/atividadeService';
import type {CriarAtividadeRequest} from '@/types/tipos';

vi.mock('@/services/atividadeService', () => ({
  criarAtividade: vi.fn(),
}));

describe('useAtividadeForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('deve inicializar com valores padrão', () => {
    const { novaAtividade, loadingAdicionar } = useAtividadeForm();
    expect(novaAtividade.value).toBe('');
    expect(loadingAdicionar.value).toBe(false);
  });

  it('não deve fazer nada se a novaAtividade estiver vazia', async () => {
    const { novaAtividade, loadingAdicionar, adicionarAtividade } = useAtividadeForm();
    novaAtividade.value = '   ';
    
    const result = await adicionarAtividade(1, 2);
    
    expect(result).toBeNull();
    expect(atividadeService.criarAtividade).not.toHaveBeenCalled();
    expect(loadingAdicionar.value).toBe(false);
  });

  it('deve adicionar atividade com sucesso, resetar input e controlar loading', async () => {
    const { novaAtividade, loadingAdicionar, adicionarAtividade } = useAtividadeForm();
    novaAtividade.value = 'Nova atividade de teste';
    
    const mockResponse = { id: 10, descricao: 'Nova atividade de teste' };
    vi.mocked(atividadeService.criarAtividade).mockResolvedValue(mockResponse as any);
    
    const promise = adicionarAtividade(1, 2);
    expect(loadingAdicionar.value).toBe(true);
    
    const result = await promise;
    
    expect(atividadeService.criarAtividade).toHaveBeenCalledWith(
      { descricao: 'Nova atividade de teste' } as CriarAtividadeRequest,
      2
    );
    expect(result).toEqual(mockResponse);
    expect(novaAtividade.value).toBe('');
    expect(loadingAdicionar.value).toBe(false);
  });

  it('deve lidar com erro ao adicionar e restaurar loading', async () => {
    const { novaAtividade, loadingAdicionar, adicionarAtividade } = useAtividadeForm();
    novaAtividade.value = 'Atividade com erro';
    
    const erro = new Error('Erro na API');
    vi.mocked(atividadeService.criarAtividade).mockRejectedValue(erro);
    
    await expect(adicionarAtividade(1, 2)).rejects.toThrow('Erro na API');
    
    expect(loadingAdicionar.value).toBe(false);
    expect(novaAtividade.value).toBe('Atividade com erro'); // Não deve limpar se falhar
  });
});
