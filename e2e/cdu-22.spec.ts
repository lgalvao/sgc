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
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();

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

    test('Cenário 4: Validar isolamento hierárquico no aceite em bloco (gestor da secretaria só aceita em bloco quando estiver na sua mesa)', async ({page}) => {
        const timestamp2 = Date.now();
        const descProcesso2 = `Mapeamento Erro Hierarquia CDU-22 ${timestamp2}`;

        // 1. Admin cria e inicia processo selecionando ASSESSORIA_11 e SECAO_111
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao: descProcesso2,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: ['ASSESSORIA_11', 'SECAO_111'],
            expandir: ['SECRETARIA_1', 'COORD_11'],
            iniciar: true
        });

        // 2. Chefe ASSESSORIA_11 (David Bowie) disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_ASSESSORIA_11.titulo, USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso2).first().click();
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Ativ ASSESSORIA_11 ${timestamp2}`);
        await adicionarConhecimento(page, `Ativ ASSESSORIA_11 ${timestamp2}`, 'Conhecimento 11');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);

        // 3. Chefe SECAO_111 (Chefe Seção 111) disponibiliza cadastro
        await login(page, USUARIOS.CHEFE_SECAO_111.titulo, USUARIOS.CHEFE_SECAO_111.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso2).first().click();
        await navegarParaAtividades(page);
        await adicionarAtividade(page, `Ativ SECAO_111 ${timestamp2}`);
        await adicionarConhecimento(page, `Ativ SECAO_111 ${timestamp2}`, 'Conhecimento 111');
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
        await verificarPaginaPainel(page);

        // 4. Logar como GESTOR SECRETARIA_1 e validar o modal de aceite em bloco
        // Ele deve ver a ASSESSORIA_11 (subordinada direta)
        // Ele NÃO deve ver a SECAO_111 (está na mesa da COORD_11)
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await page.getByTestId('tbl-processos').getByText(descProcesso2).first().click();

        const btnAceitar = page.getByTestId('btn-processo-aceitar-bloco');
        await expect(btnAceitar).toBeVisible();
        await expect(btnAceitar).toBeEnabled();
        await btnAceitar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toBeVisible();

        // Validação CRÍTICA: ASSESSORIA_11 deve estar presente, SECAO_111 deve estar AUSENTE
        await expect(modal.getByText('ASSESSORIA_11')).toBeVisible();
        await expect(modal.getByText('SECAO_111')).toBeHidden();

        await modal.getByRole('button', {name: /Cancelar/i}).click();

        // 5. Gestor COORD_11 (GESTOR_COORD) faz o aceite em bloco da SECAO_111
        await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
        await page.getByTestId('tbl-processos').getByText(descProcesso2).first().click();
        
        await page.getByTestId('btn-processo-aceitar-bloco').click();
        await page.locator('#modal-acao-bloco').getByRole('button', {name: /Registrar aceite/i}).click();
        await expect(page.getByText(/Cadastros aceitos em bloco/i).first()).toBeVisible();

        // 6. Logar novamente como GESTOR SECRETARIA_1
        // Agora ele deve ver AMBAS as unidades no modal de aceite em bloco
        await loginComPerfil(page, USUARIOS.GESTOR_SECRETARIA_1.titulo, USUARIOS.GESTOR_SECRETARIA_1.senha, 'GESTOR - SECRETARIA_1');
        await page.getByTestId('tbl-processos').getByText(descProcesso2).first().click();

        await page.getByTestId('btn-processo-aceitar-bloco').click();
        const modalFinal = page.locator('#modal-acao-bloco');
        await expect(modalFinal).toBeVisible();

        // Agora ambas devem estar visíveis
        await expect(modalFinal.getByText('ASSESSORIA_11')).toBeVisible();
        await expect(modalFinal.getByText('SECAO_111')).toBeVisible();

        // Registrar aceite em bloco pela SECRETARIA_1
        await modalFinal.getByRole('button', {name: /Registrar aceite/i}).click();
        await expect(page.getByText(/Cadastros aceitos em bloco/i).first()).toBeVisible();
    });
});
