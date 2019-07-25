-- Stats
drop table if exists resource_view;

-- Extra resources
drop table if exists follow_article;
drop table if exists follow_constitution;
drop table if exists follow_citizen;
drop table if exists follow;

drop table if exists vote_for_article;
drop table if exists vote_for_constitution;
drop table if exists vote_for_comment_on_article;
drop table if exists vote_for_comment_on_constitution;
drop table if exists vote;

drop table if exists comment_on_article;
drop table if exists comment_on_constitution;
drop table if exists comment;

drop table if exists extra;

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
