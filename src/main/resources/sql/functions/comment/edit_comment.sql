create or replace function edit_comment(_id uuid, _content text) returns void
    language plpgsql as
$$
begin
        update comment c set
            "content" = _content
        where c.id = _id;
end;
$$;

-- drop function if exists edit_comment(regclass, uuid, text);

-- select edit_comment('b0422e48-687f-bea7-b45f-b6b301246e97', 'plop4')