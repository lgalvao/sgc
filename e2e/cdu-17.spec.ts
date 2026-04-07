import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroHomologadoFixture, validarProcessoFixture} from './fixtures/index.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';

test.describe.serial('CDU-17 - Disponibilizar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-17 ${timestamp}`;

    const atividade1 = 'Atividade fixture 1';
    const atividade2 = 'Atividade fixture 2';
    const atividade3 = 'Atividade fixture 3';
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;

    function obterDataAnterior(dataIso: string): string {
        const data = new Date(`${dataIso}T00:00:00`);
        data.setDate(data.getDate() - 1);
        const ano = data.getFullYear();
        const mes = String(data.getMonth() + 1).padStart(2, '0');
        const dia = String(data.getDate()).padStart(2, '0');
        return `${ano}-${mes}-${dia}`;
    }

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoCadastroHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        validarProcessoFixture(processo, descProcesso);
    });

    // TESTES PRINCIPAIS - CDU-17

    test('Cenários CDU-17: Fluxo completo de disponibilização do mapa pelo ADMIN', async ({
                                                                                              _resetAutomatico,
                                                                                              page,
                                                                                              _autenticadoComoAdmin
}) => {

        // Cenario 1: Navegação
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, 'SECAO_211');
        await navegarParaMapa(page);

        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();

        await criarCompetencia(page, competencia1, [atividade1, atividade2]);
        await criarCompetencia(page, competencia2, [atividade3]);

        await expect(page.getByText(competencia1)).toBeVisible();
        await expect(page.getByText(competencia2)).toBeVisible();

        // Cenario 2: Abrir modal e verificar campos
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeVisible();
        await expect(page.getByText('Disponibilização do mapa')).toBeVisible();
        // CDU-17 Passo 9: verificar campo Observações (opcional) e botões
        await expect(page.getByTestId('inp-disponibilizar-mapa-data')).toBeVisible();
        await expect(page.getByTestId('inp-disponibilizar-mapa-obs')).toBeVisible();
        await expect(page.getByTestId('btn-disponibilizar-mapa-confirmar')).toBeVisible();
        await expect(page.getByTestId('btn-disponibilizar-mapa-cancelar')).toBeVisible();

        // Cenario 3: Cancelar
        await page.getByTestId('btn-disponibilizar-mapa-cancelar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();

        // Cenario 4: Validar data menor que a última data limite do subprocesso
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        const modal = page.getByTestId('mdl-disponibilizar-mapa');
        await expect(modal).toBeVisible();

        const campoData = page.getByTestId('inp-disponibilizar-mapa-data');
        const dataMinima = await campoData.getAttribute('min');
        expect(dataMinima).toBeTruthy();
        const dataInvalida = obterDataAnterior(dataMinima!);
        await campoData.fill(dataInvalida);

        const valorAplicado = await campoData.inputValue();
        if (valorAplicado === dataInvalida) {
            await expect(page.getByTestId('txt-disponibilizar-mapa-erro-data')).toHaveText(
                'A data limite deve ser maior ou igual à última data limite do subprocesso.'
            );
        } else {
            expect(valorAplicado).not.toBe(dataInvalida);
        }
        await expect(page.getByTestId('btn-disponibilizar-mapa-confirmar')).toBeDisabled();

        await page.getByTestId('btn-disponibilizar-mapa-cancelar').click();

        // Cenario 5: Disponibilizar com sucesso (com observações preenchidas)
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeVisible();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('inp-disponibilizar-mapa-obs').fill('Mapa disponibilizado com observação de teste');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, 'SECAO_211');
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);

        // CDU-17 Passo 14: verificar movimentação registrada com data/hora
        const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
            .locator('tr', {hasText: /Disponibilização do mapa/i})
            .first();
        await expect(linhaMovimentacao).toBeVisible();
        await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}/);
        await expect(linhaMovimentacao).toContainText(/ADMIN/i);
        await expect(linhaMovimentacao).toContainText(/SECAO_211/i);
    });

    test('Cenario 6: CHEFE recebe alerta de mapa disponibilizado no painel', async ({_resetAutomatico, page}) => {
        // CDU-17 Passo 17: sistema cria alerta para a unidade do subprocesso (CHEFE_SECAO_211)
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);

        const tabelaAlertas = page.getByTestId('tbl-alertas');
        const linhaAlerta = tabelaAlertas.locator('tr', {hasText: descProcesso}).first();
        await expect(linhaAlerta).toBeVisible();
        await expect(linhaAlerta).toContainText(/SECAO_211/i);
        await expect(linhaAlerta).toContainText(/\d{2}\/\d{2}\/\d{4}/);
    });
});
