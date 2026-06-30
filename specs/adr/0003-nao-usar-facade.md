# ADR 0003 - Não Usar Facade no Backend Atual

## Status

Aceito

## Contexto

O backend do SGC acumulou classes nomeadas como `Facade` em momentos diferentes da sua evolução.

Na prática, esse nome passou a agrupar classes de naturezas distintas:

- services de aplicação;
- orquestrações de controller;
- hubs de fluxo;
- classes grandes sem fronteira arquitetural própria.

Isso criou ambiguidade sem ganho estrutural correspondente.

No estado atual do sistema, não existe uma camada separada e coerente que justifique `Facade` como categoria
arquitetural própria.

## Decisão

O backend do SGC não deve usar classes com sufixo `Facade`.

Direção oficial:

- fluxos de aplicação devem usar `*Service` ou `*AplicacaoService`;
- controllers devem depender dessas classes explicitamente nomeadas;
- testes, documentação e regras arquiteturais devem refletir essa linguagem;
- legados restantes com `Facade` devem ser reclassificados.

## Consequências

Ganhos esperados:

- maior clareza semântica;
- menos categorias artificiais;
- menos ambiguidade para agentes e desenvolvedores novos;
- maior consistência entre módulos.

Custos aceitos:

- renomeações amplas em código e testes;
- atualização de documentação e regras de arquitetura;
- necessidade de migrar legados remanescentes.

## Guardrails

- `ArchUnit` deve falhar se classes de produção terminarem com `Facade`.
- documentação normativa deve afirmar explicitamente que `Facade` não é padrão aceito.
- código novo não deve reintroduzir esse sufixo.

## Migração

- reclassificar remanescentes para `Service` ou `AplicacaoService` de acordo com o papel real;
- preferir cortes amplos por módulo para evitar meio sistema em linguagem antiga e meio em linguagem nova;
- não preservar o nome `Facade` por compatibilidade estética.
