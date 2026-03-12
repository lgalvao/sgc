# Relatório de Análise dos Testes E2E — SGC

## Sumário Executivo

Este relatório analisa todos os 36 arquivos de testes E2E (`cdu-01.spec.ts` a `cdu-36.spec.ts`) mais o `captura.spec.ts` com foco em identificar oportunidades de torná-los mais rápidos e focados, aproveitando os endpoints de fixture disponíveis no `E2eController`.

### Principais achados:
- **12 testes** já estão otimizados com uso correto de fixtures de backend
- **~9 testes** têm setup pesado via UI que pode ser substituído por fixtures existentes
- **3 testes** precisam de **novas fixtures de backend** para eliminar grandes blocos de preparação
- **2 antipadrões** são recorrentes em vários arquivos

---

## 1. Infraestrutura de Fixtures Disponível

### 1.1 Endpoints do `E2eController` (`/e2e/...`)

| Endpoint | Descrição | Situação resultante |
|---|---|---|
| `POST /e2e/reset-database` | Reseta banco para o estado do `seed.sql` | — |
| `POST /e2e/processo/{codigo}/limpar` | Remove processo e dependentes | — |
| `POST /e2e/processo/{codigo}/limpar-completo` | Remoção robusta (desabilita FK temporariamente) | — |
| `POST /e2e/fixtures/processo-mapeamento` | Cria processo de mapeamento (opcional: já iniciado) | `CRIADO` ou `EM_ANDAMENTO` |
| `POST /e2e/fixtures/processo-revisao` | Cria processo de revisão | `CRIADO` ou `EM_ANDAMENTO` |
| `POST /e2e/fixtures/processo-finalizado-com-atividades` | Cria processo **finalizado** com atividades/mapa via SQL direto | `FINALIZADO` |
| `POST /e2e/fixtures/processo-mapeamento-com-cadastro-disponibilizado` | Mapeamento iniciado, cadastro disponibilizado | `MAPEAMENTO_CADASTRO_DISPONIBILIZADO` |
| `POST /e2e/fixtures/processo-mapeamento-com-mapa-disponibilizado` | Mapeamento iniciado, mapa disponibilizado | `MAPEAMENTO_MAPA_DISPONIBILIZADO` |
| `POST /e2e/fixtures/processo-mapeamento-com-mapa-validado` | Mapeamento iniciado, mapa validado pelo chefe | `MAPEAMENTO_MAPA_VALIDADO` |
| `POST /e2e/fixtures/processo-mapeamento-com-mapa-homologado` | Mapeamento iniciado, mapa homologado (pronto para finalizar) | `MAPEAMENTO_MAPA_HOMOLOGADO` |
| `POST /e2e/fixtures/processo-revisao-com-mapa-homologado` | Revisão iniciada, mapa homologado | `REVISAO_MAPA_HOMOLOGADO` |

### 1.2 Helpers em `fixtures/fixtures-processos.ts`

Para cada endpoint acima existe um helper TypeScript correspondente:
- `criarProcessoFixture(request, options)`
- `criarProcessoFinalizadoFixture(request, options)`
- `criarProcessoCadastroDisponibilizadoFixture(request, options)`
- `criarProcessoMapaDisponibilizadoFixture(request, options)`
- `criarProcessoMapaValidadoFixture(request, options)`
- `criarProcessoMapaHomologadoFixture(request, options)`
- `criarProcessoRevisaoMapaHomologadoFixture(request, options)`

### 1.3 Lacuna Identificada: `MAPEAMENTO_CADASTRO_HOMOLOGADO`

Há uma **lacuna crítica** na cobertura de fixtures: o estado `MAPEAMENTO_CADASTRO_HOMOLOGADO` (cadastro de atividades aprovado pelo admin, mapa ainda não criado). Este é exatamente o pré-requisito para iniciar a criação do mapa de competências, e **três testes** (cdu-15, cdu-17, cdu-24) precisam percorrer todo o workflow de mapeamento via UI por causa dessa lacuna.

---

## 2. Testes Já Otimizados ✅

Os seguintes testes usam fixtures de backend corretamente e servem de **modelo de referência**:

