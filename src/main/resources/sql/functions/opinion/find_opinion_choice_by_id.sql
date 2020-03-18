create or replace function find_opinion_choice_by_id(_id uuid, out resource json)
    language plpgsql as
$$
begin
    select to_json(ol) into resource
    from opinion_choice ol
    where (ol.deleted_at <= now()
       or ol.deleted_at is null)
       and (ol.id = _id);
end;
$$;
