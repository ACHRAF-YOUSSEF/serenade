// Serenade — Screen 1-5: Splash, Welcome, Sign in, Home, Search

// 1. SPLASH ───────────────────────────────────────────────
function SplashScreen({ theme = SERENADE }) {
  const T = theme;
  return (
    <Phone theme={T} hideStatus hideHome>
      <div style={{
        position: 'absolute', inset: 0,
        background: `radial-gradient(120% 80% at 50% 30%, ${T.plum}55, ${T.bg} 55%, ${T.bgDeep})`,
      }}/>
      {/* concentric rings, like a vinyl */}
      <div style={{
        position: 'absolute', inset: 0,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        {[280, 220, 160, 100].map((s, i) => (
          <div key={s} style={{
            position: 'absolute', width: s, height: s, borderRadius: '50%',
            border: `1px solid rgba(255,255,255,${0.04 + i * 0.02})`,
          }}/>
        ))}
        <div style={{
          width: 60, height: 60, borderRadius: '50%',
          background: `linear-gradient(140deg, ${T.primaryHi}, ${T.coral})`,
          boxShadow: `0 0 60px ${T.primary}66, 0 0 0 1px rgba(255,255,255,0.1)`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: '#1a0d05', fontFamily: T.serif, fontStyle: 'italic',
          fontSize: 32, fontWeight: 500,
        }}>S</div>
      </div>
      <div style={{
        position: 'absolute', bottom: 60, left: 0, right: 0,
        textAlign: 'center',
      }}>
        <div className="sr-serif" style={{
          fontSize: 32, fontStyle: 'italic', color: T.text,
          letterSpacing: '-0.01em', marginBottom: 6,
        }}>Serenade</div>
        <div className="sr-mono" style={{
          fontSize: 10, color: T.textMute, letterSpacing: '0.3em', textTransform: 'uppercase',
        }}>music for after dark</div>
      </div>
    </Phone>
  );
}

// 2. WELCOME ───────────────────────────────────────────────
function WelcomeScreen({ theme = SERENADE }) {
  const T = theme;
  return (
    <Phone theme={T}>
      {/* hero gradient */}
      <div style={{
        position: 'absolute', top: -36, left: 0, right: 0, height: 380,
        background: `radial-gradient(80% 100% at 50% 0%, ${T.coral}33 0%, ${T.plum}22 30%, transparent 70%)`,
      }}/>
      <div style={{ padding: '40px 24px 0', position: 'relative' }}>
        <Wordmark size={22} theme={T}/>
      </div>

      {/* artwork stack */}
      <div style={{
        position: 'absolute', top: 110, left: 0, right: 0,
        display: 'flex', justifyContent: 'center', gap: 0,
      }}>
        <div style={{ transform: 'rotate(-9deg) translateX(28px)', filter: 'blur(1px) brightness(.7)' }}>
          <ArtSeed seed="River Hymn" size={140} radius={14} vivid theme={T}/>
        </div>
        <div style={{ transform: 'translateY(-12px)', zIndex: 2, boxShadow: '0 30px 60px rgba(0,0,0,0.5)', borderRadius: 14 }}>
          <ArtSeed seed="Lantern Hours" size={170} radius={14} vivid theme={T}/>
        </div>
        <div style={{ transform: 'rotate(9deg) translateX(-28px)', filter: 'blur(1px) brightness(.7)' }}>
          <ArtSeed seed="Velvet Lull" size={140} radius={14} vivid theme={T}/>
        </div>
      </div>

      <div style={{ position: 'absolute', left: 0, right: 0, bottom: 28, padding: '0 24px' }}>
        <div className="sr-serif" style={{
          fontSize: 34, fontStyle: 'italic', lineHeight: 1.05, color: T.text,
          marginBottom: 10, textWrap: 'pretty',
        }}>Slow nights,<br/>louder feelings.</div>
        <div style={{ fontSize: 13.5, color: T.textDim, lineHeight: 1.5, marginBottom: 24, maxWidth: 280 }}>
          A quieter place to listen. Choose how you'd like to enter — we don't mind if you stay a stranger.
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <PillButton kind="primary" size="lg" full theme={T} icon={I.fingerprint}>
            Continue with biometric
          </PillButton>
          <PillButton kind="soft" size="lg" full theme={T} icon={I.lock}>
            Sign in with password
          </PillButton>
          <button className="sr-press" style={{
            background: 'transparent', border: 'none', color: T.textDim,
            fontSize: 13.5, fontWeight: 500, padding: '14px 0',
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
          }}>
            Just listen — stay anonymous
            <Icon d={I.chev} size={14} stroke={T.textDim}/>
          </button>
        </div>
      </div>
    </Phone>
  );
}

// 3. SIGN IN ───────────────────────────────────────────────
function SignInScreen({ theme = SERENADE }) {
  const T = theme;
  return (
    <Phone theme={T}>
      <div style={{ padding: '24px 22px 0' }}>
        <Icon d={I.back} size={22} stroke={T.text}/>
      </div>
      <div style={{ padding: '32px 24px 0' }}>
        <div className="sr-mono" style={{ fontSize: 10, color: T.textMute, letterSpacing: '0.25em', textTransform: 'uppercase', marginBottom: 8 }}>
          Welcome back
        </div>
        <div className="sr-serif" style={{ fontSize: 34, fontStyle: 'italic', color: T.text, lineHeight: 1, marginBottom: 28 }}>
          Pick up<br/>where you left off.
        </div>

        {/* Email field */}
        <div style={{ marginBottom: 14 }}>
          <div style={{ fontSize: 11, color: T.textDim, marginBottom: 6, letterSpacing: '0.05em' }}>EMAIL</div>
          <div style={{
            height: 52, padding: '0 16px',
            background: T.surface, border: `1px solid ${T.line}`, borderRadius: 12,
            display: 'flex', alignItems: 'center',
            color: T.text, fontSize: 15,
          }}>
            jules@serenade.fm
            <div style={{ flex: 1 }}/>
            <Icon d={I.check} size={18} stroke={T.good}/>
          </div>
        </div>

        {/* Password */}
        <div style={{ marginBottom: 8 }}>
          <div style={{ fontSize: 11, color: T.textDim, marginBottom: 6, letterSpacing: '0.05em', display: 'flex', justifyContent: 'space-between' }}>
            <span>PASSWORD</span><span style={{ color: T.primary, fontWeight: 600 }}>Forgot?</span>
          </div>
          <div style={{
            height: 52, padding: '0 16px',
            background: T.surface, border: `1.5px solid ${T.primary}`, borderRadius: 12,
            display: 'flex', alignItems: 'center',
            color: T.text, fontSize: 18, letterSpacing: '0.4em',
          }}>
            ••••••••
          </div>
        </div>

        <div style={{ fontSize: 12, color: T.textMute, marginBottom: 20, marginTop: 14 }}>
          We'll keep your refresh token in your phone's secure enclave.
        </div>

        <PillButton kind="primary" size="lg" full theme={T}>Sign in</PillButton>

        <div style={{
          display: 'flex', alignItems: 'center', gap: 12, margin: '24px 0',
        }}>
          <div style={{ flex: 1, height: 1, background: T.line }}/>
          <span style={{ fontSize: 11, color: T.textMute, letterSpacing: '0.15em' }}>OR</span>
          <div style={{ flex: 1, height: 1, background: T.line }}/>
        </div>

        <div style={{ display: 'flex', gap: 10 }}>
          <PillButton kind="soft" size="md" full theme={T} icon={I.fingerprint}>Biometric</PillButton>
          <PillButton kind="soft" size="md" full theme={T} icon={I.user}>Anonymous</PillButton>
        </div>
      </div>
    </Phone>
  );
}

// 3b. BIOMETRIC SHEET ─────────────────────────────────────
function BiometricSheet({ theme = SERENADE }) {
  const T = theme;
  return (
    <Phone theme={T}>
      {/* dimmed previous screen */}
      <div style={{
        position: 'absolute', inset: 0,
        background: `linear-gradient(180deg, ${T.bg}, ${T.bgDeep})`,
        opacity: .6,
      }}/>
      <div style={{
        position: 'absolute', inset: 0,
        background: 'rgba(0,0,0,0.55)',
      }}/>
      <div style={{
        position: 'absolute', left: 12, right: 12, bottom: 12,
        background: T.surface,
        borderRadius: 24, padding: '28px 24px 24px',
        border: `1px solid ${T.lineHi}`,
        boxShadow: '0 -20px 60px rgba(0,0,0,0.5)',
      }}>
        <div style={{ width: 36, height: 4, background: T.line, borderRadius: 2, margin: '0 auto 22px' }}/>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 18 }}>
          <div style={{
            width: 88, height: 88, borderRadius: '50%',
            background: `radial-gradient(circle at 50% 40%, ${T.primary}33, transparent 70%)`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            position: 'relative',
          }}>
            <div style={{
              position: 'absolute', inset: 0, borderRadius: '50%',
              border: `1px solid ${T.primary}`, opacity: .3,
              animation: 'sr-pulse 1.6s ease-in-out infinite',
            }}/>
            <Icon d={I.fingerprint} size={42} stroke={T.primary} sw={1.5}/>
          </div>
        </div>
        <div className="sr-serif" style={{ textAlign: 'center', fontSize: 22, fontStyle: 'italic', marginBottom: 6, color: T.text }}>
          Touch to continue
        </div>
        <div style={{ textAlign: 'center', fontSize: 13, color: T.textDim, marginBottom: 22 }}>
          Place your finger on the sensor to unlock your library.
        </div>
        <PillButton kind="ghost" size="md" full theme={T}>Use password instead</PillButton>
      </div>
    </Phone>
  );
}

