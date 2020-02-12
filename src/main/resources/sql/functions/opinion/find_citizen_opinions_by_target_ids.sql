create or replace function find_citizen_opinions_by_target_ids(
    _citizen_id uuid,
    _ids uuid[],
    out resource json
) language plpgsql as
$$
begin
    select json_agg(t) into resource
    from (
        select
            o.*,
            to_json(ol) as choice
        from opinion as o
        join opinion_choice ol on o.choice_id = ol.id

        where target_id = any(_ids)
            and created_by_id = _citizen_id

        order by
            ol.name
        limit 100
    ) t;
end;
$$;

-- drop function if exists find_citizen_votes_by_target_ids(uuid, uuid[], regclass);

-- select * from find_citizen_opinions_by_target_ids('045b6e9e-5a9e-d9b0-75d4-e51f0bc6cd21', '{32a18b25-507d-49d8-5168-7675fb6a7b8c, 429bfd8c-ebc2-09ac-227f-28bcdaa91d84, 550f4543-35a3-9910-e493-70d26b931473}')