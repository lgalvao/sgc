# Rastreamento de Testes de Integração V2

**Última atualização**: 2026-02-09

## Documentação Relacionada

- **[Plano de Testes](integration-test-plan.md)**: Estratégia completa e princípios dos testes
- **[Aprendizados](integration-test-learnings.md)**: Lições aprendidas, desafios e soluções
- **[README dos Testes V2](backend/src/test/java/sgc/integracao/v2/README.md)**: Guia prático para escrever testes

## Status Geral

| Categoria | Total | Implementados | Em Progresso | Pendente |
|-----------|-------|---------------|--------------|----------|
| **Autenticação** | 1 | 0 | 0 | 1 |
| **Processo** | 6 | 1 | 0 | 5 |
| **Cadastro** | 6 | 0 | 0 | 6 |
| **Mapa** | 9 | 0 | 0 | 9 |
| **Operações em Bloco** | 5 | 0 | 0 | 5 |
| **Administração** | 7 | 0 | 0 | 7 |
| **Painel** | 2 | 1 | 0 | 1 |
| **Relatórios** | 3 | 0 | 0 | 3 |
| **Fluxos Completos** | 3 | 0 | 0 | 3 |
| **TOTAL** | **42** | **2** | **0** | **40** |

**Progresso**: 4.8% (2/42)

---

## Detalhamento por CDU

### ✅ Implementados (2)

#### Processo (1)

- [x] **CDU-03**: Manter Processo
  - ✅ Criação de processos (mapeamento, revisão, diagnóstico)
  - ✅ Validações (descrição, unidades, conflitos)
  - ✅ Edição de processos em status Criado
  - ✅ Exclusão de processos em status Criado
  - ✅ Controle de acesso (ADMIN somente)
  - Arquivo: `backend/src/test/java/sgc/integracao/v2/processo/CDU03ManterProcessoIntegrationTest.java`
  - Cenários: 9 de 9 ✅
  - Data: 2026-02-09

#### Painel (1)

- [x] **CDU-02**: Visualizar Painel
  - ✅ Visibilidade de processos por perfil (ADMIN, GESTOR, CHEFE)
  - ✅ Processos 'Criado' visíveis apenas para ADMIN
  - ✅ Visibilidade baseada em hierarquia
  - ✅ Alertas pessoais e da unidade
  - ✅ Marcação de alertas como visualizados
  - ✅ Ordenação de processos e alertas
  - Arquivo: `backend/src/test/java/sgc/integracao/v2/painel/CDU02VisualizarPainelIntegrationTest.java`
  - Cenários: 6 de 6 ✅
  - Data: 2026-02-09

---

### 🔄 Em Progresso (0)

_Nenhum teste em desenvolvimento._

---

### ⏳ Pendentes (42)

#### Autenticação (1)

- [ ] **CDU-01**: Login e Estrutura de Telas
  - Cenários: 9
  - Prioridade: 🔴 Alta
  - Dependências: Nenhuma

#### Processo (5)

- [ ] **CDU-04**: Iniciar Processo de Mapeamento
  - Cenários: 7
  - Prioridade: 🔴 Alta
  - Dependências: CDU-03

- [ ] **CDU-05**: Iniciar Processo de Revisão
  - Cenários: 5
  - Prioridade: 🟡 Média
  - Dependências: CDU-03, CDU-21

- [ ] **CDU-06**: Detalhar Processo
  - Cenários: 6
  - Prioridade: 🟡 Média
  - Dependências: CDU-04

- [ ] **CDU-21**: Finalizar Processo
  - Cenários: 5
  - Prioridade: 🔴 Alta
  - Dependências: CDU-04, CDU-20

#### Cadastro (6)

- [ ] **CDU-08**: Manter Cadastro de Atividades e Conhecimentos
  - Cenários: 8
  - Prioridade: 🔴 Alta
  - Dependências: CDU-04

- [ ] **CDU-09**: Disponibilizar Cadastro
  - Cenários: 7
  - Prioridade: 🔴 Alta
  - Dependências: CDU-08

- [ ] **CDU-10**: Disponibilizar Revisão de Cadastro
  - Cenários: 4
  - Prioridade: 🟡 Média
  - Dependências: CDU-05, CDU-08

- [ ] **CDU-11**: Visualizar Cadastro
  - Cenários: 3
  - Prioridade: 🟢 Baixa
  - Dependências: CDU-09

- [ ] **CDU-13**: Analisar Cadastro
  - Cenários: 6
  - Prioridade: 🔴 Alta
  - Dependências: CDU-09

