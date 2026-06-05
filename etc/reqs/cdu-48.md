# CDU-48 - Concluir diagnóstico da unidade

Ator: CHEFE

## Pré-condições

- Usuário logado com perfil CHEFE
- Existência de processo de diagnóstico em andamento envolvendo a unidade do usuário
- Subprocesso da unidade com localização atual na própria unidade
- Todos os servidores da unidade com avaliação individual em `Avaliação de consenso aprovada` ou `Avaliação impossibilitada`
- Informações de situação de capacitação preenchidas para todos os servidores e competências da unidade

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade, com o monitoramento do diagnóstico exibido inline, incluindo a situação de cada servidor da unidade e o botão `Concluir diagnóstico`, quando aplicável.

3. O usuário clica em `Concluir diagnóstico`.
   
4. O sistema verifica se todas as avaliações individuais estão em `Consenso aprovado` ou `Avaliação impossibilitada`, e se as situações de capacitação foram integralmente preenchidas.

   4.1. Caso positivo, o sistema mostra modal de confirmação com:
      - título `Conclusão de diagnóstico`;
      - texto `Confirma a conclusão do diagnóstico da unidade?`;
      - botões `Cancelar` e `Concluir`.
   
   4.2. Caso exista pendência, o sistema mostra a mensagem "Ainda existem avaliações ou ocupações críticas
   pendentes." e interrompe a operação.
   
   4.3. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e permanece na mesma tela.

5. O usuário confirma.

6. O sistema altera a situação do subprocesso para 'Concluído'.

7. O sistema registra uma movimentação para o subprocesso com:
   - `Data/hora`: [Data/hora atual];
   - `Unidade origem`: [SIGLA_UNIDADE_SUBPROCESSO];
   - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR];
   - `Descrição`: 'Diagnóstico concluído'.

8. O sistema envia notificação por e-mail para a unidade superior:

    ```text
    Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi concluído e submetido à análise das unidades superiores.
    ```

9. O sistema cria internamente um alerta com:
    - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

10. O sistema redireciona para o `Painel` e mostra a mensagem `Diagnóstico concluído`. 
