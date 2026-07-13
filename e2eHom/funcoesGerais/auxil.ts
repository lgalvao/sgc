import { Page, expect, test } from '@playwright/test';
import { createRequire } from 'module';
import { IDENTIFICADORES, ROTAS } from './constantes.js';

export { criarProcessoSimples, acessarDetalhesProcesso, finalizarProcesso, verificarProcessoTabela } from '../../e2e/helpers/helpers-processos.js';
export { navegarParaSubprocesso, limparNotificacoes } from '../../e2e/helpers/helpers-navegacao.js';
export { navegarParaCadastro, adicionarAtividade, adicionarConhecimento, disponibilizarCadastro } from '../../e2e/helpers/helpers-atividades.js';
export { acessarSubprocessoGestor, acessarSubprocessoChefeDireto, acessarSubprocessoAdmin, aceitarCadastroMapeamento, homologarCadastroMapeamento } from '../../e2e/helpers/helpers-analise.js';
export { verificarAusenciaNotificacaoAdmin } from '../../e2e/helpers/helpers-notificacoes-admin.js';
export { navegarParaMapa, criarCompetencia, disponibilizarMapa, aceitarOuHomologarMapa } from '../../e2e/helpers/helpers-mapas.js';
import { abrirValidacaoMapa } from '../../e2e/helpers/helpers-mapas.js';
import { verificarPaginaPainel } from '../../e2e/helpers/helpers-navegacao.js';


const require = createRequire(import.meta.url);
const usuarios = require('../usuarios.local.json');

export interface Usuario {
    titulo: string;
    senha: string;
    perfil?: string;
}

/**
 * Realiza a validação do mapa na tela do mapa do subprocesso e aguarda retorno ao painel.
 */
export async function validarMapa(page: Page): Promise<void> {
    await abrirValidacaoMapa(page);
    const btnConfirmar = page.getByTestId('btn-validar-mapa-confirmar');
    await expect(btnConfirmar).toBeVisible();
    await btnConfirmar.click();
    await verificarPaginaPainel(page);
}


/**
 * Retorna as credenciais de um usuário específico a partir do arquivo usuarios.local.json.
 *
 * @param nomeUsuario Nome chave do usuário no JSON (ex: 'ADMIN', 'GESTOR-SGP', 'ADMIN-CHEFE-SEDOC')
 */
export function obterCredenciaisUsuario(nomeUsuario: string): Usuario {
    const dados = usuarios.usuario[nomeUsuario];
    if (!dados) {
        throw new Error(`Usuário "${nomeUsuario}" não encontrado no arquivo usuarios.local.json`);
    }
    return {
        titulo: dados.titulo,
        senha: dados.senha || usuarios.senhaPadrao,
        perfil: dados.perfil
    };
}

/**
 * Filtra e retorna os usuários cujo perfil é exatamente o informado.
 *
 * @param perfil Perfil de acesso exato (ex: 'ADMIN', 'GESTOR - SGP', 'CHEFE - SEDOC')
 */
export function obterUsuariosPorPerfil(perfil: string) {
    return Object.keys(usuarios.usuario || {})
        .filter(nome => usuarios.usuario[nome].perfil === perfil)
        .map(nome => {
            const dados = usuarios.usuario[nome];
            return {
                nome,
                credenciais: {
                    titulo: dados.titulo,
                    senha: dados.senha || usuarios.senhaPadrao,
                    perfil: dados.perfil
                } as Usuario
            };
        });
}

/**
 * Filtra e retorna os usuários cujo nome começa com o prefixo informado.
 *
 * @param prefixo Prefixo para filtrar os usuários (ex: 'ADMIN', 'GESTOR')
 */
export function obterUsuariosPorPrefixo(prefixo: string) {
    return Object.keys(usuarios.usuario || {})
        .filter(nome => nome.startsWith(prefixo))
        .map(nome => {
            const dados = usuarios.usuario[nome];
            return {
                nome,
                credenciais: {
                    titulo: dados.titulo,
                    senha: dados.senha || usuarios.senhaPadrao,
                    perfil: dados.perfil
                } as Usuario
            };
        });
}

/**
 * Verifica os componentes da tela de login
 */
