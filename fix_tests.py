import sys
import re
import os

def process_file(file_path):
    if not os.path.exists(file_path): return
    with open(file_path, "r") as f:
        text = f.read()

    text = text.replace("subprocessoRepo.buscarPorCodigoComMapaEAtividades", "subprocessoService.buscarSubprocesso")
    
    if "private SubprocessoService subprocessoService;" not in text and "SubprocessoRepo subprocessoRepo;" in text:
        text = text.replace("private SubprocessoRepo subprocessoRepo;", "private SubprocessoRepo subprocessoRepo;\n    @Mock\n    private SubprocessoService subprocessoService;")

    # Replace .thenReturn(Optional.of(X)) with .thenReturn(X) ONLY if they follow subprocessoService.buscarSubprocesso
    # Wait, simple regex:
    text = re.sub(r'when\(subprocessoService\.buscarSubprocesso\((.*?)\)\)\.thenReturn\(Optional\.of\((.*?)\)\);', r'when(subprocessoService.buscarSubprocesso(\1)).thenReturn(\2);', text)

    before_each_code = """
    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(subprocessoService.obterUnidadeLocalizacao(org.mockito.ArgumentMatchers.any(Subprocesso.class)))
                .thenAnswer(inv -> {
                    Subprocesso sp = inv.getArgument(0);
                    return sp.getLocalizacaoAtual() != null ? sp.getLocalizacaoAtual() : sp.getUnidade();
                });
    }
"""
    if "@BeforeEach" not in text and "@Test" in text:
        test_idx = text.find("@Test")
        text = text[:test_idx] + before_each_code + "\n    " + text[test_idx:]

    with open(file_path, "w") as f:
        f.write(text)

process_file("backend/src/test/java/sgc/subprocesso/service/SubprocessoTransicaoServiceTest.java")
process_file("backend/src/test/java/sgc/subprocesso/service/SubprocessoTransicaoServiceExtraCoverageTest.java")
print("Done!")
