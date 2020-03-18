create or replace function find_constitution_titles_by_id(in constitution_id uuid, out resource json) language plpgsql as
$$
declare
    _constitution_id alias for constitution_id;
begin
    select json_agg(t)
    from (
        select
            ti.id,
            ti.name,
            ti.rank
        into resource
        from title as ti
        where ti.constitution_id = _constitution_id
        order by ti.rank
    ) as t;
end;
$$;
