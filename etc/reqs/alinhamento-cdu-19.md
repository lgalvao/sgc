# Alinhamento CDU-19 - Validar mapa de competências

## Cobertura atual do teste
O teste E2E cobre:
- **Navegação**: Acesso ao painel, seleção do processo e navegação até a tela de visualização do mapa (passos 1-2 do CDU).
- **Botões apresentados**: Verificação da presença dos botões "Apresentar sugestões" e "Validar" (passo 2).
- **Cancelamento de validação**: Clique no botão "Validar", abertura do diálogo de confirmação, clique em "Cancelar" e permanência na tela (passo 5.1.1).
- **Validação bem-sucedida**: Clique em "Validar", confirmação no diálogo e redirecionamento ao painel (passos 5.2-5.3).
- **Verificação de situação**: Confirmação de que a situação do subprocesso mudou para "Mapa validado" (passo 5.3).

## Lacunas em relação ao requisito
O teste **NÃO cobre**:

- **Passo 3 - Botão "Histórico de análise"**: O requisito especifica que se o subprocesso retornou de análise, deve-se exibir um botão "Histórico de análise" com dados de análises prévias em uma tabela modal. O teste não valida:
  - A presença/ausência condicional deste botão
  - A abertura do modal com dados de análises (data/hora, sigla da unidade, resultado, observações)
  - Qualquer cenário de retorno de análise pelas unidades superiores

- **Passo 4 - Fluxo completo de "Apresentar sugestões"**: O teste não cobre:
  - Clique no botão "Apresentar sugestões" (passo 4)
  - Abertura do modal com campo de texto formatado obrigatório (passo 4.1)
  - Preenchimento de sugestões e clique em "Confirmar" (passo 4.2)
  - Armazenamento das sugestões (passo 4.3)
  - Mudança de situação para "Mapa com sugestões" (passo 4.3)
  - Notificação por e-mail à unidade superior (passo 4.4)
  - Criação de alerta interno (passo 4.5)
  - Mensagem de confirmação "Mapa submetido com sugestões para análise da unidade superior" (passo 4.6)
  - Caso onde há sugestões prévias e o campo vem preenchido (passo 4.1)

- **Passos 5.4-5.6 (Pós-validação)**:
  - Notificação por e-mail à unidade superior (passo 5.4) - não testado
  - Criação de alerta interno (passo 5.5) - não testado
  - Mensagem exata "Mapa validado e submetido para análise à unidade superior" (passo 5.6) - o teste verifica redirecionamento mas não a mensagem específica

- **Passos 6-8 (Efeitos colaterais)**:
  - Registro de movimentação do subprocesso com descrição "Validação do mapa de competências" (passo 6)
  - Definição da data/hora de conclusão da etapa 2 do subprocesso (passo 7)

- **Cenários alternativos e pré-condições**:
  - Teste não valida se o usuário não tem perfil CHEFE
  - Teste não valida a pré-condição "Processo de mapeamento ou revisão com subprocesso na situação 'Mapa disponibilizado'"
  - Teste não cobre múltiplos cenários de sugestões (campo vazio vs. campo preenchido com sugestões anteriores)

## Alterações necessárias no teste E2E
Para alinhamento completo com o requisito, o teste deve:

1. **Adicionar cobertura do fluxo "Apresentar sugestões"**:
   - Criar um novo teste que clique em "Apresentar sugestões"
   - Validar a abertura do modal com campo de texto formatado obrigatório
   - Preencher sugestões e confirmar
   - Verificar mensagem "Mapa submetido com sugestões para análise da unidade superior"
   - Validar mudança de situação para "Mapa com sugestões"

2. **Adicionar cobertura do botão "Histórico de análise"**:
   - Criar fixture que devolva um subprocesso com análises prévias registradas
   - Testar clique no botão "Histórico de análise"
   - Validar abertura do modal com tabela contendo: data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite'), observações

3. **Melhorar o teste de validação bem-sucedida**:
   - Validar a mensagem exata "Mapa validado e submetido para análise à unidade superior" (passo 5.6), não apenas redirecionamento
   - Validar presença/ausência condicional do botão "Histórico de análise" conforme pré-condição

4. **Adicionar validações de pré-condições**:
   - Teste de acesso negado para usuário sem perfil CHEFE
   - Teste de indisponibilidade quando o subprocesso não está em "Mapa disponibilizado"

## Notas e inconsistências do requisito
- **Clareza no texto do e-mail**: O requisito possui um typo no passo 5.4: "no O sistema de Gestão de Competências" (apresenta "no O" redundante). Mesma redundância aparece no passo 4.4.
- **Ambiguidade em "texto formatado"**: O requisito menciona "campo de texto formatado" no passo 4.1, mas não especifica se é editor WYSIWYG, markdown, RTF ou apenas textarea HTML básico. A implementação pode diferir da expectativa.
- **Falta de especificação de validação do campo obrigatório**: O requisito não detalha se o sistema impede submissão se o campo estiver vazio ou apenas exibe erro.
- **Ausência de regras de acesso explícitas**: O requisito está ligado à pré-condição "CHEFE" mas não há validação explícita de quem pode acessar a tela (há risco de GESTOR ou outro perfil conseguir acessar).
