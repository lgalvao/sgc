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

## 4. Plano de Adoção Detalhado

Propomos uma adoção gradual e controlada para minimizar riscos e permitir que a equipe se familiarize com a nova biblioteca.

### Passo 1: Instalação e Configuração
1.  **Adicionar a Dependência:**
    ```bash
    cd frontend
    npm install bootstrap-vue-next
    ```
2.  **Configurar no `main.ts`:**
    - Atualize o `main.ts` para importar e registrar o `plugin` da biblioteca. Também é importante remover as importações diretas do `CSS` e `JS` do Bootstrap, pois a `BootstrapVueNext` gerencia isso.

    ```typescript
    // frontend/src/main.ts
    import { createApp } from "vue";
    import App from "./App.vue";
    import router from "./router/index";
    import { createPinia } from "pinia";
    import { createBootstrap } from 'bootstrap-vue-next';

    // Importar o CSS da BootstrapVueNext
    import 'bootstrap-vue-next/dist/bootstrap-vue-next.css';
    import 'bootstrap/dist/css/bootstrap.min.css'; // Opcional, se houver estilos customizados
    import "bootstrap-icons/font/bootstrap-icons.css";

    const app = createApp(App);
    const pinia = createPinia();
    app.use(pinia);
    app.use(router);

    // Registrar o plugin do BootstrapVueNext
    app.use(createBootstrap());

    app.mount('#app');
    ```

### Passo 2: Projeto Piloto (Prova de Conceito)
1.  **Escolher um Componente de Baixo Risco:**
    - Selecione um componente que seja representativo, mas não crítico, para a primeira migração. Um bom candidato seria um dos modais mais simples, como `CriarCompetenciaModal.vue`.
2.  **Refatorar o Componente:**
    - Substitua o `HTML` e a lógica manual pelo componente `BModal` da biblioteca.
    - Mapeie as `props` e eventos existentes para a nova API do componente.
    - Verifique se o comportamento e o estilo permanecem consistentes.
3.  **Atualizar Testes:**
    - Ajuste os testes de unidade (`Vitest`) e `E2E` (`Playwright`) para refletir a nova estrutura do DOM.
4.  **Documentar o Processo:**
    - Crie um guia interno simples com as lições aprendidas, padrões de código e dicas para a migração dos demais componentes.

### Passo 3: Migração Gradual
1.  **Priorizar Componentes:**
    - Crie uma lista de todos os componentes a serem migrados e priorize-os com base na complexidade e no impacto. Sugestão de ordem:
        1.  Modais (`*Modal.vue`)
        2.  Tabelas (`Tabela*.vue`)
        3.  Formulários e componentes de layout.
2.  **Trabalho em Paralelo:**
    - A migração pode ser dividida em `tasks` menores e distribuída entre os membros da equipe.
    - Novos componentes devem, obrigatoriamente, ser criados com a `BootstrapVueNext`.
3.  **Revisão de Código (PRs):**
    - Realize revisões de código cuidadosas para garantir que a nova biblioteca está sendo usada corretamente e que não há regressões.

### Passo 4: Limpeza (Cleanup)
1.  **Remover Código Legado:**
    - Após a migração de todos os componentes, remova qualquer código de suporte ou `helpers` que se tornaram obsoletos.
2.  **Atualizar Documentação:**
    - Atualize o `README.md` do frontend e outros guias internos para refletir o novo `stack` de tecnologia.

## 5. Conclusão

A adoção da `BootstrapVueNext` representa um investimento inicial em tempo de refatoração, mas com um retorno claro em produtividade, qualidade e manutenibilidade a longo prazo. O plano de adoção gradual permite mitigar os riscos e garantir uma transição suave, alinhando o frontend do SGC com as melhores práticas do ecossistema Vue 3.
