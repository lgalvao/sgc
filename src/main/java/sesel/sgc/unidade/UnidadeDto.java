package sesel.sgc.unidade;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class UnidadeDto {
    private UUID id;
    private String nome;
    private List<UnidadeDto> subordinadas = new ArrayList<>();
    private UUID idUnidadeSuperior;

    public UnidadeDto(UUID id, String nome, UUID idUnidadeSuperior) {
        this.id = id;
        this.nome = nome;
        this.idUnidadeSuperior = idUnidadeSuperior;
    }

    public boolean possuiSubordinadas() {
        return subordinadas != null && !subordinadas.isEmpty();
    }
}