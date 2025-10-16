# Diretório `guias`

Este diretório centraliza a documentação e os guias de desenvolvimento do projeto frontend. O objetivo é fornecer um conjunto de diretrizes, padrões e boas práticas para garantir a consistência, qualidade e manutenibilidade do código.

Consultar estes guias é fundamental antes de iniciar novas tarefas de desenvolvimento ou refatoração.

## Conteúdo

Abaixo está um resumo de cada guia disponível:

### `guia-endpoints.md`

Descreve o padrão para a definição e consumo de endpoints da API no frontend. Detalha como as chamadas de API devem ser estruturadas, onde a lógica de acesso a dados deve residir e como os erros de comunicação com o backend devem ser tratados.

### `guia-perfis.md`

Explica o sistema de perfis de usuário (`CHEFE`, `GESTOR`, `SERVIDOR`, etc.) e como o controle de acesso baseado em papéis (RBAC - Role-Based Access Control) é implementado no frontend. Orienta sobre como exibir/ocultar componentes e rotas com base nas permissões do usuário logado.

### `guia-refatoracao.md`

Fornece diretrizes sobre como e quando refatorar o código. Inclui exemplos de "code smells" (sinais de código problemático) comuns no contexto do Vue e TypeScript e sugere as melhores abordagens para corrigi-los, promovendo um código mais limpo e eficiente.

### `guia-testes-e2e.md`

Um guia detalhado sobre como escrever e estruturar testes _end-to-end_ (E2E) com Playwright. Cobre a organização dos arquivos de teste, o uso de comandos customizados, a criação de dados de teste (massa de dados) e as melhores práticas para criar testes robustos e fáceis de manter.

### `guia-testes-vitest.md`

Focado nos testes unitários e de componentes com Vitest. O guia explica como testar componentes Vue, funções "composables", stores Pinia e utilitários. Inclui exemplos de como mockar dependências, simular interações do usuário e fazer asserções eficazes.