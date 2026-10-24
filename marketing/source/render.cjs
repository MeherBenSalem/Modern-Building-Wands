// Renders the listing artwork in marketing/images/ from real in-game captures, the mod's own textures and the bundled
// OFL fonts. Usage (from the repository root): NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility table of curseforge/description.md).
const RELEASE = { version: '2.0', name: 'The Architect Update' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Fabric', 'Forge'] },
  { minecraft: '1.21.1', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.2', loaders: ['Fabric', 'NeoForge'] },
  { minecraft: '26.3', loaders: ['Fabric', 'NeoForge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const textures = path.join(root, '26.3', 'common', 'src', 'main', 'resources', 'assets', 'reconstructedwands', 'textures', 'item');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));
const tex = (file) => url(path.join(textures, file + '.png'));

// Mode colours match WandHud.MODE_COLORS; tier stats match WandTier's defaults.
const MODES = {
  construction: { name: 'Construction', color: '#7fe8ff', core: null },
  angel: { name: 'Angel', color: '#ffe082', core: 'core_angel' },
  destruction: { name: 'Destruction', color: '#ff8a65', core: 'core_destruction' },
  exchange: { name: 'Exchange', color: '#b388ff', core: 'core_exchange' },
  architect: { name: 'Architect', color: '#80cbc4', core: 'core_architect' },
};
const TIERS = [
  { name: 'Stone', file: 'stone_wand', color: '#b8b8b8', build: '27', reach: '2', cores: false },
  { name: 'Iron', file: 'iron_wand', color: '#e4e9ee', build: '128', reach: '4', cores: true },
  { name: 'Diamond', file: 'diamond_wand', color: '#5ff0e2', build: '1,024', reach: '16', cores: true },
  { name: 'Netherite', file: 'netherite_wand', color: '#c9b3bf', build: '2,048', reach: '32', cores: true },
  { name: 'Infinity', file: 'infinity_wand', color: '#d08cff', build: '2,048', reach: '64', cores: true },
];

const css = `
@font-face { font-family: WandDisplay; src: url('${url(path.join(fonts, 'lilita-one.woff2'))}'); }
@font-face { font-family: WandBody; font-weight: 600; src: url('${url(path.join(fonts, 'nunito-600.woff2'))}'); }
@font-face { font-family: WandBody; font-weight: 800; src: url('${url(path.join(fonts, 'nunito-800.woff2'))}'); }
:root { --text:#e6f3f8; --dim:#93aab8; --cyan:#7fe8ff; --amber:#ffc66d; --bg:#08111d; }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--bg); color: var(--text); font-family: WandBody, sans-serif; font-weight: 600; }
.stage { position: relative; overflow: hidden; background: radial-gradient(ellipse at 30% 40%, #12304a 0%, #08111d 70%); }
.display { font-family: WandDisplay, sans-serif; letter-spacing: .01em; }
.title { background: linear-gradient(180deg, #ffffff 0%, #d9fbff 38%, #7fe8ff 72%, #3fb4d6 100%);
         -webkit-background-clip: text; background-clip: text; color: transparent; }
.glow { filter: drop-shadow(0 0 22px rgba(127,232,255,.35)) drop-shadow(0 3px 0 rgba(0,0,0,.6)); }
.kicker { font-weight: 800; color: var(--amber); letter-spacing: .2em; font-size: 22px; text-transform: uppercase; }
.rule { height: 3px; border-radius: 2px; background: linear-gradient(90deg, rgba(127,232,255,0), rgba(127,232,255,.95) 15%, rgba(255,198,109,.85) 70%, rgba(255,198,109,0)); }
.cube { display: inline-block; width: 13px; height: 13px; border: 2px solid var(--cyan); margin: 0 14px 2px 2px; box-shadow: 0 0 10px rgba(127,232,255,.8); }
.frame { position: relative; border-radius: 14px; overflow: hidden; border: 2px solid rgba(127,232,255,.55);
         box-shadow: 0 0 0 1px rgba(0,0,0,.7), 0 30px 80px rgba(0,0,0,.7), 0 0 70px rgba(127,232,255,.16); background: #000; }
.frame > img { display: block; width: 100%; }
.grid { position: absolute; inset: 0; pointer-events: none;
        background-image: linear-gradient(rgba(127,232,255,.07) 1px, transparent 1px), linear-gradient(90deg, rgba(127,232,255,.07) 1px, transparent 1px),
                          linear-gradient(rgba(127,232,255,.035) 1px, transparent 1px), linear-gradient(90deg, rgba(127,232,255,.035) 1px, transparent 1px);
        background-size: 120px 120px, 120px 120px, 24px 24px, 24px 24px; }
.vignette { position: absolute; inset: 0; background: radial-gradient(ellipse at center, rgba(0,0,0,0) 40%, rgba(0,0,0,.78) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(14px) brightness(.34) saturate(1.15); }
.pill { display: inline-block; border-radius: 999px; border: 2px solid rgba(127,232,255,.6); padding: 7px 20px 6px; margin-right: 12px;
        font-weight: 800; font-size: 20px; letter-spacing: .08em; color: #e8fbff; background: rgba(8,17,29,.6); }
.label { position: absolute; font-weight: 800; font-size: 21px; letter-spacing: .04em; color: #e8fbff;
         background: rgba(8,17,29,.9); border: 2px solid rgba(127,232,255,.8); border-radius: 10px; padding: 8px 16px 7px; white-space: nowrap;
         box-shadow: 0 8px 26px rgba(0,0,0,.6); z-index: 2; }
.label small { display: block; font-weight: 600; letter-spacing: 0; font-size: 17px; color: var(--dim); margin-top: 1px; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 32px; margin: 0 0 15px; font-size: 27px; line-height: 1.32; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 2px; top: 10px; width: 10px; height: 10px; border: 2px solid var(--cyan);
                       box-shadow: 0 0 8px rgba(127,232,255,.7); }
.stats { display: flex; margin-top: 22px; border-radius: 14px; overflow: hidden; border: 2px solid rgba(127,232,255,.35); background: rgba(8,17,29,.85);
         box-shadow: 0 18px 50px rgba(0,0,0,.55); }
.stats > div { flex: 1; padding: 14px 24px 12px; border-left: 2px solid rgba(127,232,255,.15); }
.stats > div:first-child { border-left: 0; }
.stats .t { font-weight: 800; font-size: 16px; letter-spacing: .16em; color: var(--dim); text-transform: uppercase; }
.stats .v { font-family: WandDisplay, sans-serif; font-size: 34px; color: #e8fbff; margin-top: 2px; white-space: nowrap; }
b.hi { color: #ffffff; font-weight: 800; }
img.px { image-rendering: pixelated; display: block; }
kbd { font-family: WandBody, sans-serif; font-weight: 800; font-size: .82em; color: #e8fbff; background: rgba(127,232,255,.12);
      border: 2px solid rgba(127,232,255,.45); border-bottom-width: 4px; border-radius: 8px; padding: 0 9px; white-space: nowrap; }
`;

// Wireframe cubes drifting in the background: the in-game preview outline, as decoration. Deterministic.
const wireframes = (count = 10, seed = 5) => `<canvas class="wires" style="position:absolute;inset:0;width:100%;height:100%"></canvas><script>
(() => { const c = document.currentScript.previousElementSibling; const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = ${seed};
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  for (let i = 0; i < ${count}; i++) { const x = rnd() * c.width, y = rnd() * c.height, z = 18 + rnd() * 46, d = z * .45, a = .05 + rnd() * .12;
    g.strokeStyle = 'rgba(127,232,255,' + a + ')'; g.lineWidth = 2; g.shadowColor = 'rgba(127,232,255,.6)'; g.shadowBlur = 8;
    g.strokeRect(x, y, z, z); g.strokeRect(x + d, y - d, z, z);
    g.beginPath(); for (const [px, py] of [[0, 0], [z, 0], [0, z], [z, z]]) { g.moveTo(x + px, y + py); g.lineTo(x + px + d, y + py - d); } g.stroke(); } })();
</script>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div></body></html>`;

const item = (file, size, color, extra = '') => `<img class="px" src="${tex(file)}" style="width:${size}px;height:${size}px;
  filter:drop-shadow(0 0 ${Math.round(size / 6)}px ${color}99) drop-shadow(0 ${Math.max(2, Math.round(size / 24))}px 0 rgba(0,0,0,.55));${extra}">`;

const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:22px">
    <div class="rule" style="width:250px;transform:scaleX(-1)"></div>
    ${item('diamond_wand', 56, '#5ff0e2')}
    <div class="display title glow" style="font-size:58px;white-space:nowrap">${title}</div>
    ${item('core_architect', 56, '#80cbc4')}
    <div class="rule" style="width:250px"></div>
  </div>`, true);

const divider = () => page(1400, 40, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:18px">
    <div class="rule" style="width:420px;transform:scaleX(-1);opacity:.7"></div>
    ${Object.values(MODES).map((m) => `<span style="display:inline-block;width:13px;height:13px;border:2px solid ${m.color};box-shadow:0 0 10px ${m.color}"></span>`).join('')}
    <div class="rule" style="width:420px;opacity:.7"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 960, backdrop = 'backdrop.jpg' }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source(backdrop)}')"></div>
  <div class="grid"></div><div class="vignette"></div>${wireframes()}
  <div style="position:absolute;left:84px;top:0;bottom:0;width:${width - artWidth - 160}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"><span class="cube"></span>${kicker}</div>
    <div class="display title glow" style="font-size:70px;line-height:1.02;margin:16px 0 22px">${title}</div>
    <div class="rule" style="width:78%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:64px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in 1920x1080 pixels) of a capture, scaled to W pixels wide.
const region = (file, x, y, w, h, W) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * 1920 / w}px;left:${-x * W / w}px;top:${-y * W / w}px;image-rendering:${w < 900 ? 'pixelated' : 'auto'}"></div>`;
const framed = (inner, style = '') => `<div class="frame" style="${style}">${inner}</div>`;
const label = (text, style, small = '') => `<div class="label" style="${style}">${text}${small ? `<small>${small}</small>` : ''}</div>`;
const stats = (items) => `<div class="stats">${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="t">${t}</div><div class="v">${v}</div></div>`).join('')}</div>`;

const versionText = VERSIONS.map((v) => v.minecraft).join(' · ');

// An excerpt of the server.json the mod writes on first start (comments and values as generated).
const configPanel = () => {
  const c = (text) => `<span style="color:#6f8796">${text}</span>`;
  const k = (text) => `<span style="color:#7fe8ff">"${text}"</span>`;
  const v = (text) => `<span style="color:#ffc66d">${text}</span>`;
  const lines = [
    c('// RE:Constructed Wands 2.0: server rules. Clients get the server’s copy when they join.'),
    c('// Reload with /wands reload. Durability changes need a restart.'),
    '{',
    `  ${k('tiers')}: {`,
    `    ${k('diamond')}: { ${k('durability')}: ${v('1561')}, ${k('construction')}: ${v('1024')}, ${k('angel')}: ${v('16')},`,
    `                 ${k('destruction')}: ${v('81')}, ${k('exchange')}: ${v('81')}, ${k('cores')}: ${v('true')} },`,
    `    ${c('…')}`,
    '  },',
    `  ${c('// No block further than this from the player is ever changed (0 = no limit).')}`,
    `  ${k('max_range')}: ${v('256')},`,
    `  ${k('undo_depth')}: ${v('10')},`,
    `  ${k('modes')}: { ${k('angel')}: ${v('true')}, ${k('destruction')}: ${v('true')}, ${k('exchange')}: ${v('true')}, ${k('architect')}: ${v('true')} },`,
    `  ${k('sources')}: { ${k('shulker_boxes')}: ${v('true')}, ${k('bundles')}: ${v('true')}, ${k('ender_chest')}: ${v('true')},`,
    `               ${k('bound_containers')}: ${v('true')}, ${k('bound_range')}: ${v('64')} },`,
    `  ${k('destruction_drops')}: ${v('true')},`,
    `  ${c('…')}`,
    '}',
  ];
  return `<pre style="margin:0;padding:24px 26px;font-size:15.5px;line-height:1.55;color:#d7e6ee;white-space:pre">${lines.join('\n')}</pre>`;
};
const modeChip = (key) => {
  const m = MODES[key];
  const icon = m.core ? item(m.core, 44, m.color) : item('diamond_wand', 44, m.color);
  return `<div style="display:flex;align-items:center;gap:12px;background:rgba(8,17,29,.88);border:2px solid ${m.color}aa;border-radius:12px;padding:8px 16px 8px 10px">
    ${icon}<span class="display" style="font-size:28px;color:${m.color}">${m.name}</span></div>`;
};

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('build.jpg')}" style="position:absolute;right:-330px;top:-150px;width:1520px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 34%);mask-image:linear-gradient(90deg,transparent 0%,#000 34%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(8,17,29,.97) 0%,rgba(8,17,29,.9) 32%,rgba(8,17,29,.3) 58%,rgba(8,17,29,0) 78%)"></div>
    <div style="position:absolute;inset:0;background:linear-gradient(0deg,rgba(8,17,29,.75) 0%,rgba(8,17,29,0) 32%)"></div>
    <div class="grid" style="-webkit-mask-image:linear-gradient(90deg,#000 0%,#000 40%,transparent 62%);mask-image:linear-gradient(90deg,#000 0%,#000 40%,transparent 62%)"></div>
    <div class="vignette" style="opacity:.55"></div>
    <div style="position:absolute;left:84px;top:58px;width:1200px">
      <div style="display:flex;gap:12px;align-items:flex-end;margin-bottom:6px">
        ${TIERS.map((t, i) => item(t.file, 52 + i * 8, t.color)).join('')}
      </div>
      <div class="kicker" style="margin-top:14px"><span class="cube"></span>${RELEASE.version} · ${RELEASE.name}</div>
      <div class="display title glow" style="font-size:96px;line-height:.95;margin:14px 0 20px;white-space:nowrap">RE:Constructed Wands</div>
      <div class="rule" style="width:640px;margin-bottom:22px"></div>
      <div style="font-size:31px;color:#e8fbff;line-height:1.3;font-weight:800">Build walls, floors and shapes in one click.<br>See it first, undo it after, never lose a block.</div>
      <div style="margin-top:28px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}
        <span style="font-size:22px;color:var(--dim);margin-left:6px;font-weight:800">${versionText}</span></div>
    </div>`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;border-radius:96px;overflow:hidden;background:radial-gradient(circle at 50% 42%,#1d4a6b 0%,#0b1a2b 58%,#060d16 100%)">
      <div class="grid" style="background-size:64px 64px,64px 64px,16px 16px,16px 16px"></div>
      <svg width="512" height="512" style="position:absolute;inset:0">
        <g fill="none" stroke="#7fe8ff" stroke-width="7" stroke-linejoin="round" style="filter:drop-shadow(0 0 12px rgba(127,232,255,.9))" opacity=".9">
          <path d="M268 96 L404 150 L404 286 L268 340 L132 286 L132 150 Z"/><path d="M132 150 L268 204 L404 150"/><path d="M268 204 L268 340"/>
        </g></svg>
      <div style="position:absolute;left:44px;top:120px">${item('diamond_wand', 336, '#5ff0e2')}</div>
    </div>
    <div style="position:absolute;inset:0;border-radius:96px;border:6px solid #7fe8ff;box-shadow:inset 0 0 40px rgba(127,232,255,.35)"></div>`),

  'card-wands': () => card({
    kicker: 'Five wands', title: 'Stone to<br>Infinity',
    points: ['Each tier builds more per click, from 27 blocks to 2,048',
      'Repair in an anvil with the wand’s material; Unbreaking and Mending work',
      'Upgrade a Diamond Wand to Netherite at the smithing table',
      'The <b class="hi">Infinity Wand</b> never breaks', 'Every number is in server.json'],
    art: `<div style="display:flex;justify-content:space-between;align-items:flex-end;padding:0 10px;height:400px">
      ${TIERS.map((t, i) => `<div style="display:flex;flex-direction:column;align-items:center;width:180px">
        <div style="height:${120 + i * 40}px;width:22px;margin-bottom:-24px;border-radius:12px;background:linear-gradient(0deg,${t.color},transparent);filter:blur(6px);opacity:.6"></div>
        ${item(t.file, 144, t.color)}
        <div class="display" style="font-size:30px;color:${t.color};margin-top:10px;text-shadow:0 2px 0 #000">${t.name}</div>
        <div style="font-size:20px;color:var(--dim);font-weight:800">${t.build} blocks</div></div>`).join('')}</div>
      ${stats([['Cores', 'Iron and up'], ['Angel reach', '2–64'], ['Undo', '10 steps'], ['Range', '256']])}` }),

  'card-construction': () => card({
    kicker: 'Construction', title: 'One click,<br>a whole wall',
    points: ['Right-click a face to extend every matching block behind it',
      'The preview shows exactly what the next click places',
      '<kbd>Ctrl</kbd> + scroll sets the size, <kbd>Sneak</kbd> + scroll locks a direction',
      'Mix blocks at random, weighted by your hotbar',
      'Stairs, slabs and logs keep their orientation'],
    art: framed(region('build.jpg', 160, 60, 1600, 900, 960)) }),

  'card-modes': () => card({
    kicker: 'Cores', title: 'Five modes,<br>one wand',
    points: ['<b class="hi" style="color:#ffe082">Angel</b>: place a block in mid-air',
      '<b class="hi" style="color:#ff8a65">Destruction</b>: clear a whole surface, drops to your inventory',
      '<b class="hi" style="color:#b388ff">Exchange</b>: swap a surface for another block, orientation kept',
      '<b class="hi" style="color:#80cbc4">Architect</b>: lines, walls, boxes, circles and rings in two clicks',
      'Craft a core with a wand to install it; switch with the mode wheel'],
    art: `<div style="display:grid;grid-template-columns:1fr 1fr;gap:18px">
      ${[['angel.jpg', 'angel'], ['destruction.jpg', 'destruction'], ['exchange.jpg', 'exchange'], ['architect.jpg', 'architect']].map(([file, key]) =>
        `<div style="position:relative">${framed(region(file, 240, 135, 1440, 810, 471), `border-color:${MODES[key].color}aa`)}
          <div style="position:absolute;left:12px;top:12px">${modeChip(key)}</div></div>`).join('')}</div>` }),

  'card-architect': () => card({
    kicker: 'Architect Core', title: 'Shapes in<br>two clicks',
    points: ['Click one corner, aim at the other: the preview follows your aim',
      'Line, wall, floor, box and cube between the corners',
      'Circles and rings around the first click',
      '<kbd>Sneak</kbd> + scroll changes the shape', 'Uses the blocks in your offhand or the one you click',
      'Right-click the air to start over'],
    art: framed(region('architect.jpg', 0, 0, 1920, 1080, 960)) }),

  'card-supply': () => card({
    kicker: 'Block supply', title: 'Never run<br>dry',
    points: ['Wands pull blocks from <b class="hi">shulker boxes</b> and <b class="hi">bundles</b> in your inventory',
      'And from your <b class="hi">Ender Chest</b>', 'Sneak + right-click a chest to bind it: the wand builds from it',
      'Hotbar stacks are used last, so you keep what you hold', 'The HUD shows how many blocks you have'],
    art: `<div style="position:relative">${framed(region('supply.jpg', 0, 0, 1920, 1080, 960))}
      <div class="frame" style="position:absolute;right:-26px;bottom:-40px;width:520px">${region('supply.jpg', 1180, 990, 740, 90, 520)}</div></div>` }),

  'card-undo': () => card({
    kicker: 'Undo and redo', title: 'Changed your<br>mind?',
    points: ['<kbd>Ctrl</kbd> + <kbd>Z</kbd> undoes, <kbd>Ctrl</kbd> + <kbd>Y</kbd> redoes',
      'Ten steps per player by default', 'Hold <kbd>Ctrl</kbd> and sneak to see what an undo would touch',
      'Every block comes back to your inventory, and nothing is duplicated', 'Destruction undoes too: the drops are taken back'],
    art: framed(region('undo.jpg', 160, 60, 1600, 900, 960)) }),

  'card-controls': () => card({
    kicker: 'Controls', title: 'Out of<br>your way',
    points: ['A small HUD next to the hotbar: mode, size and blocks left',
      'Hold <kbd>R</kbd> for the mode wheel', '<kbd>G</kbd> or Sneak + right-click the air for every setting',
      'Every key can be rebound', 'Colours, HUD and shortcuts are in client.json'],
    art: `<div style="position:relative;height:560px">${framed(region('screen.jpg', 420, 160, 1080, 760, 600), 'width:600px')}
      <div class="frame" style="position:absolute;right:0;top:170px;width:420px">${region('wheel.jpg', 600, 220, 720, 640, 420)}</div></div>` }),

  'card-servers': () => card({
    kicker: 'Made for servers', title: 'Safe on<br>every server',
    points: ['Claims and protection are asked before anything moves, on every loader',
      'Items are taken before a block is placed and refunded if it is refused',
      '<b class="hi">server.json</b> sets tiers, modes, sources and range; synced to players',
      'Deny tags keep wands off spawners, portals and more', '<b class="hi">/wands reload</b> applies changes live'],
    art: `<div style="display:flex;flex-direction:column;gap:18px">
      ${framed(configPanel(), 'background:#0b1622')}
      ${stats([['Claims', 'NeoForge · Forge · Fabric', 2], ['Config', 'Synced'], ['Reload', 'Live']])}</div>` }),
};

const HEADERS = {
  'header-whats-new': 'What’s New in 2.0', 'header-wands': 'The Wands', 'header-construction': 'Construction',
  'header-modes': 'Cores and Modes', 'header-architect': 'Architect', 'header-supply': 'Block Supply', 'header-undo': 'Undo and Redo',
  'header-controls': 'Controls', 'header-servers': 'For Servers', 'header-crafting': 'Crafting', 'header-compatibility': 'Downloads',
  'header-faq': 'FAQ', 'header-gallery': 'Gallery',
};
for (const [name, title] of Object.entries(HEADERS)) {
  assets[name] = () => header(title);
}
assets.divider = divider;

(async () => {
  const wanted = process.argv.slice(2);
  const names = wanted.length ? wanted : Object.keys(assets);
  const browser = await chromium.launch();
  const context = await browser.newContext({ deviceScaleFactor: 1 });
  for (const name of names) {
    if (!assets[name]) {
      console.error('unknown asset', name);
      continue;
    }
    const html = assets[name]();
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    const p = await context.newPage();
    await p.goto(url(file));
    await p.evaluate(() => document.fonts.ready);
    await p.waitForTimeout(150);
    const { width, height } = await p.evaluate(() => ({ width: document.body.scrollWidth, height: document.body.scrollHeight }));
    await p.setViewportSize({ width, height });
    const transparent = name.startsWith('header') || name === 'divider';
    await p.screenshot({ path: path.join(out, `${name}.png`), omitBackground: transparent || name === 'icon' });
    await p.close();
    fs.unlinkSync(file);
    console.log('rendered', name);
  }
  await browser.close();
})();
