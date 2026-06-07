# Diagnóstico de Tratamento de Erros — SGC

> **Escopo:** backend Java 25/Spring Boot 4 · frontend Vue 3.5/TypeScript  
> **Data:** 2026-06-06

---

## Resumo Executivo

O tratamento de erros do SGC cresceu organicamente ao longo do tempo e acumula
quatro problemas estruturais distintos, todos relacionados pelo mesmo diagnóstico
de raiz: **ausência de uma política explícita de "o que fazer com este erro".**

1. **Backend** usa exceções Java padrão (`IllegalStateException`,
   `IllegalArgumentException`, `RuntimeException`) com contexto perdido — a regra
   é que **nenhuma exceção Java padrão deve ser lançada no código de domínio**;
   toda exceção deve ser uma subclasse própria do SGC, carregando contexto real.
2. **Backend** usa exceções Java padrão, e o handler as mapeia para status
   semânticos errados (ex: `IllegalStateException` → 409 Conflict).
3. **Backend** exibe defensividade excessiva em invariantes de domínio: estados
   que *nunca* deveriam ocorrer em produção são verificados com
   `throw new ErroValidacao(...)` — sinalizando um bug como erro do usuário.
4. **Frontend** não tem uma política de notificação unificada: erros de rede,
   erros de validação e erros inesperados passam por até quatro caminhos
   diferentes dependendo do composable, criando comportamento inconsistente.

---

## Parte 1 — Backend

### P-0 · Regra não negociável: nenhuma exceção Java padrão no código de domínio

> **Toda exceção lançada pelo código do SGC deve ser uma subclasse própria
> da hierarquia `ErroNegocioBase` / `ErroInterno`. Exceções Java padrão
> (`IllegalStateException`, `IllegalArgumentException`, `RuntimeException`, etc.)
> nunca carregam contexto de domínio útil, enganam o handler HTTP e tornam
> o diagnóstico de produção muito mais difícil.**

O projeto já tem a hierarquia correta. O problema é que ela não está sendo
aplicada de forma consistente — desenvolvendo o hábito de alcançar a classe
Java mais próxima em vez de usar a classe de domínio adequada.

**Inventário completo de exceções Java padrão em código de produção
(excluindo `e2e/` e testes):**

| Tipo | Arquivo | Linha | Contexto |
|---|---|---|---|
| `IllegalStateException` | `ResponsavelUnidadeService` | 88, 330, 339 | Invariante de join quebrada |
| `IllegalStateException` | `UnidadeProcesso` | 97 | Código nulo após persistência |
| `IllegalStateException` | `SubprocessoDtoMapper` | 26, 78 | Subprocesso sem processo/unidade |
| `IllegalStateException` | `PainelService` | 228 | Sigla de unidade ausente no contexto |
| `IllegalStateException` | `ProcessoService` | 659 | Invariante de negócio |
| `IllegalStateException` | `CadastroFluxoService` | 324 | `default` em switch de situação |
| `IllegalStateException` | `RelatorioService` | 102, 209, 397 | Falha ao gerar PDF |
| `IllegalStateException` | `AtividadeController` | 57 | Resposta sem código gerado |
| `IllegalStateException` | `E2eController` | 950 | Diagnóstico não encontrado |
| `IllegalArgumentException` | `UnidadeProcesso` | 67 | Tipo de unidade não suportado em snapshot |
| `IllegalArgumentException` | `SgcPermissionEvaluator` | 234 | `AcaoPermissao` inválida (bug de programação) |
| `RuntimeException` | `EmailService` | 67 | Falha SMTP |
| `RuntimeException` | `E2eController` | 195 | Limpeza de processo falhou |

**Como substituir — exemplos concretos:**

```java
// ANTES (EmailService.java:67)
} catch (MessagingException | UnsupportedEncodingException e) {
    throw new RuntimeException(e);   // tipo perdido, sem contexto
}

// DEPOIS
} catch (MessagingException | UnsupportedEncodingException e) {
    throw new ErroEnvioEmail(destinatario, e);  // nova subclasse de ErroInterno
    // → 500 + log automático no RestExceptionHandler
}
```

```java
// ANTES (SgcPermissionEvaluator.java:234)
throw new IllegalArgumentException(
    "Ação de permissão desconhecida: '%s'...".formatted(permissao), e);

// DEPOIS
throw new ErroInconsistenciaInterna(
    "AcaoPermissao desconhecida no evaluator: '%s'. Bug de programação.".formatted(permissao));
// O cause original pode ser passado se ErroInconsistenciaInterna aceitar Throwable
```

