DROP SCHEMA IF EXISTS preview_code CASCADE;
CREATE SCHEMA preview_code;

CREATE SEQUENCE preview_code.seq_pk_pull_request;

CREATE TABLE preview_code.pull_request (
  id BIGINT DEFAULT nextval('preview_code.seq_pk_pull_request') NOT NULL CONSTRAINT pk_pull_request PRIMARY KEY,
  owner VARCHAR NOT NULL,
  name VARCHAR NOT NULL,
  number INT NOT NULL
);

CREATE UNIQUE INDEX pull_owner_name_number ON preview_code.pull_request (
  owner, name, number
);


CREATE SEQUENCE preview_code.seq_pk_groups;

CREATE TABLE preview_code.groups (
  id BIGINT DEFAULT nextval('preview_code.seq_pk_groups') NOT NULL CONSTRAINT pk_groups PRIMARY KEY,
  title VARCHAR NOT NULL,
  description VARCHAR NOT NULL,
  pull_request_id BIGINT NOT NULL CONSTRAINT fk_groups_pull_request REFERENCES preview_code.pull_request(id)
);

CREATE INDEX fk_group_pull_id ON preview_code.groups (pull_request_id);


CREATE TABLE preview_code.hunk (
  id VARCHAR NOT NULL,
  group_id BIGINT NOT NULL CONSTRAINT fk_hunk_group_id REFERENCES preview_code.groups(id) ON DELETE CASCADE,

  CONSTRAINT unique_hunkId_groupId UNIQUE (id, group_id)
);

CREATE INDEX idx_fk_hunk_group_id ON preview_code.hunk (group_id);


CREATE TABLE preview_code.approval (
  pull_request_id BIGINT NOT NULL CONSTRAINT fk_approval_pull_request REFERENCES preview_code.pull_request(id),
  hunk_id VARCHAR NOT NULL,
  approver VARCHAR NOT NULL,
  status VARCHAR NOT NULL,

  CONSTRAINT pk_approval PRIMARY KEY (pull_request_id, hunk_id, approver)
)

