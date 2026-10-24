#!/usr/bin/env node
'use strict';

const assert = require('assert');
const { detect, detectRequired } = require('./detect-jar-meta.js');

const cases = [
  // Current MultiLoader naming
  ['reconstructedwands-fabric-1.20.1-2.0.0.jar', 'fabric', '1.20.1'],
  ['reconstructedwands-forge-1.20.1-2.0.0.jar', 'forge', '1.20.1'],
  ['reconstructedwands-fabric-1.21.1-2.0.0.jar', 'fabric', '1.21.1'],
  ['reconstructedwands-neoforge-1.21.1-2.0.0.jar', 'neoforge', '1.21.1'],
  ['reconstructedwands-fabric-26.2-2.0.0.jar', 'fabric', '26.2'],
  ['reconstructedwands-neoforge-26.2-2.0.0.jar', 'neoforge', '26.2'],
  ['reconstructedwands-fabric-26.3-2.0.0.jar', 'fabric', '26.3'],
  ['reconstructedwands-neoforge-26.3-2.0.0.jar', 'neoforge', '26.3'],
  ['releases/reconstructedwands-neoforge-26.3-2.0.0.jar', 'neoforge', '26.3'],
  // Calendar versions with a patch number
  ['reconstructedwands-fabric-26.1.2-2.0.0.jar', 'fabric', '26.1.2'],
  // Older names (mod version first)
  ['reconstructedwands-1.1.5-forge-1.20.1.jar', 'forge', '1.20.1'],
  ['reconstructedwands-1.1.5-neoforge-1.21.1.jar', 'neoforge', '1.21.1'],
];

let failed = 0;
for (const [name, loader, gv] of cases) {
  const got = detect(name);
  try {
    assert.strictEqual(got.loader, loader, `${name} loader`);
    assert.strictEqual(got.gv, gv, `${name} gv`);
    console.log('ok', name, '->', got);
  } catch (e) {
    failed += 1;
    console.error('FAIL', name, 'got', got, e.message);
  }
}

// Unparseable names must fail instead of falling back to a default Minecraft version.
try {
  detectRequired('reconstructedwands-fabric-mystery.jar');
  failed += 1;
  console.error('FAIL expected throw for mystery jar');
} catch (e) {
  console.log('ok refuses default fallback:', e.message.split('\n')[0]);
}

if (failed) {
  console.error(`${failed} case(s) failed`);
  process.exit(1);
}
console.log('all detect-jar-meta cases passed');