```java
// ANTES (UnidadeProcesso.java:67)
throw new IllegalArgumentException(
    "Tipo de unidade nao suportado em snapshot de processo: " + unidade.getTipo());

// DEPOIS — aqui há ambiguidade: é erro de negócio (422) ou bug (500)?
// Se tipos inválidos nunca chegam a este método em fluxo correto: ErroInconsistenciaInterna
// Se podem ser enviados pelo usuário: ErroValidacao com mensagem clara
throw new ErroInconsistenciaInterna(
    "Tipo de unidade '%s' não suportado em snapshot para processo %d"
    .formatted(unidade.getTipo(), processo.getCodigo()));
```

**Passos acionáveis:**

- [ ] Criar `ErroEnvioEmail extends ErroInterno` para encapsular falhas SMTP
  com destinatário e causa original.
- [ ] Substituir cada linha da tabela acima pela exceção de domínio adequada.
  Para cada uma, decidir: **bug/inconsistência** → `ErroInconsistenciaInterna`;
  **dado inválido fornecido externamente** → `ErroValidacao`.
- [ ] Adicionar `checkstyle` ou ArchUnit proibindo `throw new IllegalStateException`,
  `throw new IllegalArgumentException` e `throw new RuntimeException` fora de
  `e2e/` e testes — enforcement automático da regra.

---

### P-1 · Exceções Java cruas recebem tratamento HTTP errado no handler

**Hierarquia disponível (correta):**

```
RuntimeException
├── ErroNegocioBase            → 4xx semanticamente informados
│   ├── ErroEntidadeNaoEncontrada  → 404
│   ├── ErroValidacao              → 422
│   ├── ErroAcessoNegado           → 403
│   └── (ErroAutenticacao)         → 401  ← fora da hierarquia, veja P-1b
└── ErroInterno                → 500 + log
    ├── ErroConfiguracao
    └── ErroInconsistenciaInterna
```

**O que acontece na prática:**

`IllegalStateException` é lançada em 15+ pontos do código para sinalizar
invariantes quebradas de domínio (bugs). O `RestExceptionHandler` a captura
e retorna **HTTP 409 Conflict** com `code: "ESTADO_ILEGAL"` — um status
completamente errado para um bug de runtime.

```java
// ResponsavelUnidadeService.java:88
.orElseThrow(() -> new IllegalStateException(
    "Usuário ausente para atribuição temporária %d".formatted(atribuicao.getCodigo())));

// RestExceptionHandler.java:281-292  → retorna 409! Errado.
@ExceptionHandler(IllegalStateException.class)
protected ResponseEntity<ErroApi> handleIllegalStateException(IllegalStateException ex) {
    ...
    .status(HttpStatus.CONFLICT.value())
    .code("ESTADO_ILEGAL")
```

Exemplos adicionais usando `IllegalStateException` onde se esperaria
`ErroInconsistenciaInterna`:

| Arquivo | Linha | Situação |
|---|---|---|
| `ResponsavelUnidadeService` | 330, 339 | Titular/responsável ausente em dado persistido |
| `SubprocessoDtoMapper` | 26, 78 | Subprocesso sem processo/unidade associada |
| `PainelService` | 228 | Sigla de unidade do usuário ausente no contexto |
| `ProcessoService` | 659 | Invariante de negócio quebrada |
| `CadastroFluxoService` | 324 | `default` em switch de situação |
| `RelatorioService` | 102–104, 209–211, 397–399 | `DocumentException`/`IOException` ao gerar PDF |
| `MapaManutencaoService` | 116, 121 | `orElseThrow()` sem mensagem (NPE silencioso) |
| `AtividadeController` | 57 | Resposta de criação sem código gerado |

Além disso, `RuntimeException` crua é usada em dois pontos:

```java
// EmailService.java:67
} catch (MessagingException | UnsupportedEncodingException e) {
    throw new RuntimeException(e);  // engole o tipo, perde contexto
}

// E2eController.java:195
throw new RuntimeException("Falha na limpeza do processo: " + e.getMessage());
```

**P-1b — `ErroAutenticacao` fora da hierarquia:**

```java
// ErroAutenticacao.java
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ErroAutenticacao extends RuntimeException { ... }
// Não estende ErroNegocioBase nem ErroInterno
```

