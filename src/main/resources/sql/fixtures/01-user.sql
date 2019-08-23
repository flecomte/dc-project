do
$$
declare
    _password text := crypt('azerty', gen_salt('bf', 8));
    multiple int = coalesce(current_setting('fixture.quantity.multiple', true), '50')::int;
begin
    delete from "user";
    insert into "user" (id, username, password, blocked_at, roles)
    select
        uuid_in(md5('user'||rn::text)::cstring),
        'username' || rn,
        _password,
        case when rn % 10 = 0 then now() else null end,
        case when rn % 2 = 0 then '{ROLE_USER}'::text[] else '{ROLE_ADMIN}'::text[] end
    from generate_series(1, multiple) rn;

    raise notice 'user fixtures done';
end;
$$;

