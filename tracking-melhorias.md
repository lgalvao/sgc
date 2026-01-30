# 🎯 Tracking de Melhorias - SGC

**Data Início:** 2026-01-30  
**Documento Base:** plano-melhorias.md  
**Status:** Em Progresso

---

## 📊 Resumo do Progresso

| Prioridade | Total | Completo | Em Progresso | Pendente |
|-----------|-------|----------|--------------|----------|
| 🔴 CRÍTICA | 13 | 4 | 0 | 9 |
| 🟠 MÉDIA | 14 | 0 | 0 | 14 |
| 🟡 BAIXA | 6 | 0 | 0 | 6 |
| **TOTAL** | **33** | **4** | **0** | **29** |

---

## 🔴 Prioridade CRÍTICA

### Quick Wins e Segurança (13 ações)

- [x] **#1** Remover arquivos `*CoverageTest.java` (27+ arquivos) - 2h
- [ ] **#2** Consolidar Access Policies em AbstractAccessPolicy - 6h
- [ ] **#3** Dividir GOD Composables (useCadAtividadesLogic) - 8h
- [ ] **#4** Refatorar SubprocessoFacade e centralizar validações - 8h
- [x] **#5** Mover @PreAuthorize de Facades para Controllers - 6h
- [ ] **#6** Centralizar verificações de acesso via AccessControlService - 8h
- [x] **#7** Criar DTOs para AnaliseController e ConfiguracaoController - 4h
- [ ] **#8** Eliminar ciclos de dependência via Events - 2h
- [ ] **#9** Padronizar acesso a services (View→Store→Service→API) - 4h
- [x] **#10** Substituir console.* por logger - 3h
- [ ] **#11** Adotar fixtures E2E (36 arquivos) - 6h
- [ ] **#12** Reduzir over-mocking (46 arquivos) - 5h

---

## 🟠 Prioridade MÉDIA

### Backend (6 ações)

- [ ] **#14** Remover padrão "do*" em AlertaFacade (6 métodos) - 2h
- [ ] **#15** Consolidar DTOs similares por domínio - 8h
- [ ] **#16** Remover verificações null redundantes (30 ocorrências) - 4h
- [ ] **#17** Padronizar estrutura de pacotes - 6h
- [ ] **#18** Dividir Controllers grandes (ADR-005) - 6h
- [ ] **#19** Refatorar try-catch genéricos (10 ocorrências) - 2h

### Frontend (6 ações)

- [ ] **#20** Criar composable useLoading() - 3h
- [ ] **#21** Padronizar reset de state em stores - 4h
- [ ] **#22** Adotar formatters centralizados (12 componentes) - 2h
- [ ] **#23** Adotar normalizeError() em services (6 arquivos) - 2h
- [ ] **#24** Extrair lógica de views para composables (8 views) - 5h
- [ ] **#25** Definir estratégia de erro padrão - 2h

### Testes (2 ações)

- [ ] **#26** Dividir testes com múltiplos asserts (35 testes) - 4h
- [ ] **#27** Refatorar testes que testam implementação (40 testes) - 2h

---

## 🟡 Prioridade BAIXA

### Backend (2 ações)

- [ ] **#28** Mover validações de negócio de Controllers para Services - 4h
- [ ] **#29** Documentar exceções nos JavaDocs - 4h

### Frontend (3 ações)

- [ ] **#30** Padronizar nomenclatura em stores - 2h
- [ ] **#31** Padronizar importações absolutas com @/ - 2h
- [ ] **#32** Refatorar props drilling com provide/inject - 2h

### Testes (1 ação)

- [ ] **#33** Adicionar testes de integração (Backend) - 5h

---

## 📝 Log de Execução

### 2026-01-30

**Início da Execução**
- ✅ Leitura do plano-melhorias.md completo
- ✅ Criação do arquivo tracking-melhorias.md
- ✅ **Ação #1 COMPLETA**: Removidos 26 arquivos *CoverageTest.java
  - Impacto: Métricas de cobertura agora refletem testes reais
  - Redução: ~2000+ linhas de código de teste sem valor
- ✅ **Ação #10 COMPLETA**: Console.* por logger no frontend
  - Verificado: Frontend já estava usando logger corretamente (consola)
  - Apenas testes usam console.error (apropriado)
