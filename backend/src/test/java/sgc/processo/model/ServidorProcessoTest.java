package sgc.processo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ServidorProcesso")
class ServidorProcessoTest {

    @Test
    @DisplayName("deve criar snapshot do servidor participante para o processo")
    void deveCriarSnapshotDoServidorParticipanteParaOProcesso() {
        Processo processo = new Processo();
        processo.setCodigo(50L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(8L);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        usuario.setMatricula("12345678");
        usuario.setNome("Servidor Snapshot");
        usuario.setEmail("snapshot@teste");
        usuario.setUnidadeLotacao(unidade);

        ServidorProcesso snapshot = ServidorProcesso.criarSnapshot(processo, unidade.getCodigo(), usuario);

        assertThat(snapshot.getProcesso()).isSameAs(processo);
        assertThat(snapshot.getUnidadeCodigo()).isEqualTo(8L);
        assertThat(snapshot.getUsuarioTitulo()).isEqualTo("123456789012");
        assertThat(snapshot.getMatricula()).isEqualTo("12345678");
        assertThat(snapshot.getNome()).isEqualTo("Servidor Snapshot");
        assertThat(snapshot.getEmail()).isEqualTo("snapshot@teste");
    }
}
