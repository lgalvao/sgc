import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, navegarParaMapa} from './helpers/helpers-mapas.js';
import {
    abrirHistoricoAnalise,
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    fecharHistoricoAnalise,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {fazerLogout, limparNotificacoes, navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';

async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}(?:/)?$`));
}

test.describe.serial('CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const timestamp = Date.now();
    const descProcessoMapeamento = `Map 10 ${timestamp}`;
    const descProcessoRevisao = `Rev 10 ${timestamp}`;

    test('1. Preparação: Mapeamento completo e Revisão iniciada', async ({page}) => {
        test.setTimeout(45000); // Passo inicial ainda é longo, mas reduzido de 60s
        // Admin cria e inicia processo de mapeamento
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcesso(page, {
            descricao: descProcessoMapeamento,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcessoMap = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descProcessoMapeamento)});
        await linhaProcessoMap.click();

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // Chefe adiciona atividades e disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Atividade Mapeamento 1 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Mapeamento 1 ${timestamp}`, 'Conhecimento 1');
        await adicionarAtividade(page, `Atividade Mapeamento 2 ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Mapeamento 2 ${timestamp}`, 'Conhecimento 2');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);

        // Aceites intermediários para Cadastro
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário COORD_22');
        await fazerLogout(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário SECRETARIA_2');
        await fazerLogout(page);

        // Admin homologa cadastro
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroMapeamento(page);

        // Admin adiciona competências e disponibiliza mapa
        await navegarParaMapa(page);
        await criarCompetencia(page, `Competência Mapeamento 1 ${timestamp}`, [`Atividade Mapeamento 1 ${timestamp}`]);
        await criarCompetencia(page, `Competência Mapeamento 2 ${timestamp}`, [`Atividade Mapeamento 2 ${timestamp}`]);

        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        const modal = page.getByTestId('mdl-disponibilizar-mapa');
        await expect(modal).toBeVisible();
        await page.getByTestId('inp-disponibilizar-mapa-data').fill('2030-12-31');
        await page.getByTestId('btn-disponibilizar-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        // Chefe valida mapa
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await fazerLogout(page);

        // Aceites intermediários para Mapa
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await fazerLogout(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await fazerLogout(page);

        // Admin homologa mapa e finaliza processo
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcessoMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);
        await page.getByTestId('tbl-processos').getByText(descProcessoMapeamento).first().click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // Admin cria e inicia processo de revisão
        await criarProcesso(page, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });
        const linhaProcessoRev = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcessoRevisao)});
        await linhaProcessoRev.click();
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await verificarPaginaPainel(page);

        // Chefe revisa atividades (muda situação para EM_ANDAMENTO)
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Atividade Revisão Nova ${timestamp}`);
        await adicionarConhecimento(page, `Atividade Revisão Nova ${timestamp}`, 'Conhecimento Revisão');
        await page.getByTestId('btn-cad-atividades-voltar').click();
        await verificarPaginaSubprocesso(page, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);
    });

    test('2. Cenário 1: Validação - Atividade sem conhecimento', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        const atividadeIncompleta = `Atividade Incompleta ${timestamp}`;
        await adicionarAtividade(page, atividadeIncompleta);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        const erroInline = page.getByTestId('atividade-erro-validacao');
        await expect(erroInline).toBeVisible();
        await expect(erroInline).toContainText(/conhecimento/i);

        await adicionarConhecimento(page, atividadeIncompleta, 'Conhecimento Corretivo');
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await expect(page.getByTestId('btn-confirmar-disponibilizacao')).toBeVisible();
        await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
    });

    test('3. Cenário 2: Caminho feliz - Disponibilizar revisão', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        const modalConfirmacao = page.getByRole('dialog');
        await expect(modalConfirmacao.getByText('Disponibilização da revisão do cadastro')).toBeVisible();
        await expect(modalConfirmacao.getByText(/Confirma a finalização da revisão e a disponibilização do cadastro/i)).toBeVisible();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Revisão do cadastro de atividades disponibilizada/i).first()).toBeVisible();
        await verificarPaginaPainel(page);

        // Verificar alerta para o gestor superior
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await expect(page.getByTestId('tbl-alertas')).toContainText(`Revisão do cadastro da unidade ${UNIDADE_ALVO} disponibilizada para análise`);

        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão d[oe] cadastro disponibilizada/i);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Disponibilização da revisão do cadastro de atividades/i);
    });

    test('4. Cenário 3: Devolução e Histórico', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-devolver').click();
        const motivoDevolucao = 'Necessário revisar os conhecimentos técnicos.';
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(motivoDevolucao);
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar movimentação de devolução
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await expect(page.getByTestId('tbl-movimentacoes')).toContainText(/Devolução da revisão do cadastro para ajustes/i);

        // Verificar alerta para o chefe da unidade
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await limparNotificacoes(page);
        await expect(page.getByTestId('tbl-alertas').locator('tr', { hasText: `Revisão do cadastro da unidade ${UNIDADE_ALVO} devolvida para ajustes` })).toBeVisible();

        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText(motivoDevolucao);
        await fecharHistoricoAnalise(page);

        await disponibilizarCadastro(page);
        await expect(page.getByText(/Revisão do cadastro de atividades disponibilizada/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('5. Cenário 4: Limpeza de Histórico após nova disponibilização', async ({page}) => {
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Segunda devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar movimentação de devolução (2ª vez)
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await expect(page.getByTestId('tbl-movimentacoes').locator('tr', { hasText: /Devolução da revisão do cadastro para ajustes/i }).first()).toBeVisible();

        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        await disponibilizarCadastro(page);
        await expect(page.getByText(/Revisão do cadastro de atividades disponibilizada/i).first()).toBeVisible();
        await verificarPaginaPainel(page);

        // Gestor devolve novamente
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-devolver').click();
        await page.getByTestId('inp-devolucao-cadastro-obs').fill('Terceira devolução');
        await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
        await verificarPaginaPainel(page);

        // Verificar alerta da 3ª devolução
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await limparNotificacoes(page);
        await expect(page.getByTestId('tbl-alertas').locator('tr', { hasText: `Revisão do cadastro da unidade ${UNIDADE_ALVO} devolvida para ajustes` }).first()).toBeVisible();

        // Chefe verifica que histórico tem apenas a última devolução
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        const modal = await abrirHistoricoAnalise(page);
        await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
        await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Terceira devolução');
        await expect(modal.getByTestId('cell-resultado-1')).toBeHidden();
        await fecharHistoricoAnalise(page);
    });

    test('6. Cenário 5: Cancelar disponibilização', async ({page}) => {
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await limparNotificacoes(page);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-disponibilizar-revisao-cancelar').click();
        await expect(page.getByRole('dialog')).toBeHidden();
        await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();
    });
});
