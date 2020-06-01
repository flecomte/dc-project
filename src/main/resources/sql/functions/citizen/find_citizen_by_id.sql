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
         left join citizen_in_workgroup ciw on z.id = ciw.citizen_id
         where z.id = _id
         group by z.id
     ) as t;
end;
$$;
