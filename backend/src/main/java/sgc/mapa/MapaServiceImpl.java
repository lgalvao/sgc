package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeRepository;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.competencia.Competencia;
import sgc.competencia.CompetenciaAtividade;
import sgc.competencia.CompetenciaAtividadeRepository;
import sgc.competencia.CompetenciaRepository;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de negócio para Mapas de Competências.
 * <p>
 * Gerencia operações agregadas sobre mapas, competências e vínculos com atividades.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaServiceImpl implements MapaService {
    
    private final MapaRepository mapaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final CompetenciaAtividadeRepository competenciaAtividadeRepository;
    private final AtividadeRepository atividadeRepository;
    private final SubprocessoRepository subprocessoRepository;
    
    @Override
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long mapaId) {
        log.debug("Obtendo mapa completo: mapaId={}", mapaId);
        
        // 1. Buscar mapa
        Mapa mapa = mapaRepository.findById(mapaId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: " + mapaId));
        
        // 2. Buscar subprocesso vinculado ao mapa
        Long subprocessoCodigo = buscarSubprocessoDoMapa(mapaId);
        
        // 3. Buscar todas competências do mapa
        List<Competencia> competencias = competenciaRepository.findByMapaCodigo(mapaId);
        
        // 4. Para cada competência, buscar atividades vinculadas
        List<CompetenciaMapaDto> competenciasDto = competencias.stream()
            .map(c -> {
                List<Long> atividadesCodigos = competenciaAtividadeRepository
                    .findByCompetenciaCodigo(c.getCodigo())
                    .stream()
                    .map(ca -> ca.getId().getAtividadeCodigo())
                    .toList();
                
                return new CompetenciaMapaDto(
                    c.getCodigo(),
                    c.getDescricao(),
                    atividadesCodigos
                );
            })
            .toList();
        
        // 5. Retornar DTO agregado
        return new MapaCompletoDto(
            mapa.getCodigo(),
            subprocessoCodigo,
            mapa.getObservacoesDisponibilizacao(),
            competenciasDto
        );
    }
    
    @Override
    public MapaCompletoDto salvarMapaCompleto(
        Long mapaId, 
        SalvarMapaRequest request,
        String usuarioTitulo
    ) {
        log.info("Salvando mapa completo: mapaId={}, usuario={}", mapaId, usuarioTitulo);
        
        // 1. Buscar mapa
        Mapa mapa = mapaRepository.findById(mapaId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: " + mapaId));
        
        // 2. Atualizar observações do mapa
        mapa.setObservacoesDisponibilizacao(request.observacoes());
        mapa = mapaRepository.save(mapa);
        
        // 3. Buscar competências atuais do mapa
        List<Competencia> competenciasAtuais = competenciaRepository.findByMapaCodigo(mapaId);
        Set<Long> codigosAtuais = competenciasAtuais.stream()
            .map(Competencia::getCodigo)
            .collect(Collectors.toSet());
        
        Set<Long> codigosNovos = request.competencias().stream()
            .map(CompetenciaMapaDto::codigo)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        // 4. Remover competências excluídas
        Set<Long> codigosRemover = new HashSet<>(codigosAtuais);
        codigosRemover.removeAll(codigosNovos);
        
        for (Long codigoRemover : codigosRemover) {
            // Remover vínculos primeiro
            competenciaAtividadeRepository.deleteByCompetenciaCodigo(codigoRemover);
            // Remover competência
            competenciaRepository.deleteById(codigoRemover);
            log.debug("Competência {} removida do mapa {}", codigoRemover, mapaId);
        }
        
        // 5. Processar cada competência do request
        for (CompetenciaMapaDto compDto : request.competencias()) {
            
            Competencia competencia;
            
            if (compDto.codigo() == null) {
                // NOVA competência
                competencia = new Competencia();
                competencia.setMapa(mapa);
                competencia.setDescricao(compDto.descricao());
                competencia = competenciaRepository.save(competencia);
                log.debug("Nova competência criada: {}", competencia.getCodigo());
            } else {
                // ATUALIZAR competência existente
                competencia = competenciaRepository.findById(compDto.codigo())
                    .orElseThrow(() -> new ErroDominioNaoEncontrado(
                        "Competência não encontrada: " + compDto.codigo()
                    ));
                
                competencia.setDescricao(compDto.descricao());
                competencia = competenciaRepository.save(competencia);
                log.debug("Competência atualizada: {}", competencia.getCodigo());
            }
            
            // 6. Atualizar vínculos com atividades
            atualizarVinculosAtividades(competencia.getCodigo(), compDto.atividadesCodigos());
        }
        
        // 7. Validar integridade do mapa (apenas warnings, não bloqueia)
        validarIntegridadeMapa(mapaId);
        
        // 8. Retornar mapa atualizado
        return obterMapaCompleto(mapaId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaDoSubprocesso(Long subprocessoId) {
        log.debug("Obtendo mapa do subprocesso: subprocessoId={}", subprocessoId);
        
        // Buscar subprocesso
        Subprocesso subprocesso = subprocessoRepository.findById(subprocessoId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));
        
        // Verificar se subprocesso tem mapa
        if (subprocesso.getMapa() == null) {
            throw new ErroDominioNaoEncontrado("Subprocesso não possui mapa associado");
        }
        
        return obterMapaCompleto(subprocesso.getMapa().getCodigo());
    }
    
    @Override
    public MapaCompletoDto salvarMapaDoSubprocesso(
        Long subprocessoId, 
        SalvarMapaRequest request,
        String usuarioTitulo
    ) {
        log.info("Salvando mapa do subprocesso: subprocessoId={}, usuario={}", subprocessoId, usuarioTitulo);
        
        // 1. Buscar subprocesso
        Subprocesso subprocesso = subprocessoRepository.findById(subprocessoId)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + subprocessoId));
        
        // 2. Validar situação (deve ser CADASTRO_HOMOLOGADO ou MAPA_CRIADO)
        String situacao = subprocesso.getSituacaoId();
        if (!"CADASTRO_HOMOLOGADO".equals(situacao) && !"MAPA_CRIADO".equals(situacao)) {
            throw new IllegalStateException(
                "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: " + situacao
            );
        }
        
        // 3. Verificar se subprocesso tem mapa
        if (subprocesso.getMapa() == null) {
            throw new ErroDominioNaoEncontrado("Subprocesso não possui mapa associado");
        }
        
        Long mapaId = subprocesso.getMapa().getCodigo();
        
        // 4. Verificar se é primeira vez criando competências
        boolean eraVazio = competenciaRepository.findByMapaCodigo(mapaId).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();
        
        // 5. Salvar mapa completo
        MapaCompletoDto mapaDto = salvarMapaCompleto(mapaId, request, usuarioTitulo);
        
        // 6. Se era vazio e agora tem competências, mudar situação para MAPA_CRIADO
        if (eraVazio && temNovasCompetencias && "CADASTRO_HOMOLOGADO".equals(situacao)) {
            subprocesso.setSituacaoId("MAPA_CRIADO");
            subprocessoRepository.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para MAPA_CRIADO", subprocessoId);
        }
        
        return mapaDto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public void validarMapaCompleto(Long mapaId) {
        log.debug("Validando integridade do mapa: mapaId={}", mapaId);
        
        MapaCompletoDto mapa = obterMapaCompleto(mapaId);
        
        // Validar: todas competências têm atividades
        for (CompetenciaMapaDto comp : mapa.competencias()) {
            if (comp.atividadesCodigos().isEmpty()) {
                throw new IllegalStateException(
                    "A competência '" + comp.descricao() + "' não possui atividades vinculadas"
                );
            }
        }
        
        // Buscar todas atividades do mapa
        List<Atividade> atividades = atividadeRepository.findByMapaCodigo(mapaId);
        
        // Validar: todas atividades estão em alguma competência
        for (Atividade atividade : atividades) {
            boolean temVinculo = competenciaAtividadeRepository
                .existsByAtividadeCodigo(atividade.getCodigo());
            
            if (!temVinculo) {
                throw new IllegalStateException(
                    "A atividade '" + atividade.getDescricao() + "' não está vinculada a nenhuma competência"
                );
            }
        }
        
        log.debug("Mapa {} validado com sucesso", mapaId);
    }
    
    /**
     * Atualiza os vínculos entre competência e atividades.
     * Remove vínculos antigos e cria novos conforme lista.
     */
    private void atualizarVinculosAtividades(Long competenciaCodigo, List<Long> atividadesCodigos) {
        // 1. Remover todos vínculos atuais
        competenciaAtividadeRepository.deleteByCompetenciaCodigo(competenciaCodigo);
        
        // 2. Criar novos vínculos
        for (Long atividadeCodigo : atividadesCodigos) {
            // Validar que atividade existe
            if (!atividadeRepository.existsById(atividadeCodigo)) {
                log.warn("Tentativa de vincular atividade inexistente: {}", atividadeCodigo);
                continue;
            }
            
            CompetenciaAtividade vinculo = new CompetenciaAtividade();
            CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(atividadeCodigo, competenciaCodigo);
            vinculo.setId(id);
            competenciaAtividadeRepository.save(vinculo);
        }
        
        log.debug("Atualizados {} vínculos para competência {}", 
                 atividadesCodigos.size(), competenciaCodigo);
    }
    
    /**
     * Valida integridade do mapa com warnings (não bloqueia operação).
     * <p>
     * Verifica:
     * - Competências sem atividades
     * - Atividades sem competências
     */
    private void validarIntegridadeMapa(Long mapaId) {
        // Buscar todas atividades do mapa
        List<Atividade> atividades = atividadeRepository.findByMapaCodigo(mapaId);
        
        // Buscar todas competências do mapa
        List<Competencia> competencias = competenciaRepository.findByMapaCodigo(mapaId);
        
        // Verificar atividades sem competência
        for (Atividade atividade : atividades) {
            boolean temVinculo = competenciaAtividadeRepository
                .existsByAtividadeCodigo(atividade.getCodigo());
            
            if (!temVinculo) {
                log.warn("Atividade {} não vinculada a nenhuma competência no mapa {}", 
                        atividade.getCodigo(), mapaId);
            }
        }
        
        // Verificar competências sem atividades
        for (Competencia competencia : competencias) {
            List<CompetenciaAtividade> vinculos = competenciaAtividadeRepository
                .findByCompetenciaCodigo(competencia.getCodigo());
            
            if (vinculos.isEmpty()) {
                log.warn("Competência {} sem atividades vinculadas no mapa {}", 
                        competencia.getCodigo(), mapaId);
            }
        }
    }
    
    /**
     * Busca o código do subprocesso associado a um mapa.
     */
    private Long buscarSubprocessoDoMapa(Long mapaId) {
        List<Subprocesso> subprocessos = subprocessoRepository.findAll();
        return subprocessos.stream()
            .filter(s -> s.getMapa() != null && s.getMapa().getCodigo().equals(mapaId))
            .map(Subprocesso::getCodigo)
            .findFirst()
            .orElse(null);
    }
}