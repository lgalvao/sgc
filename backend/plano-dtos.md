# Plano de Refatoração de DTOs

> Documento de acompanhamento da refatoração de DTOs do projeto SGC.
> Para regras e convenções, consulte [regras-dtos.md](./regras-dtos.md).
> Última atualização: 2026-01-15T14:09

---

## Status Geral

| Fase | Descrição | Status |
|------|-----------|--------|
| 1 | Eliminar duplicatas Req/Request | ✅ Completo |
| 2 | Padronizar anotações Lombok | ✅ Completo |
| 3 | Corrigir sufixos inconsistentes (Resp→Response) | ✅ Completo |
| 4 | Separar DTOs bidirecionais (SubprocessoDto) | ✅ Completo |
| 5 | Remover validação de Response DTOs | ✅ Completo |
| 6 | Converter para records | ✅ Completo |
| 7 | Atualizar AGENTS.md | ✅ Completo |

---

## 🎉 Refatoração Concluída!

Todas as fases da refatoração de DTOs foram concluídas com sucesso.

### Referências

- **Regras e Convenções**: [regras-dtos.md](./regras-dtos.md)
- **Guia de Desenvolvimento**: [AGENTS.md](/AGENTS.md) (seção ADR-004)

---

## Fases Completas (Referência)

<details>
<summary>Fase 1: Eliminar Duplicatas Req/Request ✅</summary>

**Impacto:**
- 7 arquivos deletados
- 14 DTOs renomeados (Req → Request)
- 60+ arquivos atualizados
- 174 testes passando

**Módulos:**
| Módulo | Ação |
|--------|------|
| processo | 4 DTOs deletados |
| analise | 2 deletados, 2 mantidos |
| subprocesso | 11 renomeados, 1 deletado |
| seguranca | 2 renomeados |
| organizacao | 1 renomeado |

</details>

<details>
<summary>Fase 2: Padronizar Anotações Lombok ✅</summary>

**Impacto:**
- 5 DTOs migrados de `@Getter/@Setter` para `@Data`
- Removidas cópias defensivas de `ImpactoMapaDto`

**DTOs Refatorados:**
- `DisponibilizarMapaRequest`
- `PerfilUnidadeDto`
- `ProcessoDto`
- `SubprocessoDto`
- `ImpactoMapaDto`

</details>

<details>
<summary>Fase 3: Corrigir Sufixos (Resp→Response) ✅</summary>

**Impacto:**
- 3 arquivos criados, 3 deletados
- 8 arquivos atualizados

**Renomeações:**
| Antes | Depois |
|-------|--------|
| `EntrarResp` | `EntrarResponse` |
| `AtividadeOperacaoResp` | `AtividadeOperacaoResponse` |
| `RespostaDto` | `MensagemResponse` |

</details>

<details>
<summary>Fase 4: Separar DTOs Bidirecionais ✅</summary>

**Impacto:**
- 2 novos DTOs criados
- 5 arquivos de produção atualizados
- 3 arquivos de teste atualizados

**Estrutura Final:**
| DTO | Propósito |
|-----|-----------|
| `CriarSubprocessoRequest` | Entrada para criação |
| `AtualizarSubprocessoRequest` | Entrada para atualização |
| `SubprocessoDto` | Response (sem validação) |

</details>

<details>
<summary>Fase 5: Remover Validação de Response DTOs ✅</summary>

**Impacto:**
- 4 DTOs atualizados
- Validação removida de DTOs usados como Response

**DTOs Corrigidos:**
| DTO | Validação Removida |
|-----|-------------------|
| `MapaAjusteDto` | `@NotNull`, `@NotBlank`, `@Valid` |
| `CompetenciaAjusteDto` | `@NotNull`, `@NotBlank`, `@Valid` |
| `AtividadeAjusteDto` | `@NotNull`, `@NotBlank`, `@Valid` |
| `ConhecimentoAjusteDto` | `@NotNull`, `@NotBlank` |

</details>

<details>
<summary>Fase 6: Converter para Records ✅</summary>

**Impacto:**
- 8 DTOs convertidos de class para record
- Benefícios: imutabilidade, menos boilerplate, toString/equals/hashCode automáticos

**DTOs Convertidos:**
| DTO | Anotações |
|-----|-----------|
| `ErroValidacaoDto` | `@Builder` |
| `ValidacaoCadastroDto` | `@Builder` |
| `ProcessoContextoDto` | `@Builder` |
| `ContextoEdicaoDto` | `@Builder` |
| `SubprocessoPermissoesDto` | `@Builder` |
| `SubprocessoSituacaoDto` | `@Builder` |
| `ProcessoResumoDto` | `@Builder` |
| `MovimentacaoDto` | `@Builder` |

</details>

---

## Log de Alterações

### 2026-01-15T14:01 - Fase 6 Completa
- 8 DTOs convertidos para records
- DTOs simples sem métodos de negócio migrados
- `@Builder` mantido para compatibilidade

### 2026-01-15T13:52 - Fase 5 Completa
- Removida validação de `MapaAjusteDto`, `CompetenciaAjusteDto`, `AtividadeAjusteDto`, `ConhecimentoAjusteDto`
- Esses DTOs são usados como Response (`obterMapaParaAjuste`) - validação não faz sentido
- Validação mantida apenas em `SalvarAjustesRequest` que recebe os dados

### 2026-01-15T13:47 - Reorganização do Documento
- Regras e convenções movidas para `regras-dtos.md`
- Plano focado apenas em execução

### 2026-01-15T13:42 - Fase 4 Completa
- `CriarSubprocessoRequest` e `AtualizarSubprocessoRequest` criados
- `SubprocessoDto` convertido para apenas Response
- Atualizados: Controller, Facade, Service e 3 testes

### 2026-01-15T13:30 - Fase 3 Completa
- `EntrarResp` → `EntrarResponse`
- `AtividadeOperacaoResp` → `AtividadeOperacaoResponse`
- `RespostaDto` → `MensagemResponse`

### 2026-01-14 - Fases 1 e 2 Completas
- Eliminadas duplicatas Req/Request
- Padronizadas anotações Lombok para `@Data`
