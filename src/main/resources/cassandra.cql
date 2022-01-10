**Queries to create the cassandra table**


CREATE KEYSPACE "sunbird" WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1} ;

use sunbird;

```sh
CREATE TABLE [IF NOT EXISTS]
 mandatory_user_content(
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
CREATE TABLE [IF NOT EXISTS]
 sunbird.user_assessment_summary (
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
CREATE TABLE [IF NOT EXISTS] sunbird.user_assessment_master (
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
CREATE TABLE [IF NOT EXISTS] sunbird.user_quiz_master (
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
CREATE TABLE [IF NOT EXISTS] sunbird.user_quiz_summary (
    root_org text,
    user_id text,
    content_id text,
    date_updated timestamp,
    PRIMARY KEY ((root_org, user_id), content_id)
);
```

```sh
CREATE TABLE [IF NOT EXISTS] work_order(
    id text,
    data text,
    PRIMARY KEY (id)
);
```
```sh
CREATE TABLE [IF NOT EXISTS] work_allocation(
    id text,
    data text,
    PRIMARY KEY (id)
);
```
```sh
CREATE TABLE [IF NOT EXISTS] user_work_allocation_mapping(
    userid text,
    workallocationid text,
    workorderid text,
    status text,
    PRIMARY KEY (userid, workallocationid)
);
```
```sh
CREATE TABLE [IF NOT EXISTS] sunbird.org_staff_position (
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
CREATE TABLE [IF NOT EXISTS] sunbird.org_budget_scheme (
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
CREATE TABLE [IF NOT EXISTS] sunbird.org_audit (
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