- [ ] **CDU-14**: Analisar Revisão de Cadastro
  - Cenários: 5
  - Prioridade: 🟡 Média
  - Dependências: CDU-10

#### Mapa (9)

- [ ] **CDU-12**: Verificar Impactos no Mapa
  - Cenários: 7
  - Prioridade: 🟡 Média
  - Dependências: CDU-10

- [ ] **CDU-15**: Manter Mapa de Competências
  - Cenários: 6
  - Prioridade: 🔴 Alta
  - Dependências: CDU-13

- [ ] **CDU-16**: Ajustar Mapa de Competências
  - Cenários: 3
  - Prioridade: 🟡 Média
  - Dependências: CDU-15, CDU-12

- [ ] **CDU-17**: Disponibilizar Mapa
  - Cenários: 7
  - Prioridade: 🔴 Alta
  - Dependências: CDU-15

- [ ] **CDU-18**: Visualizar Mapa
  - Cenários: 3
  - Prioridade: 🟢 Baixa
  - Dependências: CDU-17

- [ ] **CDU-19**: Validar Mapa
  - Cenários: 5
  - Prioridade: 🔴 Alta
  - Dependências: CDU-17

- [ ] **CDU-20**: Analisar Validação de Mapa
  - Cenários: 4
  - Prioridade: 🔴 Alta
  - Dependências: CDU-19

#### Operações em Bloco (5)

- [ ] **CDU-22**: Aceitar Cadastros em Bloco
  - Cenários: 4
  - Prioridade: 🟡 Média
  - Dependências: CDU-13

- [ ] **CDU-23**: Homologar Cadastros em Bloco
  - Cenários: 3
  - Prioridade: 🟡 Média
  - Dependências: CDU-13

- [ ] **CDU-24**: Disponibilizar Mapas em Bloco
  - Cenários: 4
  - Prioridade: 🟡 Média
  - Dependências: CDU-17

- [ ] **CDU-25**: Aceitar Validação de Mapas em Bloco
  - Cenários: 3
  - Prioridade: 🟡 Média
  - Dependências: CDU-20

- [ ] **CDU-26**: Homologar Validação de Mapas em Bloco
  - Cenários: 3
  - Prioridade: 🟡 Média
  - Dependências: CDU-20

#### Administração (7)

- [ ] **CDU-27**: Alterar Data Limite de Subprocesso
  - Cenários: 2
  - Prioridade: 🟢 Baixa
  - Dependências: CDU-04

- [ ] **CDU-28**: Manter Atribuição Temporária
  - Cenários: 4
  - Prioridade: 🟡 Média
  - Dependências: CDU-01

- [ ] **CDU-30**: Manter Administradores
  - Cenários: 5
  - Prioridade: 🟡 Média
  - Dependências: CDU-01

- [ ] **CDU-31**: Configurar Sistema
  - Cenários: 4
  - Prioridade: 🟢 Baixa
  - Dependências: CDU-01

- [ ] **CDU-32**: Reabrir Cadastro
  - Cenários: 5
  - Prioridade: 🟡 Média
  - Dependências: CDU-13

- [ ] **CDU-33**: Reabrir Revisão de Cadastro
  - Cenários: 4
  - Prioridade: 🟡 Média
  - Dependências: CDU-14

- [ ] **CDU-34**: Enviar Lembrete de Prazo
  - Cenários: 4
  - Prioridade: 🟢 Baixa
  - Dependências: CDU-04

#### Painel (1)

- [ ] **CDU-07**: Detalhar Subprocesso
  - Cenários: 5
  - Prioridade: 🟡 Média
  - Dependências: CDU-04

#### Relatórios (3)

- [ ] **CDU-29**: Consultar Histórico de Processos
  - Cenários: 4
  - Prioridade: 🟢 Baixa
  - Dependências: CDU-21

- [ ] **CDU-35**: Gerar Relatório de Andamento
  - Cenários: 3
  - Prioridade: 🟢 Baixa
  - Dependências: CDU-04

- [ ] **CDU-36**: Gerar Relatório de Mapas
  - Cenários: 4
  - Prioridade: 🟢 Baixa
  - Dependências: CDU-21

#### Fluxos Completos (3)

- [ ] **Fluxo Completo de Mapeamento**
  - Cenários: 1 (fluxo end-to-end)
  - Prioridade: 🔴 Alta
  - Dependências: CDU-04, CDU-08, CDU-09, CDU-13, CDU-15, CDU-17, CDU-19, CDU-20, CDU-21

