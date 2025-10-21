# Plano de Ajuste de Endpoints da API

## Objetivo
Ajustar os endpoints da API para utilizar exclusivamente os métodos HTTP `POST` e `GET`, devido à restrição de uso dos métodos `PUT` e `DELETE` no ambiente.

## Estratégia Geral

1.  **Conversão de PUT para POST:** Todos os endpoints que atualmente utilizam o método `PUT` serão convertidos para `POST`. O corpo da requisição (request body) será mantido, e a semântica da operação (atualização de recurso) será preservada. O path do endpoint pode ser ajustado para refletir a operação de atualização, se necessário, para evitar ambiguidade.
2.  **Conversão de DELETE para POST:** Todos os endpoints que atualmente utilizam o método `DELETE` serão convertidos para `POST`. O identificador do recurso a ser excluído, que geralmente é passado na URL, será mantido na URL. Se houver a necessidade de passar informações adicionais para a exclusão, um corpo de requisição poderá ser adicionado ao `POST`.

## Endpoints a serem ajustados

### Endpoints PUT (Convertidos para POST)

| Endpoint Original (PUT) | Novo Endpoint (POST) | Descrição da Operação | Corpo da Requisição (se aplicável) | Observações |
| :---------------------- | :------------------- | :-------------------- | :--------------------------------- | :---------- |
| `/api/subprocessos/{codSubprocesso}` | `/api/subprocessos/{codSubprocesso}/atualizar` | Atualizar um subprocesso | `SubprocessoDto` | |
| `/api/subprocessos/{codSubprocesso}/mapa` | `/api/subprocessos/{codSubprocesso}/mapa/atualizar` | Atualizar mapa de um subprocesso | `SalvarMapaRequest` | |
| `/api/subprocessos/{codSubprocesso}/mapa-completo` | `/api/subprocessos/{codSubprocesso}/mapa-completo/atualizar` | Atualizar mapa completo de um subprocesso | `SalvarMapaRequest` | |
| `/api/subprocessos/{codSubprocesso}/mapa-ajuste` | `/api/subprocessos/{codSubprocesso}/mapa-ajuste/atualizar` | Atualizar ajustes de mapa de um subprocesso | `SalvarAjustesReq` | |
| `/api/processos/{codProcesso}` | `/api/processos/{codProcesso}/atualizar` | Atualizar um processo | `AtualizarProcessoReq` | |
| `/api/mapas/{codMapa}` | `/api/mapas/{codMapa}/atualizar` | Atualizar um mapa | `MapaDto` | |
| `/api/competencias/{codCompetencia}` | `/api/competencias/{codCompetencia}/atualizar` | Atualizar uma competência | `CompetenciaDto` | |
| `/api/atividades/{codAtividade}` | `/api/atividades/{codAtividade}/atualizar` | Atualizar uma atividade | `AtividadeDto` | |
| `/api/atividades/{codAtividade}/conhecimentos/{codConhecimento}` | `/api/atividades/{codAtividade}/conhecimentos/{codConhecimento}/atualizar` | Atualizar um conhecimento | `ConhecimentoDto` | |

### Endpoints DELETE (Convertidos para POST)

| Endpoint Original (DELETE) | Novo Endpoint (POST) | Descrição da Operação | Corpo da Requisição (se aplicável) | Observações |
| :------------------------- | :------------------- | :-------------------- | :--------------------------------- | :---------- |
| `/api/subprocessos/{codSubprocesso}` | `/api/subprocessos/{codSubprocesso}/excluir` | Excluir um subprocesso | (Nenhum, ID na URL) | |
| `/api/processos/{codProcesso}` | `/api/processos/{codProcesso}/excluir` | Excluir um processo | (Nenhum, ID na URL) | |
| `/api/mapas/{codMapa}` | `/api/mapas/{codMapa}/excluir` | Excluir um mapa | (Nenhum, ID na URL) | |
| `/api/competencias/{codCompetencia}` | `/api/competencias/{codCompetencia}/excluir` | Excluir uma competência | (Nenhum, ID na URL) | |
| `/api/atividades/{codAtividade}` | `/api/atividades/{codAtividade}/excluir` | Excluir uma atividade | (Nenhum, ID na URL) | |
| `/api/atividades/{codAtividade}/conhecimentos/{codConhecimento}` | `/api/atividades/{codAtividade}/conhecimentos/{codConhecimento}/excluir` | Excluir um conhecimento | (Nenhum, IDs na URL) | |
| `/api/competencias/{codCompetencia}/atividades/{codAtividade}` | `/api/competencias/{codCompetencia}/atividades/{codAtividade}/desvincular` | Desvincular atividade de competência | (Nenhum, IDs na URL) | |

## Próximos Passos

