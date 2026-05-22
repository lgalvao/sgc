import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoMapaDisponibilizadoFixture, validarProcessoFixture} from './fixtures/index.js';
import {
    abrirDevolucaoMapa,
    abrirSugestoesMapa,
    abrirValidacaoMapa,
    esperarMapaSomenteLeitura,
    navegarParaMapa
} from './helpers/helpers-mapas.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {acessarSubprocessoGestor} from './helpers/helpers-analise.js';
import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

test.describe.serial('CDU-19 - Validar mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-19 ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        validarProcessoFixture(processo, descProcesso);
    });

    // TESTES PRINCIPAIS - CDU-19

    test('Cenários CDU-19: Fluxo completo de validação do mapa pelo CHEFE', async ({
                                                                                       _resetAutomatico,
                                                                                       page,
                                                                                       _autenticadoComoChefeSecao221
                                                                                   }) => {
        // Cenario 1: Navegação para visualização do mapa
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await acessarDetalhesProcesso(page, descProcesso);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);

        await navegarParaMapa(page);
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();

        // Cenario 2: Cancelar validação
        await abrirValidacaoMapa(page);
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(modal.getByText(/Confirma a validação/i)).toBeVisible();

        await page.getByTestId('btn-validar-mapa-cancelar').click();
        await expect(page.getByText('Mapa de competências técnicas')).toBeVisible();
        await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();

        // Cenario 3: Validar com sucesso
        await abrirValidacaoMapa(page);
        await expect(modal).toBeVisible();
        await page.getByTestId('btn-validar-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarNotificacaoAdmin(page, {
            destinatario: 'COORD_22',
            assunto: `Validação do mapa de competências da ${UNIDADE_ALVO} submetida para análise`,
            tipo: 'Mapa validado',
            trechoCorpo: `A unidade ${UNIDADE_ALVO} validou o mapa de competências elaborado no processo ${descProcesso}.`
        });
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarDetalhesProcesso(page, descProcesso);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa validado/i);

        // CDU-19 Passo 5.4/5.5: verificar movimentação registrada no subprocesso com data/hora
        await navegarParaSubprocesso(page, UNIDADE_ALVO);
        const linhaMovimentacao = page.getByTestId('tbl-movimentacoes')
            .locator('tr', {hasText: /Mapa validado/i})
            .first();
        await expect(linhaMovimentacao).toBeVisible();
        await expect(linhaMovimentacao).toContainText(/\d{2}\/\d{2}\/\d{4}/);
        await expect(linhaMovimentacao).toContainText(/SECAO_221/i);
    });
});

test.describe.serial('CDU-19 - Apresentar sugestões e pré-preenchimento', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const GESTOR_SUPERIOR = USUARIOS.GESTOR_COORD_22;

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-19 Sugestoes ${timestamp}`;
    const TEXTO_SUGESTAO = 'Sugestão de ajuste na competência técnica';

    test('Setup data', async ({_resetAutomatico, request}) => {
        await resetDatabase(request);
        const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('Cenario 1: CHEFE apresenta sugestões com sucesso', async ({
                                                                        _resetAutomatico,
                                                                        page,
                                                                        _autenticadoComoChefeSecao221
                                                                    }) => {
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await acessarDetalhesProcesso(page, descProcesso);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();

        // Modal abre sem pré-preenchimento (mapa novo, sem sugestões anteriores)
        await abrirSugestoesMapa(page);
        const modal = page.getByRole('dialog');
        await expect(modal).toBeVisible();
        await expect(page.getByTestId('inp-sugestoes-mapa-texto')).toHaveText('');

        // Preenche e confirma
        await page.getByTestId('inp-sugestoes-mapa-texto').fill(TEXTO_SUGESTAO);
        await page.getByTestId('btn-sugestoes-mapa-confirmar').click();

        await verificarPaginaPainel(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarNotificacaoAdmin(page, {
            destinatario: 'COORD_22',
            assunto: `Sugestões apresentadas para o mapa de competências da ${UNIDADE_ALVO}`,
            tipo: 'Sugestões apresentadas para o mapa',
            trechoCorpo: `A unidade ${UNIDADE_ALVO} apresentou sugestões para o mapa de competências elaborado no processo ${descProcesso}.`
        });
        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarDetalhesProcesso(page, descProcesso);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa com sugestões/i);
    });

    test('Cenario 1b: GESTOR superior vê alerta de sugestões no painel', async ({_resetAutomatico, page}) => {
        // CDU-19 Passo 4.5: sistema cria alerta para unidade superior (COORD_22 acima de SECAO_221)
        await login(page, GESTOR_SUPERIOR.titulo, GESTOR_SUPERIOR.senha);

        const tabelaAlertas = page.getByTestId('tbl-alertas');
        const linhaAlerta = tabelaAlertas.locator('tr', {hasText: descProcesso}).first();
        await expect(linhaAlerta).toBeVisible();
        await expect(linhaAlerta).toContainText(/SECAO_221/i);
        await expect(linhaAlerta).toContainText(/\d{2}\/\d{2}\/\d{4}/);
    });

    test('Cenario 2: GESTOR devolve mapa para ajustes', async ({_resetAutomatico, page}) => {
        await login(page, GESTOR_SUPERIOR.titulo, GESTOR_SUPERIOR.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
        await abrirDevolucaoMapa(page);
        await page.getByTestId('inp-devolucao-mapa-obs').fill('Necessário rever competências');
        await page.getByTestId('btn-devolucao-mapa-confirmar').click();

        await verificarPaginaPainel(page);

        await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
        await acessarDetalhesProcesso(page, descProcesso);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
        await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
        await navegarParaMapa(page);
        await esperarMapaSomenteLeitura(page);
        await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();
        await abrirSugestoesMapa(page);
        await expect(page.getByTestId('inp-sugestoes-mapa-texto')).toContainText(TEXTO_SUGESTAO);
        await page.getByTestId('btn-sugestoes-mapa-cancelar').click();
    });

    test('Cenario 3: CHEFE reabre modal com pré-preenchimento das sugestões anteriores', async ({
                                                                                                    _resetAutomatico,
                                                                                                    page,
                                                                                                    _autenticadoComoChefeSecao221
                                                                                                }) => {
        await expect(page.getByTestId('tbl-processos').getByText(descProcesso).first()).toBeVisible();
        await acessarDetalhesProcesso(page, descProcesso);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Mapa disponibilizado/i);
        await navegarParaMapa(page);

        await expect(page.getByTestId('btn-mapa-acoes')).toBeVisible();

        await abrirSugestoesMapa(page);
        await expect(page.getByTestId('inp-sugestoes-mapa-texto')).toContainText(TEXTO_SUGESTAO);

        // Cancela sem alterar o estado
        await page.getByTestId('btn-sugestoes-mapa-cancelar').click();
    });
});
