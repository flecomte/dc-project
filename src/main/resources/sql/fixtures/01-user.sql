do
$$
declare
    _password text := crypt('azerty', gen_salt('bf', 8));
    multiple int = coalesce(current_setting('fixture.quantity.multiple', true), '50')::int;
begin
    delete from "user";
    insert into "user" (id, username, password, blocked_at)
    select
        uuid_in(md5('user'||rn::text)::cstring),
        'username' || rn,
        _password,
        case when rn % 10 = 0 then now() else null end
    from generate_series(1, multiple) rn;

    raise notice 'user fixtures done';
end;
$$;