| Arquivo | Fixture(s) usada(s) | Abordagem |
|---|---|---|
| `cdu-08.spec.ts` | `criarProcessoFixture`, `criarProcessoFinalizadoFixture` | Setup por API, teste por UI |
| `cdu-10.spec.ts` | `criarProcessoFinalizadoFixture`, `criarProcessoFixture` | Setup por API + ação UI mínima |
| `cdu-11.spec.ts` | `criarProcessoCadastroDisponibilizadoFixture`, `criarProcessoFinalizadoFixture` | Setup por API, navega por URL direta |
| `cdu-12.spec.ts` | `criarProcessoFinalizadoFixture`, `criarProcessoFixture` | Setup por API, teste por UI |
| `cdu-13.spec.ts` | `criarProcessoCadastroDisponibilizadoFixture` | Setup parcial por API + devoluções UI (necessárias) |
| `cdu-14.spec.ts` | `criarProcessoFinalizadoFixture`, `criarProcessoFixture` | Setup por API + revisão UI (necessária) |
| `cdu-20.spec.ts` | `criarProcessoMapaValidadoFixture` | Uma linha de setup |
| `cdu-32.spec.ts` | `criarProcessoMapaHomologadoFixture` | Uma linha de setup |
| `cdu-33.spec.ts` | `criarProcessoMapaHomologadoFixture`, `criarProcessoRevisaoMapaHomologadoFixture` | Setup por API |
| `cdu-34.spec.ts` | `criarProcessoFixture` | Uma linha de setup |
| `cdu-35.spec.ts` | `criarProcessoFixture` | Uma linha de setup |
| `cdu-36.spec.ts` | `criarProcessoMapaHomologadoFixture` | Uma linha de setup |

---

## 3. Oportunidades com Fixtures Existentes

### 3.1 `cdu-21.spec.ts` — **Impacto Alto** (269 linhas, 7 testes de preparação)

**Problema:** O arquivo tem 7 testes `Preparacao` (linhas 43–205) que percorrem todo o fluxo de mapeamento (criar → iniciar → adicionar atividades → disponibilizar → aceitar ×2 → homologar cadastro → criar competências → disponibilizar mapa → validar → aceitar ×2 → homologar mapa) apenas para que o último teste (`Cenários CDU-21`) possa testar a **finalização do processo**.

**Prova:** O estado alvo é `MAPEAMENTO_MAPA_HOMOLOGADO` — exatamente o que `criarProcessoMapaHomologadoFixture` fornece.

**Código atual (setup via UI, 163 linhas):**
```typescript
// Preparacao 1: Admin cria e inicia processo de mapeamento
test('Preparacao 1: Admin cria e inicia processo de mapeamento', async ({page, autenticadoComoAdmin}) => {
    await criarProcesso(page, { descricao: descProcesso, tipo: 'MAPEAMENTO', ... });
    ...
    await page.getByTestId('btn-processo-iniciar').click();
    // + 6 outros testes de preparação
});
```

**Solução com fixture existente:**
```typescript
test('Setup Data', async ({request}) => {
    const processo = await criarProcessoMapaHomologadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_ALVO
    });
    processoId = processo.codigo;
    expect(true).toBeTruthy();
});

// + 1 teste principal CDU-21 que navega via /processo/{processoId}
```

**Ganho estimado:** Eliminar 7 testes de preparação (≈163 linhas). O arquivo passaria de 269 para ~106 linhas.

---

### 3.2 `cdu-19.spec.ts` — **Impacto Alto** (140 linhas, 4 testes de preparação)

**Problema:** Quatro testes `Preparacao` (criar → iniciar → adicionar atividades → disponibilizar → aceitar ×2 → homologar cadastro → criar competências → disponibilizar mapa) para testar a **validação do mapa pelo chefe** (CDU-19). O estado alvo é `MAPEAMENTO_MAPA_DISPONIBILIZADO`.

**Prova:** O arquivo termina com `Cenários CDU-19: Chefe valida mapa` — que precisa exatamente de mapa disponibilizado. O endpoint `/e2e/fixtures/processo-mapeamento-com-mapa-disponibilizado` já existe.

**Solução com fixture existente:**
```typescript
test('Setup Data', async ({request}) => {
    const processo = await criarProcessoMapaDisponibilizadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_ALVO
    });
    processoId = processo.codigo;
    expect(true).toBeTruthy();
});
```

**Ganho estimado:** Eliminar 4 testes de preparação (≈80 linhas).

---

### 3.3 `cdu-25.spec.ts` — **Impacto Médio** (170 linhas, 1 grande teste de setup)

**Problema:** O teste `Setup UI` (linhas 56–140) executa todo o fluxo: criar processo → adicionar atividades → disponibilizar → aceitar → homologar cadastro → criar competências → disponibilizar mapa → validar (chefe). O estado alvo é `MAPEAMENTO_MAPA_VALIDADO`.

