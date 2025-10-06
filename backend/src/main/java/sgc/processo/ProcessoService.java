package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.erros.ErroDominioProcesso;
import sgc.mapa.*;
import sgc.notificacao.EmailNotificationService;
import sgc.notificacao.EmailTemplateService;
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
 * - Persiste snapshot de unidades em UNIDADE_PROCESSO ao iniciar processo (copia dados essenciais da unidade).
 * <p>
 * Nota: regras mais complexas (verificação de UNIDADE_MAPA para revisão/diagnóstico, criação de subprocessos/mapas/movimentações)
 * estão previstas em CDU-04/CDU-05 e serão implementadas em subtasks específicas.
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
    private final CopiaMapaService copiaMapaService;
    private final ApplicationEventPublisher publisher;
    private final EmailNotificationService emailService;
    private final EmailTemplateService emailTemplateService;
    private final SgrhService sgrhService;

    @Transactional
    public ProcessoDTO criar(ReqCriarProcesso req) {
        if (req.getDescricao() == null || req.getDescricao().isBlank()) {
            throw new ConstraintViolationException("Preencha a descrição", null);
        }
        if (req.getUnidades() == null || req.getUnidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser incluída.", null);
        }

        if ("REVISAO".equalsIgnoreCase(req.getTipo()) || "DIAGNOSTICO".equalsIgnoreCase(req.getTipo())) {
            for (Long unidadeCodigo : req.getUnidades()) {
                if (unidadeRepository.findById(unidadeCodigo).isEmpty()) {
                    throw new IllegalArgumentException("Unidade " + unidadeCodigo + " não encontrada.");
                }
            }
        }

        Processo p = new Processo();
        p.setDescricao(req.getDescricao());
        p.setTipo(req.getTipo());
        p.setDataLimite(req.getDataLimiteEtapa1());
        p.setSituacao("CRIADO");
        p.setDataCriacao(LocalDateTime.now());

        Processo salvo = processoRepository.save(p);

        // publicar evento de criação (outros listeners farão envio de e-mails/alertas)
        publisher.publishEvent(new EventoProcessoCriado(this, salvo.getCodigo()));

        return ProcessoMapper.toDTO(salvo);
    }

    @Transactional
    public ProcessoDTO atualizar(Long id, ReqAtualizarProcesso req) {
        Processo atualizado = processoRepository.findById(id)
                .map(existing -> {
                    if (existing.getSituacao() != null && !"CRIADO".equalsIgnoreCase(existing.getSituacao())) {
                        throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser editados.");
                    }
                    existing.setDescricao(req.getDescricao());
                    existing.setTipo(req.getTipo());
                    existing.setDataLimite(req.getDataLimiteEtapa1());
                    return processoRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));
        return ProcessoMapper.toDTO(atualizado);
    }

    @Transactional
    public void apagar(Long id) {
        Processo proc = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));
        if (proc.getSituacao() != null && !"CRIADO".equalsIgnoreCase(proc.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser removidos.");
        }
        processoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDTO> getById(Long id) {
        return processoRepository.findById(id).map(ProcessoMapper::toDTO);
    }

    /**
     * Recupera detalhes do processo, incluindo lista de unidades snapshot (UNIDADE_PROCESSO)
     * e resumo dos subprocessos. Valida permissão do usuário pelo perfil/unidade:
     * - ADMIN tem acesso a qualquer processo
     * - GESTOR só tem acesso se sua unidade possuir algum subprocesso no processo
     *
     * @param processoId     id do processo
     * @param perfil         perfil do usuário (ADMIN|GESTOR)
     * @param unidadeUsuario unidade do usuário (pode ser null para ADMIN)
     * @return ProcessoDetalheDTO com dados completos do processo
     * @throws IllegalArgumentException se processo não encontrado
     * @throws ErroDominioAccessoNegado se usuário não tiver permissão
     */
    @Transactional(readOnly = true)
    public ProcessoDetalheDTO obterDetalhes(Long processoId, String perfil, Long unidadeUsuario) {
        if (perfil == null) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do processo.");
        }

        Processo proc = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + processoId));

        // carregar unidades snapshot e subprocessos com fetch join para evitar N+1
        List<UnidadeProcesso> unidadesProcesso = unidadeProcessoRepository.findByProcessoCodigo(processoId);
        List<Subprocesso> subprocessos = subprocessoRepository.findByProcessoCodigoWithUnidade(processoId);

        // simples validação de permissão: ADMIN ok; GESTOR precisa ter unidade presente nos subprocessos
        if ("GESTOR".equalsIgnoreCase(perfil)) {
            boolean presente = false;
            if (subprocessos != null) {
                for (Subprocesso sp : subprocessos) {
                    if (sp.getUnidade() != null && unidadeUsuario != null && unidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                        presente = true;
                        break;
                    }
                }
            }
            if (!presente) {
                throw new ErroDominioAccessoNegado("Usuário sem permissão para visualizar este processo.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil) && !"GESTOR".equalsIgnoreCase(perfil)) {
            // perfis distintos não autorizados
            throw new ErroDominioAccessoNegado("Perfil sem permissão.");
        }

        return ProcessoMapper.toDetailDTO(proc, unidadesProcesso, subprocessos);
    }

    /**
     * Inicia o processo no modo de mapeamento.
     * Persiste snapshot de unidades participantes em UNIDADE_PROCESSO e atualiza situação do processo.
     * Cria subprocessos, mapas vazios, movimentações iniciais e publica evento para alertas/e-mails.
     *
     * @param id            processo id
     * @param unidadesLista lista de codigos de unidades participantes
     * @return DTO do processo atualizado
     */
    @Transactional
    public ProcessoDTO iniciarProcessoMapeamento(Long id, List<Long> unidadesLista) {
        Processo proc = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));

        if (proc.getSituacao() == null || !"CRIADO".equalsIgnoreCase(proc.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (unidadesLista == null || unidadesLista.isEmpty()) {
            throw new IllegalArgumentException("Lista de unidades é obrigatória para iniciar processo.");
        }

        // VALIDAÇÃO: Verificar se unidades já estão em processos ativos (regra 8 do CDU-04)
        validarUnidadesNaoEmProcessosAtivos(unidadesLista);

        // Criar snapshots, subprocessos e movimentações para cada unidade
        for (Long unidadeCodigo : unidadesLista) {
            Unidade unidade = unidadeRepository.findById(unidadeCodigo)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + unidadeCodigo));

            // Criar snapshot da unidade em UNIDADE_PROCESSO
            UnidadeProcesso up = getUnidadeProcesso(proc, unidade);
            unidadeProcessoRepository.save(up);

            // criar mapa vazio vinculado ao subprocesso
            Mapa mapa = new Mapa();
            Mapa mapaSalvo = mapaRepository.save(mapa);

            // Criar subprocesso vinculado ao processo e unidade
            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setMapa(mapaSalvo);
            sp.setSituacaoId("PENDENTE");
            sp.setDataLimiteEtapa1(proc.getDataLimite()); // Copiar data limite do processo
            Subprocesso spSalvo = subprocessoRepository.save(sp);

            // Criar movimentação inicial para o subprocesso
            Movimentacao mov = new Movimentacao();
            mov.setSubprocesso(spSalvo);
            mov.setDataHora(LocalDateTime.now());
            mov.setUnidadeOrigem(null); // SEDOC não tem registro como unidade
            mov.setUnidadeDestino(unidade);
            mov.setDescricao("Processo iniciado");
            movimentacaoRepository.save(mov);
        }

        // Atualizar situação do processo
        proc.setSituacao("EM_ANDAMENTO");
        Processo salvo = processoRepository.save(proc);

        // Publicar evento para que listeners criem alertas e enviem e-mails
        publisher.publishEvent(new EventoProcessoIniciado(
                salvo.getCodigo(),
                salvo.getTipo(),
                LocalDateTime.now(),
                unidadesLista
        ));

        return ProcessoMapper.toDTO(salvo);
    }

    /**
     * Valida se unidades já estão participando de processos ativos.
     * Implementa regra 8 do CDU-04: unidades não podem estar em múltiplos processos ativos.
     */
    private void validarUnidadesNaoEmProcessosAtivos(List<Long> unidadesCodigos) {
        // Buscar processos em andamento
        List<Processo> processosAtivos = processoRepository.findBySituacao("EM_ANDAMENTO");

        for (Long unidadeCodigo : unidadesCodigos) {
            Unidade unidade = unidadeRepository.findById(unidadeCodigo)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unidade não encontrada: " + unidadeCodigo));

            // Verificar se unidade já participa de algum processo ativo
            for (Processo processoAtivo : processosAtivos) {
                List<UnidadeProcesso> unidadesProcesso = unidadeProcessoRepository
                        .findByProcessoCodigo(processoAtivo.getCodigo());

                for (UnidadeProcesso up : unidadesProcesso) {
                    if (unidade.getSigla().equals(up.getSigla())) {
                        throw new IllegalStateException(String.format(
                                "A unidade %s já está participando do processo ativo: %s (código %d)",
                                unidade.getSigla(),
                                processoAtivo.getDescricao(),
                                processoAtivo.getCodigo()
                        ));
                    }
                }
            }
        }
    }

    private static UnidadeProcesso getUnidadeProcesso(Processo proc, Unidade unidade) {
        UnidadeProcesso up = new UnidadeProcesso();
        up.setProcessoCodigo(proc.getCodigo());
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
     * Valida mapas vigentes, copia mapas existentes, cria subprocessos e publica evento.
     *
     * @param id            processo id
     * @param unidadesLista lista de codigos de unidades participantes
     * @return DTO do processo atualizado
     */
    @Transactional
    public ProcessoDTO startRevisionProcess(Long id, List<Long> unidadesLista) {
        Processo proc = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));

        if (proc.getSituacao() == null || !"CRIADO".equalsIgnoreCase(proc.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (unidadesLista == null || unidadesLista.isEmpty()) {
            throw new IllegalArgumentException("Lista de unidades é obrigatória para iniciar processo.");
        }

        // VALIDAÇÃO: Verificar se unidades já estão em processos ativos
        validarUnidadesNaoEmProcessosAtivos(unidadesLista);

        // VALIDAÇÃO: Verificar se todas as unidades possuem mapas vigentes (obrigatório para revisão)
        validarUnidadesComMapasVigentes(unidadesLista);

        // Criar snapshots, copiar mapas e criar subprocessos para cada unidade
        for (Long unidadeCodigo : unidadesLista) {
            Unidade unidade = unidadeRepository.findById(unidadeCodigo)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + unidadeCodigo));

            // Localizar mapa vigente da unidade
            UnidadeMapa unidadeMapa = unidadeMapaRepository.findByUnidadeCodigo(unidadeCodigo)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Nenhum mapa vigente encontrado para unidade: " + unidadeCodigo));

            Long sourceMapaId = unidadeMapa.getMapaVigente() != null ?
                    unidadeMapa.getMapaVigente().getCodigo() : null;

            if (sourceMapaId == null) {
                throw new IllegalArgumentException("Mapa vigente inválido para unidade: " + unidadeCodigo);
            }

            // Criar cópia do mapa vigente para o processo de revisão
            Mapa novoMapa = copiaMapaService.copyMapForUnit(sourceMapaId, unidadeCodigo);

            // Criar snapshot da unidade em UNIDADE_PROCESSO
            UnidadeProcesso up = getProcesso(proc, unidade);
            unidadeProcessoRepository.save(up);

            // Criar subprocesso vinculado ao processo e unidade com o mapa copiado
            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setMapa(novoMapa);
            sp.setSituacaoId("PENDENTE");
            sp.setDataLimiteEtapa1(proc.getDataLimite()); // Copiar data limite do processo
            Subprocesso spSalvo = subprocessoRepository.save(sp);

            // Criar movimentação inicial para o subprocesso
            Movimentacao mov = new Movimentacao();
            mov.setSubprocesso(spSalvo);
            mov.setDataHora(LocalDateTime.now());
            mov.setUnidadeOrigem(null); // SEDOC não tem registro como unidade
            mov.setUnidadeDestino(unidade);
            mov.setDescricao("Processo iniciado");
            movimentacaoRepository.save(mov);
        }

        // Atualizar situação do processo
        proc.setSituacao("EM_ANDAMENTO");
        Processo salvo = processoRepository.save(proc);

        // Publicar evento para que listeners criem alertas e enviem e-mails
        publisher.publishEvent(new EventoProcessoIniciado(
                salvo.getCodigo(),
                salvo.getTipo(),
                LocalDateTime.now(),
                unidadesLista
        ));

        return ProcessoMapper.toDTO(salvo);
    }

    /**
     * Valida se todas as unidades possuem mapas vigentes.
     * Necessário para processos de revisão - apenas unidades com mapas podem revisar.
     */
    private void validarUnidadesComMapasVigentes(List<Long> unidadesCodigos) {
        for (Long unidadeCodigo : unidadesCodigos) {
            // Buscar mapa vigente da unidade usando a nova query
            Optional<Mapa> mapaVigente = mapaRepository.findMapaVigenteByUnidade(unidadeCodigo);

            if (mapaVigente.isEmpty()) {
                Unidade unidade = unidadeRepository.findById(unidadeCodigo)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unidade não encontrada: " + unidadeCodigo));

                throw new IllegalStateException(String.format(
                        "A unidade %s não possui mapa vigente. " +
                                "Apenas unidades com mapas podem participar de processos de revisão.",
                        unidade.getSigla()
                ));
            }
        }
    }

    private static UnidadeProcesso getProcesso(Processo proc, Unidade unidade) {
        UnidadeProcesso up = new UnidadeProcesso();
        up.setProcessoCodigo(proc.getCodigo());
        up.setNome(unidade.getNome());
        up.setSigla(unidade.getSigla());
        up.setTitularTitulo(unidade.getTitular() != null ? unidade.getTitular().getTitulo() : null);
        up.setTipo(unidade.getTipo());
        up.setSituacao("PENDENTE");
        up.setUnidadeSuperiorCodigo(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null);
        return up;
    }

    /**
     * CDU-21 - Finalizar processo de mapeamento ou revisão.
     * <p>
     * Implementa o fluxo completo de finalização:
     * 1. Valida situação do processo
     * 2. Valida que todos subprocessos estão em 'MAPA_HOMOLOGADO'
     * 3. Torna mapas vigentes (atualiza UNIDADE_MAPA)
     * 4. Envia notificações diferenciadas por tipo de unidade
     * 5. Atualiza situação do processo para FINALIZADO
     * 6. Publica evento
     *
     * @param id ID do processo
     * @return DTO do processo finalizado
     * @throws ErroDominioNaoEncontrado se processo não encontrado
     * @throws IllegalStateException    se processo não está em andamento
     * @throws ErroDominioProcesso      se há subprocessos não homologados
     */
    @Transactional
    public ProcessoDTO finalizeProcess(Long id) {
        log.info("Iniciando finalização do processo: codigo={}", id);

        // 1. Buscar processo
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo não encontrado: " + id));

        // 2. Validar situação do processo
        if (processo.getSituacao() == null || !"EM_ANDAMENTO".equalsIgnoreCase(processo.getSituacao())) {
            throw new IllegalStateException("Apenas processos em andamento podem ser finalizados.");
        }

        // 3. Validar todos subprocessos em 'MAPA_HOMOLOGADO' (item 4 do CDU-21)
        validarTodosSubprocessosHomologados(processo);

        // 4. Tornar mapas vigentes (item 8) - AÇÃO CRÍTICA
        tornarMapasVigentes(processo);

        // 5. Mudar situação do processo (item 10)
        processo.setSituacao("FINALIZADO");
        processo.setDataFinalizacao(LocalDateTime.now());
        processo = processoRepository.save(processo);

        // 6. Enviar notificações diferenciadas (item 9)
        enviarNotificacoesFinalizacao(processo);

        // 7. Publicar evento
        publisher.publishEvent(new EventoProcessoFinalizado(this, processo.getCodigo()));

        log.info("Processo finalizado com sucesso: codigo={}", id);

        return ProcessoMapper.toDTO(processo);
    }

    /**
     * Item 4 do CDU-21 - Validar que todos subprocessos estão em 'Mapa homologado'.
     * <p>
     * Lança exceção detalhada listando TODAS as unidades que ainda não estão homologadas.
     *
     * @param processo Processo sendo finalizado
     * @throws ErroDominioProcesso se houver subprocessos não homologados
     */
    private void validarTodosSubprocessosHomologados(Processo processo) {
        log.debug("Validando homologação de subprocessos do processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        List<String> subprocessosPendentes = getSubprocessosPendentes(subprocessos);

        if (!subprocessosPendentes.isEmpty()) {
            String mensagem = String.format("""
                            Não é possível encerrar o processo enquanto houver unidades com mapa de competência \
                            ainda não homologado.
                            
                            Unidades pendentes:
                            - %s
                            
                            Todos os subprocessos devem estar na situação 'MAPA_HOMOLOGADO'.""",
                    String.join("\n- ", subprocessosPendentes)
            );

            log.warn("Validação falhou: {} subprocessos não homologados", subprocessosPendentes.size());
            throw new ErroDominioProcesso(mensagem);
        }

        log.info("Validação OK: {} subprocessos homologados", subprocessos.size());
    }

    private static List<String> getSubprocessosPendentes(List<Subprocesso> subprocessos) {
        List<String> subprocessosPendentes = new ArrayList<>();

        for (Subprocesso subprocesso : subprocessos) {
            if (!"MAPA_HOMOLOGADO".equalsIgnoreCase(subprocesso.getSituacaoId())) {
                String nomeUnidade = subprocesso.getUnidade() != null ?
                        subprocesso.getUnidade().getSigla() : "Unidade " + subprocesso.getCodigo();

                subprocessosPendentes.add(
                        nomeUnidade + " (Situação: " + subprocesso.getSituacaoId() + ")"
                );
            }
        }
        return subprocessosPendentes;
    }

    /**
     * Item 8 do CDU-21 - Tornar mapas dos subprocessos como vigentes de suas unidades.
     * <p>
     * AÇÃO CRÍTICA: Atualiza tabela UNIDADE_MAPA para registrar os mapas homologados
     * como os mapas vigentes de cada unidade participante.
     * <p>
     * Se unidade já possui mapa vigente: SUBSTITUI pelo novo
     * Se unidade não possui mapa vigente: CRIA novo registro
     *
     * @param processo Processo sendo finalizado
     * @throws ErroDominioNaoEncontrado se mapa de algum subprocesso não for encontrado
     */
    private void tornarMapasVigentes(Processo processo) {
        log.info("Tornando mapas vigentes para processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        int mapasAtualizados = 0;
        int mapasCriados = 0;

        for (Subprocesso subprocesso : subprocessos) {
            Long unidadeCodigo = subprocesso.getUnidade() != null ?
                    subprocesso.getUnidade().getCodigo() : null;

            if (unidadeCodigo == null) {
                log.warn("Subprocesso {} sem unidade associada, pulando", subprocesso.getCodigo());
                continue;
            }

            // Buscar mapa do subprocesso
            Mapa mapaSubprocesso = subprocesso.getMapa();

            if (mapaSubprocesso == null) {
                log.error("Subprocesso {} sem mapa associado", subprocesso.getCodigo());
                throw new ErroDominioNaoEncontrado(
                        "Mapa não encontrado para subprocesso " + subprocesso.getCodigo()
                );
            }

            // Verificar se já existe mapa vigente para esta unidade
            Optional<UnidadeMapa> unidadeMapaExistente = unidadeMapaRepository
                    .findByUnidadeCodigo(unidadeCodigo);

            if (unidadeMapaExistente.isPresent()) {
                // ATUALIZAR mapa vigente existente
                UnidadeMapa unidadeMapa = unidadeMapaExistente.get();
                unidadeMapa.setMapaVigenteCodigo(mapaSubprocesso.getCodigo());
                unidadeMapa.setDataVigencia(LocalDate.now());
                unidadeMapaRepository.save(unidadeMapa);

                mapasAtualizados++;
                log.debug("Mapa vigente ATUALIZADO: unidade={}, novoMapa={}",
                        unidadeCodigo, mapaSubprocesso.getCodigo());
            } else {
                // CRIAR novo registro de mapa vigente
                UnidadeMapa unidadeMapa = new UnidadeMapa();
                unidadeMapa.setUnidadeCodigo(unidadeCodigo);
                unidadeMapa.setMapaVigenteCodigo(mapaSubprocesso.getCodigo());
                unidadeMapa.setDataVigencia(LocalDate.now());
                unidadeMapaRepository.save(unidadeMapa);

                mapasCriados++;
                log.debug("Mapa vigente CRIADO: unidade={}, mapa={}",
                        unidadeCodigo, mapaSubprocesso.getCodigo());
            }
        }

        log.info("Mapas vigentes processados: {} atualizados, {} criados, total={}",
                mapasAtualizados, mapasCriados, subprocessos.size());
    }

    /**
     * Item 9 do CDU-21 - Enviar notificações diferenciadas por tipo de unidade.
     * <p>
     * Mensagens diferenciadas conforme especificação:
     * - Unidades OPERACIONAL: "Seu mapa de competências está agora vigente"
     * - Unidades INTERMEDIARIA: "Os mapas das unidades subordinadas estão vigentes"
     * - Unidades INTEROPERACIONAL: Ambas as informações
     *
     * @param processo Processo finalizado
     */
    private void enviarNotificacoesFinalizacao(Processo processo) {
        log.info("Enviando notificações de finalização para processo {}", processo.getCodigo());

        List<Subprocesso> subprocessos = subprocessoRepository
                .findByProcessoCodigo(processo.getCodigo());

        int notificacoesEnviadas = 0;
        int falhas = 0;

        for (Subprocesso subprocesso : subprocessos) {
            try {
                Long unidadeCodigo = subprocesso.getUnidade() != null ?
                        subprocesso.getUnidade().getCodigo() : null;

                if (unidadeCodigo == null) {
                    log.warn("Subprocesso {} sem unidade, pulando notificação", subprocesso.getCodigo());
                    continue;
                }

                // Buscar responsável da unidade
                Optional<ResponsavelDto> responsavelOpt = sgrhService
                        .buscarResponsavelUnidade(unidadeCodigo);

                if (responsavelOpt.isEmpty() || responsavelOpt.get().titularTitulo() == null) {
                    log.warn("Unidade {} sem responsável, pulando notificação", unidadeCodigo);
                    continue;
                }

                ResponsavelDto responsavel = responsavelOpt.get();

                // Buscar dados do titular
                Optional<UsuarioDto> titularOpt = sgrhService
                        .buscarUsuarioPorTitulo(responsavel.titularTitulo());

                if (titularOpt.isEmpty() || titularOpt.get().email() == null) {
                    log.warn("Titular da unidade {} sem e-mail, pulando notificação", unidadeCodigo);
                    continue;
                }

                UsuarioDto titular = titularOpt.get();

                // Buscar dados da unidade para identificar tipo
                UnidadeDto unidade = sgrhService.buscarUnidadePorCodigo(unidadeCodigo)
                        .orElseThrow(() -> new ErroDominioNaoEncontrado(
                                "Unidade não encontrada: " + unidadeCodigo));

                // Criar mensagem diferenciada conforme tipo de unidade (item 9.1 e 9.2)
                String mensagemPersonalizada = getMensagemPersonalizada(unidade);

                // Criar e enviar e-mail usando template específico
                String htmlEmail = emailTemplateService.criarEmailProcessoFinalizadoUnidade(
                        unidade.sigla(),
                        processo.getDescricao(),
                        mensagemPersonalizada
                );

                emailService.enviarEmailHtml(
                        titular.email(),
                        "SGC: Conclusão do processo " + processo.getDescricao(),
                        htmlEmail
                );

                notificacoesEnviadas++;
                log.debug("E-mail de finalização enviado para unidade {} ({})",
                        unidade.sigla(), titular.email());

            } catch (Exception e) {
                falhas++;
                log.error("Erro ao enviar notificação de finalização para subprocesso {}: {}",
                        subprocesso.getCodigo(), e.getMessage(), e);
                // Não interromper o fluxo - continua enviando para outras unidades
            }
        }

        log.info("Notificações de finalização: {} enviadas, {} falhas, total de {} subprocessos",
                notificacoesEnviadas, falhas, subprocessos.size());
    }

    private static String getMensagemPersonalizada(UnidadeDto unidade) {
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

    /* Eventos simples como classes estáticas internas para evitar necessidade de criar vários arquivos nesta tarefa.
           Listeners externos continuam compatíveis pois os eventos são publicados como objetos.
        */
    public record EventoProcessoCriado(Object source, Long processoCodigo) {
    }

    public record EventoProcessoFinalizado(Object source, Long processoCodigo) {
    }
}