## Vulnerability: Logging de Informações Sensíveis (Senha)
**Severity:** High
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\UsuarioService.java
**Line Content:** `log.info("Simulando autenticação para: {}/{}", tituloEleitoral, senha);`
**Description:** O método `autenticar` registra a `senha` (senha) em texto simples. O registro de informações confidenciais como senhas pode levar à exposição se os logs forem comprometidos.
**Recommendation:** Remova a `senha` da instrução de log. Em um aplicativo real, as senhas nunca devem ser registradas, mesmo durante o desenvolvimento ou teste. Se a depuração for necessária, considere registrar um hash ou uma versão mascarada da senha, mas, idealmente, evite registrar senhas por completo.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII)
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\UsuarioService.java
**Line Content:** `log.info("Buscando autorizações (perfis e unidades) para o título: {}", tituloEleitoral);`
**Description:** O método `autorizar` registra o `tituloEleitoral`. O registro desnecessário de PII pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash do `tituloEleitoral` ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) na Entrada do Usuário
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\UsuarioService.java
**Line Content:** `log.info("Usuário com título {} entrou com sucesso. Perfil: {}, Unidade: {}",
            tituloEleitoral, pu.getPerfil(), pu.getSiglaUnidade());`
**Description:** O método `entrar` registra `tituloEleitoral`, `pu.getPerfil()` e `pu.getSiglaUnidade()`. O registro desnecessário de PII pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash do PII ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) em SgrhService
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\SgrhService.java
**Line Content:** `log.warn("MOCK SGRH: Buscando usuário por título: {}", titulo);`
**Description:** O método `buscarUsuarioPorTitulo` registra o `titulo` (provavelmente um número de CPF/título). O registro desnecessário de PII pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash do `titulo` ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações Potencialmente Sensíveis (Código da Unidade) em SgrhService
**Severity:** Low
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\SgrhService.java
**Line Content:** `log.warn("MOCK SGRH: Buscando unidade por código: {}", codigo);`
**Description:** O método `buscarUnidadePorCodigo` registra o `codigo` da unidade. Dependendo da natureza desses códigos, isso pode expor a estrutura organizacional ou identificadores sensíveis.
**Recommendation:** Evite registrar informações sensíveis diretamente. Se o registro for necessário, considere mascarar o `codigo` ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações Potencialmente Sensíveis (Código da Unidade Pai) em SgrhService
**Severity:** Low
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\SgrhService.java
**Line Content:** `log.warn("MOCK SGRH: Buscando subunidades de: {}", codigoPai);`
**Description:** O método `buscarSubunidades` registra o `codigoPai` da unidade. Dependendo da natureza desses códigos, isso pode expor a estrutura organizacional ou identificadores sensíveis.
**Recommendation:** Evite registrar informações sensíveis diretamente. Se o registro for necessário, considere mascarar o `codigoPai` ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações Potencialmente Sensíveis (Código da Unidade) em SgrhService
**Severity:** Low
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\SgrhService.java
**Line Content:** `log.warn("MOCK SGRH: Buscando responsável da unidade: {}", unidadeCodigo);`
**Description:** O método `buscarResponsavelUnidade` registra o `unidadeCodigo`. Dependendo da natureza desses códigos, isso pode expor a estrutura organizacional ou identificadores sensíveis.
**Recommendation:** Evite registrar informações sensíveis diretamente. Se o registro for necessário, considere mascarar o `unidadeCodigo` ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) em SgrhService
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\SgrhService.java
**Line Content:** `log.warn("MOCK SGRH: Buscando unidades onde {} é responsável", titulo);`
**Description:** O método `buscarUnidadesOndeEhResponsavel` registra o `titulo` (provavelmente um número de CPF/título). O registro desnecessário de PII pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash do `titulo` ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) em SgrhService
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\SgrhService.java
**Line Content:** `log.warn("MOCK SGRH: Buscando perfis do usuário: {}", titulo);`
**Description:** O método `buscarPerfisUsuario` registra o `titulo` (provavelmente um número de CPF/título). O registro desnecessário de PII pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash do `titulo` ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) e Potencialmente Sensíveis em SgrhService
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\SgrhService.java
**Line Content:** `log.warn("MOCK SGRH: Verificando se {} tem perfil {} na unidade {}", titulo, perfil, unidadeCodigo);`
**Description:** O método `usuarioTemPerfil` registra `titulo`, `perfil` e `unidadeCodigo`. O registro desnecessário de PII e informações potencialmente sensíveis pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII e informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash dos dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) e Potencialmente Sensíveis em SgrhService
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\sgrh\SgrhService.java
**Line Content:** `log.warn("MOCK SGRH: Buscando unidades onde {} tem perfil {}", titulo, perfil);`
**Description:** O método `buscarUnidadesPorPerfil` registra `titulo` e `perfil`. O registro desnecessário de PII e informações potencialmente sensíveis pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII e informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash dos dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Tratamento Inseguro de Erros / Logging de Informações Sensíveis em EventoProcessoListener
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\notificacao\EventoProcessoListener.java
**Line Content:** `log.error("Erro ao enviar e-mail para o subprocesso {}: {}", subprocesso.getCodigo(), e.getMessage(), e);`
**Description:** O log de erro inclui `e.getMessage()`, que pode expor detalhes internos do sistema, rastreamentos de pilha ou outras informações sensíveis que podem ajudar um invasor a entender as vulnerabilidades do sistema. `subprocesso.getCodigo()` também pode ser sensível dependendo do contexto.
**Recommendation:** Evite registrar mensagens de exceção brutas diretamente em ambientes de produção. Em vez disso, registre uma mensagem de erro genérica e um ID de erro exclusivo, que pode ser usado para pesquisar os detalhes completos da exceção em um sistema de log interno seguro. Mascare ou sanitize qualquer informação sensível como `subprocesso.getCodigo()` se não for estritamente necessário para depuração.

