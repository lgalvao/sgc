# Relatório de Mensagens e Strings do SGC

> **Objetivo:** Analisar o estado atual das mensagens/strings espalhadas pelo sistema, propor estratégia de
> centralização e sugerir tecnologias para manter frontend, backend e testes sincronizados.

---

## 1. Diagnóstico Atual

### 1.1 Estado Geral

O SGC possui **centenas de strings** espalhadas por três camadas distintas, todas **hardcoded** no código-fonte, sem
qualquer sistema de centralização ou internacionalização. O sistema opera exclusivamente em **Português Brasileiro
(pt-BR)**, o que simplifica a estratégia de centralização — não é necessário multi-idioma, apenas **organização e
rastreabilidade**.

### 1.2 Distribuição de Mensagens por Camada

| Camada | Arquivos com Strings | Tipo Principal | Estimativa de Ocorrências |
|---|---|---|---|
| **Backend — DTOs** | 24 arquivos | Anotações de validação (`@NotBlank`, `@Size`, etc.) | ~60 mensagens |
| **Backend — Services** | 8 arquivos | Exceções de negócio (`ErroValidacao`, etc.) | ~45 mensagens |
| **Backend — Exception Handler** | 1 arquivo | Mensagens de erro de sistema | ~10 mensagens |
| **Frontend — Views/Components** | 20+ arquivos `.vue` | Toast de sucesso, rótulos, botões | ~30 mensagens |
| **Frontend — Constants** | `situacoes.ts` | Labels de status/situação | ~40 labels |
| **Frontend — Stores/Composables** | 5+ arquivos `.ts` | Mensagens de erro normalizadas | ~10 mensagens |
| **Testes E2E** | 36 arquivos `.spec.ts` | Asserções de texto (`getByText`, `toContain`) | ~100+ referências |
| **Testes Unitários** | 30+ arquivos `.spec.ts` | Expectativas de mensagem | ~50 referências |
| **Testes Backend** | 20+ arquivos `*Test.java` | Verificações de mensagem de erro | ~40 referências |

### 1.3 Inventário de Mensagens por Categoria

#### 1.3.1 Mensagens de Validação de DTOs (Backend)

Localizadas em anotações Jakarta Validation nos arquivos `*Request.java` e `*Dto.java`:

```
# Campos obrigatórios (36 ocorrências)
"A chave não pode estar vazia"
"A descrição da competência é obrigatória"
"A justificativa é obrigatória"
"A senha é obrigatória."
"A sigla da unidade é obrigatória"
"Código da atividade é obrigatório"
"Código do mapa é obrigatório"
"Descrição não pode ser vazia"
"O campo texto é obrigatório"
"O código da unidade é obrigatório" / "O código da unidade é obrigatório."  ← duplicata com pontuação diferente
"O código do parâmetro é obrigatório"
"O código do processo é obrigatório"
"O código do subprocesso de origem é obrigatório"
"O tipo do processo é obrigatório"
"O título eleitoral é obrigatório."
"Pelo menos uma unidade participante deve ser incluída."
"Sigla é obrigatória"
"Tipo do processo é obrigatório"                                            ← duplicata de "O tipo do processo é obrigatório"

# Limites de tamanho (14 ocorrências)
"A chave deve ter no máximo 50 caracteres"
"A descrição deve ter no máximo 255 caracteres"
"A justificativa deve ter no máximo 500 caracteres"
"A senha deve ter no máximo 64 caracteres."
"A sigla deve ter no máximo 20 caracteres"
"As observações devem ter no máximo 1000 caracteres"
"Motivo deve ter no máximo 200 caracteres"
"O perfil deve ter no máximo 50 caracteres."
"O título eleitoral deve ter no máximo 12 caracteres."
"Observações devem ter no máximo 500 caracteres"                            ← duplicata com limite diferente (500 vs 1000)
"Sigla da unidade deve ter no máximo 20 caracteres"                        ← duplicata de "A sigla deve ter no máximo 20 caracteres"

# Datas e regras
"A data limite deve ser futura"
"A data limite da etapa 1 é obrigatória"
"A data limite para validação deve ser uma data futura."
"A data é obrigatória"
"O título eleitoral deve conter apenas números."

# Coleções
"A competência deve ter pelo menos uma atividade associada"
"A lista de competências não pode ser vazia"
"Pelo menos um subprocesso deve ser selecionado"
```

#### 1.3.2 Exceções de Negócio (Backend Services)

