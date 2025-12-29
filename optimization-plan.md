# Plano de Otimização de Performance

Este documento detalha os pontos críticos de performance identificados através da infraestrutura de profiling introduzida e propõe soluções para otimização.

## 1. Metodologia de Análise

Foi introduzido um aspecto de monitoramento (`MonitoramentoAspect`) utilizando Spring AOP para interceptar execuções de métodos em Serviços e Repositórios. A análise foi realizada rodando a suíte de testes de integração do backend (`:backend:test`), simulando fluxos de negócio reais.

## 2. Pontos Críticos Identificados

### 2.1. `ProcessoService.iniciarProcessoMapeamento`
*   **Sintoma:** Execução única levando **~585 ms**.
*   **Causa Provável:** Este método orquestra a criação de subprocessos, envio de notificações e cópia de estruturas. A lentidão sugere operações sequenciais de banco de dados ou envio síncrono de e-mails/notificações dentro da transação principal.
*   **Evidência:** Logs mostram chamadas subsequentes para `NotificacaoModelosService.criarEmailDeProcessoIniciado` (495ms) e múltiplas queries.

### 2.2. `SubprocessoService.obterAtividadesSemConhecimento` e `validarExistenciaAtividades`
*   **Sintoma:** Múltiplas execuções rápidas (2-40ms) mas repetitivas (N+1).
*   **Frequência:** Ocorreu dezenas de vezes durante os testes de validação de fluxo.
*   **Causa:** O método itera sobre atividades de um mapa e, para cada uma, faz uma consulta ao banco para verificar a existência de conhecimentos (`ConhecimentoRepo.findByAtividadeCodigo`).
*   **Evidência:**
    ```text
    WARN  s.i.m.MonitoramentoAspect - EXECUCAO LENTA: sgc.subprocesso.service.SubprocessoService.validarExistenciaAtividades levou 40 ms
    WARN  s.i.m.MonitoramentoAspect - EXECUCAO LENTA: sgc.mapa.model.AtividadeRepo.findByMapaCodigo levou 20 ms
    WARN  s.i.m.MonitoramentoAspect - EXECUCAO LENTA: sgc.mapa.model.ConhecimentoRepo.findByAtividadeCodigo levou 6 ms
    ```

## 3. Propostas de Otimização

### 3.1. Otimizar Validação de Atividades (Prioridade Alta)
Refatorar `obterAtividadesSemConhecimento` para evitar o problema de N+1 queries.

*   **Solução:** Utilizar `JOIN FETCH` ou Entity Graphs para carregar as Atividades junto com seus Conhecimentos em uma única consulta.
*   **Ação:**
    1.  Alterar `AtividadeRepo` para adicionar um método `findByMapaCodigoWithConhecimentos`.
    2.  Atualizar `SubprocessoService` para usar este método e verificar a lista de conhecimentos em memória, em vez de consultar o banco para cada atividade.

### 3.2. Otimizar Início de Processo (Prioridade Média)
Investigar o envio de e-mails síncrono.

*   **Solução:** Assegurar que o envio de e-mails seja assíncrono (usando `@Async` ou sistema de mensageria) para não bloquear a thread principal do processo.
*   **Ação:** Verificar a configuração do `SubprocessoEmailService` e `NotificacaoService`.

### 3.3. Monitoramento Contínuo
Manter o `MonitoramentoAspect` ativo com um limiar conservador (ex: 100ms ou 200ms) para detectar regressões em produção ou homologação.
