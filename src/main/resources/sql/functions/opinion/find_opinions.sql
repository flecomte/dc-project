create or replace function find_opinion_choices(targets text[] default null, out resource json)
    language plpgsql as
$$
begin
    select json_agg(t)
    into resource
    from (
        select ol.*
        from opinion_list ol
        where (ol.deleted_at <= now()
           or ol.deleted_at is null)
           and (ol.target is null or array_length(targets) = 0 or ol.target = any(targets))

        order by ol.name
    ) t;
end;
$$;

-- drop function if exists find_opinions();

-- select find_opinions();