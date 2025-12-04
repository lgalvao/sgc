import {expect, Page, test} from '@playwright/test';
import {login, USUARIOS} from './helpers/auth';
import {criarProcesso, calcularDataLimite} from './helpers/processo-helpers';

async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

async function verificarPaginaSubprocesso(page: Page) {
    await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_21$/);
}

test.describe('Visual Regression - CDU-05', () => {
    test.describe.configure({ mode: 'serial' });

    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';
    const SENHA_CHEFE = 'senha';

    const timestamp = Date.now();
    const descProcMapeamento = `Mapeamento Visual ${timestamp}`;

    test('Ciclo Mapeamento Completo com Snapshots', async ({page}) => {
        // 1. Login Admin & Dashboard
        await page.goto('/login');
        await expect(page).toHaveScreenshot('01-login-page.png');

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarPaginaPainel(page);
        
        // Snapshot do Painel Inicial (pode ter outros processos, então cuidado com o diff)
        // Idealmente mascaramos a tabela se ela for muito volátil, ou apenas o conteúdo dinâmico.
        // Como é um ambiente de teste, assumimos controle relativo.
        await expect(page).toHaveScreenshot('02-dashboard-admin.png', {
            fullPage: true
        });

        // 2. Criar Processo (Manual steps to allow snapshot of empty form)
        await page.getByTestId('btn-painel-criar-processo').click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page).toHaveScreenshot('03-criar-processo-form.png');

        // Fill form manually
        await page.getByTestId('inp-processo-descricao').fill(descProcMapeamento);
        await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');
        await page.getByTestId('inp-processo-data-limite').fill(calcularDataLimite(30));

        // Aguardar que as unidades sejam carregadas antes de interagir com a árvore
        await expect(page.getByText('Carregando unidades...')).toBeHidden();

        const expandir = ['SECRETARIA_2'];
        for (const sigla of expandir) {
            await page.getByTestId(`btn-arvore-expand-${sigla}`).click();
        }

        // Usar getByTestId ao invés de getByRole para respeitar disabled
        await page.getByTestId(`chk-arvore-unidade-${UNIDADE_ALVO}`).check();
        
        // Salvar (sem iniciar ainda, pois o teste original fazia save -> click na lista -> iniciar)
        // O teste original fazia: criarProcesso (que salva) -> verificar -> clicar na linha -> iniciar.
        // Vamos manter a consistência.
        await page.getByTestId('btn-processo-salvar').click();
        await expect(page).toHaveURL(/\/painel/);

        // Snapshot do Painel com o novo processo
        // Mascaramos o texto da descrição pois contém timestamp
        await expect(page).toHaveScreenshot('04-dashboard-com-processo.png', {
            fullPage: true,
            mask: [page.getByText(descProcMapeamento)]
        });

        // 3. Iniciar Processo
        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcMapeamento)});
        await linhaProcesso.click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await page.getByTestId('btn-processo-iniciar').click();
        const iniciarModal = page.getByRole('dialog');
        await expect(iniciarModal).toBeVisible();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // 4. Chefe Login & Subprocesso
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        
        await page.getByText(descProcMapeamento).click();
        
        // Se cair na tela de processo (lista de unidades), clicar na unidade
        if (page.url().match(/\/processo\/\d+$/)) {
             await page.getByRole('row', {name: 'Assessoria 21'}).click();
        }
        await verificarPaginaSubprocesso(page);

        // Snapshot Subprocesso Inicial
        await expect(page).toHaveScreenshot('05-subprocesso-chefe-inicial.png', {
            fullPage: true,
            mask: [page.getByText(descProcMapeamento)]
        });

        // 5. Adicionar Atividade
        await page.getByTestId('card-subprocesso-atividades').click();
        await page.getByTestId('inp-nova-atividade').fill('Atividade Visual Fixa');
        await page.getByTestId('btn-adicionar-atividade').click();
        
        // Adicionar conhecimento (necessário para disponibilizar)
        const cardAtividade = page.locator('.atividade-card').filter({hasText: 'Atividade Visual Fixa'});
        await cardAtividade.getByTestId('inp-novo-conhecimento').fill('Conhecimento Visual Fixa');
        await cardAtividade.getByTestId('btn-adicionar-conhecimento').click();

        // Snapshot Atividades
        await expect(page).toHaveScreenshot('06-subprocesso-atividades.png', {
             mask: [page.getByText(descProcMapeamento)]
        });

        // 6. Disponibilizar Cadastro
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-disponibilizar-cadastro-confirmar')).toBeVisible();
        await page.getByTestId('btn-disponibilizar-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // 7. Admin Homologar Cadastro
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByText(descProcMapeamento).click();
        await page.getByRole('row', {name: 'Assessoria 21'}).click();
        await verificarPaginaSubprocesso(page);
        
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        
        // Snapshot Homologação
        await expect(page).toHaveScreenshot('07-admin-homologacao-atividades.png', {
            mask: [page.getByText(descProcMapeamento)]
        });

        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // 8. Mapa de Competências
        await page.getByText(descProcMapeamento).click();
        await page.getByRole('row', {name: 'Assessoria 21'}).click();
        
        await page.locator('[data-testid="card-subprocesso-mapa"], [data-testid="card-subprocesso-mapa-vis"]').first().click();

        await page.getByTestId('btn-abrir-criar-competencia').click();
        await page.getByTestId('inp-criar-competencia-descricao').fill('Competência Visual Fixa');
        await page.getByText('Atividade Visual Fixa').click();
        await page.getByTestId('btn-criar-competencia-salvar').click();

        // Snapshot Mapa
        await expect(page).toHaveScreenshot('08-mapa-competencias.png', {
            mask: [page.getByText(descProcMapeamento)]
        });

        // Disponibilizar Mapa
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
        
        // 9. Chefe Validar Mapa
        await fazerLogout(page);
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await page.getByText(descProcMapeamento).click();
        await page.getByTestId('card-subprocesso-mapa-vis').click();

        // Snapshot Validar Mapa
        await expect(page).toHaveScreenshot('09-chefe-validar-mapa.png', {
            mask: [page.getByText(descProcMapeamento)]
        });

        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        // 10. Admin Finalizar
        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByText(descProcMapeamento).click();
        await page.getByRole('row', {name: 'Assessoria 21'}).click();
        await page.getByTestId('card-subprocesso-mapa-vis').click();

        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();

        await page.goto('/painel');
        await page.getByText(descProcMapeamento).click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page); // Ensure navigation to panel is complete

        // Snapshot Final
        await expect(page).toHaveScreenshot('10-dashboard-finalizado.png', {
            fullPage: true,
            mask: [page.getByText(descProcMapeamento)]
        });
    });
});