Localizadas em `throw new ErroValidacao(...)` nos services:

```
# Administração de usuários
"Usuário já é um administrador do sistema"
"Não é permitido remover o único administrador do sistema"
"Não é permitido remover a si mesmo como administrador"

# Regras de processo
"Apenas processos na situação 'CRIADO' podem ser editados."
"Apenas processos na situação 'CRIADO' podem ser removidos."
"Apenas processos na situação 'CRIADO' podem ser iniciados."
"A data de término deve ser posterior à data de início."
"Situação inválida."
"Processo deve estar finalizado."
"Transição de situação inválida: %s -> %s"
"Situação do subprocesso não permite esta operação. Situação atual: %s. Situações permitidas: %s"

# Regras de unidades participantes
"A lista de unidades é obrigatória para iniciar o processo de revisão."
"Não há unidades participantes definidas."
"Selecione ao menos uma unidade."
"Unidade não participa deste processo."
"Subprocessos não homologados."
"Unidades INTERMEDIARIA inválidas: [lista]"
"Unidades sem mapa vigente: [lista]"
"Algumas unidades selecionadas não possuem subprocessos vinculados neste processo: [lista]"

# Regras de mapa de competências
"Subprocesso não possui mapa associado."
"O mapa de competências deve ter ao menos uma atividade cadastrada."
"Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes."
"Todas as competências devem estar associadas a pelo menos uma atividade."
"Existem competências que não foram associadas a nenhuma atividade."
"Existem atividades que não foram associadas a nenhuma competência."
"Existem atividades sem conhecimentos associados."

# Importação
"Uma ou mais atividades selecionadas já existentes no cadastro não puderam ser importadas."
"A importação de atividades só permite subprocessos de processos finalizados."

# Unicidade
"Já existe uma atividade com esta descrição neste mapa."
"Já existe um conhecimento com esta descrição nesta atividade."
```

#### 1.3.3 Mensagens do Exception Handler (Backend)

Localizadas em `RestExceptionHandler.java`:

```
"A requisição contém dados de entrada inválidos."
"A requisição contém dados inválidos."
"Acesso negado"
"Domínio não encontrado"
"Erro de configuração"
"ARGUMENTO INVÁLIDO: [detalhe]"
"ERRO INESPERADO: [detalhe]"
"ESTADO ILEGAL: [detalhe]"
```

#### 1.3.4 Template de Entidade Não Encontrada (Backend)

Localizado em `ComumRepo.java` / `ErroEntidadeNaoEncontrada.java`:

```
"'%s' com codigo '%s' não encontrado(a)."
```

#### 1.3.5 Mensagens de Sucesso Toast (Frontend)

Localizadas em calls `toastStore.setPending(...)` nos arquivos `.vue`:

```
"Aceite registrado"
"Cadastros aceitos em bloco"
"Disponibilização do mapa de competências efetuada"
"Devolução realizada"
"Homologação efetivada"
"Mapas de competências homologados em bloco"
"Mapa submetido com sugestões para análise da unidade superior"
"Mapa validado e submetido para análise à unidade superior"
"Processo alterado."
"Processo criado."
"Processo finalizado"
"Processo iniciado"
`Processo ${descRemovida} removido`  ← template dinâmico
```

#### 1.3.6 Labels de Status e Situação (Frontend)

Centralizadas em `frontend/src/constants/situacoes.ts` — **este arquivo já é um exemplo do padrão desejado**:

```
SITUACOES_SUBPROCESSO: 25 labels de situação de subprocesso
SITUACOES_MAPA: 4 labels de situação de mapa
LABELS_SITUACAO: mapeamento situação → label de exibição
CLASSES_BADGE_SITUACAO: mapeamento situação → classe CSS Bootstrap
```

---

## 2. Problemas Identificados

### 2.1 Inconsistências de Mensagens

| Problema | Exemplos | Risco |
|---|---|---|
| **Duplicatas com variações** | `"O código da unidade é obrigatório"` vs `"O código da unidade é obrigatório."` (com ponto) | Exibe mensagens diferentes para o mesmo erro dependendo do contexto |
| **Mensagens equivalentes diferentes** | `"Tipo do processo é obrigatório"` vs `"O tipo do processo é obrigatório"` | Inconsistência de UX |
| **Limites duplicados inconsistentes** | `"Observações devem ter no máximo 500 caracteres"` vs `"As observações devem ter no máximo 1000 caracteres"` — além de serem strings distintas, possuem **limites diferentes** (500 vs 1000), o que sugere que se referem a campos diferentes (ex: observação de subprocesso vs observação de homologação). Devem ser constantes distintas com nomes claros, não unificadas. | Usuário pode receber mensagens conflitantes se o limite não corresponder ao campo exibido |
| **Sigla duplicada** | `"A sigla deve ter no máximo 20 caracteres"` vs `"Sigla da unidade deve ter no máximo 20 caracteres"` | Duplicata sem motivo |

