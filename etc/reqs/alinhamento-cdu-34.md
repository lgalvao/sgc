# Alinhamento CDU-34 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-34.md`.
- Teste E2E: `e2e/cdu-34.spec.ts` (3 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **14**.
- Status: **10 cobertos**, **3 parciais**, **1 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- 🟡 **[PARCIAL]** 1. O usuário acessa o `Painel`
  - Palavras-chave usadas: `acessa, painel`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:3` -> `import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:4` -> `import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:13` -> `* 1. ADMIN acessa tela de Acompanhamento de Processos`
- ✅ **[COBERTO]** 2. O usuário entra em um processo em andamento e escolhe uma unidade participante.
  - Palavras-chave usadas: `processo, unidade, entra, andamento, escolhe, participante`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:48` -> `await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:51` -> `await navegarParaSubprocesso(page, UNIDADE_1);`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:62` -> `await expect(page.getByTestId('txt-modelo-lembrete')).toContainText(TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(UNIDAD...`
- ✅ **[COBERTO]** 3. O sistema mostra a tela `Detalhes do subprocesso` com as informações do subrocesso e unidade.
  - Palavras-chave usadas: `subprocesso, unidade, mostra, detalhes, informações, subrocesso`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:51` -> `await navegarParaSubprocesso(page, UNIDADE_1);`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:62` -> `await expect(page.getByTestId('txt-modelo-lembrete')).toContainText(TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(UNIDAD...`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:68` -> `await expect(page.getByTestId('txt-modelo-lembrete')).toContainText(TEXTOS.subprocesso.LEMBRETE_MODELO_PREFIXO(UNIDAD...`
- ✅ **[COBERTO]** 4. O usuário clica em `Enviar lembrete`.
  - Palavras-chave usadas: `clica, enviar, lembrete`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:8` -> `* CDU-34 - Enviar lembrete de prazo`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:16` -> `* 4. ADMIN aciona "Enviar lembrete"`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:21` -> `test.describe.serial('CDU-34 - Enviar lembrete de prazo', () => {`
- ✅ **[COBERTO]** 5. O sistema mostra um modal de confirmação, com título "Enviar lembrete", e texto "Confirma envio de lembrete para a unidade [SIGLA_UNIDADE]?", e botões `Cancelar` e `Enviar`.
  - Palavras-chave usadas: `unidade, sigla_unidade, mostra, modal, confirmação, título`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:17` -> `* 5. Sistema exibe modal de confirmação`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:15` -> `* 3. ADMIN seleciona unidades com pendências`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:19` -> `* 7. Sistema envia e-mail e cria alerta para a unidade`
- 🟡 **[PARCIAL]** 6. O usuário confirma.
  - Palavras-chave usadas: `confirma`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:17` -> `* 5. Sistema exibe modal de confirmação`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:18` -> `* 6. ADMIN confirma envio`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:69` -> `await page.getByTestId('btn-confirmar-enviar-lembrete').click();`
- ✅ **[COBERTO]** 7. O sistema envia e-mail para o responsável pela unidade.
  - Palavras-chave usadas: `unidade, envia, e-mail, responsável, pela`
  - Evidência (score 3): `e2e/cdu-34.spec.ts:19` -> `* 7. Sistema envia e-mail e cria alerta para a unidade`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:8` -> `* CDU-34 - Enviar lembrete de prazo`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:15` -> `* 3. ADMIN seleciona unidades com pendências`
- ✅ **[COBERTO]** 8. O sistema cria internamente um alerta para a unidade:
  - Palavras-chave usadas: `alerta, unidade, cria, internamente`
  - Evidência (score 3): `e2e/cdu-34.spec.ts:19` -> `* 7. Sistema envia e-mail e cria alerta para a unidade`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:42` -> `test('Cenario principal: ADMIN envia lembrete e sistema cria alerta sem alterar o workflow', async ({`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:78` -> `test('Cenario complementar: unidade de destino visualiza alerta de lembrete no painel', async ({`
- ✅ **[COBERTO]** 9. `Descrição`: "Lembrete: Prazo do processo [DESCRICAO_PROCESSO] encerra em [DATA_LIMITE]"
  - Palavras-chave usadas: `prazo, processo, descricao_processo, descrição, lembrete, encerra`
  - Evidência (score 4): `e2e/cdu-34.spec.ts:86` -> `await expect(tabelaAlertas).toContainText(new RegExp(`Lembrete: Prazo do processo ${descProcesso} encerra em [0-9]{2}...`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:8` -> `* CDU-34 - Enviar lembrete de prazo`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:14` -> `* 2. Sistema exibe processos com indicadores de prazo`
- 🟡 **[PARCIAL]** 10. `Processo`: [DESCRICAO_PROCESSO]
  - Palavras-chave usadas: `processo, descricao_processo`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:2` -> `import {criarProcessoFixture} from './fixtures/fixtures-processos.js';`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:3` -> `import {navegarParaSubprocesso, verificarPaginaPainel} from './helpers/helpers-navegacao.js';`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:4` -> `import {acessarDetalhesProcesso} from './helpers/helpers-processos.js';`
- ❌ **[NAO_COBERTO]** 11. `Data/hora`: Data/hora atual
  - Palavras-chave usadas: `data/hora, atual`
  - Evidência: nenhuma ocorrência relevante encontrada no código analisado.
- ✅ **[COBERTO]** 12. `Unidade de origem`: ADMIN
  - Palavras-chave usadas: `unidade, origem, admin`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:15` -> `* 3. ADMIN seleciona unidades com pendências`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:10` -> `* Ator: Sistema/ADMIN`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:13` -> `* 1. ADMIN acessa tela de Acompanhamento de Processos`
- ✅ **[COBERTO]** 13. `Unidade de destino`: [SIGLA_UNIDADE]
  - Palavras-chave usadas: `unidade, sigla_unidade, destino`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:78` -> `test('Cenario complementar: unidade de destino visualiza alerta de lembrete no painel', async ({`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:15` -> `* 3. ADMIN seleciona unidades com pendências`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:19` -> `* 7. Sistema envia e-mail e cria alerta para a unidade`
- ✅ **[COBERTO]** 14. O sistema exibe mensagem de sucesso "Lembrete enviado".
  - Palavras-chave usadas: `exibe, mensagem, sucesso, lembrete, enviado`
  - Evidência (score 3): `e2e/cdu-34.spec.ts:71` -> `await expect(page.getByText(TEXTOS.subprocesso.SUCESSO_LEMBRETE_ENVIADO).first()).toBeVisible();`
  - Evidência (score 2): `e2e/cdu-34.spec.ts:72` -> `await expect(page.getByTestId('tbl-movimentacoes')).not.toContainText(/Lembrete de prazo enviado/i);`
  - Evidência (score 1): `e2e/cdu-34.spec.ts:8` -> `* CDU-34 - Enviar lembrete de prazo`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **O usuário acessa o `Painel`** (atualmente parcial).
- Completar cobertura do item: **O usuário confirma.** (atualmente parcial).
- Completar cobertura do item: **`Processo`: [DESCRICAO_PROCESSO]** (atualmente parcial).

## Prontidão para o próximo PR de melhoria E2E
- Status de entrada: **PRONTO_COM_GAPS**.
- Motivos: há itens sem cobertura E2E.
- Checklist mínimo antes de codar:
  - [ ] confirmar massa de dados/fixtures para cenário positivo e negativo;
  - [ ] definir assert de regra de negócio + assert de efeito colateral;
  - [ ] validar perfil/unidade necessários no cenário (quando aplicável);
  - [ ] mapear se precisa teste de integração backend complementar.
- Escopo sugerido para o próximo PR deste CDU:
  - Completar cobertura do item: **O usuário acessa o `Painel`** (atualmente parcial).
  - Completar cobertura do item: **O usuário confirma.** (atualmente parcial).
  - Completar cobertura do item: **`Processo`: [DESCRICAO_PROCESSO]** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
