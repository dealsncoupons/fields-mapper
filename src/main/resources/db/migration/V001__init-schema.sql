drop table if exists tbl_prize;
drop table if exists tbl_sponsor;
drop table if exists tbl_player;
drop table if exists tbl_team;
drop table if exists tbl_sport;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table if not exists tbl_sport (
    id UUID default uuid_generate_v1(),
    name varchar(64) not null,
    venue varchar(64) default 'to be determined',
    start_time timestamp,
    constraint pk_sport_id primary key(id)
);

create table if not exists tbl_team (
    id UUID default uuid_generate_v1(),
    name varchar(64) not null,
    sport_id UUID not null,
    constraint pk_team_id primary key(id),
    constraint fk_sport_id foreign key(sport_id) references tbl_sport(id) on delete cascade,
    constraint uniq_team_sport unique(id, sport_id)
);

create table if not exists tbl_player (
    id UUID default uuid_generate_v1(),
    first_name varchar(64) not null,
    last_name varchar(64) not null,
    dob timestamp not null,
    home_city varchar(64) not null,
    home_country varchar(64) not null,
    team_id UUID not null,
    constraint pk_player_id primary key(id),
    constraint fk_team_id foreign key(team_id) references tbl_team(id)
);

create table if not exists tbl_sponsor (
    id UUID default uuid_generate_v1(),
    name varchar(64) not null,
    mission_statement varchar(64),
    constraint pk_sponsor_id primary key(id)
);

create table if not exists tbl_prize (
    id UUID default uuid_generate_v1(),
    rank int not null,
    title varchar(64) not null,
    prize_value decimal default 0.0,
    value_currency varchar(3) default 'USD',
    sponsor_id UUID not null,
    sport_id UUID not null,
    winner_id UUID,
    date_awarded timestamp,
    date_created timestamp default current_timestamp,
    constraint pk_prize_id primary key(id),
    constraint fk_sport_id foreign key(sport_id) references tbl_sport(id),
    constraint fk_winner_id foreign key(winner_id) references tbl_player(id),
    constraint fk_sponsor_id foreign key(sponsor_id) references tbl_sponsor(id) on delete cascade
);
