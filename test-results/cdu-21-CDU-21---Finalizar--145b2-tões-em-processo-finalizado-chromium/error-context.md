# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cdu-21.spec.ts >> CDU-21 - Finalizar processo de mapeamento ou de revisão >> Cenario 4: Verificar ausência de botões em processo finalizado
- Location: e2e/cdu-21.spec.ts:86:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: getByTestId('btn-subprocesso-acoes')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for getByTestId('btn-subprocesso-acoes')

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
                - /url: /processo/400
            - listitem [ref=e63]:
              - text: ›
              - generic [ref=e64]: SECAO_221
      - main [ref=e65]:
        - generic [ref=e67]:
          - generic [ref=e68]:
            - generic [ref=e70]:
              - heading "SECAO_221" [level=2] [ref=e71]
              - paragraph [ref=e72]: Seção 221
            - generic [ref=e74]:
              - paragraph [ref=e75]:
                - strong [ref=e76]: "Processo:"
                - text: Mapeamento CDU-21 1777767518298
              - paragraph [ref=e77]: Situação:Mapa homologado
              - paragraph [ref=e78]: Localização atual:SECAO_221
              - paragraph [ref=e79]: Prazo para conclusão da etapa atual:01/06/2026
              - paragraph [ref=e80]:
                - strong [ref=e81]: "Titular:"
                - text: Tina Turner
              - paragraph [ref=e82]:
                - generic [ref=e83]:
                  - generic [ref=e84]: 
                  - text: "2018"
                - generic [ref=e85]:
                  - generic [ref=e86]: 
                  - link "tina.turner@tre-pe.jus.br" [ref=e87] [cursor=pointer]:
                    - /url: mailto:tina.turner@tre-pe.jus.br
          - generic [ref=e88]:
            - button "Atividades e conhecimentos Cadastro de atividades e conhecimentos da unidade" [ref=e90] [cursor=pointer]:
              - generic [ref=e92]:
                - heading "Atividades e conhecimentos" [level=4] [ref=e93]
                - paragraph [ref=e94]: Cadastro de atividades e conhecimentos da unidade
            - button "Mapa de competências Mapa de competências técnicas da unidade" [ref=e96] [cursor=pointer]:
              - generic [ref=e98]:
                - heading "Mapa de competências" [level=4] [ref=e99]
                - paragraph [ref=e100]: Mapa de competências técnicas da unidade
          - generic [ref=e101]:
            - heading "Movimentações" [level=4] [ref=e102]
            - table [ref=e104]:
              - rowgroup [ref=e105]:
                - row "Data/hora Origem Destino Descrição" [ref=e106]:
                  - columnheader "Data/hora" [ref=e107]
                  - columnheader "Origem" [ref=e108]
                  - columnheader "Destino" [ref=e109]
                  - columnheader "Descrição" [ref=e110]
              - rowgroup [ref=e111]:
                - row "02/05/2026 21:18 SECAO_221 SECAO_221 Movimentação automática via fixture" [ref=e112]:
                  - cell "02/05/2026 21:18" [ref=e113]
                  - cell "SECAO_221" [ref=e114]
                  - cell "SECAO_221" [ref=e115]
                  - cell "Movimentação automática via fixture" [ref=e116]
                - row "02/05/2026 21:18 ADMIN SECAO_221 Processo iniciado" [ref=e117]:
                  - cell "02/05/2026 21:18" [ref=e118]
                  - cell "ADMIN" [ref=e119]
                  - cell "SECAO_221" [ref=e120]
                  - cell "Processo iniciado" [ref=e121]
      - contentinfo [ref=e122]:
        - generic [ref=e123]:
          - generic [ref=e124]: Versão 1.0.0
          - generic [ref=e125]: © SESEL/COSIS/TRE-PE
  - text: 
