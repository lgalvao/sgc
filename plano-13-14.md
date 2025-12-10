# Plano de Testes E2E - CDU-13 e CDU-14

## Sumário

Este documento define o plano completo para implementação dos testes E2E (End-to-End) dos casos de uso **CDU-13 - Analisar cadastro de atividades e conhecimentos** e **CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos**.

---

## 1. Visão Geral dos Casos de Uso

### CDU-13 - Analisar cadastro de atividades e conhecimentos

**Objetivo:** Testar o fluxo de análise de cadastro de atividades durante um **processo de mapeamento**.

**Atores:** GESTOR e ADMIN

**Pré-condições:**

- Usuário logado com perfil GESTOR ou ADMIN
- Processo de mapeamento iniciado que tenha a unidade como participante
- Subprocesso com cadastro de atividades e conhecimentos já disponibilizado, com localização atual na unidade do usuário

**Fluxos principais:**

1. Navegação até a tela de atividades e conhecimentos
2. Visualização do histórico de análise
3. Devolução do cadastro para ajustes (com observações opcionais)
4. Aceite do cadastro (perfil GESTOR)
5. Homologação do cadastro (perfil ADMIN)

### CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos

**Objetivo:** Testar o fluxo de análise de revisão de cadastro de atividades durante um **processo de revisão**.

**Atores:** GESTOR e ADMIN

**Pré-condições:**

- Usuário logado com perfil GESTOR ou ADMIN
- Processo de revisão iniciado que tenha a unidade como participante
- Subprocesso com revisão do cadastro de atividades e conhecimentos já disponibilizada, com localização atual na unidade do usuário

**Fluxos principais:**

1. Navegação até a tela de atividades e conhecimentos
2. Verificação de impactos no mapa de competências
3. Visualização do histórico de análise
4. Devolução do cadastro para ajustes (com observações opcionais)
5. Aceite da revisão (perfil GESTOR)
6. Homologação da revisão (perfil ADMIN) - com verificação de impactos no mapa

---

## 2. Estrutura dos Arquivos de Teste

### Arquivos a serem criados

```text
e2e/
├── cdu-13.spec.ts    # Testes para análise de cadastro (Mapeamento)
└── cdu-14.spec.ts    # Testes para análise de revisão (Revisão)
```

### Padrão de organização

Os testes devem seguir a estrutura observada nos arquivos existentes:

- Usar `test.describe.serial()` para cenários que dependem de estado compartilhado
- Incluir testes de preparação para criar o contexto necessário
- Separar cenários por funcionalidade específica
- Usar helpers existentes para operações comuns

---

## 3. Usuários e Perfis para Testes

Conforme definido em `e2e/helpers/helpers-auth.ts`:

| Usuário | Título | Senha | Perfil | Unidade |
|---------|--------|-------|--------|---------|
| ADMIN_1_PERFIL | 191919 | senha | ADMIN - SEDOC | SEDOC |
| GESTOR_COORD | 222222 | senha | GESTOR - COORD_11 | COORD_11 |
| CHEFE_SECAO_221 | 141414 | senha | CHEFE - SECAO_221 | SECAO_221 |
| CHEFE_SECAO_211 | 101010 | senha | CHEFE - SECAO_211 | SECAO_211 |

**Escolha recomendada para os testes:**

- **ADMIN:** ADMIN_1_PERFIL (191919)
- **GESTOR:** GESTOR_COORD (222222) - coordenação que supervisiona seções
- **CHEFE (Unidade subordinada):** CHEFE_SECAO_221 (141414) - Seção subordinada à COORD_22

**Hierarquia das unidades:**

```text
SEDOC (ADMIN)
└── SECRETARIA_2
    └── COORD_22 (GESTOR)
        └── SECAO_221 (CHEFE)
```

---

## 4. Helpers e Utilitários Disponíveis

### 4.1. Autenticação (`helpers/helpers-auth.ts`)

```typescript
// Login com perfil único
await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

// Login com múltiplos perfis
await loginComPerfil(page, usuario, senha, perfilUnidade);

// Logout
await page.getByTestId('btn-logout').click();
await expect(page).toHaveURL(/\/login/);
```