O handler tem um `@ExceptionHandler(ErroAutenticacao.class)` separado que
funciona, mas a classe diverge da hierarquia sem razão estrutural aparente.
Se alguém adicionar um novo handler mais específico para `RuntimeException`,
pode facilmente quebrar o comportamento.

**Passos acionáveis:**

- [ ] Substituir todos os `IllegalStateException` relacionados a invariantes de
  domínio por `ErroInconsistenciaInterna` (já existe, basta usar).
- [ ] Substituir `RuntimeException` em `EmailService` por `ErroInterno`
  apropriado (ex: `ErroConfiguracao` ou subclasse nova `ErroEnvioEmail`).
- [ ] Corrigir o handler de `IllegalStateException` para retornar **500**, não
  409 — ou removê-lo completamente (o handler genérico de `Exception` já
  devolve 500).
- [ ] Mover `ErroAutenticacao` para estender `ErroNegocioBase` e eliminar o
  handler separado.

---

### P-2 · Defensividade excessiva para situações impossíveis → erro de domínio mascarando bug

Este é o problema mais disseminado. Situações que indicam corrupção de dados
ou bug no código são sinalizadas como `ErroValidacao` (422) — o que engana o
frontend e o operador sobre a natureza do problema.

**Exemplos concretos:**

```java
// LocalizacaoSubprocessoService.java:69
// Subprocesso com situação ativa mas SEM nenhuma movimentação registrada
// → isso é corrupção de dados / bug, não erro do usuário
throw new ErroValidacao(
    "Subprocesso persistido sem movimentação em situação inválida: %s"
    .formatted(subprocesso.getSituacao()));
```

```java
// Subprocesso.java:73
// Transição de situação inválida: o código de transição de estados do próprio
// domínio lançando ErroValidacao quando deveria ser ErroInconsistenciaInterna
throw new ErroValidacao(Mensagens.TRANSICAO_INVALIDA.formatted(...));
```

```java
// RelatorioService.java:102-104
// IOException ao escrever PDF → problema de infraestrutura, não validação
} catch (DocumentException e) {
    throw new IllegalStateException("Erro ao gerar PDF", e);
} catch (IOException e) {
    throw new IllegalStateException("Erro ao gerar PDF", e);
}
// Dois catches idênticos podem ser unificados, e o tipo deveria ser ErroInterno
```

```java
// MapaManutencaoService.java:116, 121
// orElseThrow() sem lambda → NPE nua sem mensagem nem traceId
return mapaRepo.buscarCompletoPorSubprocesso(subprocessoCodigo)
        .orElseThrow();
```

```java
// SubprocessoConsultaService.java:185
// Código de erro opaco sem contexto
throw new ErroValidacao("SGC-MSG-100230");
// O usuário e o operador não conseguem entender o que é este erro
```

**Regra que falta:**

> "Entidade que **deveria** existir mas não existe" é um `ErroInconsistenciaInterna`
> (500), não um `ErroEntidadeNaoEncontrada` (404) nem um `ErroValidacao` (422).

A distinção correta é:
- **Entidade buscada por chave fornecida pelo usuário** → `ErroEntidadeNaoEncontrada` (404)
- **Entidade buscada por chave interna gerada pelo sistema** → `ErroInconsistenciaInterna` (500)

`ComumRepo` já implementa corretamente o primeiro caso. O segundo não tem um
padrão estabelecido, então os desenvolvedores escolhem qualquer coisa.

**Passos acionáveis:**

- [ ] Adicionar um comentário/javadoc explícito em `ErroInconsistenciaInterna`
  com a regra: *"Use quando uma entidade referenciada por chave interna não for
  encontrada, indicando corrupção de dados ou bug."*
- [ ] Substituir `orElseThrow()` sem lambda por
  `orElseThrow(() -> new ErroInconsistenciaInterna("Mapa não encontrado para subprocesso " + subprocessoCodigo))`.
- [ ] Rever `LocalizacaoSubprocessoService:69` e `Subprocesso:73` — se são
  transições inválidas que *nunca* deveriam ocorrer, usar
  `ErroInconsistenciaInterna`; se são erros recuperáveis pelo usuário, manter
  `ErroValidacao` com mensagem mais clara.
- [ ] Unificar os dois catches idênticos em `RelatorioService` e usar
  `ErroInconsistenciaInterna` (ou uma nova `ErroGeracaoPdf extends ErroInterno`).
- [ ] Eliminar o código opaco `"SGC-MSG-100230"` — substituir pela mensagem
  legível que está certamente documentada em algum lugar.

