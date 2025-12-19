import {expect, type Page} from '@playwright/test';

/**
 * Calcula uma data limite N dias no futuro
 */
export function calcularDataLimite(dias: number): string {
    const dataLimite = new Date();
    dataLimite.setDate(dataLimite.getDate() + dias);
    return dataLimite.toISOString().split('T')[0];
}

/**
 * Cria um processo através da UI
 */
export async function criarProcesso(page: Page, options: {
    descricao: string;
    tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO';
    diasLimite: number;
    unidade: string | string[];
    expandir?: string[];
    iniciar?: boolean;
}): Promise<void> {
    await page.getByTestId('btn-painel-criar-processo').click();
    await expect(page).toHaveURL(/\/processo\/cadastro/);

    await page.getByTestId('inp-processo-descricao').fill(options.descricao);
    await page.getByTestId('sel-processo-tipo').selectOption(options.tipo);
    await page.getByTestId('inp-processo-data-limite').fill(calcularDataLimite(options.diasLimite));

    await expect(page.getByText('Carregando unidades...')).toBeHidden();
    if (options.expandir) {
        for (const sigla of options.expandir) {
            await page.getByTestId(`btn-arvore-expand-${sigla}`).click();
        }
    }

    const unidades = Array.isArray(options.unidade) ? options.unidade : [options.unidade];
    for (const u of unidades) {
        await page.getByTestId(`chk-arvore-unidade-${u}`).check();
    }

    if (options.iniciar) {
        await page.getByTestId('btn-processo-iniciar').click();
        await page.getByTestId('btn-iniciar-processo-confirmar').click();
    } else {
        await page.getByTestId('btn-processo-salvar').click();
    }
    await page.waitForURL('/painel');
}

/**
 * Verifica que um processo aparece na tabela com situação e tipo corretos
 */
export async function verificarProcessoNaTabela(page: Page, options: {
    descricao: string;
    situacao: string;
    tipo: string;
}): Promise<void> {
    await expect(page.getByTestId('tbl-processos')).toBeVisible();
    await expect(page.getByText(options.descricao)).toBeVisible();

    const linhaProcesso = page.locator('tr', {has: page.getByText(options.descricao)});
    await expect(linhaProcesso.getByText(options.situacao)).toBeVisible();
    await expect(linhaProcesso.getByText(options.tipo, {exact: true})).toBeVisible();
}

export interface UnidadeParticipante {
    sigla: string;
    situacao: string;
    dataLimite: string | RegExp;
}

export async function verificarDetalhesProcesso(page: Page, dados: {
    descricao: string,
    tipo: string,
    situacao: string
}) {
    // Verificar descrição usando o test-id existente
    await expect(page.getByTestId('processo-info')).toHaveText(dados.descricao);

    // Verificar tipo e situação usando getByText
    await expect(page.getByText(`Tipo: ${dados.tipo}`)).toBeVisible();
    await expect(page.getByText(`Situação: ${dados.situacao}`)).toBeVisible();
}

export async function verificarUnidadeParticipante(page: Page, unidade: UnidadeParticipante) {
    const row = page.getByRole('row', {name: new RegExp(unidade.sigla, 'i')});
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
    situacao: string,
    prazo: string | RegExp,
    titular?: string
}) {
    await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toContainText(dados.sigla);
    if (dados.titular) {
        await expect(page.getByText(dados.titular).first()).toBeVisible();
    }
    await expect(page.getByText(dados.situacao).first()).toBeVisible();
}

/**
 * Extrai o ID do processo da URL atual.
 * Suporta múltiplos formatos de URL do sistema:
 * - /processo/cadastro/{id}
 * - codProcesso={id}
 * - /processo/{id}
 * 
 * @throws {Error} Se não for possível extrair o ID da URL atual
 */
export async function extrairProcessoId(page: Page): Promise<number> {
    const url = page.url();
    
    const patterns = [
        /\/processo\/cadastro\/(\d+)/,
        /codProcesso=(\d+)/,
        /\/processo\/(\d+)/
    ];
    
    for (const pattern of patterns) {
        const match = url.match(pattern);
        if (match?.[1]) {
            return parseInt(match[1]);
        }
    }
    
    throw new Error(
        `Não foi possível extrair ID do processo da URL: ${url}`
    );
}
