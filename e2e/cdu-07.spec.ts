import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcessoCadastroDisponibilizadoFixture,
    criarProcessoCadastroHomologadoFixture,
    criarProcessoFinalizadoFixture,
    criarProcessoFixture,
    criarProcessoMapaDisponibilizadoFixture
} from './fixtures/index.js';
import {autenticar, login, USUARIOS} from './helpers/helpers-auth.js';
import {
    esperarPaginaDetalhesProcesso,
    esperarPaginaSubprocesso,
    fazerLogout,
    limparNotificacoes,
    navegarParaSubprocesso
} from './helpers/helpers-navegacao.js';
import {
    acessarDetalhesProcesso,
    criarProcesso,
    extrairProcessoCodigo,
    verificarDetalhesSubprocesso
} from './helpers/helpers-processos.js';

function converterDataHoraBrParaTimestamp(dataHoraTexto: string): number {
    const textoNormalizado = dataHoraTexto.trim().replaceAll(/\s+/g, ' ');
    const correspondencia = new RegExp(/^(\d{2})\/(\d{2})\/(\d{4})(?: (\d{2}):(\d{2})(?::(\d{2}))?)?$/).exec(textoNormalizado);

    if (!correspondencia) {
        throw new Error(`Data/hora inválida na tabela de movimentações: "${dataHoraTexto}"`);
    }

    const [, dia, mes, ano, hora = '00', minuto = '00', segundo = '00'] = correspondencia;
    return new Date(
        Number(ano),
        Number(mes) - 1,
        Number(dia),
        Number(hora),
        Number(minuto),
        Number(segundo)
    ).getTime();
}

