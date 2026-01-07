import {beforeEach, describe, expect, it, vi} from 'vitest';
import * as subprocessoService from '../subprocessoService';
import apiClient from '@/axios-setup';
import * as mapasMapper from '@/mappers/mapas';
import * as atividadesMapper from '@/mappers/atividades';

vi.mock('@/axios-setup');
vi.mock('@/mappers/mapas');
vi.mock('@/mappers/atividades');

describe('subprocessoService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('importarAtividades deve fazer requisição POST', async () => {
        const codSubprocessoDestino = 1;
        const codSubprocessoOrigem = 2;
        await subprocessoService.importarAtividades(codSubprocessoDestino, codSubprocessoOrigem);

        expect(apiClient.post).toHaveBeenCalledWith(
            `/subprocessos/${codSubprocessoDestino}/importar-atividades`,
            { codSubprocessoOrigem }
        );
    });

    it('listarAtividades deve fazer requisição GET e mapear dados', async () => {
        const codSubprocesso = 1;
        const mockData = [{ id: 1 }];
        const mockMapped = { codigo: 1 } as any;

        vi.mocked(apiClient.get).mockResolvedValue({ data: mockData });
        vi.mocked(atividadesMapper.mapAtividadeVisualizacaoToModel).mockReturnValue(mockMapped);

        const result = await subprocessoService.listarAtividades(codSubprocesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/atividades`);
        expect(atividadesMapper.mapAtividadeVisualizacaoToModel).toHaveBeenCalledTimes(mockData.length);
        expect(result).toEqual([mockMapped]);
    });

    it('obterPermissoes deve fazer requisição GET', async () => {
        const codSubprocesso = 1;
        const mockData = { podeEditar: true };
        vi.mocked(apiClient.get).mockResolvedValue({ data: mockData });

        const result = await subprocessoService.obterPermissoes(codSubprocesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/permissoes`);
        expect(result).toEqual(mockData);
    });

    it('validarCadastro deve fazer requisição GET', async () => {
        const codSubprocesso = 1;
        const mockData = { valido: true };
        vi.mocked(apiClient.get).mockResolvedValue({ data: mockData });

        const result = await subprocessoService.validarCadastro(codSubprocesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/validar-cadastro`);
        expect(result).toEqual(mockData);
    });

    it('obterStatus deve fazer requisição GET', async () => {
        const codSubprocesso = 1;
        const mockData = { status: 'OK' };
        vi.mocked(apiClient.get).mockResolvedValue({ data: mockData });

        const result = await subprocessoService.obterStatus(codSubprocesso);

        expect(apiClient.get).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/status`);
        expect(result).toEqual(mockData);
    });

    it('buscarSubprocessoDetalhe deve fazer requisição GET com query params', async () => {
        const codSubprocesso = 1;
        const perfil = 'ADMIN';
        const unidadeCodigo = 2;
        const mockData = { detalhe: true };
        vi.mocked(apiClient.get).mockResolvedValue({ data: mockData });

        const result = await subprocessoService.buscarSubprocessoDetalhe(codSubprocesso, perfil, unidadeCodigo);

        expect(apiClient.get).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}`, {
            params: { perfil, unidadeUsuario: unidadeCodigo }
        });
        expect(result).toEqual(mockData);
    });

    it('buscarContextoEdicao deve fazer requisição GET com query params', async () => {
        const codSubprocesso = 1;
        const perfil = 'ADMIN';
        const unidadeCodigo = 2;
        const mockData = { contexto: true };
        vi.mocked(apiClient.get).mockResolvedValue({ data: mockData });

        const result = await subprocessoService.buscarContextoEdicao(codSubprocesso, perfil, unidadeCodigo);

        expect(apiClient.get).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/contexto-edicao`, {
            params: { perfil, unidadeUsuario: unidadeCodigo }
        });
        expect(result).toEqual(mockData);
    });

    it('buscarSubprocessoPorProcessoEUnidade deve fazer requisição GET com query params', async () => {
        const codProcesso = 1;
        const siglaUnidade = 'U1';
        const mockData = 123;
        vi.mocked(apiClient.get).mockResolvedValue({ data: mockData });

        const result = await subprocessoService.buscarSubprocessoPorProcessoEUnidade(codProcesso, siglaUnidade);

        expect(apiClient.get).toHaveBeenCalledWith("/subprocessos/buscar", {
            params: { codProcesso, siglaUnidade }
        });
        expect(result).toEqual(mockData);
    });

    it('adicionarCompetencia deve fazer requisição POST e mapear resposta', async () => {
        const codSubprocesso = 1;
        const competencia = { descricao: 'Comp', atividadesAssociadas: [1], codigo: 0 } as any;
        const mockData = { mapa: true };
        const mockMapped = { mapped: true } as any;

        vi.mocked(apiClient.post).mockResolvedValue({ data: mockData });
        vi.mocked(mapasMapper.mapMapaCompletoDtoToModel).mockReturnValue(mockMapped);

        const result = await subprocessoService.adicionarCompetencia(codSubprocesso, competencia);

        expect(apiClient.post).toHaveBeenCalledWith(
            `/subprocessos/${codSubprocesso}/competencias`,
            { descricao: competencia.descricao, atividadesIds: competencia.atividadesAssociadas }
        );
        expect(mapasMapper.mapMapaCompletoDtoToModel).toHaveBeenCalledWith(mockData);
        expect(result).toEqual(mockMapped);
    });

    it('atualizarCompetencia deve fazer requisição POST e mapear resposta', async () => {
        const codSubprocesso = 1;
        const competencia = { descricao: 'Comp', atividadesAssociadas: [1], codigo: 10 } as any;
        const mockData = { mapa: true };
        const mockMapped = { mapped: true } as any;

        vi.mocked(apiClient.post).mockResolvedValue({ data: mockData });
        vi.mocked(mapasMapper.mapMapaCompletoDtoToModel).mockReturnValue(mockMapped);

        const result = await subprocessoService.atualizarCompetencia(codSubprocesso, competencia);

        expect(apiClient.post).toHaveBeenCalledWith(
            `/subprocessos/${codSubprocesso}/competencias/${competencia.codigo}/atualizar`,
            { descricao: competencia.descricao, atividadesIds: competencia.atividadesAssociadas }
        );
        expect(mapasMapper.mapMapaCompletoDtoToModel).toHaveBeenCalledWith(mockData);
        expect(result).toEqual(mockMapped);
    });

    it('removerCompetencia deve fazer requisição POST e mapear resposta', async () => {
        const codSubprocesso = 1;
        const codCompetencia = 10;
        const mockData = { mapa: true };
        const mockMapped = { mapped: true } as any;

        vi.mocked(apiClient.post).mockResolvedValue({ data: mockData });
        vi.mocked(mapasMapper.mapMapaCompletoDtoToModel).mockReturnValue(mockMapped);

        const result = await subprocessoService.removerCompetencia(codSubprocesso, codCompetencia);

        expect(apiClient.post).toHaveBeenCalledWith(
            `/subprocessos/${codSubprocesso}/competencias/${codCompetencia}/remover`
        );
        expect(mapasMapper.mapMapaCompletoDtoToModel).toHaveBeenCalledWith(mockData);
        expect(result).toEqual(mockMapped);
    });

    it('aceitarCadastroEmBloco deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const payload = { unidadeCodigos: [1], dataLimite: '2024-12-31' };
        await subprocessoService.aceitarCadastroEmBloco(codSubprocesso, payload);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/aceitar-cadastro-bloco`, payload);
    });

    it('homologarCadastroEmBloco deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const payload = { unidadeCodigos: [1], dataLimite: '2024-12-31' };
        await subprocessoService.homologarCadastroEmBloco(codSubprocesso, payload);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/homologar-cadastro-bloco`, payload);
    });

    it('aceitarValidacaoEmBloco deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const payload = { unidadeCodigos: [1], dataLimite: '2024-12-31' };
        await subprocessoService.aceitarValidacaoEmBloco(codSubprocesso, payload);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/aceitar-validacao-bloco`, payload);
    });

    it('homologarValidacaoEmBloco deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const payload = { unidadeCodigos: [1], dataLimite: '2024-12-31' };
        await subprocessoService.homologarValidacaoEmBloco(codSubprocesso, payload);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/homologar-validacao-bloco`, payload);
    });

    it('disponibilizarMapaEmBloco deve fazer requisição POST', async () => {
        const codSubprocesso = 1;
        const payload = { unidadeCodigos: [1], dataLimite: '2024-12-31' };
        await subprocessoService.disponibilizarMapaEmBloco(codSubprocesso, payload);
        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${codSubprocesso}/disponibilizar-mapa-bloco`, payload);
    });
});
