# Auditoria: Cache e Invalidação de Estado — Frontend SGC

## Contexto

O frontend usa **Pinia como cache de sessão**. As stores mantêm snapshots de dados
enquanto o cache é válido. Rotas em `keepAlive` reaproveitam o snapshot no
`onActivated`, sem ir ao backend, se `dadosValidos()` retornar `true`.

O padrão correto de invalidação após qualquer mutação que afete outra tela é:
chamar `store.invalidar()` antes de redirecionar ou reativar a view de destino.

---

## Stores com cache (inventário)

| Store | Chave de validade | TTL / Política |
|---|---|---|
| `painelStore` | `carregado + carregadoEm` | TTL 5 min |
| `processoStore` | `contextoCompleto.codigo + !contextoInvalido` | Sem TTL; invalidação explícita |
| `subprocessoStore` | `contextoXXXInvalido + codigo` | Sem TTL; invalidação explícita |
| `mapasStore` | `codigosMapaInvalidos` | Sem TTL; invalidação explícita |
| `historicoStore` | `carregado` | Sem TTL; invalidação explícita |
| `organizacaoStore` | `carregado` | Por sessão; SSE invalida |
| `unidadeStore` | Mapa de chaves | Por sessão; SSE invalida |

---

## Problemas encontrados

### ✅ BUG 1 — CORRIGIDO: `processarRespostaLocal` não invalidava `painelStore`

**Arquivo:** `useCadastroOrquestracao.ts`  
**Impacto:** Após importar atividades (ou adicionar/remover), voltar ao painel mostrava dados desatualizados enquanto o TTL de 5 min não expirava.  
**Correção aplicada:** `painelStore.invalidar()` adicionado em `processarRespostaLocal`.

---

### ⚠️ ACHADO 2 — `sincronizarEstadoInicialContexto` invalida o painel desnecessariamente na carga inicial

**Arquivo:** `useCadastroOrquestracao.ts`, linhas 39–52  
**Causa:** `sincronizarEstadoInicialContexto` chama `processarRespostaLocal` (que agora invalida o painel). Isso é correto para mutações, mas **na carga inicial** é desnecessário — estamos apenas lendo dados do backend, não alterando nada.  
**Impacto:** Baixo. O painel vai recarregar na próxima visita, mesmo que nada tenha mudado. Não causa dados incorretos, apenas uma recarga extra.  
**Decisão:** Aceitar por ora — a correção tornaria `processarRespostaLocal` mais complexa (precisaria de parâmetro para suprimir a invalidação do painel). O risco de regressão supera o ganho.

---

### ✅ OK 3 — `useFluxoSubprocesso` invalida painel corretamente

Todas as ações de workflow (disponibilizar, devolver, aceitar, homologar) que redirecionam para o painel passam `invalidarCaches: {incluirPainel: true}`. Nenhum gap identificado.

---

### ✅ OK 4 — `ProcessoCadastroView` invalida caches corretamente

Nas ações salvar, iniciar e remover processo, `invalidarCachesProcesso()` é chamada. Esse método invalida `painelStore`, `processoStore`, `subprocessoStore` e `mapasStore`. Coberto.

---

### ⚠️ ACHADO 5 — `executarAcaoBloco` (sem redirecionar) não invalida `painelStore`

**Arquivo:** `processoDetalheAcoes.ts`, linha 124  
**Código:**
```ts
invalidarCachesSubprocesso({incluirPainel: false, incluirProcesso: true});
```
**Contexto:** Quando `redirecionarPainel === false`, a ação de bloco atualiza subprocessos mas permanece no `ProcessoDetalheView`. O `processoStore` é invalidado e recarregado no mesmo fluxo. O `painelStore` **não é invalidado** (`incluirPainel: false`).  
**Avaliação:** **Intencional e correto.** O usuário não está navegando para o painel. Se futuramente voltar ao painel, o TTL de 5 min protege. E as ações de bloco que *redirecionam* para o painel (`redirecionarPainel === true`, linha 117) já chamam `invalidarCachesProcesso()` que inclui o painel.

---

### ⚠️ ACHADO 6 — Mutações de competências no `MapaView` não invalidam `painelStore`

**Arquivo:** `useMapaCompetenciasMutacoes.ts` + `MapaView.vue`  
**Contexto:** Adicionar, editar ou remover competências chama `sincronizarMapa()` → `mapasStore.definirMapaCompleto()`. O `painelStore` **não é invalidado**.  
**Avaliação:** **Provavelmente aceitável.** Mutações de competências não alteram a *situação* do processo nem do subprocesso que aparece no painel. O painel exibe apenas situação e responsável — dados que não mudam com edição de competências.  
**Risco real:** Zero, porque o painel não exibe dados de competências.

---

### ⚠️ ACHADO 7 — `confirmarAlteracaoDataLimite` no `SubprocessoView` não invalida `painelStore`

**Arquivo:** `subprocessoAcoesAdministrativas.ts`, linha 98–108  
**Contexto:** Após alterar data limite, `atualizarSubprocessoAtual()` força recarga do `subprocessoStore` mas o `painelStore` não é invalidado.  
**Avaliação:** O painel exibe data limite? Verificando `TabelaProcessos.vue` é necessário.  
**Status:** Requer verificação adicional — ver abaixo.

---

### ✅ OK 8 — `confirmarFinalizacao` invalida `historicoStore`

**Arquivo:** `processoDetalheAcoes.ts`, linha 81  
O `historicoStore.invalidar()` é chamado ao finalizar processo. Correto — o histórico deve exibir o processo recém-finalizado.

---

### ✅ OK 9 — SSE (`useCacheSync`) invalida `painelStore`, `unidadeStore`, `organizacaoStore`

O canal SSE garante que mudanças externas (de outros usuários ou processos) invalidem os caches organizacionais. Coberto.

---

## Verificação pendente — ACHADO 7

**Precisa checar:** `TabelaProcessos.vue` exibe `prazoEtapaAtual` ou `dataLimite` dos processos no painel?
Se sim, alterar data limite e voltar ao painel sem invalidar o cache mostraria a data antiga.

