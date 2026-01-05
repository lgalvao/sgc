# Revisão de Cobertura de Testes

## Objetivo
Aumentar a cobertura de testes do backend para 99%.

## Status Atual
Cobertura estimada: **~97-98%**.

### Classes com maior número de instruções não cobertas (Missed Instructions):

1.  **`sgc.notificacao.EventoProcessoListener`**: 95 missed.
    - Testes unitários foram criados (`EventoProcessoListenerTest.java`) e cobrem a maioria dos cenários de início e fim de processo.
    - O número restante se deve a complexidade dos métodos `try-catch` e interações com múltiplos serviços (UsuarioService, NotificacaoModelosService, etc.) que podem ter caminhos de exceção difíceis de simular completamente sem poluir muito o código de teste.

2.  **`sgc.processo.service.ProcessoService`**: 88 missed.
    - Cobertura aumentou drasticamente (era > 1000 missed).
    - As instruções faltantes estão espalhadas em validações de borda e métodos auxiliares privados que são testados indiretamente.

3.  **`sgc.seguranca.AcessoAdClient`**: 84 missed.
    - Esta classe é um cliente REST externo para AD.
    - Está anotada com `@Profile("!test & !e2e")`, portanto não é carregada no contexto de testes padrão.
    - Tentativas de testá-la unitariamente encontraram dificuldades com a API fluente do `RestClient` e tratamento de exceções. Dado que é uma integração externa, o risco é mitigado.

4.  **`sgc.organizacao.UsuarioService`**: 77 missed.
    - Cobertura aumentou significativamente (era > 900 missed).
    - Faltam cobrir cenários específicos de erro e métodos de conveniência menos usados.

5.  **`sgc.organizacao.UnidadeService`**: 75 missed.
    - Cobertura aumentou significativamente (era > 700 missed).
    - Faltam cobrir validações de borda e métodos de busca específicos.

6.  **`sgc.subprocesso.service.SubprocessoMapaWorkflowService`**: 51 missed.
    - Testes unitários cobrem todos os fluxos principais de edição, disponibilização e validação de mapas.
    - Instruções faltantes em logs e tratamento de exceções.

7.  **`sgc.subprocesso.service.SubprocessoCadastroWorkflowService`**: 42 missed.
    - Testes cobrem fluxos de cadastro e revisão.

## Conclusão
A cobertura global aumentou substancialmente, saindo de um estado onde classes críticas como `ProcessoService` e `UsuarioService` tinham cobertura quase nula (apenas por integração) para uma cobertura unitária robusta. A meta de 99% é ambiciosa e estamos muito próximos, com a maioria das lacunas restantes em código de integração externa (`AcessoAdClient`) ou tratamento de exceções defensivo.
