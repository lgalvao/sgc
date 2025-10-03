# Regras para geração de código

Objetivo: padronizar scaffolding, DTOs e práticas de desenvolvimento neste repositório.

Regras principais:
- Usar português Brasileiro em todos os códigos
- DTOs: usar Lombok para getters/setters/construtores. Preferir as anotações @Getter, @Setter, @NoArgsConstructor e @AllArgsConstructor. Exemplo: [`backend/src/main/java/sgc/dto/LoginResponse.java`](backend/src/main/java/sgc/dto/LoginResponse.java:1).
- Validações: usar Jakarta Validation (pacote `jakarta.validation.constraints`) para anotações como @NotBlank, @Size etc.
- Imports: usar namespaces jakarta.* (ex.: `jakarta.validation`) em vez de javax.*.
- Controllers: validar requests com `@Valid` (do pacote `jakarta.validation`) nos métodos REST.
- Clientes externos: sempre definir uma interface e uma implementação; tornar a implementação substituível por profile (ex.: profile "test" injeta mock).
- Para testes use @MokitoBean e @MockitoSpyBean (NAO USE @MockitoBean e @SpyBean que estao deprecated)
- Para testes, use @SpringBootTest ou as anotações análogas mais específicas
- Token: expor TokenService como interface; usar stub em testes e trocar por JWT real quando implementado.
- Testes: testes unitários com Mockito/JUnit para services; integration tests com MockMvc
- Convenções de branch/commits: seguir a branch sugerida `feat/sprint1/cdu-01-auth` e commits descritos no plano.
- Documentação: documentar contratos de API em README/guias e referenciar requisitos (ex.: [`reqs/cdu-01.md`](reqs/cdu-01.md:1)).

Observações:
- Atualize este arquivo se novas regras forem identificadas ou acordadas.