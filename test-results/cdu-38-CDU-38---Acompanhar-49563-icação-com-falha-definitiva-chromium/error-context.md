# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-38.spec.ts >> CDU-38 - Acompanhar notificações por e-mail >> ADMIN visualiza detalhes, preview e reenfileira notificação com falha definitiva
- Location: e2e/cdu-38.spec.ts:5:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('tbl-notificacoes')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('tbl-notificacoes')

```

```yaml
- heading "SGC" [level=1]
- link "Pular para o conteúdo principal":
  - /url: "#main-content"
- navigation:
  - link "SGC":
    - /url: /painel
  - list:
    - listitem:
      - link "Painel":
        - /url: /painel
    - listitem:
      - link "Unidades":
        - /url: /unidades
    - listitem:
      - link "Relatórios":
        - /url: /relatorios
    - listitem:
      - link "Histórico":
        - /url: /historico
  - list:
    - listitem:
      - link "ADMIN":
        - /url: "#"
    - listitem "Notificações":
      - link "Notificações":
        - /url: /administracao/notificacoes
    - listitem "Configurações":
      - link "Configurações":
        - /url: /configuracoes
    - listitem "Administradores do sistema":
      - link "Administradores":
        - /url: /administradores
    - listitem "Ativar modo escuro":
      - link "Ativar modo escuro":
        - /url: "#"
    - listitem:
      - button "Ações especiais"
    - listitem "Sair":
      - link "Sair":
        - /url: "#"
- button "Voltar"
- navigation "breadcrumb":
  - list:
    - listitem:
      - link "Início":
        - /url: /painel
    - listitem: › Notificações
- main:
  - heading "Notificações" [level=2]
  - button "Atualizar"
  - alert:
    - text: Request failed with status code 400
    - button "Close"
