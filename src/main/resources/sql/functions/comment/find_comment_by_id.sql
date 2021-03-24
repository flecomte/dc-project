create or replace function find_comment_by_id(
    _id uuid,
    out resource json
) language plpgsql as
$$
begin
    select to_json(t)
    into resource
    from (
        select
            com.*,
            (select count(*) from "comment" c2 where c2.parents_ids @> array[com.id]) as children_count,
            find_comment_parent_by_id(com.parent_id) as parent,
            find_reference_by_id(com.target_id, com.target_reference) as target,
            find_citizen_by_id_with_user(com.created_by_id) as created_by,
            count_vote(com.id) as votes
        from "comment" as com
        where id = _id
    ) as t;
end;
$$;


