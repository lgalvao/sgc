# Relatório de Defensividade — SGC

> Gerado em 2026-05-07. Cobre backend Java e frontend TypeScript/Vue.

---

## Resumo executivo

O codebase apresenta **83 try/catch no frontend** e **29 no backend**. A maioria dos blocos no backend é
justificada por necessidade técnica real (agendadores, exceções checadas, idempotência). No frontend, porém,
há um padrão recorrente de **engolir erros silenciosamente nas stores**, retornando valores padrão (`[]`,
`null`, `false`) sem propagar o problema ao chamador nem ao usuário. Isso cria uma falsa ilusão de sucesso,
impede retry e fragmenta o tratamento de erros em vez de centralizá-lo.

---

## Backend

### Blocos JUSTIFICADOS (22 catch clauses — todos necessários)

| Arquivo | Exceção capturada | Motivo técnico |
|---|---|---|
| `NotificacaoWorker.processar()` | `Exception` | Agendador não pode falhar; marca notificação com falha |
| `AgendadorRefreshCache.atualizarTudo()` | `Exception` | Agendador de cache não pode derrubar a aplicação |
| `CacheAquecimento.onApplicationEvent()` | `Exception` | Evento de startup; falha não pode impedir o boot |
| `RegistroSseEmitter` | `IOException` | Conexão SSE pode fechar a qualquer momento |
| `NotificacaoService.enfileirar()` | `DataIntegrityViolationException` | Race condition de idempotência: re-busca o registro existente |
| `AlertaFacade.marcarAlertasComoLidos()` | `DataIntegrityViolationException` | Mesma race condition; objetivo já foi atingido por outra thread |
| `SubprocessoNotificacaoService.notificarResponsavelPessoal()` | `ErroEntidadeNaoEncontrada` | Responsável opcional; ausência não deve impedir o e-mail |
| `SubprocessoNotificacaoService.executarAlertaSemInterromperEmail()` | `RuntimeException` | Alerta é efeito colateral; falha não deve cancelar o e-mail |
| `ResponsavelUnidadeService.criarAlertaSemInterromperNotificacao()` | `RuntimeException` | Mesmo padrão: alerta é acessório ao fluxo principal |
| `RelatorioFacade.gerarRelatorio*()` x2 | `DocumentException \| IOException` | iText usa exceções checadas; wrapping em `RuntimeException` é obrigatório |
| `EmailService.enviarEmailHtml()` | `MessagingException \| UnsupportedEncodingException` | JavaMail usa exceções checadas |
| `ComumRepo.buscar()` x3 | `NoResultException` | Converte exceção JPA para domínio (`ErroEntidadeNaoEncontrada`) |
| `ValidadorDadosOrganizacionais.tentarMapearPerfil()` | `IllegalArgumentException` | Enum inválido em dados importados; coleta `PerfilInvalido` sem interromper |
| `SgcPermissionEvaluator.resolverAcao()` | `IllegalArgumentException` | Relança com mensagem de diagnóstico mais clara |
| `FeedbackController.registrar()` | `JacksonException` | Parsing de JSON em multipart exige tratamento explícito |
| `FeedbackService.obterScreenshot()` | `IOException` | `Files.readAllBytes` pode falhar; converte em `ErroInconsistenciaInterna` |
| `FeedbackService.salvarScreenshot()` | `IOException` | Salvar screenshot é opcional; falha silenciosa retorna `null` |
| `FeedbackService.serializarMetadados()` | `Exception` | Metadados são acessórios; falha não deve impedir o registro |
| `UsuarioFacade.buscarPerfisUsuario()` | `ErroEntidadeNaoEncontrada` | Converte para `ErroInconsistenciaInterna` com contexto; aceitável |
| `GerenciadorJwt` | `JwtException \| IllegalArgumentException` | Validação de JWT |
| `ClienteAcessoAd` | `ErroAutenticacao \| Exception` | Integração LDAP |
| `LoginFacade` | `ErroAutenticacao` | Fluxo de login |

**Conclusão backend:** nenhum bloco catch precisa ser removido. A fragmentação no backend é
**necessária e adequada** — cada catch está isolando uma fronteira real (agendador, I/O, race condition,
exceção checada de API externa).

---

## Frontend

### Padrões problemáticos encontrados

#### 1. Stores que engoliam erros e falsificavam estado (CRÍTICO)

**`stores/historico.ts` — `garantirDados()`**

```ts
// ANTES (problema)
} catch (erro) {
    logger.error("Erro ao carregar histórico:", erro);
    processos.value = [];
    carregado.value = true; // ← BUG: marca como carregado mesmo em falha
}
```

