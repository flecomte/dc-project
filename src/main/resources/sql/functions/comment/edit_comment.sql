create or replace function edit_comment(_id uuid, _content text) returns void
    language plpgsql as
$$
begin
    update comment c set
        "content" = _content
    where c.id = _id;
end;
$$;
