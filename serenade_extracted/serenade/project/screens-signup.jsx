// Serenade — Signup wizard (3 steps) + Setup screen (preferences, skippable)

// SIGNUP WIZARD ─────────────────────────────────────────────
function SignupWizard({ theme = SERENADE, step = 1 }) {
  const T = theme;
  const total = 3;

  return (
    <Phone theme={T}>
      <div style={{
        position: 'absolute', top: -36, left: 0, right: 0, height: 240,
        background: `radial-gradient(80% 100% at 50% 0%, ${T.coral}33, transparent 60%)`,
      }}/>

      {/* header */}
      <div style={{ position: 'relative', padding: '14px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Icon d={I.back} size={22} stroke={T.text}/>
        <span className="sr-mono" style={{ fontSize: 10.5, color: T.textDim, letterSpacing: '0.18em' }}>
          STEP {step} <span style={{ color: T.textOff }}>/ {total}</span>
        </span>
        <span style={{ fontSize: 13, color: T.textDim }}>Cancel</span>
      </div>

      {/* progress segments */}
      <div style={{ display: 'flex', gap: 4, padding: '4px 18px 0' }}>
        {[1,2,3].map(n => (
          <div key={n} style={{
            flex: 1, height: 3, borderRadius: 2,
            background: n <= step ? T.primary : T.surfaceHi,
          }}/>
        ))}
      </div>

      {step === 1 && <SignupStep1 theme={T}/>}
      {step === 2 && <SignupStep2 theme={T}/>}
      {step === 3 && <SignupStep3 theme={T}/>}

      {/* footer cta */}
      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: 18,
        padding: '14px 18px',
        background: `linear-gradient(180deg, transparent, ${T.bgDeep} 30%)`,
      }}>
        <PillButton kind="primary" size="lg" full theme={T}
          icon={step === total ? I.check : undefined}>
          {step === 1 ? 'Continue' : step === 2 ? 'Next' : 'Create account'}
        </PillButton>
      </div>
    </Phone>
  );
}

// Step 1 — name & email
function SignupStep1({ theme: T }) {
  return (
    <div style={{ padding: '22px 24px 0' }}>
      <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 8 }}>
        New listener
      </div>
      <div className="sr-serif" style={{ fontSize: 30, fontStyle: 'italic', color: T.text, lineHeight: 1.05, marginBottom: 6 }}>
        Let's start<br/>with a name.
      </div>
      <div style={{ fontSize: 12.5, color: T.textDim, lineHeight: 1.5, marginBottom: 26, maxWidth: 280 }}>
        We'll use it on your profile and shared playlists. You can always change it later.
      </div>

      <Field label="DISPLAY NAME" value="Jules Mara" focused theme={T}/>
      <Field label="EMAIL" value="jules@serenade.fm" theme={T} valid/>
      <Field label="PASSWORD" value="••••••••••" theme={T} hint="At least 10 characters · use a phrase you'll remember"/>

      <div style={{ marginTop: 18, display: 'flex', gap: 10, alignItems: 'flex-start' }}>
        <div style={{
          width: 18, height: 18, borderRadius: 5,
          background: T.primary, display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexShrink: 0, marginTop: 1,
        }}>
          <Icon d={I.check} size={12} stroke={'#1a0d05'} sw={2.6}/>
        </div>
        <div style={{ fontSize: 11.5, color: T.textDim, lineHeight: 1.5 }}>
          I agree to the <span style={{ color: T.primary }}>terms of listening</span> and our quiet little <span style={{ color: T.primary }}>privacy promise</span>.
        </div>
      </div>
    </div>
  );
}

