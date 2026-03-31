# Alinhamento CDU-07 - Reanálise (rodada 3)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-07.md`.
- Teste E2E: `e2e/cdu-07.spec.ts` (5 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **13**.
- Status: **11 cobertos**, **2 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências

- ✅ **[COBERTO]** 1. O sistema mostra a tela `Detalhes do subprocesso` com os dados do subprocesso da unidade.
  - Evidência: `e2e/cdu-07.spec.ts:67` -> `await verificarDetalhesSubprocesso(page, {sigla: UNIDADE_ALVO, nomeUnidade: NOME_UNIDADE_ALVO, ...});`
  - Evidência: `e2e/cdu-07.spec.ts:116` -> `await verificarDetalhesSubprocesso(page, {sigla: UNIDADE_ALVO, situacao: 'Não iniciado', ...});` (CHEFE)

- ✅ **[COBERTO]** 2.1.1. Seção Dados da unidade: sigla e nome da unidade, destacados.
  - Evidência: `e2e/cdu-07.spec.ts:67` -> `sigla: UNIDADE_ALVO, nomeUnidade: NOME_UNIDADE_ALVO` (verificados via verificarDetalhesSubprocesso)

- ✅ **[COBERTO]** 2.1.2. Titular: nome, ramal e e-mail (exibido quando não é o responsável).
  - Evidência: `e2e/cdu-07.spec.ts:70` -> `titular: 'Debbie Harry', ramalTitular: '2015', emailTitular: 'debbie.harry@tre-pe.jus.br'`

- 🟡 **[PARCIAL]** 2.1.3. Responsável: nome, tipo da responsabilidade (titular / substituição / atribuição temporária), ramal e e-mail.
  - O campo responsável é verificado apenas no cenário de titular como responsável. Cenários de substituição e atribuição temporária não são testados.
  - Evidência: `e2e/cdu-07.spec.ts:70` -> `titular: 'Debbie Harry'` (apenas o titular como responsável é testado)

- ✅ **[COBERTO]** 2.1.4. `Situação`: descrição da situação do subprocesso da unidade.
  - Evidência: `e2e/cdu-07.spec.ts:68` -> `situacao: 'Não iniciado'`
  - Evidência: `e2e/cdu-07.spec.ts:237` -> `situacao: 'Mapa homologado'` (processo finalizado)

- ✅ **[COBERTO]** 2.1.5. `Localização atual`: unidade destino da última movimentação do subprocesso.
  - Evidência: `e2e/cdu-07.spec.ts:69` -> `localizacao: UNIDADE_ALVO`

- 🟡 **[PARCIAL]** 2.1.6. `Prazo para conclusão (etapa atual)`: data limite da última etapa do subprocesso ainda não concluída.
  - O prazo é verificado na tabela de participantes do processo (linhaSubprocesso) mas não assertado explicitamente no header do subprocesso.
  - Evidência: `e2e/cdu-07.spec.ts` -> (verificado indiretamente via tabela de detalhes do processo, não no header do subprocesso)

- ✅ **[COBERTO]** 2.2. Seção `Movimentações do processo`: tabela com campos `Data/hora`, `Origem`, `Destino` e `Descrição`.
  - Evidência: `e2e/cdu-07.spec.ts:75` -> `await expect(page.getByRole('heading', {name: 'Movimentações'})).toBeVisible();`
  - Evidência: `e2e/cdu-07.spec.ts:76` -> `await expect(page.getByRole('columnheader', {name: 'Data/hora'})).toBeVisible();`
  - Evidência: `e2e/cdu-07.spec.ts:77` -> `await expect(page.getByRole('columnheader', {name: 'Origem'})).toBeVisible();`
  - Evidência: `e2e/cdu-07.spec.ts:78` -> `await expect(page.getByRole('columnheader', {name: 'Destino'})).toBeVisible();`
  - Evidência: `e2e/cdu-07.spec.ts:79` -> `await expect(page.getByRole('columnheader', {name: 'Descrição'})).toBeVisible();`

- ✅ **[COBERTO]** 2.2 (ordem decrescente). Movimentações apresentadas em ordem decrescente de data/hora.
  - Evidência: `e2e/cdu-07.spec.ts:18-30` -> `converterDataHoraBrParaTimestamp` + loop com `expect(datasMovimentacao[i]).toBeGreaterThanOrEqual(datasMovimentacao[i + 1])`

- ✅ **[COBERTO]** 2.3.1. Seção Elementos: cards `Atividades e conhecimentos` e `Mapa de competências` para processos de Mapeamento ou Revisão.
  - Evidência: `e2e/cdu-07.spec.ts:91` -> `await expect(page.getByTestId('card-subprocesso-atividades-vis')).toBeVisible();` (ADMIN - visualização)
  - Evidência: `e2e/cdu-07.spec.ts:119` -> `await expect(page.getByTestId('card-subprocesso-atividades')).toBeVisible();` (CHEFE - editável)
  - Evidência: `e2e/cdu-07.spec.ts:95` -> `await expect(page.getByTestId('card-subprocesso-mapa-desabilitado')).toBeVisible();`

- ✅ **[COBERTO]** 2.3.1 (habilitação por perfil e situação). Card de atividades habilitado/desabilitado conforme perfil (ADMIN, GESTOR, CHEFE, SERVIDOR) e situação do subprocesso.
  - Evidência: ADMIN (card-disabled, card-actionable após disponibilização): `e2e/cdu-07.spec.ts:92` -> `.toHaveClass(/card-disabled/)`
  - Evidência: CHEFE (card-actionable desde início): `e2e/cdu-07.spec.ts:120` -> `.toHaveClass(/card-actionable/)` + `.toHaveAttribute('role', 'button')`
  - Evidência: SERVIDOR (card-disabled antes, card-actionable após disponibilização): `e2e/cdu-07.spec.ts:131-137`
  - Evidência: GESTOR verificado em `e2e/cdu-07.spec.ts:99-105`

- ✅ **[COBERTO]** 2.3.1 (card mapa por situação e perfil). Card `Mapa de competências` habilitado após homologação (ADMIN) e disponibilização (demais perfis).
  - Evidência: `e2e/cdu-07.spec.ts:162` -> `await expect(page.getByTestId('card-subprocesso-mapa-edicao')).toBeVisible();` (ADMIN após homologação)
  - Evidência: `e2e/cdu-07.spec.ts:181` -> `await expect(page.getByTestId('card-subprocesso-mapa-visualizacao')).toHaveClass(/card-actionable/);` (GESTOR)
  - Evidência: `e2e/cdu-07.spec.ts:196` -> card-subprocesso-mapa-visualizacao para SERVIDOR após disponibilização do mapa

- ✅ **[COBERTO]** 2.3.2. Se o processo for do tipo **Diagnóstico**, a seção apresentará cards `Diagnóstico da equipe`, `Ocupações críticas` e (implicitamente) `Monitoramento`.
  - Evidência: `e2e/cdu-07.spec.ts:283` -> `await expect(page.getByTestId('card-subprocesso-diagnostico')).toBeVisible();`
  - Evidência: `e2e/cdu-07.spec.ts:284` -> `await expect(page.getByTestId('card-subprocesso-diagnostico')).toContainText('Autoavaliação');`
  - Evidência: `e2e/cdu-07.spec.ts:285` -> `await expect(page.getByTestId('card-subprocesso-ocupacoes')).toBeVisible();`
  - Evidência: `e2e/cdu-07.spec.ts:287` -> `await expect(page.getByTestId('card-subprocesso-monitoramento')).toBeVisible();`

## Cenários adicionais cobertos (além do fluxo principal)
- **Teste 3**: Servidor mantém acesso de visualização no processo finalizado (card-atividades-vis e card-mapa-visualizacao habilitados).
- **Teste 4**: Cards exibidos com rotas corretas ao navegar entre subprocessos distintos na mesma sessão (sem dados desatualizados do store).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO**.
- Gaps remanescentes:
  - Item 2.1.3 (responsável por substituição ou atribuição temporária): cenários específicos com servidor em substituição não testados; baixa frequência no mundo real.
  - Item 2.1.6 (prazo no header do subprocesso): poderia ser assertado diretamente no header do subprocesso, além da tabela de participantes.