**Prova:** O CDU-25 testa "Aceitar validação de mapas em bloco" pelo gestor, que exige `MAPEAMENTO_MAPA_VALIDADO`. Este endpoint já existe.

**Solução com fixture existente:**
```typescript
test('Setup Data', async ({request}) => {
    await criarProcessoMapaValidadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_1
    });
    expect(true).toBeTruthy();
});
```

**Ganho estimado:** ~85 linhas de setup eliminadas.

---

### 3.4 `cdu-26.spec.ts` — **Impacto Médio** (185 linhas, 1 grande teste de setup)

**Problema:** Idêntico ao `cdu-25`. O `Setup UI` percorre o mesmo fluxo até `MAPEAMENTO_MAPA_VALIDADO` para testar "Homologar validação de mapas em bloco" (CDU-26).

**Solução com fixture existente:**
```typescript
test('Setup Data', async ({request}) => {
    await criarProcessoMapaValidadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_1
    });
    expect(true).toBeTruthy();
});
```

**Ganho estimado:** ~90 linhas de setup eliminadas.

---

### 3.5 `cdu-22.spec.ts` — **Impacto Médio** (165 linhas, 1 teste de setup)

**Problema:** O primeiro teste `Cria processo, cadastra atividades e disponibiliza cadastro` (linhas 36–79) cria processo via UI, chefe adiciona atividades e disponibiliza. Estado alvo: `MAPEAMENTO_CADASTRO_DISPONIBILIZADO`.

**Solução com fixture existente:**
```typescript
test('Setup Data', async ({request}) => {
    await criarProcessoCadastroDisponibilizadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_1
    });
    expect(true).toBeTruthy();
});
```

**Ganho estimado:** ~44 linhas de setup eliminadas.

---

### 3.6 `cdu-23.spec.ts` — **Impacto Médio** (122 linhas, 1 teste de setup)

**Problema:** O `Setup UI` (linhas 44–78) repete o mesmo padrão de cdu-22: criar processo + adicionar atividades + disponibilizar. Estado alvo: `MAPEAMENTO_CADASTRO_DISPONIBILIZADO`.

**Solução com fixture existente:**
```typescript
test('Setup Data', async ({request}) => {
    await criarProcessoCadastroDisponibilizadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_1
    });
    expect(true).toBeTruthy();
});
```

**Ganho estimado:** ~35 linhas de setup eliminadas.

---

### 3.7 `cdu-27.spec.ts` — **Impacto Baixo** (83 linhas, 1 teste de setup)

**Problema:** O `Setup UI` (linhas 28–53) cria processo via UI e inicia. Estado alvo: processo `EM_ANDAMENTO`. O `criarProcessoFixture` com `iniciar: true` já faz isso.

**Solução com fixture existente:**
```typescript
test('Setup Data', async ({request}) => {
    await criarProcessoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_1,
        iniciar: true
    });
    expect(true).toBeTruthy();
});
```

**Ganho estimado:** ~26 linhas de setup eliminadas + remoção de `login` explícito.

---

## 4. Oportunidades que Requerem Novas Fixtures de Backend

### 4.1 NOVA FIXTURE NECESSÁRIA: `MAPEAMENTO_CADASTRO_HOMOLOGADO`

**Estado alvo:** Processo de mapeamento iniciado, com atividades cadastradas, e o cadastro **homologado pelo admin** — mas mapa de competências ainda não criado.

**Endpoint proposto:** `POST /e2e/fixtures/processo-mapeamento-com-cadastro-homologado`

**Testes que se beneficiam:**

#### 4.1.1 `cdu-15.spec.ts` (133 linhas, 1 longo Preparacao)

**Problema:** O único `Preparacao` (linhas 39–88) percorre: criar processo → iniciar → chefe adiciona 2 atividades → disponibilizar → 2 gestores aceitam → admin homologa. São ~50 linhas de UI para atingir `CADASTRO_HOMOLOGADO`.

**Solução:**
```typescript
test('Preparacao: Criar processo no estado correto', async ({request}) => {
    const processo = await criarProcessoCadastroHomologadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_ALVO
    });
    processoId = processo.codigo;
    // A fixture insere ATIVIDADE_1 e ATIVIDADE_2 automaticamente
    expect(true).toBeTruthy();
});
```

**Observação importante:** O teste `Cenários CDU-15` usa os nomes `ATIVIDADE_1` e `ATIVIDADE_2` para criar competências. A nova fixture deve retornar os nomes das atividades inseridas para que o teste possa referenciar as atividades corretas. Alternativamente, o fixture pode seguir o padrão já estabelecido: `Atividade Fixture - {procId}`.

