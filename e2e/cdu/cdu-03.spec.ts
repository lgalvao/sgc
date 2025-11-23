import { vueTest as test } from '../support/vue-specific-setup';
import { PaginaPainel, PaginaProcesso } from '~/helpers';
import {expect} from "@playwright/test";

test.describe('CDU-03: Manter processo', () => {
    let paginaPainel: PaginaPainel;
    let paginaProcesso: PaginaProcesso;

    test.beforeEach(async ({ page }) => {
        paginaPainel = new PaginaPainel(page);
        paginaProcesso = new PaginaProcesso(page);
        await paginaPainel.loginComoAdmin();
    });

    // ===== CRIAÇÃO DE PROCESSO =====

    test('deve criar processo e redirecionar para o Painel', async () => {
        const descricao = `Processo E2E ${Date.now()}`;

        await paginaPainel.irParaCriacaoDeProcesso();
        
        await paginaProcesso.preencherDescricao(descricao);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.selecionarUnidadesPorSigla(['STIC']);
        await paginaProcesso.clicarBotaoSalvar();

        await paginaPainel.aguardarProcessoNoPainel(descricao);
    });

    test('deve validar descrição obrigatória', async () => {
        await paginaPainel.irParaCriacaoDeProcesso();

        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.selecionarUnidadesPorSigla(['STIC']);
        await paginaProcesso.clicarBotaoSalvar();

        await paginaProcesso.verificarPaginaDeCadastro(); // Deve permanecer na página
    });

    test('deve validar ao menos uma unidade selecionada', async () => {
        await paginaPainel.irParaCriacaoDeProcesso();

        await paginaProcesso.preencherDescricao('Processo sem unidades');
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.clicarBotaoSalvar();

        await paginaProcesso.verificarPaginaDeCadastro(); // Deve permanecer na página
    });

    // ===== EDIÇÃO DE PROCESSO =====

    test('deve abrir processo para edição e modificar descrição', async () => {
        const descricaoOriginal = `Processo para Editar ${Date.now()}`;
        const novaDescricao = `Processo Editado ${Date.now()}`;

        // Criação
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.preencherDescricao(descricaoOriginal);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31'); // <-- FIX: Data limite ausente
        await paginaProcesso.selecionarUnidadesPorSigla(['SEDESENV']);
        await paginaProcesso.clicarBotaoSalvar();
        await paginaPainel.aguardarProcessoNoPainel(descricaoOriginal);

        // Edição
        await paginaPainel.clicarProcessoNaTabela(descricaoOriginal);
        await paginaProcesso.verificarPaginaDeEdicao();
        await paginaProcesso.verificarValorDescricao(descricaoOriginal);
        
        await paginaProcesso.preencherDescricao(novaDescricao);
        await paginaProcesso.verificarValorDescricao(novaDescricao);
        
        await paginaProcesso.clicarBotaoSalvar();
        
        // Após salvar, deve redirecionar para o Painel (comportamento padrão de "Salvar" no sistema)
        await paginaPainel.verificarUrlDoPainel();
        await paginaPainel.aguardarProcessoNoPainel(novaDescricao);
    });

    test('deve exibir botão Remover apenas em modo de edição', async () => {
        // Na criação, o botão não deve existir
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.verificarBotaoRemoverVisivel(false);

        // Após criar e abrir para edição, o botão deve aparecer
        const descricao = `Processo com Botão Remover ${Date.now()}`;
        await paginaProcesso.preencherDescricao(descricao);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.selecionarUnidadesPorSigla(['SEDESENV']);
        await paginaProcesso.clicarBotaoSalvar();
        await paginaPainel.aguardarProcessoNoPainel(descricao);
        
        await paginaPainel.clicarProcessoNaTabela(descricao);
        await paginaProcesso.verificarPaginaDeEdicao();
        await paginaProcesso.verificarBotaoRemoverVisivel(true);
    });

    // ===== REMOÇÃO DE PROCESSO =====

    test('deve cancelar remoção e permanecer na tela de edição', async () => {
        const descricao = `Processo para Cancelar Remoção ${Date.now()}`;
        
        // Criação
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.preencherDescricao(descricao);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.selecionarUnidadesPorSigla(['SEDESENV']);
        await paginaProcesso.clicarBotaoSalvar();
        await paginaPainel.aguardarProcessoNoPainel(descricao);
        
        // Ação de cancelar
        await paginaPainel.clicarProcessoNaTabela(descricao);
        await paginaProcesso.verificarPaginaDeEdicao();
        await paginaProcesso.clicarBotaoRemover();
        await paginaProcesso.verificarDialogoConfirmacaoRemocao(descricao);
        await paginaProcesso.cancelarNoModal();

        // Verificação
        await paginaProcesso.verificarModalFechado();
        await paginaProcesso.verificarPaginaDeEdicao();
    });

    test('deve remover processo após confirmação', async () => {
        const descricao = `Processo para Remover ${Date.now()}`;

        // Criação
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.preencherDescricao(descricao);
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        await paginaProcesso.selecionarUnidadesPorSigla(['SEDESENV']);
        await paginaProcesso.clicarBotaoSalvar();
        await paginaPainel.aguardarProcessoNoPainel(descricao);

        // Ação de remover
        await paginaPainel.clicarProcessoNaTabela(descricao);
        await paginaProcesso.verificarPaginaDeEdicao();
        await paginaProcesso.clicarBotaoRemover();
        await paginaProcesso.verificarDialogoConfirmacaoRemocao(descricao);
        await paginaProcesso.confirmarRemocaoNoModal();

        // Verificação
        await paginaPainel.verificarUrlDoPainel();
        await paginaPainel.verificarProcessoNaoVisivel(descricao);
    });

    // ===== REGRAS DE SELEÇÃO DE UNIDADES (ÁRVORE) =====

    test('deve selecionar automaticamente as unidades filhas ao selecionar o pai', async ({ page }) => {
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');

        const paiSigla = 'COSIS';
        const filhosSiglas = ['SEDESENV', 'SEDIA', 'SESEL'];

        const checkboxPai = page.locator(`#chk-${paiSigla}`);
        const checkboxesFilhos = filhosSiglas.map(sigla => page.locator(`#chk-${sigla}`));

        // Garante que o pai e os filhos existem antes de interagir
        await expect(checkboxPai).toBeVisible();
        for (const checkbox of checkboxesFilhos) {
            await expect(checkbox).toBeVisible();
        }

        // Selecionar Pai
        await checkboxPai.check();

        // Verificar Pai e todos os Filhos marcados
        await expect(checkboxPai).toBeChecked();
        for (const checkbox of checkboxesFilhos) {
            await expect(checkbox).toBeChecked();
        }

        // Desmarcar Pai
        await checkboxPai.uncheck();

        // Verificar Pai e todos os Filhos desmarcados
        await expect(checkboxPai).not.toBeChecked();
        for (const checkbox of checkboxesFilhos) {
            await expect(checkbox).not.toBeChecked();
        }
    });

    test('deve selecionar o pai automaticamente ao selecionar todas as filhas', async ({ page }) => {
        await paginaPainel.irParaCriacaoDeProcesso();
        await paginaProcesso.selecionarTipoProcesso('MAPEAMENTO');

        const paiSigla = 'COSIS';
        const filhosSiglas = ['SEDESENV', 'SEDIA', 'SESEL'];

        const checkboxPai = page.locator(`#chk-${paiSigla}`);
        const checkboxesFilhos = filhosSiglas.map(sigla => page.locator(`#chk-${sigla}`));

        // Garante que o pai e os filhos existem antes de interagir
        await expect(checkboxPai).toBeVisible();
        for (const checkbox of checkboxesFilhos) {
            await expect(checkbox).toBeVisible();
        }

        // Marcar todos os filhos
        for (const checkbox of checkboxesFilhos) {
            await checkbox.check();
        }

        // Verificar se o pai foi marcado automaticamente
        await expect(checkboxPai).toBeChecked();

        // Desmarcar um filho
        await checkboxesFilhos[0].uncheck();

        // Verificar se o pai foi desmarcado
        await expect(checkboxPai).not.toBeChecked();
    });

    // ===== VALIDAÇÃO DE REVISÃO =====

    test('deve validar unidades sem mapa ao criar processo de Revisão', async () => {
        await paginaPainel.irParaCriacaoDeProcesso();

        await paginaProcesso.preencherDescricao('Processo Revisão Inválido');
        await paginaProcesso.selecionarTipoProcesso('REVISAO');
        await paginaProcesso.preencherDataLimite('2025-12-31');
        
        // Seleciona todas as unidades para garantir que pegamos alguma sem mapa (ou use uma específica se souber)
        // Assumindo que 'STIC' não tem mapa no seed inicial.
        await paginaProcesso.selecionarUnidadesPorSigla(['STIC']);
        
        await paginaProcesso.clicarBotaoSalvar();

        // Deve exibir erro (não redirecionar)
        // A mensagem de erro aparece em um toast ou modal de erro?
        // O código CadProcesso.vue usa notificacoesStore.erro() -> provavelmente um toast.
        // Verificamos se permaneceu na página
        await paginaProcesso.verificarPaginaDeCadastro();
        
        // Opcional: Verificar mensagem de erro específica
        // await expect(page.locator('text=não possuem mapa vigente')).toBeVisible();
    });
});

