
create TABLE user_email(
  id                      UUID PRIMARY KEY,
  email                   VARCHAR(255) NOT NULL UNIQUE,
  created_at              TIMESTAMP WITH TIME ZONE NOT NULL default 'now()'
);

create TABLE sheet_name(
  id                      UUID PRIMARY KEY,
  author_id               UUID NOT NULL references user_email,
  fileId                  UUID NOT NULL,
  name                    TEXT NOT NULL UNIQUE,
  created_at              TIMESTAMP WITH TIME ZONE NOT NULL default 'now()',
  UNIQUE (fileId, name)
);

create TABLE sharing_group(
  id                      UUID PRIMARY KEY,
  author_id               UUID NOT NULL references user_email,
  permission_read         BOOLEAN NOT NULL default TRUE,
  permission_write        BOOLEAN NOT NULL default FALSE,
  permission_share        BOOLEAN NOT NULL default FALSE,
  created_at              TIMESTAMP WITH TIME ZONE NOT NULL default 'now()'
  );

create TABLE data_reference(
  id                      UUID PRIMARY KEY,
  sharing_group_id        UUID NOT NULL references sharing_group,
  file_id                 UUID NOT NULL,
  sheet_id                UUID references sheet_name,
  range_cells_set         VARCHAR,
  range_cells_between     VARCHAR,
  created_at              TIMESTAMP WITH TIME ZONE NOT NULL default 'now()'

);


