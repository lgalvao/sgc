const { execSync } = require('child_process');
try {
  const result = execSync('grep -rnw "backend/src/main/java" -e "/impactos-mapa"', { encoding: 'utf8' });
  console.log(result);
} catch (e) {
  console.error(e.message);
}
