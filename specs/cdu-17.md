# CDU-17 - Disponibilizar mapa de competências

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Ao menos uma unidade com subprocesso na situação 'Mapa criado' (Mapeamento) ou 'Mapa ajustado' (Revisão).

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de mapeamento/revisão.

2. O sistema mostra a tela `Detalhes do processo`.

3. O usuário aciona uma unidade com subprocesso na situação 'Mapa criado' ou 'Mapa ajustado'.

4. O sistema mostra a tela `Detalhes de subprocesso` para a unidade acionada.

5. O usuário aciona o card `Mapa de competências`.

6. O sistema mostra a tela `Edição de mapa`, preenchida com os dados do mapa de competências da unidade.

7. O usuário aciona `Disponibilizar`.

8. O sistema verifica se todas as competências criadas estão associadas a pelo menos uma atividade do cadastro da
   unidade; e se todas as atividades foram associadas a pelo menos uma competência. Se alguma das validações falhar o
   sistema mostra mensagem indicando a falha e interrompe o processo.

9. O sistema mostra um modal com título "Disponibilização do mapa de competências" e os seguintes elementos:
    - Campo `Data limite`, obrigatório
    - Campo `Observações`, opcional
    - Botões `Disponibilizar` e `Cancelar`.

10. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e permanece na tela `Edição de mapa`.

11. O usuário preenche as informações e aciona `Disponibilizar`.

12. O sistema registra a data fornecida em `Data limite`, no campo `Data limite da etapa 2` do subprocesso.

13. O sistema realiza as seguintes ações:

    13.1. Altera a situação do subprocesso para 'Mapa disponibilizado'.

    13.2. Registra uma movimentação para o subprocesso:
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: ADMIN
    - `Unidade destino`: :UNIDADE_SUBPROCESSO:
    - `Descrição`: 'Mapa disponibilizado para validação'

    13.3. Envia notificação por e-mail à unidade do subprocesso:
     ```text
        Assunto: SGC: Mapa de competências disponibilizado
    
        Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,
    
        O mapa de competências de sua unidade foi disponibilizado no contexto do processo :DESCRICAO_PROCESSO:.
    
        O prazo para conclusão desta etapa é :DATA_LIMITE:.

        A validação já pode ser realizada no Sistema de Gestão de Competências (:URL_SISTEMA:). 
     ```

    13.4. Cria um alerta:
    - `Descrição`: "Mapa de competências da unidade disponibilizado para validação"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: Data/hora atual 
    - `Unidade de origem`: ADMIN 
    - `Unidade de destino`: :UNIDADE_SUBPROCESSO:.

14. O sistema exclui as sugestões apresentadas do mapa de competência do subprocesso da unidade.

15. O sistema redireciona para o `Painel` e mostra o *toast* "Mapa de competências disponibilizado".