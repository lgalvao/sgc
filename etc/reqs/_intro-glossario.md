# Glossário

## Acesso TRE-PE

O sistema de Acesso do TRE-PE, que oferece uma API REST de autenticação de usuários, usando título de eleitor e senha. O acesso também inclui sistemas e perfis de usuários, mas esta funcionalidade não será usada no sistema, sendo os perfis
determinados a partir do SGRH juntamente com atribuições temporárias cadastradas.

## Atribuição temporária

Designação provisória da responsabilidade de uma unidade organizacional, realizada por usários com perfil ADMIN, com data de início e término definidas, sobrepondo temporariamente a informação de responsabilidade obtida do SGRH. Também referida como **atribuição temporária de responsabilidade**.

## Atividade

Ação desempenhada por uma unidade operacional ou interoperacional no exercício de suas funções. As atividades somente podem ser criadas e mantidas pelos usuários com perfil CHEFE.

## Árvore de unidades

Estrutura hierárquica das unidades organizacionais; no contexto dos processos de mapeamento, de revisão e de
diagnóstico.

## Cadastro

Termo simplificado para o cadastro de atividades e conhecimentos. Sempre que for usado o termo sem qualificações, refere-se a esse cadastro e a apenas este.

## Conhecimento

Conhecimento técnico necessário para desempenhar uma atividade específica. Uma atividade geralmente requer mais de um conhecimento.

## Competência

Elemento sintetizante das atribuições de uma unidade. É criado pelos perfis ADMIN a partir das atividades e conhecimentos cadastrados pelas unidades.

## Devolução

Ato de devolver para ajustes, após análise, as informações fornecidas (cadastro, mapa etc.) por uma unidade subordinada.

## Localização atual de subprocesso

Unidade destino da última movimentação registrada para o subprocesso. É fundamental para o fluxo dos processos de mapeamento, revisão e diagnóstico.

## Mapa de competências

Conjunto consolidado de competências criado pelos perfis ADMIN, para uma unidade. Também referido como mapa de competências técnicas.

## Movimentação

Registro da transição do subprocesso de uma unidade de origem para uma unidade de destino.

## Perfil ADMIN
O perfil ADMIN é o perfil de administrador do sistema. Os usuários com este perfil são responsáveis por criar, configurar e monitorar processos. Os usuários que logarem com este perfil no sistema estarão associados à unidade raiz ADMIN. Usuários com perfil ADMIN **não são 'todo-poderosos'** -- a grande maioria das ações que envolvem escrita só podem ser feitas pelo ADMIN quando o subprocesso estiver na sua unidade. Há também ações que são exclusivas de outros perfis, que nem o ADMIN podem executar (exemplo: o cadastro de atividades e conhecimentos só pode ser feito por usuários com perfil CHEFE).

## Processos ativos/inativos

Os processos são considerados ativos no sistema quando ainda não estão finalizados ou foram finalizados há no máximo o número de dias definidos na configuração DIAS_INATIVACAO_PROCESSO (padrão 10 dias). A partir dessa quantidade de dias da finalização do processo, ele será considerado inativo, sendo disponível apenas para consulta a partir da tela Histórico de processos.

## Processo de mapeamento

Ciclo completo de coleta, validação e consolidação de atividades e conhecimentos das unidades operacionais e interoperacionais, e posterior geração e validação do mapa de competências. Também referido como processo de mapeamento de competências técnicas.

## Processo de revisão

Ciclo de revisão e validação do cadastro de atividades e conhecimentos das unidades operacionais e interoperacionais, e posterior adequação e validação do mapa de competências. Também referido como processo de revisão do mapa de competências técnicas.

## Processo de diagnóstico

Avaliação realizada pelos servidores e pelos responsáveis pelas unidades para identificar a importância e o domínio das competências das unidades por parte dos seus servidores, assim como as competências com poucos servidores capacitados.
Também referido como de competências técnicas e identificação das ocupações críticas.

## Responsável

Servidor titular ou substituto da titularidade de uma unidade organizacional, de acordo com as informações vigentes no SGRH no momento da consulta.

## SEDOC

Seção de desenvolvimento organizacional e capacitação. Principal unidade usuária do sistema no contexto dos processos de mapeamento, de revisão e de diagnóstico. Os seus servidores geralmente terão o perfil ADMIN, embora possam logar na sua unidade SEDOC e outro perfil aplicavel (geralmente CHEFE ou SERVIDOR)

## SGRH

O sistema de Gestão de Recursos humanos (SGRH) é o sistema cujo banco de dados fornece as informações das unidades de lotação e titularidade dos servidores do Tribunal. As views do sistema são majoritamemente baseadas nos dados fornecidos pelo SGRH.

## Subprocesso

Instância de um processo de mapeamento, revisão ou diagnóstico no contexto de uma unidade operacional ou interoperacional.

## Unidade

Elemento da estrutura hierárquica do tribunal (árvore de unidades) onde os servidores estão lotados. Para efeito do sistema, podem ser classificadas em intermediárias, operacionais ou interoperacionais. Também referido como **unidade organizacional**.

## Unidade intermediária

Unidade abaixo da unidade raiz (ADMIN), que possua uma ou mais unidades subordinadas a ela mas que não possua servidores lotados nela além do titular.

## Unidade interoperacional

Unidade que possui unidades subordinadas, mas também possui mais de um servidor lotado.

## Unidade operacional

Unidade com mais de um servidor lotado e que não possui unidades subordinadas a ela.

## Unidade raiz (ADMIN)

A única unidade, no contexto do atual sistema, que não possui unidade superior na árvore de unidades. No contexto dos processos de mapeamento, de revisão e de diagnóstico, o papel de unidade raiz é exercido por uma unidade 'virtual', acima de todas as unidades, com sigla 'ADMIN'. Usuários que fazem login com o perfil ADMIN estarão associados a esta unidade automaticamente.

## Validação

Ato de ratificar, após análise, as informações fornecidas por uma unidade subordinada. A validação encaminha a análise para a unidade superior.