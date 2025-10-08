# Regras para geração de código

Objetivo: padronizar DTOs e práticas de desenvolvimento neste repositório.

Regras:
- Usar português Brasileiro em todos os códigos
- DTOs: usar Lombok para getters/setters/construtores. Preferir as anotações @Getter, @Setter, @NoArgsConstructor e @AllArgsConstructor.
- Imports: usar namespaces jakarta.* (ex.: `jakarta.validation`) em vez de javax.*.
- Controllers: validar requests com `@Valid` nos Metodos REST.
- Clientes externos: sempre definir uma interface e uma implementação; tornar a implementação substituível por profile (ex.: profile "test" injeta mock).
- Para testes use @MockitoBean e @MockitoSpyBean (NAO USE @MockitoBean e @SpyBean -- estao deprecated)
- Para testes, use @SpringBootTest ou as anotações análogas mais específicas
- Testes: testes unitários com Mockito/JUnit para services; integration tests com MockMvc

Observações:
- Atualize este arquivo se novas regras forem identificadas ou acordadas.