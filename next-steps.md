# Próximos Passos - Implementação e Testes dos Casos de Uso (CDU-27 a CDU-36)

Este documento registra o estado atual e as ações necessárias para concluir a verificação e testes dos casos de uso restantes.

## Estado Atual
- **CDU-22 a CDU-26 (Ações em Bloco):** Testes de integração implementados e aprovados. Backend verificado.

## Casos de Uso Pendentes

### CDU-27 - Alterar data limite de subprocesso
- **Status Backend:** Implementação identificada em `SubprocessoService.alterarDataLimite` e `SubprocessoCrudController`.
- **Ação Necessária:** Criar `CDU27IntegrationTest.java` para verificar o fluxo de alteração, persistência e envio de notificações.

### CDU-28 - Manter atribuição temporária
- **Status Backend:** Implementação identificada em `UnidadeService` (`criarAtribuicaoTemporaria`) e `UnidadeController`.
- **Ação Necessária:** Criar `CDU28IntegrationTest.java`. Verificar permissões e prioridade da atribuição temporária sobre a titularidade padrão.

### CDU-29 - Consultar histórico de processos
- **Status Backend:** Endpoints de listagem (`/finalizados`) e detalhes identificados em `ProcessoController`.
- **Ação Necessária:** Criar `CDU29IntegrationTest.java` para garantir que apenas processos finalizados são listados e que os detalhes são retornados corretamente.

### CDU-30 - Manter Administradores
- **Status Backend:** Entidade `Administrador` e repositório `AdministradorRepo` existem.
- **Status Requisito:** Arquivo `cdu-30.md` diz "Em construção".
- **Ação Necessária:** Verificar se existe Controller/Service para CRUD de administradores. Se não, implementar ou aguardar definição do requisito.

### CDU-31 a CDU-36
- **Ação Necessária:**
    1. Ler os arquivos de requisitos correspondentes.
    2. Verificar a existência da implementação no backend.
    3. Criar testes de integração (`CDUxxIntegrationTest.java`) para cada um.

## Observações Gerais
- Manter o padrão de testes de integração independentes e transacionais.
- Verificar sempre se as notificações (e-mail) e alertas internos estão sendo gerados conforme especificado nos requisitos.
