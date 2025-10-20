import {afterEach, beforeAll, beforeEach, describe, expect, it, vi, Mock} from 'vitest'
import * as service from '../processoService'
import apiClient from '@/axios-setup'

const getSpy = vi.spyOn(apiClient, 'get')
const postSpy = vi.spyOn(apiClient, 'post')
const putSpy = vi.spyOn(apiClient, 'put')
const deleteSpy = vi.spyOn(apiClient, 'delete')

import {AtualizarProcessoRequest, CriarProcessoRequest, TipoProcesso} from '@/types/tipos';

vi.mock('@/axios-setup', () => {
    return {
        default: {
            get: vi.fn(),
            post: vi.fn(),
            put: vi.fn(),
            delete: vi.fn(),
        },
    };
});

describe('processoService', () => {
    let mockedMappers: typeof import('@/mappers/processos'); // Declarar a variável aqui

    beforeAll(() => {
        vi.doMock('@/mappers/processos', () => ({
            mapProcessoDtoToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
            mapProcessoDetalheDtoToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
        }));
    });

    beforeEach(async () => {
        mockedMappers = await import('@/mappers/processos');
        vi.spyOn(mockedMappers, 'mapProcessoDtoToFrontend') as Mock;
        vi.spyOn(mockedMappers, 'mapProcessoDetalheDtoToFrontend') as Mock;
    });

    afterEach(() => {
        vi.clearAllMocks()
    })





    it('iniciarProcesso should post with correct params', async () => {
            postSpy.mockResolvedValue({})
            await service.iniciarProcesso(1, TipoProcesso.REVISAO, [10, 20])
            expect(postSpy).toHaveBeenCalledWith('/processos/1/iniciar?tipo=REVISAO', [10, 20]);})

    it('finalizarProcesso should post to the correct endpoint', async () => {
            postSpy.mockResolvedValue({})
            await service.finalizarProcesso(1)
            expect(postSpy).toHaveBeenCalledWith('/processos/1/finalizar');})





    it('excluirProcesso should call delete', async () => {
            deleteSpy.mockResolvedValue({})
            await service.excluirProcesso(1)
            expect(deleteSpy).toHaveBeenCalledWith('/processos/1');})



    // Error handling
    it('criarProcesso should throw error on failure', async () => {
            const request: CriarProcessoRequest = { descricao: 'teste', tipo: TipoProcesso.MAPEAMENTO, dataLimiteEtapa1: '2025-12-31', unidades: [1] }
            postSpy.mockRejectedValue(new Error('Failed'));        await expect(service.criarProcesso(request)).rejects.toThrow()
    })
})