export async function verificarTelaLogin(page: Page): Promise<void> {
    await expect(page.getByTestId(IDENTIFICADORES.FORM_LOGIN)).toBeVisible();
    await expect(page.getByTestId(IDENTIFICADORES.INPUT_USUARIO)).toBeVisible();
    await expect(page.getByTestId(IDENTIFICADORES.INPUT_SENHA)).toBeVisible();
    await expect(page.getByTestId(IDENTIFICADORES.BTN_ENTRAR)).toBeVisible();
}

/**
 * Realiza o login com as credenciais informadas.
 * Aguarda todos os componentes do formulário estarem prontos antes de interagir.
 */
export async function login(page: Page, usuario: Usuario): Promise<void> {
    await expect(page.getByTestId(IDENTIFICADORES.FORM_LOGIN)).toBeVisible();
    await expect(page.getByTestId(IDENTIFICADORES.INPUT_USUARIO)).toBeEnabled();
    await expect(page.getByTestId(IDENTIFICADORES.INPUT_SENHA)).toBeEnabled();
    await expect(page.getByTestId(IDENTIFICADORES.BTN_ENTRAR)).toBeEnabled();
    await page.getByTestId(IDENTIFICADORES.INPUT_USUARIO).fill(usuario.titulo);
    await page.getByTestId(IDENTIFICADORES.INPUT_SENHA).fill(usuario.senha);
    await page.getByTestId(IDENTIFICADORES.BTN_ENTRAR).click();

    if (usuario.perfil) {
        const seletorPerfil = page.getByTestId(IDENTIFICADORES.SELECT_PERFIL);
        const apareceu = await seletorPerfil.waitFor({ state: 'visible' }).then(() => true).catch(() => false);
        if (apareceu) {
            await seletorPerfil.selectOption({ label: usuario.perfil });
            await page.getByTestId(IDENTIFICADORES.BTN_ENTRAR).click();
        }
    }
}

/**
 * Realiza o logout e valida o retorno à tela de login.
 */
export async function efetuarLogout(page: Page): Promise<void> {
    const btnLogout = page.getByTestId(IDENTIFICADORES.BTN_LOGOUT);
    await expect(btnLogout).toBeVisible();
    await btnLogout.click();
    await expect(page).toHaveURL(/\/login/);
}

/**
 * Navega para a URL informada
 */
export async function acessarURL(page: Page, caminho: string): Promise<void> {
    await page.goto(caminho);
}

/**
 * Clica no elemento de navegação identificado pelo testId e aguarda o carregamento da tela
 */
export async function selecionarTela(page: Page, testId: string, urlEsperada: string | RegExp): Promise<void> {
    await page.getByTestId(testId).click();
    await expect(page).toHaveURL(urlEsperada);
}

/**
 * Aguarda a estabilização do Painel após o login ou redirecionamento.
 */
export async function aguardarPainelCarregado(page: Page): Promise<void> {
    await page.waitForURL(/\/painel(?:\?.*)?$/);
    const carregando = page.getByTestId(IDENTIFICADORES.PAINEL_CARREGANDO);
    if (await carregando.count() > 0) {
        await expect(carregando).toBeHidden();
    }
}

/**
 * Aguarda o painel carregar E aguarda que a linha do processo apareça na tabela de processos.
 *
 * A tabela `tbl-processos` só é renderizada após o bootstrap do painel completar (v-else do spinner).
 * Por isso aguardamos primeiro a tabela existir, e só então a linha específica do processo.
 */
export async function aguardarProcessoNoPainel(page: Page, descricaoProcesso: string): Promise<void> {
    await page.waitForURL(/\/painel(?:\?.*)?$/);
    await aguardarPainelCarregado(page);
    const tabela = page.getByTestId('tbl-processos');
    await expect(tabela).toBeVisible();
    const linhaProcesso = tabela.locator('tr', { hasText: descricaoProcesso });
    await expect(linhaProcesso).toBeVisible();
}


/**
 * Verifica que a tabela de movimentações do subprocesso contém uma entrada com os dados informados.
 * Deve ser chamado enquanto a tela de detalhes do subprocesso (ou de cadastro) está visível.
 *
 * @param origem    Sigla da unidade de origem (ex: 'SEDOC')
 * @param destino   Sigla da unidade de destino (ex: 'COEDE')
 * @param descricao Texto da coluna "Descrição" (ex: 'Cadastro disponibilizado')
 */
