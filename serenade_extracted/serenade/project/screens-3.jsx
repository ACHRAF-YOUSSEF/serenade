// Serenade — Screens 11-15: Downloads, Upload, Providers, Settings, Notification

// 11. DOWNLOADS / OFFLINE ──────────────────────────────────
function DownloadsScreen({ theme = SERENADE }) {
  const T = theme;
  const tracks = [
    { t: 'Bonfires in Reverse', a: 'Hana Okuda',         d: '4:12', s: 'done' },
    { t: 'Slow Tide',            a: 'Vesper & the Hours', d: '3:48', s: 'done' },
    { t: 'A Letter in November', a: 'Iben Mell',          d: '5:02', s: 'progress', p: 64 },
    { t: 'Cinders, Cinders',     a: 'Marit Rún',          d: '6:12', s: 'queued' },
    { t: 'Wintering',            a: 'Hana Okuda',         d: '3:04', s: 'done' },
  ];
  return (
    <Phone theme={T}>
      <div style={{ padding: '20px 18px 0' }}>
        <div className="sr-serif" style={{ fontSize: 26, fontStyle: 'italic', color: T.text }}>Downloads</div>
        <div style={{ fontSize: 12.5, color: T.textDim, marginTop: 4 }}>2.1 GB on this device · 12 tracks ready offline</div>

        {/* storage bar */}
        <div style={{ marginTop: 16, height: 6, borderRadius: 3, background: T.surface, overflow: 'hidden', display: 'flex' }}>
          <div style={{ width: '38%', background: T.primary }}/>
          <div style={{ width: '12%', background: T.coral, opacity: .6 }}/>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 6, fontSize: 10.5, color: T.textMute, fontFamily: T.mono }}>
          <span>USED 2.1G</span><span>FREE 4.4G</span>
        </div>

        {/* in-progress card */}
        <div style={{
          marginTop: 16, background: T.surface, border: `1px solid ${T.lineHi}`,
          borderRadius: 14, padding: 14,
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{
              width: 28, height: 28, borderRadius: '50%',
              border: `2px solid ${T.line}`,
              borderTopColor: T.primary,
              animation: 'sr-rotate 1.2s linear infinite',
            }}/>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13.5, fontWeight: 600, color: T.text }}>Downloading Lantern Hours</div>
              <div style={{ fontSize: 11.5, color: T.textDim }}>3 of 38 · over Wi-Fi only</div>
            </div>
            <span style={{ fontSize: 11.5, color: T.primary, fontFamily: T.mono }}>64%</span>
          </div>
          <div style={{ height: 3, background: 'rgba(255,255,255,0.08)', borderRadius: 2, marginTop: 12, overflow: 'hidden' }}>
            <div style={{ width: '64%', height: '100%', background: T.primary }}/>
          </div>
        </div>
      </div>

      <div style={{ marginTop: 18, padding: '0 18px 8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase' }}>On this device</span>
        <span style={{ fontSize: 11.5, color: T.primary, fontWeight: 600 }}>Edit</span>
      </div>

      <div style={{ paddingBottom: 130 }}>
        {tracks.map((t, i) => (
          <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '8px 18px' }}>
            <ArtSeed seed={t.t} size={42} radius={6} theme={T}/>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 500, color: T.text }}>{t.t}</div>
              <div style={{ fontSize: 11.5, color: T.textDim }}>{t.a} · {t.d}</div>
            </div>
            {t.s === 'done' && <Icon d={I.downloaded} size={18} stroke={T.primary}/>}
            {t.s === 'progress' && (
              <div style={{ position: 'relative', width: 22, height: 22 }}>
                <svg width="22" height="22" style={{ position: 'absolute', inset: 0, transform: 'rotate(-90deg)' }}>
                  <circle cx="11" cy="11" r="9" stroke={T.line} strokeWidth="2" fill="none"/>
                  <circle cx="11" cy="11" r="9" stroke={T.primary} strokeWidth="2" fill="none"
                          strokeDasharray={`${(t.p/100)*56.5} 56.5`} strokeLinecap="round"/>
                </svg>
              </div>
            )}
            {t.s === 'queued' && <span style={{ fontSize: 10.5, color: T.textMute, fontFamily: T.mono, letterSpacing: '0.1em' }}>WAIT</span>}
          </div>
        ))}
      </div>
      <BottomNav active="library" theme={T}/>
    </Phone>
  );
}

