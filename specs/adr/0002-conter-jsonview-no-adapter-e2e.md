# ADR 0002: Conter JsonView no Legado de Model e no Adapter E2E

## Status

Aceita

## Contexto

`JsonView` foi usado no SGC como mecanismo de reaproveitamento de estruturas entre múltiplos contratos. Isso reduziu a
clareza da borda HTTP e espalhou dependência de serialização condicional por DTOs, controllers e entidades.

Após a migração dos contratos principais para DTOs explícitos, o uso restante de `JsonView` ficou concentrado quase todo
em:

- entidades/modelos legados;
- adapter técnico de `e2e`.

## Decisão

`JsonView` deixa de ser ferramenta válida para código novo da aplicação.

Ele fica contido apenas em:

- classes de `..model..` ainda legadas;
- `E2eController` e infraestrutura técnica estritamente necessária para fixtures de teste.

Fora dessa ilha:

- controllers não usam `@JsonView`;
- DTOs públicos não usam `@JsonView`;
- classes de aplicação não devem voltar a depender de `*Views`.

## Consequências

### Positivas

- a direção arquitetural fica inequívoca;
- o legado restante fica visível e isolado;
- reduzimos o risco de regressão para contratos implícitos.

### Custos

- fixtures E2E continuam carregando herança técnica temporária;
- a remoção completa do legado de `JsonView` em entidades exige uma frente futura específica.

## Guardrails

- [ArchConsistencyTest.java](/Users/leonardo/sgc/backend/src/test/java/sgc/arquitetura/ArchConsistencyTest.java)
- [plano-qualidade-diretrizes.md](/Users/leonardo/sgc/etc/docs/plano-qualidade-diretrizes.md)

## Próximo passo natural

Quando as fixtures E2E deixarem de depender de retorno serializado de entidades, a próxima etapa é eliminar `JsonView`
também dos modelos legados que ainda o carregam.
