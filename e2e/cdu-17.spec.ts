import {expect, test} from './fixtures/complete-fixtures.js';
import {USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-17 - Disponibilizar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-17 ${timestamp}`;

    // Atividades e competências para os testes
    const atividade1 = `Atividade 1 ${timestamp}`;
    const atividade2 = `Atividade 2 ${timestamp}`;
    const atividade3 = `Atividade 3 ${timestamp}`;
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;

    // ========================================================================
    // PREPARAÇÃO - Criar mapa pronto para disponibilização
    // ========================================================================

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        const processoId = await extrairProcessoId(page);
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro', async ({page, autenticadoComoChefeSecao221}) => {
        // Acessar subprocesso
        await page.getByText(descProcesso).click();
        await navegarParaAtividades(page);

        // Três atividades para mapear corretamente
        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento 1A');

        await adicionarAtividade(page, atividade2);
        await adicionarConhecimento(page, atividade2, 'Conhecimento 2A');

        await adicionarAtividade(page, atividade3);
        await adicionarConhecimento(page, atividade3, 'Conhecimento 3A');

        await expect(page.getByTestId('btn-cad-atividades-disponibilizar')).toBeVisible(); 
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Cadastro de atividades disponibilizado/i)).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Admin homologa cadastro', async ({page, autenticadoComoAdmin}) => {
        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
    });

    test('Preparacao 4: Admin cria competências com todas as atividades associadas', async ({page, autenticadoComoAdmin}) => {
        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await navegarParaMapa(page);

        // Criar duas competências cobrindo todas as três atividades
        await criarCompetencia(page, competencia1, [atividade1, atividade2]);
        await criarCompetencia(page, competencia2, [atividade3]);

        // Verificar competências criadas
        await expect(page.getByText(competencia1)).toBeVisible();
        await expect(page.getByText(competencia2)).toBeVisible();

        // Verificar que o botão Disponibilizar está visível (situação: MAPEAMENTO_MAPA_CRIADO)
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();
    });

    // ========================================================================
    // TESTES PRINCIPAIS - CDU-17
    // ========================================================================

    test('Cenario 1: ADMIN navega para tela de edição do mapa', async ({page, autenticadoComoAdmin}) => {
        // Passo 1: ADMIN escolhe o processo de mapeamento
        await expect(page.getByText(descProcesso)).toBeVisible();
        await page.getByText(descProcesso).click();

        // Passo 2-3: Detalhes do processo, clica na unidade
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
        await navegarParaSubprocesso(page, 'SECAO_221');
        
        // Passo 5-6: Clica no card Mapa de Competências
        await page.getByTestId('card-subprocesso-mapa').click();

        // Verificar tela de Edição de mapa
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();
    });

    test('Cenario 2: ADMIN abre modal de disponibilização', async ({page, autenticadoComoAdmin}) => {
        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();

        // Passo 7: ADMIN clica em Disponibilizar
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();

        // Passo 10: Sistema mostra modal com Data e Observações
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeVisible();
        await expect(page.getByText('Disponibilização do mapa')).toBeVisible();
    });

    test('Cenario 3: ADMIN cancela disponibilização - permanece na tela', async ({page, autenticadoComoAdmin}) => {
        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();

        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeVisible();

        // Passo 11: ADMIN clica em Cancelar
        await page.getByTestId('btn-disponibilizar-mapa-cancelar').click();

        // Sistema permanece na tela de Edição de mapa
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
    });

    test('Cenario 4: ADMIN disponibiliza mapa com sucesso', async ({page, autenticadoComoAdmin}) => {
        await page.getByText(descProcesso).click();
        await navegarParaSubprocesso(page, 'SECAO_221');
        await page.getByTestId('card-subprocesso-mapa').click();

        // Usar helper para disponibilizar
        await disponibilizarMapa(page, '2030-12-31');

        // Passo 20: Redireciona para Painel com confirmação
        await verificarPaginaPainel(page);
        await expect(page.getByText(/Mapa disponibilizado/i)).toBeVisible();
    });
});