### 2.2 Sincronização Frontend–Backend Impossível de Verificar

Os testes E2E e unitários duplicam strings literais do código de produção. Qualquer mudança em uma mensagem no backend
exige busca manual em todos os arquivos de teste para atualizar as referências. Não há como saber se uma string de teste
ainda está sincronizada com o código de produção sem busca textual.

### 2.3 Ausência de Tipagem nas Strings de Mensagem

No frontend, strings de erro recebidas do backend são comparadas diretamente em templates e testes:
```typescript
// Em testes:
expect(page).toContain("Usuário já é um administrador do sistema")
// No componente:
if (error.message === "Usuário já é um administrador do sistema") ...
```
Se a mensagem mudar no backend, o TypeScript não detecta o problema em compile-time.

### 2.4 Arquivo `textos.ts` Vazio

O arquivo `frontend/src/constants/textos.ts` foi criado como placeholder, mas nunca foi preenchido. Isso indica que
houve intenção de centralizar, mas não foi executada.

---

## 3. Estratégia de Centralização Recomendada

Como o sistema é **exclusivamente pt-BR** (sem planos de internacionalização), a estratégia recomendada é a
**centralização por constantes tipadas**, sem a complexidade de um sistema i18n completo.

### 3.1 Arquitetura Proposta

```
┌─────────────────────────────────────────────────────────────────┐
│                      MENSAGENS CENTRALIZADAS                    │
├─────────────────────────────────────────────────────────────────┤
│  Backend                       │  Frontend                      │
│  ─────────────────────────     │  ─────────────────────────     │
│  ValidationMessages.java       │  src/constants/textos.ts       │
│  (constantes estáticas finais) │  (objeto TS tipado)            │
│                                │                                 │
│  Todas as strings de           │  Todas as strings de           │
│  validação e negócio           │  UI, sucesso e labels          │
└─────────────────────────────────────────────────────────────────┘
         │                                        │
         ▼                                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                           TESTES                                │
├─────────────────────────────────────────────────────────────────┤
│  Backend Tests                 │  Frontend/E2E Tests            │
│  import ValidationMessages     │  import TEXTOS from            │
│  import MsgNegocio             │  '@/constants/textos'          │
│                                │  import TEXTOS from helpers    │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Estrutura Backend — `MsgValidacao.java`

Criar uma classe de constantes no pacote `sgc.comum`:

```java
// backend/src/main/java/sgc/comum/MsgValidacao.java
package sgc.comum;

/**
 * Constantes de mensagens de validação usadas em anotações de Bean Validation
 * e em regras de negócio dos services.
 *
 * Centraliza todos os textos do sistema para facilitar manutenção e consistência.
 */
public final class MsgValidacao {

    private MsgValidacao() {}

    // ── Campos obrigatórios ─────────────────────────────────────────────────
    public static final String CHAVE_OBRIGATORIA       = "A chave não pode estar vazia";
    public static final String DESCRICAO_OBRIGATORIA   = "A descrição não pode estar vazia";
    public static final String JUSTIFICATIVA_OBRIGATORIA = "A justificativa é obrigatória";
    public static final String SENHA_OBRIGATORIA       = "A senha é obrigatória.";
    public static final String SIGLA_OBRIGATORIA       = "A sigla é obrigatória";
    public static final String TIPO_PROCESSO_OBRIGATORIO = "O tipo do processo é obrigatório";
    public static final String UNIDADE_OBRIGATORIA     = "O código da unidade é obrigatório";
    // ... demais campos obrigatórios

    // ── Limites de tamanho ──────────────────────────────────────────────────
    public static final String CHAVE_MAX               = "A chave deve ter no máximo 50 caracteres";
    public static final String DESCRICAO_MAX           = "A descrição deve ter no máximo 255 caracteres";
    public static final String SIGLA_MAX               = "A sigla deve ter no máximo 20 caracteres";
    // ... demais limites

