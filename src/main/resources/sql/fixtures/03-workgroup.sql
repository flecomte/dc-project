do
$$
declare
    citizen_count int = (select count(*) from citizen);
begin
    delete from citizen_in_workgroup;
    delete from workgroup;

    insert into workgroup (id, created_by_id, name, description, annonymous, owner_id)
    select
        uuid_in(md5('workgroup'||rn::text)::cstring),
        z.id,
        'name' || rn,
        'description' || rn,
        rn % 3 = 1,
        z.id
    from (select *, row_number() over () rn from citizen) z;

    insert into citizen_in_workgroup (citizen_id, workgroup_id)
    select
        z.id,
        w.id
    from (select *, row_number() over ()+5 % citizen_count rn from citizen) z
    join (select *, row_number() over () rn from workgroup) w using (rn);

    raise notice 'workgroup fixtures done';
end;
$$;