// 12. UPLOAD ───────────────────────────────────────────────
function UploadScreen({ theme = SERENADE }) {
  const T = theme;
  return (
    <Phone theme={T}>
      <div style={{ padding: '14px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Icon d={I.back} size={22} stroke={T.text}/>
        <span style={{ fontSize: 13, color: T.text, fontWeight: 600 }}>Upload to Studio</span>
        <span style={{ fontSize: 14, color: T.primary, fontWeight: 600 }}>Send</span>
      </div>

      <div style={{ padding: '8px 18px 0' }}>
        <div className="sr-serif" style={{ fontSize: 22, fontStyle: 'italic', color: T.text, marginBottom: 4 }}>
          A track of your own.
        </div>
        <div style={{ fontSize: 12.5, color: T.textDim }}>We'll transcode to FLAC + Opus and tune subtitles after.</div>
      </div>

      {/* selected file */}
      <div style={{ padding: '20px 18px 0' }}>
        <div style={{
          background: T.surface, border: `1px solid ${T.lineHi}`,
          borderRadius: 14, padding: 14, display: 'flex', gap: 12, alignItems: 'center',
        }}>
          <div style={{
            width: 46, height: 46, borderRadius: 10,
            background: `linear-gradient(135deg, ${T.plum}, ${T.coral})`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon d={I.music} size={22} stroke={T.text}/>
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 13.5, fontWeight: 600, color: T.text }}>january_demo_v3.wav</div>
            <div style={{ fontSize: 11.5, color: T.textDim, fontFamily: T.mono }}>48.2 MB · 4:08 · 24-bit / 48k</div>
          </div>
          <Icon d={I.close} size={18} stroke={T.textMute}/>
        </div>
      </div>

      {/* meta fields */}
      <div style={{ padding: '20px 18px 0' }}>
        {[
          { l: 'TITLE', v: 'January Demo' },
          { l: 'ARTIST', v: 'Jules Mara' },
          { l: 'ALBUM', v: 'Untitled · 2026' },
        ].map(f => (
          <div key={f.l} style={{ marginBottom: 10 }}>
            <div style={{ fontSize: 10, color: T.textMute, letterSpacing: '0.15em', marginBottom: 4 }}>{f.l}</div>
            <div style={{
              height: 44, padding: '0 14px',
              background: T.surface, border: `1px solid ${T.line}`, borderRadius: 10,
              display: 'flex', alignItems: 'center', fontSize: 14, color: T.text,
            }}>{f.v}</div>
          </div>
        ))}
      </div>

      {/* genre chips */}
      <div style={{ padding: '8px 18px 0' }}>
        <div style={{ fontSize: 10, color: T.textMute, letterSpacing: '0.15em', marginBottom: 8 }}>GENRE</div>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {['Folk','Ambient','Jazz','Classical','R&B','Electronic','Pop','Other'].map((g, i) => (
            <Chip key={g} on={i === 1} theme={T}>{g}</Chip>
          ))}
        </div>
      </div>

      {/* options */}
      <div style={{ padding: '20px 18px 0' }}>
        {[
          { l: 'Generate AI lyrics if missing', on: true },
          { l: 'Make searchable to community', on: true },
          { l: 'Allow remix into other playlists', on: false },
        ].map(o => (
          <div key={o.l} style={{
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            padding: '12px 0', borderBottom: `1px solid ${T.line}`,
          }}>
            <span style={{ fontSize: 13.5, color: T.text }}>{o.l}</span>
            <div style={{
              width: 38, height: 22, borderRadius: 11,
              background: o.on ? T.primary : T.surface, border: `1px solid ${T.line}`,
              padding: 2, display: 'flex', justifyContent: o.on ? 'flex-end' : 'flex-start',
            }}>
              <div style={{ width: 16, height: 16, borderRadius: '50%', background: o.on ? '#1a0d05' : T.textMute }}/>
            </div>
          </div>
        ))}
      </div>

      <div style={{ padding: '20px 18px 0' }}>
        <div style={{ fontSize: 11.5, color: T.textMute, lineHeight: 1.5 }}>
          By uploading, you confirm you own the rights to this work. Transcoding takes 30–90 seconds; you'll get a notification when ready.
        </div>
      </div>
    </Phone>
  );
}

// 13. PROVIDERS ────────────────────────────────────────────
function ProvidersScreen({ theme = SERENADE }) {
  const T = theme;
  const providers = [
    { n: 'Serenade Catalog', s: 'Built-in · 12.4M tracks', built: true, on: true, color: T.primary, icon: '★' },
    { n: 'Bandwidth Bridge',  s: 'community · v2.1.0',     on: true,  color: T.coral, icon: 'B' },
    { n: 'Highline FLAC',     s: 'self-hosted · v1.4.3',   on: true,  color: '#7BD2A4', icon: 'H' },
    { n: 'Garage Tape',       s: 'community · v0.9.1',     on: false, color: T.amber,  icon: 'G' },
  ];
  return (
    <Phone theme={T}>
      <div style={{ padding: '14px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Icon d={I.back} size={22} stroke={T.text}/>
        <span style={{ fontSize: 13, color: T.text, fontWeight: 600 }}>Sources</span>
        <Icon d={I.add} size={22} stroke={T.primary}/>
      </div>

      <div style={{ padding: '8px 18px 0' }}>
        <div className="sr-serif" style={{ fontSize: 22, fontStyle: 'italic', color: T.text, marginBottom: 4 }}>
          Where music comes from.
        </div>
        <div style={{ fontSize: 12.5, color: T.textDim, lineHeight: 1.5 }}>
          Add a community provider via its manifest URL to expand what Serenade can play. Each provider runs sandboxed.
        </div>

        {/* paste-manifest card */}
        <div style={{
          marginTop: 18,
          background: `linear-gradient(135deg, ${T.surface}, ${T.surfaceHi})`,
          border: `1px dashed ${T.lineHi}`, borderRadius: 12, padding: 14,
          display: 'flex', alignItems: 'center', gap: 12,
        }}>
          <Icon d={I.link} size={20} stroke={T.primary}/>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: T.text }}>Paste a manifest URL</div>
            <div style={{ fontSize: 11.5, color: T.textMute, fontFamily: T.mono }}>https://…/provider.json</div>
          </div>
          <PillButton kind="primary" size="sm" theme={T}>Add</PillButton>
        </div>
      </div>

      <div style={{ padding: '20px 18px 8px' }}>
        <span className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase' }}>Enabled · 3</span>
      </div>

      <div style={{ padding: '0 18px 130px' }}>
        {providers.map(p => (
          <div key={p.n} style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '12px 0', borderBottom: `1px solid ${T.line}`,
          }}>
            <div style={{
              width: 40, height: 40, borderRadius: 10,
              background: `linear-gradient(135deg, ${p.color}, ${T.bgDeep})`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: T.text, fontFamily: T.serif, fontStyle: 'italic', fontWeight: 600, fontSize: 18,
              border: `1px solid ${T.lineHi}`,
            }}>{p.icon}</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 13.5, color: T.text, fontWeight: 500, display: 'flex', alignItems: 'center', gap: 6 }}>
                {p.n}
                {p.built && <span style={{ fontSize: 9, color: T.primary, padding: '1px 5px', borderRadius: 3, border: `1px solid ${T.primary}55`, letterSpacing: '0.1em' }}>CORE</span>}
              </div>
              <div style={{ fontSize: 11, color: T.textDim, fontFamily: T.mono }}>{p.s}</div>
            </div>
            <div style={{
              width: 38, height: 22, borderRadius: 11,
              background: p.on ? T.primary : T.surface, border: `1px solid ${T.line}`,
              padding: 2, display: 'flex', justifyContent: p.on ? 'flex-end' : 'flex-start',
            }}>
              <div style={{ width: 16, height: 16, borderRadius: '50%', background: p.on ? '#1a0d05' : T.textMute }}/>
            </div>
          </div>
        ))}

        <div style={{
          marginTop: 16, padding: 12,
          background: 'rgba(232,176,122,0.06)', borderRadius: 10,
          border: `1px solid ${T.primary}33`,
          fontSize: 11.5, color: T.textDim, lineHeight: 1.5,
          display: 'flex', gap: 10,
        }}>
          <Icon d={I.lock} size={16} stroke={T.primary}/>
          <span>Community providers are sandboxed — they can't run code, only read JSON. Verified manifests show a copper seal.</span>
        </div>
      </div>
    </Phone>
  );
}

