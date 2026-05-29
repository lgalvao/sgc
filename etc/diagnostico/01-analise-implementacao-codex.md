# Relatório 01 — Análise das Mudanças Implementadas pelo Codex

> **Data de análise:** 2026-05-29  
> **Referência:** Commits recentes relacionados a `sgc.diagnostico` e plano em `etc/diagnostico/plano-diagnostico.md`

---

## 1. Resumo Executivo

O agente Codex criou o módulo backend `sgc.diagnostico` do zero, cobrindo as lacunas estruturais identificadas no
plano de implementação (seção 2.3 do `plano-diagnostico.md`). A entrega inclui entidades JPA, repositórios,
services, DTOs e um controller REST completo. As permissões de diagnóstico foram adicionadas ao enum central
`AcaoPermissao`. Tipos de transição e notificação específicos do diagnóstico foram inseridos nos enums `TipoTransicao`
e `TipoNotificacao`. No frontend, os cards já navegam para rotas de diagnóstico (`AutoavaliacaoDiagnostico`,
`OcupacoesCriticasDiagnostico`, `MonitoramentoDiagnostico`), mas **essas rotas e views ainda não existem**.

---

## 2. Arquivos Criados (novos)

### 2.1 Módulo `sgc/diagnostico` (Backend)

| Arquivo | Tamanho | Papel |
|---|---|---|
| `DiagnosticoController.java` | 159 linhas | Expõe 13 endpoints REST sob `/api/diagnosticos` |
| `model/Diagnostico.java` | 43 linhas | Entidade JPA — agrega avaliações e ocupações críticas |
| `model/AvaliacaoServidor.java` | 50 linhas | Entidade JPA — avaliação de competência por servidor |
| `model/OcupacaoCritica.java` | 39 linhas | Entidade JPA — ocupação crítica por servidor e competência |
| `model/DiagnosticoRepo.java` | 23 linhas | Repositório JPA — busca por subprocesso |
| `model/AvaliacaoServidorRepo.java` | 30 linhas | Repositório JPA — busca por diagnóstico e servidor |
| `model/OcupacaoCriticaRepo.java` | 22 linhas | Repositório JPA — busca por diagnóstico |
| `model/SituacaoDiagnostico.java` | 4 valores | Enum: `EM_ANDAMENTO`, `CONCLUIDO`, `VALIDADO`, `HOMOLOGADO` |
| `model/SituacaoAvaliacaoServidor.java` | 5 valores | Enum: estados da avaliação individual do servidor |
| `model/SituacaoCapacitacao.java` | 5 valores | Enum: `NA`, `AC`, `EC`, `C`, `I` |
| `model/DiagnosticoViews.java` | 8 linhas | Interfaces de view Jackson (`Publica`, `Detalhada`) |
| `service/DiagnosticoAvaliacaoService.java` | 150 linhas | Autoavaliação, consenso, aprovação, impossibilidade, ocupações críticas |
| `service/DiagnosticoConsultaService.java` | 153 linhas | Consultas: contexto, autoavaliação, consenso, equipe, unidade |
| `service/DiagnosticoFluxoService.java` | 198 linhas | Fluxo: concluir, devolver, validar, homologar |
| `service/DiagnosticoGapService.java` | 18 linhas | Cálculo de gap = importância − domínio |
| `service/DiagnosticoNotificacaoService.java` | 255 linhas | E-mails e alertas para todos os eventos do fluxo |
| `service/DiagnosticoValidacaoService.java` | 39 linhas | Validação de autoavaliação completa e conclusão de unidade |
| `dto/AutoavaliacaoDto.java` | — | Resposta: competências + situação do servidor |
| `dto/AutoavaliacaoRequest.java` | — | Requisição: lista de competências com importância/domínio |
| `dto/AvaliacaoCompetenciaDto.java` | — | Item: código da competência + importância + domínio |
| `dto/CompetenciaResumoDto.java` | — | Resumo: código + descrição |
| `dto/ConsensoDto.java` | — | Resposta: competências do consenso + situação |
| `dto/ConsensoRequest.java` | — | Requisição: competências + motivo de reabertura |
| `dto/DiagnosticoContextoDto.java` | — | Contexto do subprocesso: processo, unidade, situação, competências |
| `dto/DiagnosticoEquipeDto.java` | — | Equipe: lista de servidores com situação |
| `dto/DiagnosticoMonitoramentoDto.java` | — | Monitoramento: lista de unidades com situação e localização |
| `dto/DiagnosticoUnidadeDto.java` | — | Detalhamento da unidade: servidores, ocupações, movimentações |
| `dto/JustificativaRequest.java` | — | Requisição simples com justificativa obrigatória |
| `dto/OcupacaoCriticaDto.java` | — | Item: competência + servidor + situação de capacitação |
| `dto/OcupacoesCriticasRequest.java` | — | Requisição: lista de ocupações críticas |
| `dto/ServidorDiagnosticoDto.java` | — | Servidor: título, nome, situação, competências de consenso |
| `dto/UnidadeResumoDto.java` | — | Unidade: código, sigla, nome, situação |
| `dto/ValidarDiagnosticosEmBlocoRequest.java` | — | Requisição: lista de subprocessos para validação em bloco |

