# Alinhamento CDU-25 - Aceitar validação de mapas de competências em bloco

## Cobertura atual do teste
O teste cobre:
- **Setup preparatório**: Criação de processo, disponibilização de atividades, homologação de cadastro, criação e disponibilização de mapa, validação do mapa pelo chefe.
- **Cenário 1**: Verificação de visibilidade e habilitação do botão "Aceitar mapas em bloco" após GESTOR acessar processo em andamento.
- **Cenário 2**: Verificação de abertura, cancelamento e fechamento do modal.
- **Cenário 3**: Verificação de título/texto do modal, checkbox selecionado por padrão, mensagem de confirmação "Mapas aceitos em bloco" e redirecionamento para Painel.

## Lacunas em relação ao requisito
**Lacunas críticas:**

1. **Falta validação de pré-condições**: O requisito exige que subprocessos estejam em situações 'Mapa validado' **OU** 'Mapa com sugestões'. O teste apenas valida com 'Mapa validado'. Não há cenário com 'Mapa com sugestões'.

2. **Falta validação de localização**: O requisito especifica que subprocessos devem estar com "localização atual na unidade do usuário". O teste não valida explicitamente isso.

3. **Falta verificação de conteúdo do modal**: O teste não valida:
   - Se as unidades corretas são exibidas na lista (especificamente que mostra apenas as elegíveis)
   - Se o texto "Selecione as unidades para aceite dos mapas correspondentes" está exatamente como especificado

4. **Ausência de validação de múltiplas unidades**: O teste apenas trabalha com uma unidade (SECAO_211). Não valida o cenário de múltiplas unidades subordinadas onde o gestor pode selecionar/deselecionar seletivamente.

5. **Falta de validação de movimentação e alerta internos**: O requisito especifica (passo 8) o registro de:
   - Análise de validação (resultado "Aceite de mapa", observação, etc.)
   - Movimentação (unidade origem/destino, descrição)
   - Alerta (descrição, processo, datas, unidades)
   
   O teste não valida nenhum desses registros internos.

6. **Falta validação de notificação por e-mail**: O requisito especifica envio de e-mail para unidade superior (passo 8.4). O teste não valida recebimento ou conteúdo do e-mail.

7. **Falta cenário de cancelamento**: Embora haja teste de cancelamento (Cenário 2), não há validação de que nenhuma movimentação/alerta é registrada após cancelamento.

8. **Falta validação de comportamento com unidades deselacionadas**: O teste não cobre o caso de o usuário desselecionar unidades antes de clicar "Registrar aceite".

## Alterações necessárias no teste E2E
1. **Adicionar cenário com 'Mapa com sugestões'**: Criar um teste paralelo que valida o fluxo com subprocessos em estado 'Mapa com sugestões'.

2. **Adicionar validação de múltiplas unidades**: 
   - Expandir a hierarquia para incluir múltiplas unidades subordinadas
   - Validar que todas aparecem no modal com checkboxes
   - Validar deselecção de unidades e verificar que apenas as selecionadas são processadas

3. **Adicionar validação de registros internos**: 
   - Após aceite, consultar API ou dados persistidos para validar:
     - Análise de validação registrada com resultado "Aceite de mapa"
     - Movimentação registrada com siglas corretas
     - Alerta criado com descrição esperada

4. **Adicionar validação de e-mail**: 
   - Simular envio de e-mail ou interceptar chamadas de API de notificação
   - Validar conteúdo esperado (assunto, corpo com descrição do processo e unidades)

5. **Adicionar teste de deselecção**: Validar que ao desselecionar uma unidade e confirmar, apenas as selecionadas são processadas.

6. **Adicionar validação pós-cancelamento**: Após cancelar no Cenário 2, validar que nenhum registro foi criado no banco de dados.

## Notas e inconsistências do requisito
- **Ambiguidade em "unidades cujos mapas poderão ser aceitos"**: O requisito não clarifica se deve mostrar apenas unidades subordinadas diretas ou toda a hierarquia recursiva. O passo 2 menciona "subprocessos das unidades que compõem a hierarquia do usuário (sua própria unidade e subordinadas recursivamente)", sugerindo inclusão de toda a hierarquia.

- **Falta clareza em dados de notificação**: O requisito especifica `[LISTA_UNIDADES_SUBORDINADAS_SELECIONADAS]` no e-mail, mas não clarifica se devem vir no formato de sigla, nome ou ambos, nem o separador (vírgula, quebra de linha, etc.).

- **Inconsistência em "unidade de origem" vs "sigla"**: O passo 8.1 e 8.2 usam `[SIGLA_UNIDADE_ATUAL]` e `[SIGLA_UNIDADE_SUPERIOR]`, mas o passo 8.3 (alerta) usa `[SIGLA_UNIDADE_SUBPROCESSO]` que não foi definida anteriormente. Presume-se ser a unidade do subprocesso sendo aceito.
