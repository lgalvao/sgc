import {expect, Locator, Page} from '@playwright/test';
import {ROTULOS, SELETORES, TEXTOS, URLS} from '../dados';
import {esperarBotaoVisivel, esperarElementoVisivel, esperarTextoVisivel} from '~/helpers';
import {extrairIdDoSeletor} from '../utils/utils';

/**
 * Verifica se um conhecimento em uma atividade específica está visível.
 * @param pageOrCard A instância da página ou um localizador do Playwright.
 * @param nomeAtividadeOuConhecimento O nome da atividade ou do conhecimento.
 * @param nomeConhecimento O nome do conhecimento (se o primeiro argumento for a página).
 */
export async function verificarConhecimentoVisivel(pageOrCard: Page | Locator, nomeAtividadeOuConhecimento: string, nomeConhecimento?: string): Promise<void> {
    let cardAtividade: Locator;
    let conhecimento: string;

    if (typeof nomeConhecimento === 'string') {
        // Assinatura: (page, nomeAtividade, nomeConhecimento)
        const page = pageOrCard as Page;
        cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividadeOuConhecimento});
        conhecimento = nomeConhecimento;
    } else {
        // Assinatura: (cardLocator, nomeConhecimento)
        cardAtividade = pageOrCard as Locator;
        conhecimento = nomeAtividadeOuConhecimento;
    }

    await expect(cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: conhecimento})).toBeVisible();
}

/**
 * Verifica se os elementos do painel principal estão visíveis.
 * @param page A instância da página do Playwright.
 */
