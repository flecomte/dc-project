do
$$
begin
    delete from citizen;
    insert into citizen (name, birthday, user_id, vote_annonymous, follow_annonymous)
    select
        jsonb_build_object(
            'first_name', 'first name' || row_number() over (),
            'last_name', 'LAST NAME' || row_number() over (),
            'civility', 'm'
        ),
        now() - interval '25 years',
        u.id,
        row_number() over () % 3 = 0,
        row_number() over () % 5 = 1
    from "user" u;

    raise notice 'citizen fixtures done';
end;
$$;

