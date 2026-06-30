# Estratégia de Cache — Frontend SGC

## Visão geral

O SGC é uma SPA com rotas em `keepAlive`. Quando o usuário navega entre telas, componentes preservados em memória são
**reativados** via `onActivated` em vez de remontados. Para evitar consultas desnecessárias ao backend a cada
reativação, as stores Pinia funcionam como **cache de sessão**: enquanto os dados forem considerados válidos, o snapshot
já carregado é reutilizado diretamente.

Para stores de contexto sensíveis à autorização, a validade do snapshot depende também da **sessão corrente**
(`usuarioCodigo`, `perfilSelecionado`,
`unidadeSelecionada`). Um snapshot carregado por uma combinação de sessão não deve ser reaproveitado por outra, mesmo
que o `codigo` do processo/subprocesso seja o mesmo.

A consequência desta escolha é que **qualquer mutação que altere dados exibidos em outra tela deve invalidar
explicitamente o cache correspondente**, caso contrário o usuário verá dados desatualizados até um refresh completo da
página.

---

## Inventário de stores com cache

| Store              | Chave de validade                             | Política                 | Dono da invalidação                          |
|--------------------|-----------------------------------------------|--------------------------|----------------------------------------------|
| `painelStore`      | `carregado + carregadoEm`                     | TTL 5 min                | `useInvalidacaoNavegacao`                    |
| `processoStore`    | `contextoCompleto.codigo + !contextoInvalido` | Explícita por evento     | `useInvalidacaoNavegacao`                    |
| `subprocessoStore` | `contextoXXXInvalido + codigo + sessão`       | Explícita por evento     | `useInvalidacaoNavegacao` + métodos internos |
| `mapasStore`       | `codigosMapaInvalidos`                        | Explícita por evento     | `useInvalidacaoNavegacao` + métodos internos |
| `historicoStore`   | `carregado`                                   | Explícita por evento     | `processoDetalheAcoes` diretamente           |
| `organizacaoStore` | `carregado`                                   | Por sessão; SSE invalida | `useCacheSync` (SSE)                         |
| `unidadeStore`     | Mapa de chaves                                | Por sessão; SSE invalida | `useCacheSync` (SSE)                         |

---

## Ponto central de invalidação

**`useInvalidacaoNavegacao`** é o único lugar autorizado a chamar
`store.invalidar()` em resposta a ações do usuário. Composables e views **não devem importar stores de cache diretamente
apenas para invalidar**.

Isso garante rastreabilidade: ao buscar quem invalida o painel ou o processo, basta procurar por
`useInvalidacaoNavegacao`.

### Quando usar cada função

```ts
const { invalidarCachesProcesso, invalidarCachesSubprocesso } = useInvalidacaoNavegacao();
```

#### `invalidarCachesProcesso()`

Use em ações que afetam o **processo inteiro**. Invalida tudo.

Casos: criar processo, iniciar processo, finalizar processo, remover processo.

#### `invalidarCachesSubprocesso(opcoes?)`

Use em ações que afetam um **subprocesso específico**. Invalida seletivamente:

| Opção                   | Quando usar                                                                                                                                                        |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `incluirPainel: true`   | Mutação altera campos de `ProcessoResumo` exibidos no painel (situação, data limite). Obrigatório após qualquer transição de workflow ou alteração de data limite. |
| `incluirProcesso: true` | O `ProcessoDetalheView` exibe dados que foram alterados (ex: ações de bloco que não redirecionam para o painel).                                                   |
| `incluirMapas: true`    | O mapa do subprocesso foi alterado (disponibilizar, validar, devolver).                                                                                            |
| `codigoSubprocessoMapa` | Limita a invalidação do mapa a um subprocesso específico, evitando recarregar todos os mapas.                                                                      |

---

## Regra fundamental: leitura não invalida, mutação invalida

Funções de **carga inicial** (ex: `sincronizarEstadoInicialContexto`,
`carregarContextoInicial`) sincronizam o estado local a partir de dados já carregados do backend. Não alteram nada no
servidor — são operações de leitura e **não devem invalidar** caches de outras telas.

Apenas **ações de escrita confirmadas pelo backend** (salvar, importar, disponibilizar, homologar, devolver, alterar
data…) devem disparar invalidação.

### Exemplo correto

```ts
// ✅ Carga inicial: só sincroniza estado local, não invalida painel
function sincronizarEstadoInicialContexto(data) {
    aplicarEstadoContexto(data);  // atualiza atividades, status local, etc.
    // SEM invalidarCachesSubprocesso aqui
}

// ✅ Mutação: aplica estado E invalida caches afetados
function processarRespostaLocal(response) {
    aplicarEstadoContexto(response);
    invalidarCachesSubprocesso({ incluirPainel: true });  // o painel será recarregado
}
```

---

## Regras específicas para o painel

O painel exibe dados de `ProcessoResumo`. Use `incluirPainel: true` sempre que a mutação alterar **qualquer um destes
campos**:

- `situacao` — alterada em toda transição de workflow
- `dataLimite` — alterada em `confirmarAlteracaoDataLimite`
- `descricao`, `tipo` — alterados em edição de processo

**Não precisa invalidar o painel** ao mutar:

- Atividades de um subprocesso (não aparecem no painel)
- Competências de um mapa (não aparecem no painel)
- Ações de bloco que permanecem no `ProcessoDetalheView` (usuário não vai ao painel)

---

## Fluxo de reativação do painel

```
Usuário navega para outra tela (ex: CadastroView)
  → Mutação ocorre
  → invalidarCachesSubprocesso({ incluirPainel: true })
      → painelStore.invalidar()    ← marca cache como inválido

Usuário navega de volta para PainelView
  → onActivated() é chamado
  → painelStore.dadosValidos() retorna false (TTL expirado OU invalidado)
  → carregarDadosPainel()          ← backend é consultado
  → painel exibe dados atualizados ✅
```

---

## Anti-padrões

### ❌ Chamar `store.invalidar()` diretamente em composables de leitura

```ts
// ❌ Errado: carga inicial invalidando o painel sem necessidade
function sincronizarEstadoInicialContexto(data) {
    processarRespostaLocal(data);  // se processarRespostaLocal invalida o painel,
                                   // a carga inicial também invalida — incorreto
}
```

### ❌ Passar `store.invalidar` como callback injetado

```ts
// ❌ Errado: esconde a dependência e impede rastreabilidade
type Deps = {
    invalidarPainel: () => void;  // quem chama isso? de onde vem?
};
```

Use `useInvalidacaoNavegacao` diretamente dentro do composable — a dependência fica explícita e rastreável.

### ❌ Reutilizar funções de processamento de mutação na carga inicial

Separe sempre a lógica comum em uma função interna pura (ex: `aplicarEstadoContexto`)
e chame a invalidação apenas nas funções de mutação.

### ❌ Invalidar o painel em rotas que não redirecionam para ele

Se o usuário permanece na tela atual após a ação (ex: ações de bloco que ficam no `ProcessoDetalheView`), não há razão
para invalidar o painel agora. Quando o usuário eventualmente navegar para o painel, o TTL de 5 minutos cobre a grande
maioria dos casos.