### 4.2. Processos (`helpers/helpers-processos.ts`)

```typescript
// Criar processo
await criarProcesso(page, {
    descricao: 'Descrição do processo',
    tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO',
    diasLimite: 30,
    unidade: 'SECAO_221',
    expandir: ['SECRETARIA_2', 'COORD_22'],
    iniciar: true // opcional - inicia o processo automaticamente
});

// Verificar detalhes do processo
await verificarDetalhesProcesso(page, {
    descricao: 'Descrição',
    tipo: 'MAPEAMENTO',
    situacao: 'EM_ANDAMENTO'
});

// Verificar detalhes do subprocesso
await verificarDetalhesSubprocesso(page, {
    sigla: 'SECAO_221',
    situacao: 'Cadastro disponibilizado',
    prazo: '/'
});
```

### 4.3. Atividades (`helpers/helpers-atividades.ts`)

```typescript
// Navegar para tela de atividades
await navegarParaAtividades(page);

// Adicionar atividade e conhecimento
await adicionarAtividade(page, 'Descrição da atividade');
await adicionarConhecimento(page, 'Descrição da atividade', 'Conhecimento');

// Disponibilizar cadastro
await page.getByTestId('btn-cad-atividades-disponibilizar').click();
await page.getByTestId('btn-confirmar-disponibilizacao').click();

// Verificar botão de impactos (somente em revisão)
await verificarBotaoImpacto(page, true);

// Abrir modal de impactos
await abrirModalImpacto(page);
await fecharModalImpacto(page);
```

### 4.4. Limpeza e Reset (`hooks/hooks-limpeza.ts`)

```typescript
// Reset completo do banco (beforeAll)
await resetDatabase(request);

// Cleanup de processos criados
let cleanup: ReturnType<typeof useProcessoCleanup>;

test.beforeEach(() => {
    cleanup = useProcessoCleanup();
});

test.afterEach(async ({ request }) => {
    await cleanup.limpar(request);
});

// Registrar processo para cleanup
cleanup.registrar(processoId);
```

---

## 5. TestIds Importantes

### 5.1. Navegação e Cards

| Elemento | TestId |
|----------|--------|
| Card de Atividades | `card-subprocesso-atividades` |
| Voltar ao Subprocesso | `btn-cad-atividades-voltar` |
| Badge de Situação (Subprocesso) | `subprocesso-header__txt-badge-situacao` |
| Badge de Situação (Atividades) | `cad-atividades__txt-badge-situacao` |

### 5.2. Botões de Análise

| Ação | TestId | Contexto |
|------|--------|----------|
| Histórico de Análise | `btn-cad-atividades-historico` | Atividades (leitura/escrita) |
| Impactos no Mapa | `cad-atividades__btn-impactos-mapa` | Somente em Revisão |
| Devolver para Ajustes | `btn-acao-devolver` | Análise |
| Registrar Aceite (GESTOR) | `btn-acao-analisar-principal` | Análise |
| Homologar (ADMIN) | `btn-acao-analisar-principal` | Análise |

### 5.3. Modais de Análise

#### Modal de Devolução

| Elemento | TestId |
|----------|--------|
| Campo Observação | `inp-devolucao-cadastro-obs` |
| Botão Confirmar | `btn-devolucao-cadastro-confirmar` |
| Botão Cancelar | Usar `getByRole('button', {name: 'Cancelar'})` |

#### Modal de Aceite (GESTOR)

| Elemento | TestId |
|----------|--------|
| Campo Observação | `inp-aceite-cadastro-obs` |
| Botão Confirmar | `btn-aceite-cadastro-confirmar` |
| Botão Cancelar | Usar `getByRole('button', {name: 'Cancelar'})` |

#### Modal de Homologação (ADMIN)

**Para Mapeamento (CDU-13):**

| Elemento | TestId |
|----------|--------|
| Botão Confirmar | `btn-aceite-cadastro-confirmar` |
| Botão Cancelar | Usar `getByRole('button', {name: 'Cancelar'})` |

**Para Revisão (CDU-14):**

