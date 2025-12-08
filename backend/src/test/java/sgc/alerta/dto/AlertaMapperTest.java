package sgc.alerta.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.alerta.model.Alerta;
import sgc.processo.model.Processo;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

@ExtendWith(MockitoExtension.class)
class AlertaMapperTest {

    @Mock private SubprocessoRepo subprocessoRepo;

    // Subclass to test protected methods and inject mocks
    static class AlertaMapperImpl extends AlertaMapper {
        @Override
        public AlertaDto toDto(Alerta alerta) {
            return null;
        }
    }

    @InjectMocks private AlertaMapperImpl mapper;

    @Test
    @DisplayName("formatDataHora returns formatted string")
    void formatDataHora() {
        LocalDateTime dt = LocalDateTime.of(2023, 10, 25, 14, 30);
        assertThat(mapper.formatDataHora(dt)).isEqualTo("25/10/2023 14:30");
    }

    @Test
    @DisplayName("formatDataHora returns empty string if null")
    void formatDataHoraNull() {
        assertThat(mapper.formatDataHora(null)).isEqualTo("");
    }

    @Test
    @DisplayName("extractProcessoName returns name")
    void extractProcessoName() {
        String desc = "Início do processo 'Mapeamento 2023'. Preencha...";
        assertThat(mapper.extractProcessoName(desc)).isEqualTo("Mapeamento 2023");
    }

    @Test
    @DisplayName("extractProcessoName returns empty if no match")
    void extractProcessoNameNoMatch() {
        String desc = "Outra descrição";
        assertThat(mapper.extractProcessoName(desc)).isEqualTo("");
    }

    @Test
    @DisplayName("buildLinkDestino returns correct link")
    void buildLinkDestino() {
        Alerta alerta = new Alerta();
        Processo p = new Processo();
        p.setCodigo(1L);
        alerta.setProcesso(p);
        Unidade u = new Unidade();
        u.setCodigo(10L);
        alerta.setUnidadeDestino(u);

        Usuario user = new Usuario();
        Unidade uUser = new Unidade();
        uUser.setCodigo(10L);
        user.setUnidadeLotacao(uUser);
        user.getAtribuicoes()
                .add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(user)
                                .unidade(uUser)
                                .perfil(sgc.sgrh.model.Perfil.SERVIDOR)
                                .build()); // Add a profile to avoid empty attributions logic if
        // tested

        Authentication auth = mock(Authentication.class);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(sc);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(50L);
        when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(1L, 10L))
                .thenReturn(Optional.of(sp));

        String link = mapper.buildLinkDestino(alerta);
        assertThat(link).isEqualTo("/subprocessos/50");
    }

    @Test
    @DisplayName("buildLinkDestino returns null if missing info")
    void buildLinkDestinoMissingInfo() {
        Alerta alerta = new Alerta();
        assertThat(mapper.buildLinkDestino(alerta)).isNull();
    }
}
