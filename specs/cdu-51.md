# CDU-51 - Validar diagnósticos em bloco

**Ator:** GESTOR

## Pré-condições

- Usuário logado com perfil GESTOR.
- Existência de ao menos um processo de diagnóstico em andamento.
- Existência de pelo menos um subprocesso de unidade subordinada na situação `Concluído` e localizado na unidade do
  usuário.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo` com:

- uma tabela hierárquica contendo as unidades participantes do processo, limitando-se à unidade do usuário e sua
  subordinadas, recursivamente.
- um botão *drop_down* `Ações`, com as ações:
  - `Validar em bloco`, habilitado apenas se houver unidades  
  - `Devolver para ajustes`

4. Caso existam unidades subordinadas com subprocessos elegíveis para aceite em bloco do diagnóstico, o sistema exibe,
   na seção `Unidades participantes`, abaixo da árvore de unidades, o botão `Validar diagnósticos em bloco`.
   
4. O usuário clica em `Validar diagnósticos em bloco`.

5. O sistema abre modal de confirmação, com os elementos a seguir:
    - título `Aceite de diagnósticos em bloco`;
    - texto `Selecione as unidades cujos diagnósticos deverão ser aceitos`;
    - lista das unidades elegíveis, sendo apresentadas, para cada unidade, um checkbox selecionado por padrão, a sigla,
      o nome e a situação atual do subprocesso;
    - botões `Cancelar` e `Registrar aceite`.

6. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação, permanecendo na tela `Detalhes do processo`.

7. O usuário clica em `Registrar aceite`.

8. O sistema atua, para cada unidade selecionada, da seguinte forma:

   8.1. Registra internamente uma análise de validação para o subprocesso:

    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ATUAL]
    - `Resultado`: `Aceite`
    - `Observação`: `De acordo com o diagnóstico apresentado pela unidade`

   8.2. Registra internamente uma movimentação para o subprocesso:

    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR_IMEDIATA]
    - `Descrição`: `Aceite`

   8.3. Registra internamente um alerta:

    - `Descrição`: `Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] aceito`
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR_IMEDIATA]

   8.4. Altera a localização atual do subprocesso para a unidade imediatamente superior à unidade do subprocesso.

9. O sistema agrupa as unidades selecionadas por unidade superior imediata e envia, para cada unidade superior imediata
   que tenha ao menos uma subordinada direta selecionada, uma única notificação consolidada por e-mail, com o modelo a
   seguir:

   ```text
   Assunto: SGC: Diagnósticos submetidos para análise

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   Os diagnósticos das unidades [LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS] no processo [DESCRICAO_PROCESSO] foram submetidos para análise por essa unidade.

   As análises já podem ser realizadas no Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

10. O agrupamento do passo anterior considera apenas a unidade superior imediata de cada subprocesso selecionado. O
    sistema não propaga automaticamente a consolidação para níveis hierárquicos acima.

11. O sistema mostra a mensagem `Diagnósticos aceitos em bloco` e redireciona para o `Painel`.
