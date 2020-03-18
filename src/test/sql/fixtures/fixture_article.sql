create or replace function fixture_article(in name text default 'love', _citizen_id uuid default fixture_citizen(), out _article_id uuid)
    language plpgsql as
$$
declare
    created_article  json := $json$
    {
      "version_id": "933b6a1b-50c9-42b6-989f-c02a57814ef9",
      "title": "Love the world",
      "anonymous": false,
      "content": "bla bal bla",
      "tags": [
        "love",
        "test"
      ],
      "draft":false
    }
    $json$;
begin
    if (name = 'love') then
        -- set citizen id to article
        created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
        assert created_article#>>'{created_by, id}' = _citizen_id::text, format('citizenId in article must be the same as citizen, %s != %s', created_article#>>'{created_by, id}', _citizen_id::text);
        -- upsert article
        select (a->>'id')::uuid into _article_id from upsert_article(created_article) a;
    end if;
end;
$$;
