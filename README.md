# sunbird-cb-ext

This service created to manage portal module, work allocation tool and many others features.

## Features

- Portal module (MDO, SPV, CBP, CBC)
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

**Kafka Topics Required**

- orgCreation - Used to create SB Org object when new Department is created
- userRoleAuditTopic - Used to update the user_department_role record in Audit table 

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
  logo text,
  creation_date bigint,
  source_id integer,
  created_by text,
  isdeleted boolean NOT NULL
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
  isBlocked boolean NOT NULL,
  source_user_id TEXT
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
- user_assessment_summary
- user_assessment_master
- user_quiz_master
- user_quiz_summary

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

```sh
CREATE TABLE sunbird.user_assessment_summary (
    root_org text,
    user_id text,
    content_id text,
    first_passed_score float,
    first_passed_score_date timestamp,
    max_score float,
    max_score_date timestamp,
    PRIMARY KEY ((root_org, user_id), content_id)
);
```

```sh
CREATE TABLE sunbird.user_assessment_master (
    root_org text,
    ts_created timestamp,
    parent_source_id text,
    result_percent decimal,
    id uuid,
    correct_count int,
    date_created timestamp,
    incorrect_count int,
    not_answered_count int,
    parent_content_type text,
    pass_percent decimal,
    source_id text,
    source_title text,
    user_id text,
    PRIMARY KEY ((root_org, ts_created), parent_source_id, result_percent, id)
);
```

```sh
CREATE MATERIALIZED VIEW sunbird.user_assessment_top_performer AS
    SELECT root_org, parent_source_id, ts_created, result_percent, id, pass_percent, source_id, source_title, user_id
    FROM sunbird.user_assessment_master
    WHERE root_org IS NOT NULL AND ts_created IS NOT NULL AND parent_source_id IS NOT NULL AND id IS NOT NULL AND result_percent IS NOT NULL AND result_percent >= 90
    PRIMARY KEY ((root_org, parent_source_id), ts_created, result_percent, id);
```

```sh
CREATE MATERIALIZED VIEW sunbird.user_assessment_by_date AS
    SELECT root_org, date_created, ts_created, parent_source_id, result_percent, id, parent_content_type, pass_percent, source_id, user_id
    FROM sunbird.user_assessment_master
    WHERE root_org IS NOT NULL AND date_created IS NOT NULL AND ts_created IS NOT NULL AND parent_source_id IS NOT NULL AND id IS NOT NULL AND result_percent IS NOT NULL
    PRIMARY KEY ((root_org, date_created), ts_created, parent_source_id, result_percent, id);
```

```sh
CREATE MATERIALIZED VIEW sunbird.assessment_by_content_user AS
    SELECT root_org, user_id, parent_source_id, ts_created, result_percent, id, correct_count, incorrect_count, not_answered_count, pass_percent, source_id, source_title
    FROM sunbird.user_assessment_master
    WHERE root_org IS NOT NULL AND ts_created IS NOT NULL AND parent_source_id IS NOT NULL AND id IS NOT NULL AND result_percent IS NOT NULL AND user_id IS NOT NULL
    PRIMARY KEY ((root_org, user_id, parent_source_id), ts_created, result_percent, id);
```

```sh
CREATE TABLE sunbird.user_quiz_master (
    root_org text,
    ts_created timestamp,
    result_percent decimal,
    id uuid,
    correct_count int,
    date_created timestamp,
    incorrect_count int,
    not_answered_count int,
    pass_percent decimal,
    source_id text,
    source_title text,
    user_id text,
    PRIMARY KEY ((root_org, ts_created), result_percent, id)
);
```

```sh
CREATE TABLE sunbird.user_quiz_summary (
    root_org text,
    user_id text,
    content_id text,
    date_updated timestamp,
    PRIMARY KEY ((root_org, user_id), content_id)
);
```
