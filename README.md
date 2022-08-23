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
- dev.rating.event - Used to produce the event when an add or update rating is done

**ES Index Details**
- Need to create indexes 
  - workallocationv2 - this name should be same as property value of workallocationv2.index.name
  - workorderv1 - this name should be same as property value of workorder.index.name

- Update the work_allocation mapping using file @ https://github.com/sunbird-cb/sunbird-cb-ext/blob/cbrelease-3.0.1/src/main/resources/elasticsearch/index/workallocationv2.json

- Update the work_order mapping using file @ https://github.com/sunbird-cb/sunbird-cb-ext/blob/cbrelease-3.0.1/src/main/resources/elasticsearch/index/workorderv1.json

**ES Index Alias Details**
- Check any existing index has alias "work_allocation" or "work_order". If so, delete the alias mapping using following CURL in ElasticSerach server.
	- curl --location --request DELETE 'http://localhost:9200/{{existingIndexName}}/_alias/{{aliasName}}'
	- existingIndexName - refers the existing index in ES
	- aliasName - refers either "work_allocation" or "work_order"

- Add alias for newly created Indexes
	- curl --location --request POST 'http://localhost:9200/_aliases' --header 'Content-Type: application/json' --data-raw '{"actions":[{"add":{"index":"workorderv1","alias":"work_order"}}]}'
	- curl --location --request POST 'http://localhost:9200/_aliases' --header 'Content-Type: application/json' --data-raw '{"actions":[{"add":{"index":"workallocationv2","alias":"work_allocation"}}]}'

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
- work_order
- work_allocation
- org_staff_position
- org_budget_scheme
- org_audit

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

```sh
CREATE TABLE work_order(
    id text,
    data text,
    PRIMARY KEY (id)
);
```
```sh
CREATE TABLE work_allocation(
    id text,
    data text,
    PRIMARY KEY (id)
);
```
```sh
CREATE TABLE user_work_allocation_mapping(
    userid text,
    workallocationid text,
    workorderid text,
    status text,
    PRIMARY KEY (userid, workallocationid)
);
```
```sh
CREATE TABLE sunbird.org_staff_position (
	orgId text,
	id text,
	position text, 
	totalPositionsFilled int,
	totalPositionsVacant int,
	PRIMARY KEY (orgId, id)
);
CREATE INDEX IF NOT EXISTS staff_position_index on sunbird.org_staff_position (position);
```
```sh
CREATE TABLE sunbird.org_budget_scheme (
	orgId text,
	budgetYear text,
	id text,
	schemeName text,
	salaryBudgetAllocated bigint,
	trainingBudgetAllocated bigint, 
	trainingBudgetUtilization bigint, 
	proofDocs frozen<list<map<text,text>>>,
	PRIMARY KEY ((orgId, budgetYear), id)
);
CREATE INDEX IF NOT EXISTS budget_schemeName_index on sunbird.org_budget_scheme (schemeName);
```
```sh
CREATE TABLE sunbird.org_audit (
	orgId text,
	auditType text,
	createdBy text,
	createdDate text,
	updatedBy text,
	updatedDate text,
	transactionDetails text,
	PRIMARY KEY (orgId, auditType, createdDate, updatedDate)
);
```
```sh
CREATE TABLE sunbird.ratings (
    activity_id text,
    activity_type text,
    userid text,
    comment text,
    commentby text,
    commentupdatedon timeuuid,
    createdon timeuuid,
    rating float,
    review text,
    updatedon timeuuid,
    PRIMARY KEY ((activity_id, activity_type, userid))
);
```
```sh
CREATE TABLE sunbird.ratings_summary (
    activity_id text,
    activity_type text,
    latest50reviews text,
    sum_of_total_ratings float,
    total_number_of_ratings float,
    totalcount1stars float,
    totalcount2stars float,
    totalcount3stars float,
    totalcount4stars float,
    totalcount5stars float,
    PRIMARY KEY (activity_id, activity_type)
) WITH CLUSTERING ORDER BY (activity_type ASC);
```
```sh
CREATE TABLE sunbird.ratings_lookup (
    activity_id text,
    activity_type text,
    rating float,
    updatedon timeuuid,
    userid text,
    PRIMARY KEY ((activity_id, activity_type, rating), updatedon)
) WITH CLUSTERING ORDER BY (updatedon DESC); 
```
```sh
CREATE TABLE sunbird.explore_course_list ( identifier text primary key ) ;
```
