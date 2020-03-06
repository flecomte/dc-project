do
$$
declare
    _article_id uuid := fixture_article();
    _citizen_id uuid := fixture_citizen('john');
    created_constitution json := $json$
    {
      "version_id": "18ff6dd6-3bc1-4c59-82f0-5e2a8d54ae3e",
      "title": "Love the world",
      "anonymous": false,
      "titles": [
        {
          "name": "titleOne"
        },
        {
          "name": "titleTwo"
        }
      ]
    }
    $json$;
begin
    -- create new constitution
    created_constitution := jsonb_set(created_constitution::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    created_constitution := jsonb_set(created_constitution::jsonb, '{titles, 0, articles}'::text[], jsonb_build_array(jsonb_build_object('id', _article_id)), true)::json;
    select upsert_constitution(created_constitution) into created_constitution;
    assert (created_constitution->>'version_number')::int = 1, format('version_number must be equal to 1, %s instead', created_constitution->>'version_number');
    assert created_constitution#>>'{titles, 0, name}' = 'titleOne'::text, format('the name of the first title of contitution must be %s, not %s', 'titleOne', created_constitution#>>'{titles, 0, name}');

    rollback;
    raise notice 'constitution test pass';
end
$$;


-- select uuid_generate_v4();