create or replace function find_citizen_by_name(_name json, out resource json) language plpgsql as
$$
begin
    select to_json(t) into resource
    from (
        select
            z.*,
            find_user_by_id(u.id) as "user"
        from citizen as z
        join "user" as u on z.user_id = u.id
        where z.name->>'first_name' = _name->>'first_name'
          and z.name->>'last_name' = _name->>'last_name'
    ) as t;
end;
$$;

