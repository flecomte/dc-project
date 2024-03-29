do
$$
begin
    insert into citizen (id, name, birthday, user_id, vote_anonymous, follow_anonymous, email)
    select
        uuid_in(md5('citizen'||row_number() over ()::text)::cstring),
        jsonb_build_object(
            'first_name', 'first name' || row_number() over (),
            'last_name', 'LAST NAME' || row_number() over (),
            'civility', 'm'
        ),
        now() - interval '25 years',
        u.id,
        row_number() over () % 3 = 0,
        row_number() over () % 5 = 1,
        u.username || '@domain'|| (row_number() over () % 5 = 1) || '.com'
    from "user" u;

    raise notice 'citizen fixtures done';
end;
$$;

