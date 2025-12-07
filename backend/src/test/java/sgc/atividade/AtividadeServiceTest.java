package sgc.atividade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.atividade.dto.ConhecimentoMapper;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

@ExtendWith(MockitoExtension.class)
class AtividadeServiceTest {

    @InjectMocks private AtividadeService atividadeService;

    @Mock private AtividadeRepo atividadeRepo;
    @Mock private AtividadeMapper atividadeMapper;
    @Mock private ConhecimentoRepo conhecimentoRepo;
    @Mock private ConhecimentoMapper conhecimentoMapper;
    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private UsuarioRepo usuarioRepo;

    @Test
    @DisplayName("listar deve retornar todas atividades")
    void listar() {
        when(atividadeRepo.findAll()).thenReturn(List.of(new Atividade()));
        when(atividadeMapper.toDto(any())).thenReturn(new AtividadeDto());

        var result = atividadeService.listar();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("obterPorCodigo deve retornar atividade se existir")
    void obterPorCodigo() {
        Long id = 1L;
        when(atividadeRepo.findById(id)).thenReturn(Optional.of(new Atividade()));
        when(atividadeMapper.toDto(any())).thenReturn(new AtividadeDto());

        var result = atividadeService.obterPorCodigo(id);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterPorCodigo deve lançar exceção se não existir")
    void obterPorCodigoNaoEncontrado() {
        Long id = 1L;
        when(atividadeRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.obterPorCodigo(id))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("criar deve salvar e retornar atividade se usuario autorizado")
    void criar() {
        Long mapaId = 10L;
        String usuarioId = "user1";
        AtividadeDto dto = new AtividadeDto();
        dto.setMapaCodigo(mapaId);

        Unidade unidade = new Unidade();
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioId);
        unidade.setTitular(usuario);

        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setMapa(mapa);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
        when(atividadeRepo.save(any())).thenReturn(new Atividade());
        when(atividadeMapper.toDto(any())).thenReturn(dto);

        var result = atividadeService.criar(dto, usuarioId);

        assertThat(result).isNotNull();
        verify(atividadeRepo).save(any());
    }

    @Test
    @DisplayName("criar deve atualizar situacao se nao iniciado")
    void criarAtualizaSituacao() {
        Long mapaId = 10L;
        String usuarioId = "user1";
        AtividadeDto dto = new AtividadeDto();
        dto.setMapaCodigo(mapaId);

        Unidade unidade = new Unidade();
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioId);
        unidade.setTitular(usuario);

        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);

        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        subprocesso.setProcesso(processo);
        subprocesso.setMapa(mapa);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade());
        when(atividadeRepo.save(any())).thenReturn(new Atividade());
        when(atividadeMapper.toDto(any())).thenReturn(dto);

        atividadeService.criar(dto, usuarioId);

        assertThat(subprocesso.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(subprocessoRepo).save(subprocesso);
    }

    @Test
    @DisplayName("criar falha se subprocesso nao encontrado")
    void criarSubprocessoNaoEncontrado() {
        Long mapaId = 10L;
        AtividadeDto dto = new AtividadeDto();
        dto.setMapaCodigo(mapaId);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.criar(dto, "user"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("criar falha se usuario nao encontrado")
    void criarUsuarioNaoEncontrado() {
        Long mapaId = 10L;
        AtividadeDto dto = new AtividadeDto();
        dto.setMapaCodigo(mapaId);
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);
        sp.setMapa(mapa);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(sp));
        when(usuarioRepo.findById("user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atividadeService.criar(dto, "user"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("criar deve lançar exceção se usuario não for titular")
    void criarNegado() {
        Long mapaId = 10L;
        String usuarioId = "user1";
        AtividadeDto dto = new AtividadeDto();
        dto.setMapaCodigo(mapaId);

        Unidade unidade = new Unidade();
        Usuario titular = new Usuario();
        titular.setTituloEleitoral("outro");
        unidade.setTitular(titular);

        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioId);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> atividadeService.criar(dto, usuarioId))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("atualizar deve atualizar e retornar dto")
    void atualizar() {
        Long id = 1L;
        Long mapaId = 100L;
        AtividadeDto dto = new AtividadeDto();
        dto.setDescricao("Nova desc");

        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);

        Atividade atividade = new Atividade();
        atividade.setDescricao("Velha desc");
        atividade.setMapa(mapa);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
        when(atividadeMapper.toEntity(dto)).thenReturn(new Atividade(null, "Nova desc"));
        when(atividadeRepo.save(any())).thenReturn(atividade);
        when(atividadeMapper.toDto(any())).thenReturn(dto);

        var result = atividadeService.atualizar(id, dto);

        assertThat(result.getDescricao()).isEqualTo("Nova desc");
    }

    @Test
    @DisplayName("atualizar falha se nao encontrado")
    void atualizarNaoEncontrado() {
        when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> atividadeService.atualizar(1L, new AtividadeDto()))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("excluir deve remover atividade e conhecimentos")
    void excluir() {
        Long id = 1L;
        Long mapaId = 100L;

        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);

        Atividade atividade = new Atividade();
        atividade.setCodigo(id);
        atividade.setMapa(mapa);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
        when(conhecimentoRepo.findByAtividadeCodigo(id)).thenReturn(List.of());

        atividadeService.excluir(id);

        verify(conhecimentoRepo).deleteAll(any());
        verify(atividadeRepo).delete(atividade);
    }

