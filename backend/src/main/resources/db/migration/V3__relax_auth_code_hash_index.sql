drop index if exists idx_auth_codes_active_hash;

create index idx_auth_codes_active_hash
    on auth_codes (purpose, code_hash)
    where consumed_at is null;