// 14. SETTINGS ─────────────────────────────────────────────
function SettingsScreen({ theme = SERENADE }) {
  const T = theme;
  const groups = [
    { eyebrow: 'Account', items: [
      { i: I.user, l: 'Jules Mara', s: 'jules@serenade.fm' },
      { i: I.fingerprint, l: 'Biometric unlock', s: 'On · Pixel fingerprint' },
      { i: I.lock, l: 'Change password' },
    ]},
    { eyebrow: 'Playback', items: [
      { i: I.music, l: 'Audio quality', s: 'FLAC on Wi-Fi · 256k Opus on cellular' },
      { i: I.airplane, l: 'Crossfade & gapless', s: '6 seconds · gapless on' },
      { i: I.cast, l: 'Output devices' },
    ]},
    { eyebrow: 'Storage', items: [
      { i: I.download, l: 'Downloads', s: '2.1 GB used · cellular off' },
      { i: I.trash2, l: 'Clear cache', s: '184 MB' },
    ]},
    { eyebrow: 'Sources', items: [
      { i: I.globe, l: 'Providers', s: '3 enabled · 1 paused' },
      { i: I.upload, l: 'My uploads', s: '12 tracks · 1 transcoding' },
    ]},
  ];
  return (
    <Phone theme={T}>
      <div className="sr-noscroll" style={{ height: '100%', overflow: 'auto', paddingBottom: 130 }}>
        <div style={{ padding: '24px 18px 0' }}>
          <div className="sr-serif" style={{ fontSize: 26, fontStyle: 'italic', color: T.text }}>You</div>
        </div>

        {/* profile card */}
        <div style={{ padding: '14px 18px 0' }}>
          <div style={{
            background: `linear-gradient(135deg, ${T.plum}55, ${T.coral}33)`,
            border: `1px solid ${T.lineHi}`, borderRadius: 14, padding: 16,
            display: 'flex', alignItems: 'center', gap: 12,
          }}>
            <div style={{
              width: 56, height: 56, borderRadius: '50%',
              background: `linear-gradient(140deg, ${T.coral}, ${T.plum})`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: T.text, fontFamily: T.serif, fontStyle: 'italic', fontSize: 22, fontWeight: 500,
            }}>J</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 15, fontWeight: 600, color: T.text }}>Jules Mara</div>
              <div style={{ fontSize: 11.5, color: T.textDim }}>Member since March 2026 · 1,408 hrs listened</div>
            </div>
            <Icon d={I.chev} size={18} stroke={T.textDim}/>
          </div>
        </div>

        {/* theme switcher (live) */}
        <div style={{ padding: '20px 18px 0' }}>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: 10 }}>Visual theme</div>
          <div style={{ display: 'flex', gap: 10 }}>
            {[
              { n: 'Midnight Velvet', c1: SERENADE.plum,  c2: SERENADE.coral, on: theme === SERENADE },
              { n: 'Aurora Pulse',    c1: AURORA.plum,    c2: AURORA.primary, on: theme === AURORA },
            ].map(s => (
              <div key={s.n} style={{
                flex: 1, padding: 12, borderRadius: 12,
                border: `1.5px solid ${s.on ? T.primary : T.line}`,
                background: s.on ? 'rgba(232,176,122,0.06)' : T.surface,
              }}>
                <div style={{
                  height: 50, borderRadius: 8, marginBottom: 8,
                  background: `linear-gradient(135deg, ${s.c1}, ${s.c2})`,
                }}/>
                <div style={{ fontSize: 12, fontWeight: 600, color: T.text, display: 'flex', justifyContent: 'space-between' }}>
                  {s.n}
                  {s.on && <Icon d={I.check} size={14} stroke={T.primary} sw={2.4}/>}
                </div>
              </div>
            ))}
          </div>
        </div>

        {groups.map(g => (
          <div key={g.eyebrow} style={{ marginTop: 22 }}>
            <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase', padding: '0 18px 8px' }}>{g.eyebrow}</div>
            <div style={{ background: T.surface, marginInline: 12, borderRadius: 14, border: `1px solid ${T.line}` }}>
              {g.items.map((it, i) => (
                <div key={it.l} style={{
                  display: 'flex', alignItems: 'center', gap: 12, padding: '12px 14px',
                  borderTop: i === 0 ? 'none' : `1px solid ${T.line}`,
                }}>
                  <Icon d={it.i} size={18} stroke={T.textDim}/>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 14, color: T.text }}>{it.l}</div>
                    {it.s && <div style={{ fontSize: 11.5, color: T.textDim }}>{it.s}</div>}
                  </div>
                  <Icon d={I.chev} size={16} stroke={T.textMute}/>
                </div>
              ))}
            </div>
          </div>
        ))}

        <div style={{ padding: '32px 18px 20px', textAlign: 'center', fontSize: 10.5, color: T.textOff, fontFamily: T.mono, letterSpacing: '0.15em' }}>
          SERENADE · v0.4.1 (mvp)
        </div>
      </div>
      <BottomNav active="settings" theme={T}/>
    </Phone>
  );
}