**Ganho estimado:** ~50 linhas de setup, 1 teste de preparação eliminado.

---

#### 4.1.2 `cdu-17.spec.ts` (140 linhas, 3+ testes de preparação)

**Problema:** O CDU-17 testa "Disponibilizar mapa de competências". Os testes `Preparacao 1` a `Preparacao 3` (criar → iniciar → adicionar atividades → disponibilizar → 2 gestores aceitam → admin homologa cadastro) apenas preparam o estado `CADASTRO_HOMOLOGADO` para que o admin possa criar o mapa e disponibilizar.

**Solução:**
```typescript
test('Setup Data', async ({request}) => {
    const processo = await criarProcessoCadastroHomologadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_ALVO
    });
    processoId = processo.codigo;
    expect(true).toBeTruthy();
});
// Testes reais: admin cria competências (UI) e disponibiliza mapa (UI)
```

**Ganho estimado:** Eliminar 3 testes de preparação (≈75 linhas).

---

#### 4.1.3 `cdu-24.spec.ts` (102 linhas, 1 teste único muito longo)

**Problema:** O arquivo tem um único e extenso teste `Fluxo completo: De criação de processo à disponibilização em bloco` que mistura 4 fases de setup com o comportamento real a ser testado. O estado necessário antes de testar "Disponibilizar mapas em bloco" é `CADASTRO_HOMOLOGADO`.

**Solução:** Separar em dois testes — um de setup via API e um de verificação via UI:
```typescript
test('Setup Data', async ({request}) => {
    await criarProcessoCadastroHomologadoFixture(request, {
        descricao: descProcesso,
        unidade: UNIDADE_1
    });
    expect(true).toBeTruthy();
});

test('ADMIN disponibiliza mapas em bloco', async ({page, autenticadoComoAdmin}) => {
    // Apenas os passos que testam o CDU-24 de fato
    await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
    // ...
});
```

**Ganho estimado:** ~60 linhas de setup, estrutura mais clara.

---

### 4.2 NOVA FIXTURE NECESSÁRIA: `REVISAO_CADASTRO_HOMOLOGADO`

**Estado alvo:** Mapeamento anterior finalizado (para gerar mapa vigente) + revisão iniciada com cadastro **homologado pelo admin**, pronto para o admin ajustar o mapa de competências.

**Endpoint proposto:** `POST /e2e/fixtures/processo-revisao-com-cadastro-homologado`

**Nota:** Esta fixture deve criar tanto o mapeamento finalizado (mapa vigente) quanto a revisão com o cadastro em estado `REVISAO_CADASTRO_HOMOLOGADO`. Ela precisaria de parâmetros de atividades (nomes) pois o CDU-16 verifica impactos específicos.

#### 4.2.1 `cdu-16.spec.ts` — **MAIOR IMPACTO** (277 linhas, 9 testes de preparação)

**Problema:** Este é o caso mais crítico do repositório. Os testes `Preparacao 1` a `Preparacao 9` (linhas 47–201) percorrem **dois workflows completos**: primeiro um mapeamento inteiro (criar → iniciar → atividades → disponibilizar → aceitar ×2 → homologar cadastro → criar 3 competências → disponibilizar mapa → validar → aceitar ×2 → homologar mapa → finalizar), depois uma revisão (criar → iniciar → modificar atividades → disponibilizar → aceitar ×2 → homologar cadastro revisão). O único teste de conteúdo CDU-16 são os `Cenários CDU-16` (linhas 203-277, 75 linhas) que testam a edição do mapa durante a revisão.

**Detalhamento do custo atual:**
```
Preparacao 1: Admin cria e inicia processo de mapeamento         (UI)
Preparacao 2: Chefe adiciona atividades e disponibiliza cadastro (UI)
Preparacao 3: Gestores aceitam cadastro                          (UI x2)
Preparacao 4: Admin homologa cadastro, cria competências         (UI)
Preparacao 5: Chefe valida mapa                                  (UI)
Preparacao 6: Gestores aceitam mapa                              (UI x2)
Preparacao 7: Admin homologa mapa, finaliza e inicia revisão     (UI)
Preparacao 8: Chefe revisa atividades com alterações             (UI)
Preparacao 9: Gestores e Admin aceitam revisão                   (UI)
Cenários CDU-16: ADMIN ajusta mapa e visualiza impactos          [TESTE REAL]
```

