create or replace function edit_comment(_id uuid, _content text, out resource json)
    language plpgsql as
$$
begin
    update comment c set
        "content" = _content
    where c.id = _id;

    select find_comment_by_id(_id) into resource;
end;
$$;
