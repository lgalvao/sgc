# Plano de Padronização de E-mails do SGC

Data de referência: 28/04/2026

## Objetivo

Padronizar os e-mails do SGC em dois eixos:

- conteúdo alinhado aos arquivos `etc/reqs/cdu-xx.md`;
- estrutura visual simples, sóbria e consistente, adequada a clientes corporativos de e-mail.

Escopo atual:

- manter abordagem simples com Thymeleaf nativo;
- usar layout compartilhado;
- evitar HTML pesado, estilos chamativos, JS e dependências desnecessárias;
- deixar CSS inlining para uma etapa futura.

## Fonte de verdade

Base primária para assunto e corpo:

- `etc/reqs/cdu-xx.md`

Documentos derivados, úteis mas não primários:

- `etc/reqs/regras-negocio.md`

## Estrutura atual

Layout compartilhado:

- [backend/src/main/resources/templates/email/_layout.html](C:/sgc/backend/src/main/resources/templates/email/_layout.html)

Mapeamento auxiliar criado durante o trabalho:

- [planos/matriz-emails-cdu.md](C:/sgc/planos/matriz-emails-cdu.md)

## O que já foi feito

### 1. Base estrutural

- Todos os templates de e-mail passaram a usar o layout compartilhado `_layout.html`.
- O caminho do fragmento foi corrigido para o resolver atual do projeto:
  - usar `~{_layout :: email(...)}`
  - não usar `~{email/_layout :: email(...)}`
- O layout ficou mais conservador para ambiente corporativo:
  - sem JS;
  - sem efeitos visuais chamativos;
  - com identidade visual única no arquivo de layout.

### 2. E-mails já revisados contra CDU

#### Início e finalização de processo

- [backend/src/main/resources/templates/email/email-inicio-processo-consolidado.html](C:/sgc/backend/src/main/resources/templates/email/email-inicio-processo-consolidado.html)
- [backend/src/main/resources/templates/email/processo-finalizado-por-unidade.html](C:/sgc/backend/src/main/resources/templates/email/processo-finalizado-por-unidade.html)
- [backend/src/main/resources/templates/email/processo-finalizado-unidades-subordinadas.html](C:/sgc/backend/src/main/resources/templates/email/processo-finalizado-unidades-subordinadas.html)

Ajustes relacionados:

- [backend/src/main/java/sgc/alerta/EmailModelosService.java](C:/sgc/backend/src/main/java/sgc/alerta/EmailModelosService.java)
- [backend/src/main/java/sgc/processo/service/ProcessoService.java](C:/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java)

#### Lembrete e atribuição temporária

- [backend/src/main/resources/templates/email/lembrete-prazo.html](C:/sgc/backend/src/main/resources/templates/email/lembrete-prazo.html)
- [backend/src/main/resources/templates/email/atribuicao-temporaria.html](C:/sgc/backend/src/main/resources/templates/email/atribuicao-temporaria.html)

#### Cadastro

- [backend/src/main/resources/templates/email/cadastro-disponibilizado.html](C:/sgc/backend/src/main/resources/templates/email/cadastro-disponibilizado.html)
- [backend/src/main/resources/templates/email/cadastro-disponibilizado-superior.html](C:/sgc/backend/src/main/resources/templates/email/cadastro-disponibilizado-superior.html)
- [backend/src/main/resources/templates/email/cadastro-devolvido.html](C:/sgc/backend/src/main/resources/templates/email/cadastro-devolvido.html)
- [backend/src/main/resources/templates/email/cadastro-devolvido-superior.html](C:/sgc/backend/src/main/resources/templates/email/cadastro-devolvido-superior.html)
- [backend/src/main/resources/templates/email/aceite-cadastro.html](C:/sgc/backend/src/main/resources/templates/email/aceite-cadastro.html)
- [backend/src/main/resources/templates/email/aceite-cadastro-superior.html](C:/sgc/backend/src/main/resources/templates/email/aceite-cadastro-superior.html)

#### Revisão de cadastro