**Complicação:** O teste real verifica **impactos específicos** no mapa (atividades inseridas/alteradas/removidas). A fixture precisa inserir os dados com os nomes corretos que o teste vai verificar: `atividadeBase1`, `atividadeBase2`, `atividadeBase3`, `competencia1`, `competencia2`, `competencia3`, `atividadeNovaRevisao`.

**Abordagem recomendada para a nova fixture:**
A fixture `/e2e/fixtures/processo-revisao-com-cadastro-homologado` deve:
1. Criar processo de mapeamento finalizado com atividades `A`, `B`, `C` e competências associadas (via SQL direto, como faz `processo-finalizado-com-atividades`)
2. Criar processo de revisão iniciado, com atividade nova `D` adicionada, `B` editada para `B Editada`, `C` removida — simulando as alterações que o CDU-16 precisa verificar
3. Colocar subprocesso em situação `REVISAO_CADASTRO_HOMOLOGADO`
4. Retornar os nomes das atividades e competências criadas para que o teste possa verificá-las

**Alternativa mais simples:** Aceitar nomes de atividades como parâmetros no request body da fixture.

**Ganho estimado:** Eliminar 9 testes de preparação (≈155 linhas). O arquivo passaria de 277 para ~122 linhas.

---

## 5. Casos Complexos com Análise Especial

### 5.1 `cdu-05.spec.ts` — Mapeamento completo como prerequisito para testar revisão (256 linhas)

**Problema:** Fases 1.1 a 1.7 (7 testes) e Fase 2.1 executam o workflow completo de mapeamento (criar → iniciar → adicionar atividades → disponibilizar → aceitar → homologar → criar competências → disponibilizar → validar → aceitar → homologar → finalizar) para que a Fase 2 possa testar a **criação de revisão**. A Fase 3 verifica que as atividades do mapeamento foram copiadas para a revisão.

**Por que é complexo:** A Fase 3 verifica especificamente que `Atividade Teste ${timestamp}` foi copiada para a revisão. Se usarmos `criarProcessoFinalizadoFixture`, as atividades geradas teriam nomes como `Atividade Origem A - {procId}`, exigindo ajuste nas asserções.

**Solução proposta:**
1. Substituir Fases 1.1–1.7 por:
   ```typescript
   test('Setup Data', async ({request}) => {
       const processo = await criarProcessoFinalizadoFixture(request, {
           unidade: UNIDADE_ALVO,
           descricao: descProcMapeamento
       });
       // Finalizar o processo via UI (necessário para criar mapa vigente)
       // OU: adicionar endpoint /e2e/fixtures/processo-finalizado-encerrado
   });
   ```
2. Adaptar as asserções da Fase 3 para verificar `Atividade Origem A - ${proceso.codigo}` em vez de `Atividade Teste ${timestamp}`

**Observação:** A finalização do processo é um passo separado da homologação do mapa. O endpoint `processo-mapeamento-com-mapa-homologado` cria um processo em estado `MAPEAMENTO_MAPA_HOMOLOGADO` mas o processo ainda não está `FINALIZADO`. Seria necessário finalizar via UI (1 passo) ou adicionar um endpoint `processo-finalizado` que inclui a finalização.

**Ganho potencial:** Eliminar 7 testes de preparação (≈140 linhas).

---

### 5.2 `cdu-33.spec.ts` — Setup misto (78 linhas)

**Situação atual:** Este teste já usa fixtures (`criarProcessoMapaHomologadoFixture`, `criarProcessoRevisaoMapaHomologadoFixture`), mas o `Setup UI` ainda precisa finalizar o processo de mapeamento via UI (linhas 24-35) antes de criar a revisão. Isso ocorre porque a fixture de mapeamento deixa o processo em `MAPEAMENTO_MAPA_HOMOLOGADO` mas não o finaliza.

**Oportunidade:** Adicionar endpoint `/e2e/fixtures/processo-mapeamento-finalizado` que cria o processo E o finaliza, gerando o mapa vigente na unidade. Isso tornaria o `Setup UI` do cdu-33 uma chamada de API pura.

---

## 6. Antipadrões Recorrentes

### 6.1 Testes de setup disfarçados de testes reais

Em vários arquivos, o primeiro teste (ou primeiros testes) não valida comportamento de usuário — é apenas código de preparação. Exemplos:

| Arquivo | Nome do "teste" | Conteúdo real |
|---|---|---|
| `cdu-22` | `Cria processo, cadastra atividades e disponibiliza cadastro` | Código de setup |
| `cdu-23` | `Setup UI` | Código de setup |
| `cdu-24` | *(tudo em 1 teste)* | Setup + teste misturados |
| `cdu-25` | `Setup UI` | Código de setup |
| `cdu-26` | `Setup UI` | Código de setup |
| `cdu-27` | `Setup UI` | Código de setup |
| `cdu-32` | `Setup UI` | Código de setup — **mas já usa fixture** |
| `cdu-33` | `Setup UI` | Fixture + 1 passo UI necessário |

