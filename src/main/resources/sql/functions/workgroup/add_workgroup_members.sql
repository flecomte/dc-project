create or replace function add_workgroup_members(in _id uuid, inout members json)
    language plpgsql as
$$
begin
    perform add_workgroup_member(_id, b)
    from json_array_elements(members) b;

    select find_workgroup_members(_id) into members;
end;
$$;


