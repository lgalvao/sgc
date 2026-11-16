package sesel.sgc.unidade;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;

    public UnidadeService(UnidadeRepository unidadeRepository) {
        this.unidadeRepository = unidadeRepository;
    }

    public List<Unidade> listarTodas() {
        return unidadeRepository.findAll();
    }

    public Unidade buscarPorId(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new RuntimeException("Unidade não encontrada"));
    }

    public List<UnidadeDto> listarHierarquicamente() {
        List<Unidade> todas = unidadeRepository.findAllWithSubordinadas();
        // Encontrar unidades sem superior (raiz)
        List<Unidade> raizes = todas.stream().filter(u -> u.getUnidadeSuperior() == null).toList();
        return raizes.stream().map(this::toDtoRecursivo).toList();
    }

    private UnidadeDto toDtoRecursivo(Unidade unidade) {
        UnidadeDto dto = new UnidadeDto(unidade.getId(), unidade.getNome(),
                unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getId() : null);
        if (unidade.getUnidadesSubordinadas() != null && !unidade.getUnidadesSubordinadas().isEmpty()) {
            dto.setSubordinadas(unidade.getUnidadesSubordinadas().stream().map(this::toDtoRecursivo).toList());
        }
        return dto;
    }
}