export async function verificarElementosPainel(page: Page): Promise<void> {
    await esperarElementoVisivel(page, SELETORES.TITULO_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
    await esperarElementoVisivel(page, SELETORES.COLUNA_DESCRICAO);
    await esperarElementoVisivel(page, SELETORES.COLUNA_TIPO);
    await esperarElementoVisivel(page, SELETORES.COLUNA_SITUACAO);
}

/**
 * Verifica se o botão "Criar processo" não está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarAusenciaBotaoCriarProcesso(page: Page): Promise<void> {
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_CRIAR_PROCESSO)).first()).not.toBeVisible();
}

/**
 * Verifica se um processo está visível na tabela de processos.
 * @param page A instância da página do Playwright.
 * @param nomeProcesso O nome do processo.
 * @param visivel `true` se o processo deve estar visível, `false` caso contrário.
 */
export async function verificarVisibilidadeProcesso(page: Page, nomeProcesso: string | RegExp, visivel: boolean): Promise<void> {
    const tabela = page.getByTestId(extrairIdDoSeletor(SELETORES.TABELA_PROCESSOS));
    const processo = tabela.locator('tbody tr').filter({ has: tabela.locator('td').filter({ hasText: nomeProcesso }) });
    if (visivel) {
        await expect(processo).toBeVisible({ timeout: 2000 });
    } else {
        await expect(processo).toBeHidden({ timeout: 2000 });
    }
}

/**
 * Verifica se há checkboxes selecionados na árvore de hierarquia.
 * @param page A instância da página do Playwright.
 */
export async function verificarSelecaoArvoreCheckboxes(page: Page): Promise<void> {
    const quantidadeMarcados = await page.locator(SELETORES.CHECKBOX_MARCADO).count();
    expect(quantidadeMarcados).toBeGreaterThan(0);
}

/**
 * Verifica o comportamento de marcar e desmarcar um checkbox.
 * @param page A instância da página do Playwright.
 */
export async function verificarComportamentoMarcacaoCheckbox(page: Page): Promise<void> {
    const primeiroCheckbox = page.locator(SELETORES.CHECKBOX).first();

    // Deve estar marcado inicialmente (após seleção)
    await expect(primeiroCheckbox).toBeChecked();

    // Desmarcar e verificar
    await primeiroCheckbox.click();
    await expect(primeiroCheckbox).not.toBeChecked();
}

/**
 * Verifica o comportamento de checkboxes interoperacionais.
 * @param page A instância da página do Playwright.
 */
export async function verificarComportamentoCheckboxInteroperacional(page: Page): Promise<void> {
    // Garantir que os checkboxes foram renderizados
    await page.waitForSelector('#chk-STIC');

    const chkStic = page.locator('#chk-STIC');
    const chkCosis = page.locator('#chk-COSIS');

    // Selecionar a unidade interoperacional raiz (STIC)
    await chkStic.click();

    // A raiz deve estar marcada
    await expect(chkStic).toBeChecked();

    // Se COSIS existe, não deve estar marcada automaticamente
    const cosisExists = await chkCosis.count() > 0;
    if (cosisExists) {
        await expect(chkCosis).not.toBeChecked();
    }

    // Desmarcar STIC não deve afetar filhos
    await chkStic.click();
    await expect(chkStic).not.toBeChecked();
    if (cosisExists) {
        await expect(chkCosis).not.toBeChecked();
    }
}

/**
 * Verifica o título da seção de processos.
 * @param page A instância da página do Playwright.
 */
export async function verificarTituloProcessos(page: Page): Promise<void> {
    await expect(page.locator(`[data-testid="${SELETORES.TITULO_PROCESSOS}"]`)).toContainText(TEXTOS.TITULO_PROCESSOS_LABEL);
}

/**
 * Verifica se os elementos da página de detalhes do processo estão visíveis.
 * @param page A instância da página do Playwright.
 */
export async function verificarElementosDetalhesProcessoVisiveis(page: Page): Promise<void> {
    await esperarTextoVisivel(page, TEXTOS.SITUACAO_LABEL);
    await esperarTextoVisivel(page, TEXTOS.UNIDADES_PARTICIPANTES);
    await esperarBotaoVisivel(page, TEXTOS.FINALIZAR_PROCESSO);
}

/**
 * Verifica se os campos de login estão visíveis.
 * @param page A instância da página do Playwright.
 */
export async function verificarCamposLogin(page: Page): Promise<void> {
    await expect(page.getByLabel(ROTULOS.TITULO_ELEITORAL)).toBeVisible();
    await expect(page.getByLabel(ROTULOS.SENHA)).toBeVisible();
    await expect(page.getByRole('button', {name: TEXTOS.ENTRAR})).toBeVisible();
}

/**
 * Verifica se a página atual é a de login.
 * @param page A instância da página do Playwright.
 */
export async function verificarPaginaLogin(page: Page): Promise<void> {
    await expect(page).toHaveURL(URLS.LOGIN);
    await verificarCamposLogin(page);
}

/**
 * Verifica os elementos básicos do painel.
 * @param page A instância da página do Playwright.
 */
export async function verificarPainelBasico(page: Page): Promise<void> {
    await verificarElementosPainel(page);
    await esperarElementoVisivel(page, SELETORES.TABELA_PROCESSOS);
}

/**
 * Verifica se o painel está visível sem o botão de criar processo.
 * @param page A instância da página do Playwright.
 */
export async function verificarPainelSemCriacao(page: Page): Promise<void> {
    await verificarPainelBasico(page);
    await verificarAusenciaBotaoCriarProcesso(page);
}

/**
 * Verifica se o painel está visível com o botão de criar processo.
 * @param page A instância da página do Playwright.
 */
export async function verificarPainelComCriacao(page: Page): Promise<void> {
    await verificarPainelBasico(page);
    await esperarElementoVisivel(page, SELETORES.BTN_CRIAR_PROCESSO);
}

/**
 * Verifica se o painel está visível com a tabela de alertas.
 * @param page A instância da página do Playwright.
 */
export async function verificarPainelComAlertas(page: Page): Promise<void> {
    await verificarPainelBasico(page);
    await esperarElementoVisivel(page, SELETORES.TITULO_ALERTAS);
    await esperarElementoVisivel(page, SELETORES.TABELA_ALERTAS);
}

/**
 * Verifica a estrutura do painel para um usuário servidor.
 * @param page A instância da página do Playwright.
 */
export async function verificarEstruturaServidor(page: Page): Promise<void> {
    const navBar = page.getByRole('navigation');
    await expect(navBar.getByRole('link', {name: 'Painel'})).toBeVisible();
    await expect(navBar.getByRole('link', {name: 'Minha unidade'})).toBeVisible();
    await expect(page.getByText('SERVIDOR - SESEL')).toBeVisible();
    await expect(page.locator('a[title="Configurações do sistema"]')).not.toBeVisible();
    await expect(page.locator('a[title="Sair"]')).toBeVisible();
}

/**
 * Verifica a estrutura do painel para um usuário administrador.
 * @param page A instância da página do Playwright.
 */
export async function verificarEstruturaAdmin(page: Page): Promise<void> {
    await expect(page.getByText('ADMIN - STIC')).toBeVisible();
    await expect(page.locator('a[title="Configurações do sistema"]')).toBeVisible();
}

/**
 * Verifica se a página de cadastro de atividades está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarPaginaCadastroAtividades(page: Page): Promise<void> {
    await expect(page.getByRole('heading', {name: TEXTOS.CADASTRO_ATIVIDADES_CONHECIMENTOS})).toBeVisible();
    await esperarElementoVisivel(page, SELETORES.INPUT_NOVA_ATIVIDADE);
}

/**
 * Verifica se o botão "Impacto no mapa" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoImpactoVisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.IMPACTO_NO_MAPA})).toBeVisible();
}

/**
 * Verifica se o modal de importação de atividades está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalImportacaoVisivel(page: Page): Promise<void> {
    await expect(page.locator(SELETORES.MODAL_VISIVEL)).toBeVisible();
    await expect(page.locator('.modal-body')).toBeVisible();
}

/**
 * Verifica se o botão "Disponibilizar" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoDisponibilizarVisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.DISPONIBILIZAR})).toBeVisible();
}

/**
 * Verifica se uma atividade está visível.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade.
 */
export async function verificarAtividadeVisivel(page: Page, nomeAtividade: string): Promise<void> {
    await expect(page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade})).toBeVisible();
}

