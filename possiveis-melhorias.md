# Possíveis melhorias

## M01. Alertas no painel para unidades INTEROPERACIONAL com perfis CHEFE e GESTOR

### Situação observada

Quando a unidade é do tipo `INTEROPERACIONAL`, um mesmo usuário pode atuar com os perfis `CHEFE` e `GESTOR` na mesma
unidade. Hoje, ao acessar o painel com qualquer um desses perfis, ele pode ver ao mesmo tempo (por exemplo):

- "Início do processo"
- "Início do processo em unidade(s) subordinada(s)"

### Causa técnica identificada

O comportamento nasce no backend:

- `sgc.alerta.AlertaAplicacaoService.java`
    - `criarAlertasProcessoIniciado(...)` inclui unidades `INTEROPERACIONAL` tanto no conjunto operacional quanto no
      intermediário.
- `sgc.alerta.model.AlertaRepo.java`
    - a busca de alertas de gestão filtra por `unidade ativa` e `usuário destino`, mas não distingue o `perfil` ativo
      (`CHEFE` ou `GESTOR`).

Com isso, a mesma unidade recebe dois alertas diferentes um agregado e um específico.

### Direção de solução discutida

Uma solução seria introduzir no domínio do alerta o perfil, por exemplo:

- alerta voltado a perfil `CHEFE`
- alerta voltado a perfil `GESTOR`

Isso exigiria:

- ajustar a modelagem de `Alerta`
- alterar a criação dos alertas no backend
- filtrar a leitura do painel pelo perfil ativo
- revisar contratos e requisitos relacionados a alertas e visibilidade

### Decisão atual

Ideia arquivada por enquanto.

Motivo:

- a mudança é estrutural
- o risco é alto para tratar como ajuste pontual
- exigiria revisão ampla de requisitos e comportamento esperado por perfil
