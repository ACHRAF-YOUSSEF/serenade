// Serenade — primitive UI atoms shared across screens
// Buttons, chips, list rows, sheets, mini-player, bottom nav, etc.

const PHONE_W = 360;
const PHONE_H = 760;

// ───────────────────── Phone shell (no Material, custom) ─────────────────────
function Phone({ children, theme = SERENADE, dark = true, label, height = PHONE_H, hideStatus = false, hideHome = false }) {
  const T = theme;
  return (
    <div className="sr" style={{
      width: PHONE_W, height, position: 'relative',
      background: T.bg, color: T.text,
      borderRadius: 36, overflow: 'hidden',
      boxShadow: '0 30px 80px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(255,255,255,0.06)',
      fontFamily: T.sans,
    }}>
      {!hideStatus && <SrStatusBar theme={T} />}
      <div className="sr-noscroll" style={{
        position: 'absolute', top: hideStatus ? 0 : 36, left: 0, right: 0,
        bottom: hideHome ? 0 : 18,
        overflow: 'hidden',
      }}>
        {children}
      </div>
      {!hideHome && (
        <div style={{
          position: 'absolute', bottom: 6, left: 0, right: 0,
          display: 'flex', justifyContent: 'center',
        }}>
          <div style={{ width: 110, height: 4, borderRadius: 2, background: 'rgba(255,255,255,0.45)' }} />
        </div>
      )}
    </div>
  );
}

function SrStatusBar({ theme = SERENADE }) {
  return (
    <div style={{
      height: 36, position: 'relative',
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 22px', fontSize: 13, fontWeight: 600,
      color: theme.text,
      letterSpacing: '0.01em',
    }}>
      <span style={{ fontVariantNumeric: 'tabular-nums' }}>9:41</span>
      <div style={{
        position: 'absolute', left: '50%', top: 10, transform: 'translateX(-50%)',
        width: 90, height: 22, borderRadius: 12, background: '#000',
      }}/>
      <div style={{ display: 'flex', gap: 5, alignItems: 'center' }}>
        <Icon d={I.wifi} size={14} stroke={theme.text} sw={1.8}/>
        <svg width="22" height="11" viewBox="0 0 22 11">
          <rect x="0" y="0" width="19" height="11" rx="2.5" fill="none" stroke={theme.text} strokeWidth="1" opacity="0.8"/>
          <rect x="2" y="2" width="14" height="7" rx="1" fill={theme.text}/>
          <rect x="20" y="3.5" width="1.5" height="4" rx=".5" fill={theme.text} opacity=".8"/>
        </svg>
      </div>
    </div>
  );
}

