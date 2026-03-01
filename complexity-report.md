# Relatório de Complexidade – SGC

> Contexto: aplicação de intranet, 5-10 usuários simultâneos. Análise realizada em 01/03/2026.

---

## 1. Resumo Executivo

| Camada | Indicador | Valor |
|---|---|---|
| Backend – Services | Maior arquivo | `SubprocessoService.java` — **1 712 linhas / 104 métodos** |
| Backend – Facades | Total | **8 facades** (nem todas justificáveis) |
| Backend – DTOs | Subprocesso module | **27 DTOs** para um único domínio |
| Backend – Erros | Hierarquia global | **12 classes** de exceção + 6 adicionais em submódulos |
| Backend – Processo services | Total | **9 classes** de service para um único módulo |
| Frontend – Stores | Total | **13 stores** globais (Pinia) |
| Frontend – Services | Total | **12 service files** |
| Frontend – Components | Total | **~146 componentes** em 9 subpastas |
| Frontend – Types | `tipos.ts` | **~11 000 bytes** em arquivo único |

---

## 2. Backend — Pontos Críticos

### 2.1 `SubprocessoService` — God Service

- **1 712 linhas** e **104 métodos públicos/privados** em uma única classe.
- Mistura responsabilidades de: CRUD, validação de cadastro, transições de workflow, formatação de resposta, delegação para mapas.
- Presença de 3 overloads de `validarSituacaoPermitida` com semânticas sutilmente diferentes.

> [!CAUTION]
> Qualquer mudança neste arquivo exige leitura de centenas de linhas de contexto. Risco alto de efeitos colaterais.

**Oportunidade:** extrair pelo menos 3 services focados: `SubprocessoWorkflowService` (transições), `SubprocessoValidadorService` (validações), `SubprocessoCadastroService` (CRUD + disponibilização).

---

### 2.2 Facades pass-through — custo sem benefício

| Facade | Linhas | Métodos | % Pass-Through | Veredicto |
|---|---|---|---|---|
| `OrganizacaoFacade` | 151 | 28 | **~100%** | 🔴 Remover |
| `UsuarioFacade` | 213 | 19 | ~50% | 🟡 Avaliar |
| `LoginFacade` | 130 | 4 | 0% | 🟢 Manter |
| `AlertaFacade` | 203 | 16 | ~15% | 🟢 Manter |
| `AtividadeFacade` | 173 | 12 | 0% | 🟢 Manter |
| `ProcessoFacade` | 314 | 25 | ~20% | 🟢 Manter |
| `PainelFacade` | 222 | 7 | 0% | 🟢 Manter |
| `RelatorioFacade` | 95 | 2 | 0% | 🟢 Manter |

**`OrganizacaoFacade`** é o caso mais claro: todos os 28 métodos são delegação direta a `UnidadeService`, `UnidadeHierarquiaService`, `ResponsavelUnidadeService` ou `UsuarioFacade`. Consumidores podem injetar os services diretamente.

**`UsuarioFacade`** mistura delegação pura (`buscarUsuariosPorUnidade → usuarioService.buscarPorUnidadeLotacao`) com lógica real (perfis, autenticação). Separar os dois papéis.

---

### 2.3 Proliferação de classes de erro

**Hierarquia em `comum/erros` (12 classes):**

```
ErroNegocioBase (abstract)
├── ErroNegocio
├── ErroValidacao
├── ErroEntidadeNaoEncontrada
├── ErroAcessoNegado
├── ErroConfiguracao
├── ErroInterno
└── ErroAutenticacao
+ ErroApi, ErroSubApi (DTOs de resposta)
+ RestExceptionHandler (192 linhas, 10 handlers)
```

**Erros adicionais em submódulos (6):**

- `subprocesso/erros/` — `ErroMapaEmSituacaoInvalida`, `ErroMapaNaoAssociado`, `ErroTransicaoInvalida`
- `processo/erros/` — `ErroProcesso`, `ErroProcessoEmSituacaoInvalida`, `ErroUnidadesNaoDefinidas`
- `processo/painel/` — `ErroParametroPainelInvalido`
- `relatorio/` — `ErroRelatorio`

> [!WARNING]
> São **18+ classes de erro** para uma aplicação de intranet. Muitas das exceções de submódulo poderiam ser consolidadas em `ErroNegocio` com códigos de erro dinâmicos.

**Oportunidade:** reduzir para 5-6 exceções base (`ErroNegocio`, `ErroValidacao`, `ErroEntidadeNaoEncontrada`, `ErroAcessoNegado`, `ErroInterno`) e usar códigos/mensagens dinâmicos no lugar de cada subclasse específica.

---

### 2.4 Módulo `processo/service` — fragmentação excessiva

O módulo `processo` tem **9 classes de service** para um domínio relativamente simples:

| Classe | Linhas | Responsabilidade |
|---|---|---|
| `ProcessoConsultaService` | 4 638B | Queries |
| `ProcessoManutencaoService` | 3 826B | CRUD |
| `ProcessoInicializador` | 6 327B | Iniciar processo |
| `ProcessoFinalizador` | 2 002B | Finalizar processo |
| `ProcessoValidador` | 3 371B | Validações |
| `ProcessoAcessoService` | 3 651B | Controle de acesso |
| `ProcessoDetalheBuilder` | 3 661B | DTO builder |
| `ProcessoNotificacaoService` | 11 136B | Notificações por e-mail |

