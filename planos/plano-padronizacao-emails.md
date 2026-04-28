# Plano de Padronização de E-mails do SGC

Data de referência: 28/04/2026 (atualizado em 28/04/2026)

## Objetivo

Padronizar os e-mails do SGC em dois eixos:

- conteúdo alinhado aos arquivos `etc/reqs/cdu-xx.md`;
- estrutura visual simples, sóbria e consistente, adequada a clientes corporativos de e-mail.

## Diretrizes de continuidade

### Fonte de verdade

- Base primária para assunto e corpo: `etc/reqs/cdu-xx.md`.
- Documento derivado de apoio: `etc/reqs/regras-negocio.md`.

### Padrão técnico atual

- Manter templates com Thymeleaf nativo e layout compartilhado `_layout.html`.
- Usar fragmento via `~{_layout :: email(...)}`.
- Evitar HTML pesado, JavaScript, efeitos visuais chamativos e dependências desnecessárias.
- Não introduzir `style=` inline em templates novos/revisados.
- Preferir links simples ao sistema em vez de botões chamativos.
- Evitar `highlight-box` quando o CDU não exigir destaque real.
- Usar apenas variáveis efetivamente fornecidas pelo fluxo atual.

### Regra de validação ao alterar template

- Ler primeiro o `cdu-xx.md` correspondente.
- Ao revisar template, incluir/ajustar teste de renderização.
- Quando um grupo estiver estável, garantir inclusão no teste sistêmico de padrão visual.

## Pendências

1. **Revisar redação dos templates de reabertura por CDU**
   - Arquivos-alvo:
     - `backend/src/main/resources/templates/email/cadastro-reaberto.html`
     - `backend/src/main/resources/templates/email/cadastro-reaberto-superior.html`
     - `backend/src/main/resources/templates/email/revisao-cadastro-reaberta.html`
     - `backend/src/main/resources/templates/email/revisao-cadastro-reaberta-superior.html`
   - Referência principal: CDU de reabertura correspondente.

2. **Confirmar cobertura GreenMail para alteração de data limite (CDU-27)**
   - Validar destinatário, assunto e trechos essenciais do corpo para `data-limite-alterada`.
   - Ajustar/expandir cenário de integração se necessário.

3. **Avaliar expansão do teste sistêmico de padrão visual**
   - Critério: reforçar ausência de `highlight-box` onde o CDU não exige destaque.
   - Arquivo-base do teste: `backend/src/test/java/sgc/alerta/EmailTemplatesPadraoVisualTest.java`.

## Observação futura (fora de escopo atual)

- CSS inlining no pós-processamento de HTML antes do envio.
- Ponto de entrada natural, se retomado: `backend/src/main/java/sgc/alerta/EmailService.java`.
