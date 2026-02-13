import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
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

        const linhaProcesso = page.locator('tr').filter({has: page.getByText(descProcesso)});
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

        // 3. Gestor aceita
        await login(page, USUARIOS.GESTOR_COORD_21.titulo, USUARIOS.GESTOR_COORD_21.senha);

        await acessarSubprocessoGestor(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await aceitarCadastroMapeamento(page);

        // 4. Admin homologa
        await login(page, USUARIO_ADMIN, SENHA_ADMIN);

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);
        await homologarCadastroMapeamento(page);

        // Agora deve estar em "Cadastro homologado"
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(/Cadastro homologado/i);
    });

    test('CT-00 e CT-01: Acessar Edição de Mapa e verificar elementos', async ({page, autenticadoComoAdmin}) => {
        

        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);

        await navegarParaMapa(page);

        // Verificar título
        await expect(page.getByRole('heading', {name: /Mapa de competências/i})).toBeVisible();

        // Verificar botões principais
        await expect(
            page.getByTestId('btn-abrir-criar-competencia').or(page.getByTestId('btn-abrir-criar-competencia-empty'))
        ).toBeVisible();

        // Botão disponibilizar deve estar desabilitado se não houver competências
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();
    });

    test('CT-02: Criar Competência', async ({page, autenticadoComoAdmin}) => {
        
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const compDesc = `Competência 1 ${timestamp}`;
        await criarCompetencia(page, compDesc, [ATIVIDADE_1]);

        // Verificar mudança de situação
        await verificarSituacaoSubprocesso(page, 'Mapa criado');

        // Verificar se botão disponibilizar ficou habilitado
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeEnabled();
    });

    test('CT-03: Editar Competência', async ({page, autenticadoComoAdmin}) => {
        
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const oldDesc = `Competência 1 ${timestamp}`;
        const newDesc = `Competência 1 Editada ${timestamp}`;

        // Editar descrição e adicionar mais uma atividade
        await editarCompetencia(page, oldDesc, newDesc, [ATIVIDADE_2]);

        // Verificar que agora tem as duas atividades
        await verificarCompetenciaNoMapa(page, newDesc, [ATIVIDADE_1, ATIVIDADE_2]);
    });

    test('CT-05: Validar Cancelamento da Exclusão', async ({page, autenticadoComoAdmin}) => {
        
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const compDesc = `Competência 1 Editada ${timestamp}`;

        await excluirCompetenciaCancelando(page, compDesc);

        await expect(page.getByText(compDesc).first()).toBeVisible();
    });

    test('CT-04: Excluir Competência com Confirmação', async ({page, autenticadoComoAdmin}) => {
        
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const compDesc = `Competência 1 Editada ${timestamp}`;

        await excluirCompetenciaConfirmando(page, compDesc);

        // Lista deve estar vazia agora, botão disponibilizar deve desabilitar
        await expect(page.getByTestId('btn-cad-mapa-disponibilizar')).toBeDisabled();
    });

    test('CT-06: Navegar para Disponibilização', async ({page, autenticadoComoAdmin}) => {
        // Recriar uma competência para poder disponibilizar
        
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        const compDesc = `Competência Final ${timestamp}`;
        await criarCompetencia(page, compDesc, [ATIVIDADE_1, ATIVIDADE_2]);

        await disponibilizarMapa(page);

        // Após disponibilizar, deve redirecionar para o painel
        await expect(page).toHaveURL(/\/painel/);
        await expect(page.getByText(/Mapa disponibilizado/i)).toBeVisible();
    });
});