    @Test
    @DisplayName("excluir falha se nao encontrado")
    void excluirNaoEncontrado() {
        when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> atividadeService.excluir(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("listarConhecimentos deve retornar lista")
    void listarConhecimentos() {
        Long id = 1L;
        when(atividadeRepo.existsById(id)).thenReturn(true);
        when(conhecimentoRepo.findByAtividadeCodigo(id)).thenReturn(List.of(new Conhecimento()));
        when(conhecimentoMapper.toDto(any())).thenReturn(new ConhecimentoDto());

        var result = atividadeService.listarConhecimentos(id);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("listarConhecimentos falha se atividade nao existe")
    void listarConhecimentosNaoEncontrada() {
        when(atividadeRepo.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> atividadeService.listarConhecimentos(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("criarConhecimento deve salvar")
    void criarConhecimento() {
        Long id = 1L;
        Long mapaId = 100L;
        ConhecimentoDto dto = new ConhecimentoDto();

        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);

        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(atividadeRepo.findById(id)).thenReturn(Optional.of(atividade));
        when(conhecimentoMapper.toEntity(dto)).thenReturn(new Conhecimento());
        when(conhecimentoRepo.save(any())).thenReturn(new Conhecimento());
        when(conhecimentoMapper.toDto(any())).thenReturn(dto);

        var result = atividadeService.criarConhecimento(id, dto);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("criarConhecimento falha se atividade nao existe")
    void criarConhecimentoAtividadeNaoEncontrada() {
        when(atividadeRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> atividadeService.criarConhecimento(1L, new ConhecimentoDto()))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("atualizarConhecimento deve atualizar se pertencer a atividade")
    void atualizarConhecimento() {
        Long ativId = 1L;
        Long conId = 2L;
        Long mapaId = 100L;
        ConhecimentoDto dto = new ConhecimentoDto();
        dto.setDescricao("Novo");

        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);

        Atividade atividade = new Atividade();
        atividade.setCodigo(ativId);
        atividade.setMapa(mapa);

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setAtividade(atividade);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(conhecimentoRepo.findById(conId)).thenReturn(Optional.of(conhecimento));
        when(conhecimentoMapper.toEntity(dto)).thenReturn(new Conhecimento("Novo", atividade));
        when(conhecimentoRepo.save(any())).thenReturn(conhecimento);
        when(conhecimentoMapper.toDto(any())).thenReturn(dto);

        var result = atividadeService.atualizarConhecimento(ativId, conId, dto);

        assertThat(result.getDescricao()).isEqualTo("Novo");
    }

    @Test
    @DisplayName("atualizarConhecimento falha se nao pertence a atividade")
    void atualizarConhecimentoErrado() {
        Long ativId = 1L;
        Long conId = 2L;

        Atividade atividadeOutra = new Atividade();
        atividadeOutra.setCodigo(99L);

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setAtividade(atividadeOutra);

        when(conhecimentoRepo.findById(conId)).thenReturn(Optional.of(conhecimento));

        assertThatThrownBy(
                        () ->
                                atividadeService.atualizarConhecimento(
                                        ativId, conId, new ConhecimentoDto()))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("excluirConhecimento deve excluir se pertencer a atividade")
    void excluirConhecimento() {
        Long ativId = 1L;
        Long conId = 2L;
        Long mapaId = 100L;

        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaId);

        Atividade atividade = new Atividade();
        atividade.setCodigo(ativId);
        atividade.setMapa(mapa);

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setAtividade(atividade);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(conhecimentoRepo.findById(conId)).thenReturn(Optional.of(conhecimento));

        atividadeService.excluirConhecimento(ativId, conId);

        verify(conhecimentoRepo).delete(conhecimento);
    }

    @Test
    @DisplayName("excluirConhecimento falha se nao pertence a atividade")
    void excluirConhecimentoErrado() {
        Long ativId = 1L;
        Long conId = 2L;

        Atividade atividadeOutra = new Atividade();
        atividadeOutra.setCodigo(99L);

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setAtividade(atividadeOutra);

        when(conhecimentoRepo.findById(conId)).thenReturn(Optional.of(conhecimento));

        assertThatThrownBy(() -> atividadeService.excluirConhecimento(ativId, conId))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
