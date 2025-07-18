## **UC-009 \- Manter Atribuição Temporária**

**Ator Principal**: SEDOC

**Pré-condições**: Processo iniciado e Usuário autenticado com perfil SEDOC.

**Fluxo principal:**

1. SEDOC acessa funcionalidade de atribuições temporárias.
2. Sistema apresenta:
   1. Lista de unidades organizacionais, permitindo pesquisa e navegação pela hierarquia.
   2. Lista de servidores, preenchida dinamicamente, de acordo com a unidade selecionada. Inicialmente vazia.
   3. Campo 'Data de término'
   4. Campo 'Justificativa'.
   5. Botão 'Criar atribuição'
3. SEDOC escolhe uma unidade.
4. Sistema mostra lista de servidores na unidade selecionada, destacando o responsável atual (titular ou substituto).
   1. Apenas poderá ser selecionado nessa lista um servidor que não seja titular da unidade selecionada (pois neste caso não faz sentido uma atribuição temporária).
   2. REGRA: Um mesmo servidor pode receber mais de uma atribuição temporária, desde que nãos seja para a mesma unidade.
5. SEDOC seleciona servidor que receberá a atribuição temporária e define a data de término da atribuição, além da justificativa.
   1. REGRA: A atribuição temporária terá prioridade sobre os dados importados do SGRH.
6. Sistema registra internamente a atribuição temporária e mostra uma confirmação.
7. Sistema envia notificação para o servidor que recebeu a atribuição.
   1. REGRA: O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE (habilidade de cadastrar atividades e conhecimentos).
8. Sistema agenda remoção automática da atribuição na data definida.
