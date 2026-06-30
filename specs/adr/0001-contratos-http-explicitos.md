# ADR 0001: Contratos HTTP Explícitos no Backend

## Status

Aceita

## Contexto

O backend do SGC acumulou duas direções concorrentes ao longo do tempo:

- contratos HTTP explícitos com DTOs dedicados;
- reaproveitamento de entidades, enums e serialização condicional para compor respostas.

Essa convivência aumentou o acoplamento entre backend e frontend, dificultou refatorações internas e tornou a borda HTTP
menos previsível.

## Decisão

O padrão oficial do backend passa a ser:

- controllers recebem request DTO;
- controllers respondem response DTO;
- `@RequestBody` não recebe entidade JPA;
- contratos HTTP não expõem tipos de `..model..` diretamente;
- tradução entre domínio e contrato fica em mapper explícito ou service de visualização/contexto;
- enums de domínio podem ser convertidas para `String` quando o contrato externo não precisa conhecer a enum interna.

## Consequências

### Positivas

- a API fica mais clara para frontend, testes e manutenção;
- refatorações no domínio deixam de vazar automaticamente para integração;
- o backend recupera liberdade para reorganizar entidades e relacionamentos sem quebrar contrato por acidente;
- montagem de resposta passa a ter ponto de verdade explícito.

### Custos

- haverá mais mappers e DTOs explícitos;
- testes de controller e integração precisam declarar dependências de mapper quando usarem `@WebMvcTest`;
- a migração do legado exige rodadas incrementais em módulos antigos.

## Guardrails

- [ArchConsistencyTest.java](/Users/leonardo/sgc/backend/src/test/java/sgc/arquitetura/ArchConsistencyTest.java)
- [backend-borda-http.md](/Users/leonardo/sgc/etc/docs/backend-borda-http.md)

## Não fazer

- voltar a expor entidade por conveniência;
- recolocar `fromEntity` ou montagem de resposta dentro de DTO público;
- tratar enum de domínio como contrato automático da API sem decisão explícita.
