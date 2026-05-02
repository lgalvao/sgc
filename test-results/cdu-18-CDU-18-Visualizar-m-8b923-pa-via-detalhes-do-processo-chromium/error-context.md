# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-18.spec.ts >> CDU-18: Visualizar mapa de competências >> Cenário 1: ADMIN visualiza mapa via detalhes do processo
- Location: e2e/cdu-18.spec.ts:20:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('subprocesso-header__txt-header-unidade')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('subprocesso-header__txt-header-unidade')

```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - heading "SGC" [level=1] [ref=e2]
  - generic [ref=e3]:
    - link "Pular para o conteúdo principal" [ref=e4] [cursor=pointer]:
      - /url: "#main-content"
    - generic [ref=e5]:
      - navigation [ref=e6]:
        - generic [ref=e7]:
          - link "SGC" [ref=e8] [cursor=pointer]:
            - /url: /painel
          - generic [ref=e9]:
            - list [ref=e10]:
              - listitem [ref=e11]:
                - link "Painel" [ref=e12] [cursor=pointer]:
                  - /url: /painel
                  - generic [ref=e13]: 
                  - text: Painel
              - listitem [ref=e14]:
                - link "Unidades" [ref=e15] [cursor=pointer]:
                  - /url: /unidades
                  - generic [ref=e16]: 
                  - text: Unidades
              - listitem [ref=e17]:
                - link "Relatórios" [ref=e18] [cursor=pointer]:
                  - /url: /relatorios
                  - generic [ref=e19]: 
                  - text: Relatórios
              - listitem [ref=e20]:
                - link "Histórico" [ref=e21] [cursor=pointer]:
                  - /url: /historico
                  - generic [ref=e22]: 
                  - text: Histórico
            - list [ref=e23]:
              - listitem [ref=e24]:
                - link "ADMIN" [ref=e25] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e26]:
                    - generic [ref=e27]: 
                    - generic [ref=e28]: ADMIN
              - listitem "Notificações" [ref=e29]:
                - link "Notificações" [ref=e30] [cursor=pointer]:
                  - /url: /administracao/notificacoes
                  - generic [ref=e31]: Notificações
                  - generic [ref=e32]: 
              - listitem "Configurações" [ref=e33]:
                - link "Configurações" [ref=e34] [cursor=pointer]:
                  - /url: /configuracoes
                  - generic [ref=e35]: Configurações
                  - generic [ref=e36]: 
              - listitem "Administradores do sistema" [ref=e37]:
                - link "Administradores" [ref=e38] [cursor=pointer]:
                  - /url: /administradores
                  - generic [ref=e39]: Administradores
                  - generic [ref=e40]: 
              - listitem [ref=e41]:
                - generic [ref=e42]:
                  - button "Ações Especiais" [ref=e43] [cursor=pointer]:
                    - generic [ref=e44]: Ações Especiais
                    - generic [ref=e45]: 
                  - text: 
              - listitem "Sair" [ref=e46]:
                - link "Sair" [ref=e47] [cursor=pointer]:
                  - /url: "#"
                  - generic [ref=e48]: Sair
                  - generic [ref=e49]: 
      - generic [ref=e52]:
        - button "Voltar" [ref=e53] [cursor=pointer]:
          - generic [ref=e54]: 
        - navigation "breadcrumb" [ref=e55]:
          - list [ref=e56]:
            - listitem [ref=e57]:
              - link "Início" [ref=e58] [cursor=pointer]:
                - /url: /painel
                - generic [ref=e59]: 
                - generic [ref=e60]: Início
            - listitem [ref=e61]:
              - text: ›
              - link "Detalhes do processo" [ref=e62] [cursor=pointer]:
                - /url: /processo/99
            - listitem [ref=e63]:
              - text: ›
              - link "ASSESSORIA_12" [ref=e64] [cursor=pointer]:
                - /url: /processo/99/ASSESSORIA_12
            - listitem [ref=e65]:
              - text: ›
              - generic [ref=e66]: Mapa de competências
      - main [ref=e67]:
        - generic [ref=e68]:
          - generic [ref=e69]:
            - generic [ref=e70]:
              - heading "Mapa de competências técnicas" [level=2] [ref=e71]
              - paragraph [ref=e72]:
                - generic [ref=e73]: ASSESSORIA_12
            - generic [ref=e75]:
              - button "Histórico de análise" [ref=e76] [cursor=pointer]
              - button "Ações" [ref=e78] [cursor=pointer]
          - generic [ref=e81]:
            - heading "Competência Técnica Seed 99" [level=4] [ref=e83]:
              - strong [ref=e84]: Competência Técnica Seed 99
            - generic [ref=e86]:
              - generic [ref=e88]:
                - generic [ref=e89]: Atividade Seed 1
                - list [ref=e91]:
                  - listitem [ref=e92]: Conhecimento Seed 1.1
              - generic [ref=e94]:
                - generic [ref=e95]: Atividade Seed 2
                - list [ref=e97]:
                  - listitem [ref=e98]: Conhecimento Seed 2.1
      - contentinfo [ref=e99]:
        - generic [ref=e100]:
          - generic [ref=e101]: Versão 1.0.0
          - generic [ref=e102]: © SESEL/COSIS/TRE-PE
  - text: 
```

