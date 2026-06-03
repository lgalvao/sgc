# CDU-44 - Manter avaliação de consenso

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Existencia de processo de diagnóstico em andamento
- Existência de servidor da unidade com situação `Autoavaliação concluída`, `Avaliação de consenso criada` ou
  `Avaliação de consenso aprovada`

## Fluxo principal

1. No `Painel`, o usuario escolhe um processo de diagnostico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade do usuário.

3. O usuário aciona o card `Avaliações de consenso`.

4. O sistema mostra a tela `Avaliações de consenso`, contendo uma tabela dos servidores da unidade, com o nome do servidor e situação da avaliação individual.

5. O usuário aciona uma linha da tabela referente a um servidor.

6. O sistema mostra a tela `Avaliação de consenso de servidor` com:
    - título "Avaliação de consenso"
    - subtítulo com o nome do servidor
    - grade de competências da unidade com estrutura abaixo:

|                     | Importância |       |          | Domínio  |       |          |
  |:--------------------|-------------|-------|----------|----------|-------|----------|
| Competência         | Servidor    | Chefe | Consenso | Servidor | Chefe | Consenso |
| Desc. competência 1 | 1           | 5     | 3        | 2        | 2     | 2        |
| Desc. competência 2 | NA          | 2     | 2        | 2        | 3     | 2        |
| Desc. competência 2 | 3           | 2     | 3        | 3        | 1     | 2        |

  **Regras:**

- Os campos `Importância` e `Domínio` do _servidor_ devem ser nao-editáveis e serem preenchidos com os valores
  fornecidos na autoavaliação do servidor;
- Os campos `Importância` e `Domínio` do _chefe_ devem estar abertos para preenchimento;
- Os dois campos de `Consenso` devem estar abertos para preenchimento;
- Se os valores `Importância` e `Domínio` forem iguais, o sistema deve preencher automaticamente o valor de `Consenso` com o valor do compartilhado.

**IMPORTANTE**: Se o servidor já tiver aprovado a avaliação de consenso (ou seja, estiver na situacao
  `Avaliação de consenso aprovada`), o sistema permitirá apenas visualização dos dados acima.

7. Com a presença do servidor, o usuário preenche o valor de consenso.

8. O sistema salva automaticamente cada alteração realizada.

9. O usuário aciona o botão `Voltar` para mostrar a lista de servidores.

10. O sistema volta a mostrar a tela `Avaliações de consenso`, onde o usuário poderá proceder ao prenchimento de consenso dos outros servidores.