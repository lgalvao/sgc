import {expect, type Locator, type Page} from '@playwright/test';

export interface CriteriosNotificacaoAdmin {
    assunto: string | RegExp;
    destinatario?: string | RegExp;
    tipo?: string | RegExp;
    situacao?: string | RegExp;
    trechoCorpo?: string | RegExp;
}

interface NotificacaoAdminApi {
    codigo: number;
    unidadeSigla?: string | null;
    usuarioDestinoTitulo?: string | null;
    destinatario: string;
    tipoNotificacao: string;
    assunto: string;
    corpoHtml?: string | null;
    situacao: string;
}

const TIPOS_NOTIFICACAO_LABELS: Record<string, string> = {
    PROCESSO_INICIADO: 'Início do processo',
    PROCESSO_FINALIZADO: 'Finalização de processo',
    DATA_LIMITE_ALTERADA: 'Alteração da data limite',
    LEMBRETE_PRAZO: 'Lembrete de prazo',
    ATRIBUICAO_TEMPORARIA: 'Atribuição temporária',
    CADASTRO_DISPONIBILIZADO: 'Cadastro disponibilizado',
    CADASTRO_DEVOLVIDO: 'Cadastro devolvido para ajustes',
    CADASTRO_ACEITO: 'Cadastro aceito',
    CADASTRO_HOMOLOGADO: 'Cadastro homologado',
    CADASTRO_REABERTO: 'Cadastro reaberto',
    REVISAO_CADASTRO_DISPONIBILIZADA: 'Revisão de cadastro disponibilizada',
    REVISAO_CADASTRO_DEVOLVIDA: 'Revisão de cadastro devolvida',
    REVISAO_CADASTRO_ACEITA: 'Revisão de cadastro aceita',
    REVISAO_CADASTRO_HOMOLOGADA: 'Revisão de cadastro homologada',
    REVISAO_CADASTRO_REABERTA: 'Revisão de cadastro reaberta',
    MAPA_DISPONIBILIZADO: 'Mapa disponibilizado',
    MAPA_SUGESTOES_APRESENTADAS: 'Sugestões apresentadas para o mapa',
    MAPA_VALIDADO: 'Mapa validado',
    MAPA_VALIDACAO_DEVOLVIDA: 'Validação do mapa devolvida',
    MAPA_VALIDACAO_ACEITA: 'Validação do mapa aceita',
    MAPA_HOMOLOGADO: 'Mapa homologado',
    DIAGNOSTICO_AUTOAVALIACAO_CONCLUIDA: 'Autoavaliação concluída',
    DIAGNOSTICO_CONSENSO_DISPONIVEL: 'Consenso disponível',
    DIAGNOSTICO_CONSENSO_APROVADO: 'Consenso aprovado',
    DIAGNOSTICO_CONCLUIDO: 'Diagnóstico concluído',
    DIAGNOSTICO_DEVOLVIDO: 'Diagnóstico devolvido',
    DIAGNOSTICO_ACEITO: 'Diagnóstico aceito',
    DIAGNOSTICO_HOMOLOGADO: 'Diagnóstico homologado'
};

function normalizarAssuntoVisivel(assunto: string): string {
    return assunto.replace(/^SGC:\s*/i, '').trim();
}

function correspondeTexto(valor: string | undefined | null, criterio?: string | RegExp): boolean {
    if (!criterio) return true;
    const texto = valor?.trim() || '';
    return typeof criterio === 'string' ? texto.includes(criterio) : criterio.test(texto);
}

function obterPossiveisDestinatarios(item: NotificacaoAdminApi): string[] {
    const valores = new Set<string>();
    if (item.unidadeSigla?.trim()) valores.add(item.unidadeSigla.trim().toUpperCase());
    if (item.usuarioDestinoTitulo?.trim()) valores.add(item.usuarioDestinoTitulo.trim());
    if (item.destinatario?.trim()) valores.add(item.destinatario.trim());
    const local = item.destinatario?.trim().match(/^([^@]+)@tre-pe\.jus\.br$/i)?.[1];
    if (local) valores.add(local.toUpperCase());
    return [...valores];
}

function correspondeDestinatario(item: NotificacaoAdminApi, criterio?: string | RegExp): boolean {
    if (!criterio) return true;
    return obterPossiveisDestinatarios(item).some(valor => correspondeTexto(valor, criterio));
}

function correspondeTipo(item: NotificacaoAdminApi, criterio?: string | RegExp): boolean {
    if (!criterio) return true;
    const label = TIPOS_NOTIFICACAO_LABELS[item.tipoNotificacao];
    return correspondeTexto(item.tipoNotificacao, criterio) || correspondeTexto(label, criterio);
}

