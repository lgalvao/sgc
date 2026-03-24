import {test, expect} from '@playwright/test';
import * as AuthHelpers from './helpers/helpers-auth.js';
import * as ProcessoHelpers from './helpers/helpers-processos.js';
import * as AtividadeHelpers from './helpers/helpers-atividades.js';
import * as MapaHelpers from './helpers/helpers-mapas.js';
import * as AnaliseHelpers from './helpers/helpers-analise.js';
import {fazerLogout} from './helpers/helpers-navegacao.js';

test.describe.serial('Jornada do Ciclo de Vida Completo do SGC', () => {
    test.setTimeout(20_000);

    const timestamp = Date.now();
    const descricaoMapeamento = `Mapeamento Ciclo Completo ${timestamp}`;
    const descricaoRevisao = `Revisão Ciclo Completo ${timestamp}`;
    const siglaUnidade = 'ASSESSORIA_11'; // Unidade alvo (David Bowie)

    test.beforeAll(async ({request}) => {
        // Reset do banco de dados UMA VEZ para iniciar a jornada
        const response = await request.post('/e2e/reset-database');
        expect(response.ok()).toBeTruthy();
    });

    test('Fase 1: Mapeamento Inicial - Cadastro de Atividades', async ({page}) => {
        // 1. ADMIN cria o processo
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.titulo, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.senha);
        await ProcessoHelpers.criarProcesso(page, {
            descricao: descricaoMapeamento,
            tipo: 'MAPEAMENTO',
            unidade: [siglaUnidade],
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });
        await fazerLogout(page);

        // 2. CHEFE (David Bowie) preenche as atividades
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11.titulo, AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
        await AtividadeHelpers.navegarParaAtividades(page);
        
        // Importa atividades de um processo base (Seed 200)
        await AtividadeHelpers.importarAtividadesVazia(page, 'Processo Seed 200', 'SECRETARIA_1', ['Atividade 1']);
        
        // Disponibiliza o cadastro para o Gestor
        await AtividadeHelpers.disponibilizarCadastro(page);
        await fazerLogout(page);

        // 3. GESTOR (John Lennon) realiza o Aceite
        await AuthHelpers.loginComPerfil(page, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.titulo, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.senha, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.perfil);
        await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
        await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
        await AnaliseHelpers.aceitarCadastroMapeamento(page, 'Cadastro aceito pelo Gestor.');
        await fazerLogout(page);

        // 4. ADMIN realiza a Homologação do Cadastro
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.titulo, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.senha);
        await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
        await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
        await AnaliseHelpers.homologarCadastroMapeamento(page);
        
        // Admin permanece no Subprocesso após homologar (segundo redirect de CadastroVisualizacaoView.vue)
        await expect(page).toHaveURL(/\/processo\/\d+\/ASSESSORIA_11$/);
        await fazerLogout(page);
    });

    test('Fase 2: Elaboração e Homologação do Mapa', async ({page}) => {
        // 5. ADMIN cria e disponibiliza o Mapa para a unidade
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.titulo, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.senha);
        await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
        await MapaHelpers.navegarParaMapa(page);
        
        // Cria uma competência no Mapa
        await MapaHelpers.criarCompetencia(page, 'Competência Técnica Básica', ['Atividade 1']);
        
        // Disponibiliza o Mapa para validação do Chefe
        await MapaHelpers.disponibilizarMapa(page);
        await fazerLogout(page);

        // 6. CHEFE (David Bowie) valida o Mapa
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11.titulo, AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoMapeamento, siglaUnidade);
        await MapaHelpers.navegarParaMapa(page);
        
        // Ação: Validar Mapa
        await page.getByTestId('btn-mapa-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await page.waitForURL(/\/painel$/);
        await fazerLogout(page);

        // 7. GESTOR (John Lennon) realiza o Aceite do Mapa
        await AuthHelpers.loginComPerfil(page, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.titulo, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.senha, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.perfil);
        await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoMapeamento, siglaUnidade);
        
        // O Aceite do mapa é feito na visualização do mapa
        await page.getByTestId('card-subprocesso-mapa-visualizacao').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('inp-aceite-mapa-observacao').fill('Mapa aceito pelo Gestor.');
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await page.waitForURL(/\/painel$/);
        await fazerLogout(page);

        // 8. ADMIN realiza a Homologação do Mapa
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.titulo, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.senha);
        await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoMapeamento, siglaUnidade);
        
        await page.getByTestId('card-subprocesso-mapa-visualizacao').click();
        await page.getByTestId('btn-mapa-homologar-aceite').click();
        await page.getByTestId('inp-aceite-mapa-observacao').fill('Mapa homologado pelo Admin. Ciclo base concluído.');
        await page.getByTestId('btn-aceite-mapa-confirmar').click();
        await page.waitForURL(/\/painel$/);

        // 8.1 ADMIN finaliza o processo para liberar a revisão
        // Clica no processo na tabela do painel para abrir os detalhes
        await page.getByTestId('tbl-processos').getByText(descricaoMapeamento).first().click();
        await expect(page).toHaveURL(/\/processo\/\d+$/);
        
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        await page.waitForURL(/\/painel$/);

        await fazerLogout(page);
    });

    test('Fase 3: Ciclo de Revisão e Manutenção', async ({page}) => {
        // 9. ADMIN cria o processo de REVISÃO referenciando o ciclo concluído
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.titulo, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.senha);
        await ProcessoHelpers.criarProcesso(page, {
            descricao: descricaoRevisao,
            tipo: 'REVISAO',
            unidade: [siglaUnidade],
            expandir: ['SECRETARIA_1'],
            iniciar: true
        });
        await fazerLogout(page);

        // 10. CHEFE (David Bowie) realiza a Revisão
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11.titulo, AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11.senha);
        await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoRevisao, siglaUnidade);
        await AtividadeHelpers.navegarParaAtividades(page);
        
        // Verifica se a atividade do ciclo anterior foi importada automaticamente
        await expect(page.getByText('Atividade 1')).toBeVisible();
        
        // Altera para marcar a revisão (Adiciona conhecimento)
        await AtividadeHelpers.adicionarConhecimento(page, 'Atividade 1', 'Conhecimento Revisado');
        
        // Disponibiliza a revisão
        await AtividadeHelpers.disponibilizarCadastro(page);
        await fazerLogout(page);

        // 11. GESTOR realiza o Aceite da Revisão
        await AuthHelpers.loginComPerfil(page, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.titulo, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.senha, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.perfil);
        await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoRevisao, siglaUnidade);
        await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
        await AnaliseHelpers.aceitarRevisao(page, 'Revisão aceita.');
        await fazerLogout(page);

        // 12. ADMIN realiza a Homologação da Revisão
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.titulo, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.senha);
        await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoRevisao, siglaUnidade);
        await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
        
        // Aqui Admin homologa (mais uma vez, redirect para subprocesso)
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('inp-aceite-cadastro-obs').fill('Revisão homologada. Ciclo de manutenção completo.');
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();
        await page.waitForURL(/\/processo\/\d+\/ASSESSORIA_11$/);
        
        await fazerLogout(page);
    });
});
