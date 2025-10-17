import { test } from '@playwright/test';
import {
    clicarUnidadeNaTabelaDetalhes,
    loginComoAdmin,
    loginComoGestor,
    navegarParaProcessoPorId,
    verificarAtividadeVisivel,
    verificarConhecimentoVisivel,
    verificarModoSomenteLeitura,
} from './helpers';
import * as processoService from '../../src/services/processoService';
import * as painelService from '../../src/services/painelService';
import * as atividadeService from '../../src/services/atividadeService';

test.describe('CDU-11: Visualizar cadastro de atividades (somente leitura)', () => {

    const nomeAtividade = `ATIVIDADE VISUALIZAR - ${Date.now()}`;
    const nomeConhecimento = `CONHECIMENTO VISUALIZAR - ${Date.now()}`;

    // Setup: Cria um processo, inicia e adiciona dados para serem visualizados.
    // Isso é executado uma vez antes de todos os testes neste describe.
    test.beforeAll(async () => {
        const processo = await processoService.criarProcesso({
            descricao: `PROCESSO VISUALIZAR TESTE - ${Date.now()}`,
            tipo: 'MAPEAMENTO',
            dataLimiteEtapa1: '2025-12-31T00:00:00',
            unidades: [2, 3] // Unidade 2 = STIC, Unidade 3 = SESEL
        });
        await processoService.iniciarProcesso(processo.codigo, 'MAPEAMENTO', [2, 3]);

        // Adiciona uma atividade e conhecimento diretamente via serviço para ter dados consistentes.
        const atividade = await atividadeService.criarAtividade({ descricao: nomeAtividade });
        await atividadeService.criarConhecimento(atividade.codigo, { descricao: nomeConhecimento });

        // A lógica para associar a atividade ao subprocesso/mapa é do backend,
        // aqui assumimos que ao listar, ela aparecerá.
    });

    test('ADMIN: deve visualizar cadastro em modo somente leitura', async ({ page }) => {
        await loginComoAdmin(page);
        const processos = await painelService.listarProcessos('ADMIN', 0, 0, 100); // Usar painelService
        const processo = processos.content.find(p => p.descricao.startsWith('PROCESSO VISUALIZAR TESTE'));

        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC'); // Unidade do Chefe/Servidor

        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoVisivel(page, nomeConhecimento, nomeAtividade);
        await verificarModoSomenteLeitura(page);
    });

    test('GESTOR: deve visualizar cadastro em modo somente leitura', async ({ page }) => {
        await loginComoGestor(page); // Gestor da SESEL/STIC
        const processos = await painelService.listarProcessos('GESTOR', 3, 0, 100); // Unidade 3 = SESEL
        const processo = processos.content.find(p => p.descricao.startsWith('PROCESSO VISUALIZAR TESTE'));

        await navegarParaProcessoPorId(page, processo.codigo);
        await clicarUnidadeNaTabelaDetalhes(page, 'STIC');

        await verificarAtividadeVisivel(page, nomeAtividade);
        await verificarConhecimentoVisivel(page, nomeConhecimento, nomeAtividade);
        await verificarModoSomenteLeitura(page);
    });
});