**Efeito colateral:** Estes "testes" sempre passam (o único `expect` é `expect(true).toBeTruthy()`), inflam a contagem de testes do relatório, e quando falham, escondem que o problema é de infraestrutura e não de comportamento.

**Recomendação:** Mover todo o setup para um `test.beforeAll` com `async ({request}) => { ... }`, que é o padrão correto do Playwright para setup de série:

```typescript
test.beforeAll(async ({request}) => {
    await criarProcessoMapaValidadoFixture(request, { descricao: descProcesso, unidade: UNIDADE_1 });
});
```

---

### 6.2 Navegação por tabela em vez de URL direta

Muitos testes, mesmo após criar um processo via fixture (obtendo o `codigo`), ainda navegam pelo painel para acessar o processo:

```typescript
// Padrão atual (lento, frágil):
await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
await navegarParaSubprocesso(page, UNIDADE_ALVO);

// Padrão otimizado (rápido, robusto):
await page.goto(`/processo/${processoId}/${UNIDADE_ALVO}`);
```

**Impacto:** A navegação via tabela envolve renderização da lista completa e localização de texto. A navegação direta por URL é instantânea. Além disso, se houver múltiplos processos com nomes similares no banco, `getByText(...).first()` pode selecionar o errado.

**Arquivos com maior ocorrência:** `cdu-21`, `cdu-25`, `cdu-26`, `cdu-27`, `cdu-33`, `cdu-34`

**Nota:** Quando a listagem do painel **é** o objeto do teste (CDU-02, CDU-04), a navegação via tabela é correta e deve ser mantida.

---

### 6.3 Helpers locais duplicando helpers centrais

Alguns arquivos definem funções locais que duplicam funcionalidade dos helpers centrais:

**`cdu-21.spec.ts`** (linha 16):
```typescript
async function acessarSubprocessoChefe(page: Page, descProcesso: string, siglaUnidade: string) {
    await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
    await navegarParaSubprocesso(page, siglaUnidade);
}
```
Essa função é essencialmente igual a `acessarSubprocessoChefeDireto` de `helpers-analise.ts`.

**`cdu-25.spec.ts`** (linha 16):
```typescript
async function acessarSubprocessoChefe(page: Page, descProcesso: string) {
    await page.getByTestId('tbl-processos').getByText(descProcesso).first().click();
    await navegarParaMapa(page);
}
```

**Recomendação:** Remover funções locais e usar os helpers centrais de `helpers-analise.ts`.

---

## 7. Análise por Arquivo — Tabela Resumo

