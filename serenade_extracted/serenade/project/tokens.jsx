// Serenade — design tokens & shared atoms
// Brand: warm-romantic, nighttime, cinematic. NOT Spotify.

const SERENADE = {
  // Midnight Velvet (default)
  bg:        '#0B0612',   // near-black with violet undertone
  bgDeep:    '#070410',
  surface:   '#160E22',   // raised surface, plum-ink
  surfaceHi: '#1F1530',   // hovered/elevated
  line:      'rgba(255,255,255,0.07)',
  lineHi:    'rgba(255,255,255,0.14)',

  // Brand
  primary:   '#E8B07A',   // warm copper / candlelight
  primaryHi: '#F4C492',
  plum:      '#6B3F8E',   // deep velvet plum
  indigo:    '#3A2E6B',   // midnight indigo
  coral:     '#E26D6D',   // glow coral
  amber:     '#F2B547',   // amber glint

  // Text
  text:      '#F4ECDF',   // warm cream
  textDim:   '#B7A7C0',
  textMute:  '#7A6E86',
  textOff:   '#544A60',

  // Accents
  good:      '#7CC4A4',
  warn:      '#E8A05B',

  // Type
  serif:    '"Cormorant Garamond", "Times New Roman", serif',
  sans:     '"Inter Tight", "Helvetica Neue", system-ui, sans-serif',
  mono:     '"JetBrains Mono", ui-monospace, monospace',
};

// Aurora Pulse — alternate
const AURORA = {
  ...SERENADE,
  bg:        '#06121A',
  bgDeep:    '#020A12',
  surface:   '#0E2030',
  surfaceHi: '#16304A',
  primary:   '#5EE6C7',   // aurora teal
  primaryHi: '#7BF1D5',
  plum:      '#3D6FB8',   // electric blue
  indigo:    '#1D3A6B',
  coral:     '#C76FE6',   // ultraviolet
  amber:     '#F2D14A',   // hot star
  text:      '#E6F4FF',
  textDim:   '#9DB6CC',
  textMute:  '#5E7388',
  textOff:   '#3A4B5E',
};

// Inject global styles + fonts once
if (typeof document !== 'undefined' && !document.getElementById('serenade-styles')) {
  const link = document.createElement('link');
  link.rel = 'stylesheet';
  link.href = 'https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500&family=Inter+Tight:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap';
  document.head.appendChild(link);

  const s = document.createElement('style');
  s.id = 'serenade-styles';
  s.textContent = `
    .sr * { box-sizing: border-box; -webkit-font-smoothing: antialiased; }
    .sr { font-family: ${SERENADE.sans}; color: ${SERENADE.text}; }
    .sr-serif { font-family: ${SERENADE.serif}; font-weight: 500; letter-spacing: -0.01em; }
    .sr-mono { font-family: ${SERENADE.mono}; }
    .sr-noscroll::-webkit-scrollbar { display: none; }
    .sr-noscroll { scrollbar-width: none; }
    .sr-hairline { position: relative; }
    .sr-hairline::after { content:''; position:absolute; left:0; right:0; bottom:0; height:1px; background:rgba(255,255,255,0.06); }

    @keyframes sr-pulse { 0%,100% { opacity:.5; transform:scale(1);} 50% { opacity:1; transform:scale(1.04);} }
    @keyframes sr-rotate { to { transform: rotate(360deg); } }
    @keyframes sr-shimmer {
      0% { background-position: -200% 0; }
      100% { background-position: 200% 0; }
    }
    @keyframes sr-bars {
      0%,100% { transform: scaleY(.35); }
      50% { transform: scaleY(1); }
    }
    .sr-shimmer {
      background: linear-gradient(90deg, rgba(255,255,255,0.04) 0%, rgba(255,255,255,0.10) 50%, rgba(255,255,255,0.04) 100%);
      background-size: 200% 100%;
      animation: sr-shimmer 1.8s linear infinite;
    }
    .sr-bar { transform-origin: bottom; animation: sr-bars 1s ease-in-out infinite; }
    .sr-bar:nth-child(2) { animation-delay: .15s; }
    .sr-bar:nth-child(3) { animation-delay: .3s; }
    .sr-bar:nth-child(4) { animation-delay: .45s; }

    .sr-press { transition: transform .15s, background .15s, opacity .15s; }
    .sr-press:active { transform: scale(0.96); opacity: .85; }

    .sr-dot { width:6px; height:6px; border-radius:50%; display:inline-block; }
  `;
  document.head.appendChild(s);
}

