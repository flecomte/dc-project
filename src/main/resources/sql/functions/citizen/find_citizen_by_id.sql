create or replace function find_citizen_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t) into resource
    from (
         select
            z.*
         from citizen as z
         where z.id = _id
         group by z.id
     ) as t;
end;
$$;
