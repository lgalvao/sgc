# Diretório de Utilitários (Utils)

Contém funções puras e auxiliares que não dependem do estado do Vue ou de componentes específicos.

## Utilitários Disponíveis

* **`apiUtils.ts`**: Helpers para manipulação de URLs, parâmetros de consulta e tratamento de respostas brutas.
* **`apiError.ts`**: Definições de classes de erro customizadas para falhas de rede e API.
* **`dateUtils.ts`**: Formatação e manipulação de datas usando padrões brasileiros e ISO.
* **`treeUtils.ts`**: Algoritmos para manipulação de estruturas hierárquicas (unidades, árvores).
* **`validators.ts`**: Funções de validação para formulários (CPF, e-mail, campos obrigatórios).
* **`styleUtils.ts`**: Helpers para manipulação dinâmica de classes CSS e estilos.
* **`logger.ts`**: Abstração de log para facilitar o rastreamento em desenvolvimento e produção.
* **`csv.ts`**: Utilitários para exportação e importação de arquivos CSV.
* **`index.ts`**: Ponto de exportação para utilitários comuns.

## Princípios

1. **Pureza**: As funções devem ser preferencialmente puras (mesma entrada sempre gera mesma saída).
2. **Sem Efeitos Colaterais**: Evite manipular o DOM ou estado global diretamente aqui.
3. **Testabilidade**: Devem ser facilmente testáveis de forma isolada.
