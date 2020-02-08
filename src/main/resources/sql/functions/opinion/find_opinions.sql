create or replace function find_opinions(out resource json)
    language plpgsql as
$$
begin
    select json_agg(t)
    into resource
    from (
        select ol.*
        from opinion_list ol
        where ol.deleted_at <= now()
           or ol.deleted_at is null
        order by ol.name
    ) t;
end;
$$;

-- drop function if exists find_opinions();

-- select find_opinions();