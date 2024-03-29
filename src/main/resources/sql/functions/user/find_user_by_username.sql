create or replace function find_user_by_username(in username text, out resource json) language plpgsql as
$$
declare
    _username alias for username;
begin
    select to_jsonb(u) - 'password' into resource
    from "user" as u
    where u.username = _username;
end;
$$;

