create table users (
    id uuid primary key,
    username varchar(64) not null unique,
    email varchar(320) not null unique,
    password_hash varchar(255) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table tracks (
    id uuid primary key,
    title varchar(255) not null,
    artist varchar(255) not null,
    album varchar(255),
    genre varchar(32) not null,
    duration_ms bigint,
    artwork_url text,
    stream_url text,
    status varchar(32) not null,
    uploader_id uuid references users(id),
    version integer not null default 0,
    updated_at timestamptz not null default now(),
    search_vector tsvector,
    constraint tracks_genre_check check (
        genre in ('POP', 'ROCK', 'HIPHOP', 'RNB', 'ELECTRONIC', 'CLASSICAL', 'JAZZ', 'METAL', 'FOLK', 'COUNTRY', 'LATIN', 'OTHER')
    ),
    constraint tracks_status_check check (status in ('PROCESSING', 'READY', 'FAILED'))
);

create function tracks_search_vector_update() returns trigger as $$
begin
    new.search_vector :=
        setweight(to_tsvector('simple', coalesce(new.title, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(new.artist, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(new.album, '')), 'C');
    return new;
end;
$$ language plpgsql;

create trigger tracks_search_vector_trigger
before insert or update of title, artist, album on tracks
for each row execute function tracks_search_vector_update();

create table subtitle_lines (
    id uuid primary key,
    track_id uuid not null references tracks(id) on delete cascade,
    start_ms bigint not null,
    end_ms bigint not null,
    text text not null,
    created_at timestamptz not null default now(),
    constraint subtitle_lines_time_check check (start_ms >= 0 and end_ms >= start_ms)
);

create table playlists (
    id uuid primary key,
    name varchar(255) not null,
    owner_id uuid not null references users(id) on delete cascade,
    is_copy boolean not null default false,
    source_playlist_id uuid references playlists(id),
    version integer not null default 0,
    updated_at timestamptz not null default now()
);

create table playlist_tracks (
    playlist_id uuid not null references playlists(id) on delete cascade,
    track_id uuid not null references tracks(id) on delete cascade,
    position integer not null,
    primary key (playlist_id, track_id),
    constraint playlist_tracks_position_check check (position >= 0)
);

create table ratings (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    target_type varchar(16) not null,
    target_id uuid not null,
    value integer not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint ratings_target_type_check check (target_type in ('TRACK', 'PLAYLIST')),
    constraint ratings_value_check check (value between 1 and 5),
    constraint ratings_unique_user_target unique (user_id, target_type, target_id)
);

create table uploads_idempotency (
    idempotency_key text primary key,
    user_id uuid not null references users(id) on delete cascade,
    track_id uuid references tracks(id) on delete set null,
    response_json jsonb not null,
    created_at timestamptz not null default now()
);

create table provider_manifests (
    id uuid primary key,
    manifest_url text not null unique,
    name varchar(255) not null,
    version varchar(64) not null,
    capabilities jsonb not null,
    enabled boolean not null default true,
    updated_at timestamptz not null default now()
);

create index idx_tracks_search_vector on tracks using gin (search_vector);
create index idx_tracks_genre_updated_at on tracks (genre, updated_at);
create index idx_tracks_updated_at on tracks (updated_at);
create index idx_playlists_owner_updated_at on playlists (owner_id, updated_at);
create index idx_playlists_updated_at on playlists (updated_at);
create index idx_playlist_tracks_playlist_position on playlist_tracks (playlist_id, position);
create index idx_ratings_target on ratings (target_type, target_id);
create index idx_ratings_updated_at on ratings (updated_at);
create index idx_subtitle_lines_track_start on subtitle_lines (track_id, start_ms);
create index idx_provider_manifests_updated_at on provider_manifests (updated_at);

