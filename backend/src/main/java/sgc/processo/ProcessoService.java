package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.*;
import sgc.notificacao.EmailNotificationService;
import sgc.notificacao.EmailTemplateService;
import sgc.processo.dto.ProcessoDTO;
import sgc.processo.dto.ProcessoDetalheDTO;
import sgc.processo.dto.ReqAtualizarProcesso;
import sgc.processo.dto.ReqCriarProcesso;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.Movimentacao;
import sgc.subprocesso.MovimentacaoRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviço com regras de domínio para Processos.
 * <p>
 * Observações:
 * - Implementa validações básicas (descrição não vazia; pelo menos 1 unidade).
 * - Publica eventos quando um processo é criado, iniciado ou finalizado.
 * - Persiste um snapshot das unidades na tabela UNIDADE_PROCESSO ao iniciar o processo.
 * <p>
 * Nota: regras mais complexas (verificação de UNIDADE_MAPA para revisão/diagnóstico, criação de subprocessos/mapas/movimentações)
 * estão previstas em CDU-04/CDU-05 e serão implementadas em tarefas específicas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoService {
    private final ProcessoRepository processoRepository;
    private final UnidadeRepository unidadeRepository;
    private final UnidadeProcessoRepository unidadeProcessoRepository;
    private final SubprocessoRepository subprocessoRepository;
    private final MapaRepository mapaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final UnidadeMapaRepository unidadeMapaRepository;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final EmailNotificationService servicoDeEmail;
    private final EmailTemplateService servicoDeTemplateDeEmail;
    private final SgrhService sgrhService;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheMapper processoDetalheMapper;

    @Transactional
    public ProcessoDTO criar(ReqCriarProcesso requisicao) {
        if (requisicao.getDescricao() == null || requisicao.getDescricao().isBlank()) {
            throw new ConstraintViolationException("Preencha a descrição", null);
        }
        if (requisicao.getUnidades() == null || requisicao.getUnidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser incluída.", null);
        }

        if ("REVISAO".equalsIgnoreCase(requisicao.getTipo()) || "DIAGNOSTICO".equalsIgnoreCase(requisicao.getTipo())) {
            for (Long codigoUnidade : requisicao.getUnidades()) {
                if (unidadeRepository.findById(codigoUnidade).isEmpty()) {
                    throw new IllegalArgumentException("Unidade " + codigoUnidade + " não encontrada.");
                }
            }
        }

        Processo processo = new Processo();
        processo.setDescricao(requisicao.getDescricao());
        processo.setTipo(requisicao.getTipo());
        processo.setDataLimite(requisicao.getDataLimiteEtapa1());
        processo.setSituacao("CRIADO");
        processo.setDataCriacao(LocalDateTime.now());

        Processo processoSalvo = processoRepository.save(processo);

        // Publicar evento de criação (outros listeners cuidarão do envio de e-mails/alertas)
        publicadorDeEventos.publishEvent(new EventoDeProcessoCriado(this, processoSalvo.getCodigo()));

        return processoMapper.toDTO(processoSalvo);
    }

    @Transactional
    public ProcessoDTO atualizar(Long id, ReqAtualizarProcesso requisicao) {
        Processo processoAtualizado = processoRepository.findById(id)
                .map(processoExistente -> {
                    if (processoExistente.getSituacao() != null && !"CRIADO".equalsIgnoreCase(processoExistente.getSituacao())) {
                        throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser editados.");
                    }
                    processoExistente.setDescricao(requisicao.getDescricao());
                    processoExistente.setTipo(requisicao.getTipo());
                    processoExistente.setDataLimite(requisicao.getDataLimiteEtapa1());
                    return processoRepository.save(processoExistente);
                })
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));
        return processoMapper.toDTO(processoAtualizado);
    }

    @Transactional
    public void apagar(Long id) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));
        if (processo.getSituacao() != null && !"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }
        processoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDTO> obterPorId(Long id) {
        return processoRepository.findById(id).map(processoMapper::toDTO);
    }

    /**
     * Recupera detalhes do processo, incluindo a lista de snapshots de unidades (UNIDADE_PROCESSO)
     * e o resumo dos subprocessos. Valida a permissão do usuário pelo perfil/unidade:
     * - ADMIN tem acesso a qualquer processo.
     * - GESTOR só tem acesso se sua unidade possuir algum subprocesso no processo.
     *
     * @param idProcesso       ID do processo
     * @param perfil           perfil do usuário (ADMIN|GESTOR)
     * @param idUnidadeUsuario unidade do usuário (pode ser nulo para ADMIN)
     * @return ProcessoDetalheDTO com dados completos do processo
     * @throws IllegalArgumentException   se o processo não for encontrado
     * @throws ErroDominioAccessoNegado se o usuário não tiver permissão
     */
    @Transactional(readOnly = true)
    public ProcessoDetalheDTO obterDetalhes(Long idProcesso, String perfil, Long idUnidadeUsuario) {
        if (perfil == null) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do processo.");
        }

        Processo processo = processoRepository.findById(idProcesso)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + idProcesso));

        // Carregar snapshots de unidades e subprocessos com fetch join para evitar N+1
        List<UnidadeProcesso> listaUnidadesProcesso = unidadeProcessoRepository.findByProcessoCodigo(idProcesso);
        List<Subprocesso> subprocessos = subprocessoRepository.findByProcessoCodigoWithUnidade(idProcesso);

        // Validação de permissão: ADMIN pode ver tudo; GESTOR precisa ter sua unidade presente nos subprocessos
        if ("GESTOR".equalsIgnoreCase(perfil)) {
            boolean unidadePresenteNoProcesso = false;
            if (subprocessos != null) {
                for (Subprocesso sp : subprocessos) {
                    if (sp.getUnidade() != null && idUnidadeUsuario != null && idUnidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                        unidadePresenteNoProcesso = true;
                        break;
                    }
                }
            }
            if (!unidadePresenteNoProcesso) {
                throw new ErroDominioAccessoNegado("Usuário sem permissão para visualizar este processo.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil) && !"GESTOR".equalsIgnoreCase(perfil)) {
            // Outros perfis não são autorizados
            throw new ErroDominioAccessoNegado("Perfil sem permissão.");
        }

        return processoDetalheMapper.toDetailDTO(processo, listaUnidadesProcesso, subprocessos);
    }

    /**
     * Inicia o processo no modo de mapeamento.
     * Persiste o snapshot das unidades participantes, atualiza a situação do processo,
     * cria subprocessos, mapas vazios, movimentações iniciais e publica um evento para alertas/e-mails.
     *
     * @param id              ID do processo
     * @param codigosUnidades lista de códigos das unidades participantes
     * @return DTO do processo atualizado
     */
    @Transactional
    public ProcessoDTO iniciarProcessoMapeamento(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));

        if (processo.getSituacao() == null || !"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo.");
        }

        // VALIDAÇÃO: Verificar se as unidades já estão em processos ativos (regra 8 do CDU-04)
        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        // Criar snapshots, subprocessos e movimentações para cada unidade
        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepository.findById(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + codigoUnidade));

            // Criar snapshot da unidade em UNIDADE_PROCESSO
            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
            unidadeProcessoRepository.save(unidadeProcesso);

            // Criar mapa vazio vinculado ao subprocesso
            Mapa mapa = new Mapa();
            Mapa mapaSalvo = mapaRepository.save(mapa);

            // Criar subprocesso vinculado ao processo e à unidade
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setProcesso(processo);
            subprocesso.setUnidade(unidade);
            subprocesso.setMapa(mapaSalvo);
            subprocesso.setSituacaoId("PENDENTE");
            subprocesso.setDataLimiteEtapa1(processo.getDataLimite()); // Copiar data limite do processo
            Subprocesso subprocessoSalvo = subprocessoRepository.save(subprocesso);

            // Criar movimentação inicial para o subprocesso
            Movimentacao movimentacao = new Movimentacao();
            movimentacao.setSubprocesso(subprocessoSalvo);
            movimentacao.setDataHora(LocalDateTime.now());
            movimentacao.setUnidadeOrigem(null); // SEDOC não tem registro como unidade
            movimentacao.setUnidadeDestino(unidade);
            movimentacao.setDescricao("Processo iniciado");
            movimentacaoRepository.save(movimentacao);
        }

        // Atualizar situação do processo
        processo.setSituacao("EM_ANDAMENTO");
        Processo processoSalvo = processoRepository.save(processo);

        // Publicar evento para que listeners criem alertas e enviem e-mails
        publicadorDeEventos.publishEvent(new EventoDeProcessoIniciado(
                processoSalvo.getCodigo(),
                processoSalvo.getTipo(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        return processoMapper.toDTO(processoSalvo);
    }

    /**
     * Valida se as unidades já estão participando de processos ativos.
     * Implementa a regra 8 do CDU-04: unidades não podem estar em múltiplos processos ativos.
     */
    private void validarUnidadesNaoEmProcessosAtivos(List<Long> codigosDasUnidades) {
        // Buscar processos em andamento
        List<Processo> processosEmAndamento = processoRepository.findBySituacao("EM_ANDAMENTO");

        for (Long codigoUnidade : codigosDasUnidades) {
            Unidade unidade = unidadeRepository.findById(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unidade não encontrada: " + codigoUnidade));

            // Verificar se a unidade já participa de algum processo ativo
            for (Processo processoEmAndamento : processosEmAndamento) {
                List<UnidadeProcesso> unidadesDoProcesso = unidadeProcessoRepository
                        .findByProcessoCodigo(processoEmAndamento.getCodigo());

                for (UnidadeProcesso up : unidadesDoProcesso) {
                    if (unidade.getSigla().equals(up.getSigla())) {
                        throw new IllegalStateException(String.format(
                                "A unidade %s já está participando do processo ativo: %s (código %d)",
                                unidade.getSigla(),
                                processoEmAndamento.getDescricao(),
                                processoEmAndamento.getCodigo()
                        ));
                    }
                }
            }
        }
    }

    private static UnidadeProcesso criarSnapshotUnidadeProcesso(Processo processo, Unidade unidade) {
        UnidadeProcesso up = new UnidadeProcesso();
        up.setProcessoCodigo(processo.getCodigo());
        up.setNome(unidade.getNome());
        up.setSigla(unidade.getSigla());
        up.setTitularTitulo(unidade.getTitular() != null ? unidade.getTitular().getTitulo() : null);
        up.setTipo(unidade.getTipo());
        up.setSituacao("PENDENTE");
        up.setUnidadeSuperiorCodigo(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null);
        return up;
    }

    /**
     * Inicia o processo no modo de revisão.
     * Valida os mapas vigentes, copia os mapas existentes, cria os subprocessos e publica um evento.
     *
     * @param id              ID do processo
     * @param codigosUnidades lista de códigos das unidades participantes
     * @return DTO do processo atualizado
     */
    @Transactional
    public ProcessoDTO iniciarProcessoRevisao(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));

        if (processo.getSituacao() == null || !"CRIADO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo.");
        }

        // VALIDAÇÃO: Verificar se as unidades já estão em processos ativos
        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        // VALIDAÇÃO: Verificar se todas as unidades possuem mapas vigentes (obrigatório para revisão)
        validarUnidadesComMapasVigentes(codigosUnidades);

        // Criar snapshots, copiar mapas e criar subprocessos para cada unidade
        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepository.findById(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + codigoUnidade));

            // Localizar mapa vigente da unidade
            UnidadeMapa unidadeMapa = unidadeMapaRepository.findByUnidadeCodigo(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Nenhum mapa vigente encontrado para a unidade: " + codigoUnidade));

            Long idMapaOrigem = unidadeMapa.getMapaVigente() != null ?
                    unidadeMapa.getMapaVigente().getCodigo() : null;

            if (idMapaOrigem == null) {
                throw new IllegalArgumentException("Mapa vigente inválido para a unidade: " + codigoUnidade);
            }

            // Criar cópia do mapa vigente para o processo de revisão
            Mapa mapaNovo = servicoDeCopiaDeMapa.copiarMapaParaUnidade(idMapaOrigem, codigoUnidade);

            // Criar snapshot da unidade em UNIDADE_PROCESSO
            UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
            unidadeProcessoRepository.save(unidadeProcesso);

            // Criar subprocesso vinculado ao processo e unidade com o mapa copiado
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setProcesso(processo);
            subprocesso.setUnidade(unidade);
            subprocesso.setMapa(mapaNovo);
            subprocesso.setSituacaoId("PENDENTE");
            subprocesso.setDataLimiteEtapa1(processo.getDataLimite()); // Copiar data limite do processo
            Subprocesso subprocessoSalvo = subprocessoRepository.save(subprocesso);

            // Criar movimentação inicial para o subprocesso
            Movimentacao movimentacao = new Movimentacao();
            movimentacao.setSubprocesso(subprocessoSalvo);
            movimentacao.setDataHora(LocalDateTime.now());
            movimentacao.setUnidadeOrigem(null); // SEDOC não tem registro como unidade
            movimentacao.setUnidadeDestino(unidade);
            movimentacao.setDescricao("Processo iniciado");
            movimentacaoRepository.save(movimentacao);
        }

        // Atualizar situação do processo
        processo.setSituacao("EM_ANDAMENTO");
        Processo processoSalvo = processoRepository.save(processo);

        // Publicar evento para que listeners criem alertas e enviem e-mails
        publicadorDeEventos.publishEvent(new EventoDeProcessoIniciado(
                processoSalvo.getCodigo(),
                processoSalvo.getTipo(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        return processoMapper.toDTO(processoSalvo);
    }

    /**
     * Valida se todas as unidades possuem mapas vigentes.
     * Necessário para processos de revisão, pois apenas unidades com mapas podem participar.
     */
    private void validarUnidadesComMapasVigentes(List<Long> codigosDasUnidades) {
        for (Long codigoUnidade : codigosDasUnidades) {
            // Buscar mapa vigente da unidade usando a nova query
            Optional<Mapa> mapaVigenteOptional = mapaRepository.findMapaVigenteByUnidade(codigoUnidade);

            if (mapaVigenteOptional.isEmpty()) {
                Unidade unidade = unidadeRepository.findById(codigoUnidade)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unidade não encontrada: " + codigoUnidade));

                throw new IllegalStateException(String.format(
                        "A unidade %s não possui mapa vigente. " +
                                "Apenas unidades com mapas podem participar de processos de revisão.",
                        unidade.getSigla()
                ));
            }
        }
    }

    /**
     * CDU-21 - Finalizar processo de mapeamento ou revisão.
     * <p>
     * Implementa o fluxo completo de finalização:
     * 1. Valida a situação do processo.
     * 2. Valida que todos os subprocessos estão em 'MAPA_HOMOLOGADO'.
     * 3. Torna os mapas vigentes (atualiza UNIDADE_MAPA).
     * 4. Envia notificações diferenciadas por tipo de unidade.
     * 5. Atualiza a situação do processo para 'FINALIZADO'.
     * 6. Publica um evento de finalização.
     *
     * @param id ID do processo
     * @return DTO do processo finalizado
     * @throws ErroEntidadeNaoEncontrada se o processo não for encontrado
     * @throws IllegalStateException    se o processo não estiver em andamento
     * @throws ErroProcesso             se houver subprocessos não homologados
     */
    @Transactional
    public ProcessoDTO finalizarProcesso(Long id) {
        log.info("Iniciando finalização do processo: código={}", id);

        // 1. Buscar processo
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: " + id));

        // 2. Validar situação do processo
        if (processo.getSituacao() == null || !"EM_ANDAMENTO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos em andamento podem ser finalizados.");
        }

        // 3. Validar se todos os subprocessos estão em 'MAPA_HOMOLOGADO' (item 4 do CDU-21)
        validarTodosSubprocessosHomologados(processo);

        // 4. Tornar os mapas vigentes (item 8) - AÇÃO CRÍTICA
        tornarMapasVigentes(processo);

        // 5. Mudar a situação do processo (item 10)
        processo.setSituacao("FINALIZADO");
        processo.setDataFinalizacao(LocalDateTime.now());
        processo = processoRepository.save(processo);

        // 6. Enviar notificações diferenciadas (item 9)
        enviarNotificacoesDeFinalizacao(processo);

        // 7. Publicar evento
        publicadorDeEventos.publishEvent(new EventoDeProcessoFinalizado(this, processo.getCodigo()));

        log.info("Processo finalizado com sucesso: código={}", id);

        return processoMapper.toDTO(processo);
    }

    /**
     * Item 4 do CDU-21 - Validar que todos os subprocessos estão com 'Mapa homologado'.
     * <p>
     * Lança uma exceção detalhada listando TODAS as unidades que ainda não estão homologadas.
     *
     * @param processo Processo sendo finalizado
     * @throws ErroProcesso se houver subprocessos não homologados
     */
    private void validarTodosSubprocessosHomologados(Processo processo) {
        log.debug("Validando homologação de subprocessos do processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        List<String> listaSubprocessosPendentes = obterListaDeSubprocessosPendentes(subprocessos);

        if (!listaSubprocessosPendentes.isEmpty()) {
            String mensagem = String.format("""
                            Não é possível encerrar o processo enquanto houver unidades com mapa de competência \
                            ainda não homologado.
                            
                            Unidades pendentes:
                            - %s
                            
                            Todos os subprocessos devem estar na situação 'MAPA_HOMOLOGADO'.""",
                    String.join("\n- ", listaSubprocessosPendentes)
            );

            log.warn("Validação falhou: {} subprocessos não homologados", listaSubprocessosPendentes.size());
            throw new ErroProcesso(mensagem);
        }

        log.info("Validação OK: {} subprocessos homologados", subprocessos.size());
    }

    private static List<String> obterListaDeSubprocessosPendentes(List<Subprocesso> subprocessos) {
        List<String> listaSubprocessosPendentes = new ArrayList<>();

        for (Subprocesso subprocesso : subprocessos) {
            if (!"MAPA_HOMOLOGADO".equalsIgnoreCase(subprocesso.getSituacaoId())) {
                String nomeUnidade = subprocesso.getUnidade() != null ?
                        subprocesso.getUnidade().getSigla() : "Unidade " + subprocesso.getCodigo();

                listaSubprocessosPendentes.add(
                        nomeUnidade + " (Situação: " + subprocesso.getSituacaoId() + ")"
                );
            }
        }
        return listaSubprocessosPendentes;
    }

    /**
     * Item 8 do CDU-21 - Tornar os mapas dos subprocessos como vigentes para suas unidades.
     * <p>
     * AÇÃO CRÍTICA: Atualiza a tabela UNIDADE_MAPA para registrar os mapas homologados
     * como os mapas vigentes de cada unidade participante.
     * <p>
     * Se a unidade já possui um mapa vigente, ele é SUBSTITUÍDO pelo novo.
     * Se a unidade não possui um mapa vigente, um novo registro é CRIADO.
     *
     * @param processo Processo sendo finalizado
     * @throws ErroEntidadeNaoEncontrada se o mapa de algum subprocesso não for encontrado
     */
    private void tornarMapasVigentes(Processo processo) {
        log.info("Tornando mapas vigentes para o processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        int totalMapasAtualizados = 0;
        int totalMapasCriados = 0;

        for (Subprocesso subprocesso : subprocessos) {
            Long codigoUnidade = subprocesso.getUnidade() != null ?
                    subprocesso.getUnidade().getCodigo() : null;

            if (codigoUnidade == null) {
                log.warn("Subprocesso {} sem unidade associada. Pulando.", subprocesso.getCodigo());
                continue;
            }

            // Buscar mapa do subprocesso
            Mapa mapaDoSubprocesso = subprocesso.getMapa();

            if (mapaDoSubprocesso == null) {
                log.error("Subprocesso {} sem mapa associado.", subprocesso.getCodigo());
                throw new ErroEntidadeNaoEncontrada(
                        "Mapa não encontrado para o subprocesso " + subprocesso.getCodigo()
                );
            }

            // Verificar se já existe mapa vigente para esta unidade
            Optional<UnidadeMapa> unidadeMapaExistenteOptional = unidadeMapaRepository
                    .findByUnidadeCodigo(codigoUnidade);

            if (unidadeMapaExistenteOptional.isPresent()) {
                // ATUALIZAR mapa vigente existente
                UnidadeMapa unidadeMapa = unidadeMapaExistenteOptional.get();
                unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
                unidadeMapa.setDataVigencia(LocalDate.now());
                unidadeMapaRepository.save(unidadeMapa);

                totalMapasAtualizados++;
                log.debug("Mapa vigente ATUALIZADO: unidade={}, novoMapa={}",
                        codigoUnidade, mapaDoSubprocesso.getCodigo());
            } else {
                // CRIAR novo registro de mapa vigente
                UnidadeMapa unidadeMapa = new UnidadeMapa();
                unidadeMapa.setUnidadeCodigo(codigoUnidade);
                unidadeMapa.setMapaVigenteCodigo(mapaDoSubprocesso.getCodigo());
                unidadeMapa.setDataVigencia(LocalDate.now());
                unidadeMapaRepository.save(unidadeMapa);

                totalMapasCriados++;
                log.debug("Mapa vigente CRIADO: unidade={}, mapa={}",
                        codigoUnidade, mapaDoSubprocesso.getCodigo());
            }
        }

        log.info("Mapas vigentes processados: {} atualizados, {} criados, total={}",
                totalMapasAtualizados, totalMapasCriados, subprocessos.size());
    }

    /**
     * Item 9 do CDU-21 - Enviar notificações diferenciadas por tipo de unidade.
     * <p>
     * Mensagens são customizadas conforme a especificação:
     * - Unidades OPERACIONAL: "Seu mapa de competências está agora vigente"
     * - Unidades INTERMEDIARIA: "Os mapas das unidades subordinadas estão vigentes"
     * - Unidades INTEROPERACIONAL: Ambas as informações
     *
     * @param processo Processo finalizado
     */
    private void enviarNotificacoesDeFinalizacao(Processo processo) {
        log.info("Enviando notificações de finalização para o processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        int totalNotificacoesEnviadas = 0;
        int totalFalhas = 0;

        for (Subprocesso subprocesso : subprocessos) {
            try {
                Long codigoUnidade = subprocesso.getUnidade() != null ?
                        subprocesso.getUnidade().getCodigo() : null;

                if (codigoUnidade == null) {
                    log.warn("Subprocesso {} sem unidade. Pulando notificação.", subprocesso.getCodigo());
                    continue;
                }

                // Buscar responsável da unidade
                Optional<ResponsavelDto> responsavelOptional = sgrhService
                        .buscarResponsavelUnidade(codigoUnidade);

                if (responsavelOptional.isEmpty() || responsavelOptional.get().titularTitulo() == null) {
                    log.warn("Unidade {} sem responsável. Pulando notificação.", codigoUnidade);
                    continue;
                }

                ResponsavelDto responsavel = responsavelOptional.get();

                // Buscar dados do titular
                Optional<UsuarioDto> titularOptional = sgrhService
                        .buscarUsuarioPorTitulo(responsavel.titularTitulo());

                if (titularOptional.isEmpty() || titularOptional.get().email() == null) {
                    log.warn("Titular da unidade {} sem e-mail. Pulando notificação.", codigoUnidade);
                    continue;
                }

                UsuarioDto titular = titularOptional.get();

                // Buscar dados da unidade para identificar o tipo
                UnidadeDto unidade = sgrhService.buscarUnidadePorCodigo(codigoUnidade)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                                "Unidade não encontrada: " + codigoUnidade));

                // Criar mensagem diferenciada conforme o tipo de unidade (item 9.1 e 9.2)
                String mensagemPersonalizada = criarMensagemPersonalizada(unidade);

                // Criar e enviar e-mail usando o template específico
                String htmlEmail = servicoDeTemplateDeEmail.criarEmailProcessoFinalizadoUnidade(
                        unidade.sigla(),
                        processo.getDescricao(),
                        mensagemPersonalizada
                );

                servicoDeEmail.enviarEmailHtml(
                        titular.email(),
                        "SGC: Conclusão do processo " + processo.getDescricao(),
                        htmlEmail
                );

                totalNotificacoesEnviadas++;
                log.debug("E-mail de finalização enviado para a unidade {} ({})",
                        unidade.sigla(), titular.email());

            } catch (Exception ex) {
                totalFalhas++;
                log.error("Erro ao enviar notificação de finalização para o subprocesso {}: {}",
                        subprocesso.getCodigo(), ex.getMessage(), ex);
                // Não interromper o fluxo, continua enviando para outras unidades
            }
        }

        log.info("Notificações de finalização: {} enviadas, {} falhas, total de {} subprocessos.",
                totalNotificacoesEnviadas, totalFalhas, subprocessos.size());
    }

    private static String criarMensagemPersonalizada(UnidadeDto unidade) {
        String mensagemPersonalizada;

        if ("OPERACIONAL".equalsIgnoreCase(unidade.tipo())) {
            // Item 9.1 - Unidades operacionais
            mensagemPersonalizada = "Seu mapa de competências está agora vigente e pode ser " +
                    "visualizado através do sistema.";
        } else if ("INTERMEDIARIA".equalsIgnoreCase(unidade.tipo())) {
            // Item 9.2 - Unidades intermediárias
            mensagemPersonalizada = "Os mapas de competências das unidades subordinadas a esta " +
                    "unidade estão agora vigentes e podem ser visualizados através do sistema.";
        } else if ("INTEROPERACIONAL".equalsIgnoreCase(unidade.tipo())) {
            // Unidades interoperacionais recebem ambas as informações
            mensagemPersonalizada = "Seu mapa de competências e os mapas das unidades subordinadas " +
                    "estão agora vigentes e podem ser visualizados através do sistema.";
        } else {
            // Tipo desconhecido - mensagem genérica
            mensagemPersonalizada = "O mapa de competências da unidade está agora vigente.";
        }
        return mensagemPersonalizada;
    }

    /**
     * Eventos simples definidos como classes record internas para evitar a criação de múltiplos arquivos.
     * Listeners externos continuam compatíveis, pois os eventos são publicados como objetos.
     */
    public record EventoDeProcessoCriado(Object source, Long idProcesso) {
    }

    public record EventoDeProcessoIniciado(Long idProcesso, String tipo, LocalDateTime dataInicio, List<Long> idsUnidades) {
    }

    public record EventoDeProcessoFinalizado(Object source, Long idProcesso) {
    }
}