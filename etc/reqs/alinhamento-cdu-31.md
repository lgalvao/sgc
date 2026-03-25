# Alinhamento CDU-31 - Reanálise (rodada 2)

## Artefatos analisados
- Requisito: `etc/reqs/cdu-31.md`.
- Teste E2E: `e2e/cdu-31.spec.ts` (1 cenários `test`, 0 `test.step`).

## Resultado da comparação requisito x E2E
- Itens do fluxo principal avaliados: **6**.
- Status: **3 cobertos**, **3 parciais**, **0 não cobertos** (baseado em evidências textuais no spec e helpers).

## Matriz de evidências
- ✅ **[COBERTO]** 1. ADMIN clica no botão de configurações ('engrenagem') na barra de navegação
  - Palavras-chave usadas: `admin, clica, botão, configurações, engrenagem, barra`
  - Evidência (score 2): `e2e/cdu-31.spec.ts:14` -> `test('Cenários CDU-31: ADMIN navega, valida entradas e persiste alterações de configurações', async ({_resetAutomatic...`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:7` -> `* Ator: ADMIN`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:10` -> `* - Usuário logado como ADMIN`
- 🟡 **[PARCIAL]** 2. O sistema mostra a tela Configurações com o valor atual das seguintes configurações, permitindo edição.
  - Palavras-chave usadas: `mostra, configurações, valor, atual, seguintes, permitindo`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:14` -> `test('Cenários CDU-31: ADMIN navega, valida entradas e persiste alterações de configurações', async ({_resetAutomatic...`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:30` -> `const valorInicialInativacao = Number(await campoDiasInativacao.inputValue());`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:31` -> `const valorInicialAlerta = Number(await campoDiasAlertaNovo.inputValue());`
- 🟡 **[PARCIAL]** 3. Dias para inativação de processos (referenciado neste documento como DIAS_INATIVACAO_PROCESSO): Dias depois da
  - Palavras-chave usadas: `processos, dias_inativacao_processo, dias, inativação, referenciado, neste`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:19` -> `const campoDiasInativacao = page.getByLabel(TEXTOS.configuracoes.LABEL_DIAS_INATIVACAO);`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:20` -> `const campoDiasAlertaNovo = page.getByLabel(TEXTOS.configuracoes.LABEL_DIAS_ALERTA_NOVO);`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:23` -> `await expect(campoDiasInativacao).toBeVisible();`
- ✅ **[COBERTO]** 4. Dias para indicação de alerta como novo (referenciado neste documento como DIAS_ALERTA_NOVO): Dias depois depois
  - Palavras-chave usadas: `alerta, dias_alerta_novo, dias, indicação, novo, referenciado`
  - Evidência (score 4): `e2e/cdu-31.spec.ts:20` -> `const campoDiasAlertaNovo = page.getByLabel(TEXTOS.configuracoes.LABEL_DIAS_ALERTA_NOVO);`
  - Evidência (score 3): `e2e/cdu-31.spec.ts:24` -> `await expect(campoDiasAlertaNovo).toBeVisible();`
  - Evidência (score 3): `e2e/cdu-31.spec.ts:28` -> `await expect(campoDiasAlertaNovo).toHaveValue(/\d+/);`
- ✅ **[COBERTO]** 5. ADMIN altera os valores das configurações e clica em `Salvar`.
  - Palavras-chave usadas: `admin, altera, valores, configurações, clica, salvar`
  - Evidência (score 3): `e2e/cdu-31.spec.ts:14` -> `test('Cenários CDU-31: ADMIN navega, valida entradas e persiste alterações de configurações', async ({_resetAutomatic...`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:7` -> `* Ator: ADMIN`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:10` -> `* - Usuário logado como ADMIN`
- 🟡 **[PARCIAL]** 6. O sistema mostra mensagem de confirmação e guarda as configurações internamente. O efeito das configurações deve ser
  - Palavras-chave usadas: `mostra, mensagem, confirmação, guarda, configurações, internamente`
  - Evidência (score 1): `e2e/cdu-31.spec.ts:14` -> `test('Cenários CDU-31: ADMIN navega, valida entradas e persiste alterações de configurações', async ({_resetAutomatic...`

## Ajustes recomendados para próximo ciclo
- Completar cobertura do item: **O sistema mostra a tela Configurações com o valor atual das seguintes configurações, permitindo edição.** (atualmente parcial).
- Completar cobertura do item: **Dias para inativação de processos (referenciado neste documento como DIAS_INATIVACAO_PROCESSO): Dias depois da** (atualmente parcial).
- Completar cobertura do item: **O sistema mostra mensagem de confirmação e guarda as configurações internamente. O efeito das configurações deve ser** (atualmente parcial).

## Observações metodológicas
- Esta rodada incluiu leitura de helpers importados para reduzir falso negativo de cobertura indireta.
- Classificação automática por evidência textual; recomenda-se validação humana dos itens `🟡` e `❌` antes da implementação final.
