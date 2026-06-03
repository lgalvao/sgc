# Borda HTTP do Backend

Este documento define a direção oficial para contratos HTTP do backend do SGC.

O objetivo é maximizar clareza e consistência na integração com o frontend. A API deve ser compreensível sem depender de conhecimento implícito sobre entidades, `JsonView`, serialização condicional ou detalhes de persistência.

## Regras obrigatórias

- Controllers devem expor DTOs e requests explícitos.
- `@RequestBody` nunca deve receber entidade JPA.
- DTOs de resposta não devem expor tipos de `..model..` diretamente.
- DTOs de resposta devem preferir tipos estáveis de integração:
  - `String` para estados e perfis quando o frontend não precisa conhecer a enum interna.
  - DTOs de resumo para relacionamentos (`UnidadeResumoDto`, equivalentes).
  - coleções simples e explicitamente montadas.
- `@JsonView` não deve ser usado na borda HTTP dos módulos já migrados:
  - `processo`
  - `subprocesso`
  - `seguranca`
  - `alerta`
  - `configuracoes`

## O que evitar

- Retornar entidade JPA direto do controller.
- Reaproveitar entidade como payload HTTP “por conveniência”.
- Fazer um mesmo DTO servir múltiplos contratos por `@JsonView`.
- Expor enums ou objetos de domínio só porque “já existem”.
- Colocar lógica de montagem do contrato dentro do próprio DTO.

## Padrão preferido

1. O controller recebe request DTO.
2. O service executa a regra de negócio.
3. Um mapper explícito traduz domínio para contrato HTTP.
4. O controller responde com response DTO estável.

## Quando usar mapper explícito

Use mapper explícito quando pelo menos um destes pontos ocorrer:

- o contrato HTTP não é idêntico ao modelo interno;
- há conversão de enums, datas, permissões ou resumos relacionais;
- o mesmo domínio aparece em mais de um endpoint com shapes diferentes;
- a mudança pretende reduzir acoplamento frontend/backend.

## Exceções controladas

- O adapter `e2e` pode continuar usando `@JsonView` enquanto existir dependência operacional dele.
- Módulos ainda não migrados podem conviver temporariamente com `@JsonView`, mas não devem ser usados como referência para código novo.

## ADRs relacionadas

- [ADR 0001 - Contratos HTTP Explícitos no Backend](/Users/leonardo/sgc/etc/docs/adr/0001-contratos-http-explicitos.md)
- [ADR 0002 - Conter JsonView no Legado de Model e no Adapter E2E](/Users/leonardo/sgc/etc/docs/adr/0002-conter-jsonview-no-adapter-e2e.md)

## Regra de decisão

Se houver dúvida entre reaproveitar estrutura interna ou declarar contrato explícito, a decisão padrão é declarar contrato explícito.
