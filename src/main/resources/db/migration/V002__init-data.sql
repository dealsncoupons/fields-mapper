with rowing as(
    insert into tbl_sport (name) values('rowing') returning id
)
insert into tbl_team (name, sport_id)
select 'water-works', rowing.id from rowing
union
select 'splash-valley', rowing.id from rowing
union
select 'star-cross', rowing.id from rowing
union
select 'no-sweat', rowing.id from rowing;

with biking as(
    insert into tbl_sport (name) values('biking') returning id
)
insert into tbl_team (name, sport_id)
select 'hillside', biking.id from biking
union
select 'mountaineers', biking.id from biking
union
select 'pedal-paw', biking.id from biking
union
select 'biotech', biking.id from biking;

with skiing as (
    insert into tbl_sport (name) values('skiing') returning id
)
insert into tbl_team (name, sport_id)
select 'snow-side', skiing.id from skiing
union
select 'winter-land', skiing.id from skiing
union
select 'tundra-cross', skiing.id from skiing
union
select 'ever-resting', skiing.id from skiing;

with splashValley as (
    select * from tbl_team where name = 'splash-valley'
)
insert into tbl_player (first_name, last_name, dob, home_city, home_country, team_id)
select 'sv1_first', 'sv1_last', current_timestamp, 'sv1_city', 'sv1_country',splashValley.id from splashValley
union
select 'sv2_first', 'sv2_last', current_timestamp, 'sv2_city', 'sv2_country',splashValley.id from splashValley
union
select 'sv3_first', 'sv3_last', current_timestamp, 'sv3_city', 'sv3_country',splashValley.id from splashValley
union
select 'sv4_first', 'sv4_last', current_timestamp, 'sv4_city', 'sv4_country',splashValley.id from splashValley;

with waterWorks as (
    select * from tbl_team where name = 'water-works'
)
insert into tbl_player (first_name, last_name, dob, home_city, home_country, team_id)
select 'ww1_first', 'ww1_last', current_timestamp, 'ww1_city', 'ww1_country',waterWorks.id from waterWorks
union
select 'ww2_first', 'ww2_last', current_timestamp, 'ww2_city', 'ww2_country',waterWorks.id from waterWorks
union
select 'ww3_first', 'ww3_last', current_timestamp, 'ww3_city', 'ww3_country',waterWorks.id from waterWorks
union
select 'ww4_first', 'ww4_last', current_timestamp, 'ww4_city', 'ww4_country',waterWorks.id from waterWorks;

with hillSide as (
    select * from tbl_team where name = 'hillside'
)
insert into tbl_player (first_name, last_name, dob, home_city, home_country, team_id)
select 'hs1_first', 'hs1_last', current_timestamp, 'hs1_city', 'hs1_country',hillSide.id from hillSide
union
select 'hs2_first', 'hs2_last', current_timestamp, 'hs2_city', 'hs2_country',hillSide.id from hillSide
union
select 'hs3_first', 'hs3_last', current_timestamp, 'hs3_city', 'hs3_country',hillSide.id from hillSide
union
select 'hs4_first', 'hs4_last', current_timestamp, 'hs4_city', 'hs4_country',hillSide.id from hillSide;

with mountaineers as (
    select * from tbl_team where name = 'mountaineers'
)
insert into tbl_player (first_name, last_name, dob, home_city, home_country, team_id)
select 'mt1_first', 'mt1_last', current_timestamp, 'mt1_city', 'mt1_country',mountaineers.id from mountaineers
union
select 'mt2_first', 'mt2_last', current_timestamp, 'mt2_city', 'mt2_country',mountaineers.id from mountaineers
union
select 'mt3_first', 'mt3_last', current_timestamp, 'mt3_city', 'mt3_country',mountaineers.id from mountaineers
union
select 'mt4_first', 'mt4_last', current_timestamp, 'mt4_city', 'mt4_country',mountaineers.id from mountaineers;

with snowSide as (
    select * from tbl_team where name = 'snow-side'
)
insert into tbl_player (first_name, last_name, dob, home_city, home_country, team_id)
select 'ss1_first', 'ss1_last', current_timestamp, 'ss1_city', 'ss1_country',snowSide.id from snowSide
union
select 'ss2_first', 'ss2_last', current_timestamp, 'ss2_city', 'ss2_country',snowSide.id from snowSide
union
select 'ss3_first', 'ss3_last', current_timestamp, 'ss3_city', 'ss3_country',snowSide.id from snowSide
union
select 'ss_first', 'ss4_last', current_timestamp, 'ss4_city', 'ss4_country',snowSide.id from snowSide;

with winterLand as (
    select * from tbl_team where name = 'winter-land'
)
insert into tbl_player (first_name, last_name, dob, home_city, home_country, team_id)
select 'wl1_first', 'wl1_last', current_timestamp, 'wl1_city', 'wl1_country',winterLand.id from winterLand
union
select 'wl2_first', 'wl2_last', current_timestamp, 'wl2_city', 'wl2_country',winterLand.id from winterLand
union
select 'wl3_first', 'wl3_last', current_timestamp, 'wl3_city', 'wl3_country',winterLand.id from winterLand
union
select 'wl4_first', 'wl4_last', current_timestamp, 'wl4_city', 'wl4_country',winterLand.id from winterLand;

with rowingSponsor as(
    insert into tbl_sponsor (name) values('rowing_sponsor') returning id
)
insert into tbl_prize (rank, title, sport_id, sponsor_id)
select 1, 'rowing_1st', (select id from tbl_sport where name = 'rowing'), rowingSponsor.id from rowingSponsor
union
select 2, 'rowing_2nd', (select id from tbl_sport where name = 'rowing'), rowingSponsor.id from rowingSponsor
union
select 3, 'rowing_3rd', (select id from tbl_sport where name = 'rowing'), rowingSponsor.id from rowingSponsor;

with bikingSponsor as(
    insert into tbl_sponsor (name) values('biking_sponsor') returning id
)
insert into tbl_prize (rank, title, sport_id, sponsor_id)
select 1, 'biking_1st', (select id from tbl_sport where name = 'biking'), bikingSponsor.id from bikingSponsor
union
select 2, 'biking_2nd', (select id from tbl_sport where name = 'biking'), bikingSponsor.id from bikingSponsor
union
select 3, 'biking_3rd', (select id from tbl_sport where name = 'biking'), bikingSponsor.id from bikingSponsor;

with skiingSponsor as(
    insert into tbl_sponsor (name) values('skiing_sponsor') returning id
)
insert into tbl_prize (rank, title, sport_id, sponsor_id)
select 1, 'skiing_1st', (select id from tbl_sport where name = 'skiing'), skiingSponsor.id from skiingSponsor
union
select 2, 'skiing_2nd', (select id from tbl_sport where name = 'skiing'), skiingSponsor.id from skiingSponsor
union
select 3, 'skiing_3rd', (select id from tbl_sport where name = 'skiing'), skiingSponsor.id from skiingSponsor;
