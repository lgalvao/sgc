## Regras para corrigir problemas em testes e2e

Se um testes e2e falhar, geralmente será por uma dessas causas:

- As expectativas do teste estão erradas.
- Dados nao estão presentes no banco de dados
- Elementos esperados não estão sendo mostrados porque alguma validação falhou no backend. 

Nunca será porque um elemento não deve tempo de carregar ou renderizar. Então **aumentar o timeout NAO RESOLVERÁ NADA**! Esse sistema está rodando localmente, com um banco H2 em memória. Tudo é rápido. Se um elemento nao aparece, é por alguns dos motivos indicados acima.
