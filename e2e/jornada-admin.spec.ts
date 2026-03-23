import {test, expect} from '@playwright/test';
import * as ProcessoHelpers from './helpers/helpers-processos.js';
import * as AtividadeHelpers from './helpers/helpers-atividades.js';
import * as AuthHelpers from './helpers/helpers-auth.js';
import * as AnaliseHelpers from './helpers/helpers-analise.js';

test.describe('Jornada do perfil ADMIN', () => {
    
    test.beforeEach(async ({request}) => {
        await request.post('/e2e/reset-database');
    });

    test('ADMIN: Criação, acompanhamento e acesso ao cadastro via UI', async ({page}) => {
        test.slow(); // Triplica o timeout padrão para esta jornada longa
        const descricaoProcesso = 'Jornada Admin Fim-a-Fim';
        const siglaUnidade = 'ASSESSORIA_11';
        const paiUnidade = 'SECRETARIA_1';

        // 1. Login
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.titulo, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.senha);
        
        // 2. Criar Processo (Navegação via Botão no Painel)
        await ProcessoHelpers.criarProcesso(page, {
            descricao: descricaoProcesso,
            tipo: 'MAPEAMENTO',
            expandir: [paiUnidade],
            unidade: siglaUnidade,
            iniciar: true
        });

        // 3. Acompanhar no Detalhe do Processo (ADMIN)
        await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoProcesso, siglaUnidade);
        
        // 4. Verificar Header do Subprocesso (Vazio inicia como Não iniciado)
        await ProcessoHelpers.verificarDetalhesSubprocesso(page, {
            sigla: siglaUnidade,
            situacao: 'Não iniciado'
        });

        // 5. Alternar para Perfil CHEFE da Unidade para preencher atividades (Regra de Ouro)
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11.titulo, AuthHelpers.USUARIOS.CHEFE_ASSESSORIA_11.senha);

        // 6. Navegar para o Subprocesso como CHEFE (David Bowie acessa diretamente sua unidade)
        await AnaliseHelpers.acessarSubprocessoChefeDireto(page, descricaoProcesso, siglaUnidade);

        // 7. Entrar no Cadastro e Importar Atividades
        await AtividadeHelpers.navegarParaAtividades(page);
        
        // Importar do Processo Seed 200 (SECRETARIA_1) - Atividades de exemplo do seed.sql
        await AtividadeHelpers.importarAtividadesVazia(page, 'Processo Seed 200', 'SECRETARIA_1', ['Atividade 1']);

        // 8. Verificar Mudança de Situação para Em Andamento
        await expect(page.getByTestId('cad-atividades__txt-badge-situacao')).toHaveText(/Cadastro em andamento/i);

        // 9. Disponibilizar Cadastro
        await AtividadeHelpers.disponibilizarCadastro(page);

        // 10. Alternar para Perfil GESTOR da Unidade Superior para Aceitar Cadastro
        // John Lennon tem múltiplos perfis, precisamos usar loginComPerfil
        await AuthHelpers.loginComPerfil(page, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.titulo, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.senha, AuthHelpers.USUARIOS.GESTOR_SECRETARIA_1.perfil!);
        
        // 11. GESTOR Aceita o Cadastro
        await AnaliseHelpers.acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
        
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByTestId('inp-aceite-cadastro-obs').fill('Cadastro muito bem feito.');
        await page.getByTestId('btn-aceite-cadastro-confirmar').click();

        // 12. Alternar para Perfil ADMIN para Homologar
        // Agora o subprocesso foi movido para a unidade central ADMIN
        await AuthHelpers.login(page, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.titulo, AuthHelpers.USUARIOS.ADMIN_1_PERFIL.senha);

        // 13. Acompanhar no Detalhe do Processo (ADMIN)
        await AnaliseHelpers.acessarSubprocessoAdmin(page, descricaoProcesso, siglaUnidade);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toContainText(/Cadastro disponibilizado/i);

        // 14. Acessar Subprocesso de novo e Homologar
        await AtividadeHelpers.navegarParaAtividadesVisualizacao(page);
        
        await page.getByTestId('btn-acao-analisar-principal').click();
        await page.getByRole('dialog').getByRole('button', {name: 'Confirmar'}).click();

        // 15. Verificar Finalização do Ciclo de Cadastro
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);

        // 16. Voltar ao Painel
        await page.getByTestId('nav-link-painel').click();
        await expect(page).toHaveURL(/\/painel/);
    });
});