// ───────────────────── Bottom nav (5 tabs) ─────────────────────
function BottomNav({ active = 'home', theme = SERENADE }) {
  const T = theme;
  const tabs = [
    { id: 'home',     label: 'Listen',  icon: I.home },
    { id: 'search',   label: 'Search',  icon: I.search },
    { id: 'library',  label: 'Library', icon: I.library },
    { id: 'upload',   label: 'Studio',  icon: I.upload },
    { id: 'settings', label: 'You',     icon: I.user },
  ];
  return (
    <div style={{
      position: 'absolute', left: 0, right: 0, bottom: 0,
      paddingBottom: 18,
      background: 'linear-gradient(180deg, rgba(11,6,18,0) 0%, rgba(11,6,18,0.85) 30%, ' + T.bgDeep + ' 70%)',
    }}>
      <div style={{
        display: 'flex', justifyContent: 'space-between',
        padding: '12px 18px 8px',
      }}>
        {tabs.map(t => {
          const on = t.id === active;
          return (
            <div key={t.id} className="sr-press" style={{
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
              flex: 1, color: on ? T.primary : T.textMute,
            }}>
              <Icon d={t.icon} size={22} sw={on ? 2 : 1.6}/>
              <span style={{ fontSize: 10.5, fontWeight: on ? 600 : 500, letterSpacing: '0.03em' }}>{t.label}</span>
              {on && <div style={{ width: 4, height: 4, borderRadius: 2, background: T.primary, marginTop: -2 }}/>}
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ───────────────────── Mini player ─────────────────────
function MiniPlayer({ track, theme = SERENADE, bottom = 78, playing = true }) {
  const T = theme;
  return (
    <div style={{
      position: 'absolute', left: 8, right: 8, bottom,
      background: 'rgba(31, 21, 48, 0.85)',
      backdropFilter: 'blur(20px)',
      WebkitBackdropFilter: 'blur(20px)',
      border: `1px solid ${T.lineHi}`,
      borderRadius: 14, padding: 8, display: 'flex', alignItems: 'center', gap: 12,
      boxShadow: '0 12px 30px rgba(0,0,0,0.4)',
    }}>
      <ArtSeed seed={track.title} size={44} radius={8} theme={T} vivid />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: T.text, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {track.title}
        </div>
        <div style={{ fontSize: 11, color: T.textDim, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {track.artist}
        </div>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, paddingRight: 6 }}>
        <Icon d={I.heart} size={20} stroke={T.textDim}/>
        <div style={{
          width: 36, height: 36, borderRadius: '50%',
          background: T.primary, color: '#1a0d05',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Icon d={playing ? I.pause : I.play} size={18} stroke={'#1a0d05'} sw={2.2}/>
        </div>
      </div>
      {/* progress hairline */}
      <div style={{
        position: 'absolute', left: 12, right: 12, bottom: 4, height: 2,
        background: 'rgba(255,255,255,0.08)', borderRadius: 2, overflow: 'hidden',
      }}>
        <div style={{ width: '38%', height: '100%', background: T.primary, borderRadius: 2 }}/>
      </div>
    </div>
  );
}

// ───────────────────── Buttons ─────────────────────
function PillButton({ children, kind = 'primary', icon, theme = SERENADE, size = 'md', full, style }) {
  const T = theme;
  const h = size === 'lg' ? 52 : size === 'sm' ? 32 : 44;
  const base = {
    display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8,
    height: h, padding: `0 ${size === 'sm' ? 14 : 22}px`,
    borderRadius: h, fontWeight: 600, fontSize: size === 'lg' ? 16 : size === 'sm' ? 13 : 14.5,
    letterSpacing: '0.01em', cursor: 'pointer',
    width: full ? '100%' : undefined,
    ...style,
  };
  const skin = {
    primary: { background: T.primary, color: '#1a0d05', border: 'none' },
    soft:    { background: 'rgba(255,255,255,0.07)', color: T.text, border: `1px solid ${T.line}` },
    ghost:   { background: 'transparent', color: T.text, border: `1px solid ${T.lineHi}` },
    dark:    { background: T.bgDeep, color: T.text, border: `1px solid ${T.line}` },
  }[kind];
  return (
    <button className="sr-press" style={{ ...base, ...skin }}>
      {icon && <Icon d={icon} size={size === 'lg' ? 20 : 17} stroke="currentColor" sw={2}/>}
      {children}
    </button>
  );
}

// ───────────────────── Section header ─────────────────────
function Section({ title, action, children, theme = SERENADE, eyebrow, big }) {
  const T = theme;
  return (
    <div style={{ marginBottom: 24 }}>
      <div style={{ padding: '0 18px 10px', display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
        <div>
          {eyebrow && (
            <div className="sr-mono" style={{
              fontSize: 9.5, color: T.textMute, letterSpacing: '0.18em',
              textTransform: 'uppercase', marginBottom: 4,
            }}>{eyebrow}</div>
          )}
          <div className="sr-serif" style={{
            fontSize: big ? 26 : 19, color: T.text,
            fontStyle: big ? 'italic' : 'normal',
            lineHeight: 1.05,
          }}>{title}</div>
        </div>
        {action && <div style={{ fontSize: 12, color: T.textDim, fontWeight: 500 }}>{action}</div>}
      </div>
      {children}
    </div>
  );
}

// ───────────────────── Track row ─────────────────────
function TrackRow({ idx, title, artist, dur, playing, downloaded, theme = SERENADE, art, explicit, hi }) {
  const T = theme;
  return (
    <div className="sr-press" style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '8px 18px',
      background: hi ? 'rgba(232,176,122,0.06)' : 'transparent',
    }}>
      {idx !== undefined ? (
        <div style={{
          width: 22, textAlign: 'center', fontSize: 13, color: playing ? T.primary : T.textMute,
          fontFamily: T.mono,
        }}>
          {playing ? (
            <div style={{ display: 'inline-flex', gap: 1.5, alignItems: 'flex-end', height: 12 }}>
              <span className="sr-bar" style={{ display: 'inline-block', width: 2, height: 12, background: T.primary }}/>
              <span className="sr-bar" style={{ display: 'inline-block', width: 2, height: 12, background: T.primary }}/>
              <span className="sr-bar" style={{ display: 'inline-block', width: 2, height: 12, background: T.primary }}/>
            </div>
          ) : idx}
        </div>
      ) : null}
      {art !== false && <ArtSeed seed={title} size={42} radius={6} theme={T}/>}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{
          fontSize: 14, fontWeight: 500,
          color: playing ? T.primary : T.text,
          whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
        }}>{title}</div>
        <div style={{ fontSize: 11.5, color: T.textDim, display: 'flex', alignItems: 'center', gap: 6 }}>
          {explicit && <span style={{
            fontSize: 8.5, fontWeight: 700, padding: '1px 4px', borderRadius: 2,
            background: 'rgba(255,255,255,0.12)', color: T.textDim, letterSpacing: '0.05em',
          }}>E</span>}
          <span style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{artist}</span>
        </div>
      </div>
      {downloaded && <Icon d={I.downloaded} size={14} stroke={T.primary} sw={1.6}/>}
      {dur && <span style={{ fontSize: 11.5, color: T.textMute, fontFamily: T.mono }}>{dur}</span>}
      <Icon d={I.more} size={18} stroke={T.textMute}/>
    </div>
  );
}

// ───────────────────── Chip / pill ─────────────────────
function Chip({ children, on, theme = SERENADE, color, icon }) {
  const T = theme;
  const hue = color || T.primary;
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      padding: '6px 12px', borderRadius: 100, fontSize: 12, fontWeight: 500,
      background: on ? hue : 'rgba(255,255,255,0.06)',
      color: on ? '#1a0d05' : T.text,
      border: on ? 'none' : `1px solid ${T.line}`,
      whiteSpace: 'nowrap', cursor: 'pointer',
    }}>
      {icon && <Icon d={icon} size={13} stroke="currentColor" sw={2}/>}
      {children}
    </span>
  );
}

// ───────────────────── Wordmark / logo ─────────────────────
function Wordmark({ size = 28, theme = SERENADE, mark = true }) {
  const T = theme;
  return (
    <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
      {mark && <SerenadeMark size={size * 0.92} theme={T}/>}
      <span className="sr-serif" style={{
        fontStyle: 'italic', fontSize: size, color: T.text,
        letterSpacing: '-0.015em', fontWeight: 500,
      }}>Serenade</span>
    </div>
  );
}

// Mark — a hand-cut crescent + the "S" — original geometry, not derivative.
function SerenadeMark({ size = 28, theme = SERENADE }) {
  const T = theme;
  return (
    <svg width={size} height={size} viewBox="0 0 32 32">
      <defs>
        <linearGradient id={`sg-${theme === AURORA ? 'a' : 'v'}`} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor={T.primaryHi || T.primary}/>
          <stop offset="1" stopColor={T.coral}/>
        </linearGradient>
      </defs>
      {/* outer crescent */}
      <path d="M16 2a14 14 0 1 0 9.9 23.9A11 11 0 0 1 16 2z"
            fill={`url(#sg-${theme === AURORA ? 'a' : 'v'})`}/>
      {/* serif S */}
      <text x="16" y="22" textAnchor="middle" fontFamily={T.serif} fontStyle="italic"
            fontSize="16" fontWeight="500" fill="#0B0612">S</text>
    </svg>
  );
}

Object.assign(window, {
  Phone, SrStatusBar, BottomNav, MiniPlayer, PillButton, Section, TrackRow, Chip, Wordmark, SerenadeMark,
  PHONE_W, PHONE_H,
});
