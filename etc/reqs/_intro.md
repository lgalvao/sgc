# Informações gerais

O sistema de Gestão de Competências (SGC) tem como objetivo gerenciar as competências técnicas das unidades organizacionais do Tribunal. O sistema opera com uma estrutura organizacional hierárquica em que a árvore de unidades fica subordinada a uma unidade virtual raiz (sigla ADMIN) a partir da qual os processos de mapeamento, revisão e diagnóstico de competências são iniciados e concluídos, após passarem pela atuação das demais unidades da hierarquia.

Para fins de classificação estrutural (detalhada no Glossário do sistema), as unidades são divididas em:
- **Unidade operacional**: Unidade de ponta, com mais de um servidor e sem unidades subordinadas.
- **Unidade intermediária**: Unidade de chefia/gestão, com unidades subordinadas mas apenas um servidor (o titular) lotado.
- **Unidade interoperacional**: Unidade híbrida (atua tanto como chefia quanto ponta), que possui unidades subordinadas e também mais de um servidor lotado.

Os componentes essenciais do sistema incluem:
- **atividades**: ações específicas desempenhadas por cada unidade no exercício de suas funções;
- **conhecimentos**: conhecimentos técnicos necessários para executar cada atividade (cada atividade terá um ou mais conhecimentos)
- **competências**: elementos sintetizantes agrupando atividades relacionadas, criadas e mantidas apenas pelo perfil ADMIN;
- **mapas de competências**: conjunto de competências associadas a uma unidade, resultante do processo de mapeamento, criadas e mantidas apenas pelo perfil ADMIN

O sistema suporta três tipos principais de processos: 
- **mapeamento**: representa o processo inicial de coleta sistemática das atividades e conhecimentos necessários para cada unidade operacional visando a construção do primeiro mapa de competências da unidade; 
- **revisão**: consiste na atualização periódica dos mapas de competências vigentes em cada unidade considerando as mudanças organizacionais e de atribuições; 
- **diagnóstico**: avalia a importância e domínio das competências pelos servidores das unidades, identificando lacunas (gaps) e necessidades de capacitação; **o módulo de diagnóstico ainda está em especificação**.

## Atores e perfis

O sistema opera com os seguintes perfis de usuários, cujas atribuições e acessos são automaticamente reconhecidos com base na condição de responsabilidade ou lotação em uma unidade, de acordo com o SGRH, ou por atribuição de responsabilidade temporária, ou cadastro como administrador no SGC. Caso um usuário acumule mais de um perfil ou seja responsável por mais de uma unidade, será necessário selecionar o perfil e a unidade de trabalho após o primeiro passo do login.

- **ADMIN**: Administrador do sistema. Esse papel, que será geralmente exercido por servidores da SEDOC, é responsável por criar, configurar e monitorar processos (dos três tipos). Usuários que logarem com este perfil estarão associados à unidade raiz ADMIN. Este perfil é responsável exclusivo por criar e ajustar os mapas de competências.
- **GESTOR**: Responsável por uma unidade intermediária ou interoperacional (geralmente será um Coordenador ou Secretário). Pode visualizar e validar as informações cadastradas pelas unidades sob sua gestão, submetendo para análise da unidade superior, ou devolvendo à unidade subordinada para realização de retificações. Usários com perfil GESTOR não podem criar nada no sistema -- apenas validam/devolvem/analisam subprocessos.
- **CHEFE**: Responsável por uma unidade operacional ou interoperacional. Responsável exclusivo por cadastrar atividades e conhecimentos de sua unidade.
- **SERVIDOR**: Servidor lotado em uma unidade operacional ou interoperacional. Este papel só atua nos processos de diagnóstico.

## Situações

Os processos e subprocessos mantidos pelo sistema seguem um fluxo que passa por situações que variam de acordo com o tipo de processo/subprocesso. Essas situações são referenciadas entre aspas simples (por exemplo, 'Não iniciado') nas especificações de casos de uso.

Nos fluxos e situações diagramados a seguir, adotamos as seguintes siglas para os atores de transição:
- **`udp`** (unidade do processo): A unidade (operacional ou interoperacional) para a qual o mapa será criado, e que é responsável por realizar o cadastro de atividades e conhecimentos.
- **`int`** (unidade intermediária): A unidade de gestão imediatamente superior na árvore hierárquica, que avalia as informações submetidas pelas unidades a ela subordinadas.
- **ADMIN**: Unidade raiz administradora (geralmente um servidor da SEDOC).

### Situações de Processos (todos os tipos)

