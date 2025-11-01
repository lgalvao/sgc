import { vueTest as test } from './support/vue-specific-setup';
import { expect } from '@playwright/test';
import { loginComoAdmin, criarProcesso, limparProcessos, submeterProcesso } from './helpers';
import { setupScenario } from './helpers/setup/api-setup';
import { getSubprocessosByProcessoId, getMapDetailsBySubprocessoId } from './helpers/verificacoes/api-verifications';

test.describe('CDU-05: Iniciar processo de revisão (com verificação de cópia de mapa)', () => {

    // O teste agora é auto-contido e não precisa de limpeza entre testes.
    // O beforeEach e afterEach foram removidos para simplificar.

    test('deve criar uma cópia exata do mapa vigente ao iniciar um processo de revisão', async ({ page }) => {
        await loginComoAdmin(page);
        await limparProcessos(page); // Limpeza inicial para garantir um estado limpo

        // 1. SETUP: Criar um processo de Mapeamento completo com um mapa vigente para a unidade 'SGP'
        const setupData = await setupScenario(page, 'processoComMapaFinalizado', {
            siglaUnidade: 'SGP',
            prefixo: 'CDU05'
        });
        const subprocessoOriginalId = setupData.subprocessoId;

        // 2. AÇÃO: Criar e iniciar um novo processo de REVISÃO para a mesma unidade
        const descricaoRevisao = `Processo de Revisão para SGP ${Date.now()}`;
        const processoRevisaoId = await criarProcesso(page, 'REVISAO', descricaoRevisao, ['SGP']);
        await submeterProcesso(page, processoRevisaoId);

        // 3. VERIFICAÇÃO:
        // a. Buscar os detalhes do mapa original e do novo mapa via API
        const subprocessosRevisao = await getSubprocessosByProcessoId(page, processoRevisaoId);
        expect(subprocessosRevisao).toHaveLength(1);
        const subprocessoRevisaoId = subprocessosRevisao[0].codigo;

        const mapaOriginal = await getMapDetailsBySubprocessoId(page, subprocessoOriginalId);
        const mapaCopiado = await getMapDetailsBySubprocessoId(page, subprocessoRevisaoId);

        // b. Comparar os conteúdos, ignorando os IDs
        expect(mapaCopiado.codigo).not.toEqual(mapaOriginal.codigo); // O ID do mapa deve ser novo

        // c. Normalizar os dados para comparação, removendo IDs que devem mudar
        const normalizarMapa = (mapa) => {
            mapa.codigo = null;
            mapa.competencias.forEach(c => {
                c.codigo = null;
                c.atividades.forEach(a => {
                    a.codigo = null;
                    a.conhecimentos.forEach(con => con.codigo = null);
                    // A ordem pode não ser garantida, então ordenamos para a comparação
                    a.conhecimentos.sort((c1, c2) => c1.descricao.localeCompare(c2.descricao));
                });
                c.atividades.sort((a1, a2) => a1.descricao.localeCompare(a2.descricao));
            });
            mapa.competencias.sort((c1, c2) => c1.descricao.localeCompare(c2.descricao));
            return mapa;
        };

        const mapaOriginalNormalizado = normalizarMapa(mapaOriginal);
        const mapaCopiadoNormalizado = normalizarMapa(mapaCopiado);

        // d. Realizar a asserção profunda
        expect(mapaCopiadoNormalizado).toEqual(mapaOriginalNormalizado);

        // e. Limpeza final
        await loginComoAdmin(page);
        await limparProcessos(page);
    });
});
