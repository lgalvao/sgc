import {describe, expect, it, vi} from 'vitest';
import path from 'path';
import {MatchersV3, PactV3} from '@pact-foundation/pact';
import {obterProcessoPorCodigo} from '../processoService';
import apiClient from '../../axios-setup';

// Mock the store before imports to avoid Pinia errors
vi.mock('@/stores/feedback', () => ({
  useFeedbackStore: () => ({
    show: vi.fn(),
  }),
}));

// Mock localStorage
vi.stubGlobal('localStorage', {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn(),
});

// Create a new PactV3 instance
const provider = new PactV3({
  consumer: 'Frontend',
  provider: 'Backend',
  dir: path.resolve(process.cwd(), 'pact/pacts'), // Output directory
});

describe('ProcessoService Pact', () => {
  it('returns a process', async () => {
    // Define the interaction
    provider.addInteraction({
      states: [{ description: 'processo 1 existe' }],
      uponReceiving: 'a request for process 1',
      withRequest: {
        method: 'GET',
        path: '/api/processos/1',
      },
      willRespondWith: {
        status: 200,
        body: {
          codigo: 1,
          descricao: MatchersV3.string('Processo de Teste'),
          situacao: MatchersV3.string('CRIADO'),
          tipo: MatchersV3.string('MAPEAMENTO'),
          dataCriacao: MatchersV3.string('2023-01-01T00:00:00'),
          dataLimite: MatchersV3.string('2023-12-31T23:59:59'),
        },
      },
    });

    await provider.executeTest(async (mockServer) => {
      // Configure API client to use the mock server
      // mockServer.url does not include trailing slash
      apiClient.defaults.baseURL = mockServer.url + '/api';

      const response = await obterProcessoPorCodigo(1);

      expect(response).toEqual(expect.objectContaining({
        codigo: 1,
        descricao: 'Processo de Teste',
        situacao: 'CRIADO',
        tipo: 'MAPEAMENTO',
      }));
    });
  });
});
