package sgc.service;

import jakarta.validation.ConstraintViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.dto.CreateProcessRequest;
import sgc.dto.ProcessoDTO;
import sgc.dto.UpdateProcessRequest;
import sgc.events.ProcessStartedEvent;
import sgc.mapper.ProcessoMapper;
import sgc.model.*;
import sgc.repository.*;

import java.time.LocalDateTime;
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
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final UnidadeRepository unidadeRepository;
    private final UnidadeProcessoRepository unidadeProcessoRepository;
    private final SubprocessoRepository subprocessoRepository;
    private final MapaRepository mapaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final UnidadeMapaRepository unidadeMapaRepository;
    private final MapCopyService mapCopyService;
    private final ApplicationEventPublisher publisher;
    
    // Explicit constructor kept for tests that instantiate the service directly (backwards compatibility)
    public ProcessoService(ProcessoRepository processoRepository,
                           UnidadeRepository unidadeRepository,
                           UnidadeProcessoRepository unidadeProcessoRepository,
                           SubprocessoRepository subprocessoRepository,
                           MapaRepository mapaRepository,
                           MovimentacaoRepository movimentacaoRepository,
                           UnidadeMapaRepository unidadeMapaRepository,
                           MapCopyService mapCopyService,
                           ApplicationEventPublisher publisher) {
        this.processoRepository = processoRepository;
        this.unidadeRepository = unidadeRepository;
        this.unidadeProcessoRepository = unidadeProcessoRepository;
        this.subprocessoRepository = subprocessoRepository;
        this.mapaRepository = mapaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.unidadeMapaRepository = unidadeMapaRepository;
        this.mapCopyService = mapCopyService;
        this.publisher = publisher;
    }

    @Transactional
    public ProcessoDTO create(CreateProcessRequest req) {
        if (req.getDescricao() == null || req.getDescricao().isBlank()) {
            throw new ConstraintViolationException("Preencha a descrição", null);
        }
        if (req.getUnidades() == null || req.getUnidades().isEmpty()) {
            throw new ConstraintViolationException("Pelo menos uma unidade participante deve ser incluída.", null);
        }

        // validação simplificada para REVISÃO/DIAGNOSTICO (detalhes via UNIDADE_MAPA ficam para outra subtask)
        if ("REVISAO".equalsIgnoreCase(req.getTipo()) || "DIAGNOSTICO".equalsIgnoreCase(req.getTipo())) {
            // placeholder: verificar existência de mapas vigentes (UNIDADE_MAPA) -> atualmente não implementado completamente
            // para não bloquear o progresso aqui, vamos apenas lançar uma IllegalStateException quando alguma unidade não existir
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
        publisher.publishEvent(new ProcessCreatedEvent(this, salvo.getCodigo()));

        return ProcessoMapper.toDTO(salvo);
    }

    @Transactional
    public ProcessoDTO update(Long id, UpdateProcessRequest req) {
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
    public void delete(Long id) {
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
     * @param processoId   id do processo
     * @param perfil       perfil do usuário (ADMIN|GESTOR)
     * @param unidadeUsuario unidade do usuário (pode ser null para ADMIN)
     * @return ProcessDetailDTO com dados completos do processo
     * @throws IllegalArgumentException se processo não encontrado
     * @throws sgc.exception.DomainAccessDeniedException se usuário não tiver permissão
     */
    @Transactional(readOnly = true)
    public sgc.dto.ProcessDetailDTO getDetails(Long processoId, String perfil, Long unidadeUsuario) {
        if (perfil == null) {
            throw new sgc.exception.DomainAccessDeniedException("Perfil inválido para acesso aos detalhes do processo.");
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
                throw new sgc.exception.DomainAccessDeniedException("Usuário sem permissão para visualizar este processo.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil) && !"GESTOR".equalsIgnoreCase(perfil)) {
            // perfis distintos não autorizados
            throw new sgc.exception.DomainAccessDeniedException("Perfil sem permissão.");
        }

        return ProcessoMapper.toDetailDTO(proc, unidadesProcesso, subprocessos);
    }

    /**
     * Inicia o processo no modo de mapeamento.
     * Persiste snapshot de unidades participantes em UNIDADE_PROCESSO e atualiza situação do processo.
     *
     * @param id            processo id
     * @param unidadesLista lista de codigos de unidades participantes (se null, tenta inferir do estado atual; aqui obrigatório)
     * @return DTO do processo atualizado
     */
    @Transactional
    public ProcessoDTO startMappingProcess(Long id, List<Long> unidadesLista) {
        Processo proc = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));

        if (proc.getSituacao() == null || !"CRIADO".equalsIgnoreCase(proc.getSituacao())) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (unidadesLista == null || unidadesLista.isEmpty()) {
            throw new IllegalArgumentException("Lista de unidades é obrigatória para iniciar processo.");
        }
 
        // validar se unidades não possuem processo ativo do mesmo tipo
        for (Long unidadeCodigo : unidadesLista) {
            Unidade unidade = unidadeRepository.findById(unidadeCodigo)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + unidadeCodigo));

            // verificar unidades já relacionadas em UNIDADE_PROCESSO por sigla
            List<UnidadeProcesso> existentes = unidadeProcessoRepository.findBySigla(unidade.getSigla());
            for (UnidadeProcesso existente : existentes) {
                if (existente.getProcessoCodigo() != null) {
                    processoRepository.findById(existente.getProcessoCodigo()).ifPresent(p -> {
                        if (p.getSituacao() != null && !"FINALIZADO".equalsIgnoreCase(p.getSituacao())) {
                            throw new IllegalStateException("Unidade " + unidade.getSigla() + " já participa de processo ativo: " + p.getCodigo());
                        }
                    });
                }
            }

            UnidadeProcesso up = new UnidadeProcesso();
            up.setProcessoCodigo(proc.getCodigo());
            up.setNome(unidade.getNome());
            up.setSigla(unidade.getSigla());
            up.setTitularTitulo(unidade.getTitular() != null ? unidade.getTitular().getTitulo() : null);
            up.setTipo(unidade.getTipo());
            up.setSituacao("PENDENTE");
            up.setUnidadeSuperiorCodigo(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null);
    
            unidadeProcessoRepository.save(up);

            // criar mapa vazio vinculado ao subprocesso
            Mapa mapa = new Mapa();
            Mapa mapaSalvo = mapaRepository.save(mapa);

            // criar subprocesso vinculado ao processo e unidade
            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setMapa(mapaSalvo);
            sp.setSituacaoId("PENDENTE");
            Subprocesso spSalvo = subprocessoRepository.save(sp);

            // criar movimentação inicial para o subprocesso
            Movimentacao mov = new Movimentacao();
            mov.setSubprocesso(spSalvo);
            mov.setDataHora(LocalDateTime.now());
            // origem não definida no início; destino é a unidade responsável
            mov.setUnidadeOrigem(null);
            mov.setUnidadeDestino(unidade);
            mov.setDescricao("INÍCIO_DO_MAPEAMENTO");
            movimentacaoRepository.save(mov);
        }

        proc.setSituacao("EM_ANDAMENTO");
        Processo salvo = processoRepository.save(proc);

        // publicar evento com payload mais rico (id, tipo, timestamp e unidades)
        publisher.publishEvent(new ProcessStartedEvent(salvo.getCodigo(), salvo.getTipo(), LocalDateTime.now(), unidadesLista));

        return ProcessoMapper.toDTO(salvo);
    }

    /**
     * Inicia o processo no modo de revisão.
     * Implementação semelhante ao startMappingProcess; comportamentos adicionais (copiar mapas) serão adicionados na subtask CDU-05.
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
 
        for (Long unidadeCodigo : unidadesLista) {
            Unidade unidade = unidadeRepository.findById(unidadeCodigo)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + unidadeCodigo));

            // localizar mapa vigente para a unidade
            UnidadeMapa unidadeMapa = unidadeMapaRepository.findByUnidadeCodigo(unidadeCodigo)
                    .orElseThrow(() -> new IllegalArgumentException("Nenhum mapa vigente encontrado para unidade: " + unidadeCodigo));

            Long sourceMapaId = unidadeMapa.getMapaVigente() != null ? unidadeMapa.getMapaVigente().getCodigo() : null;
            if (sourceMapaId == null) {
                throw new IllegalArgumentException("Mapa vigente inválido para unidade: " + unidadeCodigo);
            }

            // cria cópia do mapa para a unidade participante
            Mapa novoMapa = mapCopyService.copyMapForUnit(sourceMapaId, unidadeCodigo);

            // persistir snapshot de unidade no processo
            UnidadeProcesso up = new UnidadeProcesso();
            up.setProcessoCodigo(proc.getCodigo());
            up.setNome(unidade.getNome());
            up.setSigla(unidade.getSigla());
            up.setTitularTitulo(unidade.getTitular() != null ? unidade.getTitular().getTitulo() : null);
            up.setTipo(unidade.getTipo());
            up.setSituacao("PENDENTE");
            up.setUnidadeSuperiorCodigo(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null);
    
            unidadeProcessoRepository.save(up);

            // criar subprocesso vinculado ao processo e unidade com o mapa copiado
            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setMapa(novoMapa);
            sp.setSituacaoId("PENDENTE");
            Subprocesso spSalvo = subprocessoRepository.save(sp);

            // criar movimentação inicial para o subprocesso
            Movimentacao mov = new Movimentacao();
            mov.setSubprocesso(spSalvo);
            mov.setDataHora(LocalDateTime.now());
            mov.setUnidadeOrigem(null);
            mov.setUnidadeDestino(unidade);
            mov.setDescricao("INÍCIO_DA_REVISAO");
            movimentacaoRepository.save(mov);
        }

        proc.setSituacao("EM_ANDAMENTO");
        Processo salvo = processoRepository.save(proc);

        // publicar evento indicando início de revisão (reaproveitamos ProcessStartedEvent)
        publisher.publishEvent(new ProcessStartedEvent(salvo.getCodigo(), salvo.getTipo(), LocalDateTime.now(), unidadesLista));

        return ProcessoMapper.toDTO(salvo);
    }

    @Transactional
    public ProcessoDTO finalizeProcess(Long id) {
        Processo proc = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado: " + id));

        // Validar pré-condições mínimas — regras completas do CDU-21 serão validadas em subtasks dependentes.
        // Aqui checamos apenas se está em andamento.
        if (proc.getSituacao() == null || !"EM_ANDAMENTO".equalsIgnoreCase(proc.getSituacao())) {
            throw new IllegalStateException("Apenas processos em andamento podem ser finalizados.");
        }

        proc.setSituacao("FINALIZADO");
        proc.setDataFinalizacao(LocalDateTime.now());
        Processo salvo = processoRepository.save(proc);

        publisher.publishEvent(new ProcessFinalizedEvent(this, salvo.getCodigo()));

        return ProcessoMapper.toDTO(salvo);
    }

    /* Eventos simples como classes estáticas internas para evitar necessidade de criar vários arquivos nesta tarefa.
           Listeners externos continuam compatíveis pois os eventos são publicados como objetos.
        */
        public record ProcessCreatedEvent(Object source, Long processoCodigo) {
    }

    public record ProcessFinalizedEvent(Object source, Long processoCodigo) {
    }
}