import {expect, test} from './fixtures/complete-fixtures.js';
import {USUARIOS, login, loginComPerfil} from './helpers/helpers-auth.js';
import {acessarDetalhesProcesso, criarProcesso} from './helpers/helpers-processos.js';
import {navegarParaDiagnosticoUnidade} from './helpers/helpers-navegacao.js';

const UNIDADE_SUBORDINADA = 'ASSESSORIA_12';

test.describe.serial('CDU-49 - Acompanhar diagnóstico de unidades subordinadas', () => {
    const descricao = `Diagnóstico CDU-49 ${Date.now()}`;

    test('Setup: ADMIN cria processo de diagnóstico', async ({
        _resetAutomatico,
        page,
    }) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcesso(page, {
            descricao,
            tipo: 'DIAGNOSTICO',
            unidade: UNIDADE_SUBORDINADA,
            expandir: ['SECRETARIA_1'],
            iniciar: true,
        });
        await expect(page.getByTestId('tbl-processos').getByText(descricao).first()).toBeVisible();
    });

    test('GESTOR vê sua unidade subordinada e acessa o monitoramento inline', async ({
        _resetAutomatico,
        page,
    }) => {
        await loginComPerfil(
            page,
            USUARIOS.GESTOR_SECRETARIA_1.titulo,
            USUARIOS.GESTOR_SECRETARIA_1.senha,
            USUARIOS.GESTOR_SECRETARIA_1.perfil,
        );

        await acessarDetalhesProcesso(page, descricao);

        const tabelaArvore = page.getByTestId('tbl-tree');
        await expect(tabelaArvore).toBeVisible();
        await expect(tabelaArvore.getByText('SECRETARIA_1 - Secretaria 1').first()).toBeVisible();
        await expect(tabelaArvore.getByRole('row', {name: /^ASSESSORIA_12\b/i}).first()).toBeVisible();

        await navegarParaDiagnosticoUnidade(page, UNIDADE_SUBORDINADA);
        await expect(page.getByRole('heading', {name: 'Análise do Diagnóstico da Unidade'})).toBeVisible();
        await expect(page.getByText(UNIDADE_SUBORDINADA, {exact: true})).toBeVisible();
        await expect(page.getByText('Competência x Servidor', {exact: true})).toBeVisible();
        await expect(page.getByText('Servidores e Consenso', {exact: true})).toBeVisible();
    });

    test('ADMIN acompanha o mesmo subprocesso pela árvore e acessa o monitoramento inline', async ({
        _resetAutomatico,
        page,
    }) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await acessarDetalhesProcesso(page, descricao);

        const tabelaArvore = page.getByTestId('tbl-tree');
        await expect(tabelaArvore).toBeVisible();
        await expect(tabelaArvore.getByRole('row', {name: /^ASSESSORIA_12\b/i}).first()).toBeVisible();

        await navegarParaDiagnosticoUnidade(page, UNIDADE_SUBORDINADA);
        await expect(page.getByRole('heading', {name: 'Análise do Diagnóstico da Unidade'})).toBeVisible();
        await expect(page.getByText(UNIDADE_SUBORDINADA, {exact: true})).toBeVisible();
        await expect(page.getByText('Competência x Servidor', {exact: true})).toBeVisible();
        await expect(page.getByText('Servidores e Consenso', {exact: true})).toBeVisible();
    });
});
