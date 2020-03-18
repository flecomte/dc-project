create or replace function comment(reference regclass, resource json, out _id uuid)
    language plpgsql as
$$
declare
    _created_by_id uuid = resource#>>'{created_by,id}';
    _target_id uuid = resource#>>'{target,id}';
    _content text = resource#>>'{content}';
    _parent_comment_id uuid = resource#>>'{parent,id}';
    _new_id uuid = coalesce((resource->>'id')::uuid, uuid_generate_v4());
begin
    if reference = 'article'::regclass then
        insert into comment_on_article (id, created_by_id, target_id, content, parent_comment_id)
        values (_new_id, _created_by_id, _target_id, _content, _parent_comment_id);
    elseif reference = 'constitution'::regclass then
        insert into comment_on_constitution (id, created_by_id, target_id, content, parent_comment_id)
        values (_new_id, _created_by_id, _target_id, _content, _parent_comment_id);
    else
        raise exception 'comment with target as "%", is not implemented', reference::text;
    end if;
    _id = _new_id;
end;
$$;

