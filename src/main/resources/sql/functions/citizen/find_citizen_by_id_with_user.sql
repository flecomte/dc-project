create or replace function find_citizen_by_id_with_user(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t) into resource
    from (
         select 
            z.*,
            find_user_by_id(z.user_id) as "user"
         from citizen as z
         where z.id = _id
         group by z.id
     ) as t;
end;
$$;

