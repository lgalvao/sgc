# Guia de Criação de Testes E2E — Semântica e DSL

Objetivo
- Fornecer um passo a passo prático e padronizado para criar novos testes E2E seguindo a arquitetura em 3 camadas e a DSL semântica adotada no projeto.

Arquitetura em 3 camadas (recap)
- Camada 1 — Constantes (dados e textos): [`e2e/cdu/helpers/dados/constantes-teste.ts`](e2e/cdu/helpers/dados/constantes-teste.ts:1)
  - Centralize todos os textos visíveis (`TEXTOS`), seletores/test-ids (`SELETORES`) e URLs (`URLS`) antes de escrever helpers ou specs.
- Camada 2 — Linguagem de domínio (helpers): [`e2e/cdu/helpers/`](e2e/cdu/helpers:1)
  - Subdiretórios esperados: `acoes/`, `verificacoes/`, `navegacao/`, `dados/`, `utils/`.
  - Helpers encapsulam toda a lógica de Playwright (`page.*`, `expect`) e expõem intenções de negócio em português.
- Camada 3 — Especificações (`spec`): `e2e/cdu/cdu-XX.spec.ts`
  - Specs apenas orquestram helpers semânticos; não devem conter lógica de controle, seletores ou expectations diretos.

Princípios essenciais (resumido)
- Nunca usar strings literais de UI no spec ou em helpers de alto nível — sempre via [`TEXTOS`](e2e/cdu/helpers/dados/constantes-teste.ts:91) / [`SELETORES`](e2e/cdu/helpers/dados/constantes-teste.ts:1).
- `page: Page` sempre como primeiro parâmetro em helpers.
- Try/catch é proibido nos specs — tratamentos devem ocorrer nos helpers.
- Verificações (helpers em `verificacoes/`) encapsulam `expect` e expõem estados legíveis.
- Ações (helpers em `acoes/`) descrevem intenções de negócio (ex.: `disponibilizarMapaComData`, `criarCompetencia`).

Passo a passo para criar um novo teste E2E

1) Planejamento
- Identifique o caso de uso (CDU) e o fluxo do usuário (pré-condição → ação → verificação).
- Liste os textos e seletores necessários. Atualize [`TEXTOS`](e2e/cdu/helpers/dados/constantes-teste.ts:91) e [`SELETORES`](e2e/cdu/helpers/dados/constantes-teste.ts:1) antes de implementar helpers.

2) Criar helpers de navegação (se necessário)
- Coloque em: [`e2e/cdu/helpers/navegacao/navegacao.ts`](e2e/cdu/helpers/navegacao/navegacao.ts:1)
- Exemplo de assinatura:
  - async function navegarParaMapaCompetencias(page: Page, idProcesso: number, unidade: string): Promise<void>

3) Criar ações (Camada 2 — `acoes/`)
- Local: [`e2e/cdu/helpers/acoes/`](e2e/cdu/helpers/acoes:1)
- Regras:
  - A ação deve representar uma intenção ("criarCompetencia", "disponibilizarMapaComData").
  - Use utilitários de fallback (ex.: `clicarPorTestIdOuRole`) em vez de `page.getByRole` direto.
  - Assinatura padrão: async function nome(page: Page, ...): Promise<void>
- Exemplo:
  - [`e2e/cdu/helpers/acoes/acoes-atividades.ts`](e2e/cdu/helpers/acoes/acoes-atividades.ts:1) contém padrões de como implementar `criarCompetencia(page, descricao)` e `disponibilizarMapaComData(page, data, observacoes)`.

4) Criar verificações (Camada 2 — `verificacoes/`)
- Local: [`e2e/cdu/helpers/verificacoes/`](e2e/cdu/helpers/verificacoes:1)
- Regras:
  - Encapsule `expect` e exponha um estado legível (ex.: `verificarCompetenciaVisivel(page, descricao)`).
  - Helpers tolerantes (fallbacks, timeouts) são permitidos, mas implementados aqui — specs não devem ver esses detalhes.
  - Exemplos: `verificarModalDisponibilizacaoVisivel`, `verificarDisponibilizacaoConcluida`.

5) Reexportar nos índices
- Depois de criar helpers, sempre reexporte em:
  - [`e2e/cdu/helpers/acoes/index.ts`](e2e/cdu/helpers/acoes/index.ts:1)
  - [`e2e/cdu/helpers/verificacoes/index.ts`](e2e/cdu/helpers/verificacoes/index.ts:1)
  - E no índice principal [`e2e/cdu/helpers/index.ts`](e2e/cdu/helpers/index.ts:1)

