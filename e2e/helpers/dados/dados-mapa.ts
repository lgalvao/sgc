import { APIRequestContext } from '@playwright/test';

export async function criarMapaVigenteParaUnidade(request: APIRequestContext, unidadeId: number): Promise<void> {
    const response = await request.post('/api/e2e/data/mapa', {
        data: {
            unidadeId: unidadeId,
        },
    });
    if (response.status() !== 200) {
        throw new Error(`Falha ao criar mapa vigente para unidade ${unidadeId}. Status: ${response.status()}`);
    }
}
