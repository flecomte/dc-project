create or replace function comment(reference regclass, target_id uuid, citizen_id uuid, content text, parent_id uuid default null, out id uuid)
    language plpgsql as
$$
declare
    _citizen_id alias for citizen_id;
    _target_id alias for target_id;
    _content alias for content;
    _parent_id alias for parent_id;
    _id alias for id;
begin
    if reference = 'article'::regclass then
        insert into comment_on_article (citizen_id, target_id, content, parent_id)
        values (_citizen_id, _target_id, _content, _parent_id)
        returning comment_on_article.id into _id;
    elseif reference = 'constitution'::regclass then
        insert into comment_on_constitution (citizen_id, target_id, content, parent_id)
        values (_citizen_id, _target_id, _content, _parent_id)
        returning comment_on_constitution.id into _id;
    else
        raise exception 'comment with target as "%", is no implemented', reference::text;
    end if;
end;
$$;

-- drop function if exists comment(regclass, uuid, uuid, text, uuid);