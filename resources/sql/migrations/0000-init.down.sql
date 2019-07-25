-- Article & Contitution
drop table if exists article_relations;
drop trigger if exists set_constitution_link_trigger on article_on_title;
drop table if exists article_in_title;
drop table if exists title;
drop function if exists set_constitution_link();

drop trigger if exists generate_version_number_trigger on article;
drop table if exists article;
drop function if exists generate_version_number(regclass, uuid);
drop trigger if exists generate_version_number_trigger on constitution;
drop table if exists constitution;
drop function if exists set_version_number();

-- User
drop table if exists moderator;
drop table if exists citizen_in_workgroup;
drop table if exists workgroup;
drop table if exists citizen;
drop table if exists "user";
drop type if exists public."name";