test.describe('CDU-07 - Detalhar subprocesso', () => {
    const UNIDADE_ALVO = 'SECAO_211';
    const NOME_UNIDADE_ALVO = 'Seção 211';
    const UNIDADE_ALVO_2 = 'SECAO_212';
    const UNIDADE_ALVO_3 = 'SECAO_221';

    test('Deve exibir detalhes do subprocesso em mapeamento para ADMIN, GESTOR, CHEFE e SERVIDOR', async ({
                                                                                                              _resetAutomatico,
                                                                                                              _autenticadoComoAdmin,
                                                                                                              request,
                                                                                                              page
                                                                                                          }) => {
        const processo = await criarProcessoFixture(request, {
            unidade: UNIDADE_ALVO,
            iniciar: true,
            descricao: `Fixture CDU-07 PERFIS ${Date.now()}`,
            diasLimite: 30
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processo.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processo.descricao);
        await esperarPaginaDetalhesProcesso(page, processo.codigo);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            nomeUnidade: NOME_UNIDADE_ALVO,
            situacao: 'Não iniciado',
            localizacao: UNIDADE_ALVO,
            titular: 'Debbie Harry',
            ramalTitular: '2015',
            emailTitular: 'debbie.harry@tre-pe.jus.br'
        });

        await expect(page.getByRole('heading', {name: 'Movimentações'})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Data/hora'})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Origem'})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Destino'})).toBeVisible();
        await expect(page.getByRole('columnheader', {name: 'Descrição'})).toBeVisible();
        const linhasMovimentacao = page.getByTestId('tbl-movimentacoes').locator('tbody tr');
        await expect(linhasMovimentacao).not.toHaveCount(0);

        const totalLinhasMovimentacao = await linhasMovimentacao.count();
        const datasMovimentacao: number[] = [];

        for (let i = 0; i < totalLinhasMovimentacao; i++) {
            const dataHoraLinha = await linhasMovimentacao.nth(i).locator('td').first().innerText();
            datasMovimentacao.push(converterDataHoraBrParaTimestamp(dataHoraLinha));
        }

        for (let i = 0; i < datasMovimentacao.length - 1; i++) {
            expect(datasMovimentacao[i]).toBeGreaterThanOrEqual(datasMovimentacao[i + 1]);
        }

        const cardAtividadesAdmin = page.getByTestId('card-subprocesso-atividades');
        await expect(cardAtividadesAdmin).toBeVisible();
        await expect(cardAtividadesAdmin).toHaveClass(/card-disabled/);
        await expect(cardAtividadesAdmin).not.toHaveAttribute('role', 'button');
        await expect(cardAtividadesAdmin).toContainText('Cadastro de atividades e conhecimentos da unidade');

        const cardMapaAdmin = page.getByTestId('card-subprocesso-mapa-desabilitado');
        await expect(cardMapaAdmin).toBeVisible();
        await expect(cardMapaAdmin).toHaveClass(/card-disabled/);

        await fazerLogout(page);
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processo.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processo.descricao);
        await esperarPaginaDetalhesProcesso(page, processo.codigo);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        const cardAtividadesGestor = page.getByTestId('card-subprocesso-atividades');
        await expect(cardAtividadesGestor).toBeVisible();
        await expect(cardAtividadesGestor).toHaveClass(/card-disabled/);
        await expect(cardAtividadesGestor).toContainText('Cadastro de atividades e conhecimentos da unidade');

        const cardMapaGestor = page.getByTestId('card-subprocesso-mapa-desabilitado');
        await expect(cardMapaGestor).toBeVisible();
        await expect(cardMapaGestor).toHaveClass(/card-disabled/);

        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processo.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processo.descricao);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO);

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            nomeUnidade: NOME_UNIDADE_ALVO,
            situacao: 'Não iniciado',
            localizacao: UNIDADE_ALVO,
            titular: 'Debbie Harry'
        });

        const cardAtividadesChefe = page.getByTestId('card-subprocesso-atividades');
        await expect(cardAtividadesChefe).toBeVisible();
        await expect(cardAtividadesChefe).toHaveClass(/card-actionable/);
        await expect(cardAtividadesChefe).toHaveAttribute('role', 'button');

        const cardMapaChefe = page.getByTestId('card-subprocesso-mapa-desabilitado');
        await expect(cardMapaChefe).toBeVisible();
        await expect(cardMapaChefe).toHaveClass(/card-disabled/);

        await fazerLogout(page);
        await login(page, USUARIOS.SERVIDOR_SECAO_211.titulo, USUARIOS.SERVIDOR_SECAO_211.senha);
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processo.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processo.descricao);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO);

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            nomeUnidade: NOME_UNIDADE_ALVO,
            situacao: 'Não iniciado',
            localizacao: UNIDADE_ALVO,
            titular: 'Debbie Harry'
        });

        const cardAtividadesServidor = page.getByTestId('card-subprocesso-atividades');
        await expect(cardAtividadesServidor).toBeVisible();
        await expect(cardAtividadesServidor).toHaveClass(/card-disabled/);
        await expect(cardAtividadesServidor).toContainText('Cadastro de atividades e conhecimentos da unidade');

        const cardMapaServidor = page.getByTestId('card-subprocesso-mapa-desabilitado');
        await expect(cardMapaServidor).toBeVisible();
        await expect(cardMapaServidor).toHaveClass(/card-disabled/);
    });

    test('Deve habilitar os cards conforme o avanço do subprocesso', async ({
                                                                                _resetAutomatico,
                                                                                _autenticadoComoAdmin,
                                                                                request,
                                                                                page
                                                                            }) => {
        const processoCadastroDisponibilizado = await criarProcessoCadastroDisponibilizadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: `Fixture CDU-07 CAD DISP ${Date.now()}`
        });
        const processoCodigo = processoCadastroDisponibilizado.codigo;

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processoCadastroDisponibilizado.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processoCadastroDisponibilizado.descricao);
        await esperarPaginaDetalhesProcesso(page, processoCodigo);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        const cardAtividadesAdmin = page.getByTestId('card-subprocesso-atividades');
        await expect(cardAtividadesAdmin).toBeVisible();
        await expect(cardAtividadesAdmin).toHaveClass(/card-actionable/);
        await expect(cardAtividadesAdmin).toHaveAttribute('role', 'button');
        await expect(cardAtividadesAdmin).toContainText('Cadastro de atividades e conhecimentos da unidade');

        const cardMapaAdminBloqueado = page.getByTestId('card-subprocesso-mapa-desabilitado');
        await expect(cardMapaAdminBloqueado).toBeVisible();
        await expect(cardMapaAdminBloqueado).toHaveClass(/card-disabled/);

        const linhasMovimentacao = page.getByTestId('tbl-movimentacoes').locator('tbody tr');
        await expect(linhasMovimentacao.first()).toContainText('Movimentação automática via fixture');
        await expect(linhasMovimentacao.nth(1)).toContainText('Processo iniciado');

        await fazerLogout(page);
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processoCadastroDisponibilizado.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processoCadastroDisponibilizado.descricao);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        const cardAtividadesGestor = page.getByTestId('card-subprocesso-atividades');
        await expect(cardAtividadesGestor).toBeVisible();
        await expect(cardAtividadesGestor).toHaveClass(/card-actionable/);
        await expect(cardAtividadesGestor).toHaveAttribute('role', 'button');
        await expect(cardAtividadesGestor).toContainText('Cadastro de atividades e conhecimentos da unidade');

        await fazerLogout(page);
        await login(page, USUARIOS.SERVIDOR_SECAO_211.titulo, USUARIOS.SERVIDOR_SECAO_211.senha);
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processoCadastroDisponibilizado.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processoCadastroDisponibilizado.descricao);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        const cardAtividadesServidor = page.getByTestId('card-subprocesso-atividades');
        await expect(cardAtividadesServidor).toBeVisible();
        await expect(cardAtividadesServidor).toHaveClass(/card-actionable/);
        await expect(cardAtividadesServidor).toHaveAttribute('role', 'button');
        await expect(cardAtividadesServidor).toContainText('Cadastro de atividades e conhecimentos da unidade');

        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        const processoCadastroHomologado = await criarProcessoCadastroHomologadoFixture(request, {
            unidade: UNIDADE_ALVO_2,
            descricao: `Fixture CDU-07 CAD HOM ${Date.now()}`
        });

        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processoCadastroHomologado.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processoCadastroHomologado.descricao);
        await navegarParaSubprocesso(page, UNIDADE_ALVO_2);

        const cardMapaAdminEdicao = page.getByTestId('card-subprocesso-mapa');
        await expect(cardMapaAdminEdicao).toBeVisible();
        await expect(cardMapaAdminEdicao).toContainText('Mapa de competências técnicas da unidade');

        const processoMapaDisponibilizado = await criarProcessoMapaDisponibilizadoFixture(request, {
            unidade: UNIDADE_ALVO_3,
            descricao: `Fixture CDU-07 MAPA DISP ${Date.now()}`
        });

        await fazerLogout(page);
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processoMapaDisponibilizado.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processoMapaDisponibilizado.descricao);
        await navegarParaSubprocesso(page, UNIDADE_ALVO_3);

        const cardMapaGestor = page.getByTestId('card-subprocesso-mapa');
        await expect(cardMapaGestor).toBeVisible();
        await expect(cardMapaGestor).toHaveClass(/card-actionable/);
        await expect(cardMapaGestor).toHaveAttribute('role', 'button');
        await expect(cardMapaGestor).toContainText('Mapa de competências técnicas da unidade');

        await fazerLogout(page);
        await login(page, USUARIOS.SERVIDOR_SECAO_221.titulo, USUARIOS.SERVIDOR_SECAO_221.senha);
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processoMapaDisponibilizado.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processoMapaDisponibilizado.descricao);
        await navegarParaSubprocesso(page, UNIDADE_ALVO_3);

        const cardMapaServidor = page.getByTestId('card-subprocesso-mapa');
        await expect(cardMapaServidor).toBeVisible();
        await expect(cardMapaServidor).toHaveClass(/card-actionable/);
        await expect(cardMapaServidor).toHaveAttribute('role', 'button');
        await expect(cardMapaServidor).toContainText('Mapa de competências técnicas da unidade');
    });

    test('Deve manter o acesso de visualização no processo finalizado para servidor da própria unidade', async ({
                                                                                                                    _resetAutomatico,
                                                                                                                    request,
                                                                                                                    page
                                                                                                                }) => {
        const processoFinalizado = await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: `Fixture CDU-07 FINAL ${Date.now()}`
        });

        await login(page, USUARIOS.SERVIDOR_SECAO_211.titulo, USUARIOS.SERVIDOR_SECAO_211.senha);
        await page.goto('/painel');
        await expect(page.getByTestId('tbl-processos').getByText(processoFinalizado.descricao).first()).toBeVisible();
        await acessarDetalhesProcesso(page, processoFinalizado.descricao);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO);

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            nomeUnidade: NOME_UNIDADE_ALVO,
            situacao: 'Mapa homologado',
            localizacao: UNIDADE_ALVO,
            titular: 'Debbie Harry'
        });

        const cardAtividades = page.getByTestId('card-subprocesso-atividades');
        await expect(cardAtividades).toBeVisible();
        await expect(cardAtividades).toHaveClass(/card-actionable/);
        await expect(cardAtividades).toHaveAttribute('role', 'button');
        await expect(cardAtividades).toContainText('Cadastro de atividades e conhecimentos da unidade');

        const cardMapa = page.getByTestId('card-subprocesso-mapa');
        await expect(cardMapa).toBeVisible();
        await expect(cardMapa).toHaveClass(/card-actionable/);
        await expect(cardMapa).toHaveAttribute('role', 'button');
        await expect(cardMapa).toContainText('Mapa de competências técnicas da unidade');
    });

    test('Deve exibir cards com rotas corretas ao navegar entre subprocessos distintos na mesma sessão', async ({
                                                                                                                    _resetAutomatico,
                                                                                                                    _autenticadoComoAdmin,
                                                                                                                    request,
                                                                                                                    page
                                                                                                                }) => {
        // Cenário: o CHEFE visita primeiro um subprocesso somente leitura e depois outro editável.
        // O card é o mesmo, mas o texto/acionabilidade precisam refletir o estado atual sem reaproveitar cache antigo.
        const ts = Date.now();
        const UNIDADE = 'ASSESSORIA_12';
        const chefe = USUARIOS.CHEFE_ASSESSORIA_12;

        const processoDisponibilizado = await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE,
            descricao: `CDU-07 Finalizado ${ts}`
        });
        const processoEmAndamento = await criarProcessoFixture(request, {
            unidade: UNIDADE,
            descricao: `CDU-07 Andamento ${ts}`,
            iniciar: true,
            diasLimite: 30
        });

        await login(page, chefe.titulo, chefe.senha);

        // 1. Primeira visita: subprocesso finalizado -> card de cadastro somente leitura
        await page.goto(`/processo/${processoDisponibilizado.codigo}/${UNIDADE}`);
        await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');

        // 2. Segunda visita: subprocesso com cadastro EM ANDAMENTO → card editável
        await page.goto(`/processo/${processoEmAndamento.codigo}/${UNIDADE}`);
        // O card deve atualizar imediatamente para o modo editável.
        await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
    });

    test('Deve exibir os cards do ramo de diagnóstico na tela de detalhes do subprocesso', async ({
                                                                                                      _resetAutomatico,
                                                                                                      _autenticadoComoAdmin,
                                                                                                      page
                                                                                                  }) => {
        const descricao = `Processo CDU-07 DIAGNOSTICO ${Date.now()}`;

        await criarProcesso(page, {
            descricao,
            tipo: 'DIAGNOSTICO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_12',
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });

        await acessarDetalhesProcesso(page, descricao);
        await esperarPaginaDetalhesProcesso(page);
        await navegarParaSubprocesso(page, 'ASSESSORIA_12');

        await expect(page.getByTestId('card-subprocesso-diagnostico')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-diagnostico')).toContainText('Autoavaliação');
        await expect(page.getByTestId('card-subprocesso-ocupacoes')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-ocupacoes')).toContainText('Ocupações críticas');
        await expect(page.getByTestId('card-subprocesso-monitoramento')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-monitoramento')).toContainText('Monitoramento');
    });

    test('Regressão - cache de sessão no subprocesso: deve habilitar atividades para CHEFE após logout de ADMIN sem limpar caches da SPA', async ({
                                                                                                                                                      _resetAutomatico,
                                                                                                                                                      page,
                                                                                                                                                      _autenticadoComoAdmin
                                                                                                                                                  }) => {
        const descricao = `Regressao cache subprocesso ${Date.now()}`;
        const unidade = 'ASSESSORIA_12';

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade,
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });

        await acessarDetalhesProcesso(page, descricao);
        const codProcesso = await extrairProcessoCodigo(page);

        // Primeiro acesso como ADMIN para deixar o contexto do subprocesso carregado na SPA.
        await page.goto(`/processo/${codProcesso}/${unidade}`);
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${unidade}(?:\?.*)?$`));
        await expect(page.getByTestId('header-subprocesso')).toBeVisible();

        // Logout e novo login na mesma aba, sem limpar storage nem reiniciar a aplicação.
        await fazerLogout(page);
        await autenticar(page, USUARIOS.CHEFE_ASSESSORIA_12.titulo, USUARIOS.CHEFE_ASSESSORIA_12.senha);
        await page.waitForURL(/\/painel(?:\?|$)/);
        await limparNotificacoes(page);

        // Acessa o mesmo subprocesso imediatamente após o login do CHEFE.
        await page.goto(`/processo/${codProcesso}/${unidade}`);
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${unidade}(?:\?.*)?$`));

        const cardCadastro = page.getByTestId('card-subprocesso-atividades');
        await expect(cardCadastro).toBeVisible();

        await cardCadastro.click();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/${codProcesso}/${unidade}/cadastro(?:\?.*)?$`));
    });
});