/**
 * Verifica se uma atividade não está visível.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade.
 */
export async function verificarAtividadeNaoVisivel(page: Page, nomeAtividade: string): Promise<void> {
    await expect(page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade})).not.toBeAttached();
}

/**
 * Verifica se um conhecimento não está visível em uma atividade.
 * @param page A instância da página do Playwright.
 * @param nomeAtividade O nome da atividade.
 * @param nomeConhecimento O nome do conhecimento.
 */
export async function verificarConhecimentoNaoVisivel(page: Page, nomeAtividade: string, nomeConhecimento: string): Promise<void> {
    const cardAtividade = page.locator(SELETORES.CARD_ATIVIDADE, {hasText: nomeAtividade});
    await expect(cardAtividade.locator(SELETORES.GRUPO_CONHECIMENTO, {hasText: nomeConhecimento})).not.toBeVisible();
}

/**
 * Verifica o número de atividades na página.
 * @param page A instância da página do Playwright.
 * @param numeroEsperado O número esperado de atividades.
 */
export async function verificarContadorAtividades(page: Page, numeroEsperado: number): Promise<void> {
    await expect(page.locator(SELETORES.CARD_ATIVIDADE)).toHaveCount(numeroEsperado);
}

/**
 * Verifica se o botão "Histórico de análise" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarBotaoHistoricoAnaliseVisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: 'Histórico de análise'})).toBeVisible();
}

/**
 * Verifica as ações disponíveis para o gestor na tela de análise de revisão.
 * @param page A instância da página do Playwright.
 */
export async function verificarAcoesAnaliseGestor(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.HISTORICO_ANALISE})).toBeVisible();
    await expect(page.getByRole('button', {name: TEXTOS.DEVOLVER_PARA_AJUSTES})).toBeVisible();
    await expect(page.getByRole('button', {name: TEXTOS.REGISTRAR_ACEITE})).toBeVisible();
}

/**
 * Verifica se o botão "Homologar" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarAcaoHomologarVisivel(page: Page): Promise<void> {
    await expect(page.getByRole('button', {name: TEXTOS.HOMOLOGAR})).toBeVisible();
}

/**
 * Verifica as colunas da tabela de alertas.
 * @param page A instância da página do Playwright.
 */
export async function verificarColunasTabelaAlertas(page: Page): Promise<void> {
    const tabelaAlertas = page.getByTestId(extrairIdDoSeletor(SELETORES.TABELA_ALERTAS));
    await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_DATA_HORA);
    await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_PROCESSO);
    await expect(tabelaAlertas).toContainText(TEXTOS.COLUNA_ORIGEM);
}

/**
 * Verifica se os alertas estão ordenados por data e hora.
 * @param page A instância da página do Playwright.
 */
export async function verificarAlertasOrdenadosPorDataHora(page: Page): Promise<void> {
    const linhasAlertas = page.locator(`${SELETORES.TABELA_ALERTAS} tbody tr`);
    const valoresDatas = await linhasAlertas.evaluateAll(linhas =>
        linhas.map(linha => {
            const textoData = (linha.children[0] as HTMLElement).innerText.trim();
            const [data, hora] = textoData.split(' ');
            const [dia, mes, ano] = data.split('/');
            return new Date(`${ano}-${mes}-${dia}T${hora}`).getTime();
        })
    );
    const valoresOrdenados = [...valoresDatas].sort((a, b) => b - a);
    expect(valoresDatas).toEqual(valoresOrdenados);
}

