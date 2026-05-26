const fs = require('fs');
const path = require('path');

const covPath = path.join(__dirname, '../../frontend/coverage/coverage-final.json');
if (!fs.existsSync(covPath)) {
  console.error('Coverage file not found at:', covPath);
  process.exit(1);
}

const cov = JSON.parse(fs.readFileSync(covPath));
const branchStats = [];
let globalTotal = 0;
let globalCovered = 0;

for (const [file, data] of Object.entries(cov)) {
  let fileTotal = 0;
  let fileCovered = 0;

  for (const b of Object.values(data.b)) {
    fileTotal += b.length;
    fileCovered += b.filter(count => count > 0).length;
  }

  globalTotal += fileTotal;
  globalCovered += fileCovered;

  const missingBranches = fileTotal - fileCovered;
  if (missingBranches > 0) {
    branchStats.push({
      file: file.replace(/.*\/frontend\//, ''),
      missingBranches,
      fileTotal,
      fileCovered,
      percentage: fileTotal ? ((fileCovered / fileTotal) * 100).toFixed(2) : '100.00'
    });
  }
}

branchStats.sort((a, b) => b.missingBranches - a.missingBranches);

const globalPercentage = globalTotal ? ((globalCovered / globalTotal) * 100).toFixed(2) : '0.00';
const target90 = Math.ceil(globalTotal * 0.9);
const gap = target90 - globalCovered;

console.log(`Global Branch Coverage: ${globalPercentage}% (${globalCovered}/${globalTotal})`);
console.log(`Target (90%): ${target90}. Gap: ${gap} branches.`);
console.log('\nTop 15 files with missing branches:');
branchStats.slice(0, 15).forEach(stat => {
  console.log(`${stat.file}: ${stat.missingBranches} missing branches (${stat.percentage}% covered)`);
});