- [ ] **Fluxo Completo de Revisão**
  - Cenários: 1 (fluxo end-to-end)
  - Prioridade: 🟡 Média
  - Dependências: CDU-05, CDU-10, CDU-14, CDU-16, CDU-17, CDU-19, CDU-20, CDU-21

- [ ] **Fluxo Completo de Diagnóstico**
  - Cenários: 1 (fluxo end-to-end)
  - Prioridade: 🟡 Média
  - Dependências: Requer especificações adicionais

---

## Ordem de Implementação Recomendada

### Sprint 1: Fundação (CDUs Críticos)
1. ✅ Criar estrutura base (`BaseIntegrationTestV2`)
2. CDU-01: Login e Autenticação
3. CDU-03: Manter Processo
4. CDU-04: Iniciar Processo de Mapeamento

### Sprint 2: Cadastro
5. CDU-08: Manter Cadastro
6. CDU-09: Disponibilizar Cadastro
7. CDU-13: Analisar Cadastro

### Sprint 3: Mapa
8. CDU-15: Manter Mapa
9. CDU-17: Disponibilizar Mapa
10. CDU-19: Validar Mapa
11. CDU-20: Analisar Validação de Mapa

### Sprint 4: Finalização e Fluxos
12. CDU-21: Finalizar Processo
13. Fluxo Completo de Mapeamento

### Sprint 5: Revisão
14. CDU-05: Iniciar Revisão
15. CDU-10: Disponibilizar Revisão
16. CDU-14: Analisar Revisão
17. CDU-12: Verificar Impactos
18. CDU-16: Ajustar Mapa
19. Fluxo Completo de Revisão

### Sprint 6: Operações em Bloco
20. CDU-22: Aceitar Cadastros em Bloco
21. CDU-23: Homologar Cadastros em Bloco
22. CDU-24: Disponibilizar Mapas em Bloco
23. CDU-25: Aceitar Validação em Bloco
24. CDU-26: Homologar Validação em Bloco

### Sprint 7: Administração
25. CDU-28: Manter Atribuição Temporária
26. CDU-30: Manter Administradores
27. CDU-32: Reabrir Cadastro
28. CDU-33: Reabrir Revisão

### Sprint 8: Visualização e Relatórios
29. CDU-02: Visualizar Painel
30. CDU-06: Detalhar Processo
31. CDU-07: Detalhar Subprocesso
32. CDU-11: Visualizar Cadastro
33. CDU-18: Visualizar Mapa
34. CDU-29: Consultar Histórico
35. CDU-35: Relatório de Andamento
36. CDU-36: Relatório de Mapas

### Sprint 9: Funcionalidades Complementares
37. CDU-27: Alterar Data Limite
38. CDU-31: Configurar Sistema
39. CDU-34: Enviar Lembrete
40. Fluxo Completo de Diagnóstico

---

## Métricas de Qualidade

### Cobertura de Código (Meta: >= 80%)

| Camada | Meta | Atual | Status |
|--------|------|-------|--------|
| Controllers | 80% | - | ⏳ Pendente |
| Service Facades | 80% | - | ⏳ Pendente |
| Services | 70% | - | ⏳ Pendente |
| Repositories | 60% | - | ⏳ Pendente |

### Tempo de Execução (Meta: < 10 minutos para toda a suíte)

| Categoria | Testes | Tempo Médio | Total |
|-----------|--------|-------------|-------|
| Autenticação | 0 | - | - |
| Processo | 0 | - | - |
| Cadastro | 0 | - | - |
| Mapa | 0 | - | - |
| Operações em Bloco | 0 | - | - |
| Administração | 0 | - | - |
| Painel | 0 | - | - |
| Relatórios | 0 | - | - |
| Fluxos Completos | 0 | - | - |
| **TOTAL** | **0** | - | **0s** |

---

## Issues e Bloqueios

_Nenhum bloqueio identificado ainda._

---

## Notas e Descobertas

### Divergências entre Requisitos e Implementação

_Documentar aqui quaisquer divergências encontradas entre os requisitos em `/etc/reqs` e a implementação atual._

### Melhorias Identificadas

_Documentar sugestões de melhoria no código ou nos requisitos._

### Perguntas Pendentes

_Documentar questões que precisam de clarificação._

---

## Changelog

| Data | Versão | Autor | Mudanças |
|------|--------|-------|----------|
| 2026-02-09 | 0.2.0 | Sistema | Implementação de CDU-02 e CDU-03 (2/42 testes) |
| 2026-02-09 | 0.1.0 | Sistema | Criação inicial do documento de rastreamento |
