const semver = require('semver');
const packageJson = require('./package.json');

const requiredVersion = packageJson.engines.node;

function checkNodeVersion() {
  const currentVersion = process.version;
  
  if (!semver.satisfies(currentVersion, requiredVersion)) {
    console.error(
      `\n当前 Node.js 版本 ${currentVersion} 不满足要求的 ${requiredVersion} 版本范围。`
    );
    console.error('请切换到正确的 Node.js 版本后再运行项目。\n');
    process.exit(1);
  } else {
    console.log(`✓ 当前 Node.js 版本 ${currentVersion} 满足要求`);
  }
}

checkNodeVersion();