| Elemento | TestId | Contexto |
|----------|--------|----------|
| Botão Confirmar (Sem impactos) | `btn-aceite-cadastro-confirmar` | Modal pergunta sobre manutenção do mapa |
| Botão Confirmar (Com impactos) | `btn-aceite-cadastro-confirmar` | Modal pergunta sobre homologação do cadastro |

### 5.4. Histórico de Análise (Modal)

| Elemento | TestId |
|----------|--------|
| Célula Resultado (linha N) | `cell-resultado-N` |
| Célula Observação (linha N) | `cell-observacao-N` |
| Botão Fechar | Usar `getByRole('button', {name: 'Fechar'})` |

### 5.5. Modal de Impactos no Mapa

| Elemento | TestId |
|----------|--------|
| Botão Fechar | `btn-fechar-impacto` |
| Lista de Atividades Removidas | `lista-atividades-removidas` |

---

## 6. Estrutura dos Testes - CDU-13

### Arquivo: `e2e/cdu-13.spec.ts`

#### 6.1. Estrutura Geral

```typescript
test.describe.serial('CDU-13 - Analisar cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-13 ${timestamp}`;
    let processoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({ request }) => {
        await cleanup.limpar(request);
    });

    // Testes de preparação
    // Cenários de teste
});
```

#### 6.2. Testes de Preparação

##### Preparacao 1: ADMIN cria e inicia processo de mapeamento**

- Login como ADMIN
- Criar processo de mapeamento para SECAO_221
- Iniciar processo
- Registrar processoId para cleanup

##### Preparacao 2: CHEFE preenche atividades e disponibiliza

- Login como CHEFE
- Navegar para atividades
- Adicionar 2-3 atividades com conhecimentos
- Disponibilizar cadastro
- Verificar situação: "Cadastro disponibilizado"

#### 6.3. Cenários de Teste

##### Cenário 1: GESTOR visualiza histórico de análise (vazio inicialmente)

- Login como GESTOR
- Acessar processo e subprocesso da unidade subordinada (SECAO_221)
- Navegar para atividades
- Clicar em "Histórico de Análise"
- Verificar que modal abre mas não tem registros (ou mensagem apropriada)
- Fechar modal

##### Cenário 2: GESTOR devolve cadastro para ajustes COM observação

- Login como GESTOR
- Acessar atividades do subprocesso
- Clicar em "Devolver para ajustes"
- Preencher observação: "Favor incluir mais detalhes nos conhecimentos"
- Confirmar devolução
- Verificar mensagem de sucesso: "Devolução realizada"
- Verificar redirecionamento para painel

**Cenário 3: CHEFE visualiza histórico após devolução e corrige**

- Login como CHEFE
- Acessar subprocesso
- Verificar situação: "Cadastro em andamento"
- Navegar para atividades
- Clicar em "Histórico de Análise"
- Verificar registro de devolução:
  - Resultado: "Devolução"
  - Observação: "Favor incluir mais detalhes nos conhecimentos"
  - Unidade: Sigla do GESTOR
- Fechar modal
- (Opcional) Adicionar mais conhecimentos
- Disponibilizar novamente

##### Cenário 4: GESTOR cancela devolução

- Login como GESTOR
- Acessar atividades
- Clicar em "Devolver para ajustes"
- Preencher observação
- Clicar em "Cancelar"
- Verificar que permanece na mesma tela (modal fechado)

##### Cenário 5: GESTOR registra aceite SEM observação

- Login como GESTOR
- Acessar atividades
- Clicar em "Registrar aceite"
- NÃO preencher observação
- Confirmar aceite
- Verificar mensagem: "Aceite registrado"
- Verificar redirecionamento para painel

##### Cenário 6: GESTOR registra aceite COM observação

- Requer nova devolução e disponibilização
- Login como GESTOR
- Acessar atividades
- Clicar em "Registrar aceite"
- Preencher observação: "Cadastro aprovado conforme análise"
- Confirmar aceite
- Verificar mensagem: "Aceite registrado"

##### Cenário 7: ADMIN visualiza histórico com múltiplas análises

- Login como ADMIN
- Acessar atividades da unidade
- Clicar em "Histórico de Análise"
- Verificar múltiplos registros:
  - Devolução (GESTOR)
  - Aceite (GESTOR)
- Fechar modal

##### Cenário 8: ADMIN cancela homologação**

- Login como ADMIN
- Acessar atividades
- Clicar em "Homologar"
- Verificar modal com título "Homologação do cadastro de atividades e conhecimentos"
- Verificar pergunta: "Confirma a homologação do cadastro de atividades e conhecimentos?"
- Clicar em "Cancelar"
- Verificar que permanece na mesma tela

##### Cenário 9: ADMIN homologa cadastro

- Login como ADMIN
- Acessar atividades
- Clicar em "Homologar"
- Confirmar homologação
- Verificar mensagem: "Homologação efetivada"
- Verificar redirecionamento para tela de detalhes do subprocesso
- Verificar situação do subprocesso: "Cadastro homologado"

---

## 7. Estrutura dos Testes - CDU-14

### Arquivo: `e2e/cdu-14.spec.ts`

#### 7.1. Estrutura Geral

Similar ao CDU-13, mas com processo de REVISÃO.

```typescript
test.describe.serial('CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'SECAO_221';
    const USUARIO_CHEFE = USUARIOS.CHEFE_SECAO_221.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_SECAO_221.senha;
    const USUARIO_GESTOR = USUARIOS.GESTOR_COORD.titulo;
    const SENHA_GESTOR = USUARIOS.GESTOR_COORD.senha;
    const USUARIO_ADMIN = USUARIOS.ADMIN_1_PERFIL.titulo;
    const SENHA_ADMIN = USUARIOS.ADMIN_1_PERFIL.senha;

    const timestamp = Date.now();
    const descProcessoMapeamento = `Mapeamento CDU-14 ${timestamp}`;
    const descProcessoRevisao = `Revisão CDU-14 ${timestamp}`;
    let processoMapeamentoId: number;
    let processoRevisaoId: number;
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
        cleanup = useProcessoCleanup();
    });

    test.afterAll(async ({ request }) => {
        await cleanup.limpar(request);
    });

    // Testes de preparação (mais complexos)
    // Cenários de teste
});
```

#### 7.2. Testes de Preparação (Mais extensos que CDU-13)

##### Preparacao 1: Criar e finalizar processo de mapeamento

Este passo é necessário para ter um mapa vigente antes da revisão. Pode seguir o padrão do CDU-12:

1. ADMIN cria processo de mapeamento
2. CHEFE preenche atividades e disponibiliza
3. ADMIN homologa cadastro de atividades
4. ADMIN cria e disponibiliza mapa de competências
5. CHEFE valida mapa
6. ADMIN homologa mapa
7. ADMIN finaliza processo de mapeamento

##### Preparacao 2: ADMIN cria e inicia processo de revisão

- Login como ADMIN
- Criar processo de REVISAO para SECAO_221
- Iniciar processo
- Registrar processoId para cleanup

##### Preparacao 3: CHEFE revisa atividades e disponibiliza

- Login como CHEFE
- Navegar para atividades (deve carregar atividades do mapeamento)
- Adicionar uma nova atividade (gera impacto)
- Editar uma atividade existente (gera impacto)
- Remover uma atividade vinculada a competência (gera impacto)
- Disponibilizar revisão
- Verificar situação: "Revisão do cadastro disponibilizada"

#### 7.3. Cenários de Teste

##### Cenário 1: GESTOR visualiza impactos no mapa

- Login como GESTOR
- Acessar atividades do subprocesso
- Clicar em "Impactos no mapa"
- Verificar modal aberto
- Verificar seções:
  - "Atividades inseridas" (com a nova atividade)
  - "Competências impactadas" (com competências das atividades alteradas/removidas)
  - "Atividades removidas" (se houver)
- Fechar modal

##### Cenário 2: GESTOR visualiza histórico de análise (vazio inicialmente)

- Login como GESTOR
- Acessar atividades
- Clicar em "Histórico de Análise"
- Verificar que modal abre (sem registros ou mensagem apropriada)
- Fechar modal

##### Cenário 3: GESTOR devolve revisão para ajustes COM observação

- Login como GESTOR
- Acessar atividades
- Clicar em "Devolver para ajustes"
- Preencher observação: "Revisar impactos na Competência X"
- Confirmar devolução
- Verificar mensagem: "Devolução realizada"
- Verificar redirecionamento para painel

##### Cenário 4: CHEFE visualiza histórico após devolução e disponibiliza novamente

- Login como CHEFE
- Acessar subprocesso
- Verificar situação: "Revisão do cadastro em andamento"
- Navegar para atividades
- Clicar em "Histórico de Análise"
- Verificar registro de devolução
- Fechar modal
- Disponibilizar novamente

**Cenário 5: GESTOR cancela aceite**

- Login como GESTOR
- Acessar atividades
- Clicar em "Registrar aceite"
- Preencher observação
- Clicar em "Cancelar"
- Verificar que permanece na mesma tela

**Cenário 6: GESTOR registra aceite da revisão**

- Login como GESTOR
- Acessar atividades
- Clicar em "Registrar aceite"
- Verificar modal com pergunta: "Confirma o aceite da revisão do cadastro de atividades?"
- Preencher observação (opcional): "Revisão aprovada"
- Confirmar aceite
- Verificar mensagem: "Aceite registrado"
- Verificar redirecionamento para painel

**Cenário 7: ADMIN visualiza histórico com múltiplas análises**

- Login como ADMIN
- Acessar atividades
- Clicar em "Histórico de Análise"
- Verificar múltiplos registros
- Fechar modal

**Cenário 8: ADMIN visualiza impactos antes de homologar**

- Login como ADMIN
- Acessar atividades
- Clicar em "Impactos no mapa"
- Verificar impactos (atividades inseridas, competências impactadas, etc.)
- Fechar modal

**Cenário 9: ADMIN cancela homologação**

- Login como ADMIN
- Acessar atividades
- Clicar em "Homologar"
- Verificar modal apropriado (depende de impactos)
- Clicar em "Cancelar"
- Verificar que permanece na mesma tela

**Cenário 10: ADMIN homologa revisão SEM impactos no mapa**

Este cenário requer preparação especial onde a revisão não gera impactos:

- Preparar processo de revisão onde CHEFE NÃO altera nada que impacte o mapa
  - Exemplo: adicionar atividade SEM competência vinculada
  - Ou editar descrição de conhecimento (não altera atividades vinculadas a competências)
- Login como ADMIN
- Acessar atividades
- Clicar em "Homologar"
- Verificar modal com título "Homologação do mapa de competências"
- Verificar mensagem: "A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade. Confirma a manutenção do mapa de competências vigente?"
- Confirmar
- Verificar situação do subprocesso: "Mapa homologado"
- Verificar mensagem: "Homologação efetivada"
- Verificar redirecionamento para tela de detalhes do subprocesso

**Cenário 11: ADMIN homologa revisão COM impactos no mapa**

- Login como ADMIN
- Acessar atividades (com impactos detectados)
- Clicar em "Homologar"
- Verificar modal com título "Homologação do cadastro de atividades e conhecimentos"
- Verificar pergunta: "Confirma a homologação do cadastro de atividades e conhecimentos?"
- Confirmar
- Verificar situação do subprocesso: "Revisão do cadastro homologada"
- Verificar mensagem: "Homologação efetivada"
- Verificar redirecionamento para tela de detalhes do subprocesso

---

## 8. Funções Auxiliares a Criar

### 8.1. Funções de Navegação

```typescript
// Função para fazer logout
async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