```

# Test source

```ts
  2   | import {
  3   |     criarProcessoMapaHomologadoFixture,
  4   |     criarProcessoMapaValidadoFixture,
  5   |     criarProcessoRevisaoMapaHomologadoFixture,
  6   |     validarProcessoFixture
  7   | } from './fixtures/index.js';
  8   | import {acessarDetalhesProcesso, verificarDetalhesProcesso} from './helpers/helpers-processos.js';
  9   | import {navegarParaSubprocesso, obterAcaoCabecalhoSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';
  10  | import {TEXTOS} from '../frontend/src/constants/textos.js';
  11  | 
  12  | test.describe.serial('CDU-21 - Finalizar processo de mapeamento ou de revisão', () => {
  13  |     const UNIDADE_ALVO = 'SECAO_221';
  14  | 
  15  |     const timestamp = Date.now();
  16  |     const descProcesso = `Mapeamento CDU-21 ${timestamp}`;
  17  |     let codProcesso: number;
  18  | 
  19  |     test('Setup data', async ({_resetAutomatico, request}) => {
  20  |         const processo = await criarProcessoMapaHomologadoFixture(request, {
  21  |             descricao: descProcesso,
  22  |             unidade: UNIDADE_ALVO
  23  |         });
  24  |         codProcesso = processo.codigo;
  25  |         validarProcessoFixture(processo, descProcesso);
  26  |     });
  27  | 
  28  |     // TESTES PRINCIPAIS - CDU-21
  29  | 
  30  |     test('Cenario 1: ADMIN navega para detalhes do processo', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  31  |         // CDU-21: Passos 1-2
  32  | 
  33  |         await acessarDetalhesProcesso(page, descProcesso);
  34  | 
  35  |         await expect(page.getByTestId('processo-info')).toBeVisible();
  36  | 
  37  |         // Botão finalizar visível
  38  |         await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
  39  |     });
  40  | 
  41  |     test('Cenario 2: ADMIN cancela finalização - permanece na tela', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  42  | 
  43  | 
  44  |         await acessarDetalhesProcesso(page, descProcesso);
  45  | 
  46  |         await page.getByTestId('btn-processo-finalizar').click();
  47  | 
  48  |         const modal = page.getByRole('dialog');
  49  |         await expect(modal).toBeVisible();
  50  | 
  51  |         // CDU-21 Passo 6: verificar título, mensagem completa e botões do modal
  52  |         await expect(modal.getByRole('heading', {name: TEXTOS.processo.FINALIZACAO_TITULO})).toBeVisible();
  53  |         await expect(modal.getByText(TEXTOS.processo.FINALIZACAO_CONFIRMACAO_PREFIXO)).toBeVisible();
  54  |         await expect(modal.getByText(descProcesso)).toBeVisible();
  55  |         await expect(modal.getByText(TEXTOS.processo.FINALIZACAO_CONFIRMACAO_COMPLEMENTO)).toBeVisible();
  56  |         await expect(page.getByTestId('btn-finalizar-processo-cancelar')).toBeVisible();
  57  |         await expect(page.getByTestId('btn-finalizar-processo-confirmar')).toBeVisible();
  58  | 
  59  |         await page.getByTestId('btn-finalizar-processo-cancelar').click();
  60  | 
  61  |         // Permanece na tela de detalhes do processo
  62  |         await expect(page.getByTestId('processo-info')).toBeVisible();
  63  |         await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
  64  |     });
  65  | 
  66  |     test('Cenario 3: ADMIN finaliza processo com sucesso', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  67  |         // CDU-21: Passos 7-10
  68  | 
  69  | 
  70  |         await acessarDetalhesProcesso(page, descProcesso);
  71  | 
  72  |         await page.getByTestId('btn-processo-finalizar').click();
  73  | 
  74  |         const modal = page.getByRole('dialog');
  75  |         await expect(modal).toBeVisible();
  76  | 
  77  |         await page.getByTestId('btn-finalizar-processo-confirmar').click();
  78  | 
  79  |         await verificarPaginaPainel(page);
  80  |         await expect(page.getByText(TEXTOS.sucesso.PROCESSO_FINALIZADO)).toBeVisible();
  81  | 
  82  |         // Verificar que processo não aparece mais no painel ativo (foi finalizado)
  83  |         // (Processo finalizado não aparece na lista de processos ativos)
  84  |     });
  85  | 
  86  |     test('Cenario 4: Verificar ausência de botões em processo finalizado', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  87  |         // Garantir que botões de ação não aparecem para processos finalizados
  88  | 
  89  |         await page.goto(`/processo/${codProcesso}`);
  90  |         await expect(page.getByTestId('processo-info')).toBeVisible();
  91  | 
  92  |         await expect(page.getByText(/Situação:\s*Finalizado/i)).toBeVisible();
  93  | 
  94  | 
  95  |         await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
  96  |         await expect(page.getByTestId('btn-processo-finalizar')).toBeDisabled();
  97  | 
  98  |         await expect(page.getByTestId('btn-processo-acoes-bloco')).toBeVisible();
  99  |         await expect(page.getByTestId('btn-processo-acoes-bloco').getByRole('button')).toBeEnabled();
  100 | 
  101 |         await navegarParaSubprocesso(page, 'SECAO_221');
> 102 |         await expect(page.getByTestId('btn-subprocesso-acoes')).toBeVisible();
      |                                                                 ^ Error: expect(locator).toBeVisible() failed
  103 |         await expect(await obterAcaoCabecalhoSubprocesso(page, 'btn-enviar-lembrete')).toBeDisabled();
  104 |         await expect(await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-cadastro')).toBeDisabled();
  105 |         await expect(await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-revisao')).toBeDisabled();
  106 |         await expect(await obterAcaoCabecalhoSubprocesso(page, 'btn-alterar-data-limite')).toBeDisabled();
  107 | 
  108 |         await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();
  109 |         await expect(page.getByTestId('card-subprocesso-atividades')).toHaveClass(/card-actionable/);
  110 |         await expect(page.getByTestId('card-subprocesso-atividades')).toContainText('Cadastro de atividades e conhecimentos da unidade');
  111 |     });
  112 | });
  113 | 
  114 | test.describe.serial('CDU-21 - Processo com mapas não homologados', () => {
  115 |     const UNIDADE_ALVO = 'SECAO_221';
  116 |     const timestamp = Date.now();
  117 |     const descProcessoErro = `Mapeamento CDU-21 Erro ${timestamp}`;
  118 | 
  119 |     test('Setup data: processo com mapa validado mas não homologado', async ({_resetAutomatico, request}) => {
  120 |         const processo = await criarProcessoMapaValidadoFixture(request, {
  121 |             descricao: descProcessoErro,
  122 |             unidade: UNIDADE_ALVO
  123 |         });
  124 |         validarProcessoFixture(processo, descProcessoErro);
  125 |     });
  126 | 
  127 |     test('Cenario 5: ADMIN não vê botão Finalizar quando mapas não estão todos homologados', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  128 |         // CDU-21 Passos 4-5: sistema verifica situação dos subprocessos e bloqueia finalização
  129 |         await acessarDetalhesProcesso(page, descProcessoErro);
  130 |         await expect(page.getByTestId('processo-info')).toBeVisible();
  131 | 
  132 |         // Processo com mapa validado (não homologado): botão Finalizar não deve estar disponível
  133 |         await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
  134 |         await expect(page.getByTestId('btn-processo-finalizar')).toBeDisabled();
  135 |     });
  136 | });
  137 | 
  138 | test.describe.serial('CDU-21 - Finalizar processo de REVISÃO', () => {
  139 |     const UNIDADE_ALVO = 'SECAO_212';
  140 | 
  141 |     const timestamp = Date.now();
  142 |     const descProcessoRevisao = `Revisão CDU-21 ${timestamp}`;
  143 |     let codProcessoRevisao: number;
  144 | 
  145 |     test('Setup: Criar processo de revisão com mapa homologado', async ({_resetAutomatico, request}) => {
  146 |         const processo = await criarProcessoRevisaoMapaHomologadoFixture(request, {
  147 |             descricao: descProcessoRevisao,
  148 |             unidade: UNIDADE_ALVO
  149 |         });
  150 |         codProcessoRevisao = processo.codigo;
  151 |         validarProcessoFixture(processo, descProcessoRevisao);
  152 |     });
  153 | 
  154 |     test('Cenario 1: ADMIN finaliza processo de revisão com sucesso', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  155 |         await acessarDetalhesProcesso(page, descProcessoRevisao);
  156 | 
  157 |         // Verificar que é processo de revisão
  158 |         await verificarDetalhesProcesso(page, {
  159 |             descricao: descProcessoRevisao,
  160 |             tipo: 'Revisão',
  161 |             situacao: 'Em andamento'
  162 |         });
  163 |         await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
  164 | 
  165 |         await page.getByTestId('btn-processo-finalizar').click();
  166 | 
  167 |         const modal = page.getByRole('dialog');
  168 |         await expect(modal).toBeVisible();
  169 |         await expect(modal.getByText(/Confirma a finalização/i)).toBeVisible();
  170 | 
  171 |         await page.getByTestId('btn-finalizar-processo-confirmar').click();
  172 | 
  173 |         await verificarPaginaPainel(page);
  174 |         await expect(page.getByText(TEXTOS.sucesso.PROCESSO_FINALIZADO)).toBeVisible();
  175 |     });
  176 | 
  177 |     test('Cenario 2: Verificar ausência de botões em processo de revisão finalizado', async ({_resetAutomatico, page, _autenticadoComoAdmin}) => {
  178 |         await page.goto(`/processo/${codProcessoRevisao}`);
  179 |         await expect(page.getByTestId('processo-info')).toBeVisible();
  180 | 
  181 |         await expect(page.getByText(/Situação:\s*Finalizado/i)).toBeVisible();
  182 | 
  183 |         await expect(page.getByTestId('btn-processo-finalizar')).toBeVisible();
  184 |         await expect(page.getByTestId('btn-processo-finalizar')).toBeDisabled();
  185 | 
  186 |         await navegarParaSubprocesso(page, UNIDADE_ALVO);
  187 |         await expect(page.getByTestId('btn-subprocesso-acoes')).toBeVisible();
  188 |         await expect(await obterAcaoCabecalhoSubprocesso(page, 'btn-enviar-lembrete')).toBeDisabled();
  189 |         await expect(await obterAcaoCabecalhoSubprocesso(page, 'btn-reabrir-revisao')).toBeDisabled();
  190 |     });
  191 | });
  192 | 
```