Consequências:
- `dadosValidos()` retorna `true` após uma falha, impedindo qualquer retry.
- O componente `HistoricoView` exibe tabela vazia sem nenhuma indicação de erro.
- O próprio teste anotava: `// O código define como true mesmo no erro`.

**`stores/unidade.ts` — `garantirArvoreElegibilidade()`**

```ts
// ANTES (problema)
} catch (error) {
    logger.error(`Erro ao buscar árvore de unidades (${key}):`, error);
    return []; // ← retorna lista vazia; chamador não sabe que houve falha
}
```

Consequências:
- `ProcessoCadastroView` exibia campo de unidades vazio sem avisar o usuário.
- O formulário ficava tecnicamente válido com zero unidades, podendo levar a submissões incorretas.
- Nenhum retry era possível (o cache permanecia vazio mas "preenchido").

#### 2. Stores com try/catch-log-rethrow inútil (RUÍDO)

**`stores/mapas.ts` — `garantirMapaCompleto()` e `garantirImpactoMapa()`**

```ts
// ANTES (inútil)
try {
    const mapa = await serviceObterMapaCompleto(codigoSubprocesso);
    definirMapaCompleto(codigoSubprocesso, mapa);
    return mapa;
} catch (erro) {
    logger.error(`Erro ao carregar mapa...`, erro);
    throw erro; // ← relança de qualquer forma
} finally {
    carregamentosMapa.delete(codigoSubprocesso);
}
```

O catch não altera o fluxo. O erro é logado pelo catch e relançado, então o chamador
(`useMapas.ts` via `executarSilencioso`) também loga. Dupla entrada de log, zero valor.

#### 3. Try/catch ao redor de navegação de rota (RUÍDO)

**`views/ProcessoDetalheView.vue` — `abrirDetalhesUnidade()`**

```ts
// ANTES (defensivo sem ganho)
try {
    await router.push({...});
} catch (error) {
    logger.error(`Erro ao navegar...`, error);
}
```

Vue Router 4 não lança exceções para falhas de navegação normais (guardas que bloqueiam, rota
inexistente etc.). Retorna `NavigationFailure` em vez de rejeitar. O catch só pegaria erros de
guardas que explicitamente lançam — um cenário improvável que não precisa de tratamento silencioso.

#### 4. Try/catch aninhado para scroll DOM (SOBRE-ENGENHARIA)

**`views/cadastroDisponibilizacao.ts` — dentro de `disponibilizarCadastro()`**

```ts
await nextTick();
try {
    scrollParaPrimeiroErro(); // ← scroll DOM
} catch (erroDom) {
    logger.warn("Falha ao executar scroll para erro", erroDom);
}
```

`scrollIntoView()` não lança exceções em circunstâncias normais. O try/catch interno
fragmenta visualmente o fluxo sem proteger nada real.

#### 5. Duplo-wrapping com retorno booleano (FRAGMENTAÇÃO DESNECESSÁRIA)

**`composables/useMapaSugestoes.ts` — `carregarSugestoesParaVisualizacao()` + `verSugestoes()`**

```ts
// ANTES: carregarSugestoesParaVisualizacao retorna boolean de sucesso
async function carregarSugestoesParaVisualizacao() {
    try {
        sugestoesVisualizacao.value = await sincronizarSugestoesMapa();
        return true;
    } catch (error) {
        logger.error(error);
        notify(TEXTOS.mapa.ERRO_SUGESTOES, 'danger');
        return false;
    }
}

// verSugestoes verifica o boolean para decidir abrir o modal
const carregou = await carregarSugestoesParaVisualizacao();
if (carregou) { mostrarModalVerSugestoes.value = true; }
```

A função intermediária existe apenas para esconder um `try/catch` e retornar um booleano que
o chamador precisa verificar. Cria uma ramificação extra que pode ser eliminada inlining o
tratamento diretamente em `verSugestoes`.

#### 6. Feedback ausente ao usuário em carregamento crítico

**`views/ProcessoCadastroView.vue` — `buscarUnidadesParaProcesso()`**

```ts
// ANTES: erro logado, usuário não vê nada
} catch (error) {
    logger.error("Erro ao buscar unidades:", error);
}
```

Quando o carregamento de unidades falha, o formulário fica com a lista de unidades vazia.
O usuário não recebe nenhum alerta. Isso combina com o bug da store de unidades (item 1).

---

### Blocos catch JUSTIFICADOS no frontend (permanecem)

