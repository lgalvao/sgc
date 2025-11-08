## Regras para Agentes

- O sistema deve ter todos os nomes e mensagens em português brasileiro
- Ao executar o gradle evite usar `--no-daemon` a não ser que seja estritamente necessário
- O nome das execões do sistema segue sempre o padrao 'ErroXxxx', por exemplo `ErroEntidadeNaoEncontrada`

### Se for rodar testes e2e (playwright)

- Rode sempre o mínimo de testes possível. Por exemplo, o comando abaixo roda apenas os primeiros cinco testes em
  `/cdu`, de `/cdu/cdu-01.spec.ts` até `/cdu/cdu-05.spec.ts` focando apenas nos últimos que falharam:

    ```shell
    npx playwright test /cdu/cdu-0[1-5] --last-failed
    ```

### Se for corrigir ou ajustar testes e2e (playwright)
-  Não usar timeouts explicitos.
-  Considerar que timeouts indicam que os elemntos não estão visíveis de fato, por questões de expectativas dos testes ou defeitos no sistema em si
