import { vueTest as test } from './support/vue-specific-setup';
import {
    loginComoAdmin,
    criarProcesso,
    limparProcessos,
    submeterProcesso,
    setupScenario,
    verificarCopiaMapa,
} from './helpers';

test.describe('CDU-05: Iniciar processo de revisão (com verificação de cópia de mapa)', () => {
    test('deve criar uma cópia exata do mapa vigente ao iniciar um processo de revisão', async ({ page }) => {
        await loginComoAdmin(page);
        await limparProcessos(page);

        const setupData = await setupScenario(page, 'processoComMapaFinalizado', {
            siglaUnidade: 'SGP',
            prefixo: 'CDU05'
        });
        const subprocessoOriginalId = setupData.subprocessoId;

        const descricaoRevisao = `Processo de Revisão para SGP ${Date.now()}`;
        const processoRevisaoId = await criarProcesso(page, 'REVISAO', descricaoRevisao, ['SGP']);
        await submeterProcesso(page, processoRevisaoId);

        await verificarCopiaMapa(page, processoRevisaoId, subprocessoOriginalId);

        await loginComoAdmin(page);
        await limparProcessos(page);
    });
});