- contentinfo: Versão 1.2.0 © SESEL/COSIS/TRE-PE
- button "Enviar feedback"
```

# Test source

```ts
  1   | import {expect, type Locator, type Page} from '@playwright/test';
  2   | 
  3   | export interface CriteriosNotificacaoAdmin {
  4   |     assunto: string | RegExp;
  5   |     destinatario?: string | RegExp;
  6   |     tipo?: string | RegExp;
  7   |     situacao?: string | RegExp;
  8   |     trechoCorpo?: string | RegExp;
  9   | }
  10  | 
  11  | interface NotificacaoAdminApi {
  12  |     codigo: number;
  13  |     unidadeSigla?: string | null;
  14  |     destinatario: string;
  15  |     tipoNotificacao: string;
  16  |     assunto: string;
  17  |     situacao: string;
  18  | }
  19  | 
  20  | const TIPOS_NOTIFICACAO_LABELS: Record<string, string> = {
  21  |     PROCESSO_INICIADO: 'Início do processo',
  22  |     PROCESSO_FINALIZADO: 'Finalização de processo',
  23  |     DATA_LIMITE_ALTERADA: 'Alteração da data limite',
  24  |     LEMBRETE_PRAZO: 'Lembrete de prazo',
  25  |     ATRIBUICAO_TEMPORARIA: 'Atribuição temporária',
  26  |     CADASTRO_DISPONIBILIZADO: 'Cadastro disponibilizado',
  27  |     CADASTRO_DEVOLVIDO: 'Cadastro devolvido para ajustes',
  28  |     CADASTRO_ACEITO: 'Cadastro aceito',
  29  |     CADASTRO_HOMOLOGADO: 'Cadastro homologado',
  30  |     CADASTRO_REABERTO: 'Cadastro reaberto',
  31  |     REVISAO_CADASTRO_DISPONIBILIZADA: 'Revisão de cadastro disponibilizada',
  32  |     REVISAO_CADASTRO_DEVOLVIDA: 'Revisão de cadastro devolvida',
  33  |     REVISAO_CADASTRO_ACEITA: 'Revisão de cadastro aceita',
  34  |     REVISAO_CADASTRO_HOMOLOGADA: 'Revisão de cadastro homologada',
  35  |     REVISAO_CADASTRO_REABERTA: 'Revisão de cadastro reaberta',
  36  |     MAPA_DISPONIBILIZADO: 'Mapa disponibilizado',
  37  |     MAPA_SUGESTOES_APRESENTADAS: 'Sugestões apresentadas para o mapa',
  38  |     MAPA_VALIDADO: 'Mapa validado',
  39  |     MAPA_VALIDACAO_DEVOLVIDA: 'Validação do mapa devolvida',
  40  |     MAPA_VALIDACAO_ACEITA: 'Validação do mapa aceita',
  41  |     MAPA_HOMOLOGADO: 'Mapa homologado'
  42  | };
  43  | 
  44  | function normalizarAssuntoVisivel(assunto: string): string {
  45  |     return assunto.replace(/^SGC:\s*/i, '').trim();
  46  | }
  47  | 
  48  | function correspondeTexto(valor: string | undefined | null, criterio?: string | RegExp): boolean {
  49  |     if (!criterio) return true;
  50  |     const texto = valor?.trim() || '';
  51  |     return typeof criterio === 'string' ? texto.includes(criterio) : criterio.test(texto);
  52  | }
  53  | 
  54  | function obterPossiveisDestinatarios(item: NotificacaoAdminApi): string[] {
  55  |     const valores = new Set<string>();
  56  |     if (item.unidadeSigla?.trim()) valores.add(item.unidadeSigla.trim().toUpperCase());
  57  |     if (item.destinatario?.trim()) valores.add(item.destinatario.trim());
  58  |     const local = item.destinatario?.trim().match(/^([^@]+)@tre-pe\.jus\.br$/i)?.[1];
  59  |     if (local) valores.add(local.toUpperCase());
  60  |     return [...valores];
  61  | }
  62  | 
  63  | function correspondeDestinatario(item: NotificacaoAdminApi, criterio?: string | RegExp): boolean {
  64  |     if (!criterio) return true;
  65  |     return obterPossiveisDestinatarios(item).some(valor => correspondeTexto(valor, criterio));
  66  | }
  67  | 
  68  | function correspondeTipo(item: NotificacaoAdminApi, criterio?: string | RegExp): boolean {
  69  |     if (!criterio) return true;
  70  |     const label = TIPOS_NOTIFICACAO_LABELS[item.tipoNotificacao];
  71  |     return correspondeTexto(item.tipoNotificacao, criterio) || correspondeTexto(label, criterio);
  72  | }
  73  | 
  74  | export async function abrirNotificacoesAdmin(page: Page): Promise<Locator> {
  75  |     const linkNotificacoes = page.getByTestId('nav-link-notificacoes');
  76  |     const urlListagem = /\/api\/admin\/notificacoes\/listar(?:\?|$)/;
  77  | 
  78  |     if (!(await linkNotificacoes.isVisible())) {
  79  |         const botaoNavbar = page.locator('.navbar-toggler:visible').first();
  80  |         await expect(botaoNavbar).toBeVisible();
  81  |         await botaoNavbar.click();
  82  |         await expect(linkNotificacoes).toBeVisible();
  83  |     }
  84  | 
  85  |     await linkNotificacoes.click();
  86  |     await expect(page).toHaveURL(/\/administracao\/notificacoes/);
  87  | 
  88  |     const tabela = page.getByTestId('tbl-notificacoes');
> 89  |     await expect(tabela).toBeVisible();
      |                          ^ Error: expect(locator).toBeVisible() failed
  90  |     await Promise.all([
  91  |         page.waitForResponse(response => urlListagem.test(response.url()) && response.ok()),
  92  |         page.getByTestId('btn-notificacoes-atualizar').click()
  93  |     ]);
  94  |     await expect(tabela).toBeVisible();
  95  |     return tabela;
  96  | }
  97  | 
  98  | export async function verificarNotificacaoAdmin(page: Page, criterios: CriteriosNotificacaoAdmin): Promise<void> {
  99  |     await abrirNotificacoesAdmin(page);
  100 | 
  101 |     async function listarNotificacoesApi(): Promise<NotificacaoAdminApi[]> {
  102 |         return await page.evaluate(async () => {
  103 |             const resposta = await fetch('/api/admin/notificacoes/listar?limite=50', {credentials: 'include'});
  104 |             if (!resposta.ok) {
  105 |                 throw new Error(`HTTP ${resposta.status}`);
  106 |             }
  107 |             return await resposta.json();
  108 |         });
  109 |     }
  110 | 
  111 |     let itemEncontrado: NotificacaoAdminApi | undefined;
  112 |     try {
  113 |         const botaoAtualizar = page.getByTestId('btn-notificacoes-atualizar');
  114 |         const urlListagem = /\/api\/admin\/notificacoes\/listar(?:\?|$)/;
  115 |         await expect.poll(async () => {
  116 |             await Promise.all([
  117 |                 page.waitForResponse(response => urlListagem.test(response.url()) && response.ok()),
  118 |                 botaoAtualizar.click()
  119 |             ]);
  120 |             itemEncontrado = localizarNotificacaoAdmin(await listarNotificacoesApi(), criterios);
  121 |             return itemEncontrado ? 1 : 0;
  122 |         }, {
  123 |             message: `Notificação não encontrada para assunto ${String(criterios.assunto)}`,
  124 |             timeout: 15000,
  125 |             intervals: [500, 1000, 1500, 2000]
  126 |         }).toBeGreaterThan(0);
  127 |     } catch {
  128 |         const notificacoes = await listarNotificacoesApi();
  129 |         const resumo = notificacoes
  130 |                 .slice(0, 10)
  131 |                 .map(item => `${item.tipoNotificacao} | ${item.unidadeSigla || item.destinatario} | ${item.assunto}`)
  132 |                 .join(' || ');
  133 | 
  134 |         throw new Error(
  135 |             `Notificação não encontrada para assunto ${String(criterios.assunto)}. ` +
  136 |             `Amostra da API admin: ${resumo || 'sem registros'}`
  137 |         );
  138 |     }
  139 | 
  140 |     const item = itemEncontrado!;
  141 |     const botaoDetalhes = page.getByTestId(`btn-detalhes-${item.codigo}`);
  142 |     await expect(botaoDetalhes).toBeVisible();
  143 | 
  144 |     if (!criterios.trechoCorpo) return;
  145 | 
  146 |     await page.getByTestId(`btn-preview-${item.codigo}`).click();
  147 |     const modal = page.getByTestId('modal-preview-email');
  148 |     await expect(modal).toBeVisible();
  149 |     const iframe = modal.frameLocator('[data-testid="iframe-preview-email"]');
  150 |     await expect(iframe.locator('body')).toContainText(criterios.trechoCorpo);
  151 |     await page.getByTestId('btn-fechar-preview-email').click();
  152 |     await expect(modal).toBeHidden();
  153 | }
  154 | 
  155 | export async function verificarAusenciaNotificacaoAdmin(page: Page, criterios: CriteriosNotificacaoAdmin): Promise<void> {
  156 |     await abrirNotificacoesAdmin(page);
  157 |     const notificacoes = await page.evaluate(async () => {
  158 |         const resposta = await fetch('/api/admin/notificacoes/listar?limite=50', {credentials: 'include'});
  159 |         if (!resposta.ok) {
  160 |             throw new Error(`HTTP ${resposta.status}`);
  161 |         }
  162 |         return await resposta.json();
  163 |     }) as NotificacaoAdminApi[];
  164 | 
  165 |     const item = localizarNotificacaoAdmin(notificacoes, criterios);
  166 |     expect(item, `Notificação inesperada encontrada para assunto ${String(criterios.assunto)}`).toBeUndefined();
  167 | }
  168 | 
  169 | function localizarNotificacaoAdmin(notificacoes: NotificacaoAdminApi[], criterios: CriteriosNotificacaoAdmin): NotificacaoAdminApi | undefined {
  170 |     return notificacoes.find(item =>
  171 |         correspondeTexto(normalizarAssuntoVisivel(item.assunto), criterios.assunto)
  172 |         && correspondeDestinatario(item, criterios.destinatario)
  173 |         && correspondeTipo(item, criterios.tipo)
  174 |         && correspondeTexto(item.situacao, criterios.situacao)
  175 |     );
  176 | }
  177 | 
```