# Definição do Produto: SGC - Sistema de Gestão de Competências

## Conceito Inicial
O **SGC (Sistema de Gestão de Competências)** é uma aplicação corporativa para mapeamento, revisão e diagnóstico de competências organizacionais. O sistema permite que as unidades mapeiem suas atividades e conhecimentos necessários, identifiquem lacunas (gaps) de competência e gerenciem o desenvolvimento de suas equipes.

## Visão e Objetivos
O sistema visa gerenciar sistematicamente as competências técnicas das unidades organizacionais do tribunal.
- **Objetivos Primários:** Mapeamento e identificação de gaps de competência organizacional; Geração de diagnósticos e relatórios estratégicos.
- **Objetivo Secundário:** Apoiar o planejamento e a gestão do desenvolvimento dos servidores com base nos dados coletados.

## Público-Alvo (Atores)
O acesso e as permissões são determinados automaticamente pela hierarquia do SGRH ou por atribuições temporárias.
- **ADMIN (SEDOC):** Administrador da unidade raiz. Gerencia processos, configura competências e homologa mapas de competências.
- **GESTOR:** Responsável por unidade intermediária (ex-coordenador). Valida ou devolve informações das unidades subordinadas.
- **CHEFE:** Responsável por unidade operacional. Cadastra atividades/conhecimentos da unidade e submete para validação.
- **SERVIDOR:** Servidor lotado em unidade operacional. Atua especificamente na fase de **Diagnóstico**.

## Funcionalidades Principais
1. **Mapeamento:** Coleta sistemática de atividades e conhecimentos das unidades operacionais para a criação do mapa de competências.
2. **Revisão:** Atualização periódica dos mapas de competências para refletir mudanças organizacionais.
3. **Diagnóstico:** Avaliação de importância e proficiência para identificar lacunas e necessidades de capacitação.
4. **Fluxo de Validação:** Ciclos de aprovação hierárquica dos mapas de competências com situações precisas (ex: 'Disponibilizado', 'Homologado', 'Com sugestões').
5. **Navegação Hierárquica:** Gestão visual da árvore de unidades para revisão e estratégia dos mapas de competências.

## Restrições e Prioridades
- **Acesso Baseado em Hierarquia:** Sincronização automática de perfis com o SGRH.
- **Integridade do Fluxo:** Adesão estrita à máquina de estados para cada subprocesso.
- **Auditabilidade:** Histórico preciso de alterações nos mapas de competências e justificativas de revisões.
- **Usabilidade:** Feedback visual claro sobre a situação dos processos para orientar os usuários na gestão dos mapas de competências.