| Arquivo | Linhas | Status | Problema Principal | Solução |
|---|---|---|---|---|
| `cdu-01` | 109 | ✅ OK | — | — |
| `cdu-02` | 170 | ⚠️ Parcial | Cria processos via UI em 2 testes | `criarProcessoFixture` para setup |
| `cdu-03` | 281 | ✅ OK | Setup via UI é o próprio objeto do teste | — |
| `cdu-04` | 111 | ✅ OK | Setup via UI é o próprio objeto do teste | — |
| `cdu-05` | 256 | 🔴 Alto | 9 testes Fase 1.x como setup de mapeamento | `criarProcessoFinalizadoFixture` + ajustar asserções |
| `cdu-06` | 158 | ⚠️ Parcial | Cria processos via UI em múltiplos testes | `criarProcessoFixture(iniciar:true)` |
| `cdu-07` | 86 | ⚠️ Parcial | Cria processo via UI no início | `criarProcessoFixture(iniciar:true)` |
| `cdu-08` | 174 | ✅ Otimizado | — | Modelo de referência |
| `cdu-09` | 95 | ⚠️ Parcial | Cria processo via UI dentro do test.step | `criarProcessoFixture(iniciar:true)` |
| `cdu-10` | 179 | ✅ Otimizado | — | Modelo de referência |
| `cdu-11` | 94 | ✅ Otimizado | — | Modelo de referência |
| `cdu-12` | 87 | ✅ Otimizado | — | Modelo de referência |
| `cdu-13` | 80 | ✅ Otimizado | Setup adicional via UI necessário | — |
| `cdu-14` | 98 | ✅ Otimizado | — | Modelo de referência |
| `cdu-15` | 133 | 🔴 Alto | 1 Preparacao percorre mapeamento até CADASTRO_HOMOLOGADO | Nova fixture `processo-mapeamento-com-cadastro-homologado` |
| `cdu-16` | 277 | 🔴 Crítico | 9 Preparacao tests cobrindo 2 workflows completos | Nova fixture `processo-revisao-com-cadastro-homologado` |
| `cdu-17` | 140 | 🔴 Alto | 3+ Preparacao tests até CADASTRO_HOMOLOGADO | Nova fixture `processo-mapeamento-com-cadastro-homologado` |
| `cdu-18` | 87 | ✅ OK | Usa dados do seed | — |
| `cdu-19` | 140 | 🟡 Médio | 4 Preparacao tests até MAPA_DISPONIBILIZADO | `criarProcessoMapaDisponibilizadoFixture` (já existe) |
| `cdu-20` | 57 | ✅ Otimizado | — | Modelo de referência |
| `cdu-21` | 269 | 🔴 Alto | 7 Preparacao tests até MAPA_HOMOLOGADO | `criarProcessoMapaHomologadoFixture` (já existe) |
| `cdu-22` | 165 | 🟡 Médio | 1 Setup test até CADASTRO_DISPONIBILIZADO | `criarProcessoCadastroDisponibilizadoFixture` (já existe) |
| `cdu-23` | 122 | 🟡 Médio | 1 Setup test até CADASTRO_DISPONIBILIZADO | `criarProcessoCadastroDisponibilizadoFixture` (já existe) |
| `cdu-24` | 102 | 🟡 Médio | 1 teste monolítico mistura setup+teste | Nova fixture `processo-mapeamento-com-cadastro-homologado` + separação |
| `cdu-25` | 170 | 🟡 Médio | 1 Setup UI até MAPA_VALIDADO | `criarProcessoMapaValidadoFixture` (já existe) |
| `cdu-26` | 185 | 🟡 Médio | 1 Setup UI até MAPA_VALIDADO | `criarProcessoMapaValidadoFixture` (já existe) |
| `cdu-27` | 83 | ⚠️ Baixo | 1 Setup UI para processo EM_ANDAMENTO | `criarProcessoFixture(iniciar:true)` (já existe) |
| `cdu-28` | 56 | ✅ OK | Usa dados do seed | — |
| `cdu-29` | 63 | ✅ OK | Usa dados do seed | — |
| `cdu-30` | 35 | ✅ OK | — | — |
| `cdu-31` | 30 | ✅ OK | — | — |
| `cdu-32` | 57 | ✅ Otimizado | — | Modelo de referência |
| `cdu-33` | 78 | ✅ Bom | 1 passo UI necessário (finalizar processo) | Opcional: nova fixture `processo-finalizado` |
| `cdu-34` | 71 | ✅ Otimizado | — | Modelo de referência |
| `cdu-35` | 45 | ✅ Otimizado | — | Modelo de referência |
| `cdu-36` | 47 | ✅ Otimizado | — | Modelo de referência |

---

## 8. Plano de Ação Priorizado

### Prioridade 1 — Usar fixtures existentes (sem código novo no backend)

Estas mudanças são puramente no código TypeScript dos testes e podem ser feitas imediatamente:

1. **`cdu-21`**: Substituir 7 testes Preparacao por `criarProcessoMapaHomologadoFixture` + navegar por URL (`/processo/{id}`)
2. **`cdu-19`**: Substituir 4 testes Preparacao por `criarProcessoMapaDisponibilizadoFixture`
3. **`cdu-25`**: Substituir `Setup UI` por `criarProcessoMapaValidadoFixture`
4. **`cdu-26`**: Substituir `Setup UI` por `criarProcessoMapaValidadoFixture`
5. **`cdu-22`**: Substituir `Setup UI` por `criarProcessoCadastroDisponibilizadoFixture`
6. **`cdu-23`**: Substituir `Setup UI` por `criarProcessoCadastroDisponibilizadoFixture`
7. **`cdu-27`**: Substituir `Setup UI` por `criarProcessoFixture` com `iniciar: true`

**Impacto combinado estimado:** Eliminar ~20 testes de setup + ~400 linhas de código de preparação.

---

### Prioridade 2 — Adicionar nova fixture no backend: `CADASTRO_HOMOLOGADO`

