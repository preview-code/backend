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
  pull_request_id BIGINT NOT NULL CONSTRAINT fk_groups_pull_request REFERENCES preview_code.pull_request(id),
  default_group BOOLEAN,

  CONSTRAINT unique_default_group UNIQUE (default_group, pull_request_id)

);

CREATE INDEX fk_group_pull_id ON preview_code.groups (pull_request_id);


CREATE SEQUENCE preview_code.seq_pk_hunk;

CREATE TABLE preview_code.hunk (
  id BIGINT DEFAULT nextval('preview_code.seq_pk_hunk') NOT NULL CONSTRAINT pk_hunk PRIMARY KEY,
  checksum VARCHAR NOT NULL,
  group_id BIGINT NOT NULL CONSTRAINT fk_hunk_group_id REFERENCES preview_code.groups(id) ON DELETE CASCADE,
  pull_request_id BIGINT NOT NULL CONSTRAINT fk_hunk_pull_id REFERENCES preview_code.pull_request(id),

  CONSTRAINT unique_hunkId_groupId UNIQUE (checksum, pull_request_id)
);

CREATE INDEX idx_fk_hunk_group_id ON preview_code.hunk (group_id);


CREATE TABLE preview_code.approval (
  hunk_id BIGINT NOT NULL CONSTRAINT fk_approval_hunk REFERENCES preview_code.hunk(id),
  approver VARCHAR NOT NULL,
  status VARCHAR NOT NULL,

  CONSTRAINT pk_approval PRIMARY KEY (hunk_id, approver)
)