// 4. HOME / DISCOVER ───────────────────────────────────────
function HomeScreen({ theme = SERENADE }) {
  const T = theme;
  const greetingChips = ['Made for tonight', 'Slow returns', 'Background', 'Heartbreak hi-fi'];
  const featured = [
    { title: 'Lantern Hours', sub: 'A late-night mix · 38 tracks' },
    { title: 'River Hymn',    sub: 'Folk · ambient · returning' },
    { title: 'Velvet Lull',   sub: 'For winding down' },
    { title: 'Cinder Choir',  sub: 'Cinematic vocal' },
  ];
  const recent = [
    { title: 'Bonfires in Reverse', artist: 'Hana Okuda' },
    { title: 'Slow Tide',           artist: 'Vesper & the Hours' },
    { title: 'A Letter in November',artist: 'Iben Mell' },
    { title: 'The Long Way Down',   artist: 'Rooks Aubrey' },
  ];

  return (
    <Phone theme={T}>
      <div className="sr-noscroll" style={{ height: '100%', overflow: 'auto', paddingBottom: 130 }}>
        {/* atmospheric top */}
        <div style={{
          position: 'relative', height: 220,
          background: `radial-gradient(90% 100% at 20% 0%, ${T.plum}55, transparent 60%), radial-gradient(80% 80% at 100% 0%, ${T.coral}33, transparent 50%)`,
        }}>
          <div style={{ padding: '20px 18px 0', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 4 }}>Friday · 11:42 pm</div>
              <div className="sr-serif" style={{ fontSize: 26, fontStyle: 'italic', color: T.text, lineHeight: 1 }}>Good evening, Jules.</div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
              <Icon d={I.bell} size={22} stroke={T.text}/>
              <div style={{
                width: 32, height: 32, borderRadius: '50%',
                background: `linear-gradient(140deg, ${T.coral}, ${T.plum})`,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: T.text, fontFamily: T.serif, fontStyle: 'italic', fontWeight: 500,
                fontSize: 14,
              }}>J</div>
            </div>
          </div>

          {/* mood chips */}
          <div className="sr-noscroll" style={{ marginTop: 20, paddingLeft: 18, overflowX: 'auto', whiteSpace: 'nowrap' }}>
            <div style={{ display: 'inline-flex', gap: 8 }}>
              {greetingChips.map((c, i) => <Chip key={c} on={i === 0} theme={T} icon={i === 0 ? I.flame : undefined}>{c}</Chip>)}
              <div style={{ width: 18 }}/>
            </div>
          </div>
        </div>

        {/* featured card — "Made for tonight" */}
        <div style={{ padding: '8px 18px 0' }}>
          <div style={{
            position: 'relative',
            borderRadius: 18, overflow: 'hidden',
            background: `linear-gradient(135deg, ${T.indigo}, ${T.plum} 60%, ${T.coral}AA)`,
            padding: '20px 18px',
            border: `1px solid ${T.lineHi}`,
            display: 'flex', alignItems: 'center', gap: 16,
            minHeight: 132,
          }}>
            <div style={{ flex: 1 }}>
              <div className="sr-mono" style={{ fontSize: 9.5, color: 'rgba(255,255,255,0.7)', letterSpacing: '0.22em', textTransform: 'uppercase', marginBottom: 6 }}>Daily Mix · 01</div>
              <div className="sr-serif" style={{ fontSize: 22, fontStyle: 'italic', color: '#fff', lineHeight: 1.05, marginBottom: 4 }}>Lantern Hours</div>
              <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.78)', marginBottom: 14 }}>38 tracks · 2h 14m</div>
              <PillButton size="sm" theme={T} icon={I.play} style={{ background: T.primary, color: '#1a0d05' }}>Play now</PillButton>
            </div>
            <div style={{ position: 'relative' }}>
              <ArtSeed seed="Lantern Hours" size={92} radius={10} vivid theme={T}/>
              <div style={{
                position: 'absolute', top: -6, right: -6,
                width: 18, height: 18, borderRadius: '50%',
                background: T.primary, color: '#1a0d05',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 10, fontWeight: 700,
              }}>♪</div>
            </div>
          </div>
        </div>

        {/* recently played */}
        <div style={{ marginTop: 24 }}>
          <Section eyebrow="Continue listening" title="Recently played" action="See all" theme={T}>
            <div className="sr-noscroll" style={{ overflowX: 'auto', paddingLeft: 18 }}>
              <div style={{ display: 'inline-flex', gap: 12, paddingRight: 18 }}>
                {recent.map(r => (
                  <div key={r.title} style={{ width: 132 }}>
                    <ArtSeed seed={r.title} size={132} radius={10} vivid theme={T}/>
                    <div style={{ fontSize: 13, fontWeight: 500, color: T.text, marginTop: 8, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{r.title}</div>
                    <div style={{ fontSize: 11.5, color: T.textDim, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{r.artist}</div>
                  </div>
                ))}
              </div>
            </div>
          </Section>
        </div>

        {/* curated rows */}
        <Section eyebrow="Curated" title="For your evening" action="See all" theme={T}>
          <div style={{ padding: '0 18px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
            {featured.slice(0, 4).map((p, i) => (
              <div key={p.title} style={{
                background: T.surface, borderRadius: 12,
                padding: 10, display: 'flex', alignItems: 'center', gap: 10,
                border: `1px solid ${T.line}`,
              }}>
                <ArtSeed seed={p.title} size={42} radius={6} vivid theme={T}/>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 12.5, fontWeight: 600, color: T.text, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{p.title}</div>
                  <div style={{ fontSize: 10.5, color: T.textDim, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{p.sub}</div>
                </div>
              </div>
            ))}
          </div>
        </Section>

        {/* poetic interstitial — visual motif */}
        <div style={{ padding: '8px 18px 24px' }}>
          <div style={{
            border: `1px dashed ${T.lineHi}`, borderRadius: 14, padding: '20px 18px',
            background: `linear-gradient(180deg, ${T.surface}, transparent)`,
            display: 'flex', gap: 14, alignItems: 'center',
          }}>
            <div style={{
              width: 44, height: 44, borderRadius: '50%',
              background: `radial-gradient(circle at 40% 40%, ${T.primary}, ${T.coral})`,
              boxShadow: `0 0 30px ${T.primary}55`,
            }}/>
            <div style={{ flex: 1 }}>
              <div className="sr-serif" style={{ fontSize: 17, fontStyle: 'italic', color: T.text, marginBottom: 2 }}>Tonight's whisper</div>
              <div style={{ fontSize: 12, color: T.textDim }}>A 7-track mix that follows the weather where you are.</div>
            </div>
            <Icon d={I.chev} size={18} stroke={T.textDim}/>
          </div>
        </div>
      </div>

      <MiniPlayer track={{ title: 'Slow Tide', artist: 'Vesper & the Hours' }} theme={T}/>
      <BottomNav active="home" theme={T}/>
    </Phone>
  );
}

// 5. SEARCH ───────────────────────────────────────────────
function SearchScreen({ theme = SERENADE }) {
  const T = theme;
  const genres = [
    { name: 'Folk',       hue: T.amber },
    { name: 'Ambient',    hue: T.plum },
    { name: 'Jazz',       hue: T.coral },
    { name: 'Classical',  hue: T.indigo },
    { name: 'R&B',        hue: T.primary },
    { name: 'Electronic', hue: '#8a78c8' },
    { name: 'Hip-hop',    hue: '#a4624c' },
    { name: 'Rock',       hue: '#7a5c93' },
  ];

  return (
    <Phone theme={T}>
      <div style={{ padding: '20px 18px 0' }}>
        <div className="sr-serif" style={{ fontSize: 26, fontStyle: 'italic', color: T.text, marginBottom: 14 }}>
          Search
        </div>
        <div style={{
          height: 48, padding: '0 14px',
          background: T.surface, border: `1px solid ${T.line}`, borderRadius: 14,
          display: 'flex', alignItems: 'center', gap: 10,
        }}>
          <Icon d={I.search} size={18} stroke={T.textDim}/>
          <span style={{ fontSize: 14, color: T.textDim, flex: 1 }}>Songs, artists, lyrics, moods…</span>
          <Icon d={I.mic} size={18} stroke={T.textDim}/>
        </div>

        <div style={{ display: 'flex', gap: 8, marginTop: 14, flexWrap: 'wrap' }}>
          <Chip theme={T} icon={I.filter}>All sources</Chip>
          <Chip theme={T} on>Tracks</Chip>
          <Chip theme={T}>Playlists</Chip>
          <Chip theme={T}>Artists</Chip>
          <Chip theme={T}>Lyrics</Chip>
        </div>

        <div style={{ marginTop: 20 }}>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: 12 }}>Browse by genre</div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
            {genres.map(g => (
              <div key={g.name} style={{
                position: 'relative', height: 76, borderRadius: 12, overflow: 'hidden',
                background: `linear-gradient(135deg, ${g.hue}, ${T.bgDeep})`,
                padding: 12, display: 'flex', alignItems: 'flex-start',
              }}>
                <div className="sr-serif" style={{ fontSize: 18, fontStyle: 'italic', color: T.text, fontWeight: 500 }}>{g.name}</div>
                <div style={{
                  position: 'absolute', right: -10, bottom: -10,
                  width: 60, height: 60, borderRadius: '50%',
                  border: '1px solid rgba(255,255,255,0.15)',
                }}/>
                <div style={{
                  position: 'absolute', right: 4, bottom: 4,
                  width: 32, height: 32, borderRadius: '50%',
                  border: '1px solid rgba(255,255,255,0.2)',
                  background: 'rgba(0,0,0,0.25)',
                }}/>
              </div>
            ))}
          </div>
        </div>

        <div style={{ marginTop: 22, marginBottom: 130 }}>
          <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: 8 }}>Recent</div>
          {['Hana Okuda', 'long winter', 'velvet'].map(t => (
            <div key={t} style={{
              display: 'flex', alignItems: 'center', gap: 12,
              padding: '10px 0',
              borderBottom: `1px solid ${T.line}`,
            }}>
              <Icon d={I.search} size={15} stroke={T.textMute}/>
              <span style={{ flex: 1, fontSize: 14, color: T.text }}>{t}</span>
              <Icon d={I.close} size={15} stroke={T.textMute}/>
            </div>
          ))}
        </div>
      </div>
      <BottomNav active="search" theme={T}/>
    </Phone>
  );
}

Object.assign(window, {
  SplashScreen, WelcomeScreen, SignInScreen, BiometricSheet, HomeScreen, SearchScreen,
});
