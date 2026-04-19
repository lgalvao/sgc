package sgc.organizacao.model;

public record UsuarioPerfilLeitura(
        String usuarioTitulo,
        Long unidadeCodigo,
        Perfil perfil
) {
}
