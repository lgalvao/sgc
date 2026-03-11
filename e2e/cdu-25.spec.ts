 
import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {aceitarCadastroMapeamento, acessarSubprocessoGestor} from './helpers/helpers-analise.js';

async function acessarSubprocessoChefe(page: Page, descProcesso: string) {
    await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
    await navegarParaMapa(page);
}

/**
 * CDU-25 - Aceitar validação de mapas de competências em bloco
 *
 * Ator: GESTOR
 *
 * Pré-condições:
 * - Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões'
 * - Localização atual na unidade do usuário
 *
 * Fluxo principal:
 * 1. GESTOR acessa processo em andamento
 * 2. Sistema mostra Detalhes do processo
 * 3. Sistema identifica unidades elegíveis para aceite
 * 4. GESTOR clica no botão 'Aceitar Mapa em Bloco'
 * 5. Sistema abre modal com lista de unidades
 * 6. GESTOR confirma
 * 7. Sistema executa aceite para cada unidade
 */
test.describe.serial('CDU-25 - Aceitar validação de mapas em bloco', () => {
    test.setTimeout(60000);
    const UNIDADE_1 = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-25 ${timestamp}`;

    const atividade1 = `Atividade Val ${timestamp}`;
    const competencia1 = `Competência Val ${timestamp}`;

    test('Setup UI', async ({page}) => {

        // Preparacao 1: Admin cria e inicia processo
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_1,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);

        // Preparacao 2: Chefe disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Val 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);

        // Gestor COORD_21 aceita cadastro
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // Gestor SECRETARIA_2 aceita cadastro
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_1);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // Admin homologa cadastro e cria mapa
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, UNIDADE_1);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividade1]);
        await disponibilizarMapa(page, '2030-12-31');
        await verificarPaginaPainel(page);

        // Chefe valida o mapa
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);

        await acessarSubprocessoChefe(page, descProcesso);

        await page.getByTestId('btn-mapa-validar').click();
        await expect(page.getByRole('dialog')).toBeVisible();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Cenario 1: GESTOR acessa processo com mapa validado', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByRole('button', {name: /Aceitar mapas em bloco/i}).first();
        await expect(btnAceitar).toBeVisible();
        await expect(btnAceitar).toBeEnabled();
    });

    test('Cenario 2: GESTOR abre modal de aceite de mapa em bloco e cancela', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        const btnAceitar = page.getByRole('button', {name: /Aceitar mapas em bloco/i}).first();
        await btnAceitar.click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).toBeHidden();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });

    test('Cenario 3: GESTOR realiza aceite em bloco com sucesso', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await page.getByRole('button', {name: /Aceitar mapas em bloco/i}).first().click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        
        // Verifica título e texto
        await expect(modal.getByText(/Aceite de mapas em bloco/i)).toBeVisible();
        await expect(modal.getByText(/Selecione as unidades para aceite dos mapas correspondentes/i)).toBeVisible();

        const linhaUnidade = modal.locator('tr', { hasText: 'SECAO_211' });
        await expect(linhaUnidade).toBeVisible();
        
        const checkbox = linhaUnidade.locator('input[type="checkbox"]');
        await expect(checkbox).toBeChecked();

        await modal.getByRole('button', {name: /Registrar aceite/i}).click();

        await expect(page.getByText('Mapas aceitos em bloco')).toBeVisible();
        await verificarPaginaPainel(page);
    });
});
