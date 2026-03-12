import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, verificarDetalhesSubprocesso, verificarProcessoNaTabela} from './helpers/helpers-processos.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

test.describe('CDU-07 - Detalhar subprocesso', () => {
    const UNIDADE_ALVO = 'SECAO_211';
    const CHEFE_UNIDADE = USUARIOS.CHEFE_SECAO_211.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_211.senha;
    const GESTOR_UNIDADE = USUARIOS.GESTOR_COORD_21.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_21.senha;

    test('Deve exibir detalhes do subprocesso para ADMIN, GESTOR e CHEFE e respeitar regras de cards', async (
        {
            page, autenticadoComoAdmin
        }) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-07 ${timestamp}`;

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21'],
            iniciar: true
        });

        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });

        await page.getByTestId('tbl-processos').getByText(descricao, {exact: true}).first().click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            situacao: 'Não iniciado',
            localizacao: UNIDADE_ALVO
        });
        
        await expect(page.getByText('Titular:')).toBeVisible();

        // Para ADMIN (antes da homologação do cadastro), ambos os cards devem estar no modo de visualização
        const cardAtividadesAdmin = page.locator('[data-testid="card-subprocesso-atividades-vis"]');
        await expect(cardAtividadesAdmin).toBeVisible();
        
        const cardMapaAdmin = page.locator('[data-testid="card-subprocesso-mapa-visualizacao"]');
        await expect(cardMapaAdmin).toBeVisible();

        await expect(page.getByRole('heading', {name: 'Movimentações'})).toBeVisible();
        await expect(page.locator('table tbody tr')).not.toHaveCount(0);

        await page.getByTestId('btn-logout').click();
        await login(page, GESTOR_UNIDADE, SENHA_GESTOR);
        await page.getByTestId('tbl-processos').getByText(descricao, {exact: true}).first().click();
        await expect(page).toHaveURL(/\/processo\/\d+$/);
        await expect(page.getByRole('row').filter({has: page.getByRole('cell', {name: new RegExp(String.raw`^${UNIDADE_ALVO}\b`)})})).toBeVisible();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        
        // Para GESTOR (antes da disponibilização), cards no modo visualização
        const cardAtividadesGestor = page.locator('[data-testid="card-subprocesso-atividades-vis"]');
        await expect(cardAtividadesGestor).toBeVisible();

        await page.getByTestId('btn-logout').click();
        await login(page, CHEFE_UNIDADE, SENHA_CHEFE);
        await page.getByTestId('tbl-processos').getByText(descricao, {exact: true}).first().click();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}$`));

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            situacao: 'Não iniciado',
            localizacao: UNIDADE_ALVO
        });
        
        // Para CHEFE, card de atividades DEVE estar em modo de edição
        const cardAtividadesChefe = page.locator('[data-testid="card-subprocesso-atividades"]');
        await expect(cardAtividadesChefe).toBeVisible();
        
        // Mas card de mapa DEVE continuar em modo visualização
        const cardMapaChefe = page.locator('[data-testid="card-subprocesso-mapa-visualizacao"]');
        await expect(cardMapaChefe).toBeVisible();
    });
});