    // ── Regras de negócio de processo ───────────────────────────────────────
    public static final String PROCESSO_SO_EDITAVEL_EM_CRIADO  = "Apenas processos na situação 'CRIADO' podem ser editados.";
    public static final String PROCESSO_SO_REMOVIVEL_EM_CRIADO = "Apenas processos na situação 'CRIADO' podem ser removidos.";
    public static final String PROCESSO_SO_INICIAVEL_EM_CRIADO = "Apenas processos na situação 'CRIADO' podem ser iniciados.";
    public static final String DATA_FIM_DEVE_SER_POSTERIOR      = "A data de término deve ser posterior à data de início.";
    // ... demais regras

    // ── Templates (usar com String.format) ─────────────────────────────────
    public static final String ENTIDADE_NAO_ENCONTRADA = "'%s' com codigo '%s' não encontrado(a).";
    public static final String TRANSICAO_INVALIDA      = "Transição de situação inválida: %s -> %s";
    public static final String SITUACAO_NAO_PERMITE    = "Situação do subprocesso não permite esta operação. Situação atual: %s. Situações permitidas: %s";
}
```

**Uso em DTOs:**
```java
// Antes:
@NotBlank(message = "A chave não pode estar vazia")

// Depois:
@NotBlank(message = MsgValidacao.CHAVE_OBRIGATORIA)
```

**Uso em Services:**
```java
// Antes:
throw new ErroValidacao("Apenas processos na situação 'CRIADO' podem ser editados.");

// Depois:
throw new ErroValidacao(MsgValidacao.PROCESSO_SO_EDITAVEL_EM_CRIADO);
```

**Uso em Testes:**
```java
// Antes:
assertThat(erro.getMessage()).isEqualTo("Apenas processos na situação 'CRIADO' podem ser editados.");

// Depois:
assertThat(erro.getMessage()).isEqualTo(MsgValidacao.PROCESSO_SO_EDITAVEL_EM_CRIADO);
```

### 3.3 Estrutura Frontend — `textos.ts`

Preencher o arquivo `frontend/src/constants/textos.ts` (já existente, mas vazio):

```typescript
// frontend/src/constants/textos.ts

/**
 * Constantes de strings do sistema SGC.
 * Centraliza todos os textos de UI, mensagens de sucesso e labels.
 */
export const TEXTOS = {

  // ── Mensagens de Sucesso (Toast) ─────────────────────────────────────────
  sucesso: {
    ACEITE_REGISTRADO:              "Aceite registrado",
    CADASTROS_ACEITOS_EM_BLOCO:     "Cadastros aceitos em bloco",
    DEVOLVIDO:                      "Devolução realizada",
    HOMOLOGACAO_EFETIVADA:          "Homologação efetivada",
    MAPAS_HOMOLOGADOS_EM_BLOCO:     "Mapas de competências homologados em bloco",
    MAPA_DISPONIBILIZADO:           "Disponibilização do mapa de competências efetuada",
    MAPA_SUBMETIDO_COM_SUGESTOES:   "Mapa submetido com sugestões para análise da unidade superior",
    MAPA_VALIDADO_SUBMETIDO:        "Mapa validado e submetido para análise à unidade superior",
    PROCESSO_ALTERADO:              "Processo alterado.",
    PROCESSO_CRIADO:                "Processo criado.",
    PROCESSO_FINALIZADO:            "Processo finalizado",
    PROCESSO_INICIADO:              "Processo iniciado",
    PROCESSO_REMOVIDO:              (desc: string) => `Processo ${desc} removido`,
  },

  // ── Mensagens de Erro (Feedback ao usuário) ──────────────────────────────
  erro: {
    CARREGAR_GENERICOS:       "Não foi possível carregar as informações.",
    SALVAR_PROCESSO:          "Não foi possível salvar o processo",
    REMOVER_PROCESSO:         "Não foi possível remover o processo",
    INICIAR_PROCESSO:         "Erro ao iniciar",
  },

  // ── Labels de UI ─────────────────────────────────────────────────────────
  ui: {
    CARREGANDO:               "Carregando...",
    NENHUM_RESULTADO:         "Nenhum resultado encontrado.",
    SALVANDO:                 "Salvando...",
  },

  // ── Textos de confirmação ─────────────────────────────────────────────────
  confirmacao: {
    INICIAR_PROCESSO:         "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo",
    HOMOLOGAR_EM_BLOCO:       "Selecione abaixo as unidades cujos mapas deverão ser homologados",
  },

} as const;

