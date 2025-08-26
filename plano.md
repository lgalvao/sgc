# Plano de Desenvolvimento do Protótipo SGC

## Estado Atual

O protótipo está em um estágio bastante avançado, com a maioria das funcionalidades principais já implementadas. O sistema permite simular todo o ciclo de vida de um processo de mapeamento de competências, desde a criação até a finalização.

### Funcionalidades Implementadas

1. **Autenticação e Perfil**
   - Tela de login funcional com seleção de perfil/unidade
   - Sistema de perfis (ADMIN, GESTOR, CHEFE, SERVIDOR) implementado
   - Persistência de sessão via localStorage

2. **Painel e Navegação**
   - Painel com listagem de processos e alertas
   - Sistema de navegação completo com breadcrumbs
   - Barra de navegação funcional

3. **Processos**
   - Criação de processos (CDU-03)
   - Início de processos de mapeamento (CDU-04) e revisão (CDU-05)
   - Detalhamento de processos (CDU-07A) e subprocessos (CDU-07B)
   - Finalização de processos (CDU-27)

4. **Atividades e Conhecimentos**
   - Cadastro de atividades e conhecimentos (CDU-08)
   - Disponibilização do cadastro (CDU-09)
   - Visualização de atividades e conhecimentos (CDU-11)
   - Importação de atividades de outros processos

5. **Mapas de Competências**
   - Criação e edição de mapas de competências (CDU-15)
   - Disponibilização de mapas (CDU-17)
   - Visualização de mapas (CDU-19)
   - Validação de mapas (CDU-20)

6. **Atribuições Temporárias**
   - Criação de atribuições temporárias (CDU-29)
   - Integração com o sistema de perfis

7. **Histórico de Processos**
   - Consulta a processos finalizados (CDU-28)

8. **Configurações**
   - Sistema de configurações (CDU-32)

## Funcionalidades Pendentes ou Parcialmente Implementadas

### 1. Análise de Cadastro (CDU-13)
**Status:** Parcialmente implementado
**O que falta:**
- [ ] Implementar lógica completa de aceitação/homologação
- [ ] Implementar funcionalidade de devolução para ajustes
- [ ] Implementar histórico de análises
- [ ] Adicionar notificações por e-mail (simuladas)
- [ ] Criar alertas automáticos

### 2. Aceitar/Homologar Cadastro em Bloco (CDU-14)
**Status:** Não implementado
**O que falta:**
- [ ] Criar funcionalidade para aceitar/homologar múltiplos cadastros de uma vez
- [ ] Implementar interface para seleção em lote
- [ ] Adicionar validações para processos em bloco

### 3. Processos de Revisão
**Status:** Parcialmente implementado
**O que falta:**
- [ ] Completar implementação do CDU-12 (Verificar impactos no mapa)
- [ ] Implementar CDU-16 (Ajustar mapa de competências)
- [ ] Finalizar CDU-10 (Disponibilizar revisão do cadastro)
- [ ] Completar fluxo completo de revisão

### 4. Processos de Diagnóstico (CDU-06)
**Status:** Em definição
**O que falta:**
- [ ] Definir requisitos completos
- [ ] Implementar funcionalidades específicas
- [ ] Criar telas e componentes necessários

### 5. Aceitar/Homologar Mapa de Competências em Bloco (CDU-22)
**Status:** Não implementado
**O que falta:**
- [ ] Criar funcionalidade para aceitar/homologar múltiplos mapas de uma vez
- [ ] Implementar interface para seleção em lote
- [ ] Adicionar validações para processos em bloco

### 6. Reabertura de Cadastro (CDU-23) e Revisão (CDU-24)
**Status:** Não implementado
**O que falta:**
- [ ] Implementar funcionalidade de reabertura
- [ ] Adicionar notificações para unidades superiores
- [ ] Implementar fluxo de nova análise

### 7. Alteração de Data Limite (CDU-25)
**Status:** Parcialmente implementado
**O que falta:**
- [ ] Implementar interface para alteração de datas
- [ ] Adicionar notificações automáticas

### 8. Envio de Lembretes (CDU-26)
**Status:** Não implementado
**O que falta:**
- [ ] Criar sistema de indicadores de prazo
- [ ] Implementar funcionalidade de envio de lembretes

### 9. Manter Administradores (CDU-31)
**Status:** Não implementado
**O que falta:**
- [ ] Criar interface para gestão de administradores
- [ ] Implementar funcionalidades de CRUD para administradores

### 10. Vincular Unidades (CDU-30)
**Status:** Não implementado
**O que falta:**
- [ ] Criar funcionalidade de vinculação entre unidades
- [ ] Implementar mecanismo de reaproveitamento de informações

### 11. Gerar Relatórios (CDU-33)
**Status:** Não implementado
**O que falta:**
- [ ] Criar tela de relatórios
- [ ] Implementar diferentes tipos de relatórios
- [ ] Adicionar filtros e opções de exportação

## Priorização das Tarefas

### Fase 1: Finalização dos Fluxos Principais
1. Completar Análise de Cadastro (CDU-13)
2. Finalizar Processos de Revisão
3. Implementar Aceitar/Homologar em Bloco (CDU-14 e CDU-22)

### Fase 2: Funcionalidades Secundárias
1. Implementar Reabertura de Cadastro/Revisão (CDU-23 e CDU-24)
2. Completar Alteração de Data Limite (CDU-25)
3. Implementar Envio de Lembretes (CDU-26)

### Fase 3: Funcionalidades Administrativas
1. Implementar Manter Administradores (CDU-31)
2. Implementar Vincular Unidades (CDU-30)
3. Implementar Gerar Relatórios (CDU-33)

### Fase 4: Processos de Diagnóstico
1. Definir e implementar Processos de Diagnóstico (CDU-06)

## Considerações Finais

O protótipo já possui uma base sólida e funcional. A maior parte do trabalho restante consiste em:
1. Finalizar os fluxos de análise e aprovação
2. Implementar funcionalidades em bloco para maior eficiência
3. Completar os processos de revisão
4. Adicionar funcionalidades administrativas

Com a implementação dessas funcionalidades pendentes, o protótipo estará completo conforme as especificações do documento CDU.