export async function verificarMovimentacao(
    page: Page,
    origem: string,
    destino: string,
    descricao: string
): Promise<void> {
    const tabela = page.getByTestId('tbl-movimentacoes');
    await expect(tabela).toBeVisible();
    const linha = tabela.locator('tr').filter({
        has: page.locator(`td:has-text("${origem}")`),
    }).filter({
        has: page.locator(`td:has-text("${destino}")`),
    }).filter({
        has: page.locator(`td:has-text("${descricao}")`),
    });
    await expect(linha).toBeVisible();
}


/**
 * Verifica que a tabela de alertas do painel contém uma entrada com a descrição e processo informados.
 * O painel deve já estar carregado (tbl-alertas visível) quando esta função for chamada.
 *
 * @param descricao Texto da coluna "Descrição" do alerta
 * @param processo  Descrição do processo ao qual o alerta pertence
 */
export async function verificarAlertaPainel(
    page: Page,
    descricao: string,
    processo: string
): Promise<void> {
    const tabela = page.getByTestId('tbl-alertas');
    await expect(tabela).toBeVisible();
    const linha = tabela.locator('tr').filter({
        has: page.locator(`td:has-text("${descricao}")`),
    }).filter({
        has: page.locator(`td:has-text("${processo}")`),
    });
    await expect(linha).toBeVisible();
}


/**
 * Retorna uma data futura no formato AAAA-MM-DD com base nos dias informados a partir de hoje.
 */
export function calcularDataLimite(dias: number): string {
    const data = new Date();
    data.setDate(data.getDate() + dias);
    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
}

/**
 * Retorna uma data futura no formato DD/MM/YYYY com base nos dias informados a partir de hoje.
 */
