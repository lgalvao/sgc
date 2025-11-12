## Regras para Agentes

- Leia detalhes sobre o projeto no README.md na raiz do repositório.
- Cada pacote principal do backend tem um resumo de suas responsabilidades no README.md dentro do próprio pacote.
- Cada diretorio do frontend tambem tem um README.md

Algumas regrais gerais
- O sistema deve ter todos os nomes e mensagens em português brasileiro
- O nome das exceções do sistema segue sempre o padrao 'ErroXxxx', por exemplo `ErroEntidadeNaoEncontrada`
- O nome dos repositórios JPA seguem o padrão 'XxxxRepo', por exemplo `SubprocessoRepo`

### Ferramentas
- Ao executar o gradle evite usar `--no-daemon` a não ser que seja estritamente necessário

### Se for rodar testes e2e (playwright)
- Rode sempre o mínimo de testes possível. Por exemplo, o comando abaixo roda apenas os primeiros cinco testes em
  `/cdu`, de `/cdu/cdu-01.spec.ts` até `/cdu/cdu-05.spec.ts` focando apenas nos últimos que falharam:

    ```shell
    npx playwright test /cdu/cdu-0[1-5] --last-failed
    ```
  
### Se for corrigir ou ajustar testes e2e (playwright)
- Não usar timeouts explicitos: timeouts indicam que os elementos não estão visíveis de fato, por questões de expectativas dos testes ou defeitos no sistema em si