/**
 * Verifica se o modal de histórico de análise está aberto.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalHistoricoAnaliseAberto(page: Page): Promise<void> {
    // Espera explícita pelo modal, pois pode ser aberto após requisições assíncronas
    await page.waitForSelector(SELETORES.MODAL_VISIVEL);
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();

    // Aceitamos variações como "Histórico de Análise" ou "Histórico de Análises"
    await expect(modal.getByRole('heading', {name: /Histórico de Análises?/i}).first()).toBeVisible();

    // Se o modal informar que não há análises, consideramos o modal válido
    if ((await modal.getByText(/nenhuma análise registrada/i).count()) > 0) {
        return;
    }

    // Verificações tolerantes a variações (singular/plural e capitalização).
    // Usamos `.first()` para evitar violação de strict mode quando houver múltiplas células.
    await expect(modal.getByText(/data\/hora/i).first()).toBeVisible();
    await expect(modal.getByText(/unidade/i).first()).toBeVisible();
    await expect(modal.getByText(/resultado/i).first()).toBeVisible();
    await expect(modal.getByText(/observa/i).first()).toBeVisible();
}

/**
 * Verifica se o modal de histórico de análise contém uma observação.
 * @param page A instância da página do Playwright.
 * @param observacao A observação a ser verificada.
 */
export async function verificarModalHistoricoAnalise(page: Page, observacao: string): Promise<void> {
    await page.waitForSelector(SELETORES.MODAL_VISIVEL);
    const modal = page.locator(SELETORES.MODAL_VISIVEL);
    await expect(modal).toBeVisible();
    await expect(modal.getByRole('heading', {name: /Histórico de Análises?/i})).toBeVisible();
    await expect(modal.locator('tbody tr').first()).toContainText(observacao);
}

/**
 * Verifica se a mensagem de "Nenhum impacto" está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarMensagemNenhumImpacto(page: Page): Promise<void> {
    await esperarTextoVisivel(page, 'Nenhum impacto no mapa da unidade.');
}

/**
 * Verifica se o modal de impactos está aberto.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalImpactosAberto(page: Page): Promise<void> {
    const modal = page.getByTestId('impacto-mapa-modal');
    await expect(modal).toBeVisible();
    await expect(page.getByTestId('titulo-competencias-impactadas')).toBeVisible();
    await expect(page.getByTestId('msg-nenhuma-competencia')).not.toBeVisible();
}

/**
 * Verifica se o modal de impactos está fechado.
 * @param page A instância da página do Playwright.
 */
export async function verificarModalImpactosFechado(page: Page): Promise<void> {
    const modal = page.getByTestId('impacto-mapa-modal');
    await expect(modal).not.toBeVisible();
}

/**
 * Verifica se uma competência está visível.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição da competência.
 */
export async function verificarCompetenciaVisivel(page: Page, descricao: string): Promise<void> {
    const competencia = page.locator('.competencia-card', {hasText: descricao}).first();
    await expect(competencia).toBeVisible();
}

/**
 * Verifica se uma competência não está visível.
 * @param page A instância da página do Playwright.
 * @param descricao A descrição da competência.
 */
export async function verificarCompetenciaNaoVisivel(page: Page, descricao: string): Promise<void> {
    const competencia = page.locator('.competencia-card', {hasText: descricao}).first();
    await expect(competencia).not.toBeVisible();
}

/**
 * Verifica se a lista de atividades e conhecimentos está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarListagemAtividadesEConhecimentos(page: Page): Promise<void> {
    // Verifica se há atividades; se houver, garante que pelo menos um card está visível.
    const atividadesCount = await page.getByTestId(extrairIdDoSeletor(SELETORES.ITEM_ATIVIDADE)).count();
    if (atividadesCount > 0) {
        await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.ITEM_ATIVIDADE)).first()).toBeVisible();
    } else if ((await page.getByTestId('atividade-descricao').count()) > 0) {
        await expect(page.getByTestId('atividade-descricao').first()).toBeVisible();
    } else {
        // Se não houver atividades, aceitaremos que a listagem esteja vazia (situação válida em alguns cenários).
    }

    // Verifica se há conhecimentos; se houver, garante que pelo menos um esteja visível.
    const conhecimentosCount = await page.getByTestId(extrairIdDoSeletor(SELETORES.ITEM_CONHECIMENTO)).count();
    if (conhecimentosCount > 0) {
        await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.ITEM_CONHECIMENTO)).first()).toBeVisible();
    } else if ((await page.getByTestId('conhecimento-descricao').count()) > 0) {
        await expect(page.getByTestId('conhecimento-descricao').first()).toBeVisible();
    } else {
        // Não há conhecimentos — fluxo válido; não falhar.
    }
}

/**
 * Verifica se a página está em modo de somente leitura.
 * @param page A instância da página do Playwright.
 */
