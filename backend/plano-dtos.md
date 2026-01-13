# Plano de Padronização de DTOs

> Documento de acompanhamento da refatoração de DTOs do projeto SGC.
> Última atualização: 2026-01-13T14:38

## Status Geral

| Fase | Descrição | Status |
|------|-----------|--------|
| 1 | Renomear `*Req` → `*Request` | 🔄 Em Progresso |
| 2 | Padronizar anotações Lombok | ⏳ Pendente |
| 3 | Separar DTOs bidirecionais | ⏳ Pendente |
| 4 | Converter para records | ⏳ Pendente |

---

## Fase 1: Renomear `*Req` → `*Request`

### Módulo `processo`
- [ ] `CriarProcessoReq` → `CriarProcessoRequest`
- [ ] `AtualizarProcessoReq` → `AtualizarProcessoRequest`
- [ ] `IniciarProcessoReq` → `IniciarProcessoRequest`
- [ ] `EnviarLembreteReq` → `EnviarLembreteRequest`

### Módulo `subprocesso`
- [ ] `DisponibilizarMapaReq` → `DisponibilizarMapaRequest`
- [ ] `DevolverCadastroReq` → `DevolverCadastroRequest`
- [ ] `DevolverValidacaoReq` → `DevolverValidacaoRequest`
- [ ] `AceitarCadastroReq` → `AceitarCadastroRequest`
- [ ] `HomologarCadastroReq` → `HomologarCadastroRequest`
- [ ] `ApresentarSugestoesReq` → `ApresentarSugestoesRequest`
- [ ] `CompetenciaReq` → `CompetenciaRequest`
- [ ] `ImportarAtividadesReq` → `ImportarAtividadesRequest`
- [ ] `ReabrirProcessoReq` → `ReabrirProcessoRequest`
- [ ] `SalvarAjustesReq` → `SalvarAjustesRequest`
- [ ] `SubmeterMapaAjustadoReq` → `SubmeterMapaAjustadoRequest`

### Módulo `seguranca`
- [ ] `AutenticarReq` → `AutenticarRequest`
- [ ] `EntrarReq` → `EntrarRequest`

### Módulo `organizacao`
- [ ] `CriarAtribuicaoTemporariaReq` → `CriarAtribuicaoTemporariaRequest`

---

## Fase 2: Padronizar Anotações Lombok

Padrão alvo:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExemploRequest { ... }
```

- [ ] Verificar e ajustar anotações após Fase 1

---

## Fase 3: Separar DTOs Bidirecionais

- [ ] Avaliar `SubprocessoDto` (tem `@NotNull` mas é usado como resposta)
- [ ] Outros DTOs identificados durante execução

---

## Fase 4: Converter para Records

- [ ] Identificar DTOs candidatos (sem lógica customizada)
- [ ] Converter seguindo padrão de `CriarAnaliseRequest`

---

## Convenção Final (a ser adicionada ao AGENTS.md)

| Tipo | Sufixo | Estrutura | Uso |
|------|--------|-----------|-----|
| Request API | `*Request` | record ou class | Entrada de Controllers |
| Response API | `*Response` | record ou class | Saída de Controllers |
| Comando interno | `*Command` | record | Chamadas entre serviços |
| DTO genérico | `*Dto` | class | Mapeamento de entidades |
