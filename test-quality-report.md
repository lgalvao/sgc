# Test Quality Check — Backend (`backend/src/test/`)

## Plano de ataque imediato (foco em criticidade alta)

1. **Rastreabilidade de CDU-15 e CDU-21 (High) — ENDEREÇADO**  
   - **Ação executada:** adicionados `CDU15IntegrationTest` e `CDU21IntegrationTest` com validação de cenários principais e falha de regra de negócio.  
   - **Próximo passo:** ampliar cenários de borda/erros e manter referência explícita ao requisito em todos os casos novos.

2. **Integridade do schema de teste divergente do modelo (High)**  
   - **Ação inicial:** alinhar `backend/src/test/resources/db/schema.sql` com `NOT NULL` de `subprocesso` (`processo_codigo`, `unidade_codigo`, `situacao`, `data_limite_etapa1`).  
   - **Critério de pronto:** fixtures inválidas passam a falhar cedo em persistência e testes deixam de depender de estados impossíveis em produção.

3. **Oráculos frágeis baseados em IDs fixos de seed (High)**  
   - **Ação inicial:** refatorar cenários de segurança para gerar dados por fixture própria de teste, removendo dependência de IDs como `50002`.  
   - **Critério de pronto:** asserções verificam invariantes de autorização (escopo por token/perfil/unidade), independentemente de valores fixos do `data.sql`.

---

## 1) Corrupted test oracles

### High
- **Arquivo:** `backend/src/test/java/sgc/processo/painel/PainelSecurityReproductionTest.java`  
  **Teste:** `listarProcessos_Sucesso`  
  **Categoria:** Corrupted test oracles  
  **Problema:** O oráculo depende de códigos fixos do `data.sql` (`50002`) em vez de validar a regra de segurança (escopo por unidade/perfil). Isso torna o teste frágil a mudanças de seed e incentiva “assert de massa de dados” em vez de requisito.  
  **Sugestão:** Criar os dados dentro do próprio teste (ou fixture isolada) e validar invariantes de autorização (ex.: “somente processos da unidade do token”).

### Medium
- **Arquivo:** `backend/src/test/java/sgc/integracao/ProcessoServiceIntegrationTest.java`  
  **Teste:** `deveLancarErroAoAtualizarProcessoEmAndamento` e `deveLancarErroAoApagarProcessoEmAndamento`  
  **Categoria:** Corrupted test oracles  
  **Problema:** Os testes assumem implicitamente que o processo `50000` está em situação específica no seed. O esperado vem da observação do estado atual do ambiente de teste, não de uma pré-condição explicitamente montada no cenário.  
  **Sugestão:** Montar o processo no `@BeforeEach` com situação explícita e usar esse objeto como base para asserções.

---

## 2) Business rule coverage

> Atualização: `SubprocessoControllerCoverageTest` recebeu reforço de asserções para `validarCadastro` e `salvarMapa`
> (payload + verificação explícita de chamadas ao serviço), reduzindo risco de falso positivo por “status-only”.

### High
- **Arquivo:** `backend/src/test/java/sgc/subprocesso/SubprocessoControllerCoverageTest.java`  
  **Teste:** `validarCadastro`  
  **Categoria:** Business rule coverage  
  **Status:** **Mitigado parcialmente neste ciclo** (agora valida payload e interação com serviço).  
  **Próximo passo:** adicionar cenário inválido com inconsistências reais de cadastro.

### Medium
- **Arquivo:** `backend/src/test/java/sgc/subprocesso/SubprocessoControllerCoverageTest.java`  
  **Teste:** `importarAtividades`  
  **Categoria:** Business rule coverage  
  **Problema:** Verifica só status e não confirma pós-condições funcionais (atividades importadas, cardinalidade, vínculo no subprocesso).  
  **Sugestão:** Após a chamada, buscar estado persistido e validar invariantes de domínio.

---

## 3) Fixture integrity

### High
- **Arquivo:** `backend/src/test/resources/db/schema.sql`  
  **Teste:** `N/A (infra de teste)`  
  **Categoria:** Fixture integrity  
  **Status:** **Resolvido neste ciclo** — o schema de teste agora exige `NOT NULL` para `processo_codigo`, `unidade_codigo`, `situacao` e `data_limite_etapa1`.  
  **Próximo passo:** manter os novos testes/fixtures sempre explicitando `dataLimiteEtapa1` para evitar regressão silenciosa.

