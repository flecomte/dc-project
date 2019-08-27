create or replace function edit_comment(reference regclass, _id uuid, _content text) returns void
    language plpgsql as
$$
begin
    if reference = 'article'::regclass then
        update comment_on_article c set
            "content" = _content
        where c.id = _id;
    elseif reference = 'constitution'::regclass then
        update comment_on_constitution c set
            "content" = _content
        where c.id = _id;
    end if;
end;
$$;

-- drop function if exists edit_comment(regclass, uuid, text);