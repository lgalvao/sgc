# Documentação e Requisitos

Este diretório contém a documentação do sistema, requisitos (Casos de Uso), e scripts utilitários.

## Estrutura

- **`docs/`**: Documentação técnica detalhada, padrões de projeto, diretrizes de design, e regras de acesso.
- **`reqs/`**: Especificações de requisitos funcionais documentados como Casos de Uso (CDU-XX). Esta é a fonte de verdade para as regras de negócio e fluxos do sistema.
- **`scripts/`**: Scripts utilitários para CI/CD, verificação de qualidade, relatórios de cobertura, etc.

## Notas Importantes

- Os arquivos em `reqs/` (`cdu-xx.md`) devem ser usados como referência primária ao implementar novas funcionalidades ou escrever testes E2E.
- As regras de controle de acesso estão detalhadas em `docs/regras-acesso.md`. NUNCA altere essas regras para contornar falhas em testes; ajuste o fluxo do teste para respeitar as permissões estabelecidas.