**Oportunidade:** consolidar as classes menores. `ProcessoFinalizador` (2KB) e `ProcessoInicializador` (6KB) podem ser métodos de um `ProcessoWorkflowService`. `ProcessoValidador` e `ProcessoAcessoService` podem ser fundidos. Meta: de 9 → 4 classes.

---

### 2.5 Volume de DTOs no módulo `subprocesso`

O diretório `subprocesso/dto/` contém **27 arquivos** de DTO. Alguns são bastante granulares:

- `MensagemResponse` (118 bytes — provavelmente apenas `record MensagemResponse(String mensagem)`)
- `ValidacaoCadastroDto` (253 bytes)
- `ErroValidacaoDto` (304 bytes)
- `ConhecimentoAjusteDto` (304 bytes)

**Oportunidade:** consolidar DTOs pequenos que são usados em um único endpoint. Pesquisar DTOs usados apenas internamente — podem virar records inline ou inner classes.

---

## 3. Frontend — Pontos Críticos

### 3.1 Stores globais com boilerplate repetitivo

As **13 stores Pinia** seguem um padrão repetitivo de gerenciamento de estado:

```typescript
// Padrão repetido em TODAS as stores (mapas.ts, processos.ts, etc.)
async function <acao>(args) {
    carregando.value = true;
    erro.value = null;
    try {
        resultado.value = await service<Acao>(args);
    } catch (e: any) {
        erro.value = e.message || "Erro ao ...";
        throw e;
    } finally {
        carregando.value = false;
    }
}
```

A store `mapas.ts` (229 linhas) repete esse padrão em **10 de 11 métodos** — são ~15 linhas de boilerplate por método.

**Oportunidade:**
1. Criar um composable `useAsyncAction()` genérico que abstraia o try/catch/loading/error.
2. Para entidades de leitura simples (alertas, configurações, unidades), substituir stores por composables com estado local na view que consome os dados.

---

### 3.2 Stores com lógica duplicada entre módulos

A store `processos.ts` (243 linhas) contém ações como `alterarDataLimiteSubprocesso`, `validarMapa`, `disponibilizarMapa` — que são operações de **subprocesso/mapa**, não de processo.

A store `subprocessos.ts` (275 linhas) também contém `disponibilizarCadastro`, `aceitarCadastro`, etc.

**Resultado:** lógica de subprocesso fragmentada entre duas stores, aumentando acoplamento e dificultando rastreamento.

**Oportunidade:** centralizar toda operação de subprocesso em uma única store ou, melhor, migrar para services chamados diretamente.

---

### 3.3 Contagem elevada de componentes

O projeto tem **~146 componentes** distribuídos em 9 subpastas:

| Pasta | Componentes |
|---|---|
| `processo/` | 26 |
| `comum/` | 26 |
| `mapa/` | 16 |
| `relatorios/` | 13 |
| `atividades/` | 10 |
| `layout/` | 8 |
| `configuracoes/` | 7 |
| `unidade/` | 6 |

Para uma aplicação com ~20 views, a razão é de **~7 componentes por view** — alta para o escopo. Muitos componentes provavelmente são wrappers finos ou usados em um único lugar.

**Oportunidade:** identificar componentes usados apenas uma vez e inlinear na view pai.

---

## 4. Cortes Transversais

### 4.1 Camada `@Lazy` e dependências circulares

`AtividadeFacade` injeta `SubprocessoService` com `@Lazy`, indicando dependência circular entre `mapa` → `subprocesso`. Isso é sintoma de responsabilidades entrelaçadas.

**Oportunidade:** extrair a interface compartilhada (permissões de edição de mapa) para um serviço independente, eliminando a dependência circular.

---

### 4.2 `SgcPermissionEvaluator` como ponto de mistura

A classe (237 linhas) implementa `PermissionEvaluator` do Spring e consolida regras de subprocesso + processo + perfil em um único ponto. Embora seja coerente como design, a lógica de checagem de hierarquia é complexa e poderia ser simplificada com cache de hierarquia ou com uma abordagem mais declarativa.

---

## 5. Priorização Sugerida

### Fase 1 — Impacto Alto, Risco Baixo

| # | Ação | Impacto | Risco |
|---|---|---|---|
| 1 | **Remover `OrganizacaoFacade`** — consumidores injetam services diretamente | Elimina ~150 linhas de indireção pura | Baixo — refactoring mecânico |
| 2 | **Consolidar classes de erro** — de 18 → ~6 | Reduz arquivos e simplifica handler | Baixo |
| 3 | **Criar composable `useAsyncAction()`** no frontend | Elimina ~150 linhas de boilerplate nas stores | Baixo |

### Fase 2 — Impacto Alto, Risco Médio

| # | Ação | Impacto | Risco |
|---|---|---|---|
| 4 | **Quebrar `SubprocessoService`** em 3-4 services focados | Reduz God Service de 1712 → ~400-500 linhas cada | Médio — requer cuidado com transações |
| 5 | **Consolidar services de `processo`** de 9 → 4 | Simplifica navegação no código | Médio |
| 6 | **Migrar stores de leitura simples** para composables locais | Elimina estado global desnecessário | Médio |

### Fase 3 — Médio Prazo

| # | Ação | Impacto | Risco |
|---|---|---|---|
| 7 | **Resolver dependência circular** (`@Lazy` em `AtividadeFacade`) | Melhora testabilidade | Baixo-Médio |
| 8 | **Inlinear componentes single-use** | Reduz contagem de arquivos | Baixo |
| 9 | **Consolidar DTOs** do subprocesso de 27 → ~15 | Reduz cognitive load | Baixo-Médio |
