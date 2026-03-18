# Alinhamento CDU-26 - Homologar validação de mapas de competências em bloco

## Cobertura atual do teste
O teste cobre:
- **Setup preparatório**: Criação de processo, disponibilização de atividades, homologação de cadastro por múltiplas gestores, criação/disponibilização de mapa, validação e aceite de mapa pelos gestores.
- **Cenário 1**: Verificação de visibilidade e habilitação do botão "Homologar mapas em bloco" após ADMIN acessar processo em andamento.
- **Cenário 2**: Verificação de abertura do modal, validação de título, texto, lista de unidades, tabela e botões.
- **Cenário 3**: Verificação de cancelamento do modal e permanência na tela "Detalhes do processo".

## Lacunas em relação ao requisito
**Lacunas críticas:**

1. **Falta validação de pré-condições**: O requisito exige subprocessos em situações 'Mapa validado' **OU** 'Mapa com sugestões'. O teste apenas valida com 'Mapa validado'. Não há cenário com 'Mapa com sugestões'.

2. **Falta validação de localização na unidade ADMIN**: O requisito especifica (pré-condição) "com localização atual na unidade do usuário" (ADMIN). O teste não valida explicitamente isso.

3. **Ausência de cenário de homologação efetiva**: O teste abre o modal e cancela, mas **não executa a homologação** propriamente dita. Não há validação de:
   - Clique efetivo no botão "Homologar"
   - Alteração de situação para 'Mapa homologado'
   - Registros de movimentação, alerta e e-mail

4. **Falta validação de mudança de situação**: O requisito especifica (passo 9.2) que a situação do subprocesso deve mudar para 'Mapa homologado'. O teste não valida isso.

5. **Falta validação de movimentação registrada**: O requisito especifica (passo 9.1) o registro de movimentação com:
   - `Unidade origem`: "ADMIN"
   - `Unidade destino`: "ADMIN"
   - `Descrição`: "Mapa de competências homologado"
   
   O teste não valida nenhum desses registros.

6. **Falta validação de alerta**: O requisito especifica (passo 9.3) criação de alerta com descrição específica. O teste não valida.

7. **Falta validação de e-mail**: O requisito especifica (passo 9.4) envio de e-mail para "a unidade do subprocesso". O teste não valida.

8. **Falta validação de múltiplas unidades**: O teste não cobre seleção/deselecção de unidades no modal ou homologação seletiva.

9. **Falta validação de mensagem de confirmação final**: O requisito especifica (passo 10) mensagem "Mapas de competências homologados em bloco" e redirecionamento para Painel. O teste não valida isso.

10. **Falta validação do tipo de unidades elegíveis**: O requisito especifica "operacionais ou interoperacionais". O teste não valida se apenas essas unidades aparecem.

## Alterações necessárias no teste E2E
1. **Adicionar cenário de homologação completa**: 
   - Criar teste que executa a homologação efetivamente (clique em "Homologar", não "Cancelar")
   - Validar mensagem de confirmação "Mapas de competências homologados em bloco"
   - Validar redirecionamento para Painel

2. **Adicionar validação de mudança de situação**: 
   - Após homologação, navegar de volta para o subprocesso
   - Validar que a situação agora é 'Mapa homologado'

3. **Adicionar validação de registros internos**: 
   - Validar movimentação registrada (origem/destino "ADMIN", descrição)
   - Validar alerta criado com campos esperados
   - Validar e-mail enviado para unidade do subprocesso

4. **Adicionar cenário com 'Mapa com sugestões'**: Criar teste paralelo com essa situação.

5. **Adicionar validação de seleção/deselecção**: 
   - Validar comportamento ao desselecionar unidades
   - Validar que apenas as selecionadas são homologadas

6. **Adicionar validação de filtro de unidades**: 
   - Validar que apenas unidades operacionais/interoperacionais aparecem (se aplicável)

## Notas e inconsistências do requisito
- **Ambiguidade em "localização atual na unidade do usuário"**: Para ADMIN, qual seria sua "unidade"? O requisito assume que ADMIN tem uma unidade associada, mas isso não é claro. Presume-se que refira-se à possibilidade de o subprocesso estar em qualquer lugar da hierarquia.

- **Inconsistência em campo "Unidade de origem" do alerta**: O passo 9.3 especifica `Unidade de origem: ADMIN`, mas não é claro se "ADMIN" é a sigla literal ou um placeholder. Presume-se ser a sigla literal ou identificador especial.

- **Falta informação sobre estados anteriores**: O requisito não especifica quais estados o subprocesso pode ter antes de chegar a 'Mapa validado' ou 'Mapa com sugestões', tornando difícil construir dados de teste realistas.
