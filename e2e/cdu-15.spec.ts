import {expect, test} from './fixtures/base';
import {login, USUARIOS} from './helpers/helpers-auth';
import {criarProcesso} from './helpers/helpers-processos';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento
} from './helpers/helpers-analise';
import {
    adicionarAtividade,
    adicionarConhecimento,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao
} from './helpers/helpers-atividades';
import {
    criarCompetencia,
    disponibilizarMapa,
    editarCompetencia,
    excluirCompetenciaCancelando,
    excluirCompetenciaConfirmando,
    navegarParaMapa,
    verificarCompetenciaNoMapa,
    verificarSituacaoSubprocesso
} from './helpers/helpers-mapas';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

test.describe.serial('CDU-15 - Manter mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD_22.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD_22.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-15 ${timestamp}`;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    const ATIVIDADE_1 = `Atividade 1 ${timestamp}`;
    const ATIVIDADE_2 = `Atividade 2 ${timestamp}`;
    const CONHECIMENTO_1 = `Conhecimento 1 ${timestamp}`;
    const CONHECIMENTO_2 = `Conhecimento 2 ${timestamp}`;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({request}) => {
        await cleanup.limpar(request);
    });

    test('Preparacao: Criar processo e homologar cadastro de atividades', async ({page}) => {
        // 1. Admin cria e inicia processo
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await criarProcesso(page, {
            descricao: descProcesso,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: UNIDADE_ALVO,
            expandir: ['SECRETARIA_2', 'COORD_22']
        });

        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descProcesso)});
        await linhaProcesso.click();

        const currentUrl = page.url();
        const match = /\/processo\/cadastro\/(\d+)/.exec(currentUrl);
        if (match && match[1]) {
            cleanup.registrar(parseInt(match[1]));
        }

        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
        await expect(page.getByText('Processo iniciado')).toBeVisible();

        // 2. Chefe adiciona atividades e conhecimentos
        await login(page, USUARIO_CHEFE, SENHA_CHEFE);

        await acessarSubprocessoChefeDireto(page, descProcesso);
        await navegarParaAtividades(page);

        await adicionarAtividade(page, ATIVIDADE_1);
        await adicionarConhecimento(page, ATIVIDADE_1, CONHECIMENTO_1);

        await adicionarAtividade(page, ATIVIDADE_2);
        await adicionarConhecimento(page, ATIVIDADE_2, CONHECIMENTO_2);

        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();

        // 3. Gestor aceita
        await login(page, USUARIO_GESTOR, SENHA_GESTOR);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // 4. Admin homologa
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await page.getByTestId('card-subprocesso-atividades-vis').click();
        await homologarCadastroMapeamento(page);

        // Agora deve estar em "Cadastro homologado"
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
    });

    test('CT-00 e CT-01: Acessar Edição de Mapa e verificar elementos', async ({page}) => {
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);

        // Verificar card do mapa
        await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
        await page.getByTestId('card-subprocesso-mapa').click();

        // Verificar título
        await expect(page.getByRole('heading', {name: /Mapa de competências/i})).toBeVisible();

        // Verificar botões principais
        await expect(
            page.getByTestId('btn-abrir-criar-competencia').or(page.getByTestId('btn-abrir-criar-competencia-empty'))
        ).toBeVisible();

        // Botão disponibilizar deve estar desabilitado se não houver competências
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();
    });

    test('CT-02: Criar Competência', async ({page}) => {
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const compDesc = `Competência 1 ${timestamp}`;
        await criarCompetencia(page, compDesc, [ATIVIDADE_1]);

        // Verificar mudança de situação
        await verificarSituacaoSubprocesso(page, 'Mapa criado');

        // Verificar se botão disponibilizar ficou habilitado
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeEnabled();
    });

    test('CT-03: Editar Competência', async ({page}) => {
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const oldDesc = `Competência 1 ${timestamp}`;
        const newDesc = `Competência 1 Editada ${timestamp}`;

        // Editar descrição e adicionar mais uma atividade
        await editarCompetencia(page, oldDesc, newDesc, [ATIVIDADE_2]);

        // Verificar que agora tem as duas atividades
        await verificarCompetenciaNoMapa(page, newDesc, [ATIVIDADE_1, ATIVIDADE_2]);
    });

    test('CT-05: Validar Cancelamento da Exclusão', async ({page}) => {
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const compDesc = `Competência 1 Editada ${timestamp}`;

        await excluirCompetenciaCancelando(page, compDesc);

        await expect(page.getByText(compDesc).first()).toBeVisible();
    });

    test('CT-04: Excluir Competência com Confirmação', async ({page}) => {
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const compDesc = `Competência 1 Editada ${timestamp}`;

        await excluirCompetenciaConfirmando(page, compDesc);

        // Lista deve estar vazia agora, botão disponibilizar deve desabilitar
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();
    });

    test('CT-06: Navegar para Disponibilização', async ({page}) => {
        // Recriar uma competência para poder disponibilizar
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const compDesc = `Competência Final ${timestamp}`;
        await criarCompetencia(page, compDesc, [ATIVIDADE_1, ATIVIDADE_2]);

        await disponibilizarMapa(page);

        // Após disponibilizar, deve redirecionar para o painel
        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByRole('heading', {name: /Mapa disponibilizado/i})).toBeVisible();
    });
});
