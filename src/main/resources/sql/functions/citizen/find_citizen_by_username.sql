create or replace function find_citizen_by_username(username text, out resource json) language plpgsql as
$$
declare
    _username alias for username;
begin
    select to_json(t) into resource
    from (
        select
            z.*,
            find_user_by_id(u.id) as "user"
        from citizen as z
        join "user" as u on z.user_id = u.id
        where u.username = _username
    ) as t;
end;
$$;