## Próximos Passos (Instruções para Agentes)

1.  **Implementar as mudanças nos controladores da API no backend:**
    *   **Ação:** Identificar todos os arquivos Java de controladores (`@RestController`) no diretório `backend/src/main/java/sgc/**/controller/` que contêm os endpoints listados nas tabelas acima.
    *   **Ação:** Para cada arquivo de controlador identificado:
        *   Ler o conteúdo do arquivo.
        *   Localizar os métodos de endpoint que correspondem aos `Endpoint Original (PUT)` e `Endpoint Original (DELETE)` nas tabelas.
        *   **Para endpoints PUT:**
            *   Alterar a anotação `@PutMapping` para `@PostMapping`.
            *   Atualizar o caminho do endpoint para o `Novo Endpoint (POST)` correspondente.
            *   Renomear o parâmetro de path de `{id}` para `{codigo}` (ou `{codSubprocesso}`, `{codProcesso}`, etc., conforme a padronização).
            *   Renomear o parâmetro de path de `{atividadeId}` para `{codAtividade}`.
            *   Renomear o parâmetro de path de `{conhecimentoId}` para `{codConhecimento}`.
            *   Renomear o parâmetro de path de `{idCompetencia}` para `{codCompetencia}`.
            *   Garantir que o corpo da requisição (`@RequestBody`) e o tipo de retorno estejam corretos.
        *   **Para endpoints DELETE:**
            *   Alterar a anotação `@DeleteMapping` para `@PostMapping`.
            *   Atualizar o caminho do endpoint para o `Novo Endpoint (POST)` correspondente.
            *   Renomear o parâmetro de path de `{id}` para `{codigo}` (ou `{codSubprocesso}`, `{codProcesso}`, etc., conforme a padronização).
            *   Renomear o parâmetro de path de `{atividadeId}` para `{codAtividade}`.
            *   Renomear o parâmetro de path de `{conhecimentoId}` para `{codConhecimento}`.
            *   Renomear o parâmetro de path de `{idCompetencia}` para `{codCompetencia}`.
            *   Se o endpoint DELETE original não possuía corpo, o novo POST também não deve ter, a menos que a lógica de exclusão exija dados adicionais.
        *   Salvar as alterações no arquivo.

2.  **Atualizar a documentação da API:**
    *   **Ação:** Após a implementação das mudanças no backend, gerar novamente a documentação da API (se for gerada automaticamente, como com SpringDoc/Swagger).
    *   **Ação:** Verificar se a documentação reflete corretamente os novos endpoints `POST` e os parâmetros padronizados (`cod...`).

3.  **Atualizar o frontend para consumir os novos endpoints:**
    *   **Ação:** Identificar os arquivos no frontend que fazem chamadas aos endpoints alterados.
    *   **Ação:** Para cada arquivo identificado:
        *   Atualizar o método HTTP de `PUT` para `POST` e de `DELETE` para `POST`.
        *   Atualizar o caminho do endpoint para o novo caminho `POST`.
        *   Ajustar os parâmetros enviados na requisição para corresponderem aos novos nomes (`cod...`).
        *   Salvar as alterações no arquivo.

4.  **Atualizar o JavaDoc para refletir as mudanças nos endpoints e parâmetros:**
    *   **Ação:** Revisar e atualizar os comentários JavaDoc nos métodos dos controladores e DTOs afetados para refletir as mudanças nos métodos HTTP, caminhos dos endpoints e nomes dos parâmetros (`cod...`).
    *   **Sugestão:** Adicionar um gerador/validador/linter de JavaDoc ao build do Gradle para feedback imediato sobre pendências.

5.  **Atualizar os testes existentes e criar novos testes, se necessário, para cobrir as mudanças nos endpoints:**
    *   **Ação:** Identificar os arquivos de teste (unitários e de integração) que testam os endpoints alterados.
    *   **Ação:** Para cada arquivo de teste identificado:
        *   Atualizar as chamadas aos endpoints para usar o método `POST` e os novos caminhos.
        *   Ajustar os parâmetros e corpos de requisição nos testes para corresponderem aos novos nomes (`cod...`).
        *   Criar novos testes, se necessário, para garantir que a nova lógica de `POST` para operações de atualização/exclusão funcione corretamente.
        *   Salvar as alterações no arquivo.

6.  **Realizar testes abrangentes para garantir que todas as funcionalidades continuam operacionais:**
    *   **Ação:** Executar a suíte de testes completa do projeto (backend e frontend).
    *   **Ação:** Realizar testes manuais ou automatizados de ponta a ponta para verificar a integração entre frontend e backend com os novos endpoints.
    *   **Ação:** Monitorar logs e métricas para identificar quaisquer regressões ou problemas de desempenho.
