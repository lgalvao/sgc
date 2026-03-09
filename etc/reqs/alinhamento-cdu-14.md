# Alinhamento CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos

## Cobertura atual do teste
O teste E2E cobre:
- **Setup (Preparação)**: Cria processo de revisão, CHEFE adiciona/modifica atividades, disponibiliza (linhas 29-78).
- **Histórico de análise**: GESTOR abre histórico em modo visualização; testa apresentação em tabela (linhas 60-62).
- **Botão "Impactos no mapa"**: Testa presença e funcionalidade (linha 64).
- **Devolução**: GESTOR devolve revisão com observação (linha 65).
- **Cancelamento de devolução**: GESTOR clica para devolver, depois cancela (linha 86).
- **Aceite de revisão**: GESTOR aceita revisão com observação (linha 88).
- **Visualização de histórico por ADMIN**: Em modo visualização após aceite (linhas 94-96).

## Lacunas em relação ao requisito
**Fluxo principal não coberto:**
- **Passos 1-4**: Não há assertions explícitas de navegação via Painel, tela "Detalhes do processo", seleção da unidade subordinada.
- **Passo 5-6**: Não valida explicitamente que o card `Atividades e conhecimentos` é clicado ou que botões aparecem (`Impactos no mapa`, `Histórico de análise`, `Devolver para ajustes`, `Registrar aceite`/`Homologar`).

**Fluxo de devolução (passo 10) não completamente coberto:**
- **10.2**: Não valida modal com título "Devolução", pergunta exata "Confirma a devolução do cadastro para ajustes?".
- **10.5-10.10**: Não valida registros de análise, movimentação com descrição correta, identificação de unidade de devolução, mudança de situação para "Revisão do cadastro em andamento", e-mail e alerta.
- **10.11**: Não valida mensagem "Devolução realizada" e redirecionamento para Painel.

**Fluxo de aceite (passo 11) não completamente coberto:**
- **11.2**: Não valida modal com título "Aceite" e pergunta exata "Confirma o aceite da revisão do cadastro de atividades?".
- **11.5-11.8**: Não valida registros de análise, movimentação, e-mail com assunto/corpo específicos, alerta criado.
- **11.9**: Não valida mensagem "Aceite registrado".

**Fluxo de homologação (passo 12) não coberto:**
- **12.1-12.4**: Cenário de "sem impactos no mapa" - modal com título "Homologação do mapa de competências", mensagem específica, mudança de situação para "Mapa homologado".
- **12.3.1-12.4**: Cenário de "com impactos detectados" - modal com título "Homologação do cadastro de atividades e conhecimentos", movimentação registrada, situação alterada para "Revisão do cadastro homologada".
- Teste não verifica se o ADMIN realmente escolhe entre os dois caminhos baseado em impactos.

**Modal de histórico:**
- Não valida estrutura completa: data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite'), observações em tabela.

**Botão "Impactos no mapa":**
- Passo 7: Teste valida presença do botão mas não executa o fluxo "Verificar impactos no mapa de competências" referenciado.

## Alterações necessárias no teste E2E
1. **Cobertura de botões obrigatórios (passo 6)**: Validar presença simultânea de `Impactos no mapa`, `Histórico de análise`, `Devolver para ajustes`, `Registrar aceite`/`Homologar`.

2. **Cenário de cancelamento de devolução completo**: Validar que modal abre com título, pergunta, botões, e ao clicar Cancelar, permanece em `Atividades e conhecimentos`.

3. **Validar diálogos modais com textos exatos**:
   - Devolução: título "Devolução", pergunta "Confirma a devolução do cadastro para ajustes?".
   - Aceite: título "Aceite", pergunta "Confirma o aceite da revisão do cadastro de atividades?".
   - Homologação: dois diálogos diferentes conforme presença de impactos.

4. **Cenário de homologação**: Adicionar teste que como ADMIN clica "Homologar" e valida:
   - Se há impactos: abre modal "Homologação do cadastro de atividades e conhecimentos".
   - Se sem impactos: abre modal "Homologação do mapa de competências" com mensagem sobre manutenção.
   - Registra movimentação correta.
   - Altera situação conforme esperado.

5. **Validar registros de análise e movimentação**: Consultar backend ou UI que registra Data/hora, Unidade, Resultado, Observação após devolução/aceite.

6. **Validar notificações**: E-mail enviado com assunto/corpo corretos; alerta criado com descrição e unidades.

7. **Validar mudanças de situação**: 
   - Devolução para própria unidade → "Revisão do cadastro em andamento".
   - Aceite → "Revisão do cadastro em andamento" → movimentação para unidade superior.
   - Homologação → "Revisão do cadastro homologada" ou "Mapa homologado".

8. **Validar histórico de análise**: Deve mostrar tabela com data/hora, sigla da unidade, resultado, observações; usar resultado com enum correto (ex: "ACEITE_REVISAO" em vez de "Aceite").

## Notas e inconsistências do requisito
- **Linha 134 (passo 11.7)**: Há erro de digitação "O sistema de Gestão de Competências ([URL_SISTEMA])" deveria dizer "no O sistema..." (extra "no").
- **Linha 152 (passo 12.2)**: Lógica de "impactos detectados" não é especificada - como o sistema detecta? Requisito não detalha o algoritmo.
- **Ambiguidade em passo 12.3.4-12.3.5**: Quando há impactos, registra movimentação com Unidade origem/destino = 'ADMIN'? Isso é diferente do fluxo de aceite (linhas 119-121) onde há unidades reais. Não fica claro se isso é intencional.
- **Falta de cenário de recusa**: Requisito não especifica se ADMIN pode devolver uma revisão ou apenas aceitar/homologar.
