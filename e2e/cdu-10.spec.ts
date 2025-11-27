import { test, expect } from '@playwright/test';
import { loginComoAdmin, loginComo } from './helpers/auth';
import { USUARIOS } from './helpers/dados/constantes';

/**
 * CDU-10: Disponibilizar revisão do cadastro
 * 
 * REWRITTEN: Uses API for setup and direct navigation.
 */
test.describe('CDU-10: Disponibilizar revisão do cadastro', () => {

    test('deve validar atividades incompletas e permitir disponibilizar após correção', async ({ page }) => {
        const timestamp = Date.now();
        const nomeProcesso = `Processo Revisao ${timestamp}`;
        const nomeAtividade = `Atividade Incompleta ${timestamp}`;
        const nomeCompetencia = `Competencia Teste ${timestamp}`;
        const nomeConhecimento = `Conhecimento Teste ${timestamp}`;

        // Authenticate as Chefe Teste (Titular SEDIA - 333333333333) for Setup
        await loginComo(page, USUARIOS.CHEFE_TESTE);

        // Get Token from localStorage
        const jwtToken = await page.evaluate(() => localStorage.getItem('jwtToken'));
        if (!jwtToken) throw new Error('jwtToken not found in localStorage after login');

        // 1. Setup via API (using page.evaluate with explicit Authorization header)
        const setupData = await page.evaluate(async ({ nomeProcesso, nomeAtividade, nomeCompetencia, jwtToken }) => {
            const headers = {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwtToken}`
            };

            // Create Process
            const procRes = await fetch('/api/processos', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    descricao: nomeProcesso,
                    tipo: 'REVISAO',
                    dataLimiteEtapa1: '2025-12-31T00:00:00',
                    dataLimiteEtapa2: '2026-01-31T00:00:00',
                    unidades: [9] // SEDIA
                })
            });
            if (!procRes.ok) throw new Error(`Erro criar processo: ${procRes.status} - ${await procRes.text()}`);
            const processo = await procRes.json();

            // Initiate Process
            const initRes = await fetch(`/api/processos/${processo.codigo}/iniciar`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    tipo: 'REVISAO',
                    unidades: [9]
                })
            });
            if (!initRes.ok) throw new Error(`Erro iniciar processo: ${initRes.status} - ${await initRes.text()}`);

            // Get Subprocess
            const subRes = await fetch(`/api/subprocessos/buscar?codProcesso=${processo.codigo}&siglaUnidade=SEDIA`, {
                headers: headers
            });
            if (!subRes.ok) throw new Error(`Erro buscar subprocesso: ${subRes.status} - ${await subRes.text()}`);
            const subprocesso = await subRes.json();

            // Create Activity
            // Must provide mapaCodigo (from subprocesso.codMapa)
            const ativRes = await fetch('/api/atividades', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    descricao: nomeAtividade,
                    mapaCodigo: subprocesso.codMapa
                })
            });
            if (!ativRes.ok) throw new Error(`Erro criar atividade: ${ativRes.status} - ${await ativRes.text()}`);
            const atividade = await ativRes.json();

            // Add Competence
            const compRes = await fetch(`/api/subprocessos/${subprocesso.codigo}/competencias`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    descricao: nomeCompetencia,
                    atividadesIds: [atividade.codigo]
                })
            });
            if (!compRes.ok) throw new Error(`Erro criar competencia: ${compRes.status} - ${await compRes.text()}`);

            return { processo, subprocesso, atividade };
        }, { nomeProcesso, nomeAtividade, nomeCompetencia, jwtToken });

        const { processo, atividade } = setupData;

        // 2. UI: Login as CHEFE (User 333333333333) and Navigate
        await loginComo(page, USUARIOS.CHEFE_TESTE);
        await page.goto(`/processo/${processo.codigo}/SEDIA`);

        // 3. UI: Go to "Atividades e conhecimentos"
        await page.getByTestId('card-atividades').click();
        await expect(page.getByRole('heading', { name: 'Cadastro de atividades e conhecimentos' })).toBeVisible();

        // 4. UI: Try to Disponibilizar (Should fail)
        await page.getByRole('button', { name: 'Disponibilizar' }).click();

        // Assert Alert
        await expect(page.getByRole('alert')).toContainText('Atividades Incompletas');
        await expect(page.getByText(`A atividade "${nomeAtividade}" não possui conhecimentos associados.`)).toBeVisible();

        // 5. UI: Add Knowledge via API (using page.evaluate with explicit Token)
        // Note: We need a valid token for this call.
        // We are logged in as CHEFE now. So localStorage should have CHEFE's token.
        // Let's get the current token from localStorage inside page.evaluate.
        await page.evaluate(async ({ codAtividade, nomeConhecimento }) => {
            const currentToken = localStorage.getItem('jwtToken');
            if (!currentToken) throw new Error('jwtToken not found for Chefe');

            const res = await fetch(`/api/atividades/${codAtividade}/conhecimentos`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${currentToken}`
                },
                body: JSON.stringify({ descricao: nomeConhecimento })
            });
            if (!res.ok) throw new Error(`Erro criar conhecimento: ${res.status} - ${await res.text()}`);
        }, { codAtividade: atividade.codigo, nomeConhecimento });

        // Reload page to reflect API change
        await page.reload();
        await expect(page.getByRole('heading', { name: 'Cadastro de atividades e conhecimentos' })).toBeVisible();

        // 6. UI: Disponibilizar again (Should success)
        await page.getByRole('button', { name: 'Disponibilizar' }).click();

        // Confirm dialog
        await expect(page.getByText('Confirma a finalização da revisão')).toBeVisible();
        await page.getByRole('button', { name: 'Confirmar' }).click();

        // 7. Assert Success
        await expect(page.getByText('Revisão do cadastro de atividades disponibilizada')).toBeVisible();
        await expect(page).toHaveURL('/painel');
    });

    test('deve exibir histórico de análise após devolução', async ({ page }) => {
        const timestamp = Date.now();
        const nomeProcesso = `Processo Devolucao ${timestamp}`;
        const nomeAtividade = `Atividade Devolucao ${timestamp}`;
        const nomeCompetencia = `Competencia Devolucao ${timestamp}`;
        const nomeConhecimento = `Conhecimento Devolucao ${timestamp}`;
        const motivoDevolucao = `Motivo Devolucao ${timestamp}`;

        // Authenticate as Chefe Teste (Titular SEDIA) for Setup
        await loginComo(page, USUARIOS.CHEFE_TESTE);

        // Get Token from localStorage
        const jwtToken = await page.evaluate(() => localStorage.getItem('jwtToken'));
        if (!jwtToken) throw new Error('jwtToken not found in localStorage after login');

        // 1. Setup via API (using page.evaluate)
        const setupData = await page.evaluate(async ({ nomeProcesso, nomeAtividade, nomeCompetencia, nomeConhecimento, motivoDevolucao, jwtToken }) => {
            const headers = {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwtToken}`
            };

            // Create Process
            const procRes = await fetch('/api/processos', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    descricao: nomeProcesso,
                    tipo: 'REVISAO',
                    dataLimiteEtapa1: '2025-12-31T00:00:00',
                    dataLimiteEtapa2: '2026-01-31T00:00:00',
                    unidades: [9] // SEDIA
                })
            });
            if (!procRes.ok) throw new Error(`Erro criar processo: ${procRes.status} - ${await procRes.text()}`);
            const processo = await procRes.json();

            // Initiate
            const initRes = await fetch(`/api/processos/${processo.codigo}/iniciar`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ tipo: 'REVISAO', unidades: [9] })
            });
            if (!initRes.ok) throw new Error(`Erro iniciar processo: ${initRes.status} - ${await initRes.text()}`);

            // Get Subprocess
            const subRes = await fetch(`/api/subprocessos/buscar?codProcesso=${processo.codigo}&siglaUnidade=SEDIA`, {
                headers: headers
            });
            if (!subRes.ok) throw new Error(`Erro buscar subprocesso: ${subRes.status} - ${await subRes.text()}`);
            const subprocesso = await subRes.json();

            // Create Activity
            const ativRes = await fetch('/api/atividades', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    descricao: nomeAtividade,
                    mapaCodigo: subprocesso.codMapa
                })
            });
            if (!ativRes.ok) throw new Error(`Erro criar atividade: ${ativRes.status} - ${await ativRes.text()}`);
            const atividade = await ativRes.json();

            // Add Knowledge
            const knowRes = await fetch(`/api/atividades/${atividade.codigo}/conhecimentos`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ descricao: nomeConhecimento })
            });
            if (!knowRes.ok) throw new Error(`Erro criar conhecimento: ${knowRes.status} - ${await knowRes.text()}`);

            // Add Competence
            const compRes = await fetch(`/api/subprocessos/${subprocesso.codigo}/competencias`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ descricao: nomeCompetencia, atividadesIds: [atividade.codigo] })
            });
            if (!compRes.ok) throw new Error(`Erro criar competencia: ${compRes.status} - ${await compRes.text()}`);

            // Disponibilizar
            const dispRes = await fetch(`/api/subprocessos/${subprocesso.codigo}/disponibilizar`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ declaracaoVeracidade: true })
            });
            if (!dispRes.ok) throw new Error(`Erro disponibilizar: ${dispRes.status} - ${await dispRes.text()}`);

            // Devolver (Create Analysis)
            const analiseRes = await fetch(`/api/subprocessos/${subprocesso.codigo}/analises-cadastro`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    motivo: motivoDevolucao,
                    siglaUnidade: 'TRE-PE',
                    tituloUsuario: '123456789012',
                    observacoes: 'Devolvido para ajustes'
                })
            });
            if (!analiseRes.ok) throw new Error(`Erro criar analise: ${analiseRes.status} - ${await analiseRes.text()}`);

            return { processo };
        }, { nomeProcesso, nomeAtividade, nomeCompetencia, nomeConhecimento, motivoDevolucao, jwtToken });

        const { processo } = setupData;

        // 2. UI: Login as CHEFE and Navigate
        await loginComo(page, USUARIOS.CHEFE_TESTE);
        await page.goto(`/processo/${processo.codigo}/SEDIA`);
        await page.getByTestId('card-atividades').click();

        // 3. UI: Check History Button
        await expect(page.getByRole('button', { name: 'Histórico de análise' })).toBeVisible();
        await page.getByRole('button', { name: 'Histórico de análise' }).click();

        // 4. UI: Check Modal Content
        await expect(page.getByText(motivoDevolucao)).toBeVisible();
    });
});