### 2.2 Tipo de diagnóstico no frontend

| Arquivo | Localização |
|---|---|
| `types/diagnostico.ts` | Tipos `DiagnosticoOrganizacional` e `GrupoViolacaoOrganizacional` (diagnóstico organizacional, não de competências) |

---

## 3. Arquivos Modificados

### 3.1 Backend

#### `sgc/seguranca/AcaoPermissao.java`
- **Adicionadas** 7 novas ações de permissão para o fluxo de diagnóstico:
  ```
  VISUALIZAR_DIAGNOSTICO  → LEITURA  (todos os perfis)
  PREENCHER_AUTOAVALIACAO → ESCRITA  (SERVIDOR, CHEFE)
  CRIAR_CONSENSO          → ESCRITA  (CHEFE)
  CONCLUIR_DIAGNOSTICO    → ESCRITA  (CHEFE)
  VALIDAR_DIAGNOSTICO     → ESCRITA  (GESTOR)
  DEVOLVER_DIAGNOSTICO    → ESCRITA  (ADMIN, GESTOR)
  HOMOLOGAR_DIAGNOSTICO   → ESCRITA  (ADMIN)
  ```

#### `sgc/subprocesso/model/TipoAcaoAnalise.java`
- **Adicionados** dois valores: `ACEITE_DIAGNOSTICO` e `DEVOLUCAO_DIAGNOSTICO`

#### `sgc/subprocesso/model/TipoTransicao.java`
- **Adicionados** 4 valores de transição de diagnóstico:
  - `DIAGNOSTICO_CONCLUIDO`, `DIAGNOSTICO_DEVOLVIDO`, `DIAGNOSTICO_ACEITO`, `DIAGNOSTICO_HOMOLOGADO`
  - Todos com templates `null` (sem geração de alerta ou email inline — notificações via `DiagnosticoNotificacaoService`)

#### `sgc/comum/Mensagens.java`
- **Adicionadas** 4 constantes de histórico de movimentação:
  - `HIST_DIAGNOSTICO_CONCLUIDO`, `HIST_DIAGNOSTICO_DEVOLVIDO`, `HIST_DIAGNOSTICO_ACEITO`, `HIST_DIAGNOSTICO_HOMOLOGADO`

#### `sgc/alerta/model/TipoNotificacao.java`
- **Adicionados** 4 tipos de notificação:
  - `DIAGNOSTICO_CONCLUIDO`, `DIAGNOSTICO_DEVOLVIDO`, `DIAGNOSTICO_ACEITO`, `DIAGNOSTICO_HOMOLOGADO`

#### `sgc/alerta/AssuntosNotificacao.java`
- Suporte ao tipo `DIAGNOSTICO` no método `inicioProcesso()` — traduz para "diagnóstico" em português

#### `sgc/subprocesso/model/SituacaoSubprocesso.java`
- Já possuía `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`, `DIAGNOSTICO_MONITORAMENTO`, `DIAGNOSTICO_CONCLUIDO`
- O método `transicaoDiagnostico()` já estava presente e define: `AUTOAVALIACAO → MONITORAMENTO | CONCLUIDO`, `CONCLUIDO → AUTOAVALIACAO` (devolução)

#### `sgc/seguranca/dto/PermissoesSessaoResponse.java`
- Campo `mostrarDiagnosticoOrganizacional` já presente (para diagnóstico organizacional)

#### Schema SQL (`schema.sql`)
- Tabelas `diagnostico`, `avaliacao_servidor`, `ocupacao_critica` com constraints de FK e índices já existentes

### 3.2 Frontend

#### `components/processo/SubprocessoCards.vue`
- **Adicionados** 3 cards de diagnóstico que navegam para:
  - `AutoavaliacaoDiagnostico` — card "Autoavaliação"
  - `OcupacoesCriticasDiagnostico` — card "Ocupações Críticas"
  - `MonitoramentoDiagnostico` — card "Monitoramento"
- Lógica de navegação separada (`navegarParaDiag`) usando params `codSubprocesso` e `siglaUnidade`

#### `types/tipos.ts`
- Exporta `./diagnostico` (mas o arquivo `diagnostico.ts` refere-se ao diagnóstico organizacional, não ao módulo de competências)

---

## 4. Análise de Qualidade da Implementação

### ✅ Acertos

1. **Arquitetura bem separada** — Controller, Services (consulta, avaliação, fluxo, gap, notificação, validação) e
   Repositórios com responsabilidades claras e coesas.
