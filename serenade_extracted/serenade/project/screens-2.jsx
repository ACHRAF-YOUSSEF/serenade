// Serenade — Screens 6-10: Now Playing, Lyrics, Playlist, Create, Library

// 6. NOW PLAYING ───────────────────────────────────────────
function NowPlayingScreen({ theme = SERENADE }) {
  const T = theme;
  return (
    <Phone theme={T}>
      {/* atmospheric backdrop tinted from album */}
      <div style={{
        position: 'absolute', inset: 0,
        background: `radial-gradient(80% 60% at 50% 20%, ${T.coral}66, ${T.plum}99 40%, ${T.bgDeep})`,
      }}/>
      <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.25)' }}/>

      <div style={{ position: 'relative', padding: '14px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Icon d={I.chevDown} size={24} stroke={T.text}/>
        <div style={{ textAlign: 'center' }}>
          <div className="sr-mono" style={{ fontSize: 9.5, color: 'rgba(255,255,255,0.7)', letterSpacing: '0.22em', textTransform: 'uppercase' }}>Playing from</div>
          <div style={{ fontSize: 13, color: T.text, fontWeight: 600, marginTop: 2 }}>Lantern Hours</div>
        </div>
        <Icon d={I.more} size={22} stroke={T.text}/>
      </div>

      {/* artwork */}
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 22, position: 'relative' }}>
        <div style={{ boxShadow: '0 30px 80px rgba(0,0,0,0.6)', borderRadius: 16 }}>
          <ArtSeed seed="Bonfires in Reverse" size={264} radius={16} vivid theme={T}/>
        </div>
      </div>

      {/* meta */}
      <div style={{ padding: '28px 28px 0', position: 'relative' }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12 }}>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="sr-serif" style={{ fontSize: 24, fontStyle: 'italic', color: T.text, lineHeight: 1.1 }}>Bonfires in Reverse</div>
            <div style={{ fontSize: 13.5, color: 'rgba(255,255,255,0.78)', marginTop: 4 }}>Hana Okuda · The Long Way Home</div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14, alignItems: 'center', marginTop: 4 }}>
            <Icon d={I.heartOn} size={22} stroke={T.primary} fill={T.primary}/>
            <Icon d={I.add} size={22} stroke={T.text}/>
          </div>
        </div>

        {/* progress */}
        <div style={{ marginTop: 22 }}>
          <div style={{ height: 3, background: 'rgba(255,255,255,0.18)', borderRadius: 2, position: 'relative' }}>
            <div style={{ position: 'absolute', left: 0, top: 0, width: '42%', height: '100%', background: T.text, borderRadius: 2 }}/>
            <div style={{ position: 'absolute', left: '42%', top: '50%', transform: 'translate(-50%,-50%)', width: 12, height: 12, borderRadius: '50%', background: T.text, boxShadow: '0 0 8px rgba(255,255,255,0.6)' }}/>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 6, fontSize: 11, color: 'rgba(255,255,255,0.7)', fontFamily: T.mono }}>
            <span>1:42</span><span>-2:18</span>
          </div>
        </div>

        {/* controls */}
        <div style={{ marginTop: 22, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Icon d={I.shuffle} size={20} stroke={T.primary} sw={2}/>
          <Icon d={I.prev} size={28} stroke={T.text} sw={1.6}/>
          <div style={{
            width: 72, height: 72, borderRadius: '50%',
            background: T.text, color: T.bgDeep,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
          }}>
            <Icon d={I.pause} size={28} stroke={T.bgDeep} sw={2.2}/>
          </div>
          <Icon d={I.next} size={28} stroke={T.text} sw={1.6}/>
          <Icon d={I.repeat} size={20} stroke={T.text} sw={1.8}/>
        </div>

        {/* footer actions */}
        <div style={{ marginTop: 28, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Icon d={I.cast} size={20} stroke={'rgba(255,255,255,0.7)'}/>
          <div style={{ display: 'flex', gap: 14, alignItems: 'center' }}>
            <span className="sr-mono" style={{ fontSize: 10, color: 'rgba(255,255,255,0.55)', letterSpacing: '0.18em' }}>FLAC · 24/96</span>
          </div>
          <Icon d={I.queue} size={20} stroke={'rgba(255,255,255,0.7)'}/>
        </div>

        {/* lyrics teaser */}
        <div style={{
          marginTop: 24,
          background: 'rgba(0,0,0,0.35)', border: `1px solid ${T.lineHi}`,
          borderRadius: 14, padding: '14px 16px', backdropFilter: 'blur(10px)',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div className="sr-mono" style={{ fontSize: 9.5, color: T.primary, letterSpacing: '0.22em', textTransform: 'uppercase' }}>Lyrics</div>
            <Icon d={I.chev} size={14} stroke={'rgba(255,255,255,0.6)'}/>
          </div>
          <div style={{ marginTop: 6, fontSize: 14, color: 'rgba(255,255,255,0.55)', lineHeight: 1.4, fontFamily: T.serif, fontStyle: 'italic' }}>
            we lit the road behind us<br/>
            <span style={{ color: T.text, fontWeight: 500 }}>and watched it learn to glow —</span>
          </div>
        </div>
      </div>
    </Phone>
  );
}

// 7. LYRICS ────────────────────────────────────────────────
function LyricsScreen({ theme = SERENADE }) {
  const T = theme;
  const lines = [
    { t: '0:00', text: 'we lit the road behind us', state: 'past' },
    { t: '0:08', text: 'and watched it learn to glow', state: 'past' },
    { t: '0:16', text: 'every house we never bought', state: 'past' },
    { t: '0:24', text: 'every river we said no', state: 'past' },
    { t: '0:32', text: 'now the bonfires move in reverse —', state: 'now' },
    { t: '0:42', text: 'pulling embers from the cold', state: 'next' },
    { t: '0:50', text: 'and i find you in the smoke again', state: 'next' },
    { t: '0:58', text: 'soft, and twenty years old', state: 'next' },
    { t: '1:08', text: '(let it burn the long way home)', state: 'next' },
  ];
  return (
    <Phone theme={T}>
      <div style={{
        position: 'absolute', inset: 0,
        background: `linear-gradient(180deg, ${T.bgDeep}, ${T.plum}55 50%, ${T.bgDeep})`,
      }}/>

      <div style={{ position: 'relative', padding: '14px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Icon d={I.chevDown} size={22} stroke={T.text}/>
        <div className="sr-mono" style={{ fontSize: 10, color: T.textDim, letterSpacing: '0.22em', textTransform: 'uppercase' }}>Lyrics · synced</div>
        <Icon d={I.sliders} size={20} stroke={T.text}/>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 22px 0' }}>
        <ArtSeed seed="Bonfires in Reverse" size={42} radius={6} vivid theme={T}/>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 13.5, fontWeight: 600, color: T.text }}>Bonfires in Reverse</div>
          <div style={{ fontSize: 11.5, color: T.textDim }}>Hana Okuda</div>
        </div>
        <div style={{
          padding: '4px 10px', borderRadius: 100,
          border: `1px solid ${T.primary}`, color: T.primary,
          fontSize: 10.5, fontWeight: 600, letterSpacing: '0.05em',
        }}>AI · whispered</div>
      </div>

      <div className="sr-noscroll" style={{ padding: '24px 28px 220px', overflow: 'auto', height: 'calc(100% - 100px)' }}>
        {lines.map((l, i) => {
          const isNow = l.state === 'now';
          return (
            <div key={i} className="sr-serif" style={{
              fontSize: isNow ? 26 : 19, fontStyle: 'italic',
              color: l.state === 'past' ? 'rgba(255,255,255,0.28)' : isNow ? T.text : 'rgba(255,255,255,0.55)',
              fontWeight: isNow ? 600 : 400,
              padding: isNow ? '14px 0' : '8px 0',
              lineHeight: 1.25,
              transition: 'all .3s',
              transform: isNow ? 'translateX(0)' : undefined,
              textWrap: 'pretty',
            }}>
              {l.text}
              {isNow && (
                <div style={{
                  marginTop: 10,
                  height: 2, width: 56, background: T.primary, borderRadius: 2,
                }}/>
              )}
            </div>
          );
        })}
      </div>

      {/* mini control strip */}
      <div style={{
        position: 'absolute', left: 12, right: 12, bottom: 14,
        background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(20px)',
        border: `1px solid ${T.lineHi}`, borderRadius: 16, padding: '10px 14px',
        display: 'flex', alignItems: 'center', gap: 14,
      }}>
        <Icon d={I.prev} size={20} stroke={T.text}/>
        <div style={{
          width: 38, height: 38, borderRadius: '50%', background: T.primary,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Icon d={I.pause} size={18} stroke={'#1a0d05'} sw={2.2}/>
        </div>
        <Icon d={I.next} size={20} stroke={T.text}/>
        <div style={{ flex: 1, height: 2, background: 'rgba(255,255,255,0.15)', borderRadius: 2, position: 'relative' }}>
          <div style={{ width: '38%', height: '100%', background: T.primary, borderRadius: 2 }}/>
        </div>
        <span className="sr-mono" style={{ fontSize: 10, color: T.textDim }}>1:42</span>
      </div>
    </Phone>
  );
}

// 8. PLAYLIST DETAIL ────────────────────────────────────────
function PlaylistDetailScreen({ theme = SERENADE }) {
  const T = theme;
  const tracks = [
    { t: 'Bonfires in Reverse',   a: 'Hana Okuda',          d: '4:12', dl: true,  e: false, hi: true },
    { t: 'Slow Tide',              a: 'Vesper & the Hours',  d: '3:48', dl: true },
    { t: 'A Letter in November',   a: 'Iben Mell',           d: '5:02' },
    { t: 'The Long Way Down',      a: 'Rooks Aubrey',        d: '4:25', e: true },
    { t: 'Cinders, Cinders',       a: 'Marit Rún',           d: '6:12' },
    { t: 'Wintering',              a: 'Hana Okuda',          d: '3:04', dl: true },
    { t: 'Where the Rivers Meet',  a: 'Vesper & the Hours',  d: '4:36' },
  ];
  return (
    <Phone theme={T}>
      <div className="sr-noscroll" style={{ height: '100%', overflow: 'auto', paddingBottom: 130 }}>
        <div style={{
          position: 'relative', height: 320,
          background: `radial-gradient(80% 80% at 50% 0%, ${T.plum}aa, ${T.bg} 70%)`,
        }}>
          <div style={{ padding: '14px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Icon d={I.back} size={22} stroke={T.text}/>
            <Icon d={I.more} size={22} stroke={T.text}/>
          </div>
          <div style={{ display: 'flex', justifyContent: 'center', marginTop: 22 }}>
            <div style={{ boxShadow: '0 24px 48px rgba(0,0,0,0.6)', borderRadius: 12 }}>
              <ArtSeed seed="Lantern Hours" size={170} radius={12} vivid theme={T}/>
            </div>
          </div>
          <div style={{ textAlign: 'center', padding: '16px 28px 0' }}>
            <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.22em', textTransform: 'uppercase' }}>Playlist · public</div>
            <div className="sr-serif" style={{ fontSize: 28, fontStyle: 'italic', color: T.text, lineHeight: 1.05, margin: '6px 0 4px' }}>Lantern Hours</div>
            <div style={{ fontSize: 12.5, color: T.textDim }}>Curated by Marit · 38 tracks · 2h 14m</div>
          </div>
        </div>

        <div style={{ padding: '0 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 4, marginBottom: 14 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
            <Icon d={I.heart} size={22} stroke={T.text}/>
            <Icon d={I.download} size={22} stroke={T.text}/>
            <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
              <Icon d={I.copy} size={20} stroke={T.primary}/>
              <span style={{ fontSize: 12, color: T.primary, fontWeight: 600 }}>Remix</span>
            </div>
          </div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <Icon d={I.shuffle} size={20} stroke={T.text}/>
            <div style={{
              width: 44, height: 44, borderRadius: '50%',
              background: T.primary, display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: `0 8px 18px ${T.primary}55`,
            }}>
              <Icon d={I.play} size={20} stroke={'#1a0d05'} sw={2}/>
            </div>
          </div>
        </div>

        {/* rating row */}
        <div style={{ padding: '0 18px 14px', display: 'flex', alignItems: 'center', gap: 6 }}>
          {[1,2,3,4,5].map(n => (
            <Icon key={n} d={n <= 4 ? I.starOn : I.star} size={14} stroke={T.amber} fill={n <= 4 ? T.amber : 'none'}/>
          ))}
          <span style={{ fontSize: 11.5, color: T.textDim, marginLeft: 6 }}>4.2 · 1.4k ratings</span>
        </div>

        <div style={{ height: 1, background: T.line, margin: '0 18px 6px' }}/>

        {tracks.map((t, i) => (
          <TrackRow key={i} idx={i + 1} title={t.t} artist={t.a} dur={t.d}
                    playing={t.hi} downloaded={t.dl} explicit={t.e} hi={t.hi} theme={T}/>
        ))}
      </div>

      <MiniPlayer track={{ title: 'Bonfires in Reverse', artist: 'Hana Okuda' }} theme={T}/>
      <BottomNav active="library" theme={T}/>
    </Phone>
  );
}

// 9. CREATE / EDIT PLAYLIST ────────────────────────────────
function CreatePlaylistScreen({ theme = SERENADE }) {
  const T = theme;
  const tracks = [
    { t: 'Bonfires in Reverse',   a: 'Hana Okuda' },
    { t: 'Slow Tide',              a: 'Vesper & the Hours' },
    { t: 'A Letter in November',   a: 'Iben Mell' },
    { t: 'The Long Way Down',      a: 'Rooks Aubrey' },
  ];
  return (
    <Phone theme={T}>
      <div style={{ padding: '14px 18px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: 14, color: T.textDim }}>Cancel</span>
        <span style={{ fontSize: 13, color: T.text, fontWeight: 600 }}>New playlist</span>
        <span style={{ fontSize: 14, color: T.primary, fontWeight: 600 }}>Save</span>
      </div>

      <div style={{ padding: '16px 18px 0', display: 'flex', gap: 14 }}>
        <div style={{ position: 'relative' }}>
          <ArtSeed seed="velvet ash blue" size={88} radius={10} vivid theme={T}/>
          <div style={{
            position: 'absolute', right: -6, bottom: -6,
            width: 28, height: 28, borderRadius: '50%',
            background: T.primary, color: '#1a0d05',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            border: `2px solid ${T.bg}`,
          }}>
            <Icon d={I.edit} size={14} stroke={'#1a0d05'} sw={2}/>
          </div>
        </div>
        <div style={{ flex: 1 }}>
          <div className="sr-serif" style={{ fontSize: 22, fontStyle: 'italic', color: T.text, marginBottom: 4, borderBottom: `1.5px solid ${T.primary}`, paddingBottom: 6 }}>
            Untitled · evening
          </div>
          <div style={{ fontSize: 12, color: T.textDim, lineHeight: 1.4, marginTop: 6 }}>
            Add a description so other listeners know what mood this is for.
          </div>
        </div>
      </div>

      <div style={{ padding: '20px 18px 8px', display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <Chip on theme={T}>Public</Chip>
        <Chip theme={T}>Allow remixes</Chip>
        <Chip theme={T} icon={I.user}>Collaborators</Chip>
      </div>

      <div style={{ padding: '8px 18px 0' }}>
        <div className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: 8 }}>4 tracks · drag to reorder</div>
      </div>

      {tracks.map((t, i) => (
        <div key={i} style={{
          display: 'flex', alignItems: 'center', gap: 10, padding: '10px 18px',
          background: i === 1 ? 'rgba(232,176,122,0.07)' : 'transparent',
          borderTop: i === 1 ? `1px dashed ${T.primary}55` : 'none',
          borderBottom: i === 1 ? `1px dashed ${T.primary}55` : 'none',
        }}>
          <Icon d={I.drag} size={18} stroke={T.textMute}/>
          <ArtSeed seed={t.t} size={40} radius={6} theme={T}/>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 14, fontWeight: 500, color: T.text }}>{t.t}</div>
            <div style={{ fontSize: 11.5, color: T.textDim }}>{t.a}</div>
          </div>
          <Icon d={I.trash} size={17} stroke={T.textMute}/>
        </div>
      ))}

      <div style={{ padding: '14px 18px' }}>
        <div className="sr-press" style={{
          height: 50, borderRadius: 12, border: `1.5px dashed ${T.lineHi}`,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          color: T.primary, fontSize: 14, fontWeight: 600,
        }}>
          <Icon d={I.add} size={18} stroke={T.primary} sw={2}/>
          Add tracks from your library
        </div>
      </div>
    </Phone>
  );
}

// 10. LIBRARY ──────────────────────────────────────────────
function LibraryScreen({ theme = SERENADE }) {
  const T = theme;
  const items = [
    { t: 'Lantern Hours', s: 'Playlist · 38 tracks', mine: false, copy: true },
    { t: 'Slow returns',  s: 'Playlist · 21 tracks · yours' },
    { t: 'River Hymn',    s: 'Playlist · 17 tracks' },
    { t: 'Hana Okuda',    s: 'Artist · 4 albums', artist: true },
    { t: 'Velvet Lull',   s: 'Album · Vesper' },
    { t: 'Wintering',     s: 'Track · Hana Okuda' },
  ];
  return (
    <Phone theme={T}>
      <div style={{ padding: '20px 18px 0' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div className="sr-serif" style={{ fontSize: 26, fontStyle: 'italic', color: T.text }}>Your library</div>
          <div style={{ display: 'flex', gap: 14 }}>
            <Icon d={I.search} size={20} stroke={T.text}/>
            <Icon d={I.add} size={22} stroke={T.text}/>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 8, marginTop: 16, overflowX: 'auto' }} className="sr-noscroll">
          <Chip on theme={T}>All</Chip>
          <Chip theme={T}>Playlists</Chip>
          <Chip theme={T}>Albums</Chip>
          <Chip theme={T}>Artists</Chip>
          <Chip theme={T} icon={I.downloaded}>Downloaded</Chip>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 18, marginBottom: 8 }}>
          <span className="sr-mono" style={{ fontSize: 9.5, color: T.textMute, letterSpacing: '0.2em', textTransform: 'uppercase' }}>Recent</span>
          <span style={{ fontSize: 11.5, color: T.textDim, display: 'flex', gap: 4, alignItems: 'center' }}>
            <Icon d={I.sliders} size={13} stroke={T.textDim}/>Recently played
          </span>
        </div>
      </div>

      <div style={{ paddingBottom: 130 }}>
        {items.map((it, i) => (
          <div key={i} className="sr-press" style={{
            display: 'flex', alignItems: 'center', gap: 12, padding: '8px 18px',
          }}>
            <ArtSeed seed={it.t} size={56} radius={it.artist ? 28 : 8} vivid theme={T}/>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14.5, fontWeight: 500, color: T.text, display: 'flex', alignItems: 'center', gap: 6 }}>
                {it.t}
                {it.copy && <Icon d={I.copy} size={12} stroke={T.primary}/>}
              </div>
              <div style={{ fontSize: 11.5, color: T.textDim }}>{it.s}</div>
            </div>
            <Icon d={I.more} size={18} stroke={T.textMute}/>
          </div>
        ))}
      </div>

      <MiniPlayer track={{ title: 'Bonfires in Reverse', artist: 'Hana Okuda' }} theme={T}/>
      <BottomNav active="library" theme={T}/>
    </Phone>
  );
}

Object.assign(window, {
  NowPlayingScreen, LyricsScreen, PlaylistDetailScreen, CreatePlaylistScreen, LibraryScreen,
});
