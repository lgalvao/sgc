# Relatório de Melhoria e Padronização de UX - SGC

## 1) Escopo e método

Este relatório foi elaborado com base na execução da suíte `e2e/captura-telas.spec.ts` atualizada, que gerou **81 capturas** em `screenshots/`, cobrindo autenticação, painel, processos, subprocessos, mapa, navegação, responsividade e relatórios.

As recomendações abaixo usam evidências diretas das capturas (nome do arquivo entre crases) e focam em:
- padronização visual e comportamental;
- clareza de fluxo (especialmente ações críticas);
- redução de carga cognitiva;
- consistência entre perfis (ADMIN, GESTOR, CHEFE).

---

## 2) Cobertura visual gerada

**Total de capturas:** 81  
**Categorias:**  
- `01-seguranca` (6)
- `02-painel` (10)
- `03-processo` (9)
- `04-subprocesso` (18)
- `05-mapa` (8)
- `06-navegacao` (7)
- `07-estados` (3)
- `08-responsividade` (4)
- `09-operacoes-bloco` (2)
- `10-gestao-subprocessos` (4)
- `11-unidades` (2)
- `12-historico` (3)
- `13-configuracoes` (1)
- `14-relatorios` (4)

---

## 3) Principais achados de UX (diagnóstico)

## 3.1 Hierarquia de ações (primária/secundária/perigo) ainda irregular

**Evidências:** `03-processo--02-modal-iniciar-processo.png`, `03-processo--04-modal-finalizar-processo.png`, `05-mapa--07-modal-disponibilizar-mapa.png`, `14-relatorios--02-modal-relatorio-andamento.png`.

**Problema:** em fluxos críticos, os CTAs nem sempre mantêm o mesmo padrão visual/posicional (ex.: confirmar/cancelar; iniciar/finalizar/disponibilizar), exigindo leitura detalhada em cada modal.

**Melhoria recomendada:**
- Definir padrão fixo para rodapé de modal:
  - botão secundário à esquerda (`Cancelar`);
  - botão primário/perigoso à direita (`Confirmar`, `Finalizar`, etc.).
- Aplicar escala semântica de cores:
  - primário (ação principal),
  - neutro (cancelamento),
  - perigo (ações irreversíveis).

---

## 3.2 Estados de validação são bons, mas ainda heterogêneos no sistema

**Evidências:** `03-processo--10-botoes-desativados-form-vazio.png` até `03-processo--13-botoes-ativados-form-completo.png`, `04-subprocesso--23-validacao-inline-primeira-atividade.png`, `04-subprocesso--25-detalhe-card-com-erro.png`, `04-subprocesso--26-erro-desaparece-apos-correcao.png`.

**Ponto positivo:** a área de atividades mostra validação inline clara e evolução do erro após correção.

**Gap:** esse padrão de feedback (mensagem próxima ao campo + indicação explícita do item com erro) não parece aplicado de forma igualmente forte em todos os formulários.

**Melhoria recomendada:**
- Criar padrão único de validação:
  - borda + ícone + mensagem contextual abaixo do campo;
  - resumo de erros no topo em formulários longos;
  - foco automático no primeiro erro.

---

## 3.3 Densidade de informação alta em telas de tabela/gestão

**Evidências:** `02-painel--06a-tabela-processos.png`, `07-estados--03-tabela-com-multiplos-estados.png`, `12-historico--02-tabela-processos-finalizados.png`, `06-navegacao--05-historico.png`.

**Problema:** tabelas e listas concentram muita informação sem forte diferenciação visual de prioridade, tornando leitura e escaneabilidade mais lentas.

**Melhoria recomendada:**
- Destacar colunas-chave (status, prazo, unidade, responsável);
- Usar “chips”/badges consistentes para situação;
- Aumentar contraste entre cabeçalho, linhas e ações por linha;
- Incluir estado vazio orientado (o que fazer em seguida).

---

## 3.4 Fluxos por perfil estão funcionais, mas faltam pistas contextuais persistentes

