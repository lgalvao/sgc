# CDU-44 - Manter avaliação de consenso

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Existencia de processo de diagnóstico em andamento
- Existência de servidor da unidade com umas das situações:
  - 'Autoavaliação concluída', 
  - 'Avaliação de consenso criada'
  - 'Avaliação de consenso aprovada'

## Fluxo principal

1. No `Painel`, o usuario escolhe um processo de diagnostico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade, contendo detalhes do processo/subprocesso, cards acionáveis e a grade de servidores da unidade.
   
3. O usuário escolhe a ação `Avaliação de consenso` para um servidor.
   
4. O sistema mostra a tela `Avaliação de consenso` para o servidor, com:
    - título: "Avaliação de consenso"
    - subtítulo: nome e título do servidor
    - grade de competências da unidade com estrutura como no exemplo abaixo:

|                     | Importância |       |          | Domínio  |       |          |
|:--------------------|-------------|-------|----------|----------|-------|----------|
| Competência         | Servidor    | Chefe | Consenso | Servidor | Chefe | Consenso |
| Desc. competência 1 | 1           | 5     | 3        | 2        | 2     | 2        |
| Desc. competência 2 | NA          | 2     | 2        | 2        | 3     | 2        |
| Desc. competência 2 | 3           | 2     | 3        | 3        | 1     | 2        |

  **Regras:**
    Para cada competência:
      - Os campos `Importância` e `Domínio` do _servidor_ devem ser nao-editáveis, vindo preenchidos com os valores fornecidos na autoavaliação correspondente;
      - Os campos `Importância` e `Domínio` do _chefe_ devem estar abertos para preenchimento;
      - Os dois campos de `Consenso` devem estar abertos para preenchimento;
      - Se os valores `Importância` e `Domínio` forem iguais, o sistema deve preencher automaticamente o valor de `Consenso` com o valor repetido.

    **IMPORTANTE**: Se o servidor já tiver aprovado a avaliação de consenso (ou seja, estiver na situacao `Avaliação de consenso aprovada`), o sistema permitirá apenas visualização dos dados acima.

7. O usuário preenche, com a presença física do servidor, o valor de consenso para cada competência.

8. O sistema salva automaticamente cada alteração realizada. Não é necessária nenhuma ação para concluir o preenchimento (a validação será feita na conclusão do diagnóstico).
   
9. O usuário continua o trabalho de avaliação de consenso para outros servidores: aciona o botão `Voltar` para mostrar a lista de servidores e realiza as ações desejadas para os demais servidores.