import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {criarCompetencia, disponibilizarMapa, navegarParaMapa} from './helpers/helpers-mapas.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {fazerLogout, navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-20 - Analisar validação de mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';

    const timestamp = Date.now();
    const descProcesso = `Mapeamento CDU-20 ${timestamp}`;

    const atividade1 = `Atividade 1 ${timestamp}`;
    const atividade2 = `Atividade 2 ${timestamp}`;
    const competencia1 = `Competência 1 ${timestamp}`;
    const competencia2 = `Competência 2 ${timestamp}`;

    test('Fluxo completo de validação de mapa', async ({page, autenticadoComoAdmin}) => {
        test.setTimeout(120000);

        await test.step('1. ADMIN cria processo e CHEFE disponibiliza', async () => {
            await criarProcesso(page, {
                descricao: descProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_22'],
                iniciar: true
            });
            await fazerLogout(page);

            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividades(page);

            await adicionarAtividade(page, atividade1);
            await adicionarConhecimento(page, atividade1, 'Conhecimento 1A');
            await adicionarAtividade(page, atividade2);
            await adicionarConhecimento(page, atividade2, 'Conhecimento 2A');

            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await fazerLogout(page);
        });

        await test.step('2. Aceites e Homologação de Cadastro', async () => {
            await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await aceitarCadastroMapeamento(page);
            await fazerLogout(page);

            await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await aceitarCadastroMapeamento(page);
            await fazerLogout(page);

            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

            // ADMIN clica na linha do processo no painel primeiro
            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();

            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await homologarCadastroMapeamento(page);
        });

        await test.step('3. ADMIN disponibiliza Mapa', async () => {
            await navegarParaMapa(page);
            await expect(page.getByText(/Carregando/i)).toBeHidden();

            await criarCompetencia(page, competencia1, [atividade1]);
            await criarCompetencia(page, competencia2, [atividade2]);

            await disponibilizarMapa(page, '2030-12-31');
            await expect(page).toHaveURL(/\/painel/);
            await fazerLogout(page);
        });

        await test.step('4. CHEFE valida o mapa', async () => {
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
            await navegarParaMapa(page);

            await page.getByTestId('btn-mapa-validar').click();
            await page.getByTestId('btn-validar-mapa-confirmar').click();
            await expect(page.getByText(/Mapa validado/i).first()).toBeVisible();
            await fazerLogout(page);
        });

        await test.step('5. GESTOR COORD_22 analisa e aceita', async () => {
            await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaMapa(page);

            await expect(page.getByTestId('btn-mapa-historico-gestor')).toBeVisible();
            await expect(page.getByTestId('btn-mapa-devolver')).toBeVisible();

            // Cancela devolução (passo CDU)
            await page.getByTestId('btn-mapa-devolver').click();
            await page.getByTestId('btn-devolucao-mapa-cancelar').click();

            // Aceita
            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);
            await fazerLogout(page);
        });

        await test.step('6. GESTOR SECRETARIA_2 aceita', async () => {
            await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaMapa(page);

            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await expect(page).toHaveURL(/\/painel/);
            await fazerLogout(page);
        });

        await test.step('7. ADMIN homologa final', async () => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaMapa(page);

            await page.getByTestId('btn-mapa-homologar-aceite').click();
            await page.getByTestId('btn-aceite-mapa-confirmar').click();
            await expect(page.getByText(/Homologação efetivada/i).first()).toBeVisible();
        });
    });
});
