create or replace function fixture_workgroup(in name text default 'vert', _citizen_id uuid default fixture_citizen(), out _article_id uuid)
    language plpgsql as
$$
declare
    created_workgroup  json;
begin
    if (name = 'vert') then
        created_workgroup = '{
            "name": "Le groupe des vert",
            "description": "test",
            "anonymous": false
        }';
    elseif (name = 'rouge') then
        created_workgroup = '{
          "name": "Le groupe des rouge",
          "description": "test",
          "anonymous": false
        }';
    end if;

    created_workgroup := jsonb_set(created_workgroup::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    created_workgroup := jsonb_set(created_workgroup::jsonb, '{owner}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_workgroup#>>'{created_by, id}' = _citizen_id::text, format('citizenId in workgroup must be the same as citizen, %s != %s', created_workgroup#>>'{created_by, id}', _citizen_id::text);

    -- upsert workgroup
    select upsert_workgroup(created_workgroup) into created_workgroup;
    assert created_workgroup->>'description' is not null, 'description should not be null';
end;
$$;
