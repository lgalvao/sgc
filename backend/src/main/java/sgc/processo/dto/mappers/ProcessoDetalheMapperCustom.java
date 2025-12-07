package sgc.processo.dto.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.model.Processo;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ProcessoDetalheMapperCustom implements ProcessoDetalheMapper {
    @Autowired
    private ProcessoDetalheMapper delegate;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Override
    public ProcessoDetalheDto toDetailDTO(Processo processo) {
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
                .build();

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(processo.getCodigo());
        // Montar a hierarquia de unidades participantes
        montarHierarquiaUnidades(dto, processo, subprocessos);

        return dto;
    }

    private boolean isCurrentUserChefeOuCoordenador(Processo processo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Usuario)) {
            return false;
        }
        Usuario user = (Usuario) principal;
        return processo.getParticipantes().stream()
                .anyMatch(unidade -> user.getTodasAtribuicoes().stream()
                        .anyMatch(attr -> attr.getUnidade().getCodigo().equals(unidade.getCodigo())));
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    protected void montarHierarquiaUnidades(ProcessoDetalheDto dto,
            Processo processo,
            List<Subprocesso> subprocessos) {
        Map<Long, ProcessoDetalheDto.UnidadeParticipanteDto> mapaUnidades = new HashMap<>();
        for (Unidade participante : processo.getParticipantes()) {
            mapaUnidades.put(participante.getCodigo(), delegate.unidadeToUnidadeParticipanteDTO(participante));
        }

        for (Subprocesso sp : subprocessos) {
            ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto = mapaUnidades.get(sp.getUnidade().getCodigo());
            if (unidadeDto != null) {
                unidadeDto.setSituacaoSubprocesso(sp.getSituacao());
                unidadeDto.setDataLimite(sp.getDataLimiteEtapa1());
                unidadeDto.setCodSubprocesso(sp.getCodigo());
                if (sp.getMapa() != null) {
                    unidadeDto.setMapaCodigo(sp.getMapa().getCodigo());
                }
            }
        }

        // Monta a hierarquia
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            if (unidadeDto.getCodUnidadeSuperior() != null) {
                ProcessoDetalheDto.UnidadeParticipanteDto pai = mapaUnidades.get(unidadeDto.getCodUnidadeSuperior());
                if (pai != null) {
                    pai.getFilhos().add(unidadeDto);
                }
            }
        }

        // Adiciona unidades raiz E unidades sem pai no mapa (participantes diretos sem
        // hierarquia)
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            if (unidadeDto.getCodUnidadeSuperior() == null ||
                    !mapaUnidades.containsKey(unidadeDto.getCodUnidadeSuperior())) {
                dto.getUnidades().add(unidadeDto);
            }
        }

        // Ordena as unidades e seus filhos
        Comparator<ProcessoDetalheDto.UnidadeParticipanteDto> comparator = Comparator
                .comparing(ProcessoDetalheDto.UnidadeParticipanteDto::getSigla);
        dto.getUnidades().sort(comparator);
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidadeDto : mapaUnidades.values()) {
            unidadeDto.getFilhos().sort(comparator);
        }
    }
}
