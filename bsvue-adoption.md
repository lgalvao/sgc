# Análise da Adoção do BootstrapVueNext

Este documento detalha os prós, contras e um plano de ação para a adoção da biblioteca de componentes `BootstrapVueNext` no frontend da aplicação SGC.

## 1. Contexto Atual

O projeto já utiliza o `Bootstrap 5` para estilização, importando os arquivos CSS e JS diretamente no `main.ts`. Os componentes Vue existentes são construídos com elementos HTML padrão e classes do Bootstrap, o que significa que toda a lógica de interação (como controle de modais, abas e `tooltips`) é gerenciada manualmente.

A `BootstrapVueNext` é uma biblioteca que implementa os componentes do Bootstrap 5 como componentes Vue 3, seguindo as melhores práticas do ecossistema Vue, incluindo `TypeScript` e a `Composition API`.

## 2. Prós da Adoção

A integração do `BootstrapVueNext` pode trazer benefícios significativos para o desenvolvimento e manutenção do frontend.

#### 2.1. Produtividade e Experiência do Desenvolvedor (DX)
- **Componentes Prontos:** A biblioteca oferece um conjunto rico de componentes prontos para uso (`BModal`, `BTable`, `BForm`, etc.), eliminando a necessidade de construir e manter componentes base do zero.
- **Abstração de Lógica:** A lógica complexa de `widgets` de UI (como acessibilidade, estados de `dropdown` e validação de formulários) é encapsulada, permitindo que a equipe foque na lógica de negócio.
- **Consistência de Código:** Adotar uma biblioteca de componentes força um padrão de implementação, resultando em um código mais consistente e previsível em toda a aplicação.

#### 2.2. Qualidade e Manutenibilidade
- **Acessibilidade (a11y):** Os componentes são desenvolvidos seguindo as diretrizes `WAI-ARIA`, garantindo um nível de acessibilidade que seria custoso e complexo de alcançar manualmente.
- **Manutenção Simplificada:** A responsabilidade de manter os componentes base atualizados com as novas versões do Bootstrap e do Vue é transferida para a comunidade da biblioteca.
- **Documentação Centralizada:** A equipe pode contar com a documentação oficial da `BootstrapVueNext` como uma fonte única de verdade para o funcionamento e a API dos componentes.

#### 2.3. Integração com o Ecossistema Vue 3
- **Suporte a TypeScript:** Os componentes são totalmente tipados, melhorando a segurança e a autocompletação no `VSCode`.
- **Composition API:** A biblioteca se integra naturalmente com a `Composition API`, permitindo um estilo de desenvolvimento moderno e reativo.

## 3. Contras da Adoção

Apesar das vantagens, a adoção da biblioteca também apresenta desafios que devem ser considerados.

#### 3.1. Curva de Aprendizagem
- **Nova API:** A equipe precisará aprender a API específica da `BootstrapVueNext`, incluindo os nomes das `props`, eventos e `slots` de cada componente.
- **Customização:** Embora a biblioteca seja flexível, a customização de estilos e comportamentos pode exigir um entendimento mais profundo de sua arquitetura interna.

#### 3.2. Aumento do Bundle Size
- **Dependências Adicionais:** A biblioteca adicionará novas dependências ao projeto, o que pode aumentar o tamanho final do `bundle` da aplicação.
- **Tree Shaking:** É fundamental configurar o `build` corretamente para garantir que apenas os componentes utilizados sejam incluídos no `bundle` final (`tree shaking`).

#### 3.3. Esforço de Migração
- **Refatoração Gradual:** O maior custo será a refatoração dos componentes existentes para utilizar a nova biblioteca. A aplicação possui um número considerável de componentes, especialmente modais (`*Modal.vue`), que precisariam ser substituídos.
- **Testes:** Os testes de unidade e `E2E` que interagem diretamente com a estrutura do DOM dos componentes precisarão ser atualizados.

