# sunbird-cb-ext

This service created to manage portal module, work allocation tool and many others features.

## Features

- Portal module (MDO, SPV and CBP)
- Work allocation tool
- Mandatory content check

## Tech

SB-CB-EXT uses a number of open source projects:

- [APACHE KAFKA] - used for data pipeline and data integration
- [APACHE CASSANDRA] - No sql data base used for scalability and high availability
- [ELASTIC SEARCH] - No sql data base used for great searching capability
- [SPRING BOOT] - Great framework to work with java.
- [JAVA] - used for core development
- [POSTGRESQL] - a relational database


## Installation

**Postgresql table list**

- department_types
- departments
- roles
- department_roles
- user_department_role
- user_department_role_audit

**Queries to create the tables**

```sh
CREATE TABLE IF NOT EXISTS department_types (
  id SERIAL PRIMARY KEY,
  dept_type VARCHAR(64) NOT NULL,
  dept_subType VARCHAR(64) NOT NULL,
  description TEXT,
  UNIQUE (dept_type, dept_subType)
);
```
```sh
CREATE TABLE IF NOT EXISTS departments (
  id SERIAL PRIMARY KEY, 
  root_org VARCHAR(32) NOT NULL,
  dept_name VARCHAR(128) UNIQUE NOT NULL,
  dept_type_ids integer[] NOT NULL,
  description TEXT,
  headquarters VARCHAR(64),
  logo bytea,
  creation_date bigint,
  created_by text
);
```
```sh
CREATE TABLE IF NOT EXISTS roles (
  id SERIAL PRIMARY KEY,
  role_name VARCHAR(64) UNIQUE NOT NULL,
  description TEXT
);
```
```sh
CREATE TABLE IF NOT EXISTS department_roles (
  id SERIAL PRIMARY KEY,
  dept_type VARCHAR(64) NOT NULL,
  role_ids integer[] NOT NULL
);

```
```sh
CREATE TABLE IF NOT EXISTS user_department_role (
  id SERIAL PRIMARY KEY,
  user_id TEXT NOT NULL, 
  dept_id int REFERENCES departments (id) NOT NULL,
  role_ids integer[] NOT NULL,
  isActive boolean NOT NULL,
  isBlocked boolean NOT NULL
);
```

```sh
CREATE SEQUENCE userdeptrole_audit_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
```
```sh
CREATE TABLE user_department_role_audit
(
    id integer NOT NULL DEFAULT nextval('userdeptrole_audit_id_seq'::regclass),
    user_id text,
    dept_id integer,
    role_ids integer[],
    isactive boolean,
    isblocked boolean,
    created_by text,
    created_time bigint,
    CONSTRAINT user_department_role_audit_pkey PRIMARY KEY (id)
);
```
**Cassandra table list**

- mandatory_user_content

**Queries to create the cassandra table**

```sh
CREATE TABLE mandatory_user_content(
    root_org text,
    org text,
    content_id text,
    batch_id text,
    content_type text,
    minprogressforcompletion float,
    PRIMARY KEY (root_org, org, content_id)
);
```
