# CDU-50 - Analisar diagnóstico

Ator: GESTOR, ADMIN

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo` com uma tabela hierárquica contendo as unidades participantes do
   processo.
    - Para o perfil GESTOR, a tabela deve se limitar à própria unidade do usuário e às unidades subordinadas a ela,
      recursivamente.
    - Para o perfil ADMIN, a tabela deve incluir todas as unidades participantes do processo.

3. O usuário aciona uma unidade na tabela.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade selecionada. Os elementos da tela serão:

- cabeçalho com dados gerais do subprocesso e da unidade, como detalhado em [CDU-07](cdu-07.md),
- botão `Histórico de análise`; sempre habilitado;
- controle 'drop-down' `Ações`, que dá acesso às seguintes ações (habilitadas apenas se a localização do subprocesso for
  a unidade do usuário)
    - `Devolver para ajustes` para ambos GESTOR e ADMIN;
    - `Registrar aceite`, apenas para GESTOR;
    - `Homologar`, apenas para ADMIN;
- lista dos servidores participantes da unidade, *exceto o responsável pela unidade*;
- ao lado de cada servidor dessa lista, sua situação individual;
- lista somente-leitura das competências do mapa vigente da unidade para o servidor selecionado, contendo:
    - uma linha para cada competência;
    - colunas `Importância`, `Domínio` e `Situação de Capacitação` referentes ao servidor selecionado;
    - cabeçalho com nome completo, título e situação do servidor selecionado;
- seção de movimentações do subprocesso;

---
Se o usuário clicar em `Histórico de análise`:

5. O sistema mostra os registros prévios de análise do subprocesso, contendo data/hora, unidade, resultado e observação.

---
Se o usuário optar por `Devolver para ajustes`:

6. O sistema abre um modal, com título "Devolução de diagnóstico" e texto "Confirma a devolução do diagnóstico da
   unidade [SIGLA_UNIDADE_SUBPROCESSO]?", campo `Justificativa` obrigatório e botões `Cancelar` e `Devolver`.

7. Caso o usuário confirme, o sistema:
   7.1. Registra uma análise de validação para o subprocesso com:
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ANALISE]
    - `Resultado`: 'Devolução para ajustes' - `Observação`

   7.2. Muda a localização do subprocesso para a unidade de origem da última movimentação do subprocesso. 7.3. Muda a
   situação de todos os servidores da unidade para 'Avaliação de consenso criada' (isso faz com o sistema habilite a
   edição das avaliações de consenso; ver [CDU-44](cdu-45.md))

8. O sistema envia uma notificação por e-mail para a unidade de origem da última movimentação do subprocesso, seguindo
   este modelo:

```text
Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes

Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi devolvido para ajustes.

Realize as mudanças solicitadas, acessando o Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
```

9. O sistema cria internamente um alerta com estes campos:
    - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ANALISE]
    - `Unidade de destino`: [SIGLA_UNIDADE_DEVOLUCAO]

10. O sistema cria uma movimentação para o subprocesso com estes campos:
    - `Descrição`: 'Devolução para ajustes'
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: [SIGLA_UNIDADE_ANALISE]
    - `Unidade destino`: [SIGLA_UNIDADE_DEVOLUCAO]

11. O sistema mostra a mensagem `Devolução realizada`.

---

Se o usuário optar por `Registrar aceite`:

12. O sistema abre um modal, com título "Aceitar diagnóstico" e texto "Confirma o aceite do diagnóstico da
    unidade [SIGLA_UNIDADE_SUBPROCESSO]?", um campo `Observação` opcional e os botões `Cancelar` e `Aceitar`.

13. O sistema muda a localização do subprocesso para a unidade imediatamente superior e registra uma análise de
    validação para o subprocesso com estes campos:
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ANALISE]
    - `Resultado`: 'Aceite'
    - `Observação`

14. O sistema cria internamente um alerta com estes campos:
    - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] aceito"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ANALISE]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

15. O sistema envia uma notificação por e-mail para a unidade imediatamente superior seguindo este modelo:

   ```text
   Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] aceito

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi submetido para sua análise.

   Realize a análise acessando o Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
   ```

16. O sistema cria uma movimentação para o subprocesso com estes campos:
    - `Descrição`: 'Aceite do diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO]'
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: [SIGLA_UNIDADE_ANALISE]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]

17. O sistema mostra a mensagem *toast* "Aceite registrado".

---

Se o usuário optar por `Homologar` (apenas perfil ADMIN):

18. O sistema abre um modal com título "Homologar diagnóstico" e texto "Confirma a homologação diagnóstico da
    unidade [SIGLA_UNIDADE_SUBPROCESSO]?", campo`Observação` opcional e botões `Cancelar` e `Homologar`.

19. O sistema registra uma análise de validação para o subprocesso com estes campos:
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ANALISE]
    - `Resultado`: 'Homologação'
    - `Observação`

20. O sistema cria uma movimentação para o subprocesso com estes campos:
    - `Descrição`: 'Homologação de diagnóstico'
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: [SIGLA_UNIDADE_ANALISE]
    - `Unidade destino`: [SIGLA_UNIDADE_ANALISE]

21. O sistema mostra a mensagem *toast* "Diagnóstico homologado".