## Vulnerability: Tratamento Inseguro de Erros / Logging de Informações Sensíveis em EventoProcessoListener
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\notificacao\EventoProcessoListener.java
**Line Content:** `log.error("Erro ao processar evento de processo iniciado: {}", e.getMessage(), e);`
**Description:** O log de erro inclui `e.getMessage()`, que pode expor detalhes internos do sistema, rastreamentos de pilha ou outras informações sensíveis que podem ajudar um invasor a entender as vulnerabilidades do sistema.
**Recommendation:** Evite registrar mensagens de exceção brutas diretamente em ambientes de produção. Em vez disso, registre uma mensagem de erro genérica e um ID de erro exclusivo, que pode ser usado para pesquisar os detalhes completos da exceção em um sistema de log interno seguro.

## Vulnerability: Logging de Informações Potencialmente Sensíveis (Nome e Código da Unidade) em EventoProcessoListener
**Severity:** Low
**Location:** C:\sgc\backend\src\main\java\sgc\notificacao\EventoProcessoListener.java
**Line Content:** `log.warn("Responsável não encontrado para a unidade {} ({})", unidade.nome(), codigoUnidade);`
**Description:** O método `enviarEmailDeProcessoIniciado` registra o nome (`unidade.nome()`) e o código (`codigoUnidade`) da unidade. O registro desnecessário de tais informações pode expor detalhes organizacionais internos.
**Recommendation:** Evite registrar informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar os dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) e Potencialmente Sensíveis em EventoProcessoListener
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\notificacao\EventoProcessoListener.java
**Line Content:** `log.warn("E-mail não encontrado para o titular {} da unidade {}", responsavelOpt.get().titularTitulo(), unidade.nome());`
**Description:** O método `enviarEmailDeProcessoIniciado` registra o `titularTitulo` (PII) e `unidade.nome()` (potencialmente sensível). O registro desnecessário de tais informações pode levar a preocupações com a privacidade e à exposição de detalhes organizacionais internos.
**Recommendation:** Evite registrar PII e informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar os dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrição e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) e Potencialmente Sensíveis em EventoProcessoListener
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\notificacao\EventoProcessoListener.java
**Line Content:** `log.info("E-mail enviado para a unidade {} ({}) - Destinatário: {} ({})",
                    unidade.sigla(), tipoUnidade, titular.nome(), titular.email());`
**Description:** O método `enviarEmailDeProcessoIniciado` registra `unidade.sigla()`, `tipoUnidade`, `titular.nome()` e `titular.email()`. O registro desnecessário de PII e informações potencialmente sensíveis pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII e informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash dos dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações de Identificação Pessoal (PII) e Potencialmente Sensíveis em EventoProcessoListener
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\notificacao\EventoProcessoListener.java
**Line Content:** `log.info("E-mail enviado para o substituto da unidade {} - Destinatário: {} ({})", nomeUnidade, substituto.nome(), substituto.email());`
**Description:** O método `enviarEmailParaSubstituto` registra `nomeUnidade`, `substituto.nome()` e `substituto.email()`. O registro desnecessário de PII e informações potencialmente sensíveis pode levar a preocupações com a privacidade e possíveis violações de dados se os logs forem comprometidos.
**Recommendation:** Evite registrar PII e informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar ou fazer hash dos dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Tratamento Inseguro de Erros / Logging de Informações Sensíveis em EventoProcessoListener
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\notificacao\EventoProcessoListener.java
**Line Content:** `log.warn("Erro ao enviar e-mail para o substituto da unidade {}: {}", nomeUnidade, e.getMessage());`
**Description:** O log de erro inclui `e.getMessage()`, que pode expor detalhes internos do sistema, rastreamentos de pilha ou outras informações sensíveis que podem ajudar um invasor a entender as vulnerabilidades do sistema. `nomeUnidade` também pode ser sensível dependendo do contexto.
**Recommendation:** Evite registrar mensagens de exceção brutas diretamente em ambientes de produção. Em vez disso, registre uma mensagem de erro genérica e um ID de erro exclusivo, que pode ser usado para pesquisar os detalhes completos da exceção em um sistema de log interno seguro. Mascare ou sanitize qualquer informação sensível como `nomeUnidade` se não for estritamente necessário para depuração.

