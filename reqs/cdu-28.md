# CDU-28 - Manter atribuição temporária

**Ator:** ADMIN

## Pré-condições

- Usuário autenticado com perfil ADMIN.

## Fluxo principal

1. ADMIN clica em `Unidade` no menu (este é o comando equivalente a `Minha unidade`, visto por outros perfis.

2. Sistema mostra a árvore completa de unidades.
	
3. ADMIN clica em umas das unidades.

4. Sistema mostra a pagina `Detalhes da unidade` 

5. ADMIN clica no botão `Criar atribuição`.

6. Sistema apresenta um modal com estes campos:

   - Lista de servidores da unidade, pesquisavel
   - Data de início 
   - Data de término 
   - Justificativa 
   - Botões `Confirmar` e `Cancelar`

7. ADMIN seleciona o servidor, define as datas e inclui uma justificativa. Todos os campos *são obrigatórios*.

8. Sistema registra internamente a atribuição temporária e mostra uma confirmação "Atribuição criada".

9. O sistema envia notificação por e-mail para o usuário que recebeu a atribuição temporária:

   Assunto: SGC: Atribuição de perfil CHEFE na unidade [SIGLA_UNIDADE]

   ```text
   Prezado(a) [NOME_SERVIDOR],

   Foi registrada uma atribuição temporária de perfil de CHEFE para você na unidade [SIGLA_UNIDADE].
   Período: [DATA_INICIO] a [DATA_TERMINO].
   Justificativa: [JUSTIFICATIVA].

   Acesse o sistema em: [URL_SISTEMA].
   ```

10. O sistema cria internamente um alerta para o usuário:

    - `Descrição`: "Atribuição temporária de perfil de CHEFE na unidade [SIGLA_UNIDADE]"
    - `Processo`: (Vazio)
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Usuário destino`: [USUARIO_SERVIDOR]

11. O usuário que recebe a atribuição temporária passa a ter os mesmos direitos do perfil CHEFE. A atribuição temporária terá prioridade sobre os dados de titularidade lidos do SGRH (atraves das views).