export type TextoKey = typeof TEXTOS;
```

**Uso em Views/Components:**
```typescript
// Antes:
toastStore.setPending("Aceite registrado")

// Depois:
import { TEXTOS } from '@/constants/textos'
toastStore.setPending(TEXTOS.sucesso.ACEITE_REGISTRADO)
```

**Uso em Testes E2E:**
```typescript
// Antes:
await expect(page.getByRole('alert')).toContainText("Aceite registrado")

// Depois:
import { TEXTOS } from '../../../frontend/src/constants/textos'
await expect(page.getByRole('alert')).toContainText(TEXTOS.sucesso.ACEITE_REGISTRADO)
```

---

## 4. Tecnologias Recomendadas

### 4.1 Backend

| Opção | Descrição | Recomendação |
|---|---|---|
| **Constantes Java** (`MsgValidacao.java`) | Classe com `public static final String`. Sem dependências extras. | ✅ **Recomendado** para este projeto |
| **Spring MessageSource** + `messages.properties` | Sistema padrão do Spring para i18n. Potente, mas complexo demais para pt-BR only. | ⚠️ Apenas se i18n for necessário |
| **Enum tipado** | Enum com método `getMessage()`. Permite rastreabilidade por tipo de erro. | 🔶 Alternativa válida para erros de negócio |

**Justificativa:** O projeto é pt-BR only. A classe de constantes é simples, sem overhead, com suporte completo do
compilador Java (usos de constantes são rastreados por IDEs), e os testes podem importar e reutilizar as mesmas
constantes.

### 4.2 Frontend

| Opção | Descrição | Recomendação |
|---|---|---|
| **Objeto TS tipado** (`textos.ts`) | `as const` garante tipo literal. Autocompletar no IDE. Sem dependências. | ✅ **Recomendado** para este projeto |
| **Vue I18n** (`vue-i18n`) | Framework completo de i18n para Vue. Suporte a plurais, interpolação, etc. | ⚠️ Overkill para pt-BR only, mas considerar se i18n for roadmap |
| **Fluent** (Mozilla) | DSL de localização expressiva. Menos popular no ecossistema Vue. | ❌ Não recomendado |

**Justificativa:** O `as const` no TypeScript garante que os valores sejam tipos literais, habilitando verificação de
tipo em compile-time. O autocompletar do IDE ajuda a encontrar a constante certa. Sem dependências adicionais.

### 4.3 Sincronização Frontend–Backend

O maior desafio é garantir que as mensagens de erro retornadas pelo backend estejam sincronizadas com o que o frontend
(e os testes) esperam. As estratégias recomendadas, por ordem de complexidade:

#### Estratégia 1 — Usar Códigos de Erro (Recomendada)

Em vez de comparar strings de mensagem, o frontend deve usar o campo `code` da resposta de erro:

```typescript
// backend retorna: { code: "PROCESSO_SOMENTE_CRIADO", message: "Apenas processos..." }

// Frontend — Antes (frágil):
if (error.message === "Apenas processos na situação 'CRIADO' podem ser editados.") { ... }

