import {expect, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import * as AtividadeHelpers from './helpers/helpers-atividades';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';
    const CHEFE_UNIDADE = USUARIOS.CHEFE_ASSESSORIA_11.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_ASSESSORIA_11.senha;

    test.beforeAll(async ({request}) => {
        const response = await request.post('http://localhost:10000/e2e/reset-database');
        expect(response.ok()).toBeTruthy();
    });

    test('Cenário 1: Processo de Mapeamento (Fluxo Completo + Importação)', async ({page}) => {
        const timestamp = Date.now();
        const descricaoProcesso = `Processo CDU-08 Map ${timestamp}`;

        await test.step('1. Setup: Criar Processo de Mapeamento', async () => {
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });
            await page.getByTestId('btn-logout').click();
        });

        await test.step('2. Acessar tela de Atividades', async () => {
            await login(page, CHEFE_UNIDADE, SENHA_CHEFE);
            await page.waitForLoadState('networkidle');

            // Clica no processo no painel
            await page.getByText(descricaoProcesso).click();

            // CHEFE tem acesso direto apenas à sua unidade, então vai direto para Detalhes do subprocesso
            await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

            // Agora navega para atividades
            await AtividadeHelpers.navegarParaAtividades(page);
        });

        const atividade1 = `Atividade 1 ${timestamp}`;
        await test.step('3. Adicionar Atividade e Conhecimento', async () => {
            await AtividadeHelpers.adicionarAtividade(page, atividade1);

            // Verifica mudança de status
            await AtividadeHelpers.verificarSituacaoSubprocesso(page, 'Cadastro em andamento');

            const conhecimento1 = `Conhecimento 1 ${timestamp}`;
            await AtividadeHelpers.adicionarConhecimento(page, atividade1, conhecimento1);
        });

        await test.step('4. Editar e Remover', async () => {
            const atividade1Editada = `${atividade1} EDITADA`;
            await AtividadeHelpers.editarAtividade(page, atividade1, atividade1Editada);

            const conhecimento1 = `Conhecimento 1 ${timestamp}`;
            const conhecimento1Editado = `${conhecimento1} EDITADO`;

            await AtividadeHelpers.editarConhecimento(page, atividade1Editada, conhecimento1, conhecimento1Editado);
            await AtividadeHelpers.removerConhecimento(page, atividade1Editada, conhecimento1Editado);
            await AtividadeHelpers.removerAtividade(page, atividade1Editada);
        });

        await test.step('6. Verificar Ausência de Botão de Impacto', async () => {
            await AtividadeHelpers.verificarBotaoImpacto(page, false);
        });

        await test.step('7. Disponibilizar', async () => {
            const ativFinal = `Atividade Final ${timestamp}`;
            await AtividadeHelpers.adicionarAtividade(page, ativFinal);
            await AtividadeHelpers.adicionarConhecimento(page, ativFinal, 'Conhecimento Final');

            await AtividadeHelpers.disponibilizarCadastro(page);
            await expect(page).toHaveURL(/\/painel/);
        });
    });

    test('Cenário 2: Processo de Revisão (Botão Impacto)', async ({page}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-08 Rev ${timestamp}`;
        const UNIDADE_REVISAO = 'ASSESSORIA_12';
        const CHEFE_REVISAO = USUARIOS.CHEFE_ASSESSORIA_12.titulo;
        const SENHA_REVISAO = USUARIOS.CHEFE_ASSESSORIA_12.senha;

        await test.step('Setup: Criar Processo de Revisão', async () => {
            await page.goto('/login');
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await criarProcesso(page, {
                descricao,
                tipo: 'REVISAO',
                diasLimite: 30,
                unidade: UNIDADE_REVISAO,
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });
            await page.getByTestId('btn-logout').click();
        });

        await test.step('Verificar Botão Impacto', async () => {
            await login(page, CHEFE_REVISAO, SENHA_REVISAO);
            await page.waitForLoadState('networkidle');
            await page.getByText(descricao).click();
            await AtividadeHelpers.navegarParaAtividades(page);

            // Adicionar uma atividade para garantir que o status mude para EM_ANDAMENTO e o botão apareça
            await AtividadeHelpers.adicionarAtividade(page, 'Atividade Trigger');
            await AtividadeHelpers.verificarBotaoImpacto(page, true);
            await AtividadeHelpers.abrirModalImpacto(page);
            await AtividadeHelpers.fecharModalImpacto(page);
        });
    });
});
