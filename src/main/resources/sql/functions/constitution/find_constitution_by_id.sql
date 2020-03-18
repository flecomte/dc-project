create or replace function find_constitution_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t)
    from (
        select
            c.*,
            find_citizen_by_id(c.created_by_id) as created_by,
            find_constitution_titles_by_id(c.id) as titles
        into resource
        from constitution as c
        where c.id = _id
    ) as t;
end;
$$;

