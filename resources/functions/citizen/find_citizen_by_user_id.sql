create or replace function find_citizen_by_user_id(in user_id uuid, out resource json) language plpgsql as
$$
declare
    _user_id alias for user_id;
begin
    select to_json(t) into resource
    from (
        select
            z.*,
            find_user_by_id(z.user_id)
        from citizen as z
        where z.user_id = _user_id
    ) as t;
end;
$$;

-- drop function if exists find_citizen_by_user_id(uuid, inout json);