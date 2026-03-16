import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/fixtures-processos.js';
import {
    abrirModalImpactoEdicao,
    abrirModalImpactoVisualizacao,
    adicionarAtividade,
    adicionarConhecimento,
    editarAtividade,
    fecharModalImpacto,
    navegarParaAtividades,
    navegarParaAtividadesVisualizacao,
    verificarBotaoImpactoDireto,
    verificarBotaoImpactoDropdown
} from './helpers/helpers-atividades.js';
import {acessarSubprocessoChefeDireto, acessarSubprocessoGestor} from './helpers/helpers-analise.js';

test.describe.serial('CDU-12 - Verificar impactos no mapa de competências', () => {
    const UNIDADE_ALVO = 'SECAO_121';
    const timestamp = Date.now();
    const descProcessoRevisao = `Revisão CDU-12 ${timestamp}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        // Criar processo mapeamento finalizado para gerar o Mapa vigente
        await criarProcessoFinalizadoFixture(request, {
            unidade: UNIDADE_ALVO,
            descricao: `Base map CDU-12 ${timestamp}`
        });

        // Iniciar processo de Revisão
        await criarProcessoFixture(request, {
            descricao: descProcessoRevisao,
            tipo: 'REVISAO',
            unidade: UNIDADE_ALVO,
            iniciar: true
        });
        expect(true).toBeTruthy();
    });

    test('Passo 3.1: Verificação pelo CHEFE na tela de Cadastro', async ({_resetAutomatico, page, _autenticadoComoChefeSecao121}) => {
        await acessarSubprocessoChefeDireto(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividades(page);

        // 5.1. Detectar INCLUSÃO
        const novaAtividade = `Nova atividade ${timestamp}`;
        await adicionarAtividade(page, novaAtividade);
        await adicionarConhecimento(page, novaAtividade, 'Conhecimento da nova');

        // 5.2. Detectar ALTERAÇÃO (em atividade que acabamos de criar para evitar problemas com a base SQL)
        const descEditada = `Nova ativ editada ${timestamp}`;
        await editarAtividade(page, novaAtividade, descEditada);

        await verificarBotaoImpactoDropdown(page);
        await abrirModalImpactoEdicao(page);

        // 7.1. Verificar seção Atividades inseridas
        const modal = page.getByRole('dialog');
        await expect(modal.getByRole('heading', {name: /Atividades inseridas/i})).toBeVisible();
        await expect(modal.getByText(descEditada)).toBeVisible();

        // 8 e 9. Fechar modal
        await fecharModalImpacto(page);
        
        // Disponibilizar para permitir teste dos próximos atores
        await page.getByTestId('btn-cad-atividades-disponibilizar').click();
        await page.getByTestId('btn-confirmar-disponibilizacao').click();
    });

    test('Passo 3.2: Verificação pelo GESTOR na tela de Visualização', async ({_resetAutomatico, page}) => {

        // Localização atual deve estar no COORD_12 para o Gestor ver
        // Ringo starr (GESTOR_COORD_12) possui apenas 1 perfil
        await login(page, USUARIOS.GESTOR_COORD_12.titulo, USUARIOS.GESTOR_COORD_12.senha);
        await acessarSubprocessoGestor(page, descProcessoRevisao, UNIDADE_ALVO);
        await navegarParaAtividadesVisualizacao(page);

        await verificarBotaoImpactoDireto(page);
        await abrirModalImpactoVisualizacao(page);
        
        const modal = page.getByRole('dialog');
        await expect(modal.getByRole('heading', {name: /Atividades inseridas/i})).toBeVisible();
        
        await fecharModalImpacto(page);
    });
});