// Step 2 — verify email (code)
function SignupStep2({ theme: T }) {
  const code = ['4','7','2','1','',''];
  return (
    <div style={{ padding: '22px 24px 0' }}>
      <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 8 }}>
        Confirm it's you
      </div>
      <div className="sr-serif" style={{ fontSize: 30, fontStyle: 'italic', color: T.text, lineHeight: 1.05, marginBottom: 6 }}>
        Six digits.<br/>Then you're in.
      </div>
      <div style={{ fontSize: 12.5, color: T.textDim, lineHeight: 1.5, marginBottom: 30, maxWidth: 280 }}>
        We sent a code to <span style={{ color: T.text }}>jules@serenade.fm</span>. It expires in 10 minutes.
      </div>

      {/* code boxes */}
      <div style={{ display: 'flex', gap: 8, justifyContent: 'space-between', marginBottom: 24 }}>
        {code.map((d, i) => {
          const filled = !!d;
          const cursor = i === 4;
          return (
            <div key={i} style={{
              flex: 1, height: 56, maxWidth: 48,
              borderRadius: 12,
              background: filled ? T.surface : 'transparent',
              border: `1.5px solid ${cursor ? T.primary : filled ? T.lineHi : T.line}`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontFamily: T.serif, fontStyle: 'italic',
              fontSize: 26, fontWeight: 500, color: T.text,
              position: 'relative',
            }}>
              {d}
              {cursor && (
                <div style={{
                  width: 2, height: 24, background: T.primary,
                  animation: 'sr-pulse 1s ease-in-out infinite',
                }}/>
              )}
            </div>
          );
        })}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12.5, color: T.textDim }}>
        <Icon d={I.bell} size={14} stroke={T.textDim}/>
        Didn't see it? <span style={{ color: T.primary, fontWeight: 600 }}>Resend in 0:42</span>
      </div>

      <div style={{
        marginTop: 26, padding: 14,
        background: 'rgba(232,176,122,0.06)', borderRadius: 10,
        border: `1px solid ${T.primary}33`,
        display: 'flex', gap: 10, fontSize: 11.5, color: T.textDim, lineHeight: 1.5,
      }}>
        <Icon d={I.lock} size={16} stroke={T.primary}/>
        <span>Your refresh token will be sealed in this device's secure enclave. Sign-out clears it.</span>
      </div>
    </div>
  );
}

// Step 3 — secure
function SignupStep3({ theme: T }) {
  return (
    <div style={{ padding: '22px 24px 0' }}>
      <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 8 }}>
        Almost there
      </div>
      <div className="sr-serif" style={{ fontSize: 30, fontStyle: 'italic', color: T.text, lineHeight: 1.05, marginBottom: 6 }}>
        How will you<br/>unlock Serenade?
      </div>
      <div style={{ fontSize: 12.5, color: T.textDim, lineHeight: 1.5, marginBottom: 24, maxWidth: 290 }}>
        Pick how you want to come back. You can change this anytime in Settings.
      </div>

      <ChoiceCard
        theme={T}
        on
        icon={I.fingerprint}
        title="Biometric"
        sub="Fingerprint or face — fastest. We'll seal your token to your hardware key."
        badge="Recommended"
      />
      <ChoiceCard
        theme={T}
        icon={I.lock}
        title="Password each time"
        sub="A bit slower, works on any device."
      />
      <ChoiceCard
        theme={T}
        icon={I.user}
        title="Stay signed in"
        sub="Convenient — but anyone with this phone can listen."
      />
    </div>
  );
}

function Field({ label, value, hint, focused, valid, theme: T }) {
  return (
    <div style={{ marginBottom: 14 }}>
      <div style={{ fontSize: 10, color: T.textMute, letterSpacing: '0.15em', marginBottom: 6 }}>{label}</div>
      <div style={{
        height: 50, padding: '0 14px',
        background: T.surface,
        border: `${focused ? 1.5 : 1}px solid ${focused ? T.primary : T.line}`,
        borderRadius: 12,
        display: 'flex', alignItems: 'center',
        color: T.text, fontSize: 14.5,
      }}>
        <span style={{ flex: 1 }}>{value}</span>
        {valid && <Icon d={I.check} size={16} stroke={T.good} sw={2.4}/>}
      </div>
      {hint && <div style={{ fontSize: 10.5, color: T.textMute, marginTop: 5 }}>{hint}</div>}
    </div>
  );
}

function ChoiceCard({ icon, title, sub, badge, on, theme: T }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'flex-start', gap: 12,
      padding: 14, marginBottom: 10,
      background: on ? 'rgba(232,176,122,0.06)' : T.surface,
      border: `${on ? 1.5 : 1}px solid ${on ? T.primary : T.line}`,
      borderRadius: 14,
    }}>
      <div style={{
        width: 38, height: 38, borderRadius: 10,
        background: on ? T.primary : T.surfaceHi,
        display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
      }}>
        <Icon d={icon} size={18} stroke={on ? '#1a0d05' : T.text} sw={1.8}/>
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 3 }}>
          <span style={{ fontSize: 14, fontWeight: 600, color: T.text }}>{title}</span>
          {badge && <span style={{
            fontSize: 9, fontWeight: 700, color: T.primary,
            padding: '1px 6px', borderRadius: 3,
            border: `1px solid ${T.primary}66`, letterSpacing: '0.1em', textTransform: 'uppercase',
          }}>{badge}</span>}
        </div>
        <div style={{ fontSize: 11.5, color: T.textDim, lineHeight: 1.45 }}>{sub}</div>
      </div>
      <div style={{
        width: 20, height: 20, borderRadius: '50%',
        border: `1.5px solid ${on ? T.primary : T.line}`,
        background: on ? T.primary : 'transparent',
        display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, marginTop: 2,
      }}>
        {on && <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#1a0d05' }}/>}
      </div>
    </div>
  );
}