- [backend/src/main/resources/templates/email/disponibilizacao-revisao-cadastro.html](C:/sgc/backend/src/main/resources/templates/email/disponibilizacao-revisao-cadastro.html)
- [backend/src/main/resources/templates/email/disponibilizacao-revisao-cadastro-superior.html](C:/sgc/backend/src/main/resources/templates/email/disponibilizacao-revisao-cadastro-superior.html)
- [backend/src/main/resources/templates/email/devolucao-revisao-cadastro.html](C:/sgc/backend/src/main/resources/templates/email/devolucao-revisao-cadastro.html)
- [backend/src/main/resources/templates/email/devolucao-revisao-cadastro-superior.html](C:/sgc/backend/src/main/resources/templates/email/devolucao-revisao-cadastro-superior.html)
- [backend/src/main/resources/templates/email/aceite-revisao-cadastro.html](C:/sgc/backend/src/main/resources/templates/email/aceite-revisao-cadastro.html)
- [backend/src/main/resources/templates/email/aceite-revisao-cadastro-superior.html](C:/sgc/backend/src/main/resources/templates/email/aceite-revisao-cadastro-superior.html)

#### Mapa / validação

Revisados nesta rodada:

- [backend/src/main/resources/templates/email/mapa-disponibilizado.html](C:/sgc/backend/src/main/resources/templates/email/mapa-disponibilizado.html)
- [backend/src/main/resources/templates/email/mapa-disponibilizado-superior.html](C:/sgc/backend/src/main/resources/templates/email/mapa-disponibilizado-superior.html)
- [backend/src/main/resources/templates/email/sugestoes-mapa.html](C:/sgc/backend/src/main/resources/templates/email/sugestoes-mapa.html)
- [backend/src/main/resources/templates/email/sugestoes-mapa-superior.html](C:/sgc/backend/src/main/resources/templates/email/sugestoes-mapa-superior.html)
- [backend/src/main/resources/templates/email/validacao-mapa.html](C:/sgc/backend/src/main/resources/templates/email/validacao-mapa.html)
- [backend/src/main/resources/templates/email/validacao-mapa-superior.html](C:/sgc/backend/src/main/resources/templates/email/validacao-mapa-superior.html)
- [backend/src/main/resources/templates/email/devolucao-validacao.html](C:/sgc/backend/src/main/resources/templates/email/devolucao-validacao.html)
- [backend/src/main/resources/templates/email/devolucao-validacao-superior.html](C:/sgc/backend/src/main/resources/templates/email/devolucao-validacao-superior.html)
- [backend/src/main/resources/templates/email/aceite-validacao.html](C:/sgc/backend/src/main/resources/templates/email/aceite-validacao.html)
- [backend/src/main/resources/templates/email/aceite-validacao-superior.html](C:/sgc/backend/src/main/resources/templates/email/aceite-validacao-superior.html)

### 3. Testes criados/reforçados

#### Testes de renderização real

- [backend/src/test/java/sgc/integracao/EmailModelosRenderIntegrationTest.java](C:/sgc/backend/src/test/java/sgc/integracao/EmailModelosRenderIntegrationTest.java)

Esse teste hoje cobre:

- início de processo;
- finalização de processo;
- lembrete de prazo;
- atribuição temporária;
- cadastro disponibilizado;
- cadastro devolvido;
- cadastro aceito;
- disponibilização de revisão de cadastro;
- devolução de revisão de cadastro;
- aceite de revisão de cadastro.
- mapa disponibilizado;
- mapa disponibilizado para unidade superior.
- sugestões para mapa;
- validação de mapa.
- devolução de validação de mapa;
- aceite de validação de mapa.

#### Teste sistêmico de padrão visual

- [backend/src/test/java/sgc/alerta/EmailTemplatesPadraoVisualTest.java](C:/sgc/backend/src/test/java/sgc/alerta/EmailTemplatesPadraoVisualTest.java)

Esse teste garante:

- existência do layout compartilhado como centro da identidade visual;
- uso obrigatório do layout compartilhado por todos os templates;
- ausência de `<style>` e `<head>` locais nos templates de conteúdo;
- existência dos fragmentos `cabecalho` e `conteudo`;
- ausência de `style=` inline e `class="btn"` para o conjunto de templates já revisados.

#### Testes de integração com GreenMail

Fluxos reforçados para validar destinatário, assunto e corpo:

- [backend/src/test/java/sgc/integracao/CDU04IntegrationTest.java](C:/sgc/backend/src/test/java/sgc/integracao/CDU04IntegrationTest.java)
- [backend/src/test/java/sgc/integracao/CDU05IntegrationTest.java](C:/sgc/backend/src/test/java/sgc/integracao/CDU05IntegrationTest.java)
- [backend/src/test/java/sgc/integracao/CDU21IntegrationTest.java](C:/sgc/backend/src/test/java/sgc/integracao/CDU21IntegrationTest.java)
- [backend/src/test/java/sgc/integracao/CDU28IntegrationTest.java](C:/sgc/backend/src/test/java/sgc/integracao/CDU28IntegrationTest.java)

