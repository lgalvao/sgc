package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.util.FormatadorData;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.mapper.ProcessoDetalheMapper;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProcessoDetalheBuilder {

    private final SubprocessoRepo subprocessoRepo;
    private final ProcessoDetalheMapper processoDetalheMapper;

    @Transactional(readOnly = true)
    public ProcessoDetalheDto build(Processo processo) {
        ProcessoDetalheDto dto = ProcessoDetalheDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataCriacao(processo.getDataCriacao())
                .dataFinalizacao(processo.getDataFinalizacao())
                .dataLimite(processo.getDataLimite())
                .podeFinalizar(isCurrentUserAdmin())
                .podeHomologarCadastro(isCurrentUserChefeOuCoordenador(processo))
                .podeHomologarMapa(isCurrentUserChefeOuCoordenador(processo))
                .dataCriacaoFormatada(FormatadorData.formatarData(processo.getDataCriacao()))
                .dataFinalizacaoFormatada(FormatadorData.formatarData(processo.getDataFinalizacao()))
                .dataLimiteFormatada(FormatadorData.formatarData(processo.getDataLimite()))
                .situacaoLabel(processo.getSituacao().getLabel())
                .tipoLabel(processo.getTipo().getLabel())
                .unidades(new ArrayList<>()) // Inicializar a lista
                .build();

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        montarHierarquiaUnidades(dto, processo, subprocessos);

        return dto;
    }

    private boolean isCurrentUserChefeOuCoordenador(Processo processo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Usuario user)) {
            return false;
        }
        return processo.getParticipantes()
                .stream()
                .anyMatch(unidade -> user.getTodasAtribuicoes()
                        .stream()
                        .anyMatch(attr -> Objects.equals(attr.getUnidade().getCodigo(), unidade.getCodigo()))
                );
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private void montarHierarquiaUnidades(
            ProcessoDetalheDto dto, Processo processo, List<Subprocesso> subprocessos) {
        Map<Long, ProcessoDetalheDto.UnidadeParticipanteDto> mapaUnidades = new HashMap<>();

        // Mapear participantes usando o mapper
        for (Unidade participante : processo.getParticipantes()) {
            mapaUnidades.put(
                    participante.getCodigo(),
                    processoDetalheMapper.toUnidadeParticipanteDto(participante));
        }

        // Preencher dados dos subprocessos
        for (Subprocesso sp : subprocessos) {
            ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto =
                    mapaUnidades.get(sp.getUnidade().getCodigo());
            if (unidadeDto != null) {
                unidadeDto.setSituacaoSubprocesso(sp.getSituacao());
                unidadeDto.setDataLimite(sp.getDataLimiteEtapa1());
                unidadeDto.setCodSubprocesso(sp.getCodigo());
                if (sp.getMapa() != null) {
                    unidadeDto.setMapaCodigo(sp.getMapa().getCodigo());
                }
                unidadeDto.setDataLimiteFormatada(
                        FormatadorData.formatarData(sp.getDataLimiteEtapa1()));
                unidadeDto.setSituacaoLabel(sp.getSituacao().getDescricao());
            }
        }

        // Montar a hierarquia (pai/filho)
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            if (unidadeDto.getCodUnidadeSuperior() != null) {
                ProcessoDetalheDto.UnidadeParticipanteDto pai =
                        mapaUnidades.get(unidadeDto.getCodUnidadeSuperior());
                if (pai != null) {
                    pai.getFilhos().add(unidadeDto);
                }
            }
        }

        // Adicionar raízes à lista principal do DTO
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            Long codUnidadeSuperior = unidadeDto.getCodUnidadeSuperior();
            // Se não tem pai ou o pai não está participando do processo
            if (codUnidadeSuperior == null || !mapaUnidades.containsKey(codUnidadeSuperior)) {
                dto.getUnidades().add(unidadeDto);
            }
        }

        // Ordenação
        Comparator<ProcessoDetalheDto.UnidadeParticipanteDto> comparator =
                Comparator.comparing(ProcessoDetalheDto.UnidadeParticipanteDto::getSigla);

        dto.getUnidades().sort(comparator);

        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            unidadeDto.getFilhos().sort(comparator);
        }
    }
}
