import fs from 'fs';
import path from 'path';

const TARGET_EXTS = ['.ts', '.js', '.tsx', '.jsx', '.java', '.kt', '.kts'];
const IGNORE_DIRS = ['node_modules', 'build', 'dist', '.git', '.gradle', '.idea', 'target'];

function showHelp() {
  console.log(`
Usage: node clean-ai-comments.mjs [options]

Scans the codebase for non-documentation comments.

Options:
  --dry-run      Show what would be removed without modifying files.
  --json         Output ALL found comments (unfiltered) in JSON format with context for AI evaluation.
  --help, -h     Show this help message.

AI Heuristics:
  Automatically removes conversational "agent-speak" in English and Portuguese (PT-BR) when run without --json.
  Patterns: "I will", "Let's", "Na verdade", "Vou simular", "Parece que", etc.
  `);
}

const AI_MARKERS = [
  /^\s*\/\/\s*(i will|let's|actually|wait,|it seems|i need to|maybe|oops|i'll|assuming|we should|we want|but logic|so it should|we can force|it seems i|i missed|maybe add|i'm|forgot to)\b/i,
  /\b(i need to|i will|let's|i'll|we don't|we want|it seems|keeping this|assuming|actually|wait,|but logic might|we can't|we should|i can|we would|we simulate|i missed|we can force|so it should|maybe add|i'm|oops|forgot to|wait,)\b/i,
  /^\s*\/\/\s*(eu vou|vou|vamos|na verdade|parece que|preciso|esqueci|ops|espere|talvez|então deve|então deve ser|vou agora|vou atualizar|vou simular)\b/i,
  /\b(eu vou|vou|vamos|na verdade|parece que|preciso|esqueci de|pelo que parece|vou atualizar|vou remover|vou simular|então deve|então deve ser|então deve estar|então deve ter|então deveria)\b/i
];

function isAiCommentHeuristic(text) {
  if (text.match(/^\/\/\s*[-=]{5,}/)) return false; 
  return AI_MARKERS.some(regex => regex.test(text));
}

async function run() {
  const args = process.argv.slice(2);
  const dryRun = args.includes('--dry-run');
  const jsonOutput = args.includes('--json');

  if (args.includes('--help') || args.includes('-h')) {
    showHelp();
    return;
  }

  const allComments = [];
  processDirectory('.', (filePath, commentData) => {
    allComments.push({ file: filePath, ...commentData });
  }, dryRun, jsonOutput);

  if (jsonOutput) {
    process.stdout.write(JSON.stringify(allComments, null, 2) + '\n');
  } else {
    console.log('Done.');
  }
}

function processDirectory(dir, onComment, dryRun, jsonOutput) {
  const files = fs.readdirSync(dir, { withFileTypes: true });
  for (const file of files) {
    const fullPath = path.join(dir, file.name);
    if (file.isDirectory()) {
      if (IGNORE_DIRS.includes(file.name)) continue;
      processDirectory(fullPath, onComment, dryRun, jsonOutput);
    } else if (file.isFile() && TARGET_EXTS.includes(path.extname(file.name))) {
      processFile(fullPath, onComment, dryRun, jsonOutput);
    }
  }
}

function getContext(lines, startIdx, length, contextLines = 3) {
  const start = Math.max(0, startIdx - contextLines);
  const end = Math.min(lines.length, startIdx + length + contextLines);
  return lines.slice(start, end).join('\n');
}

function processFile(filePath, onComment, dryRun, jsonOutput) {
  let content = fs.readFileSync(filePath, 'utf8');
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
    } else if (isAiCommentHeuristic(text)) {
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
        } else if (isAiCommentHeuristic(text)) {
          currentGroupIndices.forEach(idx => linesToDelete.add(idx));
        }
      }
      currentGroup = [];
      currentGroupIndices = [];
    }
  }
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
    } else if (isAiCommentHeuristic(text)) {
      currentGroupIndices.forEach(idx => linesToDelete.add(idx));
    }
  }

  if (!jsonOutput && linesToDelete.size > 0) {
    if (dryRun) {
      console.log(`[MATCH] ${filePath} (${linesToDelete.size} lines)`);
    } else {
      fs.writeFileSync(filePath, lines.filter((_, idx) => !linesToDelete.has(idx)).join('\n'));
      console.log(`Cleaned ${linesToDelete.size} lines in ${filePath}`);
    }
  }
}

run().catch(console.error);
