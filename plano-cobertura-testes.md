# Plano de Ação para Aumento da Cobertura de Testes

## 1. Visão Geral

A análise do relatório de cobertura (JaCoCo) revelou uma cobertura de linhas de código geral de aproximadamente **13%**. Vários pacotes críticos para a lógica de negócio da aplicação estão com cobertura muito baixa ou zerada.

Este documento estabelece um plano de ação para aumentar a cobertura de testes de forma estratégica, priorizando as áreas mais críticas para garantir a qualidade, estabilidade e manutenibilidade do backend.

## 2. Estratégia Geral

1. **Priorização por Criticidade:** Focar primeiro nos pacotes que contêm a lógica de negócio principal (serviços, controllers) e que atualmente possuem a menor cobertura.
2. **Tipos de Testes:**
    * **Testes de Unidade:** Para classes de Serviço (`Service`), Mapeadores (`Mapper`) e componentes de lógica de negócio. O objetivo é testar cada unidade de forma isolada, usando mocks para dependências externas (como repositórios e outros serviços).
    * **Testes de Integração:** Para as classes de Controller (`Controller`), validando os endpoints da API, o fluxo de requisição/resposta, a desserialização de DTOs e a resposta HTTP correta.
3. **Exclusões Iniciais:** DTOs, Entidades JPA, Repositórios (interfaces) e classes de Configuração (`config`) são considerados de baixa prioridade para testes de unidade e serão abordados em uma fase posterior ou conforme a necessidade.

## 3. Plano de Ação por Prioridade

A seguir estão os pacotes e classes priorizados para a criação de testes.

### Prioridade 1: Pacotes Críticos com Cobertura Próxima de 0%

Estes pacotes são a base da lógica de negócio e precisam de atenção imediata.

#### 3.1. Pacote `sgc.alerta` (Cobertura: 0%)

* **Classe Alvo:** `AlertaServiceImpl`
* **Plano:**
  * Criar testes de unidade para o método `criarAlerta`, validando a criação correta do objeto `Alerta` e a persistência.
  * Testar o método `criarAlertasProcessoIniciado`, cobrindo a lógica de iteração e a criação de múltiplos alertas.
  * Validar os métodos de formatação e criação de alertas para diferentes cenários (`CadastroDisponibilizado`, `CadastroDevolvido`).

#### 3.2. Pacote `sgc.competencia` (Cobertura: 0%)

* **Classes Alvo:** `CompetenciaController`, `CompetenciaAtividadeController`
* **Plano:**
  * Criar testes de integração para o CRUD de `Competencia` no `CompetenciaController`.
  * Desenvolver testes de integração para `CompetenciaAtividadeController`, focando na lógica de `vincular` e `desvincular` competências de atividades.

#### 3.3. Pacote `sgc.sgrh.service` (Cobertura: 0%)

* **Classe Alvo:** `SgrhServiceImpl`
* **Plano:**
  * Criar testes de unidade que isolem a lógica de negócio do serviço.
  * Utilizar mocks para os repositórios do SGRH (`VwUsuarioRepository`, `VwUnidadeRepository`, etc.) para simular as respostas do banco de dados legado.
  * Testar a lógica de `construirArvoreHierarquica`, validando a correta montagem da estrutura de unidades.
  * Testar os métodos de busca (`buscarUsuarioPorEmail`, `buscarUnidadePorCodigo`, etc.).

### Prioridade 2: Pacotes de Negócio com Cobertura Muito Baixa

#### 3.4. Pacote `sgc.notificacao` (Cobertura: ~1%)

* **Classes Alvo:** `ProcessoEventListener`, `EmailNotificationService`
* **Plano:**
  * Testar o `ProcessoEventListener` para garantir que os eventos do Spring estão sendo capturados e os e-mails correspondentes são disparados.
  * Para `EmailNotificationService`, criar testes de unidade usando um mock do `JavaMailSender` para verificar que os métodos `send` são chamados com os parâmetros corretos (destinatário, assunto, corpo do e-mail).

#### 3.5. Pacote `sgc.mapa` (Cobertura: ~10%)

* **Classes Alvo:** `MapaServiceImpl`, `ImpactoMapaServiceImpl`, `MapaController`
* **Plano:**
  * **`MapaServiceImpl`:** Testar unitariamente os fluxos de `obterMapaCompleto` e `salvarMapaCompleto`, mockando repositórios e outros serviços.
  * **`ImpactoMapaServiceImpl`:** Criar testes de unidade para a complexa lógica de `verificarImpactos`, `detectarAtividades...` e `identificarCompetenciasImpactadas`.
  * **`MapaController`:** Criar testes de integração para os principais endpoints, como `obterMapaCompleto` e `salvarMapaCompleto`.

### Prioridade 3: Melhoria Contínua

#### 3.6. Pacote `sgc.processo` (Cobertura: ~26%)

* **Classes Alvo:** `ProcessoService`, `ProcessoController`, `ProcessoDetalheMapper`
* **Plano:**
  * Aumentar a cobertura do `ProcessoService`, focando nos métodos com lógica de negócio complexa que ainda não estão cobertos, como `startRevisionProcess` e `finalizeProcess`.
  * Adicionar testes de integração para o `ProcessoController` para cobrir os endpoints de início e finalização de processos.

#### 3.7. Pacote `sgc.subprocesso` (Cobertura: ~15%)

* **Classe Alvo:** `SubprocessoService`
* **Plano:**
  * Este é o maior e mais complexo serviço. A estratégia é abordá-lo de forma incremental, focando em um fluxo por vez.
  * **Primeiro Alvo:** O fluxo de "Disponibilização" (`disponibilizarCadastroAcao`, `disponibilizarRevisaoAcao`).
  * **Segundo Alvo:** O fluxo de "Validação" (`devolverValidacao`, `aceitarValidacao`, `homologarValidacao`).

## 4. Próximos Passos

1. Iniciar a implementação dos testes seguindo a ordem de prioridade definida neste plano.
2. A cada novo conjunto de testes implementado para um pacote ou classe, executar o comando `./gradlew :backend:test` para gerar um novo relatório de cobertura e medir o progresso.
3. Atualizar este plano conforme a cobertura aumenta e novas prioridades são identificadas.
