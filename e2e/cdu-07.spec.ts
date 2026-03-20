import {expect, test} from './fixtures/complete-fixtures.js';
import {
    criarProcessoCadastroDisponibilizadoFixture,
    criarProcessoCadastroHomologadoFixture,
    criarProcessoFinalizadoFixture,
    criarProcessoFixture,
    criarProcessoMapaDisponibilizadoFixture
} from './fixtures/fixtures-processos.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {
    esperarPaginaDetalhesProcesso,
    esperarPaginaSubprocesso,
    fazerLogout,
    navegarParaSubprocesso
} from './helpers/helpers-navegacao.js';
import {criarProcesso, verificarDetalhesSubprocesso} from './helpers/helpers-processos.js';

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

        await page.goto(`/processo/${processo.codigo}`);
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
        await expect(page.getByTestId('tbl-movimentacoes').locator('tbody tr')).not.toHaveCount(0);

        const cardAtividadesAdmin = page.getByTestId('card-subprocesso-atividades-vis');
        await expect(cardAtividadesAdmin).toBeVisible();
        await expect(cardAtividadesAdmin).toHaveClass(/card-disabled/);
        await expect(cardAtividadesAdmin).not.toHaveAttribute('role', 'button');

        const cardMapaAdmin = page.getByTestId('card-subprocesso-mapa-desabilitado');
        await expect(cardMapaAdmin).toBeVisible();
        await expect(cardMapaAdmin).toHaveClass(/card-disabled/);

        await fazerLogout(page);
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await page.goto(`/processo/${processo.codigo}`);
        await esperarPaginaDetalhesProcesso(page, processo.codigo);
        await navegarParaSubprocesso(page, UNIDADE_ALVO);

        const cardAtividadesGestor = page.getByTestId('card-subprocesso-atividades-vis');
        await expect(cardAtividadesGestor).toBeVisible();
        await expect(cardAtividadesGestor).toHaveClass(/card-disabled/);

        const cardMapaGestor = page.getByTestId('card-subprocesso-mapa-desabilitado');
        await expect(cardMapaGestor).toBeVisible();
        await expect(cardMapaGestor).toHaveClass(/card-disabled/);

        await fazerLogout(page);
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
        await page.goto(`/processo/${processo.codigo}/${UNIDADE_ALVO}`);
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
        await page.goto(`/processo/${processo.codigo}/${UNIDADE_ALVO}`);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO);

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            nomeUnidade: NOME_UNIDADE_ALVO,
            situacao: 'Não iniciado',
            localizacao: UNIDADE_ALVO,
            titular: 'Debbie Harry'
        });

        const cardAtividadesServidor = page.getByTestId('card-subprocesso-atividades-vis');
        await expect(cardAtividadesServidor).toBeVisible();
        await expect(cardAtividadesServidor).toHaveClass(/card-disabled/);

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

        await page.goto(`/processo/${processoCadastroDisponibilizado.codigo}/${UNIDADE_ALVO}`);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO);

        const cardAtividadesAdmin = page.getByTestId('card-subprocesso-atividades-vis');
        await expect(cardAtividadesAdmin).toBeVisible();
        await expect(cardAtividadesAdmin).toHaveClass(/card-actionable/);
        await expect(cardAtividadesAdmin).toHaveAttribute('role', 'button');
        await expect(cardAtividadesAdmin).toContainText('Visualização das atividades e conhecimentos da unidade');

        const cardMapaAdminBloqueado = page.getByTestId('card-subprocesso-mapa-desabilitado');
        await expect(cardMapaAdminBloqueado).toBeVisible();
        await expect(cardMapaAdminBloqueado).toHaveClass(/card-disabled/);

        const linhasMovimentacao = page.getByTestId('tbl-movimentacoes').locator('tbody tr');
        await expect(linhasMovimentacao.first()).toContainText('Movimentação automática via fixture');
        await expect(linhasMovimentacao.nth(1)).toContainText('Processo iniciado');

        await fazerLogout(page);
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await page.goto(`/processo/${processoCadastroDisponibilizado.codigo}/${UNIDADE_ALVO}`);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO);

        const cardAtividadesGestor = page.getByTestId('card-subprocesso-atividades-vis');
        await expect(cardAtividadesGestor).toBeVisible();
        await expect(cardAtividadesGestor).toHaveClass(/card-actionable/);
        await expect(cardAtividadesGestor).toHaveAttribute('role', 'button');

        await fazerLogout(page);
        await login(page, USUARIOS.SERVIDOR_SECAO_211.titulo, USUARIOS.SERVIDOR_SECAO_211.senha);
        await page.goto(`/processo/${processoCadastroDisponibilizado.codigo}/${UNIDADE_ALVO}`);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO);

        const cardAtividadesServidor = page.getByTestId('card-subprocesso-atividades-vis');
        await expect(cardAtividadesServidor).toBeVisible();
        await expect(cardAtividadesServidor).toHaveClass(/card-actionable/);
        await expect(cardAtividadesServidor).toHaveAttribute('role', 'button');

        await fazerLogout(page);
        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        const processoCadastroHomologado = await criarProcessoCadastroHomologadoFixture(request, {
            unidade: UNIDADE_ALVO_2,
            descricao: `Fixture CDU-07 CAD HOM ${Date.now()}`
        });

        await page.goto(`/processo/${processoCadastroHomologado.codigo}/${UNIDADE_ALVO_2}`);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO_2);

        const cardMapaAdminEdicao = page.getByTestId('card-subprocesso-mapa-edicao');
        await expect(cardMapaAdminEdicao).toBeVisible();
        await expect(cardMapaAdminEdicao).toContainText('Mapa de competências técnicas da unidade');

        const processoMapaDisponibilizado = await criarProcessoMapaDisponibilizadoFixture(request, {
            unidade: UNIDADE_ALVO_3,
            descricao: `Fixture CDU-07 MAPA DISP ${Date.now()}`
        });

        await fazerLogout(page);
        await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
        await page.goto(`/processo/${processoMapaDisponibilizado.codigo}/${UNIDADE_ALVO_3}`);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO_3);

        const cardMapaGestor = page.getByTestId('card-subprocesso-mapa-visualizacao');
        await expect(cardMapaGestor).toBeVisible();
        await expect(cardMapaGestor).toHaveClass(/card-actionable/);
        await expect(cardMapaGestor).toHaveAttribute('role', 'button');
        await expect(cardMapaGestor).toContainText('Visualização do mapa de competências técnicas');

        await fazerLogout(page);
        await login(page, USUARIOS.SERVIDOR_SECAO_221.titulo, USUARIOS.SERVIDOR_SECAO_221.senha);
        await page.goto(`/processo/${processoMapaDisponibilizado.codigo}/${UNIDADE_ALVO_3}`);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO_3);

        const cardMapaServidor = page.getByTestId('card-subprocesso-mapa-visualizacao');
        await expect(cardMapaServidor).toBeVisible();
        await expect(cardMapaServidor).toHaveClass(/card-actionable/);
        await expect(cardMapaServidor).toHaveAttribute('role', 'button');
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
        await page.goto(`/processo/${processoFinalizado.codigo}/${UNIDADE_ALVO}`);
        await esperarPaginaSubprocesso(page, UNIDADE_ALVO);

        await verificarDetalhesSubprocesso(page, {
            sigla: UNIDADE_ALVO,
            nomeUnidade: NOME_UNIDADE_ALVO,
            situacao: 'Mapa homologado',
            localizacao: UNIDADE_ALVO,
            titular: 'Debbie Harry'
        });

        const cardAtividades = page.getByTestId('card-subprocesso-atividades-vis');
        await expect(cardAtividades).toBeVisible();
        await expect(cardAtividades).toHaveClass(/card-actionable/);
        await expect(cardAtividades).toHaveAttribute('role', 'button');

        const cardMapa = page.getByTestId('card-subprocesso-mapa-visualizacao');
        await expect(cardMapa).toBeVisible();
        await expect(cardMapa).toHaveClass(/card-actionable/);
        await expect(cardMapa).toHaveAttribute('role', 'button');
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

        await page.getByTestId('tbl-processos').getByText(descricao, {exact: true}).first().click();
        await esperarPaginaDetalhesProcesso(page);
        await navegarParaSubprocesso(page, 'ASSESSORIA_12');

        await expect(page.getByTestId('card-subprocesso-diagnostico')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-diagnostico')).toContainText('Autoavaliação');
        await expect(page.getByTestId('card-subprocesso-ocupacoes')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-ocupacoes')).toContainText('Ocupações críticas');
        await expect(page.getByTestId('card-subprocesso-monitoramento')).toBeVisible();
        await expect(page.getByTestId('card-subprocesso-monitoramento')).toContainText('Monitoramento');
    });
});