// Frontend — Depois (robusto):
if (error.code === "PROCESSO_SOMENTE_CRIADO") { ... }
```

Isso desacopla completamente a string exibida da lógica de tratamento de erro. Para isso, os `ErroValidacao` e demais
exceções devem ser criados com um código semântico, **não apenas `"VALIDACAO"`**.

#### Estratégia 2 — Arquivo de Códigos de Erro Compartilhado

Criar um arquivo de constantes de códigos de erro que pode ser exportado para o frontend via geração de código ou
mantido manualmente:

```
backend/src/main/java/sgc/comum/CodigosErro.java   ← fonte da verdade
frontend/src/constants/codigosErro.ts              ← espelho gerado ou mantido manualmente
```

#### Estratégia 3 — Contrato por Teste de Integração

Criar testes que verificam se as mensagens de erro do backend correspondem às strings usadas nos testes E2E:

```typescript
// e2e/contract/mensagens.contract.spec.ts
test('mensagem de erro de processo em situação inválida', async ({ request }) => {
  const response = await request.post('/api/processos/1/iniciar')
  const body = await response.json()
  expect(body.message).toBe(TEXTOS.errosBackend.PROCESSO_SO_INICIAVEL_EM_CRIADO)
})
```

---

## 5. Plano de Implementação

### Fase 1 — Centralização Backend (estimativa: 1–2 dias)

1. **Criar `MsgValidacao.java`** no pacote `sgc.comum` com todas as constantes
2. **Substituir strings hardcoded** nos DTOs (`@NotBlank(message = MsgValidacao.CHAVE_OBRIGATORIA)`)
3. **Substituir strings nos services** (`throw new ErroValidacao(MsgValidacao.PROCESSO_SO_EDITAVEL_EM_CRIADO)`)
4. **Atualizar testes backend** para usar as constantes importadas
5. **Resolver inconsistências** (ex: `"O código da unidade é obrigatório"` vs com ponto final)

### Fase 2 — Centralização Frontend (estimativa: 1–2 dias)

1. **Preencher `textos.ts`** com todas as strings de sucesso, erro e UI
2. **Substituir strings hardcoded** nas Views e Components
3. **Atualizar testes unitários** para usar as constantes importadas

### Fase 3 — Sincronização com Testes E2E (estimativa: 1 dia)

1. **Criar arquivo de helpers** com as constantes importadas acessíveis nos testes E2E
2. **Substituir asserções de texto hardcoded** nos testes E2E pelas constantes
3. **Adicionar lint rule** para evitar strings hardcoded em chamadas `getByText`, `toContain`

### Fase 4 — Melhoria de Códigos de Erro (estimativa: 2–3 dias)

1. **Definir códigos semânticos** para cada tipo de erro de negócio (ex: `"PROCESSO_SOMENTE_CRIADO"`)
2. **Atualizar `ErroValidacao`** para aceitar código semântico além da mensagem
3. **Criar `codigosErro.ts`** no frontend com os mesmos códigos
4. **Migrar verificações** do frontend de `message` para `code`

### Resumo de Esforço

| Fase | Esforço | Impacto |
|---|---|---|
| Fase 1 — Backend | Médio (mecânico, ~60 substituições) | Alto (consistência das mensagens) |
| Fase 2 — Frontend | Baixo (mecânico, ~30 substituições) | Médio (rastreabilidade) |
| Fase 3 — E2E | Médio (~100 substituições) | Alto (testes frágeis → robustos) |
| Fase 4 — Códigos | Alto (design + refatoração) | Muito alto (desacoplamento) |

---

## 6. Scripts de Análise

Dois scripts de análise foram criados em `frontend/etc/scripts/`:

### `extrair-mensagens.cjs`

Extrai todas as mensagens do backend, frontend e testes para um arquivo JSON estruturado:

```bash
cd frontend
node etc/scripts/extrair-mensagens.cjs
# Gera: mensagens-extraidas.json na raiz do projeto
```

### `analisar-mensagens.cjs`

Analisa o JSON gerado e produz um relatório Markdown com:
- Duplicatas exatas entre diferentes fontes
- Duplicatas com variações (pontuação, artigos)
- Strings nos testes que não aparecem no código de produção
- Strings no código que nunca aparecem nos testes

```bash
cd frontend
node etc/scripts/extrair-mensagens.cjs  # gera o JSON primeiro
node etc/scripts/analisar-mensagens.cjs  # gera mensagens-analise.md na raiz
```

---

## 7. Considerações Finais

### O que NÃO fazer

- **Não usar `vue-i18n` ou Spring `MessageSource` agora** — são soluções de i18n que adicionam complexidade
  desnecessária para um sistema pt-BR only. Se o sistema precisar de multi-idioma no futuro, a migração de constantes
  para i18n é trivial.
- **Não criar um "banco de mensagens" centralizado no banco de dados** — mensagens de sistema não são dados de negócio.
  Centralizar em banco dificulta rastreabilidade e controle de versão.
- **Não traduzir automaticamente** — algumas mensagens técnicas (`ARGUMENTO INVÁLIDO`, `ESTADO ILEGAL`) são
  intencionalmente cruas pois aparecem apenas em logs de desenvolvimento.

### O que priorizar

1. **Resolver as inconsistências detectadas** (duplicatas com pontuação diferente) — impacto imediato na UX
2. **Centralizar mensagens de toast de sucesso** — são as mais visíveis para o usuário e mais fáceis de centralizar
3. **Usar constantes nos testes** — reduz fragilidade e facilita refatoração
4. **Introduzir códigos de erro semânticos** — é a única mudança arquitetural necessária

---

*Relatório gerado em: 2026-03-14 | Versão do SGC analisada: branch copilot/generate-msg-report-scripts*
