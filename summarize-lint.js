import fs from 'fs';

const data = JSON.parse(fs.readFileSync('lint-results.json', 'utf8'));

const summary = data
  .filter(file => file.messages.length > 0)
  .map(file => ({
    file: file.filePath.replace(process.cwd(), '.'),
    errors: file.errorCount,
    warnings: file.warningCount,
    fixableErrors: file.fixableErrorCount,
    fixableWarnings: file.fixableWarningCount,
    messages: file.messages.map(m => ({
      line: m.line,
      rule: m.ruleId,
      severity: m.severity,
      message: m.message
    }))
  }));

console.log(JSON.stringify(summary, null, 2));
