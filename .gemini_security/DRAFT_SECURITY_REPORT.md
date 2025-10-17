### Vulnerabilidade: Cross-Site Scripting (XSS)
- **Severidade:** Alta
- **Localização:** backend/src/main/java/sgc/competencia/dto/CompetenciaDto.java, linha 22
- **Conteúdo da Linha:** `public CompetenciaDto sanitize() { return this; }`
- **Descrição:** O método `sanitize()` na `CompetenciaDto` não está realizando a sanitização adequada do campo `descricao`. Isso permite que entradas maliciosas (como scripts) sejam armazenadas e, potencialmente, executadas no navegador do usuário quando a descrição for renderizada sem escape no frontend, levando a um ataque de Cross-Site Scripting (XSS).
- **Recomendação:** Implementar a sanitização da `descricao` usando uma biblioteca de sanitização de HTML, como o OWASP Java HTML Sanitizer (que já está sendo usado em `AtividadeDto`).

