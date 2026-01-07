# Plano de Melhoria de Testabilidade - SGC Backend

**Data:** 2026-01-07  
**Baseado em:** Análise do BACKLOG_TESTABILIDADE.md e cobertura atual

---

## Métricas Atuais

| Métrica | Valor Anterior | Valor Atual | Meta | Status |
|---------|----------------|-------------|------|--------|
| Cobertura de Linhas | ~99% | **99.50%** | 100% | ⬆️ Melhorado |
| Cobertura de Branches | ~88% | **88.52%** | 95% | ⬆️ Melhorado |
| Total de Testes | 1026+ | **1081** | - | ✅ +55 testes |
| Linhas Perdidas | ~44 | **~88** | 0 | ⚠️ Novas linhas identificadas |

**Nota:** O número de linhas perdidas aumentou porque o relatório anterior subestimou o total.  
A análise atual com Jacoco é mais precisa e identifica todas as linhas não cobertas.

---

## 📊 Análise de Cobertura Detalhada

### Componentes com 100% de Cobertura de Linhas ✅

Os seguintes módulos atingiram cobertura perfeita de linhas:
- `sgc.processo.service` (100%)
- `sgc.comum.util` (100%)
- `sgc.comum.model` (100%)
- `sgc.processo.dto.mappers` (100%)
- `sgc.organizacao.mapper` (100%)
- `sgc.subprocesso.service.decomposed` (100%)
- Diversos outros módulos menores

### Top 5 - Arquivos com Mais Branches Perdidos (Prioridade Alta)

| Arquivo | Branches Perdidos | Linhas Perdidas | Prioridade |
|---------|-------------------|-----------------|------------|
| `SubprocessoPermissoesService` | 13 | 0 | 🔴 Alta |
| `ProcessoService` | 12 | 0 | 🔴 Alta |
| `PainelService` | 10 | 2 | 🔴 Alta |
| `SubprocessoMapaWorkflowService` | 9 | 0 | 🟡 Média |
| `UsuarioService` | 8 | 0 | 🟡 Média |

---

## 📋 Linhas Não Cobertas Identificadas

### Serviços de Domínio

| Arquivo | Linhas | Contexto | Categoria |
|---------|--------|----------|-----------|
| `PainelService` | 88, 235 | Código defensivo (fallback paths) | Lógica de Edge Case |
| `SubprocessoMapaService` | 167 | Validação específica ou branch raro | Validação |
| `SubprocessoCadastroWorkflowService` | 203 | Condição de borda em workflow | Workflow |
| `ValidadorDadosOrganizacionais` | 118 | Validação de dados inválidos | Validação |
| `AnalisadorCompetenciasService` | 145 | Branch complexo de análise | Lógica de Negócio |
| `AtividadeFacade` | 39 | Método não coberto | Serviço |
| `AlertaService` | 187 | Já coberto por testes existentes | N/A |

### Listeners e Eventos

| Arquivo | Linhas | Contexto | Categoria |
|---------|--------|----------|-----------|
| `EventoProcessoListener` | 123, 124 | Exceção no loop externo (difícil de simular) | Exception Handling |

### Controllers

| Arquivo | Linhas | Contexto | Categoria |
|---------|--------|----------|-----------|
| `SubprocessoCadastroController` | 326 | Tratamento de erro ou validação | Error Handling |

### Segurança

| Arquivo | Linhas | Contexto | Categoria |
|---------|--------|----------|-----------|
| `GerenciadorJwt` | 84, 85 | Validação de claims inválidos | Validação |
| `FiltroAutenticacaoMock` | 56 | Log quando usuário não encontrado | Mock/Test |

---

## 🎯 Estratégias de Teste Aplicadas

### 1. Testes de Utilidades e Classes Base ✅
- **EntidadeBaseTest**: Cobertura do método `toString()` herdado
- **SleeperTest**: Cobertura de `sleep()` e `InterruptedException`
- **CustomExceptionsTest**: Construtores de exceções personalizadas

