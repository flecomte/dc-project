create or replace function find_article_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t)
    from (
        select
            a.id,
            a.version_number,
            a.version_id,
            a.title,
            a.anonymous,
            a.content,
            a.description,
            a.tags,
            a.draft,
            a.last_version,
            find_citizen_by_id_with_user(a.created_by_id) as created_by,
            find_workgroup_by_id(a.workgroup_id) as workgroup,
            count_vote(a.id) as votes,
            count_opinion(a.id) as opinions
        into resource
        from article as a
        where a.id = _id
    ) as t;
end;
$$;