export function calcularDataLimiteFormatada(dias: number): string {
    const data = new Date();
    data.setDate(data.getDate() + dias);
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${dia}/${mes}/${ano}`;
}

/**
 * Preenche os dados básicos do formulário de cadastro de processo.
 */
async function preencherFormularioProcesso(page: Page, dados: {
    descricao: string;
    tipo: string;
    diasLimite: number;
}): Promise<void> {
    await page.getByTestId(IDENTIFICADORES.INPUT_DESCRICAO).fill(dados.descricao);
    await page.getByTestId(IDENTIFICADORES.SELECT_TIPO).selectOption(dados.tipo);
    await page.getByTestId(IDENTIFICADORES.INPUT_DATA_LIMITE).fill(calcularDataLimite(dados.diasLimite));
    await expect(page.getByText('Carregando unidades...')).toBeHidden();
}

/**
 * Expande os nós da árvore de unidades sequencialmente, aguardando a animação de cada um.
 */
async function expandirNosArvore(page: Page, siglas: string[]): Promise<void> {
    for (const sigla of siglas) {
        const botaoExpand = page.getByTestId(`${IDENTIFICADORES.PREFIXO_EXPAND_UNIDADE}${sigla}`);
        await expect(botaoExpand).toBeVisible();
        await botaoExpand.click();
        await expect(botaoExpand).toHaveAttribute('aria-expanded', 'true');
        await expect(
            page.getByTestId(`${IDENTIFICADORES.PREFIXO_EXPAND_UNIDADE}${sigla}`)
                .locator('xpath=../..')
                .locator('[class*="tree-node"], [data-testid^="btn-arvore"], [data-testid^="chk-arvore"]')
                .first()
        ).toBeVisible();
    }
}

/**
 * Marca os checkboxes das unidades participantes na árvore.
 * Aguarda o Ajax carregar os filhos e a árvore parar de se mover antes de cada clique.
 */
async function marcarUnidadesNaArvore(page: Page, siglas: string[]): Promise<void> {
    for (const sigla of siglas) {
        const checkbox = page.getByTestId(`${IDENTIFICADORES.PREFIXO_CHK_UNIDADE}${sigla}`);
        await expect(checkbox).toBeVisible();
        await expect(checkbox).toBeEnabled();
        await checkbox.scrollIntoViewIfNeeded();

        if (!await checkbox.isChecked()) {
            await checkbox.check();
            await expect(checkbox).toBeChecked();
        }
    }
}

/**
 * Clica no botão "Iniciar" e confirma o diálogo de confirmação de início do processo.
 */
async function confirmarInicioProcesso(page: Page): Promise<void> {
    const botaoIniciar = page.getByTestId(IDENTIFICADORES.BTN_INICIAR_RODAPE);
    await botaoIniciar.scrollIntoViewIfNeeded();
    await botaoIniciar.click();
    const dialog = page.getByRole('dialog');
    await expect(dialog).toBeVisible();
    await dialog.getByTestId(IDENTIFICADORES.BTN_CONFIRMAR_INICIO).click();
}

/**
 * Gerencia o modal de unidades interoperacionais (equipe própria), se ele abrir.
 * Marca apenas as siglas informadas e desmarca as demais.
 */
async function configurarUnidadesInteroperacionais(page: Page, incluir: boolean, siglas: string[]): Promise<void> {
    const modal = page.locator(IDENTIFICADORES.MODAL_INTEROPERACIONAL);
    const abriuModal = (await modal.count()) > 0 && await modal.isVisible().catch(() => false);
    if (!abriuModal) return;
    const siglasDesejadas = new Set(incluir ? siglas : []);
    const linhas = modal.locator('tbody tr');
    const totalLinhas = await linhas.count();
    for (let i = 0; i < totalLinhas; i++) {
        const linha = linhas.nth(i);
        const sigla = (await linha.locator('td').nth(1).innerText()).trim();
        const checkbox = linha.locator('input[type="checkbox"]');

        if (siglasDesejadas.has(sigla)) {
            await checkbox.check();
        } else {
            await checkbox.uncheck();
        }
    }

    const botaoConfirmar = modal.getByTestId(IDENTIFICADORES.BTN_CONFIRMAR_MODAL);
    await expect(botaoConfirmar).toBeVisible();
    await expect(botaoConfirmar).toBeEnabled();
    await botaoConfirmar.click();
}

/**
 * Cria e inicia um processo de mapeamento pela UI.
 *
 * @param options.unidades                 Siglas das unidades a marcar na árvore
 * @param options.expandir                 Siglas dos nós da árvore a expandir antes de marcar as unidades
 * @param options.incluirInteroperacional  Se verdadeiro, marca apenas as unidades de `interoperacionais` no modal. Se falso, desmarca todas.
 * @param options.interoperacionais        Lista de siglas das unidades interoperacionais a manter marcadas no modal
 */
export async function criarProcesso(page: Page, options: {
    descricao: string;
    unidades: string[];
    expandir: string[];
    incluirInteroperacional: boolean;
    interoperacionais?: string[];
}): Promise<void> {
    await selecionarTela(page, IDENTIFICADORES.BOTAO_CRIAR_PROCESSO, ROTAS.CADASTRO_PROCESSO);
    await preencherFormularioProcesso(page, { descricao: options.descricao, tipo: 'MAPEAMENTO', diasLimite: 15 });
    await expandirNosArvore(page, options.expandir);
    await marcarUnidadesNaArvore(page, options.unidades);
    await confirmarInicioProcesso(page);
    await configurarUnidadesInteroperacionais(page, options.incluirInteroperacional, options.interoperacionais ?? []);
    await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
}

/**
 * Faz login como admin, navega para a tela de notificações e verifica se a notificação
 * correspondente aos critérios informados foi gerada corretamente.
 *
 * A busca é feita via API (polling) para garantir robustez em ambientes lentos.
 * Ao encontrar a notificação, abre o modal de preview (ícone do olho) e valida o corpo:
 * - `interoperacional: true`  → corpo menciona "nas unidades" (notificação para chefia superior com lista de unidades subordinadas)
 * - `interoperacional: false` → corpo menciona "para a sua unidade" (notificação direta à unidade participante)
 *
 * @param options.origem          Sigla da origem esperada na tabela (ex: 'ADMIN')
 * @param options.destino         Sigla do destinatário esperado na tabela (ex: 'SEDOC')
 * @param options.processo        Trecho único da descrição do processo para identificação do e-mail
 * @param options.assunto         Texto ou regex do assunto esperado
 * @param options.interoperacional Se verdadeiro, valida a variante "em unidades subordinadas"
 */
export async function verificarNotificacao(page: Page, options: {
    origem: string;
    destino: string;
    processo: string;
    assunto: string | RegExp;
    interoperacional: boolean;
    textosCorpo?: (string | RegExp)[];
}): Promise<void> {
    const isLoginVisible = await page.getByTestId(IDENTIFICADORES.INPUT_USUARIO).isVisible().catch(() => false);
    if (isLoginVisible || page.url().includes(ROTAS.LOGIN)) {
        await login(page, obterCredenciaisUsuario('ADMIN'));
        await aguardarPainelCarregado(page);
    }
    if (!page.url().includes(ROTAS.NOTIFICACOES_ADMIN)) {
        await acessarURL(page, ROTAS.NOTIFICACOES_ADMIN);
        await expect(page).toHaveURL(/\/administracao\/notificacoes/);
    }

    interface NotificacaoApi { codigo: number; assunto: string; destinatario: string; unidadeSigla?: string | null; }

    const urlListagem = /\/api\/admin\/notificacoes\/listar(?:\?|$)/;
    const botaoAtualizar = page.getByTestId('btn-notificacoes-atualizar');

    let codigoNotificacao: number | undefined;

    await expect.poll(async () => {
        await Promise.all([
            page.waitForResponse(r => urlListagem.test(r.url()) && r.ok()),
            botaoAtualizar.click()
        ]);

        const notificacoes: NotificacaoApi[] = await page.evaluate(async () => {
            const r = await fetch('/api/admin/notificacoes/listar?limite=50', { credentials: 'include' });
            return r.ok ? r.json() : [];
        });

        const encontrada = notificacoes.find(n => {
            const assuntoSemPrefixo = n.assunto.replace(/^SGC:\s*/i, '').trim();
            const destinatarios = [n.unidadeSigla, n.destinatario].filter(Boolean).map(v => v!.toUpperCase());

            const assuntoOk = typeof options.assunto === 'string'
                ? assuntoSemPrefixo.includes(options.assunto)
                : options.assunto.test(assuntoSemPrefixo);

            const destinatarioOk = destinatarios.some(d => d.includes(options.destino.toUpperCase()));

            return assuntoOk && destinatarioOk;
        });

        codigoNotificacao = encontrada?.codigo;
        return codigoNotificacao ? 1 : 0;
    }, {
        message: `Notificação não encontrada — destino: "${options.destino}", assunto: "${String(options.assunto)}"`,
        intervals: [1000, 2000, 3000]
    }).toBeGreaterThan(0);

    const tabela = page.getByTestId(IDENTIFICADORES.TABELA_NOTIFICACOES);
    const linhaNotificacao = tabela.locator('tr').filter({ hasText: options.destino }).first();
    await expect(linhaNotificacao).toBeVisible();
    await expect(linhaNotificacao).toContainText(options.origem);

    await page.getByTestId(`${IDENTIFICADORES.PREFIXO_BTN_PREVIEW_NOTIFICACAO}${codigoNotificacao}`).click();

    const modal = page.getByTestId(IDENTIFICADORES.MODAL_PREVIEW_EMAIL);
    await expect(modal).toBeVisible();

    const corpo = modal.frameLocator(`[data-testid="${IDENTIFICADORES.IFRAME_PREVIEW_EMAIL}"]`).locator('body');

    await expect(corpo).toContainText(options.processo);

    if (options.textosCorpo) {
        for (const texto of options.textosCorpo) {
            await expect(corpo).toContainText(texto);
        }
    } else {
        if (options.interoperacional) {
            await expect(corpo).toContainText(/nas unidades/i);
        } else {
            await expect(corpo).toContainText(/para a sua unidade/i);
        }
    }

    await page.getByTestId(IDENTIFICADORES.BTN_FECHAR_PREVIEW_EMAIL).click();
    await expect(modal).toBeHidden();
}

/**
 * Gera dinamicamente cenários de teste de acesso para todos os usuários de um determinado perfil.
 *
 * @param perfil Nome exato do perfil no JSON (ex: 'ADMIN', 'SERVIDOR - SEDOC')
 */
export function verifPerfil(perfil: string): void {
    const listaUsuarios = obterUsuariosPorPerfil(perfil);
    for (const usuario of listaUsuarios) {
        test(`Acesso com sucesso para: ${usuario.nome}`, async ({ page }) => {
            await verificarTelaLogin(page);
            await login(page, usuario.credenciais);
            await efetuarLogout(page);
        });
    }
}
