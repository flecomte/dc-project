create or replace function find_reference_by_id(
    _id uuid,
    _reference regclass default null,
    out resource json
) language plpgsql as
$$
begin
    select
    case _reference
        when 'article'::regclass then
            find_article_by_id(_id)
        when 'constitution'::regclass then
            find_constitution_by_id(_id)
        when 'comment'::regclass then
            find_comment_by_id(_id)
        when 'opinion'::regclass then
            find_opinion_by_id(_id)
        else
            json_build_object('id', _id, 'reference', _reference)
    end
    into resource;

    resource = resource::jsonb || jsonb_build_object('reference', _reference);
end
$$;

-- drop function if exists find_reference_by_id(uuid, regclass, out json);

-- select find_reference_by_id('8944221c-3766-f952-7064-9f229c288049'::uuid, 'constitution'::regclass)