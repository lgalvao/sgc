import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoCodigo} from './helpers/helpers-processos.js';

test.describe('Issue #1381 - Race condition, Case sensitivity and Active Process validation', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_12';
    const timestamp = Date.now();
    const descProcMapeamento = `Mapeamento setup ${timestamp}`;
    const descProcRevisao = `Revisão teste ${timestamp}`;

    test('Deve acessar o cadastro editável da Revisão mesmo com sigla em minúsculo na URL', async ({page}) => {
        // 1. Login como Admin
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        
        // 2. Criar e iniciar processo de REVISÃO (Unidade já tem mapa na seed)
        await criarProcesso(page, {
            descricao: descProcRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1']
        });

        await page.getByTestId('tbl-processos').getByText(descProcRevisao).first().click();
        const codProcesso = await extrairProcessoCodigo(page);
        
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // 3. Login como Chefe da Unidade para ver o card de edição
        await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);

        // 4. Navegar para o subprocesso com sigla em minúsculo na URL
        const siglaMinuscula = UNIDADE_ALVO.toLowerCase();
        await page.goto(`/processo/${codProcesso}/${siglaMinuscula}`);

        // O card de atividades deve aparecer eventualmente (espera os detalhes serem carregados)
        const cardAtividades = page.locator('[data-testid^="card-subprocesso-atividades"]');
        await expect(cardAtividades.first()).toBeVisible({timeout: 10000});
        
        // Clica no card
        await cardAtividades.first().click();

        // Verifica se foi para a URL de cadastro (edição)
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${siglaMinuscula}/cadastro$`, 'i'));
        
        // Se chegar aqui e estiver visível o campo de nova atividade, a case sensitivity foi tratada
        await expect(page.getByTestId('inp-nova-atividade')).toBeVisible();
    });

    test('Deve mostrar erro ao tentar iniciar processo para unidade que já participa de processo ativo', async ({page}) => {
        // 1. Login como Admin
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        
        // 2. Criar um processo de Mapeamento para a unidade (sem iniciar ainda)
        await criarProcesso(page, {
            descricao: descProcMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1']
        });

        // 3. Criar um processo de Revisão para a mesma unidade (agora a unidade ainda não está ativa)
        await criarProcesso(page, {
            descricao: descProcRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1']
        });

        // 4. Iniciar o processo de Mapeamento
        await page.getByTestId('tbl-processos').getByText(descProcMapeamento).first().click();
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await expect(page).toHaveURL(/\/painel/);

        // 5. Tentar iniciar o processo de Revisão (agora a unidade está ativa no Mapeamento)
        await page.getByTestId('tbl-processos').getByText(descProcRevisao).first().click();
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // 6. Verificar que o erro é exibido e o redirecionamento NÃO ocorre
        const alerta = page.locator('.alert-danger');
        await expect(alerta).toBeVisible();
        await expect(alerta).toContainText('já participa de um processo ativo');
        
        // Verifica que ainda está na página de cadastro do processo (não redirecionou para o painel)
        await expect(page).toHaveURL(/\/processo\/cadastro/);
    });
});