// 15. NOTIFICATION (lock screen) ───────────────────────────
function NotificationScreen({ theme = SERENADE }) {
  const T = theme;
  return (
    <Phone theme={T} hideHome>
      <div style={{
        position: 'absolute', inset: 0,
        background: `radial-gradient(80% 60% at 50% 30%, ${T.plum}66, ${T.bgDeep})`,
      }}/>
      {/* lock screen time */}
      <div style={{ position: 'relative', textAlign: 'center', paddingTop: 26 }}>
        <div className="sr-mono" style={{ fontSize: 12, color: T.textDim, letterSpacing: '0.18em' }}>Friday, 23 January</div>
        <div className="sr-serif" style={{ fontSize: 78, fontStyle: 'italic', color: T.text, lineHeight: 1, marginTop: 6, fontWeight: 300 }}>11:42</div>
      </div>

      {/* notification card */}
      <div style={{
        position: 'absolute', left: 12, right: 12, top: 240,
        background: 'rgba(31, 21, 48, 0.85)', backdropFilter: 'blur(20px)',
        border: `1px solid ${T.lineHi}`, borderRadius: 18, padding: 14,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
          <SerenadeMark size={16} theme={T}/>
          <span style={{ fontSize: 11.5, color: T.textDim, fontWeight: 500 }}>Serenade · now playing</span>
          <span style={{ flex: 1 }}/>
          <span style={{ fontSize: 10.5, color: T.textMute, fontFamily: T.mono }}>1m</span>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <ArtSeed seed="Bonfires in Reverse" size={56} radius={8} vivid theme={T}/>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 14, fontWeight: 600, color: T.text, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>Bonfires in Reverse</div>
            <div style={{ fontSize: 12, color: T.textDim, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>Hana Okuda · The Long Way Home</div>
          </div>
        </div>
        {/* progress */}
        <div style={{ height: 2, background: 'rgba(255,255,255,0.12)', borderRadius: 2, marginTop: 12, overflow: 'hidden' }}>
          <div style={{ width: '42%', height: '100%', background: T.primary }}/>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4, fontSize: 10, color: T.textMute, fontFamily: T.mono }}>
          <span>1:42</span><span>4:00</span>
        </div>
        {/* controls */}
        <div style={{ display: 'flex', justifyContent: 'space-around', alignItems: 'center', marginTop: 10 }}>
          <Icon d={I.heart} size={20} stroke={T.textDim}/>
          <Icon d={I.prev} size={26} stroke={T.text}/>
          <div style={{
            width: 44, height: 44, borderRadius: '50%',
            background: T.text, display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon d={I.pause} size={20} stroke={T.bgDeep} sw={2.2}/>
          </div>
          <Icon d={I.next} size={26} stroke={T.text}/>
          <Icon d={I.cast} size={20} stroke={T.textDim}/>
        </div>
      </div>

      {/* second notification — download done */}
      <div style={{
        position: 'absolute', left: 12, right: 12, top: 444,
        background: 'rgba(15,10,24,0.85)', backdropFilter: 'blur(20px)',
        border: `1px solid ${T.line}`, borderRadius: 14, padding: 12,
        display: 'flex', alignItems: 'center', gap: 10,
      }}>
        <div style={{
          width: 32, height: 32, borderRadius: 8,
          background: `linear-gradient(135deg, ${T.primary}, ${T.coral})`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Icon d={I.downloaded} size={18} stroke={'#1a0d05'} sw={2.2}/>
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 12.5, fontWeight: 600, color: T.text }}>Lantern Hours is ready offline</div>
          <div style={{ fontSize: 11, color: T.textDim }}>38 tracks · 412 MB</div>
        </div>
        <span style={{ fontSize: 10.5, color: T.textMute, fontFamily: T.mono }}>now</span>
      </div>

      {/* swipe hint */}
      <div style={{
        position: 'absolute', bottom: 28, left: 0, right: 0, textAlign: 'center',
        fontSize: 11, color: T.textMute, letterSpacing: '0.12em',
      }}>swipe up to open</div>
      <div style={{
        position: 'absolute', bottom: 12, left: '50%', transform: 'translateX(-50%)',
        width: 110, height: 4, borderRadius: 2, background: 'rgba(255,255,255,0.45)',
      }}/>
    </Phone>
  );
}

Object.assign(window, {
  DownloadsScreen, UploadScreen, ProvidersScreen, SettingsScreen, NotificationScreen,
});
