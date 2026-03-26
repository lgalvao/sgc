import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoFixture, validarProcessoFixture} from './fixtures/fixtures-processos.js';
import {fazerLogout, navegarParaSubprocesso, verificarAppAlert} from './helpers/helpers-navegacao.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';

/**
 * CDU-27 - Alterar data limite de subprocesso
 *
 * Ator: ADMIN
 *
 * Pré-condições:
 * - Unidade participante com subprocesso iniciado e ainda não finalizado
 *
 * Fluxo principal:
 * 1. ADMIN acessa processo ativo e clica em uma unidade
 * 2. Sistema mostra tela Detalhes do subprocesso
 * 3. ADMIN clica no botão 'Alterar data limite'
 * 4. Sistema abre modal com campo de data preenchido
 * 5. ADMIN altera a data e confirma
 * 6. Sistema atualiza e envia notificação
 */
test.describe.serial('CDU-27 - Alterar data limite de subprocesso', () => {
    const UNIDADE_1 = 'SECAO_221';
    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-27 ${timestamp}`;

    function calcularNovaDataIso(dias: number): string {
        const novaData = new Date();
        novaData.setDate(novaData.getDate() + dias);
        const yyyy = novaData.getFullYear();
        const mm = String(novaData.getMonth() + 1).padStart(2, '0');
        const dd = String(novaData.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    function obterDataAnterior(dataIso: string): string {
        const data = new Date(`${dataIso}T00:00:00`);
        data.setDate(data.getDate() - 1);
        const yyyy = data.getFullYear();
        const mm = String(data.getMonth() + 1).padStart(2, '0');
        const dd = String(data.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    function obterDataPosterior(dataIso: string): string {
        const data = new Date(`${dataIso}T00:00:00`);
        data.setDate(data.getDate() + 1);
        const yyyy = data.getFullYear();
        const mm = String(data.getMonth() + 1).padStart(2, '0');
        const dd = String(data.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_1,
            iniciar: true
        });
        validarProcessoFixture(processo, descProcesso);
    });


    test('Cenario 1: ADMIN navega para detalhes do subprocesso', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        // CDU-27: Passos 1-2


        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);

        // Verificar que está na página do subprocesso
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toBeVisible();
    });

    test('Cenario 2: ADMIN pode cancelar a alteração da data limite sem persistir mudanças', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);

        // Obter a data atual do prazo na página
        const prazoPaginaElement = page.getByTestId('subprocesso-header__txt-prazo');
        await expect(prazoPaginaElement).not.toBeEmpty();
        const prazoPagina = await prazoPaginaElement.innerText();

        const btnAlterarData = page.getByTestId('btn-alterar-data-limite');
        await expect(btnAlterarData).toBeVisible();
        await expect(btnAlterarData).toBeEnabled();
        await btnAlterarData.click();

        const modal = page.getByRole('dialog');
        await expect(modal.getByRole('heading', {name: TEXTOS.subprocesso.BOTAO_ALTERAR_DATA_LIMITE})).toBeVisible();

        const inputData = page.getByTestId('input-nova-data-limite');
        await expect(inputData).toBeVisible();
        
        // BUG FIX VERIFICATION: Verificar se o modal inicia com a data do prazo (não criação)
        const dataInicialModal = await inputData.inputValue();
        // Converter data do modal (yyyy-mm-dd) para formato brasileiro (dd/mm/yyyy) para comparar
        const [y, m, d] = dataInicialModal.split('-');
        expect(`${d}/${m}/${y}`).toBe(prazoPagina?.trim());

        const dataMinima = await inputData.getAttribute('min');
        expect(dataMinima).toBeTruthy();
        await expect(inputData).toHaveAttribute('min', dataMinima!);

        await inputData.fill(obterDataAnterior(dataMinima!));
        await expect(page.getByText('A data limite deve ser maior ou igual à última data limite do subprocesso.')).toBeVisible();
        await expect(page.getByTestId('btn-modal-confirmar')).toBeDisabled();

        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();
    });

    test('Cenario 3: ADMIN altera data limite e recebe confirmação', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
        await acessarDetalhesProcesso(page, descProcesso);
        await navegarParaSubprocesso(page, UNIDADE_1);

        const btnAlterarData = page.getByTestId('btn-alterar-data-limite');
        await btnAlterarData.click();

        const inputData = page.getByTestId('input-nova-data-limite');
        const dataMinima = await inputData.getAttribute('min');
        expect(dataMinima).toBeTruthy();
        const novaDataIso = obterDataPosterior(dataMinima!);
        await inputData.fill(novaDataIso);

        await page.getByTestId('btn-modal-confirmar').click();
        await verificarAppAlert(page, TEXTOS.subprocesso.SUCESSO_DATA_ALTERADA);

        // Verificar se a página atualizou o prazo
        const [y, m, d] = novaDataIso.split('-');
        await expect(page.getByTestId('subprocesso-header__txt-prazo')).toContainText(`${d}/${m}/${y}`);

        // Validar alerta criado para a unidade destino
        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);

        const tabelaAlertas = page.getByTestId('tbl-alertas');
        const linhaAlerta = tabelaAlertas.locator('tr', {hasText: descProcesso})
            .filter({hasText: 'Data limite da etapa'})
            .first();

        await expect(linhaAlerta).toBeVisible();
        await expect(linhaAlerta).toContainText(descProcesso);
        await expect(linhaAlerta).toContainText(/Data limite da etapa\s+1 alterada para/i);
        await expect(linhaAlerta).toContainText(/\d{2}\/\d{2}\/\d{4}\s+\d{2}:\d{2}/);
        await expect(linhaAlerta).toContainText(/ADMIN/i);
    });
});
