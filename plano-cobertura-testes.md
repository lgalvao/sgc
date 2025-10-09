# Plano para Aumentar a Cobertura de Testes para 90%

## 1. Visão Geral e Meta

O objetivo deste plano é aumentar a cobertura de testes de linha (`Line Coverage`) do projeto de **84.6%** para **90%**. O progresso contínuo tem sido positivo, e estamos focando nos pacotes restantes com menor cobertura para atingir a meta.

## 2. Progresso Realizado

Os seguintes pacotes foram abordados com sucesso, resultando em aumentos significativos de cobertura:

- **`sgc.alerta`**: Cobertura aumentada de 9% para **71.9%**.
- **`sgc.processo.dto`**: Cobertura aumentada de 6.5% para **100%**.
- **`sgc.notificacao`**: Cobertura aumentada de 51.2% para **72%**.
- **`sgc.comum.erros`**: Cobertura aumentada de 60% para **100%** (verificado implicitamente pela execução dos testes).

## 3. Próximos Passos

Com a cobertura geral em **84.6%**, a meta de 90% está próxima. A estratégia continua a ser focar nos pacotes com menor cobertura.

### 3.1. Pacotes Prioritários Restantes

| Prioridade | Pacote         | Cobertura de Linha Atual | Meta de Cobertura |
| :--------- | :------------- | :----------------------- | :---------------- |
| 1          | `sgc.processo` | 76.3%                    | > 90%             |
| 2          | `sgc.mapa`     | 81.2%                    | > 90%             |


### 3.2. Plano de Ação Detalhado para `sgc.processo`

Este é o pacote com a menor cobertura restante e, portanto, a prioridade máxima.

**Passos:**
1.  **Análise:** Inspecionar as classes de serviço (`ProcessoService`, `ProcessoControle`) e repositórios para identificar a lógica de negócio e os branches não cobertos pelos testes existentes.
2.  **Criação de Testes:** Melhorar os testes existentes e criar novos para cobrir cenários de falha, casos de borda e diferentes caminhos lógicos que atualmente não são exercitados.
3.  **Validação:** Executar `gradle :backend:agentTest` para confirmar o aumento da cobertura.