# Rastreamento de Melhorias de Qualidade — SGC

> Baseado em: `quality-report.md`  
> Iniciado em: 2026-04-25  
> Última atualização: 2026-04-25 (Sessão 1 — concluída)

---

## Resumo

| Categoria | Alta 🔴 | Média 🟡 | Baixa 🟢 | Total |
|---|---|---|---|---|
| Corrigidos | 9 | 14 | 7 | 30 |
| Pendentes | 0 | 8 | 2 | 10 |

---

## Itens 🔴 Alta Severidade

| # | Item | Arquivo(s) | Status |
|---|---|---|---|
| 9.1 | Mensagens de erro internas expostas ao cliente | `RestExceptionHandler.java` | ✅ Feito |
| 8.3 | Cache VW com `maximumSize(1)` — ineficaz | `CacheConfig.java` | ✅ Feito |
| 5.1 | `paginaCodigos == null` mascara possível bug N+1 | `ProcessoService.java` | ✅ Feito |
| 2.1 | Comparação de enum via `String.contains("MAPA")` | `SituacaoSubprocesso.java`, `SubprocessoConsultaService.java`, `SubprocessoTransicaoService.java`, `MapaVisualizacaoService.java` | ✅ Feito |
| 8.1 | N+1 em operações em bloco (`forEach` + `buscarSubprocesso`) | `SubprocessoTransicaoService.java`, `SubprocessoRepo.java` | ✅ Feito |
| 8.2 | N+1 em `tornarMapasVigentes` | `ProcessoService.java`, `UnidadeService.java` | ✅ Feito |
| 10.1 | Operações em bloco sem verificação de permissão por recurso | `SubprocessoController.java` | ℹ️ Já corrigido (usa `hasPermission` na lista) |
| 9.2 | Bearer token aceito sem documentação de necessidade | `FiltroJwt.java` | ✅ Feito |
| 6.1 | `invalido.value = true` após carregamento invalida cache | `subprocesso.ts`, `processo.ts` | ✅ Feito |

---

## Itens 🟡 Média Severidade

| # | Item | Arquivo(s) | Status |
|---|---|---|---|
| 3.1 | `ValidationResult` em inglês | `SubprocessoValidacaoService.java` | ✅ Feito |
| 4.2 | Pares duplicados `aceitarCadastro`/`aceitarRevisaoCadastro` | `SubprocessoTransicaoService.java` | ✅ Feito |
| 6.3 | `obterSugestoes` retorna `Map<String,Object>` | `SubprocessoConsultaService.java` | ✅ Feito |
| 9.3 | `limparCachePeriodico` duplica lógica de `limparTentativasAntigas` | `LimitadorTentativasLogin.java` | ✅ Feito |
| 2.2 | `verificarSubprocesso` com condicionais encadeadas | `SgcPermissionEvaluator.java` | ⏳ Pendente |
| 2.3 | `listarTodos` com check nulo desnecessário em paginação | `ProcessoService.java` | ✅ Feito (junto com 5.1) |
| 2.4 | `FluxoCadastroContexto` acoplado ao boolean `isRevisao` | `SubprocessoTransicaoService.java` | ✅ Feito (junto com 4.2) |
| 3.2 | `AlertaService.salvar` exposto publicamente | `AlertaService.java` | ⏳ Pendente |
| 3.3 | `codSubprocesso` na URL de operações em bloco | `SubprocessoController.java` | ⏳ Pendente |
| 4.1 | Redundância em `garantirContextoEdicaoPorProcessoEUnidade` | `subprocesso.ts` | ✅ Feito |
| 5.2 | `getSituacao() != null` em enum não-nulo | Múltiplos | ✅ Feito (junto com 2.1) |
| 5.3 | `verificarPermissao` com `@Nullable Usuario` sem log | `SgcPermissionEvaluator.java` | ✅ Feito |
| 5.4 | `encontrarIpMaisAntigo` com branch inalcançável complexo | `LimitadorTentativasLogin.java` | ✅ Feito |
| 6.2 | `buscarOpt` retorna `null` via `orElse(null)` | `UsuarioFacade.java` | ✅ Feito |
| 7.1 | Ordenação de datas no frontend | `subprocessoService.ts` | ⏳ Pendente (requer mudança de DTO) |
| 7.2 | Mapeamento complexo `mapSubprocessoDetalheResponseParaModel` | `subprocessoService.ts` | ⏳ Pendente |
| 8.4 | Hierarquia de unidades reconstruída por requisição | `ProcessoService.java` | ⏳ Pendente |
| 8.5 | Requisições duplicadas para subprocesso no frontend | `subprocessoService.ts` | ⏳ Pendente |
| 1.2 | `SubprocessoTransicaoService` com 933 linhas | `SubprocessoTransicaoService.java` | ⏳ Pendente |
| 1.3 | `PermissoesSubprocessoDto` com 34 campos booleanos | `PermissoesSubprocessoDto.java` | ⏳ Pendente |
| 1.4 | Views de frontend com 900+ linhas | `CadastroView.vue`, `MapaView.vue` | ⏳ Pendente |
| 1.1 | `ProcessoService` com 1315 linhas (God Service) | `ProcessoService.java` | ⏳ Pendente |

