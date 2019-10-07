create or replace function find_article_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t)
    from (
        select
            a.*,
            find_citizen_by_id(a.created_by_id) as created_by,
            count_vote(a.id) as votes
        into resource
        from article as a
        where a.id = _id
    ) as t;
end;
$$;

-- drop function if exists find_article_by_id(uuid, out json);