export async function abrirNotificacoesAdmin(page: Page): Promise<Locator> {
    const linkNotificacoes = page.getByTestId('nav-link-notificacoes');
    const urlListagem = /\/api\/admin\/notificacoes\/listar(?:\?|$)/;

    if (!(await linkNotificacoes.isVisible())) {
        const botaoNavbar = page.locator('.navbar-toggler:visible').first();
        await expect(botaoNavbar).toBeVisible();
        await botaoNavbar.click();
        await expect(linkNotificacoes).toBeVisible();
    }

    await linkNotificacoes.click();
    await expect(page).toHaveURL(/\/administracao\/notificacoes/);

    const tabela = page.getByTestId('tbl-notificacoes');
    await expect(tabela).toBeVisible();
    await Promise.all([
        page.waitForResponse(response => urlListagem.test(response.url()) && response.ok()),
        page.getByTestId('btn-notificacoes-atualizar').click()
    ]);
    await expect(tabela).toBeVisible();
    return tabela;
}

export async function verificarNotificacaoAdmin(page: Page, criterios: CriteriosNotificacaoAdmin): Promise<void> {
    await abrirNotificacoesAdmin(page);

    async function listarNotificacoesApi(): Promise<NotificacaoAdminApi[]> {
        return await page.evaluate(async () => {
            const resposta = await fetch('/api/admin/notificacoes/listar?limite=50', {credentials: 'include'});
            if (!resposta.ok) {
                throw new Error(`HTTP ${resposta.status}`);
            }
            return await resposta.json();
        });
    }

    let itemEncontrado: NotificacaoAdminApi | undefined;
    try {
        const botaoAtualizar = page.getByTestId('btn-notificacoes-atualizar');
        const urlListagem = /\/api\/admin\/notificacoes\/listar(?:\?|$)/;
        await expect.poll(async () => {
            await Promise.all([
                page.waitForResponse(response => urlListagem.test(response.url()) && response.ok()),
                botaoAtualizar.click()
            ]);
            itemEncontrado = localizarNotificacaoAdmin(await listarNotificacoesApi(), criterios);
            return itemEncontrado ? 1 : 0;
        }, {
            message: `Notificação não encontrada para assunto ${String(criterios.assunto)}`,
            timeout: 15000,
            intervals: [500, 1000, 1500, 2000]
        }).toBeGreaterThan(0);
    } catch {
        const notificacoes = await listarNotificacoesApi();
        const resumo = notificacoes
            .slice(0, 10)
            .map(item => {
                const destino = item.usuarioDestinoTitulo || item.destinatario || item.unidadeSigla || 'sem-destino';
                return `${item.tipoNotificacao} | ${destino} | ${item.assunto}`;
            })
            .join(' || ');

        throw new Error(
            `Notificação não encontrada para assunto ${String(criterios.assunto)}. ` +
            `Amostra da API admin: ${resumo || 'sem registros'}`
        );
    }

    const item = itemEncontrado!;
    const botaoDetalhes = page.getByTestId(`btn-detalhes-${item.codigo}`);
    await expect(botaoDetalhes).toBeVisible();

    if (!criterios.trechoCorpo) return;

    const corpoHtml = item.corpoHtml?.trim();
    expect(corpoHtml, `Notificação ${item.codigo} sem corpoHtml retornado pela API admin`).toBeTruthy();
    await expect(corpoHtml!).toMatch(criterios.trechoCorpo instanceof RegExp
        ? criterios.trechoCorpo
        : new RegExp(criterios.trechoCorpo.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'i'));
}

export async function verificarAusenciaNotificacaoAdmin(page: Page, criterios: CriteriosNotificacaoAdmin): Promise<void> {
    await abrirNotificacoesAdmin(page);
    const notificacoes = await page.evaluate(async () => {
        const resposta = await fetch('/api/admin/notificacoes/listar?limite=50', {credentials: 'include'});
        if (!resposta.ok) {
            throw new Error(`HTTP ${resposta.status}`);
        }
        return await resposta.json();
    }) as NotificacaoAdminApi[];

    const item = localizarNotificacaoAdmin(notificacoes, criterios);
    expect(item, `Notificação inesperada encontrada para assunto ${String(criterios.assunto)}`).toBeUndefined();
}

function localizarNotificacaoAdmin(notificacoes: NotificacaoAdminApi[], criterios: CriteriosNotificacaoAdmin): NotificacaoAdminApi | undefined {
    return notificacoes.find(item =>
        correspondeTexto(normalizarAssuntoVisivel(item.assunto), criterios.assunto)
        && correspondeDestinatario(item, criterios.destinatario)
        && correspondeTipo(item, criterios.tipo)
        && correspondeTexto(item.situacao, criterios.situacao)
    );
}
