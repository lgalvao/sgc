# Situações

Os processos e subprocessos mantidos pelo sistema seguem um fluxo previsível, passando por um conjunto de situações que varia de acordo com o tipo de processo/subprocesso. Essas situações são referenciadas entre aspas simples (por exemplo, 'Não iniciado') nas especificações de casos de uso.

## Situações de Processos
- Criado: Processo cadastrado, mas não iniciado.
- Em andamento: Processo foi iniciado e todas as unidades participantes foram notificadas.
- Finalizado: Mapa de competências homologado para todas as unidades.

## Situações de Subprocessos de Mapeamento
- Não iniciado: Unidade notificada pela SEDOC, mas sem cadastro salvo.
- Cadastro em andamento: Cadastro salvo mas não finalizado.
- Cadastro disponibilizado: Cadastro finalizado, aguardando validação.
- Cadastro homologado: Cadastro validado pela SEDOC.
- Mapa criado: SEDOC criou mapa para a unidade mas ainda não disponibilizou.
- Mapa disponibilizado: SEDOC disponibilizou mapa da unidade para validação.
- Mapa com sugestões: CHEFE indicou sugestões para o mapa da unidade.
- Mapa validado: Hierarquia aprovou mapa disponibilizado pela SEDOC.
- Mapa homologado: SEDOC homologou mapa para a unidade após validações sem sugestões.

## Situações de Subprocessos de Revisão
- Não iniciado: Unidade foi notificada, mas não alterou cadastro.
- Revisão do cadastro em andamento: Unidade fez alteração no cadastro.
- Revisão do cadastro disponibilizada: Revisão do cadastro concluída, aguardando validação.
- Revisão do cadastro homologada: Revisão do cadastro validada pela SEDOC.
- Mapa ajustado: SEDOC criou mapa ajustado para a unidade mas ainda não disponibilizou.
- Mapa disponibilizado: SEDOC disponibilizou mapa ajustado da unidade para validação.
- Mapa com sugestões: CHEFE indicou sugestões para o mapa da unidade.
- Mapa homologado: SEDOC homologou mapa para a unidade após validações sem sugestões.
- Mapa validado: Hierarquia aprovou o mapa disponibilizado pela SEDOC.

```mermaid
---
title: "Processo de Mapeamento: Situações e transições"
---

stateDiagram-v2
    NaoIniciado: Não iniciado

    [*] --> NaoIniciado
    NaoIniciado --> CadastroEmAndamento: udp iniciou cadastro

    state Cadastro {
        CadastroEmAndamento: Em andamento
        CadastroDisponibilizado: Disponibilizado
        CadastroHomologado: Homologado

        state DecisaoValidacao <<choice>>
        state DecisaoDevolucao <<choice>>
        state DecisaoAcao <<choice>>

        CadastroEmAndamento --> CadastroDisponibilizado: udp disponibilizou

        CadastroDisponibilizado --> DecisaoAcao: validou ou devolveu?
        DecisaoAcao --> DecisaoDevolucao: devolveu
        DecisaoAcao --> DecisaoValidacao: validou
            
        DecisaoDevolucao --> CadastroEmAndamento: para udp?
        DecisaoDevolucao --> CadastroDisponibilizado: para int?

        DecisaoValidacao --> CadastroHomologado: SEDOC?
        DecisaoValidacao --> CadastroDisponibilizado: int?

        CadastroHomologado --> MapaCriado: SEDOC criou mapa
    }

    state Mapa {
        MapaCriado: Criado
        MapaDisponibilizado: Disponibilizado
        MapaValidado: Validado
        MapaComSugestoes: Com sugestões
        MapaHomologado: Homologado

        state DecisaoSugestoes <<choice>>

        state DecisaoDevolveSugestoes <<choice>>
        state DecisaoValidaSugestoes <<choice>>

        state DecisaoValidaMapa <<choice>>
        state DecisaoDevolveValidacaoMapa <<choice>>

        state DecisaoAcaoMapaSugestoes <<choice>>
        state DecisaoAcaoMapaValidado <<choice>>

        MapaCriado --> MapaDisponibilizado: SEDOC disponibilizou
            
        MapaDisponibilizado --> DecisaoSugestoes: udp tem sugestões?
        DecisaoSugestoes --> MapaComSugestoes: sim
        DecisaoSugestoes --> MapaValidado: nao
            
        DecisaoDevolveSugestoes --> MapaComSugestoes: para int?
        DecisaoDevolveSugestoes --> MapaDisponibilizado: para udp?
            
        MapaComSugestoes --> DecisaoAcaoMapaSugestoes: ação?
        DecisaoAcaoMapaSugestoes --> DecisaoValidaSugestoes: validou
        DecisaoAcaoMapaSugestoes --> DecisaoDevolveSugestoes: devolveu
            
        DecisaoValidaSugestoes --> MapaComSugestoes: int?
        DecisaoValidaSugestoes --> MapaCriado: SEDOC?

        DecisaoDevolveValidacaoMapa --> MapaValidado: para int?
        DecisaoDevolveValidacaoMapa --> MapaDisponibilizado: para udp?

        MapaValidado --> DecisaoAcaoMapaValidado: ação?
        DecisaoAcaoMapaValidado --> DecisaoValidaMapa: validou
        DecisaoAcaoMapaValidado --> DecisaoDevolveValidacaoMapa: devolveu

        DecisaoValidaMapa --> MapaHomologado: SEDOC?
        DecisaoValidaMapa --> MapaValidado: int?
    }
    MapaHomologado --> [*]
```