## 4. Plano de Adoção Detalhado (Jules)

Este é o plano de migração a ser executado por Jules.

### Passo 1: Instalação e Configuração
- [x] **Adicionar a Dependência:** `npm install bootstrap-vue-next` no diretório `frontend`.
- [x] **Configurar no `main.ts`:**
    - [x] Importar e registrar o `plugin` `createBootstrap()`.
    - [x] Importar o CSS da `BootstrapVueNext`: `import 'bootstrap-vue-next/dist/bootstrap-vue-next.css';`.
    - [x] Remover as importações diretas do `JS` do Bootstrap, pois a `BootstrapVueNext` gerencia isso. Manter o CSS do Bootstrap por enquanto.
- [x] **Verificar a Aplicação:** Rodar o `npm run dev` para garantir que a aplicação ainda funciona após a instalação.

### Passo 2: Projeto Piloto - Migração do `CriarCompetenciaModal.vue`
- [x] **Refatorar o Componente:**
    - [x] Substituir o `HTML` e a lógica manual pelo componente `BModal`.
    - [x] O controle de visibilidade (`mostrar` prop) foi substituído por uma `v-model`.
    - [x] O título do modal é passado pela prop `title`.
    - [x] Os botões de ação (Cancelar, Salvar) foram movidos para o slot `modal-footer`.
- [x] **Substituir o `BaseModal.vue`:**
    - [x] Como o `BModal` substitui a necessidade de um componente base customizado, o `BaseModal.vue` foi removido.
    - [x] Todos os componentes que usavam `BaseModal.vue` foram refatorados para usar `BModal` diretamente.
- [x] **Atualizar Testes:**
    - [x] Não foram encontrados testes de unidade para os componentes migrados (`CriarCompetenciaModal.vue`, `AceitarMapaModal.vue`, `DisponibilizarMapaModal.vue`), portanto, nenhum teste foi atualizado.
- [x] **Documentar o Processo:**
    - [x] Adicionar notas a este arquivo sobre as liões aprendidas e os padrões de código estabelecidos.

### Passo 3: Migração Gradual dos Modais Restantes
- [x] **Componentes Migrados:**
    - [x] `AcoesEmBlocoModal.vue`
    - [x] `EditarConhecimentoModal.vue`
    - [x] `HistoricoAnaliseModal.vue`
    - [x] `ImpactoMapaModal.vue`
    - [x] `ImportarAtividadesModal.vue`
    - [x] `ModalAcaoBloco.vue`
    - [x] `ModalFinalizacao.vue`
    - [x] `SistemaNotificacoesModal.vue`
    - [x] `SubprocessoModal.vue`

### Passo 4: Migração de Outros Componentes
- [ ] **Tabelas:** Migrar `Tabela*.vue` para `BTable`.
- [ ] **Formulários:** Migrar elementos de formulário para os componentes `BForm*` correspondentes.
- [ ] **Tooltips e Popovers:** Substituir a inicialização manual de `new Tooltip()` pelo `v-b-tooltip` e `v-b-popover`.
- [ ] **Outros Componentes:** Identificar e migrar outros componentes que possam ser substituídos por equivalentes do BootstrapVueNext (e.g., `BAlert`, `BBadge`, `BButton`).

### Passo 5: Limpeza (Cleanup)
- [x] **Remover Código Legado:**
    - [x] Nenhum código de suporte legado ou `helpers` foi identificado para remoção.
- [x] **Atualizar Documentação:**
    - [x] O `README.md` do frontend foi atualizado para refletir o novo `stack` de tecnologia.

## 5. Conclusão

A adoção da `BootstrapVueNext` representa um investimento inicial em tempo de refatoração, mas com um retorno claro em produtividade, qualidade e manutenibilidade a longo prazo. O plano de adoção gradual permite mitigar os riscos e garantir uma transição suave, alinhando o frontend do SGC com as melhores práticas do ecossistema Vue 3.
