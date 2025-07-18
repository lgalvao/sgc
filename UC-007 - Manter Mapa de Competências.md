## **UC-007 \- Manter Mapa de Competências**

**Ator Principal**: SEDOC 

**Pré-condições**: 

* Ao menos uma unidade com situação "Finalizada".   
* Cadastro de atividades/conhecimentos validado e por todas as unidades na hierarquia da unidade 'folha' até a SEDOC.

**Fluxo principal:**

1. SEDOC acessa o sistema.  
2. Sistema mostra o **Painel**.  
3. SEDOC escolhe uma unidade com situação 'Finalizada'.  
4. Sistema mostra a página **Detalhes de Unidade**, contendo:  
   1. Sigla e nome da unidade  
   2. Nome do responsável, com informações de contato.  
   3. Data/hora de finalização do cadastro de atividades/conhecimentos.  
   4. Todas as atividades e conhecimentos cadastrados pela unidade, com layout que deixe claras as associações entre os conhecimentos e atividades (ver **[UC-006 \- Cadastrar Atividades e Conhecimentos](https://docs.google.com/document/d/1bnyhBfW5AMvCht5aaeqJ3IIWU-itApdVb98TMTf5KWE/edit?tab=t.0#heading=h.or448dax6r88)**).  
   5. Botões para manutenção do mapa, seguindo estas regras:   
      1. Se já houver um mapa em andamento, 'Editar mapa';   
      2. Se não houver mapa, 'Criar mapa';   
      3. Se mapa já estiver disponibilizado para validação, 'Visualizar mapa'.  
5. SEDOC clica em 'Criar mapa'  
6. Sistema mostra a página **Edição de Mapa de Competências** e cria internamente um novo Mapa de Competências para a unidade.  
7. Sistema muda o layout da tela deixando as atividades selecionáveis e mostra botões 'Adicionar competência' e 'Gerar Mapa'

*\[Início de fluxo de edição de competências\]* 

8. SEDOC seleciona uma ou mais atividades e clica em 'Adicionar competência'.  
9. Sistema cria uma competência, com estes passos:  
   1. Sistema associa a nova competência com as atividades selecionadas  
   2. Sistema mostra um campo, com espaço amplo e suporte a texto formatado, para o detalhamento da competência  
   3. Sistema adiciona a descrição das atividades selecionadas abaixo do campo de descrição da competência.   
      1. Ao lado da descrição da atividade deve existir um controle permitindo a expansão para mostrar os conhecimentos associados à atividade; mas a atividade deve sempre iniciar 'recolhida'.  
   4. Sistema mostra um botão 'Salvar' ao lado do bloco de definição da competência.  
10. SEDOC, ao terminar a descrição da competência, clica em 'Salvar'.   
11. Sistema indica visualmente uma atividade como já associada a uma competência (mas não bloqueia seu uso em outras competências).

*\[Término de fluxo de edição de competências\]* 

12. SEDOC repete o [fluxo de criação de competências] até que o mapa esteja completo.  
13. SEDOC clica em 'Gerar Mapa'.  
14. Sistema verifica se todas as atividades foram associadas a pelo menos uma competência.   
    1. Caso negativo, informa quais atividades estão ainda sem associação a competências.  
    2. Caso positivo, pergunta se no mapa gerado devem ser mostradas as atividades associadas.  
15. Sistema mostra a página **Finalização de Mapa de Competências**, incluindo ou não a descrição das atividades, dependendo da escolha anterior. São mostrados os botões:  
    1. Editar Mapa (volta para página **Edição de Mapa de Competências**)  
    2. Botão Disponibilizar  
    3. Botão PDF  
    4. Botão Planilha  
16. SEDOC escolhe em 'Disponibilizar' e fornece uma data a data limite para conclusão da validação do mapa de competências pela unidade.  
17. Sistema envia notificação por email para a unidade e suas unidades superiores, informando o início da etapa de validação do mapa e o prazo para a sua conclusão. A notificação deve seguir este modelo:

    *Assunto: SGC: Mapa de Competências Disponibilizado: \[SESEL\]*

    *À equipe da \[SESEL\],*

    *Comunicamos que o mapa de competências de sua unidade já está disponível para validação, com data limite \[12/09/2026\].*

    *A visualização e validação do mapa pode ser realizada no Sistema SGC.* 

    *Atenciosamente,*  
    *Equipe da SEDOC*

18. A SEDOC também poderá disponibilizar os mapas de competências para várias unidades em lote, informando para todas a mesma data limite para conclusão da validação.   
    1. Se o processo de disponibilização tiver sido realizado em lote, as unidades intermediárias na árvore hierárquica receberão uma única notificação consolidada com os dados de todas as unidades subordinadas. Modelo:

    *Assunto: SGC: Mapas de Competências Disponibilizados*

    *À equipe da STIC,*

    *Comunicamos que os mapas de competências da seguintes unidades já estão disponíveis para validação:*

    ***\- \[GAB-STIC\]***  
       ***\- \[SESEL\]***  
       ***\- \[SENIC\]***  
       ***\- \[SESUP\]***

    *A validação dos mapas pode ser realizada no Sistema SGC.* 

    *Informamos que a data limite para finalizar essa validação é  \[22/10/2026\].*

    *Atenciosamente,*  
       *Equipe da SEDOC*

    2. Uma vez disponibilizado, o mapa de competências passará a ficar disponível para consulta pelo CHEFE e GESTOR.   
       1. O CHEFE tem acesso a apenas aos mapas de sua unidade (o atual e os históricos).  
       2. O GESTOR tem acesso a todos os mapas de suas unidades subordinadas (incluindo os históricos).  
19. Sistema redireciona para o **Painel**.