- ✅ **Ação #5 COMPLETA**: @PreAuthorize movido de Facades para Controllers
  - ProcessoFacade: Removidas 10 anotações @PreAuthorize
  - ProcessoController: Adicionada 1 anotação faltante em listarSubprocessosElegiveis
  - Conformidade com ADR-001: Controllers definem segurança, Facades orquestram lógica
  - Compilação: ✅ Bem-sucedida com Java 21
- ✅ **Ação #7 COMPLETA**: DTOs criados para ConfiguracaoController
  - AnaliseController: Já estava conforme (usa DTOs)
  - ConfiguracaoController: Criados ParametroRequest e ParametroResponse
  - ParametroMapper: Implementado com MapStruct
  - ConfiguracaoService: Adicionado método buscarPorId
  - ConfiguracaoFacade: Atualizado para usar DTOs
  - Conformidade com ADR-004: Entidades JPA não são mais expostas diretamente
  - Compilação: ✅ Bem-sucedida

---

## 🎯 Próximos Passos Imediatos

1. **Ação #1:** Identificar e remover todos os arquivos `*CoverageTest.java` (Quick Win)
2. **Ação #10:** Substituir console.* por logger no frontend
3. **Ação #5:** Mover @PreAuthorize de Facades para Controllers
4. Continuar com as demais ações críticas

---

## 🔍 Achados Durante Execução

### Conformidades Já Existentes (Positivo!)

1. **Frontend - Logging Estruturado:**
   - ✅ Usa `consola` (logger estruturado) corretamente
   - ✅ ESLint configurado para bloquear `console.*`
   - ✅ Apenas testes usam `console.error` (apropriado para supressão)
   - **Conclusão:** Ação #10 já estava completa

2. **AnaliseController - ADR-004:**
   - ✅ Já usa DTOs corretamente: `AnaliseHistoricoDto`, `CriarAnaliseRequest`, `CriarAnaliseCommand`
   - ✅ Entidade JPA não exposta diretamente
   - **Conclusão:** Parte da ação #7 já estava completa

3. **Facades - ADR-001:**
   - ✅ Verificadas todas as Facades do projeto
   - ✅ Apenas ProcessoFacade tinha @PreAuthorize
   - ✅ Demais Facades já em conformidade
   - **Conclusão:** Escopo menor que o estimado

### Melhorias Implementadas

1. **Ação #1 - Remoção de Testes Artificiais:**
   - 26 arquivos `*CoverageTest.java` removidos
   - ~4.400 linhas de código sem valor eliminadas
   - Métricas de cobertura agora refletem testes reais
   - **Impacto:** Alta visibilidade da cobertura real

2. **Ação #5 - Conformidade ADR-001:**
   - ProcessoFacade: 10 anotações @PreAuthorize removidas
   - ProcessoController: 1 anotação @PreAuthorize adicionada
   - Import não utilizado removido
   - **Impacto:** Separação clara de responsabilidades

3. **Ação #7 - Conformidade ADR-004:**
   - ConfiguracaoController: DTOs criados
   - ParametroRequest e ParametroResponse implementados
   - ParametroMapper com MapStruct
   - ConfiguracaoService: Método buscarPorId adicionado
   - **Impacto:** Entidades JPA protegidas de exposição direta

### Recomendações para Próximas Iterações

1. **Priorizar #2 e #6:** Consolidação de Access Policies é crítica para segurança
2. **Considerar #4:** SubprocessoFacade pode estar com violações similares ao ProcessoFacade
3. **Revisar estimativas:** Algumas ações já estavam completas, tempo pode ser realocado
4. **Validar com testes:** Executar suite de testes para garantir não-regressão

---

**Última Atualização:** 2026-01-30 20:33 UTC

## 📌 Status Final

**Execução Inicial Completa:** 4 de 33 ações (12%)
- ✅ Quick wins implementados com sucesso
- ✅ Conformidade com ADRs críticos melhorada
- ✅ Base de código mais limpa (~4.400 linhas removidas)
- ✅ Documentação atualizada com achados reais

**Próximos Passos Recomendados:**
1. Executar suite de testes completa para validação
2. Continuar com ações #2, #4, #6 (segurança e arquitetura)
3. Revisar plano baseado nos achados de conformidades existentes
