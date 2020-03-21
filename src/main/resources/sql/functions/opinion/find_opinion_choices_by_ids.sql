create or replace function find_opinion_choices_by_ids(_ids uuid[], out resource json)
    language plpgsql as
$$
begin
    select json_agg(ol) into resource
    from opinion_choice ol
    where (ol.deleted_at <= now()
       or ol.deleted_at is null)
       and ol.id = any(_ids);
end;
$$;