### Medium
- **Arquivo:** `backend/src/test/java/sgc/subprocesso/service/SubprocessoValidacaoServiceTest.java`  
  **Teste:** `erroSituacaoNula`  
  **Categoria:** Fixture integrity  
  **Problema:** O cenário usa `sp.setSituacao(null)` em entidade cujo estado persistido exige situação não nula. Embora útil para guarda defensiva, cria caso pouco representativo do ciclo real de persistência.  
  **Sugestão:** Preferir validação via DTO/entrada da API para nulos e manter entidades persistidas com invariantes válidas.

---

## 4) Requirement traceability (integration tests)

### High
- **Arquivo:** `backend/src/test/java/sgc/integracao/CDU15IntegrationTest.java` e `backend/src/test/java/sgc/integracao/CDU21IntegrationTest.java`  
  **Teste:** cobertura inicial dos fluxos principais de CDU-15 e CDU-21  
  **Categoria:** Requirement traceability  
  **Status:** **Resolvido neste ciclo** (lacuna de rastreabilidade fechada com testes dedicados).  
  **Próximo passo:** expandir para cenários alternativos (cancelamento/edição/erros de validação detalhados).

### Medium
- **Arquivo:** `backend/src/test/java/sgc/integracao/SubprocessoServiceCoverageIntegrationTest.java`  
  **Teste:** `validarExistenciaAtividades_SemAtividades` e `validarExistenciaAtividades_SemConhecimento`  
  **Categoria:** Requirement traceability  
  **Problema:** A classe está rotulada como “Cobertura de Validações”, sem referência clara a CDU/RN em `etc/reqs/`; rastreabilidade fica implícita.  
  **Sugestão:** Incluir referência explícita (CDU/RN) no nome da classe, `@DisplayName` e/ou comentário de rastreio.

- **Arquivo:** `backend/src/test/java/sgc/integracao/MapaManutencaoServiceIntegrationTest.java`  
  **Teste:** suíte da classe (15 testes)  
  **Categoria:** Requirement traceability  
  **Problema:** Foco técnico (manutenção de mapa) sem vínculo explícito com casos de uso em `etc/reqs/`.  
  **Sugestão:** Dividir por CDU ou adicionar matriz de rastreio no cabeçalho da classe.

---

## 5) Functional overlap

### Medium
- **Arquivo:** `backend/src/test/java/sgc/subprocesso/SubprocessoControllerCoverageTest.java` e `backend/src/test/java/sgc/subprocesso/SubprocessoControllerCoverageExtraTest.java`  
  **Teste:** `obterMapaCompleto` (em ambas), além de múltiplos “- ok / retorna 200”  
  **Categoria:** Functional overlap  
  **Problema:** Há duplicação de comportamento de controller sem diferenciação clara de cenário (mesma funcionalidade, mesmo tipo de asserção).  
  **Sugestão:** Consolidar cenários por endpoint em uma única suíte parametrizada, separando sucesso, borda e erro.

### Low
- **Arquivo:** `backend/src/test/java/sgc/processo/service/ProcessoServiceTest.java`, `ProcessoServiceCoverageTest.java` e `ProcessoServiceExtraCoverageTest.java`  
  **Teste:** múltiplos testes de “cobertura” para a mesma API de serviço  
  **Categoria:** Functional overlap  
  **Problema:** Organização em “principal + coverage + extra coverage” gera sobreposição de intenção e manutenção difícil.  
  **Sugestão:** Unificar por comportamento de negócio (criação, atualização, validação, transição) em vez de “tipo de cobertura”.

---

## 6) Test suite cohesion

### Medium
- **Arquivo:** `backend/src/test/java/sgc/seguranca/login/LoginControllerCoverageTest.java`  
  **Teste:** `deveRetornarFalseSeFalharAutenticacao` (arquivo com 1 teste)  
  **Categoria:** Test suite cohesion  
  **Problema:** Arquivo muito pequeno no mesmo domínio já coberto por `LoginControllerTest` (14 testes), aumentando fragmentação sem ganho estrutural.  
  **Sugestão:** Mesclar com `LoginControllerTest` em seção/nested class específica.

- **Arquivo:** `backend/src/test/java/sgc/subprocesso/SubprocessoControllerTest.java`  
  **Teste:** suíte da classe (7 testes)  
  **Categoria:** Test suite cohesion  
  **Problema:** Mesmo domínio possui duas suítes grandes adjacentes (`SubprocessoControllerCoverageTest` e `...CoverageExtraTest`), reduzindo coesão do conjunto.  
  **Sugestão:** Consolidar em uma suíte única orientada por recursos/endpoint.

---

## 7) Description quality

### Medium
- **Arquivo:** `backend/src/test/java/sgc/subprocesso/model/SubprocessoTest.java`  
  **Teste:** `gettersNonNull` (`@DisplayName("Getters NonNull should return values")`)  
  **Categoria:** Description quality  
  **Problema:** Nome em inglês e focado em detalhe técnico (getter), não em comportamento de negócio.  
  **Sugestão:** Renomear para português e para resultado observável (ou remover se for teste trivial).

