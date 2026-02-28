import fs from 'fs';
import path from 'path';

const TARGET_EXTS = ['.ts', '.js', '.tsx', '.jsx', '.java', '.kt', '.kts'];
const IGNORE_DIRS = ['node_modules', 'build', 'dist', '.git', '.gradle', '.idea', 'target'];

function showHelp() {
  console.log(`
Usage: node clean-ai-comments.mjs [options]

Scans the codebase for non-documentation comments and redundant markers.

Options:
  --dry-run      Show what would be removed without modifying files.
  --json         Output ALL found comments (unfiltered) in JSON format with context for AI evaluation.
  --keep-blank   Do not collapse redundant blank lines (default is to collapse 3+ empty lines).
  --help, -h     Show this help message.

Cleanup Heuristics:
  - AI "agent-speak" in EN/PT (e.g., "I will", "Let's", "Na verdade", "Vou simular").
  - BDD/AAA markers (e.g., "Given", "When", "Then", "Arrange", "Act", "Assert").
  - Redundant numbered steps (e.g., "// 1. Criar unidade", "// 2. Validar").
  - Decorative section markers (e.g., "// --- ETAPA ---").
  - Obvious dependency labels in build files (e.g., "// Spring", "// Lombok").
  `);
}

const AI_MARKERS = [
  // English Patterns
  /\b(i will|let's|actually|wait,|it seems|i need to|maybe|oops|i'll|assuming|we should|we want|but logic|so it should|we can force|it seems i|i missed|maybe add|i'm|forgot to|keeping this|we don't|we would|we simulate|i can|we can't|should be .* now|toggle again|watcher should run|verify if|verification|pattern [0-9]+|case [0-9]+)\b/i,
  // Portuguese Patterns
  /\b(eu vou|vou|vamos|na verdade|parece que|preciso|esqueci de?|pelo que parece|vou atualizar|vou remover|vou simular|então deve|então deve (ser|estar|ter)|então deveria|ops|espere|talvez|vou agora|dado|quando|então|passo [0-9]+|etapa [0-9]+)\b/i
];

const REDUNDANT_MARKERS = [
  // BDD / AAA (English & Portuguese)
  /^\s*\/\/\s*(Given|When|Then|And|Arrange|Act|Assert|Setup|Dado|Quando|Então|E)(:.*)?$/i,
  // Numbered steps with obvious actions
  /^\s*\/\/\s*[0-9]+\.\s*(Arrange|Act|Assert|Criar|Setup|Vincular|Requisito|Limpar|Recarregar|Buscar|Validar|Tentar|Ação|Verificar|Simular|Passo|Etapa).*$/i,
  // Decorative separators
  /^\s*\/\/\s*[-=*]{3,}.*[-=*]{3,}$/,
  // Obvious build file group labels (only if the line is exactly the label)
  /^\s*\/\/\s*(Spring|Lombok|Banco de Dados|Relatórios|Segurança|Testes|Testes de Mutação|Documentação da API|Analise Estatica|Bootstrap e configuração|Exceções \(maioria simples\)|Mocks de teste|Enums simples sem lógica de negócio|Classes geradas pelo MapStruct)$/i
];

function isUselessComment(text) {
  // Ignore standard documentation or "safety" separators
  if (text.match(/^\/\/\s*[-=]{10,}/)) return false; 
  
  // Check AI conversational speak
  if (AI_MARKERS.some(regex => regex.test(text))) return true;
  
  // Check redundant markers
  if (REDUNDANT_MARKERS.some(regex => regex.test(text))) return true;

  return false;
}

async function run() {
  const args = process.argv.slice(2);
  const dryRun = args.includes('--dry-run');
  const jsonOutput = args.includes('--json');
  const keepBlank = args.includes('--keep-blank');

  if (args.includes('--help') || args.includes('-h')) {
    showHelp();
    return;
  }

  const allComments = [];
  processDirectory('.', (filePath, commentData) => {
    allComments.push({ file: filePath, ...commentData });
  }, dryRun, jsonOutput, keepBlank);

  if (jsonOutput) {
    process.stdout.write(JSON.stringify(allComments, null, 2) + '\n');
  } else {
    console.log('Done.');
  }
}

function processDirectory(dir, onComment, dryRun, jsonOutput, keepBlank) {
  const files = fs.readdirSync(dir, { withFileTypes: true });
  for (const file of files) {
    const fullPath = path.join(dir, file.name);
    if (file.isDirectory()) {
      if (IGNORE_DIRS.includes(file.name)) continue;
      processDirectory(fullPath, onComment, dryRun, jsonOutput, keepBlank);
    } else if (file.isFile() && TARGET_EXTS.includes(path.extname(file.name))) {
      processFile(fullPath, onComment, dryRun, jsonOutput, keepBlank);
    }
  }
}

function getContext(lines, startIdx, length, contextLines = 3) {
  const start = Math.max(0, startIdx - contextLines);
  const end = Math.min(lines.length, startIdx + length + contextLines);
  return lines.slice(start, end).join('\n');
}

function processFile(filePath, onComment, dryRun, jsonOutput, keepBlank) {
  let content = fs.readFileSync(filePath, 'utf8');
  if (!content) return;
  
  let lines = content.split('\n');
  let linesToDelete = new Set();

  // 1. Block comments
  const blockRegex = /\/\*(?!\*)[\s\S]*?\*\//g;
  let match;
  while ((match = blockRegex.exec(content)) !== null) {
    const text = match[0];
    const startLine = content.substring(0, match.index).split('\n').length;
    const count = text.split('\n').length;

    if (jsonOutput) {
      onComment(filePath, { 
        type: 'block', 
        line: startLine, 
        length: count, 
        text,
        context: getContext(lines, startLine - 1, count)
      });
    } else if (isUselessComment(text)) {
      for (let i = 0; i < count; i++) linesToDelete.add(startLine - 1 + i);
    }
  }

  // 2. Line comments
  let currentGroup = [];
  let currentGroupIndices = [];
  for (let i = 0; i < lines.length; i++) {
    const trimmed = lines[i].trim();
    if (trimmed.startsWith('//') && !trimmed.startsWith('///')) {
      currentGroup.push(trimmed);
      currentGroupIndices.push(i);
    } else {
      if (currentGroup.length > 0) {
        const text = currentGroup.join('\n');
        if (jsonOutput) {
          onComment(filePath, { 
            type: 'line', 
            line: currentGroupIndices[0] + 1, 
            length: currentGroup.length, 
            text,
            context: getContext(lines, currentGroupIndices[0], currentGroup.length)
          });
        } else if (isUselessComment(text)) {
          currentGroupIndices.forEach(idx => linesToDelete.add(idx));
        }
      }
      currentGroup = [];
      currentGroupIndices = [];
    }
  }
  
  // Handle last group
  if (currentGroup.length > 0) {
    const text = currentGroup.join('\n');
    if (jsonOutput) {
      onComment(filePath, { 
        type: 'line', 
        line: currentGroupIndices[0] + 1, 
        length: currentGroup.length, 
        text,
        context: getContext(lines, currentGroupIndices[0], currentGroup.length)
      });
    } else if (isUselessComment(text)) {
      currentGroupIndices.forEach(idx => linesToDelete.add(idx));
    }
  }

  if (!jsonOutput) {
    let newLines = lines.filter((_, idx) => !linesToDelete.has(idx));
    
    // Collapse redundant blank lines (more than 2)
    if (!keepBlank) {
      let collapsedLines = [];
      let consecutiveBlanks = 0;
      for (const line of newLines) {
        if (line.trim() === '') {
          consecutiveBlanks++;
        } else {
          consecutiveBlanks = 0;
        }
        
        if (consecutiveBlanks <= 2) {
          collapsedLines.push(line);
        }
      }
      newLines = collapsedLines;
    }

    if (newLines.length !== lines.length) {
      if (dryRun) {
        console.log(`[MATCH] ${filePath} (${lines.length - newLines.length} lines removed)`);
      } else {
        fs.writeFileSync(filePath, newLines.join('\n'));
        console.log(`Cleaned ${lines.length - newLines.length} lines in ${filePath}`);
      }
    }
  }
}

run().catch(console.error);
