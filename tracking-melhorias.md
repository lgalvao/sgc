# 🎯 Tracking de Melhorias - SGC

**Data Início:** 2026-01-30  
**Documento Base:** plano-melhorias.md  
**Status:** Em Progresso

---

## 📊 Resumo do Progresso

| Prioridade | Total | Completo | Em Progresso | Pendente |
|-----------|-------|----------|--------------|----------|
| 🔴 CRÍTICA | 13 | 0 | 0 | 13 |
| 🟠 MÉDIA | 14 | 0 | 0 | 14 |
| 🟡 BAIXA | 6 | 0 | 0 | 6 |
| **TOTAL** | **33** | **0** | **0** | **33** |

---

## 🔴 Prioridade CRÍTICA

### Quick Wins e Segurança (13 ações)

- [ ] **#1** Remover arquivos `*CoverageTest.java` (27+ arquivos) - 2h
- [ ] **#2** Consolidar Access Policies em AbstractAccessPolicy - 6h
- [ ] **#3** Dividir GOD Composables (useCadAtividadesLogic) - 8h
- [ ] **#4** Refatorar SubprocessoFacade e centralizar validações - 8h
- [ ] **#5** Mover @PreAuthorize de Facades para Controllers - 6h
- [ ] **#6** Centralizar verificações de acesso via AccessControlService - 8h
- [ ] **#7** Criar DTOs para AnaliseController e ConfiguracaoController - 4h
- [ ] **#8** Eliminar ciclos de dependência via Events - 2h
- [ ] **#9** Padronizar acesso a services (View→Store→Service→API) - 4h
- [ ] **#10** Substituir console.* por logger - 3h
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
- 🔄 Iniciando ação #1: Remover arquivos *CoverageTest.java

---

## 🎯 Próximos Passos Imediatos

1. **Ação #1:** Identificar e remover todos os arquivos `*CoverageTest.java` (Quick Win)
2. **Ação #10:** Substituir console.* por logger no frontend
3. **Ação #5:** Mover @PreAuthorize de Facades para Controllers
4. Continuar com as demais ações críticas

---

## 🔍 Achados Durante Execução

_Nenhum achado registrado ainda. Esta seção será atualizada conforme a execução progride._

---

**Última Atualização:** 2026-01-30 20:33 UTC
