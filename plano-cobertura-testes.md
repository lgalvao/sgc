# Plano para Aumentar a Cobertura de Testes para 90%

## 1. Visão Geral e Meta

O objetivo deste plano é aumentar a cobertura de testes de linha (`Line Coverage`) do projeto de **88.4%** para **90%**. Com os últimos avanços, a meta está muito próxima.

## 2. Progresso Realizado

Os seguintes pacotes foram abordados com sucesso, resultando em aumentos significativos de cobertura:

- **`sgc.mapa`**: Cobertura aumentada de 81.2% para **91.1%**.
- **`sgc.processo`**: Cobertura aumentada de 76.3% para **92.6%**.
- **`sgc.alerta`**: Cobertura aumentada de 9% para **71.9%**.
- **`sgc.processo.dto`**: Cobertura aumentada de 6.5% para **100%**.
- **`sgc.notificacao`**: Cobertura aumentada de 51.2% para **72%**.
- **`sgc.comum.erros`**: Cobertura aumentada de 60% para **100%**.

## 3. Próximos Passos

Com a cobertura geral em **88.4%**, a estratégia é focar nos pacotes com menor cobertura para dar o impulso final e atingir os 90%.

### 3.1. Pacotes Prioritários Restantes

| Prioridade | Pacote         | Cobertura de Linha Atual | Meta de Cobertura |
| :--------- | :------------- | :----------------------- | :---------------- |
| 1          | `sgc.alerta`   | 71.9%                    | > 85%             |
| 2          | `sgc.notificacao`| 72.0%                    | > 85%             |


### 3.2. Plano de Ação Detalhado para `sgc.alerta` (Prioridade Atual)

Este pacote tem uma das menores coberturas e será o foco para atingirmos a meta.

**Passos:**
1.  **Análise:** Inspecionar as classes de serviço e repositórios para identificar a lógica de negócio e os branches não cobertos.
2.  **Criação de Testes:** Criar novos testes para cobrir cenários de falha, casos de borda e diferentes caminhos lógicos.
3.  **Validação:** Executar `gradle :backend:agentTest` para confirmar o aumento da cobertura.

### 3.3. Histórico de Planos Concluídos

- **`sgc.mapa` (Concluído):** A cobertura foi aumentada de 81.2% para 91.1% com a criação de testes para `MapaControle` e `CopiaMapaServiceImpl`.
- **`sgc.processo` (Concluído):** A cobertura foi aumentada de 76.3% para 92.6% através da adição de testes para os métodos `iniciarProcessoRevisao` e `obterDetalhes`.

## 4. Conclusão

Ao executar os planos de ação para os pacotes `sgc.alerta` e `sgc.notificacao`, a cobertura geral de testes do projeto deverá superar a meta de **90%**, garantindo maior robustez e confiança na base de código.
