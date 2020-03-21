create or replace function update_citizen_opinions_by_target_id(
    _choices_ids uuid[],
    _citizen_id uuid,
    _target_id uuid,
    _target_reference regclass,
    out opinions json,
    out ids_deleted uuid[]
) language plpgsql as
$$
begin
    if _target_reference = 'article'::regclass then
        insert into opinion_on_article (created_by_id, target_id, choice_id)
        select _citizen_id, _target_id, _choice_id
        from unnest(_choices_ids) _choice_id
        on conflict (created_by_id, target_id, choice_id) do nothing;

        with deleted as (
            delete from opinion_on_article o
                where o.created_by_id = _citizen_id
                    and o.target_id = _target_id
                    and (not array[o.choice_id]::uuid[] <@ _choices_ids or _choices_ids = '{}'::uuid[])
                returning id
        )
        select array_agg(d.id) into ids_deleted from deleted d;
    else
        raise exception '% no implemented for opinion', _target_reference::text;
    end if;

    select find_citizen_opinions_by_target_id(_citizen_id, _target_id) into opinions;
end
$$;
