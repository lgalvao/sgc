# CDU-11 - Visualizar cadastro de atividades e conhecimentos

## Atores

- ADMIN
- GESTOR
- CHEFE
- SERVIDOR

## Pré-condições

- Usuário logado com qualquer perfil
- Processo de mapeamento/revisão em andamento, que tenha a unidade como participante
- Subprocesso da unidade com cadastro de atividades e conhecimentos (processo de mapeamento) ou revisão do cadastro
  (processo de revisão) já disponibilizados.

## Fluxo principal

1. No painel, o usuário clica no processo de mapeamento ou revisão na situação `Em andamento` ou `Finalizado`.

---
Se o usuário estiver logado com perfil ADMIN ou GESTOR:

2. O sistema mostra a tela `Detalhes do processo`

3. Usuário clica em uma unidade subordinada, que seja operacional ou interoperacional

4. O sistema mostra a tela `Detalhes do subprocesso`, com os dados do subprocesso da unidade selecionada

---
Se o usuário estiver logado com perfil CHEFE ou SERVIDOR:

5. O sistema mostra diretamente a tela `Detalhes do subprocesso` com os dados do subprocesso da unidade do usuário

---

6. Na tela de `Detalhes do subprocesso`, o usuário aciona o card `Atividades e conhecimentos`.

7. O sistema mostra a tela `Atividades e conhecimentos`, preenchida com os dados da unidade. São apresentados a sigla e
   o nome da unidade, e cada atividade é apresentada como uma tabela, com cabeçalho a descrição da atividade, e as
   linhas preenchidas com os conhecimentos cadastrados para aquela atividade.