6) Escrever o spec (Camada 3)
- Local: `e2e/cdu/cdu-XX.spec.ts`
- Regras:
  - Use apenas helpers semânticos; o spec deve ser narrativa.
  - Não use `expect`, `page.locator`, `if`, `try/catch`, loops complexos.
- Esqueleto recomendado:
  - test.describe('CDU-XX: Descrição', () => {
  -   test.beforeEach(async ({page}) => { await helperDeCenario(page); });
  -   test('deve ...', async ({page}) => {
  -     await acaoSemantica(page, ...);
  -     await verificacaoSemantica(page, ...);
  -   });
  - });

Exemplo prático (esqueleto)
- Especificação (exemplo):
  - test('Deve disponibilizar mapa com sucesso', async ({page}) => {
  -   await acessarComoChefe(page);
  -   await criarCompetencia(page, 'Competência X');
  -   await disponibilizarMapaComData(page, '2025-12-31', 'Observações');
  -   await verificarDisponibilizacaoConcluida(page);
  - });

Helpers: padrões e assinaturas recomendadas
- Ação composta: export async function disponibilizarMapaComData(page: Page, dataLimite: string, observacoes?: string): Promise<void>
- Verificação composta: export async function verificarDisponibilizacaoConcluida(page: Page): Promise<void>
- Modal helpers: abrirModalDisponibilizacao, preencherDataModal, preencherObservacoesModal, verificarModalFechado

Modais e controles interativos
- Use test-ids sempre que possível; atualize [`SELETORES`](e2e/cdu/helpers/dados/constantes-teste.ts:1) com chaves novas (ex.: `BTN_DISPONIBILIZAR`, `BTN_DISPONIBILIZAR_PAGE`, `INPUT_DATA_LIMITE`).
- Na Camada 2, prefira localizar o modal por `.modal.show` e então procurar test-ids dentro do modal.
- Evite clicar por texto quando existirem múltiplos elementos com o mesmo texto. Prefira `getByTestId`.

Tratamento de falhas e fallbacks
- Toda a robustez (tentativa de seletores, clicks forçados, desabilitar backdrop) deve existir nos utilitários em `helpers/utils` e nos próprios helpers.
- Helpers tolerantes podem usar try/catch internamente, mas devem lançar um erro claro se o estado esperado não for atingido.

Fluxo de PR / revisão de código
- Checklist mínimo para PR de novos testes:
  - [ ] Atualizou `TEXTOS`/`SELETORES` se necessário.
  - [ ] Criou/atualizou helpers em `acoes/` e/ou `verificacoes/`.
  - [ ] Reexportou helpers nos `index.ts`.
  - [ ] Specs não contêm `expect`, `page.*`, `if` ou `try/catch`.
  - [ ] Testes locais rodaram com sucesso (`npx playwright test <arquivo>`).
  - [ ] Atualizou documentação (`plano-refatoracao-testes.md`, `e2e/cdu/README.md`) se introduziu novos padrões.

Debugging e execução local
- Quando um teste falhar:
  - Reproduza localmente em modo headed: `npx playwright test <spec> --project=chromium --headed`.
  - Analise `test-results/.../error-context.md` (Playwright gera snapshot do DOM no momento da falha).
  - Colete screenshot e logs da rede (Playwright tem opções de trace e screenshot).
  - Corrija helpers (não o spec) sempre que possível.

Referências e materiais
- Arquivo principal de orientação: [`plano-refatoracao-testes.md`](plano-refatoracao-testes.md:1)
- Resumo dos CDUs e filosofia: [`e2e/cdu/README.md`](e2e/cdu/README.md:1)
- Exemplos de helpers já implementados: [`e2e/cdu/helpers/acoes/acoes-atividades.ts`](e2e/cdu/helpers/acoes/acoes-atividades.ts:1), [`e2e/cdu/helpers/verificacoes/verificacoes-basicas.ts`](e2e/cdu/helpers/verificacoes/verificacoes-basicas.ts:1)

Boas práticas finais (resumo rápido)
- Teste = história do usuário; helpers = implementação.
- Priorize test-ids e constantes centralizadas.
- Evite lógica no spec; encapsule tudo na Camada 2.
- Reexporte helpers e mantenha índices atualizados.

Fim do guia.