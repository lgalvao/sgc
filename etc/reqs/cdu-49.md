# CDU-49 - Acompanhar diagnóstico de unidades subordinadas

Ator: GESTOR ou ADMIN

## Pré-condições

- Login realizado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo` com uma tabela hierárquica contendo as unidades participantes do
   processo. Para cada unidade, são mostrados:
    - sigla da unidade;
    - nome da unidade;
    - situação atual do subprocesso da unidade.

   2.1. Para o perfil GESTOR, a tabela deve se limitar à própria unidade do usuário e às unidades subordinadas a ela,
   recursivamente.

   2.2. Para o perfil ADMIN, a árvore exibida deve incluir todas as unidades participantes do processo.

3. O usuário clica em uma unidade na tabela.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada, conforme o caso de
   uso [CDU-42.md](cdu-42.md)`.

5. O sistema apresenta uma matriz `Competência x Servidor`, somente-leitura, contendo:
    - uma linha para cada competência do mapa vigente da unidade;
    - um grupo de três colunas para cada servidor participante da unidade, com valores para:
        - `I` (Importância);
        - `D` (Domínio);
        - `C` (Situação de Capacitação).

   | Competência         | João |   |   | Maria |   |   |
   | :------------------ | :--: |:-:|:-:| :---: |:-:|:-:|
   |                     | **I** | **D** | **C** | **I** | **D** | **C** |
   | Desc. competência 1 | 1    | 2 | EC | 4     | 3 | EC |
   | Desc. competência 2 | NA   | NA | C | 5     | 4 | C |
   | Desc. competência 3 | 3    | 5 | I  | 2     | 5 | I |