- **Criado**: Processo cadastrado, mas não iniciado.
- **Em andamento**: Processo foi iniciado e todas as unidades participantes foram notificadas.
- **Finalizado**: Mapa de competências homologado para todas as unidades (no caso de mapeamento/revisão).

### Situações de subprocessos de Mapeamento

- **Não iniciado**: Unidade notificada do início do processo, mas sem nenhum cadastro de atividades salvo.
- **Cadastro em andamento**: Cadastro salvo pela unidade mas não marcado como finalizado.
- **Cadastro disponibilizado**: Cadastro finalizado, aguardando validação.
- **Cadastro homologado**: Cadastro validado na unidade ADMIN.
- **Mapa criado**: Perfil ADMIN criou o mapa para a unidade mas ainda não disponibilizou.
- **Mapa disponibilizado**: Perfil ADMIN disponibilizou o mapa para validação.
- **Mapa com sugestões**: Perfil CHEFE indicou sugestões para o mapa.
- **Mapa validado**: Toda a hierarquia aprovou o mapa disponibilizado.
- **Mapa homologado**: Perfil ADMIN homologou o mapa após a sua validação pela hierarquia.

### Situações de subprocessos de Revisão

- **Não iniciado**: Unidade foi notificada do início do processo, mas ainda não iniciou a revisão do seu cadastro de atividades.
- **Revisão do cadastro em andamento**: Foi iniciada a revisão do cadastro de atividades da unidade.
- **Revisão do cadastro disponibilizada**: Foi concluída a revisão do cadastro de atividades da unidade, aguardando validação.
- **Revisão do cadastro homologada**: Foi concluída a validação da revisão do cadastro de atividades da unidade.
- **Mapa ajustado**: Perfil ADMIN criou o mapa ajustado para a unidade mas ainda não o disponibilizou.
- **Mapa disponibilizado**: Perfil ADMIN disponibilizou o mapa ajustado para validação.
- **Mapa com sugestões**: Perfil CHEFE indicou sugestões para o mapa.
- **Mapa validado**: Toda a hierarquia aprovou o mapa disponibilizado.
- **Mapa homologado**: Perfil ADMIN homologou o mapa após a sua validação por toda a hierarquia.

### Situações de subprocessos de Diagnóstico

- **Não iniciado**: Unidade notificada do início do processo de diagnóstico, mas nenhum questionário ou avaliação foi iniciado.
- **Autoavaliação em andamento**: Avaliações das competências em preenchimento pelos servidores/responsáveis da unidade.
- **Monitoramento**: Período de acompanhamento dos gaps identificados.
- **Concluído**: Avaliações e consolidações do diagnóstico estão totalmente finalizadas.

```mermaid
---
title: "Processo de Mapeamento"
---

stateDiagram-v2
    NaoIniciado: Não iniciado

    [*] --> NaoIniciado
    NaoIniciado --> CadastroEmAndamento: udp iniciou cadastro

    note right of Cadastro
        udp = unidade do subprocesso
        int = unidade intermediária
    end note

    state Cadastro {
        CadastroEmAndamento: Cadastro em andamento
        CadastroDisponibilizado: Cadastro disponibilizado
        CadastroHomologado: Cadastro homologado

        state DecisaoValidacao <<choice>>
        state DecisaoDevolucao <<choice>>
        state DecisaoAcao <<choice>>

        CadastroEmAndamento --> CadastroDisponibilizado: udp disponibilizou

        CadastroDisponibilizado --> DecisaoAcao: validou ou devolveu?
        DecisaoAcao --> DecisaoDevolucao: devolveu
        DecisaoAcao --> DecisaoValidacao: validou

        DecisaoDevolucao --> CadastroEmAndamento: para udp?
        DecisaoDevolucao --> CadastroDisponibilizado: para int?

        DecisaoValidacao --> CadastroHomologado: ADMIN?
        DecisaoValidacao --> CadastroDisponibilizado: int?

        CadastroHomologado --> MapaCriado: ADMIN criou mapa
    }

    state Mapa {
        MapaCriado: Mapa criado
        MapaDisponibilizado: Mapa disponibilizado
        MapaValidado: Mapa validado
        MapaComSugestoes: Mapa com sugestões
        MapaHomologado: Mapa homologado

        state DecisaoSugestoes <<choice>>

        state DecisaoDevolveSugestoes <<choice>>
        state DecisaoValidaSugestoes <<choice>>

        state DecisaoValidaMapa <<choice>>
        state DecisaoDevolveValidacaoMapa <<choice>>

        state DecisaoAcaoMapaSugestoes <<choice>>
        state DecisaoAcaoMapaValidado <<choice>>

        MapaCriado --> MapaDisponibilizado: ADMIN disponibilizou

        MapaDisponibilizado --> DecisaoSugestoes: udp tem sugestões?
        DecisaoSugestoes --> MapaComSugestoes: sim
        DecisaoSugestoes --> MapaValidado: nao

        DecisaoDevolveSugestoes --> MapaComSugestoes: para int?
        DecisaoDevolveSugestoes --> MapaDisponibilizado: para udp?

        MapaComSugestoes --> DecisaoAcaoMapaSugestoes: ação?
        DecisaoAcaoMapaSugestoes --> DecisaoValidaSugestoes: validou
        DecisaoAcaoMapaSugestoes --> DecisaoDevolveSugestoes: devolveu

        DecisaoValidaSugestoes --> MapaComSugestoes: int?
        DecisaoValidaSugestoes --> MapaCriado: ADMIN?

        DecisaoDevolveValidacaoMapa --> MapaValidado: para int?
        DecisaoDevolveValidacaoMapa --> MapaDisponibilizado: para udp?

        MapaValidado --> DecisaoAcaoMapaValidado: ação?
        DecisaoAcaoMapaValidado --> DecisaoValidaMapa: validou
        DecisaoAcaoMapaValidado --> DecisaoDevolveValidacaoMapa: devolveu

        DecisaoValidaMapa --> MapaHomologado: ADMIN?
        DecisaoValidaMapa --> MapaValidado: int?
    }
    MapaHomologado --> [*]
```