// ───────────────────────────────────────────────────────────
// Album art placeholder — generative gradient + monogram
// Stable per-seed; no real imagery. Reads classy and intentional.
// ───────────────────────────────────────────────────────────
function hash(str) {
  let h = 2166136261 >>> 0;
  for (let i = 0; i < str.length; i++) {
    h ^= str.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  return h >>> 0;
}

function ArtSeed({ seed = 'untitled', size = 56, radius = 8, glyph, vivid = false, theme }) {
  const T = theme || SERENADE;
  const h = hash(seed);
  const hue1 = h % 360;
  const hue2 = (hue1 + 40 + (h >> 8) % 60) % 360;
  const sat = vivid ? 70 : 45;
  const c1 = `hsl(${hue1} ${sat}% ${vivid ? 38 : 28}%)`;
  const c2 = `hsl(${hue2} ${sat - 10}% ${vivid ? 22 : 14}%)`;
  const angle = h % 180;
  const initials = (glyph !== undefined ? glyph : seed)
    .split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase().slice(0, 2);
  // a thin vinyl-ring motif on top
  const ring = (h >> 4) % 3;
  return (
    <div style={{
      position: 'relative', width: size, height: size, borderRadius: radius,
      overflow: 'hidden',
      background: `linear-gradient(${angle}deg, ${c1}, ${c2})`,
      boxShadow: 'inset 0 0 0 1px rgba(255,255,255,0.05)',
      flexShrink: 0,
    }}>
      {/* soft orbital ring */}
      <div style={{
        position: 'absolute',
        inset: ring === 0 ? '14%' : ring === 1 ? '24%' : '8%',
        borderRadius: '50%',
        border: '1px solid rgba(255,255,255,0.18)',
      }} />
      <div style={{
        position: 'absolute',
        inset: '38%',
        borderRadius: '50%',
        background: 'rgba(0,0,0,0.35)',
        boxShadow: 'inset 0 0 0 1px rgba(255,255,255,0.12)',
      }} />
      {/* monogram */}
      <div style={{
        position: 'absolute', inset: 0,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontFamily: T.serif, fontStyle: 'italic',
        color: 'rgba(255,255,255,0.85)',
        fontSize: size * 0.32, fontWeight: 500,
        textShadow: '0 1px 8px rgba(0,0,0,0.4)',
        letterSpacing: '-0.02em',
      }}>{initials}</div>
      {/* glossy highlight */}
      <div style={{
        position: 'absolute', inset: 0,
        background: 'linear-gradient(160deg, rgba(255,255,255,0.10) 0%, rgba(255,255,255,0) 40%)',
        pointerEvents: 'none',
      }} />
    </div>
  );
}

// ───────────────────────────────────────────────────────────
// Iconography — single 24px stroke-only set, drawn here for control.
// Outline, 1.6 stroke, rounded caps. NEVER emoji.
// ───────────────────────────────────────────────────────────
const Icon = ({ d, size = 22, stroke = 'currentColor', fill = 'none', sw = 1.6, style }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill={fill} stroke={stroke}
       strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round" style={style}>
    {typeof d === 'string' ? <path d={d}/> : d}
  </svg>
);

const I = {
  // navigation / actions
  home:    <><path d="M3 11.5 12 4l9 7.5"/><path d="M5 10v10h14V10"/></>,
  search:  <><circle cx="11" cy="11" r="6.5"/><path d="m20 20-3.5-3.5"/></>,
  library: <><path d="M4 5h2v14H4z"/><path d="M9 5h2v14H9z"/><path d="m16 5 4.5 14-1.9.6L14 5.6z"/></>,
  upload:  <><path d="M12 17V5"/><path d="m6 11 6-6 6 6"/><path d="M4 19h16"/></>,
  settings:<><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1A2 2 0 1 1 4.3 17l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1A2 2 0 1 1 7 4.3l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/></>,
  // player
  play:    <path d="M7 4v16l13-8z" fill="currentColor" stroke="none"/>,
  playOutline: <path d="M7 4v16l13-8z"/>,
  pause:   <><rect x="6.5" y="4.5" width="4" height="15" rx="1"/><rect x="13.5" y="4.5" width="4" height="15" rx="1"/></>,
  next:    <><path d="M5 4v16l11-8z"/><path d="M18 4v16"/></>,
  prev:    <><path d="M19 4v16L8 12z"/><path d="M6 4v16"/></>,
  shuffle: <><path d="M16 4h4v4"/><path d="M20 4 4 20"/><path d="M16 20h4v-4"/><path d="m20 20-5-5"/><path d="M9 9 4 4"/></>,
  repeat:  <><path d="M3 12V8a3 3 0 0 1 3-3h12l-3-3"/><path d="M21 12v4a3 3 0 0 1-3 3H6l3 3"/></>,
  heart:   <path d="M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.5A4 4 0 0 1 19 10c0 5.6-7 10-7 10z"/>,
  heartOn: <path d="M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.5A4 4 0 0 1 19 10c0 5.6-7 10-7 10z" fill="currentColor"/>,
  more:    <><circle cx="5" cy="12" r="1.4"/><circle cx="12" cy="12" r="1.4"/><circle cx="19" cy="12" r="1.4"/></>,
  back:    <><path d="m15 5-7 7 7 7"/></>,
  close:   <><path d="m6 6 12 12"/><path d="m18 6-12 12"/></>,
  chev:    <path d="m9 6 6 6-6 6"/>,
  chevDown:<path d="m6 9 6 6 6-6"/>,
  download:<><path d="M12 4v12"/><path d="m6 11 6 6 6-6"/><path d="M4 20h16"/></>,
  downloaded: <><circle cx="12" cy="12" r="9"/><path d="m8 12 3 3 5-6"/></>,
  cast:    <><path d="M3 17a4 4 0 0 1 4 4"/><path d="M3 13a8 8 0 0 1 8 8"/><circle cx="3.5" cy="20.5" r="1" fill="currentColor"/><path d="M3 9V6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-5"/></>,
  add:     <><path d="M12 5v14"/><path d="M5 12h14"/></>,
  check:   <path d="m5 12 4 4 10-10"/>,
  filter:  <><path d="M4 5h16"/><path d="M7 12h10"/><path d="M10 19h4"/></>,
  star:    <path d="m12 4 2.5 5 5.5.8-4 3.9 1 5.5L12 16.5 7 19.2l1-5.5-4-3.9 5.5-.8z"/>,
  starOn:  <path d="m12 4 2.5 5 5.5.8-4 3.9 1 5.5L12 16.5 7 19.2l1-5.5-4-3.9 5.5-.8z" fill="currentColor"/>,
  fingerprint: <><path d="M5 11a7 7 0 0 1 14 0v2"/><path d="M9 11a3 3 0 0 1 6 0v3a3 3 0 0 1-3 3"/><path d="M12 14v5"/><path d="M5.5 16c.7 1.5 1.5 3 1.5 4"/><path d="M19 19c-.5-1.5-1-3-1-5"/></>,
  lock:    <><rect x="5" y="11" width="14" height="9" rx="2"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/></>,
  link:    <><path d="M10 14a4 4 0 0 0 5.6 0l3-3a4 4 0 0 0-5.6-5.6l-1 1"/><path d="M14 10a4 4 0 0 0-5.6 0l-3 3a4 4 0 0 0 5.6 5.6l1-1"/></>,
  globe:   <><circle cx="12" cy="12" r="9"/><path d="M3 12h18"/><path d="M12 3a14 14 0 0 1 0 18"/><path d="M12 3a14 14 0 0 0 0 18"/></>,
  music:   <><circle cx="6" cy="18" r="2.5"/><circle cx="17" cy="16" r="2.5"/><path d="M8.5 18V6l11-2v12"/></>,
  copy:    <><rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2"/></>,
  edit:    <><path d="M14 4 5 13v4h4l9-9"/><path d="m13 5 4 4"/></>,
  trash:   <><path d="M5 7h14"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/><path d="m7 7 1 12a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2l1-12"/></>,
  drag:    <><circle cx="9" cy="6" r="1.2"/><circle cx="9" cy="12" r="1.2"/><circle cx="9" cy="18" r="1.2"/><circle cx="15" cy="6" r="1.2"/><circle cx="15" cy="12" r="1.2"/><circle cx="15" cy="18" r="1.2"/></>,
  queue:   <><path d="M4 6h12"/><path d="M4 12h12"/><path d="M4 18h8"/><path d="M16 14v8"/><path d="m13 18 3 4 3-4"/></>,
  mic:     <><rect x="9" y="3" width="6" height="12" rx="3"/><path d="M5 11a7 7 0 0 0 14 0"/><path d="M12 18v3"/></>,
  bell:    <><path d="M6 9a6 6 0 0 1 12 0c0 5 2 7 2 7H4s2-2 2-7z"/><path d="M10 20a2 2 0 0 0 4 0"/></>,
  user:    <><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></>,
  sliders: <><path d="M4 6h10"/><path d="M18 6h2"/><circle cx="16" cy="6" r="2"/><path d="M4 18h4"/><path d="M12 18h8"/><circle cx="10" cy="18" r="2"/><path d="M4 12h2"/><path d="M10 12h10"/><circle cx="8" cy="12" r="2"/></>,
  wifi:    <><path d="M5 12a10 10 0 0 1 14 0"/><path d="M8 15a6 6 0 0 1 8 0"/><circle cx="12" cy="18" r="1" fill="currentColor"/></>,
  airplane:<path d="M12 2 14 10l8 2-8 2-2 8-2-8-8-2 8-2z"/>,
  trash2:  <><path d="M4 7h16"/><path d="M9 7V4h6v3"/><path d="m6 7 1 13h10l1-13"/></>,
  arrowUp: <><path d="M12 19V5"/><path d="m5 12 7-7 7 7"/></>,
  flame:   <path d="M12 3c1 4 5 5 5 9a5 5 0 0 1-10 0c0-2 1-3 2-4 0 2 1 3 2 3-1-3 1-5 1-8z"/>,
};

const IconBtn = ({ icon, size = 22, color, ...p }) => (
  <Icon d={icon} size={size} stroke={color || 'currentColor'} {...p} />
);

Object.assign(window, { SERENADE, AURORA, ArtSeed, Icon, IconBtn, I, hash });
