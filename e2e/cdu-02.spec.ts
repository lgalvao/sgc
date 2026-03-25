import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {fazerLogout} from './helpers/helpers-navegacao.js';
import {criarProcesso, verificarCabecalhosTabelaProcessos, verificarProcessoNaTabela} from './helpers/helpers-processos.js';

test.describe('CDU-02 - Visualizar painel', () => {
    test.describe('Como ADMIN', () => {
        test('Deve exibir estrutura básica do painel e testar ordenação', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
            const descricaoProcesso = `Processo estrutura painel - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_11',
                expandir: ['SECRETARIA_1']
            });

            await test.step('Verificar seções principais', async () => {
                await expect(page.getByTestId('txt-painel-titulo-processos')).toBeVisible();
                await expect(page.getByTestId('txt-painel-titulo-processos')).toHaveText('Processos');
                await expect(page.getByTestId('txt-painel-titulo-alertas')).toBeVisible();
                await expect(page.getByTestId('txt-painel-titulo-alertas')).toHaveText('Alertas');
            });

            await test.step('Verificar botão de criação', async () => {
                await expect(page.getByTestId('btn-painel-criar-processo')).toBeVisible();
            });

            await test.step('Verificar campos da tabela de processos ativos', async () => {
                await verificarCabecalhosTabelaProcessos(page, true);
                await verificarProcessoNaTabela(page, {
                    descricao: descricaoProcesso,
                    tipo: 'Mapeamento',
                    situacao: 'Criado',
                    unidadesParticipantes: ['ASSESSORIA_11']
                });
            });

            await test.step('Testar ordenação da tabela de processos', async () => {
                const tabelaProcessos = page.locator('[data-testid="tbl-processos"]');
                const cabecalhoDescricao = tabelaProcessos.locator('th', {hasText: 'Descrição'}).first();

                await expect(cabecalhoDescricao).toHaveAttribute('aria-sort', /ascending|descending|none/);

                await cabecalhoDescricao.click();
                await expect(cabecalhoDescricao).toHaveAttribute('aria-sort', /ascending|descending/);

                await cabecalhoDescricao.click();
                await expect(cabecalhoDescricao).toHaveAttribute('aria-sort', /ascending|descending/);
            });
        });

        test('Deve criar processo e visualizá-lo na tabela', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
            const descricaoProcesso = `Processo E2E - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_21',
                expandir: ['SECRETARIA_2']
            });

            // Verifica que o processo aparece na tabela
            await page.goto('/painel');
            await expect(page).toHaveURL(/\/painel/);

            // Valida que o processo criado aparece na tabela do painel principal
            await verificarProcessoNaTabela(page, {
                descricao: descricaoProcesso,
                tipo: 'Mapeamento',
                situacao: 'Criado'
            });
        });

        test('Processos "Criado" devem aparecer apenas para ADMIN', async ({
                                                                               _resetAutomatico,
                                                                               page,
                                                                               _autenticadoComoAdmin
}) => {
            const descricaoProcesso = `Processo criado - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_12',
                expandir: ['SECRETARIA_1']
            });

            // Verifica que o processo está visível para ADMIN
            await page.goto('/painel');
            const tabelaAdmin = page.locator('[data-testid="tbl-processos"]');
            await expect(tabelaAdmin.locator('tr').filter({hasText: descricaoProcesso}).first()).toBeVisible();

            // Faz logout e login como GESTOR
            await fazerLogout(page);
            await login(page,
                USUARIOS.GESTOR_COORD.titulo,
                USUARIOS.GESTOR_COORD.senha
            );

            // Verifica que o processo NÃO está visível para GESTOR
            const tabelaGestor = page.locator('[data-testid="tbl-processos"]');
            await expect(tabelaGestor.locator('tr').filter({hasText: descricaoProcesso})).toBeHidden();
        });


        test('Não deve incluir unidades INTERMEDIARIAS na seleção', async ({
                                                                               _resetAutomatico,
                                                                               page,
                                                                               _autenticadoComoAdmin
}) => {
            await page.getByTestId('btn-painel-criar-processo').click();
            await expect(page).toHaveURL(/\/processo\/cadastro/);

            const descricaoProcesso = `Teste filtragem - ${Date.now()}`;
            await page.getByTestId('inp-processo-descricao').fill(descricaoProcesso);
            await page.getByTestId('sel-processo-tipo').selectOption('MAPEAMENTO');

            const dataLimite = new Date();
            dataLimite.setDate(dataLimite.getDate() + 30);
            await page.getByTestId('inp-processo-data-limite').fill(dataLimite.toISOString().split('T')[0]);


            // Aguarda a animação/renderização e expande SECRETARIA_1
            const btnSecretaria = page.getByTestId('btn-arvore-expand-SECRETARIA_1');
            await btnSecretaria.waitFor({state: 'visible'});
            await btnSecretaria.click();

            // Verifica que COORD_11 (INTERMEDIARIA) está HABILITADA (novo comportamento)
            const checkboxIntermediaria = page.getByTestId('chk-arvore-unidade-COORD_11');
            await expect(checkboxIntermediaria).toBeEnabled();
            await expect(checkboxIntermediaria).not.toBeChecked();

            // Clica em COORD_11 (INTERMEDIARIA) para selecionar suas filhas
            await checkboxIntermediaria.click();

            // Verifica que COORD_11 foi marcada (comportamento visual, embora filtrada no envio)
            await expect(checkboxIntermediaria).toBeChecked();

            // Expande COORD_11 para ver as filhas
            await page.getByTestId('btn-arvore-expand-COORD_11').click();

            // Verifica que as filhas OPERACIONAIS foram selecionadas automaticamente
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_111')).toBeChecked();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_112')).toBeChecked();
            await expect(page.getByTestId('chk-arvore-unidade-SECAO_113')).toBeChecked();

            await page.getByTestId('btn-processo-salvar').click();

            await expect(page).toHaveURL(/\/painel/);
            await page.goto('/painel');

            // Verifica que o processo foi criado e aparece na tabela
            await verificarProcessoNaTabela(page, {
                descricao: descricaoProcesso,
                situacao: 'Criado',
                tipo: 'Mapeamento',
                unidadesParticipantes: ['COORD_11']
            });
        });

        test('Deve abrir Cadastro de processo ao clicar em processo Criado como ADMIN', async ({
                                                                                                  _resetAutomatico,
                                                                                                  page,
                                                                                                  _autenticadoComoAdmin
}) => {
            const descricaoProcesso = `Processo criado clicável - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'ASSESSORIA_11',
                expandir: ['SECRETARIA_1']
            });

            await page.goto('/painel');
            const linhaProcessoCriado = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso}).first();
            await expect(linhaProcessoCriado).toBeVisible();

            await linhaProcessoCriado.click();
            await expect(page).toHaveURL(/\/processo\/cadastro(?:\?codProcesso=\d+|\/\d+)$/);
            await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricaoProcesso);
        });

        test('Deve respeitar regra de clique por perfil em processo Em andamento', async ({
                                                                                             _resetAutomatico,
                                                                                             page,
                                                                                             _autenticadoComoAdmin
}) => {
            const descricaoProcesso = `Processo clique perfis - ${Date.now()}`;

            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: 'COORD_11',
                expandir: ['SECRETARIA_1'],
                iniciar: true
            });

            await page.goto('/painel');
            const linhaAdmin = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso}).first();
            await expect(linhaAdmin).toBeVisible();
            await linhaAdmin.click();
            await expect(page).toHaveURL(/\/processo\/\d+$/);

            await fazerLogout(page);
            await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);

            const linhaGestor = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso}).first();
            await expect(linhaGestor).toBeVisible();
            await linhaGestor.click();
            await expect(page).toHaveURL(/\/processo\/\d+$/);

            await fazerLogout(page);
            await login(page, USUARIOS.CHEFE_SECAO_111.titulo, USUARIOS.CHEFE_SECAO_111.senha);

            const linhaChefe = page.getByTestId('tbl-processos').locator('tr', {hasText: descricaoProcesso}).first();
            await expect(linhaChefe).toBeVisible();
            await linhaChefe.click();
            await expect(page).toHaveURL(/\/processo\/\d+\/SECAO_111$/);
        });
    });

    test.describe('Como GESTOR', () => {
        test('Deve validar visualização, alertas e ordenação', async ({_resetAutomatico, page, _autenticadoComoGestor}) => {
            await test.step('Verificar restrições de botões e mensagens de tabela vazia', async () => {
                await expect(page.getByTestId('btn-painel-criar-processo')).toBeHidden();
                // A tabela não é renderizada se estiver vazia (EmptyState substitui)
                await expect(page.getByTestId('empty-state-processos')).toBeVisible();
            });

            await test.step('Verificar tabela de alertas vazia', async () => {
                await expect(page.getByTestId('empty-state-alertas')).toBeVisible();
                await expect(page.getByTestId('empty-state-alertas')).toContainText(/Nenhum alerta/i);
            });
        });
    });
});
