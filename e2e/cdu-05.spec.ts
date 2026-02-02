import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso, verificarProcessoNaTabela} from './helpers/helpers-processos.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    homologarCadastroMapeamento,
} from './helpers/helpers-analise.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades,
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

    // ========================================================================
    // PASSOS DE PREPARAÇÃO - PROCESSO DE MAPEAMENTO
    // ========================================================================

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

        const linhaProcesso = page.locator('tr', {has: page.getByText(descricao)});
        await linhaProcesso.click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        await expect(page.getByTestId('inp-processo-descricao')).toHaveValue(descricao);

        // Iniciar processo
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


    // ========================================================================
    // TESTE PRINCIPAL
    // ========================================================================

    test('Fase 1.1: ADMIN cria e inicia processo de Mapeamento', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        await passo1_AdminCriaEIniciaProcessoMapeamento(page, descProcMapeamento);
        // Capturar ID do processo para cleanup
        await page.goto('/painel');
        await page.getByText(descProcMapeamento).first().click();
        const processoMapeamentoId = Number.parseInt(page.url().match(/\/processo\/(\d+)/)?.[1] || '0');
        if (processoMapeamentoId > 0) cleanupAutomatico.registrar(processoMapeamentoId);
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

        // Validação: Mensagem de sucesso e redirecionamento para o painel
        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Fase 1.4: ADMIN homologa cadastro', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcMapeamento, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
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

    test('Fase 1.7: ADMIN homologa e finaliza processo', async ({page}) => {
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await acessarSubprocessoAdmin(page, descProcMapeamento, UNIDADE_ALVO);
        await navegarParaMapa(page);
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await verificarPaginaPainel(page);

        await page.goto('/painel');
        await page.getByText(descProcMapeamento).first().click();
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await verificarPaginaPainel(page);
    });

    test('Fase 2: Iniciar processo de Revisão', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        // Login as Admin
        

        // Criar processo de REVISÃO
        await criarProcesso(page, {
            descricao: descProcRevisao,
            tipo: 'REVISAO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2']
        });

        // Capturar ID do processo para cleanup
        await page.getByText(descProcRevisao).click();
        await expect(page).toHaveURL(/\/processo\/cadastro/);
        const processoRevisaoId = Number.parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
        if (processoRevisaoId > 0) cleanupAutomatico.registrar(processoRevisaoId);

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
    });
});