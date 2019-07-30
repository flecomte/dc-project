do
$$
declare
    _password text := crypt('azerty', gen_salt('bf', 8));
begin
    delete from "user";
    insert into "user" (username, password, blocked_at)
    select
        'username' || s,
        _password,
        case when s % 10 = 0 then now() else null end
    from generate_series(1, 1000) s;

    raise notice 'user fixtures done';
end;
$$;

