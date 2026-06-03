# CDU-42 - Manter avaliação de consenso

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento
- Existência de servidor da unidade com situação `Autoavaliação concluída`, `Avaliação de consenso criada` ou `Avaliação de consenso aprovada`

## Fluxo principal

1. Na tela `Diagnóstico da equipe`, o usuário escolhe um servidor elegível e aciona `Criar avaliação de consenso` ou `Editar avaliação de consenso`.

2. O sistema apresenta a tela `Avaliação de consenso`, com o mesmo formulário da autoavaliação.

3. Para cada competência da unidade, o sistema preenche inicialmente `Importância` e `Domínio` com os valores atuais do servidor:
   - na primeira criação, os valores da autoavaliação;
   - em edições posteriores, os valores do consenso vigente.

4. Se a situação anterior do servidor for `Consenso aprovado`, o sistema, antes de liberar a edição, mostra um modal com:
   - título `Reabrir consenso`;
   - campo obrigatório `Motivo da reabertura`;
   - botões `Cancelar` e `Reabrir`.

5. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e volta para a tela `Diagnóstico da equipe`.

6. Caso o usuário confirme a reabertura, o sistema registra o motivo da reabertura, altera a situação da avaliação
   individual para `Consenso criado` e libera a edição.

7. O usuário ajusta os valores desejados de `Importância` e `Domínio`.

8. O sistema salva automaticamente cada alteração realizada.

9. O usuário clica em `Disponibilizar consenso`, na primeira criação, ou `Atualizar consenso`, quando já existir
   consenso.

10. O sistema verifica se todas as competências tiveram `Importância` e `Domínio` preenchidos.

11. Caso positivo, o sistema altera ou mantém a situação da avaliação individual como `Consenso criado`.

12. O sistema envia notificação por e-mail para o servidor:

    ```text
    Assunto: SGC: Avaliação de consenso de [NOME_SERVIDOR] disponível para validação

    Prezado(a) [NOME_SERVIDOR],

    A chefia da unidade [SIGLA_UNIDADE_SUBPROCESSO] registrou a avaliação de consenso do processo [DESCRICAO_PROCESSO].

    O consenso já pode ser consultado e validado no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

13. O sistema cria internamente um alerta com:
    - `Descrição`: "Avaliação de consenso de [NOME_SERVIDOR] disponível para validação"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

14. O sistema mostra a mensagem `Consenso disponibilizado`.

## Observação

PENDÊNCIA DE REFINAMENTO: esta primeira versão assume que o consenso reutiliza exatamente o mesmo formulário da
autoavaliação e acrescenta apenas `Motivo da reabertura` quando houver edição após aprovação. Confirmar se haverá
campos adicionais, como observações gerais ou justificativas por competência.

## Fluxo alternativo

1. No passo 10, caso exista competência sem `Importância` ou `Domínio` preenchidos, o sistema mostra a mensagem
   `Preencha importância e domínio para todas as competências.` e interrompe a disponibilização do consenso.