// SETUP — preferences after signup, skippable ────────────────
function SetupScreen({ theme = SERENADE }) {
  const T = theme;
  const moods = [
    { n: 'Folk',       hue: T.amber },
    { n: 'Ambient',    hue: T.plum,    on: true },
    { n: 'Jazz',       hue: T.coral,   on: true },
    { n: 'Classical',  hue: T.indigo },
    { n: 'R&B',        hue: T.primary, on: true },
    { n: 'Electronic', hue: '#8a78c8' },
    { n: 'Hip-hop',    hue: '#a4624c' },
    { n: 'Rock',       hue: '#7a5c93' },
    { n: 'Country',    hue: '#c08458' },
  ];
  return (
    <Phone theme={T}>
      <div className="sr-noscroll" style={{ height: '100%', overflow: 'auto', paddingBottom: 110 }}>
        {/* warm halo */}
        <div style={{
          position: 'absolute', top: -36, left: 0, right: 0, height: 260,
          background: `radial-gradient(70% 100% at 50% 0%, ${T.primary}22, transparent 60%)`,
          pointerEvents: 'none',
        }}/>

        <div style={{ position: 'relative', padding: '14px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span className="sr-mono" style={{ fontSize: 10.5, color: T.textMute, letterSpacing: '0.18em' }}>SETUP · OPTIONAL</span>
          <span style={{ fontSize: 13, color: T.textDim, fontWeight: 500 }}>Skip</span>
        </div>

        <div style={{ padding: '14px 22px 0', position: 'relative' }}>
          <div className="sr-serif" style={{ fontSize: 28, fontStyle: 'italic', color: T.text, lineHeight: 1.05, marginBottom: 6 }}>
            Tune the room<br/>to your taste.
          </div>
          <div style={{ fontSize: 12.5, color: T.textDim, lineHeight: 1.5 }}>
            Three quick choices. Or skip — Serenade learns either way.
          </div>
        </div>

        {/* 1 · Theme */}
        <div style={{ padding: '24px 22px 0' }}>
          <SetupHeading n="01" title="Visual theme" sub="Velvet for warm nights · Aurora for electric ones." theme={T}/>
          <div style={{ display: 'flex', gap: 10 }}>
            {[
              { id: 'velvet', n: 'Midnight Velvet', c1: SERENADE.plum,  c2: SERENADE.coral,  on: true },
              { id: 'aurora', n: 'Aurora Pulse',    c1: AURORA.plum,    c2: AURORA.primary },
            ].map(s => (
              <div key={s.id} style={{
                flex: 1, padding: 12, borderRadius: 12,
                border: `1.5px solid ${s.on ? T.primary : T.line}`,
                background: s.on ? 'rgba(232,176,122,0.06)' : T.surface,
              }}>
                <div style={{
                  height: 60, borderRadius: 8, marginBottom: 10,
                  background: `linear-gradient(135deg, ${s.c1}, ${s.c2})`,
                  position: 'relative', overflow: 'hidden',
                }}>
                  <div style={{
                    position: 'absolute', inset: '20%',
                    border: '1px solid rgba(255,255,255,0.25)', borderRadius: '50%',
                  }}/>
                </div>
                <div style={{ fontSize: 12.5, fontWeight: 600, color: T.text, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  {s.n}
                  {s.on && (
                    <div style={{
                      width: 18, height: 18, borderRadius: '50%', background: T.primary,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                    }}>
                      <Icon d={I.check} size={11} stroke={'#1a0d05'} sw={2.6}/>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 2 · Genres */}
        <div style={{ padding: '24px 22px 0' }}>
          <SetupHeading n="02" title="Pick a few you love" sub="Tap three or more — we'll seed your evening." theme={T}/>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {moods.map(m => (
              <span key={m.n} style={{
                padding: '8px 14px', borderRadius: 100,
                fontSize: 13, fontWeight: 500,
                background: m.on ? `linear-gradient(135deg, ${m.hue}, ${T.bgDeep})` : 'transparent',
                color: m.on ? '#fff' : T.text,
                border: `1px solid ${m.on ? 'transparent' : T.line}`,
                display: 'inline-flex', alignItems: 'center', gap: 6,
                boxShadow: m.on ? `0 4px 12px ${m.hue}55` : 'none',
              }}>
                {m.on && <Icon d={I.check} size={12} stroke={'#fff'} sw={2.6}/>}
                {m.n}
              </span>
            ))}
          </div>
          <div style={{ fontSize: 11, color: T.textMute, marginTop: 10, fontFamily: T.mono, letterSpacing: '0.1em' }}>
            3 / 9 SELECTED
          </div>
        </div>

        {/* 3 · Audio quality */}
        <div style={{ padding: '24px 22px 0' }}>
          <SetupHeading n="03" title="How should it sound?" sub="You can override per-network later." theme={T}/>
          {[
            { l: 'Hi-Fi (FLAC)',     s: 'Wi-Fi · headphones recommended', on: true,  tag: 'Lossless' },
            { l: 'High (256 kbps)',  s: 'Cellular · best of both',       on: false },
            { l: 'Saver (96 kbps)',  s: 'Cellular · for long trips',     on: false },
          ].map(o => (
            <div key={o.l} style={{
              display: 'flex', alignItems: 'center', gap: 12,
              padding: '12px 14px', marginBottom: 8,
              background: o.on ? 'rgba(232,176,122,0.06)' : T.surface,
              border: `${o.on ? 1.5 : 1}px solid ${o.on ? T.primary : T.line}`,
              borderRadius: 12,
            }}>
              <div style={{
                width: 18, height: 18, borderRadius: '50%',
                border: `1.5px solid ${o.on ? T.primary : T.line}`,
                background: o.on ? T.primary : 'transparent',
                display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
              }}>
                {o.on && <div style={{ width: 7, height: 7, borderRadius: '50%', background: '#1a0d05' }}/>}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 13.5, fontWeight: 500, color: T.text, display: 'flex', alignItems: 'center', gap: 6 }}>
                  {o.l}
                  {o.tag && <span style={{
                    fontSize: 9, fontWeight: 700, color: T.primary,
                    padding: '1px 5px', borderRadius: 3,
                    border: `1px solid ${T.primary}55`, letterSpacing: '0.1em',
                  }}>{o.tag}</span>}
                </div>
                <div style={{ fontSize: 11, color: T.textDim }}>{o.s}</div>
              </div>
            </div>
          ))}
        </div>

        {/* 4 · little toggles */}
        <div style={{ padding: '24px 22px 0' }}>
          <SetupHeading n="04" title="One last thing" sub="Tweak any of these later in Settings · You." theme={T}/>
          {[
            { l: 'Generate AI lyrics for tracks without them', on: true },
            { l: 'Background play on lock screen',             on: true },
            { l: 'Email me weekly mix recommendations',        on: false },
          ].map(o => (
            <div key={o.l} style={{
              display: 'flex', alignItems: 'center', justifyContent: 'space-between',
              padding: '12px 0', borderBottom: `1px solid ${T.line}`,
            }}>
              <span style={{ fontSize: 13, color: T.text, paddingRight: 12 }}>{o.l}</span>
              <div style={{
                width: 38, height: 22, borderRadius: 11, flexShrink: 0,
                background: o.on ? T.primary : T.surface, border: `1px solid ${T.line}`,
                padding: 2, display: 'flex', justifyContent: o.on ? 'flex-end' : 'flex-start',
              }}>
                <div style={{ width: 16, height: 16, borderRadius: '50%', background: o.on ? '#1a0d05' : T.textMute }}/>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* sticky footer cta */}
      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: 18,
        padding: '12px 18px',
        background: `linear-gradient(180deg, transparent, ${T.bgDeep} 30%)`,
        display: 'flex', gap: 10,
      }}>
        <PillButton kind="ghost" size="lg" theme={T}>Skip</PillButton>
        <div style={{ flex: 1 }}>
          <PillButton kind="primary" size="lg" full theme={T} icon={I.check}>Tune & enter</PillButton>
        </div>
      </div>
    </Phone>
  );
}

function SetupHeading({ n, title, sub, theme: T }) {
  return (
    <div style={{ marginBottom: 12, display: 'flex', alignItems: 'flex-start', gap: 12 }}>
      <div className="sr-mono" style={{
        fontSize: 11, color: T.primary, letterSpacing: '0.15em',
        padding: '3px 8px', borderRadius: 4,
        border: `1px solid ${T.primary}55`,
        marginTop: 2,
      }}>{n}</div>
      <div style={{ flex: 1 }}>
        <div className="sr-serif" style={{ fontSize: 19, fontStyle: 'italic', color: T.text, lineHeight: 1.1 }}>{title}</div>
        <div style={{ fontSize: 11.5, color: T.textDim, marginTop: 3 }}>{sub}</div>
      </div>
    </div>
  );
}

Object.assign(window, { SignupWizard, SetupScreen });
