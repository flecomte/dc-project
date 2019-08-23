create or replace function find_citizen_by_id_with_user(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t) into resource
    from (
         select 
            z.*,
            u as "user"
         from citizen as z
         join "user" u on z.user_id = u.id
         where z.id = _id
     ) as t;
end;
$$;

-- drop function if exists find_citizen_by_id_with_user(uuid, inout json);