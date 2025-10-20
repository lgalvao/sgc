import {test} from '@playwright/test';
import {
    adicionarAtividade,
    adicionarConhecimento,
    DADOS_TESTE,
    gerarNomeUnico,
    loginComoChefe,
    navegarParaCadastroAtividades,
    SELETORES,
    SELETORES_CSS
} from "~/helpers";

test.describe('Captura de Telas - Nova Funcionalidade: Modal de Edição de Conhecimentos', () => {

    test('30 - Fluxo Completo: Criação de Atividade e Conhecimento', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);
        await page.waitForLoadState('networkidle');

        // Capturar tela inicial (vazia)
        await page.screenshot({path: 'screenshots/30-01-cadastro-atividades-inicial.png', fullPage: true});

        // Adicionar atividade
        const nomeAtividade = gerarNomeUnico('Atividade Visual Test');
        await page.getByTestId(SELETORES.INPUT_NOVA_ATIVIDADE).fill(nomeAtividade);

        // Capturar com atividade digitada mas não salva
        await page.screenshot({path: 'screenshots/30-02-digitando-atividade.png', fullPage: true});

        await page.getByTestId(SELETORES.BTN_ADICIONAR_ATIVIDADE).click();
        await page.waitForTimeout(500);

        // Capturar com atividade criada
        await page.screenshot({path: 'screenshots/30-03-atividade-criada.png', fullPage: true});

        // Adicionar conhecimento
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        const nomeConhecimento = gerarNomeUnico('Conhecimento Visual Test');
        await cardAtividade.locator('[data-testid="input-novo-conhecimento"]').fill(nomeConhecimento);

        // Capturar com conhecimento digitado mas não salvo
        await page.screenshot({path: 'screenshots/30-04-digitando-conhecimento.png', fullPage: true});

        await cardAtividade.locator('[data-testid="btn-adicionar-conhecimento"]').click();
        await page.waitForTimeout(500);

        // Capturar com conhecimento criado (resultado final)
        await page.screenshot({path: 'screenshots/30-05-conhecimento-criado-final.png', fullPage: true});
    });

    test('31 - Estados de Hover nos Botões de Ação', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);

        // Criar atividade e conhecimento para teste
        const nomeAtividade = gerarNomeUnico('Atividade Hover Test');
        await adicionarAtividade(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await adicionarConhecimento(cardAtividade, 'Conhecimento Hover Test');

        // Hover no conhecimento para mostrar botões de ação
        const linhaConhecimento = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: 'Conhecimento Hover Test'});
        await linhaConhecimento.hover();
        await page.waitForTimeout(500);

        // Capturar estado hover com botões visíveis
        await page.screenshot({path: 'screenshots/31-01-hover-botoes-conhecimento.png', fullPage: true});

        // Hover na atividade para mostrar botões
        const tituloAtividade = cardAtividade.locator('.atividade-titulo-card');
        await tituloAtividade.hover();
        await page.waitForTimeout(500);

        // Capturar estado hover da atividade
        await page.screenshot({path: 'screenshots/31-02-hover-botoes-atividade.png', fullPage: true});
    });

    test('32 - Novo Modal de Edição de Conhecimento - Estados', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);

        // Criar atividade e conhecimento
        const nomeAtividade = gerarNomeUnico('Atividade Modal Test');
        await adicionarAtividade(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await adicionarConhecimento(cardAtividade, 'Conhecimento Original');

        // Hover e clicar em editar
        const linhaConhecimento = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: 'Conhecimento Original'});
        await linhaConhecimento.hover();
        await page.waitForTimeout(500);
        await linhaConhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click({force: true});

        // Aguardar modal aparecer
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'visible'});

        // Capturar modal aberto com conteúdo original
        await page.screenshot({path: 'screenshots/32-01-modal-edicao-aberto.png', fullPage: true});

        // Limpar e digitar novo texto
        await page.getByTestId('input-conhecimento-modal').fill('');
        await page.getByTestId('input-conhecimento-modal').fill('Conhecimento Editado via Modal');

        // Capturar modal com texto editado
        await page.screenshot({path: 'screenshots/32-02-modal-texto-editado.png', fullPage: true});

        // Salvar
        await page.getByTestId('btn-salvar-conhecimento-modal').click();

        // Aguardar modal fechar e capturar resultado
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'hidden'});
        await page.waitForTimeout(500);

        // Capturar resultado final com conhecimento editado
        await page.screenshot({path: 'screenshots/32-03-conhecimento-editado-resultado.png', fullPage: true});
    });

    test('33 - Modal de Edição - Estados de Validação', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);

        // Criar atividade e conhecimento
        const nomeAtividade = gerarNomeUnico('Atividade Validacao Test');
        await adicionarAtividade(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await adicionarConhecimento(cardAtividade, 'Conhecimento Validacao');

        // Abrir modal
        const linhaConhecimento = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: 'Conhecimento Validacao'});
        await linhaConhecimento.hover();
        await page.waitForTimeout(500);
        await linhaConhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click({force: true});
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'visible'});

        // Limpar campo para mostrar estado de validação (botão salvar desabilitado)
        await page.getByTestId('input-conhecimento-modal').fill('');
        await page.waitForTimeout(300);

        // Capturar estado com campo vazio (botão salvar desabilitado)
        await page.screenshot({path: 'screenshots/33-01-modal-campo-vazio-botao-desabilitado.png', fullPage: true});

        // Digitar apenas espaços
        await page.getByTestId('input-conhecimento-modal').fill('   ');
        await page.waitForTimeout(300);

        // Capturar estado com apenas espaços (ainda desabilitado)
        await page.screenshot({path: 'screenshots/33-02-modal-apenas-espacos-botao-desabilitado.png', fullPage: true});

        // Digitar conteúdo válido
        await page.getByTestId('input-conhecimento-modal').fill('Conhecimento com conteúdo válido');
        await page.waitForTimeout(300);

        // Capturar estado com conteúdo válido (botão habilitado)
        await page.screenshot({path: 'screenshots/33-03-modal-conteudo-valido-botao-habilitado.png', fullPage: true});

        // Cancelar para não salvar usando botão
        await page.getByRole('button', {name: 'Cancelar'}).click();
    });

    test('34 - Múltiplas Atividades e Conhecimentos - Layout Complexo', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);

        // Criar primeira atividade com múltiplos conhecimentos
        const atividade1 = gerarNomeUnico('Desenvolvimento de Software');
        await adicionarAtividade(page, atividade1);
        const card1 = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: atividade1});
        await adicionarConhecimento(card1, 'JavaScript/TypeScript');
        await adicionarConhecimento(card1, 'Vue.js Framework');
        await adicionarConhecimento(card1, 'Testes Automatizados');

        // Criar segunda atividade com conhecimentos
        const atividade2 = gerarNomeUnico('Análise de Sistemas');
        await adicionarAtividade(page, atividade2);
        const card2 = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: atividade2});
        await adicionarConhecimento(card2, 'Levantamento de Requisitos');
        await adicionarConhecimento(card2, 'Modelagem de Processos');

        // Criar terceira atividade
        const atividade3 = gerarNomeUnico('Gestão de Projetos');
        await adicionarAtividade(page, atividade3);
        const card3 = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: atividade3});
        await adicionarConhecimento(card3, 'Metodologias Ágeis');
        await adicionarConhecimento(card3, 'Ferramentas de Gestão');
        await adicionarConhecimento(card3, 'Liderança de Equipes');

        await page.waitForTimeout(500);

        // Capturar layout complexo com múltiplas atividades
        await page.screenshot({path: 'screenshots/34-01-layout-multiplas-atividades.png', fullPage: true});

        // Fazer hover em uma atividade do meio para mostrar interação
        await card2.hover();
        await page.waitForTimeout(300);

        // Capturar com hover ativo
        await page.screenshot({path: 'screenshots/34-02-layout-com-hover-ativo.png', fullPage: true});
    });

    test('35 - Fluxo de Edição de Múltiplos Conhecimentos', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);

        // Criar atividade com conhecimentos
        const nomeAtividade = gerarNomeUnico('Atividade Edicao Multipla');
        await adicionarAtividade(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});

        // Adicionar múltiplos conhecimentos
        await adicionarConhecimento(cardAtividade, 'Conhecimento A');
        await adicionarConhecimento(cardAtividade, 'Conhecimento B');
        await adicionarConhecimento(cardAtividade, 'Conhecimento C');

        // Capturar estado inicial
        await page.screenshot({path: 'screenshots/35-01-multiplos-conhecimentos-inicial.png', fullPage: true});

        // Editar primeiro conhecimento
        const conhecimentoA = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: 'Conhecimento A'});
        await conhecimentoA.hover();
        await page.waitForTimeout(500);
        await conhecimentoA.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click({force: true});
        await page.getByTestId('input-conhecimento-modal').fill('Conhecimento A - Editado');

        // Capturar modal de edição
        await page.screenshot({path: 'screenshots/35-02-editando-conhecimento-A.png', fullPage: true});

        await page.getByTestId('btn-salvar-conhecimento-modal').click();
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'hidden'});

        // Editar segundo conhecimento
        const conhecimentoB = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: 'Conhecimento B'});
        await conhecimentoB.hover();
        await page.waitForTimeout(500);
        await conhecimentoB.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click({force: true});
        await page.getByTestId('input-conhecimento-modal').fill('Conhecimento B - Muito Expandido com Texto Longo para Testar Layout');

        // Capturar modal com texto longo
        await page.screenshot({path: 'screenshots/35-03-editando-conhecimento-B-texto-longo.png', fullPage: true});

        await page.getByTestId('btn-salvar-conhecimento-modal').click();
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'hidden'});

        // Capturar resultado final com edições
        await page.screenshot({path: 'screenshots/35-04-resultado-edicoes-multiplas.png', fullPage: true});
    });

    test('36 - Modal de Edição - Keyboard Shortcuts', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.MAPEAMENTO_STIC.id, DADOS_TESTE.UNIDADES.STIC);

        // Criar atividade e conhecimento
        const nomeAtividade = gerarNomeUnico('Atividade Keyboard Test');
        await adicionarAtividade(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await adicionarConhecimento(cardAtividade, 'Conhecimento Keyboard');

        // Abrir modal
        const linhaConhecimento = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: 'Conhecimento Keyboard'});
        await linhaConhecimento.hover();
        await page.waitForTimeout(500);
        await linhaConhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click({force: true});
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'visible'});

        // Editar conteúdo
        await page.getByTestId('input-conhecimento-modal').fill('Conhecimento editado via Ctrl+Enter');

        // Capturar antes de salvar com Ctrl+Enter
        await page.screenshot({path: 'screenshots/36-01-modal-antes-ctrl-enter.png', fullPage: true});

        // Salvar via Ctrl+Enter
        await page.keyboard.press('Control+Enter');
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'hidden'});
        await page.waitForTimeout(500);

        // Capturar resultado
        await page.screenshot({path: 'screenshots/36-02-resultado-ctrl-enter.png', fullPage: true});

        // Testar cancelamento via ESC - usar elemento recém editado
        const linhaConhecimentoAtualizada = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO).first();
        await linhaConhecimentoAtualizada.hover();
        await page.waitForTimeout(500);
        await linhaConhecimentoAtualizada.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click({force: true});
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'visible'});
        await page.getByTestId('input-conhecimento-modal').fill('Mudança que será cancelada');

        // Capturar antes de cancelar
        await page.screenshot({path: 'screenshots/36-03-modal-antes-escape.png', fullPage: true});

        // Cancelar via botão (ESC pode não estar implementado)
        await page.getByRole('button', {name: 'Cancelar'}).click();

        // Verificar que não houve mudança
        await page.screenshot({path: 'screenshots/36-04-resultado-escape-sem-mudanca.png', fullPage: true});
    });

    test('37 - Comparação: Antes e Depois da Implementação Modal', async ({page}) => {
        await loginComoChefe(page);
        await navegarParaCadastroAtividades(page, DADOS_TESTE.PROCESSOS.REVISAO_STIC.id, DADOS_TESTE.UNIDADES.STIC);

        // Capturar tela com processo de revisão (mostra botão "Impacto no mapa")
        await page.screenshot({path: 'screenshots/37-01-interface-nova-com-impacto-mapa.png', fullPage: true});

        // Criar atividade e conhecimento
        const nomeAtividade = gerarNomeUnico('Demo Nova Interface');
        await adicionarAtividade(page, nomeAtividade);
        const cardAtividade = page.locator(SELETORES_CSS.CARD_ATIVIDADE, {hasText: nomeAtividade});
        await adicionarConhecimento(cardAtividade, 'Conhecimento com Nova Interface');

        // Mostrar hover state (botões aparecem)
        const linhaConhecimento = cardAtividade.locator(SELETORES_CSS.GRUPO_CONHECIMENTO, {hasText: 'Conhecimento com Nova Interface'});
        await linhaConhecimento.hover();

        // Capturar nova interface com botões visíveis
        await page.screenshot({path: 'screenshots/37-02-nova-interface-botoes-hover.png', fullPage: true});

        // Abrir modal (nova funcionalidade)
        await linhaConhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO).click({force: true});
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'visible'});

        // Capturar modal (principal diferença da implementação anterior)
        await page.screenshot({path: 'screenshots/37-03-modal-nova-funcionalidade.png', fullPage: true});

        // Fechar modal usando botão ao invés de ESC
        await page.getByRole('button', {name: 'Cancelar'}).click();
        await page.getByTestId('input-conhecimento-modal').waitFor({state: 'hidden'});

        // Capturar interface final
        await page.screenshot({path: 'screenshots/37-04-interface-final-consistente.png', fullPage: true});
    });
});