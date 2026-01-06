# Plano de Melhoria de Testabilidade - SGC Backend

**Data:** 2026-01-06  
**Baseado em:** Análise do BACKLOG_TESTABILIDADE.md e cobertura atual

---

## Métricas Atuais

| Métrica | Valor | Meta |
|---------|-------|------|
| Cobertura de Linhas | 98.75% | 98% ✅ |
| Cobertura de Branches | 87.03% | 90% ❌ |
| Total de Testes | 1007 | - |
| Branches Perdidos | 170/1311 | - |

---

## 📊 Análise de Gaps por Prioridade

### Prioridade Alta (Maior impacto em branches)

#### 1. SubprocessoMapaWorkflowService (~13 branches perdidos)

**Arquivo:** `sgc.subprocesso.service.SubprocessoMapaWorkflowService.java`  
**Branches perdidos:** Linhas 53, 57, 59, 79, 110, 123, 153, 216, 267, 294, 321

**Problema identificado:**

- Condicionais aninhadas para verificação de situação do subprocesso
- Verificações de `TipoProcesso` (MAPEAMENTO vs REVISAO) repetidas

**Ação proposta:**

```java
// Antes (múltiplos branches)
if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
    sp.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
} else {
    sp.setSituacao(REVISAO_MAPA_DISPONIBILIZADO);
}

// Depois (Strategy Pattern via Map)
private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_DISPONIBILIZADO = Map.of(
    TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_DISPONIBILIZADO,
    TipoProcesso.REVISAO, REVISAO_MAPA_DISPONIBILIZADO
);

sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
```

**Esforço:** 4h | **Impacto:** +3-4 branches

---

#### 2. EventoProcessoListener (~5 branches perdidos)

**Arquivo:** `sgc.notificacao.EventoProcessoListener.java`  
**Branches perdidos:** Linhas 122-123 (não executadas), 158, 245
**Linhas não cobertas:** 122, 123 (catch de exceção)

**Problema identificado:**

- Switch por `TipoUnidade` com branches não testados
- Catch de exceções não provocadas em testes

**Ação proposta:**

1. Criar teste que simula falha em `enviarEmailProcessoIniciado`
2. Extrair lógica de switch para método separado

```java
// Extrair para método testável
String criarCorpoEmailPorTipo(TipoUnidade tipoUnidade, Processo processo, Subprocesso sp) {
    return switch (tipoUnidade) {
        case OPERACIONAL, INTEROPERACIONAL -> ...;
        case INTERMEDIARIA -> ...;
        default -> throw new IllegalArgumentException(...);
    };
}
```

**Esforço:** 3h | **Impacto:** +2-3 branches

---

#### 3. SubprocessoValidacaoService (~8 branches perdidos)

**Arquivo:** `sgc.subprocesso.service.decomposed.SubprocessoValidacaoService.java`  
**Branches perdidos:** Linhas 46, 57, 61, 65, 77, 82, 131, 138

**Problema identificado:**

- Múltiplas verificações de null em cadeia (`a.getConhecimentos() == null || isEmpty()`)
- Branches negativos não testados

**Ação proposta:**

1. Adicionar testes para cenários edge-case:
   - Mapa null
   - Lista de atividades null
   - Atividade com conhecimentos null
2. Extrair validações para métodos dedicados:

```java
boolean atividadePossuiConhecimentos(Atividade atividade) {
    return atividade.getConhecimentos() != null && !atividade.getConhecimentos().isEmpty();
}
```

**Esforço:** 2h | **Impacto:** +4 branches

---

#### 4. SubprocessoDetalheService (~8 branches perdidos)

**Arquivo:** `sgc.subprocesso.service.decomposed.SubprocessoDetalheService.java`  
**Branches perdidos:** Linhas 89, 104, 105, 109, 112, 135, 137, 139

**Ação proposta:**

- Testes para cenários de dados incompletos (null checks)
- Simular subprocessos sem unidade superior, sem mapa, etc.

**Esforço:** 2h | **Impacto:** +4 branches

---

### Prioridade Média

#### 5. ProcessoController (3 linhas + 2 branches)

**Arquivo:** `sgc.processo.ProcessoController.java`  
**Linhas não cobertas:** 141, 164, 170  
**Branches perdidos:** 163 (DIAGNOSTICO), 169 (erros não vazios)

**Problema identificado:**

- Método `iniciar()` com chain de if/else por TipoProcesso (linha 159-167)
- Branch de DIAGNOSTICO não testado

**Ação proposta:**

```java
// Aplicar Strategy Pattern
private final Map<TipoProcesso, BiFunction<Long, List<Long>, List<String>>> processadoresInicio = Map.of(
    REVISAO, processoService::iniciarProcessoRevisao,
    MAPEAMENTO, processoService::iniciarProcessoMapeamento,
    DIAGNOSTICO, processoService::iniciarProcessoDiagnostico
);

@PostMapping("/{codigo}/iniciar")
public ResponseEntity<?> iniciar(@PathVariable Long codigo, @RequestBody IniciarProcessoReq req) {
    var processador = processadoresInicio.get(req.tipo());
    if (processador == null) {
        return ResponseEntity.badRequest().build();
    }
    List<String> erros = processador.apply(codigo, req.unidades());
    // ...
}
```

**Esforço:** 2h | **Impacto:** +2 branches

---

#### 6. SubprocessoMapaService (~6 branches)

