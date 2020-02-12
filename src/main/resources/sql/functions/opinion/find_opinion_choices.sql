create or replace function find_opinion_choices(targets text[] default null, out resource json)
    language plpgsql as
$$
begin
    select json_agg(t)
    into resource
    from (
        select ol.*
        from opinion_choice ol
        where (ol.deleted_at is null or ol.deleted_at > now())
           and (ol.target is null or targets is null or array_length(targets, 1) = 0 or ol.target && targets)

        order by ol.name
    ) t;
end;
$$;

-- drop function if exists find_opinions();

-- select find_opinions();