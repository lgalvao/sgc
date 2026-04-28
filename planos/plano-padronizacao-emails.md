# Plano de Padronização de E-mails do SGC

Data de referência: 28/04/2026 (atualizado em 28/04/2026)

## Objetivo

Padronizar os e-mails do SGC em dois eixos:

- conteúdo alinhado aos arquivos `etc/reqs/cdu-xx.md`;
- estrutura visual simples, sóbria e consistente, adequada a clientes corporativos de e-mail.

## Diretrizes permanentes

### Fonte de verdade

- Base primária para assunto e corpo: `etc/reqs/cdu-xx.md`.
- Documento derivado de apoio: `etc/reqs/regras-negocio.md`.

### Padrão técnico

- Manter templates com Thymeleaf nativo e layout compartilhado `_layout.html`.
- Usar fragmento via `~{_layout :: email(...)}`.
- Evitar HTML pesado, JavaScript, efeitos visuais chamativos e dependências desnecessárias.
- Não introduzir `style=` inline em templates novos/revisados.
- Preferir links simples ao sistema em vez de botões chamativos.
- Evitar `highlight-box` sem exigência explícita do CDU.
- Usar apenas variáveis efetivamente fornecidas pelo fluxo atual.

### Regra de validação ao alterar template

- Ler primeiro o `cdu-xx.md` correspondente.
- Ao revisar template, incluir/ajustar teste de renderização.
- Quando um grupo estiver estável, garantir inclusão no teste sistêmico de padrão visual.

## Estado final desta rodada

- Fluxo CDU-27 validado com checagem de outbox e entrega real via GreenMail (destinatário, assunto e corpo essencial).
- Templates de reabertura (`cadastro` e `revisão`) revisados e alinhados à redação dos CDU-32 e CDU-33.
- Teste sistêmico visual reforçado para impedir uso indevido de `highlight-box`.
- Não há pendências abertas neste plano nesta data.

3. **Avaliar expansão do teste sistêmico de padrão visual**
   - Critério: reforçar ausência de `highlight-box` onde o CDU não exige destaque.
   - Arquivo-base do teste: `backend/src/test/java/sgc/alerta/EmailTemplatesPadraoVisualTest.java`.

## Observação futura (fora de escopo atual)

- CSS inlining no pós-processamento de HTML antes do envio.
- Ponto de entrada natural, se retomado: `backend/src/main/java/sgc/alerta/EmailService.java`.