---

### P-3 · `ErroNegocioBase` tem um construtor de emergência que não deveria existir

```java
// ErroNegocioBase.java:28-33
protected ErroNegocioBase(Throwable cause) {
    super("msg", cause);      // mensagem literal "msg"!
    this.code = "CODE";       // código literal "CODE"!
    this.status = HttpStatus.BAD_REQUEST;
    this.details = new HashMap<>();
}
```

Este construtor está marcado `protected` e parece ser uma sobra de
desenvolvimento. Se usado em produção, retornaria ao usuário
`{"message": "msg", "code": "CODE", "status": 400}` — completamente inútil.

**Passo acionável:**

- [ ] Verificar se há algum uso deste construtor (`grep -r "new Erro.*cause"`
  no projeto). Se não houver, remover imediatamente. Se houver, corrigir os
  chamadores.

---

## Parte 2 — Frontend

### P-4 · Quatro mecanismos de notificação sem política de escolha

O frontend tem os seguintes canais de comunicação de erros ao usuário:

| Canal | Onde é usado | Quem vê |
|---|---|---|
| `useNotification()` → `notify()` | Maioria dos composables de tela | Exibido na própria view |
| `useAsyncAction()` → `erro` ref | `useFluxoMapa`, `useMapas`, `useAdministradoresTela` | Varia — às vezes ignorado |
| `useErrorHandler()` → `ultimoErro` | `useCadastroTela`, `useFluxoSubprocessoExecucao` | Exibido na própria view |
| `app.config.errorHandler` → `/erro` | Erros não capturados bubblando até o Vue | Redireciona para página de erro |

**O problema:** não há uma regra documentada de *quando usar qual*. Resultado:

- Em `useFluxoMapa`, erros são capturados em `useAsyncAction().erro` (uma
  `ref<string|null>`) — mas quem usa `useFluxoMapa` às vezes ignora esta ref
  e às vezes exibe via `notify()`.
- `useMapaTela` chama `notify(subprocessoStore.erroIntegracaoContexto.mensagem)`
  mas ao mesmo tempo usa `erroMapa` de `mapasStore` — dois canais paralelos
  para a mesma tela.
- `useAsyncAction` captura o erro mas só preserva `error.message` — jogando
  fora o tipo, o código HTTP, os detalhes e o traceId estruturados que o
  backend envia.

```typescript
// useAsyncAction.ts:27-33
function obterMensagemErro(error: unknown, mensagemPadrao: string): string {
    if (error instanceof Error && error.message) {
        return error.message;  // perde tudo exceto a string
    }
    return mensagemPadrao;
}
```

**Passos acionáveis:**

- [ ] Definir e documentar explicitamente em `README.md` do frontend uma
  política de dois níveis:
  - **Erros locais** (422/validação, 404 recuperável): tratados pelo composable
    via `useNotification().notify()` ou via `useFormErrors`.
  - **Erros globais** (rede, 500, 403): propagados para cima e capturados
    pelo `app.config.errorHandler` → `/erro`.
- [ ] Considerar deprecar `useAsyncAction` em favor de `useErrorHandler` (que
  preserva o `ErroNormalizado` estruturado) ou pelo menos fazer `useAsyncAction`
  armazenar o `ErroNormalizado` completo em vez de só a string da mensagem.
- [ ] Auditar `useFluxoMapa`: escolher explicitamente entre `erro` ref ou
  `notify()` — nunca os dois para o mesmo fluxo de erro.

---

### P-5 · Axios interceptor notifica globalmente para `naoAutorizado` mas depois relança — duplicação silenciosa

```typescript
// axios-setup.ts:258-266
const normalized = normalizarErro(error);

if (normalized.tipo === 'naoAutorizado') {
    tratarErroNaoAutorizado();  // redireciona para /login
} else if (deveNotificarGlobalmente(normalized)) {
    logger.error("[axios] Erro global:", normalized.mensagem);
}

return Promise.reject(error);  // relança em qualquer caso
```

E no `main.ts`:

```typescript
app.config.errorHandler = (err) => {
    if (isErroCanceladoHttp(err)) return;
    const normalizado = normalizarErro(err);
    if (normalizado.tipo === 'naoAutorizado') return;  // suprime
    if (TIPOS_SEM_SOLUCAO.has(normalizado.tipo)) {
        logger.error('[errorHandler]', normalizado.mensagem, err);
        window.location.assign('/erro');  // redireciona para /erro
        return;
    }
    throw err;
};
```

