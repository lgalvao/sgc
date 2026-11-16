package sesel.sgc.mapa.atividades;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.mapa.ValidacaoErro;
import sesel.sgc.mapa.conhecimentos.Conhecimento;
import sesel.sgc.processounidade.ProcessoUnidade;
import sesel.sgc.processounidade.ProcessoUnidadeNaoEncontradoErro;
import sesel.sgc.processounidade.ProcessoUnidadeService;

import java.util.*;
import java.util.stream.Collectors;

import static sesel.sgc.mapa.MapaUnidadeSituacao.EM_ANDAMENTO;
import static sesel.sgc.mapa.MapaUnidadeSituacao.NAO_INICIADA;

/**
 * Serviço para gerenciar operações relacionadas a Atividades e Conhecimentos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AtividadeService {
    private final AtividadeRepository atividadeRepository;
    private final ProcessoUnidadeService processoUnidadeService;

    /**
     * Cadastra ou atualiza as atividades de um processoUnidade.
     *
     * @param request Dados do cadastro de atividades
     * @throws ValidacaoErro Quando ocorrem violações nas regras de
     *                       negócio
     */
    @Transactional
    public void cadastrarAtividades(CadastroAtividadesDto request) {
        log.info("Cadastrando/atualizando atividades para processo de unidade");

        // --- REMOÇÃO DE ATIVIDADES EXCLUÍDAS NO FORMULÁRIO ---
        // Buscar todas as atividades existentes do processoUnidade
        List<Atividade> atividadesExistentes = atividadeRepository
                .findByProcessoUnidadeIdComConhecimentos(request.getProcessoUnidadeId());
        // Pegar os IDs das atividades enviadas pelo formulário
        Set<UUID> idsAtividadesRecebidas = request.getAtividades().stream()
                .map(AtividadeDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // Remover do banco as atividades que não estão mais no formulário
        for (Atividade atividade : atividadesExistentes) {
            if (atividade.getId() != null && !idsAtividadesRecebidas.contains(atividade.getId())) {
                atividadeRepository.delete(atividade);
            }
        }
        // --- FIM DA REMOÇÃO ---

        // Valida se o processoUnidade existe e pode ser alterado
        ProcessoUnidade processoUnidade = validarProcessoUnidadeParaEdicao(request.getProcessoUnidadeId());

        // Mapeia DTOs para entidades
        List<Atividade> atividades = mapearParaEntidades(processoUnidade, request.getAtividades());

        // Salva as atividades e conhecimentos
        List<Atividade> atividadesSalvas = atividadeRepository.saveAll(atividades);

        // Atualiza a situação do processoUnidade se não for rascunho
        if (!request.isRascunho()) {
            processoUnidade.setSituacao(EM_ANDAMENTO);
            processoUnidadeService.atualizarProcessoUnidade(processoUnidade);
        }

        log.info("{} atividades cadastradas/atualizadas para processoUnidade: {}",
                atividadesSalvas.size(), request.getProcessoUnidadeId());
    }

    /**
     * Valida se o processoUnidade existe e pode ser alterado.
     */
    private ProcessoUnidade validarProcessoUnidadeParaEdicao(UUID processoUnidadeId) {
        try {
            ProcessoUnidade processoUnidade = processoUnidadeService.buscarProcessoUnidadePorId(processoUnidadeId);

            // Verifica se o processoUnidade está em um estado que permite edição
            MapaUnidadeSituacao situacao = processoUnidade.getSituacao();
            if (situacao != NAO_INICIADA && situacao != EM_ANDAMENTO) {
                throw new ValidacaoErro("Não é possível alterar atividades de uma unidade finalizada.");
            }
            return processoUnidade;
        } catch (ProcessoUnidadeNaoEncontradoErro e) {
            throw new ValidacaoErro("Processo de unidade não encontrado: %s".formatted(processoUnidadeId),
                    e);
        }
    }


    /**
     * Mapeia os DTOs para entidades, aplicando as regras de negócio.
     */
    private List<Atividade> mapearParaEntidades(ProcessoUnidade processoUnidade, List<AtividadeDto> atividadesDto) {
        List<Atividade> atividades = new ArrayList<>();

        for (AtividadeDto dto : atividadesDto) {
            Atividade atividade = new Atividade();

            // Se já existir, carrega a atividade existente
            if (dto.getId() != null) {
                atividade = atividadeRepository.findById(dto.getId()).orElseThrow(() -> new ValidacaoErro(
                        "Atividade não encontrada: %s".formatted(dto.getId())));
                // ATUALIZA A DESCRIÇÃO DA ENTIDADE PELO DTO
                atividade.setDescricao(dto.getDescricao());
                // Limpa os conhecimentos existentes para adicionar os novos
                atividade.getConhecimentos().clear();
            } else {
                atividade.setDescricao(dto.getDescricao());
            }
            atividade.setProcessoUnidade(processoUnidade);

            // Adiciona os conhecimentos
            for (AtividadeDto.ConhecimentoDto conhecimentoDto : dto.getConhecimentos()) {
                Conhecimento conhecimento = new Conhecimento();
                conhecimento.setDescricao(conhecimentoDto.getDescricao().trim());
                conhecimento.setTipo(sesel.sgc.mapa.conhecimentos.ConhecimentoTipo.TECNICO);
                conhecimento.setAtividade(atividade);
                atividade.getConhecimentos().add(conhecimento);
            }

            atividades.add(atividade);
        }

        return atividades;
    }

    /**
     * Busca as atividades de um processoUnidade.
     */
    public List<AtividadeDto> buscarAtividadesPorProcessoUnidade(UUID processoUnidadeId) {
        log.info("Buscando atividades para processo: {}", processoUnidadeId);
        List<Atividade> atividades = atividadeRepository.findByProcessoUnidadeIdComConhecimentos(processoUnidadeId);
        log.info("Atividades retornadas: {}", atividades);
        if (atividades == null) {
            log.error("Repositório retornou lista nula!");
            throw new ValidacaoErro("Erro interno: lista de atividades nula.");
        }
        if (atividades.stream().anyMatch(Objects::isNull)) {
            log.error("Repositório retornou atividade nula na lista!");
            throw new ValidacaoErro("Erro interno: atividade nula encontrada.");
        }
        return atividades.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Converte uma entidade Atividade para DTO.
     */
    private AtividadeDto toDto(Atividade atividade) {
        if (atividade == null) return null;

        AtividadeDto dto = new AtividadeDto();
        dto.setId(atividade.getId());
        dto.setDescricao(atividade.getDescricao());

        // Converte os conhecimentos para DTOs
        if (atividade.getConhecimentos() != null) {
            List<AtividadeDto.ConhecimentoDto> conhecimentos = atividade.getConhecimentos().stream()
                    .map(c -> {
                        if (c == null) return null;

                        AtividadeDto.ConhecimentoDto conhecimentoDto = new AtividadeDto.ConhecimentoDto();
                        conhecimentoDto.setId(c.getId());
                        conhecimentoDto.setDescricao(c.getDescricao());
                        conhecimentoDto.setTipo(c.getTipo());
                        return conhecimentoDto;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
            dto.setConhecimentos(conhecimentos);
        } else {
            dto.setConhecimentos(new ArrayList<>());
        }
        return dto;
    }
}