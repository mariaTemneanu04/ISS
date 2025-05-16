create table users (
	id bigint primary key generated always as identity,
	username varchar(50) not null unique,
	password varchar(100) not null
)

create table librarian (
	id bigint primary key,
	firstName varchar(100) not null,
	lastName varchar(100) not null,
	foreign key (id) references users(id)
);

create table reader (
	id bigint primary key,
	cnp varchar(12) not null,
	firstName varchar(100) not null,
	lastName varchar(100) not null,
	address varchar(200) not null,
	nrTel varchar(13) not null,
	foreign key (id) references users(id)
)

create table books (
	id bigint primary key generated always as identity,
	title varchar(100) not null,
	author varchar(150) not null,
	status varchar(50)
)

create table loans (
	id bigint primary key generated always as identity,
	dateOfLoan timestamp,
	dateOfReturn timestamp,
	status varchar(50),
	rid bigint not null,
	bid bigint not null,
	foreign key (rid) references reader(id),
	foreign key (bid) references books(id)
)

