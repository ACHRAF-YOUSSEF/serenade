alter table users add column email_verified_at timestamptz;

create table auth_codes (
    id uuid primary key,
    user_id uuid references users(id) on delete cascade,
    email varchar(320) not null,
    purpose varchar(32) not null,
    code_hash varchar(64) not null,
    expires_at timestamptz not null,
    consumed_at timestamptz,
    created_at timestamptz not null default now(),
    constraint auth_codes_purpose_check check (purpose in ('EMAIL_VERIFICATION', 'PASSWORD_RESET'))
);

create unique index idx_auth_codes_active_hash
    on auth_codes (purpose, code_hash)
    where consumed_at is null;

create index idx_auth_codes_email_purpose_created
    on auth_codes (lower(email), purpose, created_at desc);

create index idx_auth_codes_user_purpose_created
    on auth_codes (user_id, purpose, created_at desc);
