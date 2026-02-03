# Guia de Estilo: Java (Backend SGC)

## Princípios Gerais
- **Idioma:** Exclusivamente Português Brasileiro (pt-BR) para nomes de classes, variáveis, métodos e comentários.
- **Arquitetura:** Modular Monolith. Respeite as divisões por domínio (ex: `processo`, `mapa`, `atividade`).
- **Null-Safety:** Utilize as anotações do JSpecify para indicar nulidade.

## Nomenclatura
- **Classes:** PascalCase (ex: `ProcessoMapeamentoService`).
- **Métodos e Variáveis:** camelCase (ex: `salvarCadastro`, `unidadeResponsavel`).
- **Interfaces:** Não use prefixo 'I' (ex: `CalculoService`, não `ICalculoService`).
- **DTOs:** Sufixo `Dto` (ex: `MapaCompetenciasDto`).

## Padrões de Projeto
- **Fachadas (Services):** Concentre a lógica de negócio e orquestração.
- **Mapeamento:** Use MapStruct para conversões entre Entidades e DTOs.
- **Exceções:** Use exceções de negócio específicas (ex: `EntidadeNaoEncontradaException`).

## Testes
- **JUnit 6:** Utilize JUnit 6 para testes unitários e de integração.
- **Convenção:** Nomeie os testes de forma descritiva em português.
- **Cobertura:** Mínimo de 90% de branches e 99% de linhas (conforme `build.gradle.kts`).
