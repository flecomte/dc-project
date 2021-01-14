create or replace function find_comments_by_citizen(
    _created_by_id uuid,
    _reference regclass default null,
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select
        json_agg(t),
        (
            select count(id) from "comment"
            where
                  (_reference is null or _reference = target_reference)
              and created_by_id = _created_by_id
        )
    into resource, total
    from (
        select
            com.*,
            (select count(*) from "comment" c2 where c2.parents_ids @> array[com.id]) as children_count,
            find_comment_parent_by_id(com.parent_id) as parent,
            find_reference_by_id(com.target_id, _reference) as target,
            find_citizen_by_id_with_user(com.created_by_id) as created_by,
            count_vote(com.id) as votes

        from "comment" as com

        where
            (_reference is null or _reference = target_reference)
          and created_by_id = _created_by_id

        order by
            com.created_at desc

        limit "limit" offset "offset"
    ) as t;
end;
$$;