- **Arquivo:** `backend/src/test/java/sgc/subprocesso/SubprocessoControllerTest.java`  
  **Teste:** nested displays `CRUD Operations`, `Cadastro workflow`, `Mapa workflow`  
  **Categoria:** Description quality  
  **Problema:** Mistura idioma e usa rótulos genéricos; baixa clareza do cenário/resultado esperado.  
  **Sugestão:** Padronizar em português com formato “Dado/Quando/Então” no `@DisplayName`.

### Low
- **Arquivo:** `backend/src/test/java/sgc/subprocesso/SubprocessoControllerCoverageExtraTest.java`  
  **Teste:** diversos com sufixo “- ok”  
  **Categoria:** Description quality  
  **Problema:** “ok” é vago e não comunica o resultado funcional validado.  
  **Sugestão:** Substituir por descrição explícita do efeito de negócio esperado.

---

## 8) Coverage padding

### High
- **Arquivo:** `backend/src/test/java/sgc/seguranca/login/dto/UsuarioAcessoAdTest.java`  
  **Teste:** `devePermitirGettersESetters`  
  **Categoria:** Coverage padding  
  **Problema:** Teste puramente de getters/setters (POJO), sem risco funcional real; tende a inflar cobertura artificialmente.  
  **Sugestão:** Remover ou substituir por teste de serialização/validação contratual relevante.

### Medium
- **Arquivo:** `backend/src/test/java/sgc/mapa/model/CompetenciaTest.java`  
  **Teste:** `deveInstanciarViaBuilder`  
  **Categoria:** Coverage padding  
  **Problema:** Valida apenas construção e acesso simples de atributos, sem regra de domínio.  
  **Sugestão:** Cobrir invariantes da entidade (validação, relacionamento, consistência).

- **Arquivo:** `backend/src/test/java/sgc/comum/model/EntidadeBaseTest.java`  
  **Teste:** `deveGerarToStringComNomeClasseECodigo`  
  **Categoria:** Coverage padding  
  **Problema:** Asserção de `toString()` tem baixo valor para regressão funcional e alto custo de manutenção quando formato muda.  
  **Sugestão:** Manter apenas se houver requisito explícito de logging/observabilidade dependente desse formato.

---

## 9) Comment quality

### Medium
- **Arquivo:** `backend/src/test/java/sgc/processo/model/ModelCoverageTest.java`  
  **Teste:** `deveRemoverParticipantesAoSincronizar`  
  **Categoria:** Comment quality  
  **Problema:** Comentário “cobre true/false do removeIf em Proceso.java:86” descreve implementação/linha, não intenção de negócio; fica obsoleto com refatoração.  
  **Sugestão:** Trocar por comentário orientado a comportamento (regra funcional) ou remover.

### Low
- **Arquivo:** `backend/src/test/java/sgc/processo/ProcessoControllerTest.java`  
  **Teste:** `deveListarUnidadesParaImportacaoComSubprocessoNulo`  
  **Categoria:** Comment quality  
  **Problema:** Comentário “forçar subprocesso = null na iteração” repete exatamente o que o stub já torna explícito no código.  
  **Sugestão:** Remover comentário redundante e manter apenas nomes claros de teste/variáveis.

---

## Execução completa da suíte e resumo de cobertura

- **Execução solicitada:** `./gradlew :backend:test`
- **Resultado:** 1126 testes executados, 1126 aprovados, 0 falhas.
- **Relatório de cobertura:** `./gradlew :backend:jacocoTestReport`
- **Resumo JaCoCo (backend):**
  - **Instruction:** 97,89% (16524/16880)
  - **Branch:** 91,22% (1143/1253)
  - **Line:** 99,00% (3557/3593)
  - **Complexity:** 92,00% (1299/1412)
  - **Method:** 98,72% (770/780)
  - **Class:** 100,00% (66/66)

> Observação crítica: a alta cobertura estrutural não elimina bugs de negócio; por isso o foco do checklist continua sendo
> qualidade de oráculo, rastreabilidade de requisito e validação de invariantes (não apenas status HTTP).

---

## Tabela-resumo

| Categoria | Qtde. achados |
|---|---:|
| Corrupted test oracles | 2 |
| Business rule coverage | 2 |
| Fixture integrity | 1 |
| Requirement traceability | 3 |
| Functional overlap | 2 |
| Test suite cohesion | 2 |
| Description quality | 3 |
| Coverage padding | 3 |
| Comment quality | 2 |
| **Total** | **20** |
