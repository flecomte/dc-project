do
$$
declare
    citizen_count int = (select count(*) from citizen);
    _roles text[] = $roles$
    {
        "MANAGER", "EDITOR", "REPORTER"
    }
    $roles$;
begin
    insert into workgroup (id, created_by_id, name, description, anonymous)
    select
        uuid_in(md5('workgroup'||rn::text)::cstring),
        z.id,
        'name' || rn,
        'description' || rn,
        rn % 3 = 1
    from (select *, row_number() over () rn from citizen) z;

    insert into citizen_in_workgroup (citizen_id, workgroup_id, roles)
    select
        z.id,
        w.id,
        '{MASTER}'
    from (select *, row_number() over ()+5 % citizen_count rn from citizen) z
    join (select *, row_number() over () rn from workgroup) w using (rn);

    insert into citizen_in_workgroup (citizen_id, workgroup_id, roles)
    select
        z.id,
        w.id,
        _roles[(row_number() over () % 3)+1:(row_number() over () % 3)+1]
    from (select *, row_number() over ()+10 % citizen_count rn from citizen) z
    join (select *, row_number() over () rn from workgroup) w using (rn);

    raise notice 'workgroup fixtures done';
end;
$$;

