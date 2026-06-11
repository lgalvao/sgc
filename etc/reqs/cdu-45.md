# CDU-44 - Manter avaliação de consenso

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Existência de processo de diagnóstico em andamento
- Existência de servidor da unidade com uma das situações:
    - 'Autoavaliação concluída'
    - 'Avaliação de consenso criada'
    - 'Avaliação de consenso aprovada'

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento e o sistema mostra a tela
   `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md)`.

2. Na grade de servidores da unidade, o usuário escolhe a ação `Avaliação de consenso` para um servidor.

3. O sistema mostra a tela `Avaliação de consenso` para o servidor selecionado, com:
    - título: "Avaliação de consenso";
    - subtítulo: [nome e título do servidor];
    - grade de competências da unidade, com estrutura como no exemplo abaixo:

|                     | Importância |       |          | Domínio  |       |          |
|:--------------------|-------------|-------|----------|----------|-------|----------|
| Competência         | Servidor    | Chefe | Consenso | Servidor | Chefe | Consenso |
| Desc. competência 1 | 1           | 5     | 3        | 2        | 2     | 2        |
| Desc. competência 2 | NA          | 2     | 2        | 2        | 3     | 2        |
| Desc. competência 3 | 3           | 2     | 3        | 3        | 1     | 2        |

Regras de apresentação, para cada competência:

- os campos `Importância` e `Domínio` do servidor devem ser **não editáveis**, sendo preenchidos com os valores
  fornecidos na autoavaliação correspondente;
- os campos `Importância` e `Domínio` do chefe devem ser **editáveis**;
- os dois campos de `Consenso` devem ser **editáveis**;
- se os valores `Importância` e `Domínio` forem iguais, o sistema deve preencher automaticamente o valor de `Consenso`
  com o valor repetido.

**IMPORTANTE**: Se a situação do servidor for 'Avaliação de consenso aprovada', ou o subprocesso não estiver localizado
na unidade do usuário, o sistema permitirá apenas **visualização** de todos os itens acima.

4. O usuário preenche (com a presença física do servidor) o valor de consenso para cada competência.

5. O sistema salva automaticamente cada alteração realizada. Não é necessária nenhuma ação para concluir a operação (a
   validação dos dados será feita na conclusão do diagnóstico, como um todo).

6. O usuário continua o trabalho de avaliação de consenso para outros servidores; para isso, aciona o botão `Voltar`
   para ver a lista de servidores e realiza as ações desejadas para cada um dos demais.