2. **Permissões alinhadas** — `@PreAuthorize` em todos os endpoints com ações granulares no `AcaoPermissao`.
3. **Notificações completas** — `DiagnosticoNotificacaoService` cobre todos os eventos (autoavaliação concluída,
   consenso criado/aprovado, concluído, devolvido, aceito, homologado) com idempotência por chave.
4. **Cálculo de gap** — Simples e correto: `importancia - dominio`. Anulado quando qualquer campo é nulo/zero.
5. **Validação de conclusão** — Bloqueia unidade enquanto há servidores pendentes ou ocupações não preenchidas.
6. **Rastreabilidade** — Usa `SubprocessoTransicaoService` e `MovimentacaoRepo`, integrado ao histórico existente.
7. **Reabertura de consenso** — Verifica se o servidor já aprovou e exige motivo de reabertura.

### ⚠️ Pontos de Atenção

1. **`DiagnosticoMonitoramentoDto` sem endpoint** — O DTO existe mas não há endpoint no controller que o retorne.
   O método de monitoramento agregado (visualizar todas as unidades do processo) está ausente.

2. **`ValidarDiagnosticosEmBlocoRequest` sem endpoint** — O DTO de validação em bloco existe mas não há endpoint
   correspondente no controller — o `plano-diagnostico.md` (seção 4.5) previa "validar em bloco para o gestor".

3. **`validarDiagnostico` não avança a unidade superior** — Ao validar, o código define `diagnostico.situacao = VALIDADO`
   e muda a localização do subprocesso para a unidade superior, mas não faz nenhuma verificação se *todos* os
   subprocessos da unidade superior já foram validados para que a homologação possa ocorrer.

4. **Gap negativo não tratado** — A fórmula `importancia - dominio` pode produzir valores negativos (domínio >
   importância). Não há validação de escala ou regra de clampagem.

5. **`DiagnosticoRepo.buscarPorSubprocessoComRelacionamentos`** — Método com LEFT JOIN FETCH de duas coleções,
   o que pode gerar resultados duplicados em JPQL. `findBySubprocessoCodigo` (sem fetch) é o que está sendo
   usado nos services, então o método com JOIN FETCH não é utilizado e pode causar confusão.

6. **`AvaliacaoServidorRepo` extends `JpaRepository<AvaliacaoServidor, Integer>`** — O tipo da PK é `Integer`,
   mas `EntidadeBase` usa `Long` como tipo da chave. Inconsistência que pode gerar erro em runtime.

7. **Ausência de criação do `Diagnostico` na iniciação** — O `ProcessoService` já inicia subprocessos de diagnóstico
   em `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`, mas não há evidência de que o registro `Diagnostico` (e as
   `AvaliacaoServidor` iniciais) seja criado automaticamente. Se o `Diagnostico` não existe, todos os endpoints
   retornarão 404.

8. **Notificação de autoavaliação** — O método `notificarAutoavaliacaoConcluida` usa `alertaFacade.criarAlertaTransicao`
   com `unidadeOrigem == unidadeDestino`, o que pode criar alertas sem remetente claro.

---

## 5. Cobertura em Relação ao Plano

| Etapa do plano (`plano-diagnostico.md`) | Status |
|---|---|
| 4.2 — Criar módulo backend `sgc.diagnostico` | ✅ Implementado |
| 4.4 — Autoavaliação do servidor | ✅ Parcialmente (falta regra C13) |
| 4.4 — Consenso pela chefia | ✅ Implementado |
| 4.4 — Aprovação do consenso pelo servidor | ✅ Implementado |
| 4.4 — Impossibilidade de avaliação | ✅ Implementado |
| 4.5 — Ocupações críticas | ✅ Implementado (salvamento) |
| 4.5 — Bloqueio de conclusão | ✅ Implementado |
| 4.5 — Conclusão da unidade com notificação | ✅ Implementado |
| 4.5 — Validar, devolver e validar em bloco | ⚠️ Validar e devolver implementados; **bloco ausente** |
| 4.6 — Ajustar segurança e permissões | ✅ Implementado |
| 4.3 — Integrar ao ciclo processo/subprocesso | ⚠️ Transições implementadas; **criação do Diagnostico na iniciação não verificada** |
| 4.7 — Frontend do diagnóstico | ❌ Não implementado (apenas cards com navegação) |
| 4.8 — Relatórios e cálculos | ❌ Não implementado |
| 4.9 — Testes | ❌ Não implementado |

---

## 6. Conclusão

O Codex entregou a **espinha dorsal do backend de diagnóstico** com boa cobertura dos fluxos principais. Os pontos
críticos que precisam de atenção antes de avançar para o frontend são: (a) garantir a criação automática do
`Diagnostico` e das `AvaliacaoServidor` na iniciação do processo, (b) adicionar o endpoint de monitoramento agregado
e (c) corrigir a inconsistência do tipo de chave no `AvaliacaoServidorRepo`. O frontend de diagnóstico segue
completamente por implementar.
