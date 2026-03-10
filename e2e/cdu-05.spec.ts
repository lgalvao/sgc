/* eslint-disable playwright/expect-expect */
import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, verificarProcessoNaTabela} from './helpers/helpers-processos.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento,
} from './helpers/helpers-analise.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa,} from './helpers/helpers-mapas.js';


test.describe.serial('CDU-05 - Iniciar processo de revisao', () => {
    // Unidade ASSESSORIA_21 (12) - Titular 777777 (Janis Joplin)
    const UNIDADE_ALVO = 'ASSESSORIA_21';
    const USUARIO_CHEFE = '777777';
    const SENHA_CHEFE = 'senha';

    const timestamp = Date.now();
    const descProcMapeamento = `Mapeamento Setup ${timestamp}`;
    const descProcRevisao = `Revisão Teste ${timestamp}`;

    // PASSOS DE PREPARAÇÃO - PROCESSO DE MAPEAMENTO

    async function passo1_AdminCriaEIniciaProcessoMapeamento(page: Page, descricao: string): Promise<void> {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Validação: Processo foi criado e está na tela de cadastro
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Criado',
            tipo: 'Mapeamento'
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descricao)});
        await linhaProcesso.click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricao);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Validação: Processo iniciado com sucesso
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });
    }


    test('Fase 1.1: ADMIN cria e inicia processo de Mapeamento', async ({
                                                                            page,
                                                                            autenticadoComoAdmin
                                                                        }) => {
        await passo1_AdminCriaEIniciaProcessoMapeamento(page, descProcMapeamento);
        // Capturar ID do processo para cleanup
        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descProcMapeamento).first().click();

    });

    test('Fase 1.2: CHEFE adiciona atividades e conhecimentos', async ({page}) => {
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefeDireto(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Adicionar Atividade e Conhecimento usando helpers
        const descAtividade = `Atividade Teste ${timestamp}`;
        await adicionarAtividade(page, descAtividade);
        await adicionarConhecimento(page, descAtividade, 'Conhecimento Teste');
    });

    test('Fase 1.3: CHEFE disponibiliza cadastro', async ({page}) => {
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaAtividades(page);
        await disponibilizarCadastro(page);

        // Validação: helper já confirma toast atual e redirecionamento para o painel
    });

    test('Fase 1.3b: GESTOR da SECRETARIA_2 registra aceite', async ({page}) => {
        // George Harrison (212121) é Gestor da SECRETARIA_2
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page, 'Aceite intermediário');
    });

    test('Fase 1.4: ADMIN homologa cadastro', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroMapeamento(page);
    });

    test('Fase 1.5: ADMIN adiciona competências e disponibiliza mapa', async ({page, autenticadoComoAdmin}) => {

        await acessarSubprocessoAdmin(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await criarCompetencia(page, `Competência Teste ${timestamp}`, [`Atividade Teste ${timestamp}`]);
        await disponibilizarMapa(page, '2030-12-31');
        await verificarPaginaPainel(page);
    });

    test('Fase 1.6: CHEFE valida mapa', async ({page, autenticadoComoAdmin}) => {
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Fase 1.6b: GESTOR da SECRETARIA_2 aceita validação do mapa', async ({page}) => {
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Fase 1.7: ADMIN homologa e finaliza processo', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        await page.goto('/painel');
        await page.getByTestId('tbl-processos').getByText(descProcMapeamento).first().click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Fase 2: Iniciar processo de Revisão', async ({page, autenticadoComoAdmin}) => {
        // O login como Admin já foi disparado pela fixture 'autenticadoComoAdmin'
        // Se houver dúvida se a fixture trocou de usuário em um describe.serial,
        // podemos chamar o helper explicitamente para garantir a navegação para /login.
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        // Garante que está no painel e o perfil Admin foi carregado
        await verificarPaginaPainel(page);
        await expect(page.getByTestId('btn-painel-criar-processo')).toBeVisible();

        // Criar processo de REVISÃO
        await criarProcesso(page, {
            descricao: descProcRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Capturar ID do processo para cleanup
        await page.getByTestId('tbl-processos').getByText(descProcRevisao).first().click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);

        const dataLimiteStr = (await page.getByTestId('inp-processo-data-limite').inputValue()).split('-').reverse().join('/');

        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descProcRevisao);
        await page.getByTestId('btn-processo-iniciar').click();

        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText('Ao iniciar o processo, não será mais possível editá-lo')).toBeVisible();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // Validação: Redirecionamento e situação do processo iniciado
        await verificarPaginaPainel(page);
        await verificarProcessoNaTabela(page, {
            descricao: descProcRevisao,
            situacao: 'Em andamento',
            tipo: 'Revisão'
        });

        // Entrar no processo para verificar se a revisão criou o subprocesso com status "Não iniciado"
        const linhaProcessoRevisao = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcRevisao)});
        await linhaProcessoRevisao.click();

        // Verifica status e data limite na tabela de participantes (Step 9)
        const linhaSubprocesso = page.locator('tr', {hasText: UNIDADE_ALVO}).first();
        await expect(linhaSubprocesso).toContainText('Não iniciado');
        await expect(linhaSubprocesso).toContainText(dataLimiteStr);

        // Entra no subprocesso
        await linhaSubprocesso.click();

        // Verifica status no header do subprocesso
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText('Não iniciado');

        // Verifica movimentação (Step 11)
        const timeline = page.getByTestId('tbl-movimentacoes');
        await expect(timeline.getByText(/Processo iniciado/i).first()).toBeVisible();
    });

    test('Fase 2.1: Verificar alertas do processo de Revisão', async ({page}) => {
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await verificarPaginaPainel(page);
        
        const tabelaAlertasChefe = page.getByTestId('tbl-alertas');
        await expect(tabelaAlertasChefe.locator('tr', {hasText: descProcRevisao})
            .filter({hasText: 'Início do processo'})
            .filter({hasNotText: 'subordinada'})
        ).toBeVisible();

        // 2. Verificar Alerta para o GESTOR da SECRETARIA_2 (Unidade Intermediária - Step 13.2)
        // George Harrison (212121) é Gestor da SECRETARIA_2
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');
        await verificarPaginaPainel(page);

        const tabelaAlertasGestor = page.getByTestId('tbl-alertas');
        await expect(tabelaAlertasGestor.locator('tr', {hasText: descProcRevisao})
            .filter({hasText: 'Início do processo em unidade(s) subordinada(s)'})
        ).toBeVisible();
    });

    test('Fase 3: CHEFE verifica atividades copiadas na Revisão', async ({page}) => {
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);
        await acessarSubprocessoChefeDireto(page, descProcRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // Verifica que a atividade criada na fase de Mapeamento foi copiada corretamente (Step 10)
        const descAtividade = `Atividade Teste ${timestamp}`;
        await expect(page.getByText(descAtividade).first()).toBeVisible();

        // Conhecimentos são exibidos dentro do card da atividade no componente AtividadeItem
        // Não há necessidade de clicar em um botão de expandir
        await expect(page.getByText('Conhecimento Teste').first()).toBeVisible();
    });
});
