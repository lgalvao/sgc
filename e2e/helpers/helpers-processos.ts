import {expect, type Page} from '@playwright/test';

const ROTULOS_TIPO_PROCESSO = {
    MAPEAMENTO: 'Mapeamento',
    REVISAO: 'Revisão',
    DIAGNOSTICO: 'Diagnóstico'
} as const;

type TipoProcesso = keyof typeof ROTULOS_TIPO_PROCESSO;

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

    await expect(page.getByText('Carregando unidades...')).toBeHidden();
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
        await page.getByTestId('btn-processo-salvar').click();
        await expect(page).toHaveURL(/\/painel(?:\?|$)/);
    }
}

/**
 * Verifica que um processo aparece na tabela com situação e tipo corretos
 */
export async function verificarProcessoNaTabela(page: Page, options: {
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
    await expect(linhaProcesso.getByText(options.situacao)).toBeVisible();
    await expect(linhaProcesso.getByText(options.tipo, {exact: true})).toBeVisible();

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
    await expect(page).toHaveURL(/\/painel(?:\?|$)/);
    await verificarProcessoNaTabela(page, {
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
}): Promise<void> {
    await page.getByTestId('btn-processo-iniciar').click();
    await confirmarInicioProcessoPeloDialogo(page, options);
}

export async function confirmarInicioProcessoPeloDialogo(page: Page, options: {
    descricao: string;
    tipo: TipoProcesso;
    unidadesParticipantes?: string[];
}): Promise<void> {
    const dialog = page.getByRole('dialog');
    await expect(dialog).toBeVisible();
    await dialog.getByTestId('btn-iniciar-processo-confirmar').click();
    await aguardarProcessoNoPainel(page, {
        descricao: options.descricao,
        situacao: 'Em andamento',
        tipo: options.tipo,
        unidadesParticipantes: options.unidadesParticipantes
    });
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
    await expect(page.getByText('Carregando detalhes do processo...').first()).toBeHidden();

    // Verificar descrição usando o test-id existente
    await expect(page.getByTestId('processo-info')).toHaveText(dados.descricao);

    // Verificar tipo e situação usando getByText
    await expect(page.getByText(`Tipo: ${dados.tipo}`)).toBeVisible();
    await expect(page.getByText(`Situação: ${dados.situacao}`)).toBeVisible();
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
    situacao: string,
    prazo?: string | RegExp,
    localizacao?: string,
    titular?: string,
    responsavel?: string,
    tipoResponsabilidade?: string
}) {
    await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toContainText(dados.sigla);
    if (dados.titular) {
        await expect(page.getByText(`Titular: ${dados.titular}`).first()).toBeVisible();
    }
    if (dados.responsavel) {
        await expect(page.getByText(`Responsável: ${dados.responsavel}`).first()).toBeVisible();
    }
    if (dados.tipoResponsabilidade) {
        await expect(page.getByText(`- ${dados.tipoResponsabilidade}`).first()).toBeVisible();
    }
    if (dados.localizacao) {
        await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toContainText(dados.localizacao);
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
        const match = new RegExp(pattern).exec(url);
        if (match?.[1]) {
            return Number.parseInt(match[1]);
        }
    }

    throw new Error(
        `Não foi possível extrair ID do processo da URL: ${url}`
    );
}