Resultado: um erro de rede faz `logger.error` no interceptor *e* novamente no
`errorHandler` — dois logs para o mesmo evento. Para `naoAutorizado`, o
interceptor redireciona para `/login` *e* relança, mas o `errorHandler` suprime
— funciona, mas por coincidência.

**Passo acionável:**

- [ ] Centralizar a notificação de erros globais **somente** no
  `app.config.errorHandler`. O interceptor deve apenas (1) redirecionar para
  `/login` em `naoAutorizado` e (2) marcar como cancelado quando adequado —
  sem logar. Isso elimina a duplicação e garante um único ponto de registro.

---

### P-6 · Catch blocks no nível de view que não normalizam o erro

Alguns `catch` em views e composables não usam `normalizarErro()`, capturando
`error as unknown` e exibindo mensagens genéricas:

```typescript
// useCadastroTela.ts:284-286
} catch (error) {
    definirErroGlobal(normalizarErro(error).mensagem);  // OK, usa normalizarErro
}

// useCadastroTela.ts:303-306  ← sem catch! confirmarDisponibilizacao() pode lançar
async function confirmarDisponibilizacao() {
    ...
    loadingDisponibilizacao.value = true;
    try {
        if (isRevisao.value) {
            await fluxoSubprocesso.disponibilizarRevisaoCadastro(codSubprocesso);
        } else {
            await fluxoSubprocesso.disponibilizarCadastro(codSubprocesso);
        }
    } finally {                 // ← finally sem catch
        loadingDisponibilizacao.value = false;
    }
    // se a linha acima lança, o erro sobe para o Vue errorHandler → /erro
    // sem nenhuma mensagem útil para o usuário
    mostrarModalConfirmacao.value = false;
}
```

**Passo acionável:**

- [ ] Adicionar `catch` em `confirmarDisponibilizacao()` que use
  `normalizarErro(error).mensagem` e exiba via `notify()` — igual ao padrão
  já usado em `disponibilizarCadastro()`.

---

## Mapa de Prioridades

| # | Problema | Impacto | Esforço | Prioridade |
|---|---|---|---|---|
| **P-0** | **Exceções Java padrão em código de domínio (13 ocorrências)** | **Crítico (contexto perdido, status errado)** | **Médio** | **🔴 Crítica** |
| P-2 | Invariantes de domínio reportadas como erro do usuário | Alto (diagnóstico errado em produção) | Médio | **Alta** |
| P-1 | `IllegalStateException` → 409 (handler errado) | Alto (status enganoso) | Baixo | **Alta** |
| P-3 | Construtor `ErroNegocioBase(Throwable)` com "msg"/"CODE" | Alto se usado | Muito baixo | **Alta** |
| P-4 | Quatro canais de notificação sem política | Médio (comportamento inconsistente) | Médio | Média |
| P-5 | Log duplicado no interceptor e errorHandler | Baixo (ruído) | Baixo | Média |
| P-1b | `ErroAutenticacao` fora da hierarquia | Baixo (funciona por acidente) | Baixo | Baixa |
| P-6 | `confirmarDisponibilizacao()` sem catch | Baixo (raro, mas péssima UX) | Baixo | Baixa |

> [!NOTE]
> P-0 e P-1 são dois aspectos do mesmo problema raiz. Resolvendo P-0 (trocar todas
> as exceções Java padrão por subclasses de domínio), P-1 desaparece naturalmente —
> o handler de `IllegalStateException` passa a não ser mais alcançado.

---

## Princípios Orientadores Propostos

> **P-A — Exceção própria sempre.**
> Nenhuma exceção Java padrão (`IllegalStateException`, `RuntimeException` etc.)
> é lançada pelo código de domínio do SGC. Toda exceção é uma subclasse da
> hierarquia interna e carrega contexto real: entidade afetada, código,
> operação, causa.

> **P-B — Bug ≠ erro de usuário.**
> Se a situação é impossível em dados íntegros com código correto, não trate —
> deixe virar 500 imediatamente via `ErroInconsistenciaInterna` com log
> estruturado. Reserve os tipos semânticos (404, 422, 403) exclusivamente para
> situações causadas por dados fornecidos pelo usuário ou por fluxos legítimos
> de negócio.

> **P-C — Um único ponto de saída por categoria.**
> Erros de rede/inesperados → `app.config.errorHandler` → `/erro`.
> Erros de validação/negócio → composable local → `notify()`. Nunca os dois.
