# Módulo Diagnóstico

Este módulo gerencia o processo de Diagnóstico de Competências (`CDU-02` a `CDU-09` do sistema SGC).

## Visão Geral

O Diagnóstico permite que os servidores de uma unidade avaliem suas competências (Autoavaliação e Consenso), identifiquem Gaps e que a chefia defina Ocupações Críticas.

### Principais Entidades

- **Diagnostico**: Entidade raiz, vinculada 1:1 com `Subprocesso`. Gerencia o estado geral do diagnóstico da unidade.
- **AvaliacaoServidor**: Registra a autoavaliação (Importância e Domínio) de um servidor para uma competência específica. O Gap é calculado automaticamente (`Importancia - Dominio`).
- **OcupacaoCritica**: Registra a situação de capacitação para competências onde houve Gap significativo (>= 2).

### Fluxo de Trabalho

1. **Criação**: Ao iniciar um processo do tipo `DIAGNOSTICO`, o sistema cria automaticamente um subprocesso. O `Diagnostico` é criado sob demanda ao ser acessado.
2. **Autoavaliação (CDU-02)**: Servidores acessam o subprocesso e avaliam cada competência do mapa da unidade.
3. **Monitoramento (CDU-03)**: Chefia acompanha o progresso das avaliações.
4. **Consenso (CDU-04/05/06)**: *Não implementado nesta fase.* (Previsão futura).
5. **Ocupações Críticas (CDU-07)**: Chefia identifica necessidades de capacitação baseadas nos Gaps.
6. **Conclusão (CDU-09)**: Chefia conclui o diagnóstico da unidade. Se houver pendências, exige justificativa.

## Arquitetura Backend

- **Pacote**: `sgc.diagnostico`
- **Controller**: `DiagnosticoController` (REST API).
- **Service**: `DiagnosticoService` (Lógica de negócio).
- **Repository**: `DiagnosticoRepo`, `AvaliacaoServidorRepo`, `OcupacaoCriticaRepo`.
- **DTOs**: `DiagnosticoDto`, `AvaliacaoServidorDto`, etc.

## Frontend

O frontend foi refatorado para utilizar serviços dedicados e componentes Vue focados nos casos de uso.

- **Serviço**: `diagnosticoService.ts`.
- **Views**:
  - `AutoavaliacaoDiagnostico.vue`: Interface para o servidor avaliar competências.
  - `MonitoramentoDiagnostico.vue`: Dashboard para a chefia.
  - `OcupacoesCriticasDiagnostico.vue`: Interface para definição de ocupações críticas.
  - `ConclusaoDiagnostico.vue`: Tela de encerramento do diagnóstico.
- **Rotas**: Definidas em `router/diagnostico.routes.ts`.

## Testes

- **Unitários**: `DiagnosticoServiceTest.java`.
- **E2E**: `e2e/fluxo-geral-diagnostico.spec.ts` (Teste de fluxo completo: Criação de Mapa -> Criação de Diagnóstico -> Autoavaliação -> Monitoramento -> Ocupações Críticas -> Conclusão).
