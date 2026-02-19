package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PermissaoSubprocessoIntegrationTest {

    @Autowired
    private SubprocessoFacade subprocessoFacade;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Test
    @DisplayName("Deve permitir edição de cadastro para CHEFE na situação NAO_INICIADO na mesma unidade")
    void devePermitirEdicaoParaChefeEmNaoIniciado() {
        // 1. Criar cenário: Processo de Mapeamento
        Processo processo = Processo.builder()
                .descricao("Processo Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .dataCriacao(LocalDateTime.now())
                .build();
        processo = processoRepo.save(processo);

        // 2. Criar Subprocesso para Unidade 8 (SEDESENV do data.sql) em NAO_INICIADO
        Unidade unidade8 = Unidade.builder().codigo(8L).build();
        Subprocesso sp = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade8)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        sp = subprocessoRepo.save(sp);

        // 3. Simular Usuário autenticado (Fernanda Oliveira - Chefe da unidade 8 no data.sql)
        Usuario usuario = Usuario.builder()
                .tituloEleitoral("3")
                .nome("Fernanda Oliveira")
                .perfilAtivo(Perfil.CHEFE)
                .unidadeAtivaCodigo(8L)
                .authorities(Set.of(Perfil.CHEFE.toGrantedAuthority()))
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 4. Executar verificação via Facade (que chama o Calculator e a Policy)
        // OBS: subprocessoFacade.obterPermissoes usa o usuario autenticado no contexto
        SubprocessoPermissoesDto permissoes = subprocessoFacade.obterPermissoes(sp.getCodigo());

        // 5. Validar hipótese
        assertThat(permissoes.podeEditarCadastro())
                .as("Chefe da mesma unidade deve poder editar cadastro em situação NAO_INICIADO")
                .isTrue();
    }

    @Test
    @DisplayName("Deve negar edição de cadastro para CHEFE se campos transientes forem perdidos")
    void deveNegarEdicaoSeCamposTransientesPerdidos() {
        Processo processo = Processo.builder()
                .descricao("Processo Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .dataCriacao(LocalDateTime.now())
                .build();
        processo = processoRepo.save(processo);

        Unidade unidade8 = Unidade.builder().codigo(8L).build();
        Subprocesso sp = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade8)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        sp = subprocessoRepo.save(sp);

        // Simular usuário SEM unidadeAtivaCodigo e SEM perfilAtivo (campos transientes nulos)
        // Isso acontece se o UsuarioFacade.obterUsuarioAutenticado() recarregar do banco
        Usuario usuario = Usuario.builder()
                .tituloEleitoral("3")
                .nome("Fernanda Oliveira")
                .perfilAtivo(null) // PERDIDO
                .unidadeAtivaCodigo(null) // PERDIDO
                .authorities(Set.of(Perfil.CHEFE.toGrantedAuthority()))
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        SubprocessoPermissoesDto permissoes = subprocessoFacade.obterPermissoes(sp.getCodigo());

        assertThat(permissoes.podeEditarCadastro())
                .as("Sem unidadeAtivaCodigo e perfilAtivo, a permissão deve ser negada")
                .isFalse();
    }

    @Test
    @DisplayName("Deve validar que a comparação de igualdade falha entre Integer e Long")
    void deveValidarFalhaDeIgualdadeEntreTiposDiferentes() {
        Long idLong = 8L;
        Integer idInteger = 8;

        // No AbstractAccessPolicy: Objects.equals(codUnidadeUsuario, codUnidadeRecurso)
        // Se um for Integer e o outro Long, Objects.equals retorna false!
        assertThat(java.util.Objects.equals(idLong, idInteger)).isFalse();
    }
}
