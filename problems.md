# Problemas Atuais

## 1. Falha na Inicialização do Backend

O servidor backend não está iniciando com o perfil `local`, o que impede a verificação E2E e o desenvolvimento frontend. A causa raiz parece ser um `BeanCreationException` durante a execução do script `data.sql`.

### Histórico de Erros e Correções:

1.  **`JwtMockFilter` ausente**: O `SecurityConfig` tinha uma dependência de um `JwtMockFilter` que só existia nos perfis `e2e` e `test`. Isso foi corrigido removendo a dependência do filtro e o próprio arquivo `JwtMockFilter.java`, que se tornou obsoleto após a refatoração dos testes E2E.
2.  **Violação de Constraint (Check)**: O `data.sql` continha um erro de digitação no tipo de uma unidade (`INTERMEDIaria` em vez de `INTERMEDIARIA`), causando uma falha de constraint. Isso foi corrigido.
3.  **Violação de Constraint (Integridade Referencial)**: O `data.sql` tinha uma ordem incorreta de `INSERT`s, onde uma unidade filha era inserida antes de sua unidade pai. A ordem foi corrigida.

### Situação Atual:

Mesmo após as correções acima, o backend ainda falha ao iniciar, e o script de verificação do Playwright não consegue fazer login. A investigação foi interrompida e precisa ser retomada a partir da análise do último log de erro do backend para identificar a próxima falha no `data.sql` ou em outra parte da configuração.

## 2. Bloqueio na Verificação Frontend

Devido à falha do backend, o script de verificação do Playwright não consegue executar o fluxo de login com sucesso, bloqueando a etapa de verificação visual das alterações.