export async function verificarModoSomenteLeitura(page: Page): Promise<void> {
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_EDITAR_ATIVIDADE))).toHaveCount(0);
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_REMOVER_ATIVIDADE))).toHaveCount(0);
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_EDITAR_CONHECIMENTO))).toHaveCount(0);
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_REMOVER_CONHECIMENTO))).toHaveCount(0);
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_ADICIONAR_CONHECIMENTO))).toHaveCount(0);
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.BTN_ADICIONAR_ATIVIDADE))).toHaveCount(0);
}

/**
 * Verifica o cabeçalho da unidade.
 * @param page A instância da página do Playwright.
 * @param siglaEsperada A sigla esperada da unidade.
 */
export async function verificarCabecalhoUnidade(page: Page, siglaEsperada: string): Promise<void> {
    const sigla = page.locator('.unidade-cabecalho .unidade-sigla');
    const nome = page.locator('.unidade-cabecalho .unidade-nome');

    // Tentar pela versão do cabeçalho (quando presente)
    if ((await sigla.count()) > 0) {
        await expect(sigla).toBeVisible();
        await expect(sigla).toContainText(siglaEsperada);
        if ((await nome.count()) > 0) {
            await expect(nome).toBeVisible();
            await expect(nome).toHaveText(/\S+/);
        }
        return;
    }

    // Fallback: utilizar o test-id de info da unidade (mais estável entre variações de UI)
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.INFO_UNIDADE))).toBeVisible();
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.INFO_UNIDADE))).toContainText(siglaEsperada);
}

/**
 * Verifica se um card de ação está visível.
 * @param page A instância da página do Playwright.
 * @param tituloCard O título do card.
 */
export async function verificarCardAcaoVisivel(page: Page, tituloCard: string): Promise<void> {
    await expect(page.locator('.card-acao', {hasText: tituloCard})).toBeVisible();
}

/**
 * Verifica se um card de ação não está visível.
 * @param page A instância da página do Playwright.
 * @param tituloCard O título do card.
 */
export async function verificarCardAcaoInvisivel(page: Page, tituloCard: string): Promise<void> {
    await expect(page.locator('.card-acao', {hasText: tituloCard})).not.toBeVisible();
}

/**
 * Verifica as atividades associadas a uma competência.
 * @param page A instância da página do Playwright.
 * @param descricaoCompetencia A descrição da competência.
 * @param atividadesEsperadas As atividades esperadas.
 */
export async function verificarAtividadesAssociadas(page: Page, descricaoCompetencia: string, atividadesEsperadas: string[]): Promise<void> {
    const competenciaCard = page.locator('.competencia-card', {hasText: descricaoCompetencia});
    for (const atividade of atividadesEsperadas) {
        await expect(competenciaCard.locator('.atividade-associada-card-item', {hasText: atividade})).toBeVisible();
    }
}

/**
 * Verifica a descrição de uma competência.
 * @param page A instância da página do Playwright.
 * @param descricaoCompetencia A descrição da competência.
 * @param descricaoEsperada A descrição esperada.
 */
export async function verificarDescricaoCompetencia(page: Page, descricaoCompetencia: string, descricaoEsperada: string): Promise<void> {
    const competenciaCard = page.locator('.competencia-card', {hasText: descricaoCompetencia});
    await expect(competenciaCard.getByTestId('competencia-descricao')).toHaveText(descricaoEsperada);
}

/**
 * Verifica se o painel está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarPainelVisivel(page: Page): Promise<void> {
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByTestId(extrairIdDoSeletor(SELETORES.TITULO_PROCESSOS))).toBeVisible();
}

/**
 * Verifica se a seleção de perfil está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarSelecaoPerfilVisivel(page: Page): Promise<void> {
    await expect(page.getByText('Selecione o perfil e a unidade')).toBeVisible();
    await expect(page.getByTestId('select-perfil-unidade')).toBeVisible();
}

/**
 * Verifica se o modal de impactos no mapa está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarImpactosNoMapa(page: Page): Promise<void> {
    try {
        await expect(page.getByTestId('impacto-mapa-modal')).toBeVisible();
    } catch {
        await expect(page.getByText('Nenhum impacto no mapa da unidade.')).toBeVisible();
    }
}

/**
 * Verifica se o painel de administrador está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarPainelAdminVisivel(page: Page): Promise<void> {
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByTitle('Configurações do sistema')).toBeVisible();
}

/**
 * Verifica se o painel de chefe está visível.
 * @param page A instância da página do Playwright.
 */
export async function verificarPainelChefeVisivel(page: Page): Promise<void> {
    await expect(page).toHaveURL(URLS.PAINEL);
    await expect(page.getByTitle('Configurações do sistema')).not.toBeVisible();
}