```mermaid
---
title: "Processo de Revisão: Situações e transições (por IA, nao validado)"
---
stateDiagram-v2
    NaoIniciado: "Não iniciado"

    [*] --> NaoIniciado
    NaoIniciado --> RevisaoEmAndamento: unidade inicia revisão

    state Cadastro {
        RevisaoEmAndamento: Revisão do cadastro em andamento
        RevisaoDisponibilizada: Revisão do cadastro disponibilizada
        RevisaoHomologada: Revisão do cadastro homologada

        state DecisaoAcaoCadastro <<choice>>
        state DecisaoDevolucaoCadastro <<choice>>
        state DecisaoValidacaoCadastro <<choice>>

        RevisaoEmAndamento --> RevisaoDisponibilizada: unidade disponibiliza

        RevisaoDisponibilizada --> DecisaoAcaoCadastro: SEDOC avalia
        DecisaoAcaoCadastro --> DecisaoDevolucaoCadastro: devolve
        DecisaoAcaoCadastro --> DecisaoValidacaoCadastro: valida

        DecisaoDevolucaoCadastro --> RevisaoEmAndamento: para unidade
        DecisaoDevolucaoCadastro --> RevisaoDisponibilizada: para SEDOC (ajuste interno)

        DecisaoValidacaoCadastro --> RevisaoHomologada: SEDOC homologa
        DecisaoValidacaoCadastro --> RevisaoDisponibilizada: para SEDOC (ajuste interno)

        RevisaoHomologada --> MapaAjustado: SEDOC ajusta mapa
    }

    state Mapa {
        MapaAjustado: Mapa ajustado
        MapaDisponibilizado: Mapa disponibilizado
        MapaComSugestoes: Mapa com sugestões
        MapaValidado: Mapa validado
        MapaHomologado: Mapa homologado

        state DecisaoSugestoesMapa <<choice>>
        state DecisaoAcaoSugestoes <<choice>>
        state DecisaoValidaSugestoes <<choice>>
        state DecisaoDevolveSugestoes <<choice>>
        state DecisaoAcaoValidado <<choice>>
        state DecisaoValidaMapa <<choice>>
        state DecisaoDevolveValidacao <<choice>>

        MapaAjustado --> MapaDisponibilizado: SEDOC disponibiliza
        MapaDisponibilizado --> DecisaoSugestoesMapa: hierarquia avalia
        DecisaoSugestoesMapa --> MapaComSugestoes: com sugestões
        DecisaoSugestoesMapa --> MapaValidado: sem sugestões (valida)
        DecisaoSugestoesMapa --> MapaAjustado: hierarquia devolve para SEDOC (ajuste)

        MapaComSugestoes --> DecisaoAcaoSugestoes: SEDOC avalia sugestões
        DecisaoAcaoSugestoes --> DecisaoValidaSugestoes: valida
        DecisaoAcaoSugestoes --> DecisaoDevolveSugestoes: devolve

        DecisaoValidaSugestoes --> MapaAjustado: SEDOC acata e ajusta
        DecisaoValidaSugestoes --> MapaComSugestoes: para SEDOC (ajuste interno)

        DecisaoDevolveSugestoes --> MapaDisponibilizado: para hierarquia
        DecisaoDevolveSugestoes --> MapaComSugestoes: para SEDOC (ajuste interno)

        MapaValidado --> DecisaoAcaoValidado: SEDOC avalia mapa validado
        DecisaoAcaoValidado --> DecisaoValidaMapa: homologa
        DecisaoAcaoValidado --> DecisaoDevolveValidacao: devolve

        DecisaoValidaMapa --> MapaHomologado: SEDOC homologa
        DecisaoValidaMapa --> MapaValidado: para SEDOC (ajuste interno)

        DecisaoDevolveValidacao --> MapaDisponibilizado: para hierarquia
        DecisaoDevolveValidacao --> MapaValidado: para SEDOC (ajuste interno)
    }

    MapaHomologado --> [*]
```