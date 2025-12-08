# CDU-12 - Verificar impactos no mapa de competências

Atores: CHEFE, GESTOR, ADMIN

Pré-condições:

- Processo de Revisão
- Uma das seguintes situações:
  - Perfil CHEFE e subprocesso na situação 'Revisão do cadastro em andamento'.
  - Perfil GESTOR e subprocesso na situação 'Revisão do cadastro disponibilizada' e com localização atual na unidade do usuário
  - Perfil ADMIN e subprocesso nas situações 'Revisão do cadastro disponibilizada', 'Revisão do cadastro homologada' ou 'Mapa Ajustado'

Fluxo principal:

1. No Painel, o usuário clica no processo de revisão na situação 'Em andamento'.

2. O sistema mostra a tela Detalhes do subprocesso da unidade.

4. O acesso à verificação de impactos será sempre pelo botão `Impactos no mapa`, que estará em três telas:

   3.1. Na tela `Cadastro de atividades e conhecimentos`, acessível a partir do card Atividades e conhecimentos, quando
   o perfil logado for CHEFE;

   3.2. Na tela `Atividades e conhecimentos` (somente leitura), acessível a partir do card Atividades e conhecimentos,
   quando:
    - o perfil logado for GESTOR ou ADMIN; e
    - a situação do subprocesso for 'Revisão do cadastro disponibilizada'; e
    - o subprocesso estiver localizado na unidade do usuário

   3.3. Na tela `Edição de mapa`, acessível a partir do card Mapa de competências, quando:
    - o perfil logado for ADMIN; e
    - a situação do subprocesso for `Revisão do cadastro homologada`, ou `Mapa ajustado`.

4. O usuário clica no botão `Impactos no mapa`.

5. O sistema realiza uma comparação entre as atividades e conhecimentos do mapa vigente da unidade e a mesma informação do mapa do subprocesso da unidade.

   5.1. Se for detectada **inclusão** de atividades, o sistema adiciona estas atividades em uma lista de atividades inseridas (veja a seguir).

   5.2. Se for detectada **remoção** ou **alteração** de qualquer atividade, seja na descrição ou nos conhecimentos associados, o sistema identifica as competências associadas a essas atividades no mapa vigente da unidade.

   5.2.1. O sistema marca a competência como impactada.

   5.2.2. O sistema preenche, para cada competência impactada, uma lista contendo a descrição da atividade, o tipo do impacto (remoção/alteração) e, no caso de alterações, a descrição da alteração (Ex.: 'Descrição alterada para X', 'Conhecimento Y removido', 'Conhecimento Z adicionado' etc).

6. Se nenhuma divergência for detectada, o sistema mostra a mensagem "Nenhum impacto no mapa da unidade."

7. Se alguma divergência tiver sido detectada, sistema exibe a tela modal `Impacto no Mapa de Competências`, com as seguintes seções:

   7.1. Atividades inseridas: Se novas atividades tiverem sido adicionadas, esta seção é exibida, enumerando, ao lado de um ícone de adição, os elementos da lista montados no passo anterior. Abaixo de cada atividade, deverão ser listados também os conhecimentos associados a elas.

   7.2. Competências impactadas: Se alguma competência impactada tiver sido detectada, o sistema mostra uma seção composta por blocos com a descrição das competências identificadas. No conteúdo de cada bloco, o sistema apresenta a lista de impactos observados no formato:

   ```text
   <ÍCONE REMOÇÃO/ALTERAÇÃO> Atividade removida/alterada:
       <DESCRIÇÃO_ATIVIDADE>
            <DESCRIÇÃO_ALTERAÇÃO>
   ```

8. O usuário analisa as informações apresentadas na tela modal `Impacto no Mapa de Competências` e, ao concluir a análise, clica em Fechar.

9. O sistema fecha o modal, retornando o usuário para a tela original, que permanece com seu estado inalterado.
