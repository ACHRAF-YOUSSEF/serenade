// Serenade — Design system overview cards (palette, type, components)

function PaletteCard({ theme = SERENADE }) {
  const T = theme;
  const swatches = [
    { l: 'bg',        v: T.bg,        hex: T.bg },
    { l: 'bgDeep',    v: T.bgDeep,    hex: T.bgDeep },
    { l: 'surface',   v: T.surface,   hex: T.surface },
    { l: 'surfaceHi', v: T.surfaceHi, hex: T.surfaceHi },
  ];
  const accents = [
    { l: 'primary · copper', v: T.primary, hex: T.primary },
    { l: 'plum',             v: T.plum,    hex: T.plum },
    { l: 'indigo',           v: T.indigo,  hex: T.indigo },
    { l: 'coral',            v: T.coral,   hex: T.coral },
    { l: 'amber',            v: T.amber,   hex: T.amber },
  ];
  const text = [
    { l: 'text',     v: T.text,    hex: T.text },
    { l: 'textDim',  v: T.textDim, hex: T.textDim },
    { l: 'textMute', v: T.textMute,hex: T.textMute },
    { l: 'textOff',  v: T.textOff, hex: T.textOff },
  ];

  return (
    <div className="sr" style={{
      width: 720, padding: 32, background: T.bg, color: T.text,
      borderRadius: 16, border: `1px solid ${T.lineHi}`,
    }}>
      <div className="sr-mono" style={{ fontSize: 10, color: T.textMute, letterSpacing: '0.25em', textTransform: 'uppercase', marginBottom: 6 }}>Brand · 01</div>
      <div className="sr-serif" style={{ fontSize: 32, fontStyle: 'italic', color: T.text, lineHeight: 1.05, marginBottom: 6 }}>Midnight Velvet</div>
      <div style={{ fontSize: 13, color: T.textDim, marginBottom: 24, maxWidth: 540, lineHeight: 1.5 }}>
        Romantic, cinematic, after-dark. Plum-ink surfaces, candlelight copper for primary action, coral and amber as warm glints, indigo for cool depth. <strong style={{ color: T.text }}>Deliberately not Spotify green.</strong> Five adjectives: poetic · intimate · cinematic · warm · hi-fi · nocturnal · grown-up.
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
        <div>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: 10 }}>Surfaces</div>
          {swatches.map(s => (
            <div key={s.l} style={{
              display: 'flex', alignItems: 'center', gap: 12, padding: '8px 0',
              borderBottom: `1px solid ${T.line}`,
            }}>
              <div style={{ width: 54, height: 38, borderRadius: 8, background: s.v, border: `1px solid ${T.lineHi}` }}/>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12.5, color: T.text }}>{s.l}</div>
                <div style={{ fontSize: 10.5, color: T.textMute, fontFamily: T.mono }}>{s.hex}</div>
              </div>
            </div>
          ))}
        </div>
        <div>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: 10 }}>Accents</div>
          {accents.map(s => (
            <div key={s.l} style={{
              display: 'flex', alignItems: 'center', gap: 12, padding: '8px 0',
              borderBottom: `1px solid ${T.line}`,
            }}>
              <div style={{ width: 54, height: 38, borderRadius: 8, background: s.v }}/>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12.5, color: T.text }}>{s.l}</div>
                <div style={{ fontSize: 10.5, color: T.textMute, fontFamily: T.mono }}>{s.hex}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase', marginTop: 24, marginBottom: 10 }}>Text hierarchy</div>
      <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
        {text.map(s => (
          <div key={s.l}>
            <div style={{ fontSize: 22, color: s.v, fontFamily: T.serif, fontStyle: 'italic' }}>Aa</div>
            <div style={{ fontSize: 11.5, color: T.text }}>{s.l}</div>
            <div style={{ fontSize: 10, color: T.textMute, fontFamily: T.mono }}>{s.hex}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

function TypeCard({ theme = SERENADE }) {
  const T = theme;
  const scale = [
    { l: 'Display · serif italic', f: T.serif, s: 38, w: 500, italic: true,  ex: 'After dark' },
    { l: 'Title · serif italic',   f: T.serif, s: 26, w: 500, italic: true,  ex: 'Lantern Hours' },
    { l: 'Section · serif',        f: T.serif, s: 19, w: 500, italic: false, ex: 'For your evening' },
    { l: 'Body large · sans',      f: T.sans,  s: 16, w: 500, italic: false, ex: 'A quieter place to listen.' },
    { l: 'Body · sans',            f: T.sans,  s: 14, w: 400, italic: false, ex: 'Hana Okuda · 4:12' },
    { l: 'Caption · sans',         f: T.sans,  s: 12, w: 400, italic: false, ex: 'Curated by Marit · 38 tracks' },
    { l: 'Eyebrow · mono',         f: T.mono,  s: 10, w: 500, italic: false, ex: 'CONTINUE LISTENING', tracking: '0.22em' },
    { l: 'Numeric · mono',         f: T.mono,  s: 12, w: 500, italic: false, ex: '1:42 / 4:00' },
  ];
  return (
    <div className="sr" style={{
      width: 720, padding: 32, background: T.bg, color: T.text,
      borderRadius: 16, border: `1px solid ${T.lineHi}`,
    }}>
      <div className="sr-mono" style={{ fontSize: 10, color: T.textMute, letterSpacing: '0.25em', textTransform: 'uppercase', marginBottom: 6 }}>Brand · 02</div>
      <div className="sr-serif" style={{ fontSize: 32, fontStyle: 'italic', color: T.text, lineHeight: 1.05, marginBottom: 24 }}>Type system</div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 24, marginBottom: 28 }}>
        {[
          { f: 'Cormorant Garamond', role: 'Display & titles · italic', sample: 'Slow nights, louder feelings.', italic: true, family: T.serif, size: 22 },
          { f: 'Inter Tight', role: 'UI body · numeric', sample: 'Continue listening', italic: false, family: T.sans, size: 18 },
          { f: 'JetBrains Mono', role: 'Eyebrow · timecode', sample: 'FLAC · 24/96', italic: false, family: T.mono, size: 14 },
        ].map(p => (
          <div key={p.f}>
            <div style={{ fontFamily: p.family, fontSize: p.size, fontStyle: p.italic ? 'italic' : 'normal', color: T.text, marginBottom: 6, letterSpacing: '-0.01em' }}>{p.sample}</div>
            <div style={{ fontSize: 11.5, color: T.text, fontWeight: 600 }}>{p.f}</div>
            <div style={{ fontSize: 10.5, color: T.textDim }}>{p.role}</div>
          </div>
        ))}
      </div>

      {scale.map(s => (
        <div key={s.l} style={{
          display: 'flex', alignItems: 'baseline', gap: 16,
          padding: '12px 0', borderTop: `1px solid ${T.line}`,
        }}>
          <div style={{ width: 200, fontSize: 11.5, color: T.textDim }}>{s.l}</div>
          <div style={{ width: 60, fontSize: 10.5, color: T.textMute, fontFamily: T.mono }}>{s.s}px</div>
          <div style={{
            flex: 1, fontFamily: s.f,
            fontSize: s.s, fontWeight: s.w, fontStyle: s.italic ? 'italic' : 'normal',
            color: T.text, letterSpacing: s.tracking || (s.italic ? '-0.01em' : '0'),
            textTransform: s.tracking ? 'uppercase' : 'none',
          }}>{s.ex}</div>
        </div>
      ))}
    </div>
  );
}

function ComponentsCard({ theme = SERENADE }) {
  const T = theme;
  return (
    <div className="sr" style={{
      width: 720, padding: 32, background: T.bg, color: T.text,
      borderRadius: 16, border: `1px solid ${T.lineHi}`,
    }}>
      <div className="sr-mono" style={{ fontSize: 10, color: T.textMute, letterSpacing: '0.25em', textTransform: 'uppercase', marginBottom: 6 }}>Brand · 03</div>
      <div className="sr-serif" style={{ fontSize: 32, fontStyle: 'italic', color: T.text, lineHeight: 1.05, marginBottom: 24 }}>Components</div>

      {/* Buttons */}
      <DSGroup label="Buttons" theme={T}>
        <PillButton theme={T}>Play now</PillButton>
        <PillButton kind="soft" theme={T}>Save</PillButton>
        <PillButton kind="ghost" theme={T} icon={I.add}>Add</PillButton>
        <PillButton kind="primary" size="sm" theme={T} icon={I.play}>Play</PillButton>
        <PillButton kind="primary" size="lg" theme={T} icon={I.fingerprint}>Continue</PillButton>
      </DSGroup>

      {/* Chips */}
      <DSGroup label="Chips · genre & filter" theme={T}>
        <Chip on theme={T} icon={I.flame}>Made for tonight</Chip>
        <Chip theme={T}>Folk</Chip>
        <Chip theme={T}>Ambient</Chip>
        <Chip theme={T} icon={I.downloaded}>Downloaded</Chip>
      </DSGroup>

      {/* Radius */}
      <DSGroup label="Corner radius" theme={T}>
        {[
          { v: 6,  l: 'sm 6' },
          { v: 10, l: 'md 10' },
          { v: 14, l: 'lg 14' },
          { v: 18, l: 'xl 18' },
          { v: 28, l: '2xl 28' },
          { v: 100, l: 'pill' },
        ].map(r => (
          <div key={r.l} style={{ textAlign: 'center' }}>
            <div style={{ width: 48, height: 48, borderRadius: r.v, background: T.surfaceHi, border: `1px solid ${T.lineHi}`, marginBottom: 4 }}/>
            <div style={{ fontSize: 9.5, color: T.textMute, fontFamily: T.mono }}>{r.l}</div>
          </div>
        ))}
      </DSGroup>

      {/* Spacing */}
      <DSGroup label="Spacing scale · 4px base" theme={T}>
        {[4, 8, 12, 16, 20, 24, 32].map(s => (
          <div key={s} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
            <div style={{ width: s, height: 28, background: T.primary, borderRadius: 2 }}/>
            <span style={{ fontSize: 9.5, color: T.textMute, fontFamily: T.mono }}>{s}</span>
          </div>
        ))}
      </DSGroup>

      {/* Skeleton */}
      <DSGroup label="Skeleton & empty" theme={T} stack>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center', width: '100%' }}>
          <div className="sr-shimmer" style={{ width: 48, height: 48, borderRadius: 8 }}/>
          <div style={{ flex: 1 }}>
            <div className="sr-shimmer" style={{ height: 12, borderRadius: 4, marginBottom: 6, width: '70%' }}/>
            <div className="sr-shimmer" style={{ height: 10, borderRadius: 4, width: '40%' }}/>
          </div>
        </div>
        <div style={{
          width: '100%', padding: '20px 16px', borderRadius: 12,
          border: `1px dashed ${T.lineHi}`, textAlign: 'center',
        }}>
          <div className="sr-serif" style={{ fontSize: 18, fontStyle: 'italic', color: T.text, marginBottom: 4 }}>No songs yet</div>
          <div style={{ fontSize: 11.5, color: T.textDim }}>Upload a track or follow a provider to start.</div>
        </div>
      </DSGroup>

      {/* Player atom */}
      <DSGroup label="Player · controls" theme={T}>
        <Icon d={I.shuffle} size={20} stroke={T.primary}/>
        <Icon d={I.prev} size={26} stroke={T.text}/>
        <div style={{ width: 48, height: 48, borderRadius: '50%', background: T.primary, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon d={I.play} size={20} stroke={'#1a0d05'} sw={2}/>
        </div>
        <Icon d={I.next} size={26} stroke={T.text}/>
        <Icon d={I.repeat} size={20} stroke={T.text}/>
      </DSGroup>
    </div>
  );
}

function DSGroup({ label, children, theme = SERENADE, stack = false }) {
  const T = theme;
  return (
    <div style={{
      padding: '14px 0', borderTop: `1px solid ${T.line}`,
      display: 'flex', alignItems: stack ? 'stretch' : 'center', gap: 18,
      flexDirection: stack ? 'column' : 'row',
    }}>
      <div style={{ width: 140, fontSize: 11, color: T.textDim, fontFamily: T.mono, letterSpacing: '0.05em', flexShrink: 0 }}>{label}</div>
      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'center', flex: 1 }}>
        {children}
      </div>
    </div>
  );
}

// Direction note card — text-heavy summary
function DirectionCard({ theme = SERENADE }) {
  const T = theme;
  return (
    <div className="sr" style={{
      width: 720, padding: 36, background: T.bg, color: T.text,
      borderRadius: 16, border: `1px solid ${T.lineHi}`,
      backgroundImage: `radial-gradient(70% 60% at 0% 0%, ${T.plum}33, transparent 50%), radial-gradient(70% 60% at 100% 100%, ${T.coral}22, transparent 50%)`,
    }}>
      <Wordmark size={26} theme={T}/>
      <div className="sr-serif" style={{ fontSize: 44, fontStyle: 'italic', color: T.text, lineHeight: 1.05, margin: '20px 0 18px', letterSpacing: '-0.01em' }}>
        A quieter place<br/>to listen.
      </div>
      <div style={{ fontSize: 14, color: T.textDim, lineHeight: 1.6, maxWidth: 540, marginBottom: 28 }}>
        Serenade is a music app for after-hours listening — for the long drive, the closing kitchen, the second chapter. We compete on intimacy and sound, not throughput. The interface fades. The track stays.
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 28 }}>
        <div>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 8 }}>Personality</div>
          <div style={{ fontSize: 13, color: T.text, lineHeight: 1.7 }}>
            poetic · intimate · cinematic · warm · hi-fi · nocturnal · grown-up
          </div>
        </div>
        <div>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 8 }}>Visual motifs</div>
          <div style={{ fontSize: 13, color: T.text, lineHeight: 1.7 }}>
            crescent mark · vinyl rings · candlelit gradients · italic serif headlines
          </div>
        </div>
        <div>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 8 }}>Why not green</div>
          <div style={{ fontSize: 12.5, color: T.textDim, lineHeight: 1.6 }}>
            Spotify owns "energetic green on ink." Serenade owns "candlelight on plum." Different temperature, different time of day, different chair.
          </div>
        </div>
        <div>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 8 }}>Anonymous-first</div>
          <div style={{ fontSize: 12.5, color: T.textDim, lineHeight: 1.6 }}>
            Every protected action prompts a sheet, never a wall. Listening never blocks.
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { PaletteCard, TypeCard, ComponentsCard, DirectionCard });
