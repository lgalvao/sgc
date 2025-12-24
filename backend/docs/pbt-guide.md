# Property-Based Testing (PBT) no Projeto SGC

Este projeto utiliza a biblioteca **jqwik** para testes baseados em propriedades. Estes testes geram múltiplos cenários aleatórios para verificar invariantes e lógica complexa.

## Como Executar

Os testes de propriedade são executados separadamente dos testes unitários padrão para não impactar o tempo de build rápido.

### Executar Apenas PBT
Para rodar todos os testes de propriedade:
```bash
./gradlew propertyTest
```

### Executar Testes Unitários Padrão
O comando padrão `test` exclui os testes de propriedade:
```bash
./gradlew test
```

### Executar Tudo
Para garantir que tudo está passando (CI/CD):
```bash
./gradlew check
```

## Estrutura
- Os testes PBT devem ser anotados com `@Tag("pbt")`.
- Eles estão localizados junto com os testes unitários, mas seguem a convenção de nomenclatura `*PropertyTest.java`.

## Exemplo
Veja `ImpactoMapaServicePropertyTest.java` para um exemplo de geração de cenários complexos e verificação de invariantes de negócio.

## Candidatos Futuros para PBT
Abaixo estão áreas identificadas que se beneficiariam de testes baseados em propriedades:

1. **Calculadoras de Pontuação/Avaliação**: Lógica matemática pura em serviços de avaliação de desempenho.
2. **Parsers e Mappers**: `SubprocessoMapper` ou outros mappers complexos (verificar se `entity -> dto -> entity` preserva os dados).
3. **Regras de Elegibilidade de Unidade**: `UnidadeService.montarHierarquiaComElegibilidade` envolve lógica de árvore e estado.
4. **Filtros de Segurança**: Validar que combinações complexas de permissões sempre resultam no acesso correto (negado/permitido).