```mermaid
---
title: "Processo de Revisão"
---

stateDiagram-v2
    NaoIniciado: Não iniciado

    [*] --> NaoIniciado
    NaoIniciado --> RevisaoEmAndamento: udp inicia revisão

    note right of Cadastro
        udp = unidade do processo
        int = unidade intermediária
    end note

    state Cadastro {
        RevisaoEmAndamento: Revisão do cadastro em andamento
        RevisaoDisponibilizada: Revisão do cadastro disponibilizada
        RevisaoHomologada: Revisão do cadastro homologada

        state DecisaoAcaoCadastro <<choice>>
        state DecisaoDevolucaoCadastro <<choice>>
        state DecisaoValidacaoCadastro <<choice>>

        RevisaoEmAndamento --> RevisaoDisponibilizada: udp disponibilizou

        RevisaoDisponibilizada --> DecisaoAcaoCadastro: validou ou devolveu?
        DecisaoAcaoCadastro --> DecisaoDevolucaoCadastro: devolveu
        DecisaoAcaoCadastro --> DecisaoValidacaoCadastro: validou

        DecisaoDevolucaoCadastro --> RevisaoEmAndamento: para udp?
        DecisaoDevolucaoCadastro --> RevisaoDisponibilizada: para int?

        DecisaoValidacaoCadastro --> RevisaoHomologada: ADMIN?
        DecisaoValidacaoCadastro --> RevisaoDisponibilizada: int?

        RevisaoHomologada --> MapaAjustado: ADMIN ajustou mapa
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

        MapaAjustado --> MapaDisponibilizado: ADMIN disponibilizou

        MapaDisponibilizado --> DecisaoSugestoesMapa: udp tem sugestões?
        DecisaoSugestoesMapa --> MapaComSugestoes: sim
        DecisaoSugestoesMapa --> MapaValidado: nao

        DecisaoDevolveSugestoes --> MapaComSugestoes: para int?
        DecisaoDevolveSugestoes --> MapaDisponibilizado: para udp?

        MapaComSugestoes --> DecisaoAcaoSugestoes: ação?
        DecisaoAcaoSugestoes --> DecisaoValidaSugestoes: validou
        DecisaoAcaoSugestoes --> DecisaoDevolveSugestoes: devolveu

        DecisaoValidaSugestoes --> MapaComSugestoes: int?
        DecisaoValidaSugestoes --> MapaAjustado: ADMIN?

        DecisaoDevolveValidacao --> MapaValidado: para int?
        DecisaoDevolveValidacao --> MapaDisponibilizado: para udp?

        MapaValidado --> DecisaoAcaoValidado: ação?
        DecisaoAcaoValidado --> DecisaoValidaMapa: validou
        DecisaoAcaoValidado --> DecisaoDevolveValidacao: devolveu

        DecisaoValidaMapa --> MapaHomologado: ADMIN?
        DecisaoValidaMapa --> MapaValidado: int?
    }

    MapaHomologado --> [*]
```
