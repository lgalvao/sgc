# Plano de Refatoração: Eliminar Separação Interface/Implementação

O objetivo desta refatoração é simplificar a base de código, eliminando a separação entre interfaces de serviço e suas implementações (`Impl`). A abstração fornecida pelas interfaces não está sendo aproveitada (não há múltiplas implementações) e a duplicação de arquivos aumenta a complexidade.

## Arquivos-Alvo

A refatoração será aplicada aos seguintes pares de interface/implementação:

1.  `sgc.alerta.AlertaService` / `AlertaServiceImpl` (Concluído)
2.  `sgc.analise.AnaliseCadastroService` / `AnaliseCadastroServiceImpl` (Concluído)
3.  `sgc.analise.AnaliseValidacaoService` / `AnaliseValidacaoServiceImpl` (Concluído)
4.  `sgc.mapa.CopiaMapaService` / `CopiaMapaServiceImpl` (Concluído)
5.  `sgc.mapa.ImpactoMapaService` / `ImpactoMapaServiceImpl` (Concluído)
6.  `sgc.mapa.MapaService` / `MapaServiceImpl` (Concluído)
7.  `sgc.sgrh.SgrhService` / `SgrhServiceImpl` (Concluído)

**Nota:** Arquivos na pasta `generated` serão ignorados, pois são gerados automaticamente.

## Processo de Refatoração (Para cada par)

1.  **Mesclar Conteúdo**: Copiar todo o conteúdo da classe `...ServiceImpl.java` (incluindo anotações, campos e métodos) para o arquivo da interface `...Service.java`.
2.  **Transformar em Classe**: No arquivo `...Service.java`, alterar a declaração de `public interface ...Service` para `public class ...Service`.
3.  **Remover Cláusula `implements`**: Remover a cláusula `implements ...Service` da declaração da classe.
4.  **Ajustar Imports**: Garantir que todos os `import` necessários da classe `Impl` estejam presentes no novo arquivo de classe de serviço.
5.  **Excluir Arquivo `Impl`**: Apagar o arquivo `...ServiceImpl.java` que se tornou redundante.
6.  **Atualizar Referências**: Usar `grep` e `sed` (ou edição manual) para encontrar e substituir todas as importações e injeções de dependência que usavam a interface (`@Autowired AlertaService`) para usar a nova classe (`@Autowired AlertaService`). Como o nome do arquivo da interface foi mantido, a maioria das referências deve funcionar sem alterações, mas é preciso verificar.
7.  **Verificar Testes**: Compilar e rodar os testes relacionados para garantir que a injeção de dependência e a funcionalidade continuam corretas.

## Ordem de Execução

A refatoração será executada na seguinte ordem:

1.  Refatorar `AlertaService`.
2.  Refatorar `AnaliseCadastroService`.
3.  Refatorar `AnaliseValidacaoService`.
4.  Refatorar `CopiaMapaService`.
5.  Refatorar `ImpactoMapaService`.
6.  Refatorar `MapaService`.
7.  Refatorar `SgrhService`.
8.  Rodar a build completa e todos os testes para garantir a integridade do sistema.