// Função para verificar página do painel
async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

// Função para verificar página do subprocesso
async function verificarPaginaSubprocesso(page: Page, unidade: string) {
    await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${unidade}$`));
}

// Função para acessar subprocesso como GESTOR (via lista de unidades)
async function acessarSubprocessoGestor(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await page.getByText(descricaoProcesso).click();
    
    // GESTOR sempre vê lista de unidades participantes
    await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();
}

// Função para acessar subprocesso como CHEFE (vai direto)
async function acessarSubprocessoChefe(page: Page, descricaoProcesso: string) {
    await page.getByText(descricaoProcesso).click();
    // Se cair na lista de unidades (caso multiplas), clica na unidade
    if (await page.getByRole('heading', {name: /Unidades participantes/i}).isVisible()) {
        await page.getByRole('row', {name: /Seção 221/i}).click();
    }
}

// Função para acessar subprocesso como ADMIN (via lista de unidades)
async function acessarSubprocessoAdmin(page: Page, descricaoProcesso: string, siglaUnidade: string) {
    await page.getByText(descricaoProcesso).click();
    
    // ADMIN sempre vê lista de unidades participantes
    await expect(page.getByRole('heading', {name: /Unidades participantes/i})).toBeVisible();
    await page.getByRole('row', {name: new RegExp(siglaUnidade, 'i')}).click();
}
```

### 8.2. Funções de Análise

```typescript
// Função para abrir modal de histórico de análise
async function abrirHistoricoAnalise(page: Page) {
    await page.getByTestId('btn-cad-atividades-historico').click();
    const modal = page.locator('.modal-content').filter({ hasText: 'Histórico de Análise' });
    await expect(modal).toBeVisible();
    return modal;
}

// Função para fechar modal de histórico
async function fecharHistoricoAnalise(page: Page) {
    await page.getByRole('button', {name: 'Fechar'}).click();
    await expect(page.locator('.modal-content').filter({ hasText: 'Histórico de Análise' })).toBeHidden();
}

// Função para devolver cadastro
async function devolverCadastro(page: Page, observacao?: string) {
    await page.getByTestId('btn-acao-devolver').click();
    
    if (observacao) {
        await page.getByTestId('inp-devolucao-cadastro-obs').fill(observacao);
    }
    
    await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
    await expect(page.getByText(/Devolução realizada/i)).toBeVisible();
    await verificarPaginaPainel(page);
}

// Função para aceitar cadastro (GESTOR)
async function aceitarCadastro(page: Page, observacao?: string) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    if (observacao) {
        await page.getByTestId('inp-aceite-cadastro-obs').fill(observacao);
    }
    
    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Aceite registrado/i)).toBeVisible();
    await verificarPaginaPainel(page);
}

// Função para homologar cadastro (ADMIN) - Mapeamento
async function homologarCadastroMapeamento(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    // Modal: "Homologação do cadastro de atividades e conhecimentos"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();
    
    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();
    
    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}

// Função para homologar revisão (ADMIN) - COM impactos
async function homologarRevisaoComImpactos(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    // Modal: "Homologação do cadastro de atividades e conhecimentos"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a homologação do cadastro de atividades e conhecimentos/i)).toBeVisible();
    
    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();
    
    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}

// Função para homologar revisão (ADMIN) - SEM impactos
async function homologarRevisaoSemImpactos(page: Page) {
    await page.getByTestId('btn-acao-analisar-principal').click();
    
    // Modal: "Homologação do mapa de competências"
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/A revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade/i)).toBeVisible();
    await expect(page.getByText(/Confirma a manutenção do mapa de competências vigente/i)).toBeVisible();
    
    await page.getByTestId('btn-aceite-cadastro-confirmar').click();
    await expect(page.getByText(/Homologação efetivada/i)).toBeVisible();
    
    // Verifica redirecionamento para tela de detalhes do subprocesso
    await expect(page).toHaveURL(/\/processo\/\d+\/\w+$/);
}
```

---

## 9. Verificações Importantes

### 9.1. Mensagens de Sucesso

| Ação | Mensagem Esperada |
|------|-------------------|
| Devolução | "Devolução realizada" |
| Aceite | "Aceite registrado" |
| Homologação | "Homologação efetivada" |
| Disponibilização | "Cadastro de atividades disponibilizado" |

### 9.2. Situações do Subprocesso

**CDU-13 (Mapeamento):**

- Após devolução para a própria unidade: "Cadastro em andamento"
- Após disponibilização: "Cadastro disponibilizado"
- Após homologação: "Cadastro homologado"

**CDU-14 (Revisão):**

- Após devolução para a própria unidade: "Revisão do cadastro em andamento"
- Após disponibilização: "Revisão do cadastro disponibilizada"
- Após homologação (com impactos): "Revisão do cadastro homologada"
- Após homologação (sem impactos): "Mapa homologado"

### 9.3. Redirecionamentos

| Ação | Destino |
|------|---------|
| Devolução | Painel (`/painel`) |
| Aceite | Painel (`/painel`) |
| Homologação | Tela de detalhes do subprocesso (`/processo/{id}/{sigla}`) |

---

## 10. Observações e Cuidados

### 10.1. Diferenças entre CDU-13 e CDU-14

| Aspecto | CDU-13 (Mapeamento) | CDU-14 (Revisão) |
|---------|---------------------|------------------|
| Tipo de Processo | MAPEAMENTO | REVISAO |
| Botão "Impactos no mapa" | NÃO existe | Existe e é obrigatório testar |
| Preparação | Simples (criar processo e preencher) | Complexa (finalizar mapeamento primeiro) |
| Homologação ADMIN (sem impactos) | N/A | Modal diferente ("manutenção do mapa vigente") |
| Homologação ADMIN (com impactos) | Modal padrão | Modal padrão |
| Situação após homologação | "Cadastro homologado" | "Revisão do cadastro homologada" OU "Mapa homologado" |

### 10.2. Sincronização e Timeouts

- Use `await expect(...).toBeVisible()` antes de interagir com elementos
- Use `await page.waitForURL(...)` para garantir navegação completa
- Para modais, verifique visibilidade antes de interagir
- Aguarde respostas de API importantes com `page.waitForResponse()`

### 10.3. Limpeza e Isolamento

- Sempre use `resetDatabase` no `beforeAll`
- Sempre registre processos criados para cleanup
- Use `test.describe.serial()` quando cenários dependem de estado compartilhado
- Considere usar timestamps únicos nas descrições para evitar conflitos

### 10.4. Navegação entre Perfis

**ADMIN e GESTOR:**

- Sempre veem lista de unidades participantes após clicar no processo
- Devem clicar na linha da unidade desejada para acessar o subprocesso

**CHEFE:**

- Se o processo tem apenas a unidade dele, vai direto para o subprocesso
- Se o processo tem múltiplas unidades (raro), vê lista e deve clicar

---

## 11. Exemplo de Estrutura Completa de um Cenário

```typescript
test('Cenario 2: GESTOR devolve cadastro para ajustes COM observação', async ({page}) => {
    // 1. Login
    await page.goto('/login');
    await login(page, USUARIO_GESTOR, SENHA_GESTOR);
    
    // 2. Navegar para atividades do subprocesso
    await acessarSubprocessoGestor(page, descProcesso, 'Seção 221');
    await navegarParaAtividades(page);
    
    // 3. Iniciar devolução
    await page.getByTestId('btn-acao-devolver').click();
    
    // 4. Verificar modal de devolução
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByText(/Confirma a devolução do cadastro para ajustes/i)).toBeVisible();
    
    // 5. Preencher observação
    const observacao = 'Favor incluir mais detalhes nos conhecimentos';
    await page.getByTestId('inp-devolucao-cadastro-obs').fill(observacao);
    
    // 6. Confirmar
    await page.getByTestId('btn-devolucao-cadastro-confirmar').click();
    
    // 7. Verificar mensagem de sucesso
    await expect(page.getByText(/Devolução realizada/i)).toBeVisible();
    
    // 8. Verificar redirecionamento para painel
    await verificarPaginaPainel(page);
});
```

---

## 12. Checklist de Implementação

### CDU-13 (cdu-13.spec.ts)

- [ ] Estrutura básica do arquivo com describe.serial
- [ ] Declaração de variáveis (usuários, timestamps, IDs)
- [ ] Hooks (beforeAll, afterAll)
- [ ] **Preparação:**
  - [ ] ADMIN cria e inicia processo de mapeamento
  - [ ] CHEFE preenche atividades e disponibiliza
- [ ] **Cenário 1:** GESTOR visualiza histórico vazio
- [ ] **Cenário 2:** GESTOR devolve COM observação
- [ ] **Cenário 3:** CHEFE visualiza histórico após devolução
- [ ] **Cenário 4:** GESTOR cancela devolução
- [ ] **Cenário 5:** GESTOR registra aceite SEM observação
- [ ] **Cenário 6:** GESTOR registra aceite COM observação (requer nova disponibilização)
- [ ] **Cenário 7:** ADMIN visualiza histórico com múltiplas análises
- [ ] **Cenário 8:** ADMIN cancela homologação
- [ ] **Cenário 9:** ADMIN homologa cadastro

### CDU-14 (cdu-14.spec.ts)

- [ ] Estrutura básica do arquivo com describe.serial
- [ ] Declaração de variáveis (usuários, timestamps, IDs)
- [ ] Hooks (beforeAll, afterAll)
- [ ] **Preparação (Mapeamento completo):**
  - [ ] ADMIN cria e inicia processo de mapeamento
  - [ ] CHEFE preenche atividades e disponibiliza
  - [ ] ADMIN homologa cadastro
  - [ ] ADMIN cria e disponibiliza mapa de competências
  - [ ] CHEFE valida mapa
  - [ ] ADMIN homologa mapa
  - [ ] ADMIN finaliza processo de mapeamento
- [ ] **Preparação (Revisão):**
  - [ ] ADMIN cria e inicia processo de revisão
  - [ ] CHEFE revisa atividades (inserir, editar, remover) e disponibiliza
- [ ] **Cenário 1:** GESTOR visualiza impactos no mapa
- [ ] **Cenário 2:** GESTOR visualiza histórico vazio
- [ ] **Cenário 3:** GESTOR devolve COM observação
- [ ] **Cenário 4:** CHEFE visualiza histórico e disponibiliza novamente
- [ ] **Cenário 5:** GESTOR cancela aceite
- [ ] **Cenário 6:** GESTOR registra aceite da revisão
- [ ] **Cenário 7:** ADMIN visualiza histórico
- [ ] **Cenário 8:** ADMIN visualiza impactos antes de homologar
- [ ] **Cenário 9:** ADMIN cancela homologação
- [ ] **Cenário 10:** ADMIN homologa revisão SEM impactos (preparação especial)
- [ ] **Cenário 11:** ADMIN homologa revisão COM impactos

### Helpers Adicionais (Opcional)

- [ ] Criar arquivo `helpers/helpers-analise.ts` com funções auxiliares:
  - [ ] `acessarSubprocessoGestor()`
  - [ ] `acessarSubprocessoAdmin()`
  - [ ] `abrirHistoricoAnalise()`
  - [ ] `fecharHistoricoAnalise()`
  - [ ] `devolverCadastro()`
  - [ ] `aceitarCadastro()`
  - [ ] `homologarCadastroMapeamento()`
  - [ ] `homologarRevisaoComImpactos()`
  - [ ] `homologarRevisaoSemImpactos()`

---

## 13. Referências

### Especificações

- `/home/runner/work/sgc/sgc/reqs/cdu-13.md`
- `/home/runner/work/sgc/sgc/reqs/cdu-14.md`

### Testes de Referência

- `/home/runner/work/sgc/sgc/e2e/cdu-09.spec.ts` - Disponibilização e devolução em Mapeamento
- `/home/runner/work/sgc/sgc/e2e/cdu-10.spec.ts` - Disponibilização em Revisão
- `/home/runner/work/sgc/sgc/e2e/cdu-12.spec.ts` - Impactos no mapa e preparação completa

### Helpers

- `/home/runner/work/sgc/sgc/e2e/helpers/helpers-auth.ts`
- `/home/runner/work/sgc/sgc/e2e/helpers/helpers-processos.ts`
- `/home/runner/work/sgc/sgc/e2e/helpers/helpers-atividades.ts`

### Hooks

- `/home/runner/work/sgc/sgc/e2e/hooks/hooks-limpeza.ts`

---

## 14. Notas Finais

Este plano fornece uma base completa para implementação dos testes E2E dos casos de uso CDU-13 e CDU-14. Os testes devem:

1. **Seguir os padrões existentes** no repositório
2. **Cobrir todos os fluxos principais** especificados nos requisitos
3. **Validar mensagens, redirecionamentos e mudanças de estado**
4. **Usar helpers existentes** sempre que possível
5. **Criar helpers adicionais** quando necessário para reutilização
6. **Manter isolamento** entre testes através de reset e cleanup
7. **Ser legíveis e bem documentados** com comentários quando necessário

A implementação deve ser incremental, começando pelos testes de preparação e avançando para cenários mais complexos. Cada cenário deve ser testado isoladamente antes de prosseguir para o próximo.