**Evidências:** `02-painel--10-painel-gestor.png`, `02-painel--11-painel-chefe.png`, `04-subprocesso--01-dashboard-subprocesso.png`, `09-operacoes-bloco--01-detalhes-processo-gestor.png`.

**Problema:** quando o usuário alterna entre áreas e perfis, faltam sinais mais explícitos sobre “onde estou no fluxo” e “qual meu próximo passo”.

**Melhoria recomendada:**
- Cabeçalho contextual por etapa (ex.: “Cadastro de Atividades”, “Mapa de Competências”, “Homologação”);
- Indicador de etapa/situação do subprocesso no topo;
- Mensagem de próxima ação recomendada conforme perfil (ADMIN/GESTOR/CHEFE).

---

## 3.5 Navegação lateral e consistência de layout podem evoluir

**Evidências:** `06-navegacao--01-menu-principal.png`, `06-navegacao--05a-barra-lateral.png`, `06-navegacao--03-unidades.png`, `06-navegacao--04-relatorios.png`.

**Problema:** a percepção de unidade visual entre módulos poderia ser maior (especialmente espaçamento, títulos de página e bloco de ações locais).

**Melhoria recomendada:**
- Definir um “template de página” único:
  - título + subtítulo/descrição curta;
  - barra de ações no mesmo local;
  - área de conteúdo com grid e espaçamentos fixos.

---

## 3.6 Responsividade: cobertura existe, mas precisa de regras formais de adaptação

**Evidências:** `08-responsividade--01-desktop-1920x1080.png` a `08-responsividade--04-mobile-375x667.png`.

**Problema:** o sistema já é capturado em múltiplas resoluções, mas sem um guia formal pode ocorrer quebra de consistência em ações, tabelas e modais no mobile.

**Melhoria recomendada:**
- Definir breakpoints oficiais e comportamento por componente:
  - tabelas com colunas prioritárias;
  - ações secundárias em menu “mais”;
  - modais com altura máxima e scroll interno.

---

## 4) Padrões recomendados (guia de padronização)

## 4.1 Botões e ações
- **Primário:** 1 por tela/modal (ação principal).
- **Secundário:** cancelar/voltar.
- **Perigo:** ações irreversíveis com confirmação reforçada.
- Ordem fixa no rodapé de modal: `Cancelar` → `Confirmar`.

## 4.2 Formulários
- Label sempre visível, obrigatório com `*` e texto de ajuda quando necessário.
- Estados obrigatórios: padrão, foco, erro, sucesso, desabilitado.
- Validação inline + resumo de erros no topo para formulários extensos.

## 4.3 Tabelas e listas
- Coluna de status com badge padronizado.
- Coluna de ações sempre na mesma posição.
- Estado vazio com CTA (“Criar processo”, “Adicionar atividade”, etc.).

## 4.4 Modais
- Título claro + contexto (“Você está finalizando o processo X”).
- Conteúdo curto e objetivo.
- Botões com semântica visual consistente em todos os módulos.

## 4.5 Feedback do sistema
- Mensagens de sucesso/erro com texto orientado à ação.
- Sempre indicar próximo passo após operações chave (disponibilizar, homologar, finalizar).

---

## 5) Priorização de melhorias

## Prioridade alta (impacto imediato)
1. Padronizar rodapé e semântica de botões em modais críticos.
2. Unificar padrão de validação inline em todos os formulários.
3. Melhorar legibilidade de tabelas (status, contraste e ações).

## Prioridade média
1. Adicionar cabeçalho contextual por etapa/perfil.
2. Padronizar layout base das páginas de módulos.
3. Fortalecer estado vazio com CTA orientado.

## Prioridade estrutural
1. Criar design tokens de espaçamento, tipografia, cores e estados.
2. Definir regras oficiais de responsividade por componente.
3. Manter suíte de captura como auditoria visual recorrente.

---

## 6) Próximos passos sugeridos

1. Criar um checklist de UX por PR (botões, validação, estado vazio, feedback, responsividade).
2. Implementar primeiro nas áreas de maior uso: Painel, Processo e Subprocesso.
3. Reexecutar `npm run test:e2e:captura` após cada ciclo de melhoria para comparar evolução visual.

