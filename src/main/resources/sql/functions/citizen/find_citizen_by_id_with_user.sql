create or replace function find_citizen_by_id_with_user(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t) into resource
    from (
         select 
            z.*,
            find_user_by_id(z.user_id) as "user",
            array_agg(find_workgroup_by_id_simple(ciw.workgroup_id)) as "workgroups"
         from citizen as z
         left join citizen_in_workgroup ciw on z.id = ciw.citizen_id
         where z.id = _id
         group by z.id
     ) as t;
end;
$$;

