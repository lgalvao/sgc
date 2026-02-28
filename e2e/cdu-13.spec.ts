import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {
    abrirHistoricoAnalise,
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    devolverCadastroMapeamento,
    fecharHistoricoAnalise,
} from './helpers/helpers-analise.js';
import {fazerLogout, navegarParaSubprocesso} from './helpers/helpers-navegacao.js';

test.describe.serial('CDU-13 - Analisar cadastro de atividades e conhecimentos', () => {
    test.setTimeout(60000);
    const UNIDADE_ALVO = 'SECAO_211';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-13 ${timestamp}`;

    test('Fluxo Completo de Análise de Atividades (CDU-13)', async ({page, autenticadoComoAdmin}) => {

        await test.step('1. ADMIN cria e inicia processo', async () => {
            await criarProcesso(page, {
                descricao: descProcesso,
                tipo: 'MAPEAMENTO',
                diasLimite: 30,
                unidade: UNIDADE_ALVO,
                expandir: ['SECRETARIA_2', 'COORD_21'],
                iniciar: true
            });
            await fazerLogout(page);
        });

        await test.step('2. CHEFE disponibiliza atividades', async () => {
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividades(page);

            await adicionarAtividade(page, `Atividade ${timestamp}`);
            await adicionarConhecimento(page, `Atividade ${timestamp}`, 'Conhecimento A');

            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();

            // Passo 9.11/10.9: Redireciona para o Painel
            await expect(page).toHaveURL(/\/painel/);
            await fazerLogout(page);
        });

        await test.step('3. GESTOR COORD_21 aceita (sobe para Secretaria)', async () => {
            await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);

            // Conforme CDU-13 Passo 10
            await aceitarCadastroMapeamento(page, 'Ok pela Coordenação');

            // Passo 10.9: Redireciona para o Painel
            await expect(page).toHaveURL(/\/painel/);
            await fazerLogout(page);
        });

        await test.step('4. GESTOR SECRETARIA_2 devolve (desce para Coordenação)', async () => {
            await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);

            // Conforme CDU-13 Passo 9 (Devolução)
            await devolverCadastroMapeamento(page, 'Dados incompletos para a Secretaria');

            // Passo 9.11: Redireciona para o Painel
            await expect(page).toHaveURL(/\/painel/);
            await fazerLogout(page);
        });

        await test.step('5. GESTOR COORD_21 vê o processo de volta e devolve para SEÇÃO (origem)', async () => {
            await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);

            // Verificar situação (continua disponibilizado, pois não chegou na unidade dona)
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro disponibilizado/i);

            await navegarParaAtividadesVisualizacao(page);
            await devolverCadastroMapeamento(page, 'Corrigir conforme Secretaria');
            await fazerLogout(page);
        });

        await test.step('6. CHEFE vê processo "Em andamento" para ajustes', async () => {
            await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
            await acessarSubprocessoChefeDireto(page, descProcesso, UNIDADE_ALVO);

            // Passo 9.8: Chegou na unidade do subprocesso, muda para 'Cadastro em andamento'
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro em andamento/i);

            // Verificar histórico (Passo 7)
            await navegarParaAtividades(page);
            const modal = await abrirHistoricoAnalise(page);
            await expect(modal.getByTestId('cell-resultado-0')).toHaveText(/Devolu[cç][aã]o/i);
            await expect(modal.getByTestId('cell-observacao-0')).toHaveText('Corrigir conforme Secretaria');
            await fecharHistoricoAnalise(page);

            // Disponibilizar novamente para permitir aceite final
            await page.getByTestId('btn-cad-atividades-disponibilizar').click();
            await page.getByTestId('btn-confirmar-disponibilizacao').click();
            await fazerLogout(page);
        });

        await test.step('7. ACEITE FINAL -> ADMIN homologa', async () => {
            // Coordenação aceita
            await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await aceitarCadastroMapeamento(page, 'Ok final 1');
            await fazerLogout(page);

            // Secretaria aceita (move para ADMIN)
            await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
            await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);
            await aceitarCadastroMapeamento(page, 'Ok final 2');
            await fazerLogout(page);

            // ADMIN homologa (Passo 11)
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
            await navegarParaSubprocesso(page, UNIDADE_ALVO);
            await navegarParaAtividadesVisualizacao(page);

            // Clicar Homologar (conforme perfil ADMIN)
            await page.getByTestId('btn-acao-analisar-principal').click();
            await page.getByRole('dialog').getByRole('button', {name: 'Confirmar'}).click();

            // Passo 11.7: Redireciona para Detalhes do Subprocesso
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}$`));
            await expect(page.getByText(/Homologa[çcl]ão efetivada/i).first()).toBeVisible();
            await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
        });
    });
});