## Vulnerability: Logging de Informações Potencialmente Sensíveis (Código da Unidade) em AlertaService
**Severity:** Low
**Location:** C:\sgc\backend\src\main\java\sgc\alerta\AlertaService.java
**Line Content:** `log.debug("Criando alerta tipo={}" para unidade={}", tipoAlerta, codUnidadeDestino);`
**Description:** O método `criarAlerta` registra o `codUnidadeDestino`. O registro desnecessário de tais informações pode expor detalhes organizacionais internos.
**Recommendation:** Evite registrar informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar os dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações Potencialmente Sensíveis (Sigla da Unidade) em AlertaService
**Severity:** Low
**Location:** C:\sgc\backend\src\main\java\sgc\alerta\AlertaService.java
**Line Content:** `log.info("Alerta criado: código={}, tipo={}, unidade={}",
                alertaSalvo.getCodigo(), tipoAlerta, unidadeDestino.getSigla());`
**Description:** O método `criarAlerta` registra a sigla da unidade de destino (`unidadeDestino.getSigla()`). O registro desnecessário de tais informações pode expor detalhes organizacionais internos.
**Recommendation:** Evite registrar informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar os dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Logging de Informações Potencialmente Sensíveis (Código da Unidade) em AlertaService
**Severity:** Low
**Location:** C:\sgc\backend\src\main\java\sgc\alerta\AlertaService.java
**Line Content:** `log.warn("Responsável não encontrado para a unidade {}", codUnidadeDestino);`
**Description:** O método `criarAlerta` registra o `codUnidadeDestino`. O registro desnecessário de tais informações pode expor detalhes organizacionais internos.
**Recommendation:** Evite registrar informações sensíveis diretamente. Se o registro for necessário para depuração ou auditoria, considere mascarar os dados sensíveis ou garantir que os logs sejam armazenados com segurança com acesso restrito e políticas de retenção.

## Vulnerability: Tratamento Inseguro de Erros / Logging de Informações Sensíveis em AlertaService
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\alerta\AlertaService.java
**Line Content:** `log.error("Erro ao buscar responsável da unidade {}: {}", codUnidadeDestino, e.getMessage(), e);`
**Description:** O log de erro inclui `e.getMessage()`, que pode expor detalhes internos do sistema, rastreamentos de pilha ou outras informações sensíveis que podem ajudar um invasor a entender as vulnerabilidades do sistema. `codUnidadeDestino` também pode ser sensível dependendo do contexto.
**Recommendation:** Evite registrar mensagens de exceção brutas diretamente em ambientes de produção. Em vez disso, registre uma mensagem de erro genérica e um ID de erro exclusivo, que pode ser usado para pesquisar os detalhes completos da exceção em um sistema de log interno seguro. Mascare ou sanitize qualquer informação sensível como `codUnidadeDestino` se não for estritamente necessário para depuração.

## Vulnerability: Potencial XSS Armazenado via Descrições de Alerta em AlertaService
**Severity:** Medium
**Location:** C:\sgc\backend\src\main\java\sgc\alerta\AlertaService.java
**Line Content:** `Alerta alerta = new Alerta();
alerta.setProcesso(processo);
alerta.setDataHora(LocalDateTime.now());
alerta.setUnidadeOrigem(null); // SEDOC não tem registro como unidade
alerta.setUnidadeDestino(unidadeDestino);
alerta.setDescricao(descricao);`
**Description:** O campo `descricao` dos objetos `Alerta` é construído usando várias entradas (`processo.getDescricao()`, `unidadeOrigem.getSigla()`, `motivo`). Se essas entradas não forem sanitizadas e a `descricao` for posteriormente renderizada em uma interface de usuário sem o escape adequado, isso poderá levar a XSS armazenado. A responsabilidade pela sanitização final recai sobre o frontend, mas o backend deve garantir que os dados sejam seguros ou documentar a necessidade de sanitização.
**Recommendation:** Implementar sanitização de entrada para todos os dados que contribuem para a `descricao` do alerta antes de serem armazenados. Além disso, garantir que o frontend escape corretamente o conteúdo ao renderizar as descrições dos alertas para evitar XSS.