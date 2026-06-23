import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture,
    criarProcessoFinalizadoFixture
} from './fixtures/index.js';
import {login} from './helpers/helpers-auth.js';
import {navegarParaDiagnosticoUnidade} from './helpers/helpers-navegacao.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

const TITULO_SERVIDOR_SECAO_111 = '444444';
const NOME_SERVIDOR_SECAO_111 = 'SERVIDOR_SECAO_111_E_CHEFE_SECAO_112';
const UNIDADE = 'SECAO_111';

test.describe('CDU-43 - Visualizar detalhes de subprocesso de diagnóstico: GESTOR e ADMIN', () => {
    test('GESTOR e ADMIN visualizam a tabela hierárquica de unidades com restrições e analisam os detalhes da unidade', async ({
        _resetAutomatico,
        page,
        request
    }) => {
        const descricao = `Diagnóstico CDU-43 ${Date.now()}`;

        // Garante mapa vigente prévio para SECAO_111, obrigatório para processos de diagnóstico
        await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE,
            iniciar: true
        });

        const processo = await criarProcessoDiagnosticoComAutoavaliacaoConcluidaFixture(request, {
            descricao,
            unidade: UNIDADE,
            iniciar: true,
            servidorTitulo: TITULO_SERVIDOR_SECAO_111
        });

        // 1. GESTOR acessa os detalhes do processo
        // GESTOR_COORD: 222222 (COORD_11)
        await login(page, '222222', 'senha');
        await page.goto(`/processo/${processo.codigo}`);

        // A tabela hierárquica deve limitar-se à unidade do usuário e subordinadas recursivamente
        // COORD_11 e suas subordinadas devem aparecer, mas outras secretarias/unidades independentes não
        const treeTable = page.getByTestId('tbl-tree');
        await expect(treeTable).toBeVisible();
        await expect(treeTable.getByText('COORD_11')).toBeVisible();
        await expect(treeTable.getByText('SECAO_111')).toBeVisible();
        await expect(treeTable.getByText('SECRETARIA_2')).not.toBeVisible();

        // 2. ADMIN acessa os detalhes do processo
        // ADMIN_1_PERFIL: 191919
        await login(page, '191919', 'senha');
        await page.goto(`/processo/${processo.codigo}`);

        // ADMIN deve visualizar todas as unidades participantes
        await expect(treeTable.getByText('COORD_11')).toBeVisible();
        await expect(treeTable.getByText('SECAO_111')).toBeVisible();

        // 3. ADMIN clica na unidade subordinada e acessa a análise do diagnóstico
        await navegarParaDiagnosticoUnidade(page, UNIDADE);

        // Verifica a exibição da tela de análise do diagnóstico da unidade
        await expect(page.getByTestId('diagnostico-unidade-titulo')).toHaveText(UNIDADE);
        await expect(page.getByTestId('btn-historico-analise-unidade')).toBeVisible();
        
        // Deve mostrar a tabela de Servidores e Consenso
        await expect(page.getByText('Servidores participantes', {exact: true})).toBeVisible();
        await expect(page.getByRole('cell', {name: NOME_SERVIDOR_SECAO_111, exact: true}).first()).toBeVisible();
        await expect(page.getByTestId('lista-servidores-diagnostico-unidade')).toContainText(NOME_SERVIDOR_SECAO_111);
        await expect(page.getByText('Avaliações de competências', {exact: true})).toBeVisible();
        await expect(page.getByTestId('tbl-competencias-servidor-diagnostico-unidade')).toBeVisible();
        await expect(page.getByTestId('tbl-movimentacoes')).toBeVisible();

        // Testar o botão Voltar (especificamente o do painel de conteúdo principal)
        const botaoVoltar = page.getByTestId('main-content').getByRole('button', {name: TEXTOS.diagnostico.BTN_VOLTAR});
        await expect(botaoVoltar).toBeVisible();
        await botaoVoltar.click();

        // Deve redirecionar de volta para o processo
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processo.codigo}`));
    });
});
