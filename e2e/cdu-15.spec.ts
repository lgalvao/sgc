import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades.js';
import {
    criarCompetencia,
    disponibilizarMapa,
    editarCompetencia,
    excluirCompetenciaCancelando,
    excluirCompetenciaConfirmando,
    navegarParaMapa,
    verificarCompetenciaNoMapa,
    verificarSituacaoSubprocesso
} from './helpers/helpers-mapas.js';

test.describe.serial('CDU-15 - Manter mapa de competências', () => {
    test.setTimeout(60000);
    const UNIDADE_ALVO = 'SECAO_211';
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-15 ${timestamp}`;

    const ATIVIDADE_1 = `Atividade 1 ${timestamp}`;
    const ATIVIDADE_2 = `Atividade 2 ${timestamp}`;
    const CONHECIMENTO_1 = `Conhecimento 1 ${timestamp}`;
    const CONHECIMENTO_2 = `Conhecimento 2 ${timestamp}`;

    test('Preparacao: Criar processo e homologar cadastro de atividades', async ({page, autenticadoComoAdmin}) => {
        // 1. Admin cria e inicia processo
        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_21']
        });

        const linhaProcesso = page.getByTestId('tbl-processos').locator('tr').filter({has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await expect(page.getByText('Processo iniciado')).toBeVisible();

        // 2. Chefe adiciona atividades e conhecimentos
        await login(page, USUARIOS.CHEFE_SECAO_211.titulo, USUARIOS.CHEFE_SECAO_211.senha);
        await acessarSubprocessoChefeDireto(page, descProcesso);
        await navegarParaAtividades(page);
        await adicionarAtividade(page, ATIVIDADE_1);
        await adicionarConhecimento(page, ATIVIDADE_1, CONHECIMENTO_1);
        await adicionarAtividade(page, ATIVIDADE_2);
        await adicionarConhecimento(page, ATIVIDADE_2, CONHECIMENTO_2);
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // 3. Gestores aceitam
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        await loginComPerfil(page, USUARIOS.CHEFE_SECRETARIA_2.titulo, USUARIOS.CHEFE_SECRETARIA_2.senha, 'GESTOR - SECRETARIA_2');
        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // 4. Admin homologa
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroMapeamento(page);

        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
    });

    test('Cenários CDU-15: Fluxo completo de manutenção do mapa pelo ADMIN', async ({page, autenticadoComoAdmin}) => {
        // CT-00 e CT-01: Acessar Edição e verificar elementos
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await expect(page.getByRole('heading', {name: /Mapa de competências/i})).toBeVisible();
        await expect(page.getByTestId('btn-abrir-criar-competencia').or(page.getByTestId('btn-abrir-criar-competencia-empty'))).toBeVisible();
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();

        // CT-02: Criar Competência
        const compDesc = `Competência 1 ${timestamp}`;
        await criarCompetencia(page, compDesc, [ATIVIDADE_1]);
        await verificarSituacaoSubprocesso(page, 'Mapa criado');
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeEnabled();

        // CT-03: Editar Competência
        const newDesc = `Competência 1 Editada ${timestamp}`;
        await editarCompetencia(page, compDesc, newDesc, [ATIVIDADE_2]);
        await verificarCompetenciaNoMapa(page, newDesc, [ATIVIDADE_1, ATIVIDADE_2]);

        // CT-05: Validar Cancelamento da Exclusão
        await excluirCompetenciaCancelando(page, newDesc);
        await expect(page.getByText(newDesc).first()).toBeVisible();

        // CT-04: Excluir Competência com Confirmação
        await excluirCompetenciaConfirmando(page, newDesc);
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();

        // CT-06: Navegar para Disponibilização
        const compFinal = `Competência Final ${timestamp}`;
        await criarCompetencia(page, compFinal, [ATIVIDADE_1, ATIVIDADE_2]);
        await disponibilizarMapa(page);

        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByText(/Mapa disponibilizado/i)).toBeVisible();
    });
});
