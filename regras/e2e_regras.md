# Regras para corrigir problemas em testes end to end (e2e)

Se um testes end to end falhar, geralmente será por uma dessas causas:

- As expectativas do teste estão erradas.
- Dados nao estão presentes no banco de dados
- Elementos esperados não estão sendo mostrados porque alguma validação falhou no backend.
- A funcionalidade ainda não foi implementada completamente ou corretamente.

Nunca será porque um elemento não deve tempo de carregar ou renderizar. Então **aumentar o timeout NAO RESOLVERÁ NADA**!
Esse sistema está rodando localmente, com um banco H2 em memória. Tudo é rápido. Se um elemento nao aparece, é por
alguns dos motivos indicados acima.

Os testes e2e estão sendo usados para confirmar a implementação das funcionalidades do sistema. Portanto, se um teste
falhar, isso será um sinal de que devemos investigar as causas indicadas acima -- e corrigir o problema usando com base
a saída dos testes.

Ao rodar os testes e2e, tanto o frontend como o backend serão construídos e executados, e os logs de ambos serão
mostrados durante os testes. Então nao se preocupe em rodar o backend ou frontend separadamente.

Os testes que falharem geram arquivos `error-context.md`, com a situacao da tela no momento da falha -- nao deixe de ler
esses arquivos.

## Regras para execução de testes E2E

- **NUNCA rode apenas um cenário isolado**: Muitos testes usam `test.describe.serial()`, o que significa que os cenários
  dependem da execução sequencial dos anteriores. Rodar um cenário isolado causará falhas.
- **Sempre redirecione a saída para um arquivo**: Use `> resultado.txt 2>&1` ao rodar testes E2E para capturar toda a
  saída (stdout e stderr) em um arquivo de texto.
- **Use grep para analisar resultados**: Após redirecionar para arquivo, use `grep` para filtrar e analisar partes
  específicas da saída, como erros, logs do backend, ou mensagens específicas.