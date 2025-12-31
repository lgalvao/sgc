# CDU-28 - Manter atribuição temporária

**Ator:** ADMIN

## Pré-condições

- Usuário autenticado com perfil ADMIN.

## Fluxo principal

1. ADMIN clica em `Unidade` no menu (este é o comando equivalente a `Minha unidade`, visto por outros perfis.

2. Sistema mostra a árvore completa de unidades.
	
3. ADMIN clica em umas das unidades.

4. Sistema mostra a pagina `Detalhes da unidade` 

3. ADMIN clica no botão `Criar atribuição`.

4. Sistema apresenta um modal com estes campos:

   - Lista de servidores da unidade, pesquisavel
   - Data de início 
   - Data de término 
   - Justificativa 
   - Botões `Confirmar` e `Cancelar`

5. ADMIN seleciona o servidor, define as datas e inclui uma justificativa. Todos os campos *são obrigatórios*.

6. Sistema registra internamente a atribuição temporária e mostra uma confirmação "Atribuição criada".

7. Sistema cria internamente alerta e dispara uma notificacao. // TODO detalhar.

8. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE. A atribuição temporária terá prioridade sobre os dados de titularidade lidos do SGRH (atraves das views).
