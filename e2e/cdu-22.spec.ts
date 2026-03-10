import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {adicionarAtividade, adicionarConhecimento, navegarParaAtividades} from './helpers/helpers-atividades.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';

/**
 * CDU-22 - Aceitar cadastros em bloco
 *
 * Ator: GESTOR
 *
 * Pré-condições:
 * - Processo de mapeamento ou revisão com unidades subordinadas
 * - Subprocessos na situação 'Cadastro disponibilizado'
 *
 * Fluxo principal:
 * 1. No Painel, GESTOR acessa processo em andamento
 * 2. Sistema mostra tela Detalhes do processo
 * 3. Sistema identifica unidades elegíveis e exibe botão de aceite em bloco
 * 4. GESTOR clica no botão 'Aceitar em Bloco'
 * 5. Sistema abre modal com lista de unidades selecionáveis
 * 6. GESTOR seleciona unidades e confirma
 * 7. Sistema executa aceite para cada unidade selecionada
 */
test.describe.serial('CDU-22 - Aceitar cadastros em bloco', () => {
    const UNIDADE_1 = 'SECAO_221';
    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-22 ${timestamp}`;

    const atividade1 = `Atividade Bloco ${timestamp}`;

    test('Cria processo, cadastra atividades e disponibiliza cadastro', async ({page}) => {
        // Preparacao 1: Admin cria e inicia processo de mapeamento
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_1,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);

        // Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);

        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento Bloco 1');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await verificarPaginaPainel(page);
    });

    test('Cenario 1: GESTOR abre modal e cancela aceite em bloco', async ({page, autenticadoComoGestorCoord22}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();

        const btnAceitar = page.getByTestId('btn-processo-aceitar-bloco');
        await expect(btnAceitar).toBeVisible();
        await expect(btnAceitar).toBeEnabled();
        await btnAceitar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await expect(modal.getByText(/Aceite de cadastro em bloco/i)).toBeVisible();
        await expect(modal.getByText(/Selecione as unidades cujos cadastros deverão ser aceitos/i)).toBeVisible();
        await expect(modal.locator('table')).toBeVisible();
        await modal.getByRole('button', {name: /Cancelar/i}).click();

        await expect(modal).not.toHaveClass(/show/);
        await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    });

    test('Cenario 3: Botão respeita localização atual (fica desabilitado para gestor superior se o item estiver com o subordinado)', async ({
                                                                                              page,
                                                                                              autenticadoComoGestorSecretaria2,
                                                                                              autenticadoComoGestorCoord22
                                                                                          }) => {
        // 1. GESTOR SECRETARIA_2 (Superior de COORD_22) acessa o processo
        // Nesse momento, o subprocesso da SECAO_221 está com o GESTOR COORD_22 (Cadastro disponibilizado)
        // Portanto, o botão DEVE estar visível (é Gestor) mas DESABILITADO (nada na sua mesa)
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        const btnAceitarSec = page.getByTestId('btn-processo-aceitar-bloco');
        await expect(btnAceitarSec).toBeVisible();
        await expect(btnAceitarSec).toBeDisabled();

        // 2. Agora o GESTOR COORD_22 faz o aceite
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await page.getByTestId('btn-processo-aceitar-bloco').click();
        await page.locator('#modal-acao-bloco').getByRole('button', {name: /Registrar aceite/i}).click();
        await expect(page.getByText(/Cadastros aceitos em bloco/i).first()).toBeVisible();

        // 3. Agora o GESTOR SECRETARIA_2 acessa novamente. O item deve estar na sua mesa.
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

        const btnAceitarSecHabilitado = page.getByTestId('btn-processo-aceitar-bloco');
        await expect(btnAceitarSecHabilitado).toBeVisible();
        await expect(btnAceitarSecHabilitado).toBeEnabled();
    });

    test('Cenario 4: Múltiplas unidades disponibilizadas, botão desabilitado para gestor de 2 níveis acima', async ({page}) => {
        const timestamp2 = Date.now();
        const descProcesso2 = `Mapeamento Multi CDU-22 ${timestamp2}`;
        
        // 1. Admin cria processo com SECAO_221 e SECAO_211
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descProcesso2,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'SECAO_221',
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        // Expansão manual adicional para garantir visibilidade da SECAO_211
        // SECRETARIA_2 -> COORD_21 -> SECAO_211
        await page.getByTestId('btn-arvore-expand-COORD_21').click();
        await page.getByTestId('chk-unidade-SECAO_211').click();

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso2)});
        await linhaProcesso.click();
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        // 2. Chefes de ambas unidades disponibilizam
        const chefes = [
            {user: USUARIOS.CHEFE_SECAO_221, atividade: `Ativ 221 ${timestamp2}`},
            {user: USUARIOS.CHEFE_SECAO_211, atividade: `Ativ 211 ${timestamp2}`}
        ];

        for (const chefe of chefes) {
            await login(page, chefe.user.titulo, chefe.user.senha);
            await page.getByTestId('tbl-processos').getByText(descProcesso2).first().click();
            await navegarParaAtividades(page);
            await adicionarAtividade(page, chefe.atividade);
            await adicionarConhecimento(page, chefe.atividade, 'Conhecimento Multi');
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await verificarPaginaPainel(page);
        }

        // 3. Logar como GESTOR SECRETARIA_2. O botão deve estar visível mas DESABILITADO
        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await page.getByTestId('tbl-processos').getByText(descProcesso2).first().click();

        const btnAceitarSec = page.getByTestId('btn-processo-aceitar-bloco');
        await expect(btnAceitarSec).toBeVisible();
        await expect(btnAceitarSec).toBeDisabled();
    });
});
