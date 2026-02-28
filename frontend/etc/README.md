# Diretório de Suporte (etc)

Contém recursos auxiliares que não fazem parte do código-fonte da aplicação, mas são essenciais para o desenvolvimento e
manutenção.

## Estrutura

* **`scripts/`**: Scripts de automação Node.js para:
    - Verificação de cobertura de testes.
    - Auditoria de acessibilidade.
    - Listagem de Test IDs.
    - Captura automática de telas.
* **`docs/`**: Documentação técnica aprofundada, guias de padrões e estratégias arquiteturais.

## Uso dos Scripts

A maioria dos scripts pode ser executada via `node etc/scripts/<nome-do-script>.cjs`. Consulte o arquivo `package.json`
para ver os atalhos disponíveis via `npm run`.