#### Testes unitários de apoio

- [backend/src/test/java/sgc/alerta/notificacao/EmailModelosServiceTest.java](C:/sgc/backend/src/test/java/sgc/alerta/notificacao/EmailModelosServiceTest.java)

## Estado atual dos testes

Últimas validações relevantes executadas:

```powershell
./gradlew :backend:test --tests sgc.alerta.EmailTemplatesPadraoVisualTest --tests sgc.integracao.EmailModelosRenderIntegrationTest
./gradlew :backend:test --tests sgc.alerta.notificacao.EmailModelosServiceTest --tests sgc.integracao.EmailModelosRenderIntegrationTest --tests sgc.integracao.CDU04IntegrationTest --tests sgc.integracao.CDU05IntegrationTest --tests sgc.integracao.CDU21IntegrationTest --tests sgc.integracao.CDU28IntegrationTest
```

Status na última execução:

- verde.

## O que ainda falta

### 1. Grupo mapa / validação

Status atual:

- bloco principal de `mapa / validação` revisado.
- falta apenas decidir se algum template desse grupo ainda possui versão legada paralela sem uso.

Fonte principal:

- `etc/reqs/cdu-17.md`
- `etc/reqs/cdu-19.md`
- `etc/reqs/cdu-20.md`
- `etc/reqs/cdu-24.md`
- `etc/reqs/cdu-25.md`

### 2. Ajuste de outros templates ainda não revisados

- [backend/src/main/resources/templates/email/data-limite-alterada.html](C:/sgc/backend/src/main/resources/templates/email/data-limite-alterada.html)

### 3. Templates legados / órfãos

Precisam de decisão:

- [backend/src/main/resources/templates/email/processo-iniciado.html](C:/sgc/backend/src/main/resources/templates/email/processo-iniciado.html)
- [backend/src/main/resources/templates/email/processo-finalizado.html](C:/sgc/backend/src/main/resources/templates/email/processo-finalizado.html)
- [backend/src/main/resources/templates/email/email-inicio-processo-operacional.html](C:/sgc/backend/src/main/resources/templates/email/email-inicio-processo-operacional.html)
- [backend/src/main/resources/templates/email/email-inicio-processo-intermediario.html](C:/sgc/backend/src/main/resources/templates/email/email-inicio-processo-intermediario.html)
- [backend/src/main/resources/templates/email/homologacao-mapa.html](C:/sgc/backend/src/main/resources/templates/email/homologacao-mapa.html)

Sugestão:

- confirmar se ainda são usados;
- remover se realmente estiverem órfãos;
- ou migrar/alinhar se houver uso indireto.

## Ordem recomendada para continuar

1. Revisar `data-limite-alterada.html`.
2. Identificar e decidir o destino dos templates legados/órfãos.
3. Verificar se ainda há templates ativos fora do conjunto “sem style inline / sem btn” e ampliar o teste sistêmico onde couber.
4. Se houver tempo, reforçar integrações GreenMail para algum fluxo de mapa/validação além dos já existentes.

## Regras práticas para continuar

- sempre ler primeiro o `cdu-xx.md` correspondente;
- preferir links simples ao sistema em vez de botões chamativos;
- evitar `style=` inline em templates novos/revisados;
- evitar `highlight-box` quando o CDU não exigir destaque real;
- usar apenas variáveis que o fluxo atual realmente fornece;
- ao revisar um template, acrescentar ou atualizar teste de renderização;
- quando um grupo estiver estável, incluir seus arquivos no teste sistêmico de padrão visual.

## Observação para o futuro

Foi levantada a possibilidade de pós-processar o HTML com CSS inlining antes do envio, porque clientes de e-mail têm suporte inconsistente a CSS em `<style>`.

Essa ideia foi considerada útil, mas ficou explicitamente fora do escopo atual.

Se for retomada depois:

- o ponto natural de entrada é [backend/src/main/java/sgc/alerta/EmailService.java](C:/sgc/backend/src/main/java/sgc/alerta/EmailService.java);
- o fluxo seria:
  - Thymeleaf renderiza;
  - inliner aplica CSS inline;
  - `JavaMailSender` envia.
