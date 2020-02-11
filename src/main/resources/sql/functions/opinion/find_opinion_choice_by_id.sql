create or replace function find_opinion_choice_by_id(_id uuid, out resource json)
    language plpgsql as
$$
begin
    select to_json(ol) into resource
    from opinion_list ol
    where (ol.deleted_at <= now()
       or ol.deleted_at is null)
       and (ol.id = _id);
end;
$$;

-- drop function if exists find_opinion_choice_by_id();

-- select find_opinion_choice_by_id('8c6cb3cc-cac5-93ad-312e-6bd87d9916d9');