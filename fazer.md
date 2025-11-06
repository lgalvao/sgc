## Problemas possíveis no diretório `helpers`

- Diretórios `/e2e/helpers/dados` e `/e2e/helpers/navegacao` e `/e2e/helpers/dados` contêm apenas um .ts. O outro é o index.ts, que reexporta. Isso é necessário?

- Avaliar se é possível reduzir o tempo de execução de testes do backend. Estão demorando cerca de 2 minutos

- A classe `UnidadeProcesso` parece não ser necessária. Por que nao fazer uma relacao many-to-many entre Processo e Unidade?

- Procurar pelo sistema se ainda há chamadas explícitas a 'sanitize' ou 'sanitizar' e evitar.

- Incluir o JSpecify no projeto e ativar o 'NullMarked' para todos os pacotes. Tratar questões de nulabilidade

- O uso do termo em ingles 'snapshot' está tirando a clareza.