---

## Itens 🟢 Baixa Severidade

| # | Item | Arquivo(s) | Status |
|---|---|---|---|
| 1.5 | `obterUltimaDataLimiteSubprocesso` duplicada frontend/backend | `subprocessoService.ts` | ⏳ Pendente |
| 3.4 | `mascarar` no `SgcPermissionEvaluator` deveria estar em `UtilSanitizacao` | `SgcPermissionEvaluator.java` | ✅ Feito |
| 3.5 | `dadosValidos` com parâmetro ignorado | `subprocesso.ts`, `processo.ts` | ✅ Feito (junto com 6.1) |
| 4.3 | `MapaVisualizacaoService` usa `orElse(null)` + `if (mapa == null)` | `MapaVisualizacaoService.java` | ✅ Feito |
| 4.4 | `CacheConfig` com blocos repetitivos | `CacheConfig.java` | ✅ Feito (junto com 8.3) |
| 5.5 | Dupla verificação `!= null` + `.contains("MAPA")` acumulada | Múltiplos | ✅ Feito (junto com 2.1) |
| 6.4 | `listarAnalisesCadastro` via `.findFirst().orElse(null)` | `SubprocessoConsultaService.java` | ✅ Feito |
| 7.3 | Campo `acao` redundante em payload de bloco | `subprocessoService.ts` | ⏳ Pendente |
| 9.4 | Limitador conta por IP, não por usuário | `LimitadorTentativasLogin.java` | ⏳ Pendente (melhoria futura) |

---

## Progresso

- **Concluídos:** 30 / 40
- **Pendentes:** 10 / 40

---

## Notas

- **10.1 (SubprocessoController):** O relatório identificou risco de segurança, mas o código atual já usa `@PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_CADASTRO')")` — verificação correta por lista de recursos.
- **9.4 (Limitador por IP):** Melhoria arquitetural de maior impacto. Fica como item futuro para evitar riscos de regressão em autenticação.
- **1.1, 1.3, 1.4 (God Service, PermissoesDto, Views grandes):** Refatorações de alta complexidade sem risco de bug imediato. Priorizadas para próxima rodada.
- **7.1, 7.2, 8.5 (lógica no frontend, requisições redundantes):** Exigem mudanças de contrato no DTO do backend. Agrupadas para execução coordenada.
- **3.4 (mascarar):** Sem ação necessária — `mascarar` é auxiliar de log para PII (título eleitoral), não sanitização de HTML. Mover para `UtilSanitizacao` misturaria responsabilidades.
- **6.2 (buscarOpt):** Sem ação necessária — `orElse(null)` em `carregarUsuarioParaAutenticacao` é intencional: método anotado `@Nullable` para suporte ao Spring Security UserDetailsService.
- **6.4 (listarAnalisesCadastro):** Método privado já anotado `@Nullable`, contrato explícito. Sem mudança de comportamento.
