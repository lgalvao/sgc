# CDU-17 - Disponibilizar mapa de competências

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Ao menos uma unidade com subprocesso na situação 'Mapa criado' (Mapeamento) ou 'Mapa ajustado' (Revisão).

## Fluxo principal

1. No `Painel`, o usuário escolhe em um processo de mapeamento/revisão.

2. O sistema mostra tela `Detalhes do processo`.

3. O usuário aciona uma unidade com subprocesso na situação 'Mapa criado' ou 'Mapa ajustado'.

4. O sistema mostra a tela `Detalhes de subprocesso` para a unidade selecionada.

5. O usuário aciona o card `Mapa de competências`.

6. O sistema mostra a tela `Edição de mapa`, preenchida com os dados do mapa de competências da unidade.

7. O usuário aciona `Disponibilizar`.

8. O sistema verifica se todas as competências criadas estão associadas a pelo menos uma atividade do cadastro da
   unidade; e se todas as atividades foram associadas a pelo menos uma competência. Se alguma das validações falhar o
   sistema mostra mensagem indicando a falha e interrompe o processo.

9. O sistema mostra um modal com título "Disponibilização do mapa de competências", e os seguintes elementos:
    - Campo `Data limite`, obrigatório
    - Campo `Observações`, opcional
    - Botões `Disponibilizar` e `Cancelar`.

10. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de disponibilização do mapa, permanecendo na tela
    `Edição de mapa`.

11. O usuário preenche as informações e aciona `Disponibilizar`.

12. O sistema registra a informação fornecida no campo `Data limite` no campo `Data limite da etapa 2` do subprocesso.

13. O sistema altera a situação do subprocesso da unidade para 'Mapa disponibilizado'.

14. O sistema registra uma movimentação para o subprocesso:
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: ADMIN
    - `Unidade destino`: :SIGLA_UNIDADE_SUBPROCESSO:
    - `Descrição`: 'Mapa disponibilizado para validação'

15. O sistema notifica a unidade do subprocesso, com e-mail no modelo abaixo:

    ```text
    Assunto: SGC: Mapa de competências disponibilizado

    Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

    O mapa de competências de sua unidade foi disponibilizado no contexto do processo :DESCRICAO_PROCESSO:.

    A validação deste mapa já pode ser realizada no Sistema de Gestão de Competências (:URL_SISTEMA:). 
    O prazo para conclusão desta etapa do processo é :DATA_LIMITE:.
    ```

16. O sistema cria um alerta:
    - `Descrição`: "Mapa de competências da unidade disponibilizado para validação"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :UNIDADE_SUBPROCESSO:.

17. O sistema exclui as sugestões apresentadas do mapa de competência do subprocesso da unidade.

18. O sistema redireciona para o `Painel` e mostra o *toast* "Mapa de competências disponibilizado".