# Test source

```ts
  1  | import {expect, test} from './fixtures/complete-fixtures.js';
  2  | import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';
  3  | import {navegarParaMapa} from './helpers/helpers-mapas.js';
  4  | import {TEXTOS} from '../frontend/src/constants/textos.js';
  5  | 
  6  | /**
  7  |  * CDU-18: Visualizar mapa de competências
  8  |  *
  9  |  * Pré-condições do CDU-18:
  10 |  * - Usuário logado com qualquer perfil
  11 |  * - Processo de mapeamento ou de revisão iniciado ou finalizado
  12 |  * - Subprocesso da unidade com mapa de competência já disponibilizado
  13 |  *
  14 |  * O seed contém:
  15 |  * - Processo 99 (FINALIZADO) com mapa homologado para unidade de assessoria
  16 |  * - Mapa 99 com competência "Competência técnica seed 99" vinculada às atividades
  17 |  */
  18 | test.describe('CDU-18: Visualizar mapa de competências', () => {
  19 | 
  20 |     test('Cenário 1: ADMIN visualiza mapa via detalhes do processo', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  21 |         await test.step('1. Login como ADMIN', async () => {
  22 |             // Já autenticado via fixture
  23 |         });
  24 | 
  25 |         await test.step('2. Navegar para processo finalizado com mapa', async () => {
  26 |             // Clicar no processo 99 que tem mapa homologado
  27 |             await acessarDetalhesProcesso(page, 'Processo 99');
  28 |             await expect(page).toHaveURL(/\/processo\/\d+(?:\?.*)?$/);
  29 |         });
  30 | 
  31 |         await test.step('3. Selecionar unidade de assessoria participante', async () => {
  32 |             const linhaUnidade = page.getByRole('row', {name: /ASSESSORIA_/}).first();
  33 |             await expect(linhaUnidade).toBeVisible();
  34 |             await linhaUnidade.click();
  35 | 
  36 |             // Verificar navegação para detalhes do subprocesso
  37 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/ASSESSORIA_(?:\d+|12)(?:\?.*)?$`));
  38 |         });
  39 | 
  40 | 
  41 |         await test.step('4. Acessar mapa de competências via card', async () => {
  42 |             // Verificar que card de mapa está disponível e acessível
  43 |             await navegarParaMapa(page);
  44 |         });
  45 | 
  46 |         await test.step('5. Verificar visualização do mapa (CDU-18)', async () => {
  47 |             // 5.1 Título "Mapa de competências técnicas"
  48 |             await expect(page.getByRole('heading', {name: TEXTOS.mapa.TITULO_TECNICO})).toBeVisible();
  49 | 
  50 |             // 5.2 Identificação da unidade (sigla)
  51 |             const headerUnidade = page.getByTestId('subprocesso-header__txt-header-unidade');
> 52 |             await expect(headerUnidade).toBeVisible();
     |                                         ^ Error: expect(locator).toBeVisible() failed
  53 |             await expect(headerUnidade).toContainText(/ASSESSORIA_\d+/);
  54 | 
  55 |             // 5.3 Competência do seed
  56 |             await expect(page.getByText('Competência técnica seed 99')).toBeVisible();
  57 | 
  58 |             // 5.4 Atividades da competência
  59 |             await expect(page.getByText('Atividade seed 1')).toBeVisible();
  60 |             await expect(page.getByText('Atividade seed 2')).toBeVisible();
  61 | 
  62 |             // 5.5 Conhecimentos das atividades
  63 |             await expect(page.getByText('Conhecimento seed 1.1')).toBeVisible();
  64 |             await expect(page.getByText('Conhecimento seed 2.1')).toBeVisible();
  65 |         });
  66 |     });
  67 | 
  68 |     test('Cenário 2: CHEFE visualiza mapa da própria unidade', async ({_resetAutomatico, page, _autenticadoComoChefeAssessoria12}) => {
  69 |         await test.step('1. Login como CHEFE_ASSESSORIA_12', async () => {
  70 |             // Já autenticado via fixture
  71 |         });
  72 | 
  73 |         await test.step('2. Navegar para processo via painel', async () => {
  74 |             // CHEFE vê processo no painel e clica
  75 |             await acessarDetalhesProcesso(page, 'Processo 99');
  76 | 
  77 |             // CHEFE vai direto para detalhes do subprocesso da sua unidade
  78 |             await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/ASSESSORIA_12(?:\?.*)?$`));
  79 |         });
  80 | 
  81 |         await test.step('3. Acessar mapa de competências', async () => {
  82 |             await navegarParaMapa(page);
  83 |         });
  84 | 
  85 |         await test.step('4. Verificar visualização do mapa', async () => {
  86 |             await expect(page.getByRole('heading', {name: TEXTOS.mapa.TITULO_TECNICO})).toBeVisible();
  87 |             const headerUnidade = page.getByTestId('subprocesso-header__txt-header-unidade');
  88 |             await expect(headerUnidade).toBeVisible();
  89 |             await expect(headerUnidade).toHaveText('ASSESSORIA_12');
  90 |             await expect(page.getByText('Competência técnica seed 99')).toBeVisible();
  91 |         });
  92 |     });
  93 | });
  94 | 
```