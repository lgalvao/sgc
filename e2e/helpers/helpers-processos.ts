import {expect, type Locator, type Page} from '@playwright/test';
import {TEXTOS} from '../../frontend/src/constants/textos.js';

const ROTULOS_TIPO_PROCESSO = {
    MAPEAMENTO: 'Mapeamento',
    REVISAO: 'Revisão',
    DIAGNOSTICO: 'Diagnóstico'
} as const;

type TipoProcesso = keyof typeof ROTULOS_TIPO_PROCESSO;

async function buscarUnidadeNaArvore(page: Page, siglaUnidade: string): Promise<void> {
    const busca = page.getByRole('searchbox', {name: 'Buscar unidade por sigla'});
    await expect(busca).toBeVisible();
    await busca.fill(siglaUnidade);
}

/**
 * Calcula uma data limite N dias no futuro
 */
export function calcularDataLimite(dias: number): string {
    const dataLimite = new Date();
    dataLimite.setDate(dataLimite.getDate() + dias);
    const ano = dataLimite.getFullYear();
    const mes = String(dataLimite.getMonth() + 1).padStart(2, '0');
    const dia = String(dataLimite.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
}

/**
 * Cria um processo através da UI
 */
export async function criarProcesso(page: Page, options: {
    descricao: string;
    tipo: TipoProcesso;
    diasLimite?: number;
    unidade: string | string[];
    expandir?: string[];
    iniciar?: boolean;
}): Promise<void> {
    const dias = options.diasLimite ?? 30;
    await page.getByTestId('btn-painel-criar-processo').click();
    await expect(page).toHaveURL(/\/processo\/cadastro/);

    await page.getByTestId('inp-processo-descricao').fill(options.descricao);
    await page.getByTestId('sel-processo-tipo').selectOption(options.tipo);
    await page.getByTestId('inp-processo-data-limite').fill(calcularDataLimite(dias));

    await expect(page.getByText(TEXTOS.unidade.CARREGANDO)).toBeHidden();
    if (options.expandir) {
        for (const sigla of options.expandir) {
            await page.getByTestId(`btn-arvore-expand-${sigla}`).click();
        }
    }

    const unidades = Array.isArray(options.unidade) ? options.unidade : [options.unidade];
    for (const u of unidades) {
        const checkbox = page.getByTestId(`chk-arvore-unidade-${u}`);
        await expect(checkbox).toBeVisible();
        await expect(checkbox).toBeEnabled();
        // Só marca se não estiver marcado (pode já estar marcado se o pai foi selecionado)
        if (!await checkbox.isChecked()) {
            await checkbox.check();
        }
    }

    if (options.iniciar) {
        await iniciarProcessoPeloCadastro(page, {
            descricao: options.descricao,
            tipo: options.tipo
        });
    } else {
        const botaoSalvar = page.getByTestId('btn-processo-salvar-rodape');
        await botaoSalvar.scrollIntoViewIfNeeded();
        await expect(botaoSalvar).toBeInViewport();
        await botaoSalvar.click();
        await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
    }
}

/**
 * Cria um processo pela UI em formato semântico:
 * - recebe sempre um array em `unidades`
 * - expande a árvore automaticamente até encontrar as unidades
 * - apenas cria o processo, sem iniciá-lo
 */
export async function criarProcessoSimples(page: Page, options: {
    descricao: string;
    tipo: TipoProcesso;
    diasLimite?: number;
    unidades: string[];
}): Promise<void> {
    const dias = options.diasLimite ?? 30;
    await page.getByTestId('btn-painel-criar-processo').click();
    await expect(page).toHaveURL(/\/processo\/cadastro/);

    await page.getByTestId('inp-processo-descricao').fill(options.descricao);
    await page.getByTestId('sel-processo-tipo').selectOption(options.tipo);
    await page.getByTestId('inp-processo-data-limite').fill(calcularDataLimite(dias));

    await expect(page.getByText(TEXTOS.unidade.CARREGANDO)).toBeHidden();

    for (const siglaUnidade of options.unidades) {
        await buscarUnidadeNaArvore(page, siglaUnidade);

        const checkbox = page.getByTestId(`chk-arvore-unidade-${siglaUnidade}`);
        await expect(checkbox).toBeVisible();
        await expect(checkbox).toBeEnabled();

        if (!await checkbox.isChecked()) {
            await checkbox.check();
        }
    }

    const botaoSalvar = page.getByTestId('btn-processo-salvar-rodape');
    await botaoSalvar.scrollIntoViewIfNeeded();
    await expect(botaoSalvar).toBeInViewport();
    await botaoSalvar.click();
    await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
}

/**
 * Verifica os cabeçalhos obrigatórios da tabela de processos
 */

export async function verificarCabecalhosTabelaProcessos(page: Page, compacto = false): Promise<void> {
    const tabela = page.getByTestId('tbl-processos');
    await expect(tabela).toBeVisible();

    const rotuloUnidades = compacto ? 'Unidades' : 'Unidades participantes';
    const cabecalhosEsperados = ['Descrição', 'Tipo', rotuloUnidades, 'Situação'];

    for (const cabecalho of cabecalhosEsperados) {
        await expect(tabela.locator('th', {hasText: cabecalho}).first()).toBeVisible();
    }
}

/**
 * Verifica que um processo aparece na tabela com situação e tipo corretos
 */
export async function verificarProcessoTabela(page: Page, options: {
    descricao: string;
    situacao: string;
    tipo: string;
    unidadesParticipantes?: string[];
}): Promise<void> {
    const tabela = page.locator('[data-testid="tbl-processos"]');
    await expect(tabela).toBeVisible();

    // Localizar a linha que contém a descrição do processo
    const linhaProcesso = tabela.locator('tr').filter({hasText: options.descricao}).first();
    await expect(linhaProcesso).toBeVisible();
    await expect(linhaProcesso.locator('[data-testid="badge-situacao"]')).toHaveText(new RegExp(options.situacao, 'i'));
    // O tipo não tem data-testid isolado, mas podemos verificar na linha
    await expect(linhaProcesso.locator('td').filter({hasText: new RegExp(`^${options.tipo}$`, 'i')})).toBeVisible();

    if (options.unidadesParticipantes) {
        for (const unidade of options.unidadesParticipantes) {
            await expect(linhaProcesso.getByText(unidade)).toBeVisible();
        }
    }
}

export async function aguardarProcessoNoPainel(page: Page, options: {
    descricao: string;
    situacao: string;
    tipo: TipoProcesso;
    unidadesParticipantes?: string[];
}): Promise<void> {
    await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
    await expect(page.getByTestId('painel-carregando')).toBeHidden();
    await verificarProcessoTabela(page, {
        descricao: options.descricao,
        situacao: options.situacao,
        tipo: ROTULOS_TIPO_PROCESSO[options.tipo],
        unidadesParticipantes: options.unidadesParticipantes
    });
}

export async function iniciarProcessoPeloCadastro(page: Page, options: {
    descricao: string;
    tipo: TipoProcesso;
    unidadesParticipantes?: string[];
    unidadesComEquipePropriaParticipantes?: string[];
}): Promise<void> {
    await page.getByTestId('btn-processo-iniciar-rodape').click();
    await confirmarInicioProcessoPeloDialogo(page, options);
}

/**
 * Inicia o processo já aberto na tela de cadastro/detalhes.
 * Versão semântica: não exige informar o tipo novamente.
 */
export async function iniciarProcesso(page: Page, descricao: string, options?: {
    unidadesComEquipePropriaParticipantes?: string[];
}): Promise<void> {
    await page.getByTestId('btn-processo-iniciar-rodape').click();

    const dialog = page.getByRole('dialog');
    await expect(dialog).toBeVisible();
    await dialog.getByTestId('btn-iniciar-processo-confirmar').click();
    await confirmarSelecaoComplementarUnidadesComEquipePropria(page, options?.unidadesComEquipePropriaParticipantes);

    await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
    await verificarProcessoTabela(page, {
        descricao,
        situacao: 'Em andamento',
        tipo: ROTULOS_TIPO_PROCESSO.MAPEAMENTO
    });
}

export async function confirmarInicioProcessoPeloDialogo(page: Page, options: {
    descricao: string;
    tipo: TipoProcesso;
    unidadesParticipantes?: string[];
    unidadesComEquipePropriaParticipantes?: string[];
}): Promise<void> {
    const dialog = page.getByRole('dialog');
    await expect(dialog).toBeVisible();
    await dialog.getByTestId('btn-iniciar-processo-confirmar').click();
    await confirmarSelecaoComplementarUnidadesComEquipePropria(page, options.unidadesComEquipePropriaParticipantes);
    await aguardarProcessoNoPainel(page, {
        descricao: options.descricao,
        situacao: 'Em andamento',
        tipo: options.tipo,
        unidadesParticipantes: options.unidadesParticipantes
    });
}

async function confirmarSelecaoComplementarUnidadesComEquipePropria(
    page: Page,
    siglasConfirmadas?: string[]
): Promise<void> {
    const modal = page.locator('#modal-unidades-com-equipe-propria');
    const abriuModal = await modal.waitFor({state: 'visible', timeout: 2000})
        .then(() => true)
        .catch(() => false);
    if (!abriuModal) {
        return;
    }

    if (siglasConfirmadas) {
        const siglasDesejadas = new Set(siglasConfirmadas);
        const linhas = modal.locator('tbody tr');
        const totalLinhas = await linhas.count();

        for (let i = 0; i < totalLinhas; i++) {
            const linha = linhas.nth(i);
            const sigla = (await linha.locator('td').nth(1).innerText()).trim();
            const checkbox = linha.locator('input[type="checkbox"]');
            const deveFicarMarcado = siglasDesejadas.has(sigla);

            if (deveFicarMarcado) {
                await checkbox.check();
            } else {
                await checkbox.uncheck();
            }
        }
    }

    await modal.getByTestId('btn-acao-bloco-confirmar').click();
}

export interface UnidadeParticipante {
    sigla: string;
    situacao: string;
    dataLimite: string | RegExp;
}

export async function verificarDetalhesProcesso(page: Page, dados: {
    descricao: string,
    tipo: string,
    situacao: 'Criado' | 'Em andamento' | 'Finalizado'
}) {
    // Aguardar carregamento dos detalhes
    await expect(page.getByText(TEXTOS.processo.CARREGANDO_DETALHES).first()).toBeHidden();

    // Verificar descrição usando o test-id existente
    await expect(page.getByTestId('processo-info')).toHaveText(dados.descricao);

    // Verificar tipo e situação usando getByText
    await expect(page.getByText(`${TEXTOS.processo.INFO_TIPO}: ${dados.tipo}`)).toBeVisible();
    await expect(page.getByText(`${TEXTOS.processo.INFO_SITUACAO}: ${dados.situacao}`)).toBeVisible();
}

export async function verificarUnidadeParticipante(page: Page, unidade: UnidadeParticipante) {
    const row = page.locator('tr').filter({hasText: new RegExp(String.raw`^\s*${unidade.sigla}\b`, 'i')}).first();
    await expect(row).toBeVisible();
    await expect(row).toContainText(unidade.situacao);

    if (unidade.dataLimite instanceof RegExp) {
        await expect(row).toHaveText(unidade.dataLimite);
    } else {
        await expect(row).toContainText(unidade.dataLimite);
    }
}

export async function verificarDetalhesSubprocesso(page: Page, dados: {
    sigla: string,
    nomeUnidade?: string,
    situacao: string,
    prazo?: string | RegExp,
    localizacao?: string,
    titular?: string,
    ramalTitular?: string,
    emailTitular?: string,
    responsavel?: string,
    tipoResponsabilidade?: string,
    ramalResponsavel?: string,
    emailResponsavel?: string
}) {
    const header = page.getByTestId('header-subprocesso');
    await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toContainText(dados.sigla);
    if (dados.nomeUnidade) {
        await expect(header).toContainText(dados.nomeUnidade);
    }
    if (dados.titular) {
        await expect(page.getByText(`Titular: ${dados.titular}`).first()).toBeVisible();
    }
    if (dados.ramalTitular) {
        await expect(header.getByText(dados.ramalTitular)).toBeVisible();
    }
    if (dados.emailTitular) {
        await expect(header.getByRole('link', {name: dados.emailTitular})).toBeVisible();
    }
    if (dados.responsavel) {
        await expect(page.getByText(`Responsável: ${dados.responsavel}`).first()).toBeVisible();
    }
    if (dados.tipoResponsabilidade) {
        await expect(page.getByText(`- ${dados.tipoResponsabilidade}`).first()).toBeVisible();
    }
    if (dados.ramalResponsavel) {
        await expect(header.getByText(dados.ramalResponsavel)).toBeVisible();
    }
    if (dados.emailResponsavel) {
        await expect(header.getByRole('link', {name: dados.emailResponsavel})).toBeVisible();
    }
    if (dados.localizacao) {
        await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toContainText(dados.localizacao);
    }
    if (dados.prazo) {
        const campoPrazo = page.locator('span:has-text("Prazo para conclusão da etapa atual:")').first();
        await expect(campoPrazo).toContainText(dados.prazo);
    }
    await expect(page.getByTestId('subprocesso-header__txt-situacao')).toContainText(dados.situacao);
}

/**
 * Extrai o código do processo da URL atual.
 * Suporta múltiplos formatos de URL do sistema:
 * - /processo/cadastro/{codigo}
 * - codProcesso={codigo}
 * - /processo/{codigo}
 *
 * @throws {Error} Se não for possível extrair o código da URL atual
 */
export async function extrairProcessoCodigo(page: Page): Promise<number> {
    const url = page.url();

    const patterns = [
        /\/processo\/cadastro\/(\d+)/,
        /codProcesso=(\d+)/,
        /\/processo\/(\d+)/
    ];

    for (const pattern of patterns) {
        const match = new RegExp(pattern).exec(url);
        if (match?.[1]) {
            return Number.parseInt(match[1]);
        }
    }

    throw new Error(
        `Não foi possível extrair código do processo da URL: ${url}`
    );
}

export async function obterAcaoBloco(page: Page, testId: string): Promise<Locator> {
    const acao = page.getByTestId(testId);
    if (await acao.isVisible().catch(() => false)) {
        return acao;
    }

    const botaoMenu = page.getByRole('button', {name: new RegExp(`^${TEXTOS.processo.ACOES_EM_BLOCO}$`, 'i')}).first();
    if (await botaoMenu.isVisible().catch(() => false)) {
        await botaoMenu.click();
        await expect(acao).toBeVisible();
        return acao;
    }

    const alternativas = [
        'btn-processo-aceitar-bloco',
        'btn-processo-homologar-bloco',
        'btn-processo-disponibilizar-bloco',
        'btn-processo-aceitar-mapas-bloco',
        'btn-processo-homologar-mapas-bloco',
        'btn-processo-aceitar-diagnosticos-bloco',
        'btn-processo-homologar-diagnosticos-bloco',
    ];

    for (const alternativa of alternativas) {
        const botaoAlternativo = page.getByTestId(alternativa);
        if (await botaoAlternativo.isVisible().catch(() => false)) {
            return botaoAlternativo;
        }
    }

    await expect(acao).toBeVisible();
    return acao;
}

/**
 * Acessa a tela de detalhes de um processo a partir do painel.
 */
export async function acessarDetalhesProcesso(page: Page, descricao: string) {
    const linhaProcesso = page.getByTestId('tbl-processos').locator('tr', {hasText: descricao});
    await expect(linhaProcesso).toBeVisible();
    await linhaProcesso.focus();
    await linhaProcesso.press('Enter');
    await expect(page).toHaveURL(/\/processo\/(?:cadastro\?codProcesso=)?\d+(?:\/[A-Z0-9_]+)?(?:\?.*)?$/);
}


/**
 * Finaliza o processo atual a partir da tela de detalhes do processo.
 */
export async function finalizarProcesso(page: Page) {
    const botaoFinalizar = page.getByTestId('btn-processo-finalizar');
    await expect(botaoFinalizar).toBeEnabled();
    await botaoFinalizar.click();

    // 1. Instanciar promessas para os requests
    const postFinalizarPromise = page.waitForResponse(r =>
        r.url().match(/\/api\/processos\/\d+\/finalizar/) !== null &&
        r.request().method() === 'POST' &&
        r.ok()
    );

    const bootstrapPromise = page.waitForResponse(
        r => r.url().includes('/api/painel/bootstrap') && r.ok()
    );

    // 2. Clicar no botão e aguardar o POST concluir
    await page.getByTestId('btn-finalizar-processo-confirmar').click();
    await postFinalizarPromise;

    // 3. Aguardar a navegação e a recarga dos dados do painel
    await page.waitForURL(/\/painel(?:\?.*)?$/);
    await bootstrapPromise;
    await expect(page.getByTestId('painel-carregando')).toBeHidden();
}
