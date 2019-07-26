create or replace function find_citizen_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t) into resource
    from (
         select 
            z.*, 
            find_user_by_id(z.user_id)
         from citizen as z
         where z.id = _id
     ) as t;
end;
$$;

-- drop function if exists find_citizen_by_id(uuid, inout json);