**Passo 1:** Adicionar ao `E2eController`:
```java
@PostMapping("/fixtures/processo-mapeamento-com-cadastro-homologado")
@Transactional
@JsonView(ProcessoViews.Publica.class)
public Processo criarProcessoMapeamentoComCadastroHomologado(@RequestBody ProcessoFixtureRequest request) {
    return criarProcessoNaSituacao(request, TipoProcesso.MAPEAMENTO, "MAPEAMENTO_CADASTRO_HOMOLOGADO");
}
```
O método `criarProcessoNaSituacao` já existe e suporta essa extensão. A situação `MAPEAMENTO_CADASTRO_HOMOLOGADO` já é um valor válido do enum `SituacaoSubprocesso`.

**Passo 2:** Adicionar teste de integração em `E2eFixtureEndpointTest`:
```java
@Test
@DisplayName("Deve permitir criar processo com cadastro homologado via fixture")
void devePermitirCriarProcessoComCadastroHomologadoViaFixture() throws Exception { ... }
```

**Passo 3:** Adicionar helper TypeScript em `fixtures/fixtures-processos.ts`:
```typescript
export async function criarProcessoCadastroHomologadoFixture(
    request: APIRequestContext,
    options: ProcessoFixtureOptions
): Promise<ProcessoFixture> {
    const response = await request.post('/e2e/fixtures/processo-mapeamento-com-cadastro-homologado', {
        data: { unidadeSigla: options.unidade, iniciar: true,
                descricao: options.descricao ?? `...`, diasLimite: options.diasLimite ?? 30 }
    });
    if (!response.ok()) throw new Error(...);
    return await response.json();
}
```

**Passo 4:** Aplicar nos testes:
- `cdu-15`: Substituir o `Preparacao` (50 linhas de UI) por 1 chamada de fixture
- `cdu-17`: Substituir 3+ testes Preparacao por 1 chamada de fixture
- `cdu-24`: Separar em setup por API + teste por UI

---

### Prioridade 3 — Adicionar nova fixture: `REVISAO_CADASTRO_HOMOLOGADO`

**Motivação:** Desbloquear o `cdu-16.spec.ts`, o teste com maior overhead do repositório (9 testes de preparação).

**Complexidade:** Mais alta que a Prioridade 2, pois esta fixture precisa:
- Criar um processo de mapeamento **finalizado** com atividades/competências específicas
- Criar um processo de revisão com as alterações específicas já aplicadas e homologadas
- Retornar os nomes das atividades/competências para que o teste possa verificar impactos

**Abordagem sugerida:** A fixture deve aceitar nomes de atividades e competências no request body, ou retorná-los na resposta em um campo estendido. Considerar criar um `ProcessoRevisaoCadastroHomologadoResponse` que inclua os dados criados.

---

### Prioridade 4 — Opcional: `MAPEAMENTO_FINALIZADO`

Resolver o caso de `cdu-05` e `cdu-33` que precisam de um processo de mapeamento **finalizado** (não apenas com mapa homologado). Um endpoint `processo-mapeamento-finalizado` que chame `processoFacade.finalizarProcesso()` depois de `criarProcessoNaSituacao(..., "MAPEAMENTO_MAPA_HOMOLOGADO")` eliminaria o único passo UI restante nesses testes.

---

## 9. Observações sobre `captura.spec.ts`

O arquivo `captura.spec.ts` (1048 linhas) é o maior do repositório e tem uma natureza diferente: é responsável por capturar screenshots para documentação. Ele usa `waitForTimeout` extensivamente (permitido neste arquivo conforme `regras-e2e.md`) e não precisa ser "otimizado" para velocidade, pois sua função é deliberadamente lenta para garantir que as animações completem antes da captura.

O arquivo já usa `criarProcessoMapaHomologadoFixture` e outros helpers em alguns cenários, mas também tem fluxos UI completos para capturar cada tela em seu contexto natural — o que é intencional e correto para seu propósito.

---

## 10. Métricas de Impacto Estimado

| Prioridade | Testes afetados | Linhas eliminadas | Novas fixtures backend |
|---|---|---|---|
| P1 — Fixtures existentes | cdu-19, 21, 22, 23, 25, 26, 27 | ~400 | 0 |
| P2 — CADASTRO_HOMOLOGADO | cdu-15, 17, 24 | ~180 | 1 |
| P3 — REVISAO_CADASTRO_HOMOLOGADO | cdu-16 | ~155 | 1 |
| P4 — MAPEAMENTO_FINALIZADO | cdu-05, cdu-33 | ~140 | 1 |
| **Total** | **~14 arquivos** | **~875 linhas** | **3 novos endpoints** |

Além da redução de código, a eliminação de "testes de setup" reduz o número de cenários visíveis no relatório do Playwright de ~30 falsos positivos para apenas os testes que realmente validam comportamento.
