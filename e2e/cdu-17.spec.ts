import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso, extrairProcessoId} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {aceitarCadastroMapeamento, acessarSubprocessoGestor} from './helpers/helpers-analise.js';
import {loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';

test.describe.serial('CDU-17 - Disponibilizar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-17 ${timestamp}`;
    let processoId = 0;

    // Atividades e competências para os testes
    const atividade1 = `Atividade 1 ${timestamp}`;
    const atividade2 = `Atividade 2 ${timestamp}`;
    const atividade3 = `Atividade 3 ${timestamp}`;
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;

    // PREPARAÇÃO - Criar mapa pronto para disponibilização

    test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({
                                                                                page,
                                                                                autenticadoComoAdmin,
                                                                                cleanupAutomatico
                                                                            }) => {
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        processoId = await extrairProcessoId(page);
        if (processoId > 0) cleanupAutomatico.registrar(processoId);

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();

        await verificarPaginaPainel(page);
    });

    test('Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro', async ({
                                                                                        page,
                                                                                        autenticadoComoChefeSecao211
                                                                                    }) => {
        await page.goto(`/processo/${processoId}/${UNIDADE_ALVO}`);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, atividade1);
        await adicionarConhecimento(page, atividade1, 'Conhecimento 1A');

        await adicionarAtividade(page, atividade2);
        await adicionarConhecimento(page, atividade2, 'Conhecimento 2A');

        await adicionarAtividade(page, atividade3);
        await adicionarConhecimento(page, atividade3, 'Conhecimento 3A');

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        await expect(page.getByText(/Cadastro de atividades disponibilizado/i).first()).toBeVisible();
        await verificarPaginaPainel(page);
    });

    test('Preparacao 3: Gestores aceitam cadastro', async ({page, autenticadoComoGestorCoord21}) => {
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);
    });

    test('Preparacao 4: Admin homologa cadastro e cria competências', async ({page, autenticadoComoAdmin}) => {
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_211');
        await navegarParaAtividadesVisualizacao(page);
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);

        await navegarParaMapa(page);
        await criarCompetencia(page, competencia1, [atividade1, atividade2]);
        await criarCompetencia(page, competencia2, [atividade3]);

        await expect(page.getByText(competencia1)).toBeVisible();
        await expect(page.getByText(competencia2)).toBeVisible();
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();
    });

    // TESTES PRINCIPAIS - CDU-17

    test('Cenários CDU-17: Fluxo completo de disponibilização do mapa pelo ADMIN', async ({
                                                                                              page,
                                                                                              autenticadoComoAdmin
                                                                                          }) => {
        // Cenario 1: Navegação
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
        await navegarParaSubprocesso(page, 'SECAO_211');
        await navegarParaMapa(page);

        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeVisible();

        // Cenario 2: Abrir modal
        await page.getByTestId('btn-cad-mapa-disponibilizar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeVisible();
        await expect(page.getByText('Disponibilização do mapa')).toBeVisible();

        // Cenario 3: Cancelar
        await page.getByTestId('btn-disponibilizar-mapa-cancelar').click();
        await expect(page.getByTestId('mdl-disponibilizar-mapa')).toBeHidden();
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();

        // Cenario 4: Disponibilizar com sucesso
        await disponibilizarMapa(page, '2030-12-31');
        await verificarPaginaPainel(page);
        await expect(page.getByText(/Mapa disponibilizado/i).first()).toBeVisible();
    });
});
