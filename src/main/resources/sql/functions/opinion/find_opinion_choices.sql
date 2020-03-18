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
           and (
               ol.target is null or array_length(ol.target, 1) is null  -- if choice is compatible with all target
            or targets is null or array_length(targets, 1) is null -- if no target defined
            or (ol.target && targets) -- if target is compatible
           )

        order by ol.name
    ) t;
end;
$$;