| Local | Motivo |
|---|---|
| `axios-setup.ts` | Interceptor central de erros HTTP — ponto de centralização |
| `useAsyncAction.ts` / `useErrorHandler.ts` | Utilitários de tratamento para composables |
| `useFeedback.ts` | Envio de feedback é non-critical; falha silenciosa aceitável |
| `stores/perfil.ts` — `logout()` | Falha no logout remoto não deve impedir limpeza local |
| `stores/processo.ts` — `garantirContextoCompleto()` | Trata cancelamento HTTP (not propagate navigation cancel) |
| `stores/subprocesso/orquestrador.ts` | Registra erro em ref + retorna null — padrão de contexto explícito |
| `stores/mapas.ts` — chamador via `useMapas.ts` / `executarSilencioso` | O chamador decide silenciar |
| `stores/organizacao.ts` — `carregarDiagnostico()` | Seta `erroDiagnostico` com mensagem visível ao usuário |
| `stores/unidade.ts` — `obterUnidade()` / `obterReferenciaMapaVigente()` | Não têm catch — propagam corretamente |
| `views/AdministradoresView.vue` | Erros inline em modais — correto |
| `views/AtribuicaoTemporariaView.vue` | Erros inline no formulário — correto |
| `views/NotificacoesAdminView.vue` — `carregar()` | Seta `erro` visível na tela — correto |
| `views/NotificacoesAdminView.vue` — `reenviar()` | Notifica usuário — correto |
| `views/NotificacoesAdminView.vue` — `carregarUrlLeitorEmailTestes()` | URL de dev; falha silenciosa aceitável |
| `views/FeedbacksAdminView.vue` — `carregar()` | Seta `erro` visível na tela — correto |
| `views/LimpezaProcessosView.vue` — `confirmarExclusao()` | Notifica usuário — correto |
| `views/LoginView.vue` | Login tem ramificação de tipo de erro legítima |
| `views/ProcessoCadastroView.vue` — ações de workflow | Usam `handleApiErrors` com feedback estruturado — correto |
| `views/processoDetalheAcoes.ts` | Notificam usuário via `notify` — correto |
| `composables/useMapaCompetenciasMutacoes.ts` | Estado de erro em modais — correto |
| `composables/useCadastroAtividadesMutacoes.ts` | Notifica usuário — correto |
| `composables/useBuscadorUsuarios.ts` | Limpa resultados + notifica — correto |
| `composables/useMapaOrquestracao.ts` | Retorna `false` (falha explícita) e loga — correto |
| `views/mapaDisponibilizacao.ts` | Aplica `erroNormalizado` visível — correto |
| `views/cadastroDisponibilizacao.ts` — `confirmarDisponibilizacao()` | Usa `try...finally` sem swallow — correto |

---

## Mudanças realizadas nesta rodada

1. **`stores/mapas.ts`** — Removidos dois blocos try/catch-log-rethrow em `garantirMapaCompleto` e
   `garantirImpactoMapa`. Cleanup do map de carregamentos movido para `.finally()` na promise diretamente.

2. **`stores/historico.ts`** — Removido catch que engolia o erro e marcava `carregado = true` em falha.
   Agora o erro propaga; o estado permanece `carregado = false`, permitindo retry na próxima navegação.

3. **`stores/unidade.ts`** — Removido catch que retornava `[]` silenciosamente em `garantirArvoreElegibilidade`.
   Erro propaga para o chamador.

4. **`views/ProcessoCadastroView.vue`** — `buscarUnidadesParaProcesso()` agora notifica o usuário quando o
   carregamento de unidades falha (em vez de engolir silenciosamente).

5. **`views/ProcessoDetalheView.vue`** — Removido try/catch em `abrirDetalhesUnidade()` ao redor de
   `router.push()`.

6. **`views/cadastroDisponibilizacao.ts`** — Removido try/catch DOM aninhado para `scrollParaPrimeiroErro()`.

7. **`composables/useMapaSugestoes.ts`** — Inlineado o tratamento de erro em `verSugestoes()`;
   removida a função intermediária `carregarSugestoesParaVisualizacao()` da API pública.

8. **`constants/textos.ts`** — Adicionado `ERRO_CARREGAR_UNIDADES` em `processo.cadastro`.

9. **Testes** — Atualizados `stores/__tests__/historico.spec.ts` e `stores/__tests__/unidade.spec.ts`
   para refletir o comportamento correto (propagação em vez de swallow).

---

## Próximos alvos naturais

- `stores/organizacao.ts` — `carregarDiagnostico()` seta `erroDiagnostico` mas não propaga; considerar
  um único ponto de exibição de erro para o diagnóstico.
- `views/LoginView.vue` — o bloco catch tem ramificação por tipo de erro que poderia ser simplificada
  se o backend retornasse respostas mais semânticas para autenticação.
- `composables/useMapaOrquestracao.ts` — o catch que retorna `false` + loga poderia ser eliminado se
  o `garantirContextoPorProcessoEUnidade` propagasse corretamente o erro para o router.
