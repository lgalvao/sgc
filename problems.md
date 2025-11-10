# Problemas Pendentes

## Testes de Integração Falhando

Após uma série de correções que resolveram a maioria das falhas nos testes de backend, os seguintes problemas persistem:

### 1. `CDU05IntegrationTest` - Iniciar Processo de Revisão

- **Falhas:** `testIniciarProcessoRevisao_processoJaIniciado_falha` e `testIniciarProcessoRevisao_sucesso`.
- **Sintoma:** A API retorna um erro `500 Internal Server Error` em vez dos códigos de status de erro de negócio esperados (ex: 422, 409) ou de sucesso (200).
- **Investigação:**
    - A causa raiz parece ser uma `ConstraintViolationException` não tratada no `ProcessoService` quando se tenta criar a `UnidadeMapa` sem as informações corretas.
    - As tentativas de corrigir a configuração do teste em `CDU05IntegrationTest` para persistir a `UnidadeMapa` corretamente não tiveram sucesso. A lógica em `ProcessoService` para iniciar um processo de revisão depende de uma `UnidadeMapa` pré-existente, e a falha do teste em configurar isso corretamente causa a falha.

### 2. `ImpactoCompetenciaService` e `CDU12IntegrationTest`

- **Falhas:** `ImpactoCompetenciaServiceTest` e dois testes em `CDU12IntegrationTest` (`deveIdentificarCompetenciasImpactadas` e `deveDetectarAtividadesRemovidas`).
- **Sintoma:** O `ImpactoCompetenciaService` não está conseguindo identificar corretamente as competências que são impactadas por mudanças nas atividades. Os testes esperam que o serviço encontre competências vinculadas, mas o serviço retorna uma lista vazia.
- **Investigação:**
    - O problema central é a forma como o serviço e os testes estão tratando a relação `@ManyToMany` entre `Competencia` e `Atividade`.
    - As tentativas de corrigir a lógica de persistência e a configuração dos mocks não foram suficientes. A causa provável é que a lógica no `ImpactoCompetenciaService` não está carregando ou consultando as associações da forma que o JPA espera em um ambiente de teste transacional.

### 3. `CDU02IntegrationTest` - Visibilidade de Alertas e Processos

- **Falhas:** `testListarAlertas_UsuarioVeAlertasDaSuaUnidade`, `testListarProcessos_GestorRaiz_VeTodos`, `testListarProcessos_ChefeUnidadeFilha1_VeProcessosSubordinados`.
- **Sintoma:** As consultas para listar alertas e processos não estão retornando o número esperado de resultados.
- **Investigação:**
    - A tentativa de corrigir o `setupSecurityContext` para usar um `Usuario` persistido não resolveu o problema.
    - A causa provável é um problema na lógica de consulta do `AlertaRepo` e `ProcessoRepo`, que não está filtrando corretamente os resultados com base na unidade do usuário e na hierarquia de unidades.

### 4. `CDU09IntegrationTest` - Disponibilizar Cadastro

- **Falhas:** `naoDevePermitirChefeDeOutraUnidadeDisponibilizar`, `deveDisponibilizarCadastroComSucesso`, `naoDeveDisponibilizarComAtividadeSemConhecimento`.
- **Sintoma:** A API retorna `400 Bad Request` em vez dos códigos de status esperados (403, 200, 422).
- **Investigação:** A causa raiz não foi investigada.

### 5. `CDU17IntegrationTest` - Disponibilizar Mapa de Competências

- **Falha:** `disponibilizarMapa_comAtividadeNaoAssociada_retornaBadRequest`.
- **Sintoma:** O teste falha com uma asserção de que o `details` do erro da API não deve ser nulo.
- **Investigação:** A tentativa de corrigir a persistência da relação entre `Atividade` e `Competencia` não resolveu o problema.

### 6. `CDU21IntegrationTest` - Finalizar Processo

- **Falha:** `finalizarProcesso_ComSucesso_DeveAtualizarStatusENotificarUnidades`.
- **Sintoma:** A API retorna `500 Internal Server Error` em vez de `200 OK`.
- **Investigação:** A causa raiz não foi investigada.

## Próximos Passos Sugeridos

Recomenda-se uma revisão aprofundada da lógica de persistência e consulta em `ImpactoCompetenciaService` e uma análise detalhada do ciclo de vida das entidades nos testes de integração, especialmente em `CDU05IntegrationTest`, para garantir que os dados de teste estejam no estado correto antes da execução das chamadas à API. Além disso, a lógica de consulta nos repositórios de Alerta e Processo deve ser revisada para garantir que a visibilidade dos dados esteja correta.
