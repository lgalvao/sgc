# Status da Correção de Testes de Integração

## ✅ Concluído e Verificado

1.  **Correção de Schema e Dados (H2)**
    *   **`backend/src/test/resources/db/schema.sql`**:
        *   Renomeada coluna `usuario_titulo` para `usuario_codigo` na tabela `movimentacao`.
        *   Atualizada tabela `analise` para usar `unidade_codigo` e `usuario_titulo` em conformidade com o Plano de Migração.
    *   **`backend/src/test/resources/data.sql`**: Ajustado insert da tabela `movimentacao`.
    *   **`backend/src/main/java/sgc/mapa/model/MapaRepo.java`**: Corrigida query `findMapaVigenteByUnidade` para consultar `UnidadeMapa`.

2.  **Entidade `Analise` e Serviço**
    *   Atualizada entidade `Analise.java` para refletir as mudanças do banco (`unidadeCodigo`, `usuarioTitulo`).
    *   Atualizado `AnaliseService.java` para usar os novos campos e injetar `UnidadeRepo` para lookup de código por sigla.
    *   Atualizados testes de integração (`CDU13`, `CDU20`, `CDU10`) para usar os novos getters/setters da entidade `Analise`.

3.  **Correção de Teste `CDU07IntegrationTest`**
    *   Resolvido erro 403 (Chefe) injetando perfis via `JdbcTemplate` na view `VW_USUARIO_PERFIL_UNIDADE`.
    *   Resolvido erro 404 (Admin) atualizando `SubprocessoDtoService` e `SubprocessoDetalheDto` para incluir o titular da unidade.

4.  **Correção de Teste `CDU10IntegrationTest`**
    *   **STATUS: PASSOU (BUILD SUCCESSFUL)**.
    *   Aplicada correção de persistência de perfis e titularidade via `JdbcTemplate`.
    *   Configurado mock de `JavaMailSender` para retornar `MimeMessage` mockado, evitando NPE/500 no envio de e-mail.
    *   Corrigido erro de compilação (import estático de `mock`).

## ⚠️ Pendente / A Verificar

1.  **`CDU09IntegrationTest`**
    *   Aplicadas as mesmas correções do `CDU10` (JdbcTemplate + MimeMessage Mock).
    *   Provavelmente passará agora, dado que `CDU10` passou.

2.  **`CDU02IntegrationTest`**
    *   Falhava com erro 500. Aplicada correção na carga de perfis no `setupSecurityContext`.
    *   Precisa ser executado para confirmar a correção.

3.  **`CDU17IntegrationTest`**
    *   Falhava com 403 (e depois DuplicateKey). Removida inserção duplicada de perfil Admin.
    *   Precisa ser executado para confirmar se o perfil original do `data.sql` é reconhecido corretamente.

## Próximos Passos

1.  Executar a suite completa de testes para garantir que não houve regressão.
2.  Considerar a migração completa do código fonte para usar a nova estrutura da tabela `ANALISE` (DTOs, Mappers) para limpar comentários TODO.
