import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
    verificarBotaoImpactoDireto
} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    abrirHistoricoAnaliseVisualizacao,
    aceitarCadastroMapeamento,
    aceitarRevisao,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    cancelarDevolucao,
    devolverRevisao,
    fecharHistoricoAnalise,
    homologarCadastroMapeamento,
} from './helpers/helpers-analise.js';
import {fazerLogout, navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';

test.describe.serial('CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-14 ${timestamp}`;
    const descMapeamento = `Mapeamento para CDU-14 ${timestamp}`;

    test('Fluxo completo de revisão e análise', async ({page, autenticadoComoAdmin}) => {
        test.setTimeout(120000);

        await test.step('1. Preparação: Mapeamento completo e finalizado', async () => {
            await criarProcesso(page, {
                descricao: descMapeamento,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_21'],
                iniciar: true
            });
            await fazerLogout(page);

            // Chefe disponibiliza
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await acessarSubprocessoChefeDireto(page, descMapeamento, UNIDADE_ALVO);
            await navegarParaAtividades(page);
            await adicionarAtividade(page, `Atividade Map ${timestamp}`);
            await adicionarConhecimento(page, `Atividade Map ${timestamp}`, 'Conhecimento Map');
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await fazerLogout(page);

            // Aceites e Homologação
            await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
            await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await aceitarCadastroMapeamento(page);
            await fazerLogout(page);

            await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
            await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await aceitarCadastroMapeamento(page);
            await fazerLogout(page);

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.getByTestId('tbl-processos').getByText(descMapeamento).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await homologarCadastroMapeamento(page);

            // Criar Competência e Disponibilizar Mapa
            await navegarParaMapa(page);
            await expect(page.getByText(/Carregando/i)).toBeHidden();
            
            // Garantir que atividades homologadas apareçam na lista de seleção (esperar componente estabilizar)
            await page.waitForResponse(resp => resp.url().includes('/atividades-elegiveis') || resp.url().includes('/mapa'));

            await criarCompetencia(page, `Comp Map ${timestamp}`, [`Atividade Map ${timestamp}`]);
            await disponibilizarMapa(page, '2030-12-31');
            await fazerLogout(page);

            // Chefe valida e Hierarquia Aceita até ADMIN homologar
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await acessarSubprocessoChefeDireto(page, descMapeamento, UNIDADE_ALVO);
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-validar').click();
            await page.getByTestId('btn-validar-mapa-confirmar').click();
            await fazerLogout(page);

            await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
            await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await fazerLogout(page);

            await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
            await acessarSubprocessoGestor(page, descMapeamento, UNIDADE_ALVO);
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await fazerLogout(page);

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await acessarSubprocessoAdmin(page, descMapeamento, UNIDADE_ALVO);
            await navegarParaMapa(page);
            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();

            // Finalizar processo
            await page.goto('/painel');
            await page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descMapeamento)}).click();
            await page.getByTestId('btn-processo-finalizar').click();
            await page.getByTestId('btn-finalizar-processo-confirmar').click();
            await fazerLogout(page);
        });

        await test.step('2. ADMIN cria e inicia processo de revisão', async () => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await criarProcesso(page, {
                descricao: descProcesso,
                tipo: 'REVISAO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_21'],
                iniciar: true
            });
            await fazerLogout(page);
        });

        await test.step('3. CHEFE revisa e disponibiliza', async () => {
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividades(page);

            await adicionarAtividade(page, `Atividade Rev ${timestamp}`);
            await adicionarConhecimento(page, `Atividade Rev ${timestamp}`, 'Conhecimento Rev');

            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await fazerLogout(page);
        });

        await test.step('4. GESTOR visualiza histórico e impactos', async () => {
            await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);

            const modal = await abrirHistoricoAnaliseVisualizacao(page);
            await expect(modal).toBeVisible();
            await fecharHistoricoAnalise(page);

            await verificarBotaoImpactoDireto(page);
        });

        await test.step('5. GESTOR devolve revisão', async () => {
            await devolverRevisao(page, 'Favor revisar as competências associadas');
            await fazerLogout(page);

            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Revisão em andamento/i);

            await navegarParaAtividades(page);
            const modal = await abrirHistoricoAnalise(page);
            await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
            await fecharHistoricoAnalise(page);

            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await fazerLogout(page);
        });

        await test.step('6. GESTOR cancela devolução e aceita', async () => {
            await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            
            await cancelarDevolucao(page);
            await expect(page.getByRole('heading', {name: 'Atividades e conhecimentos'})).toBeVisible();

            await aceitarRevisao(page, 'Revisão aprovada conforme análise');
            await fazerLogout(page);
        });

        await test.step('7. ADMIN visualiza histórico final', async () => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            const modal = await abrirHistoricoAnaliseVisualizacao(page);
            await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/ACEITE_REVISAO/i);
            await fecharHistoricoAnalise(page);
        });
    });
});