### 2. Testes de Controllers ✅
- **AlertaControllerTest**: Endpoint `listarAlertas` adicionado
- Todos os endpoints principais com cobertura de casos de sucesso e erro

### 3. Testes de Serviços ✅
- **ProcessoServiceTest**: 
  - Método `obterContextoCompleto`
  - Caso de autenticação null
- **SubprocessoFactoryTest**: 
  - Validação de `unidadeMapa` null

### 4. Testes de Listeners e Eventos ✅
- **EventoProcessoListenerTest**:
  - Envio de email para unidade INTERMEDIARIA
  - Tratamento de exceção em loop de subprocessos

---

## 🚀 Plano de Execução para Atingir Metas

### Fase 1: Cobertura de Branches (Prioridade Máxima)
**Meta:** Atingir 95% de branch coverage

1. **SubprocessoPermissoesService** (13 branches)
   - Testar todos os cenários de permissão
   - Casos de acesso negado por perfil
   - Casos de unidade não compatível

2. **ProcessoService** (12 branches)
   - Testar edge cases em filtros
   - Validações de estado
   - Casos de processo não encontrado

3. **PainelService** (10 branches)
   - Testes de ordenação e paginação
   - Casos de unidade sem processos
   - Links de destino para diferentes perfis

### Fase 2: Linhas Restantes (Prioridade Alta)
**Meta:** Atingir 100% de line coverage

1. **Código Defensivo Alcançável**
   - `PainelService` linhas 88, 235
   - `SubprocessoCadastroController` linha 326
   - `ValidadorDadosOrganizacionais` linha 118

2. **Exception Handling**
   - `EventoProcessoListener` linhas 123-124 (requer mock específico)

3. **Validações Complexas**
   - `GerenciadorJwt` linhas 84-85 (já existe teste, verificar cobertura)
   - `SubprocessoMapaService` linha 167

### Fase 3: Limpeza e Documentação
1. Remover código morto (se identificado)
2. Documentar casos de teste complexos
3. Atualizar este documento com resultados finais

---

## 📝 Observações e Decisões

### Código Inalcançável ou Defensivo
Alguns casos são difíceis de testar por serem:
- **Código defensivo** que nunca deveria ser executado em produção
- **Validações de enum** onde todos os casos já são cobertos (ex: `ProcessoController` linha 174)
- **Mocks de teste/e2e** que não são críticos para produção (ex: `FiltroAutenticacaoMock`)

### Recomendações
1. **Aceitar ~0.5% de código não coberto** se for exclusivamente código defensivo inalcançável
2. **Focar em branches** - O maior impacto está em atingir 95% de branch coverage
3. **Priorizar serviços de negócio** sobre infraestrutura e mocks

---

## ✅ Progresso e Conquistas

### Melhorias Implementadas
- ✅ +55 testes adicionados
- ✅ Cobertura de linhas: 98.95% → 99.50% (+0.55%)
- ✅ Cobertura de branches: 88.21% → 88.52% (+0.31%)
- ✅ Todas as classes utilitárias com 100% de cobertura
- ✅ Serviços principais melhorados significativamente

### Próximos Marcos
- [ ] Branch coverage ≥ 90%
- [ ] Branch coverage ≥ 92%
- [ ] Branch coverage ≥ 95% (Meta Final)
- [ ] Line coverage ≥ 99.8%
- [ ] Line coverage = 100% (Meta Final)

---

## 🔧 Comandos para Verificação

```bash
# Executar testes e gerar relatório
cd backend && ./gradlew test jacocoTestReport

# Verificar cobertura geral
python3 scripts/check_coverage.py

# Listar linhas perdidas detalhadamente
python3 scripts/list_missed_lines.py

# Verificar cobertura de um pacote específico
python3 scripts/check_coverage.py sgc.processo 95.0

# Listar linhas perdidas de uma classe específica
python3 scripts/list_missed_lines.py ProcessoService
```

---

**Última Atualização:** 2026-01-07  
**Status:** 🟢 Em Progresso - Metas Parcialmente Atingidas

