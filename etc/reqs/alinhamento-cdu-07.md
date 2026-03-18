
# Alinhamento CDU-07 - Detalhar subprocesso

## Cobertura atual do teste
O teste `cdu-07.spec.ts` cobre:
- **Navegação**: Acesso a detalhes de subprocesso via tabela de processos para 3 perfis (ADMIN, GESTOR, CHEFE)
- **Seção dados da Unidade**: Verifica exibição de "Titular" (linha 44)
- **Seção movimentações**: Valida presença de tabela com movimentações (linha 56)
- **Regras de Habilitação de Cards**: Testa habilitação/desabilitação de cards "Atividades" e "Mapa de Competências" para cada perfil:
  - ADMIN (antes de homologação): ambos desabilitados
  - GESTOR (antes de disponibilização): card de atividades desabilitado
  - CHEFE: card de atividades habilitado, card de mapa desabilitado

## Lacunas em relação ao requisito
O requisito especifica 2.1.1 até 2.1.6, mas o teste:
- **NÃO valida**: "Sigla e nome da unidade" destacado (2.1.1)
- **NÃO valida**: "Responsável" (2.1.3) com tipo de responsabilidade ("Titular", "Substituição", "Atrib. temporária"), ramal e e-mail
- **NÃO valida**: "Situação do Subprocesso" descritiva (2.1.4)
- **NÃO valida**: "Localização atual" (2.1.5)
- **NÃO valida**: "Prazo para conclusão da etapa atual" (2.1.6)
- **NÃO verifica**: Seção "Elementos do Processo" (2.3) - cards clicáveis com descrições específicas ("Cadastro de atividades e conhecimentos da unidade", "Mapa de competências da unidade", "Diagnóstico da equipe", "Ocupações críticas")
- **NÃO valida**: Distinção entre processo Mapeamento/Revisão vs Diagnóstico (2.3.1 vs 2.3.2)
- **NÃO valida**: Ordem decrescente de data/hora nas movimentações (2.2)
- **NÃO testa**: Navegação para tela Detalhes do Processo por ADMIN/GESTOR antes de chegar ao subprocesso (fluxo 2 do requisito)
- **NÃO valida**: Diferença de comportamento entre processo "Em andamento" vs "Finalizado" para SERVIDOR

## Alterações necessárias no teste E2E
- Adicionar verificações de todos os campos da seção Dados da Unidade (sigla, nome, titular, responsável com tipos, ramal, e-mail, situação, localização, prazo)
- Validar presença, visibilidade e textos dos cards na seção "Elementos do Processo" com descrições exatas
- Testar habilitação de card de "Diagnóstico da equipe" e "Ocupações críticas" para processos de tipo Diagnóstico
- Verificar ordem decrescente de data/hora nas movimentações
- Adicionar teste para perfil SERVIDOR
- Validar distinção visual/comportamental entre situações "Em andamento" e "Finalizado"
- Testar habilitação do card Mapa de Competências após homologação (pré-condição necessária)

## Notas e inconsistências do requisito
- **Ambiguidade em 2.1.2**: "Titular (exibido apenas se não for o responsável)" - não fica claro se é o servidor titular da unidade ou outro conceito
- **Referência incompleta em 2.1.4**: "ver seção Situações de subprocessos" aponta para definição externa não incluída neste requisito
- **Indefinição de "Mapa vigente"**: O requisito menciona "mapa vigente" mas não o define neste CDU
