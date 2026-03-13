import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    criarProcesso,
    extrairProcessoCodigo,
    verificarDetalhesProcesso,
    verificarUnidadeParticipante
} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaAtividades
} from './helpers/helpers-atividades.js';
import {acessarSubprocessoChefeDireto} from './helpers/helpers-analise.js';
import {
    esperarPaginaDetalhesProcesso,
    navegarParaSubprocesso,
    verificarPaginaPainel
} from './helpers/helpers-navegacao.js';

test.describe('CDU-06 - Detalhar processo', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_12';

    test('Fase 1: Deve exibir detalhes do processo para ADMIN e ações de unidade', async ({page, autenticadoComoAdmin}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-06 ${timestamp}`;

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });

        // Capturar ID do processo para cleanup (padrão CDU-04/05)
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await esperarPaginaDetalhesProcesso(page);

        await verificarDetalhesProcesso(page, {
            descricao,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });

        await verificarUnidadeParticipante(page, {
            sigla: 'ASSESSORIA_12',
            situacao: 'Não iniciado',
            dataLimite: '/'
        });

        // 5. [Step 2.2.1] ADMIN deve ver elementos de alteração ao entrar no subprocesso
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        
        // Botão "Alterar data limite" deve estar visível para Admin
        await expect(page.getByTestId('btn-alterar-data-limite')).toBeVisible();
        
        // Botão "Reabrir cadastro" NÃO deve estar visível pois a situação é "Não iniciado" 
        // (Regra: requer situação >= MAPEAMENTO_MAPA_HOMOLOGADO)
        await expect(page.getByTestId('btn-reabrir-cadastro')).toBeHidden();
        
        // Botão "Enviar lembrete" deve estar visível
        await expect(page.getByTestId('btn-enviar-lembrete')).toBeVisible();
    });

    test('Fase 1b: Deve exibir detalhes do processo para GESTOR e ocultar ações ADMIN', async ({page}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-06 Gestor ${timestamp}`;
        const UNIDADE_PROCESSO = 'ASSESSORIA_21'; // Subordinada à SECRETARIA_2 (George harrison)

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_PROCESSO,
            expandir: ['SECRETARIA_2'],
            iniciar: true
        });

        // Capturar ID para cleanup
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await esperarPaginaDetalhesProcesso(page);
        const codProcesso = await extrairProcessoCodigo(page);

        await page.getByTestId('btn-logout').click();

        // George harrison (212121) é Gestor da SECRETARIA_2
        await loginComPerfil(page, '212121', 'senha', 'GESTOR - SECRETARIA_2');

        // Aguardar que o processo apareça no painel
        await expect(page.getByTestId('tbl-processos').getByRole('row', {name: descricao})).toBeVisible();
        await page.getByTestId('tbl-processos').getByRole('row', {name: descricao}).click();

        await esperarPaginaDetalhesProcesso(page, codProcesso);

        await verificarDetalhesProcesso(page, {
            descricao,
            tipo: 'Mapeamento',
            situacao: 'Em andamento'
        });

        // GESTOR não vê Finalizar processo
        await expect(page.getByTestId('btn-processo-finalizar')).toBeHidden();

        // GESTOR não vê ações de alteração administrativa no subprocesso
        await navegarParaSubprocesso(page, UNIDADE_PROCESSO);
        await expect(page.getByTestId('btn-alterar-data-limite')).toBeHidden();
        await expect(page.getByTestId('btn-reabrir-cadastro')).toBeHidden();
    });

    test('Fase 2: Verificar botões de ação em bloco [Step 2.2.2]', async ({page}) => {
        const timestamp = Date.now();
        const descricao = `Bloco CDU-06 ${timestamp}`;
        const UNIDADE_SUB = 'ASSESSORIA_12';

        // 1. ADMIN cria processo
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_SUB,
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });
        
        // Capturar ID para cleanup
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await esperarPaginaDetalhesProcesso(page);
        const codProcesso = await extrairProcessoCodigo(page);

        // 2. CHEFE disponibiliza cadastro para habilitar ações em bloco
        await login(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
        await acessarSubprocessoChefeDireto(page, descricao, UNIDADE_SUB);
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Atividade bloco ${timestamp}`);
        await adicionarConhecimento(page, `Atividade bloco ${timestamp}`, 'Conhecimento bloco');
        await disponibilizarCadastro(page);
        await verificarPaginaPainel(page);

        // 3. ADMIN verifica botão "Homologar em bloco"
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await esperarPaginaDetalhesProcesso(page, codProcesso);
        await expect(page.getByRole('button', {name: 'Homologar em bloco'})).toBeVisible();

        // 4. GESTOR verifica botão "Aceitar cadastro em bloco"
        // John lennon (202020) é Gestor da SECRETARIA_1 (que engloba ASSESSORIA_12)
        await loginComPerfil(page, '202020', 'senha', 'GESTOR - SECRETARIA_1');
        await page.getByTestId('tbl-processos').getByText(descricao).first().click();
        await esperarPaginaDetalhesProcesso(page, codProcesso);
        await expect(page.getByRole('button', {name: 'Aceitar cadastro em bloco'})).toBeVisible();
    });
});
