# Problemas Encontrados Durante a Implementação da CDU-17

Este documento detalha os problemas encontrados e o progresso feito na implementação do Caso de Uso 17 (CDU-17).

## Progresso Realizado

A lógica de negócio para a disponibilização do mapa de competências foi implementada conforme o plano, incluindo:
- Adição de verificações de autorização (`@PreAuthorize`) e pré-condições de estado do subprocesso.
- Implementação de validações para garantir a associação completa entre atividades e competências.
- Correção da lógica de persistência para salvar observações e criar a movimentação correta (origem SEDOC).
- Refinamento das notificações por e-mail e alertas para corresponderem às especificações do CDU.
- Implementação da limpeza de dados históricos (sugestões e análises de validação anteriores).
- Padronização da resposta da API para retornar `ResponseEntity<RespostaDto>`.
- Criação de uma suíte de testes de integração (`CDU17IntegrationTest.java`) para cobrir os cenários de sucesso e falha.

## Problemas Atuais nos Testes

Apesar do progresso na implementação da funcionalidade, os testes de integração estão falhando, impedindo a conclusão da tarefa. Os problemas são os seguintes:

1.  **`TransientObjectException` em Testes de Segurança:**
    - **Erro:** `org.hibernate.TransientObjectException: object references an unsaved transient instance - save the transient instance before flushing : sgc.comum.modelo.Usuario`
    - **Causa Provável:** Ocorre no método `setUp` do teste `disponibilizarMapa_semPermissao_retornaForbidden`. A factory `WithMockGestorSecurityContextFactory` cria um `Usuario` e o associa a uma `Unidade`. No `@BeforeEach` do teste, a tentativa de `unidadeRepo.deleteAll()` falha porque a `Unidade` tem uma referência a um `Usuario` (o `titular`) que não foi salvo e não faz parte do ciclo de vida do Hibernate no momento da limpeza.
    - **Tentativas de Correção:** Adicionei `usuarioRepo.deleteAll()` antes de `unidadeRepo.deleteAll()` para garantir a ordem correta de limpeza, mas o erro persiste. A próxima etapa seria usar `@DirtiesContext` para forçar a recriação do contexto Spring a cada teste, garantindo um isolamento completo.

2.  **Falhas de Asserção em Respostas de Erro de Validação:**
    - **Erro:** `AssertionError: Status expected:<400> but was:<422>` (corrigido) e subsequentemente `No results for path: $['...']`.
    - **Causa Provável:** Os testes que verificam a falha de validação (e.g., atividade sem competência) esperavam um status HTTP 400 (Bad Request). A correção para esperar 422 (Unprocessable Entity) foi feita, mas a estrutura do corpo do JSON de erro não corresponde à asserção do teste. A `ErroValidacao` é lançada corretamente, mas o `RestExceptionHandler` parece serializá-la de uma forma que não inclui o campo `subErrors` ou coloca os detalhes em um local diferente do esperado pelo `jsonPath`.
    - **Próximos Passos:** A próxima etapa seria inspecionar `RestExceptionHandler.java` para entender como a `ErroValidacao` é convertida em JSON e ajustar a asserção `jsonPath` no teste para corresponder à estrutura real da resposta.

Devido a esses bloqueios nos testes, a implementação não pode ser validada e submetida com segurança. As alterações no código-fonte da aplicação foram mantidas para análise futura.