**Arquivo:** `sgc.subprocesso.service.SubprocessoMapaService.java`  
**Linhas não cobertas:** 167
**Branches perdidos:** 100, 114, 120, 165, 178

**Ação proposta:**

- Testes para validações de associação
- Simular mapas com competências/atividades não associadas

**Esforço:** 2h | **Impacto:** +3 branches

---

#### 7. GerenciadorJwt (2 linhas + 3 branches)

**Arquivo:** `sgc.seguranca.GerenciadorJwt.java`  
**Linhas não cobertas:** 84, 85 (claims null)
**Branches perdidos:** 83 (verificações de claims null)

**Problema identificado:**

- Verificações de ambiente em `@PostConstruct`
- Claims JWT incompletos não testados

**Ação proposta:**

1. Injetar `AmbienteInfo` interface para testar verificação de ambiente
2. Adicionar teste com token JWT malformado (claims parciais)

```java
// Interface para abstração de ambiente
public interface AmbienteInfo {
    boolean isProducao();
    boolean isAmbienteSeguro(); // test, e2e, local
}
```

**Esforço:** 3h | **Impacto:** +2 branches

---

#### 8. FiltroAutenticacaoMock (1 linha + 3 branches)

**Arquivo:** `sgc.seguranca.FiltroAutenticacaoMock.java`  
**Linha não coberta:** 56 (usuário não encontrado)
**Branches perdidos:** 39 (header null), 45 (usuario null)

**Ação proposta:**

- Teste de integração com JWT válido mas usuário inexistente no banco
- Teste sem header Authorization

**Esforço:** 1h | **Impacto:** +2 branches

---

### Prioridade Baixa (Erros/Exceções não utilizadas)

| Classe | Problema | Ação |
|--------|----------|------|
| `ErroConfiguracao` (26-27) | Construtor alternativo não usado | Remover ou adicionar teste |
| `ErroInterno` (54-55) | Construtor alternativo não usado | Remover ou adicionar teste |
| `ErroNegocio` (16) | Branch não coberto | Teste de edge-case |
| `ErroUnidadeNaoEncontrada` (13-14) | Classe não utilizada | Considerar remoção |
| `EntidadeBase` (26) | Método não chamado | Validar necessidade |

**Esforço:** 1h | **Impacto:** +5 linhas

---

## 🎯 Plano de Execução

### Fase 1: Quick Wins (Meta: 88% branches)

**Tempo estimado:** 4h

| Tarefa | Arquivo | Impacto |
|--------|---------|---------|
| 1.1 | Testes para `SubprocessoValidacaoService` edge-cases | +4 branches |
| 1.2 | Testes para `SubprocessoDetalheService` null checks | +4 branches |
| 1.3 | Teste `ProcessoController.iniciar` com DIAGNOSTICO | +2 branches |
| 1.4 | Remover/testar construtores não usados de erros | +5 linhas |

### Fase 2: Refatorações de Médio Esforço (Meta: 90% branches)

**Tempo estimado:** 6h

| Tarefa | Arquivo | Impacto |
|--------|---------|---------|
| 2.1 | Strategy Pattern em `SubprocessoMapaWorkflowService` | +3 branches |
| 2.2 | Extrair lógica de `EventoProcessoListener` | +2 branches |
| 2.3 | Testes para `SubprocessoMapaService` | +3 branches |
| 2.4 | Testes para `GerenciadorJwt` claims parciais | +2 branches |

### Fase 3: Refatorações Estruturais (Melhoria contínua)

**Tempo estimado:** 8h

| Tarefa | Descrição |
|--------|-----------|
| 3.1 | Strategy Pattern em `ProcessoController.iniciar()` |
| 3.2 | Interface `AmbienteInfo` para abstração de ambiente |
| 3.3 | Separação de guards via AOP (se necessário) |
| 3.4 | Factory Methods para responses complexos |

---

## 📋 Checklist de Implementação

### Para cada refatoração

- [ ] Executar testes existentes antes da mudança
- [ ] Implementar a refatoração
- [ ] Adicionar novos testes cobrindo os branches
- [ ] Verificar que nenhum teste existente quebrou
- [ ] Rodar `python3 scripts/check_coverage.py "" 90` para validar
- [ ] Atualizar BACKLOG_TESTABILIDADE.md com métricas atualizadas

### Comandos úteis

```bash
# Executar testes e gerar relatório
./gradlew test jacocoTestReport

# Verificar cobertura geral
python3 scripts/check_coverage.py "" 90

# Verificar branches perdidos de uma classe específica
python3 scripts/list_missed_lines.py SubprocessoMapaWorkflowService

# Rodar um teste específico
./gradlew test --tests "*SubprocessoMapaWorkflowServiceTest*"
```

---

## 📈 Projeção de Resultados

| Fase | Branches Cobertos | Cobertura Esperada |
|------|-------------------|-------------------|
| Atual | 1141/1311 | 87.03% |
| Fase 1 | ~1155/1311 | ~88.1% |
| Fase 2 | ~1180/1311 | ~90.0% ✅ |
| Fase 3 | ~1200/1311 | ~91.5% |

---

## Referências

- [BACKLOG_TESTABILIDADE.md](./BACKLOG_TESTABILIDADE.md) - Backlog original
- [AGENTS.md](/AGENTS.md) - Diretrizes de desenvolvimento
- [backend-padroes.md](/regras/backend-padroes.md) - Padrões de código backend
