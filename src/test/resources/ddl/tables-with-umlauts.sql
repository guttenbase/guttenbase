CREATE TABLE FOO_ÜSER
(
   ID bigint PRIMARY KEY,
   PERSONAL_NUMBER SMALLINT,
   USERNAME varchar(100),
   NAME varchar(100),
   PASSWORD varchar(255),
   COMPANY_ID bigint
);

CREATE TABLE FOO_ÜSER_CÖMPÄNY
(
   USER_ID bigint,
   ASSIGNED_COMPANY_ID bigint,
   CONSTRAINT PK_FOO_USER_COMPANY PRIMARY KEY (USER_ID, ASSIGNED_COMPANY_ID)
);

CREATE TABLE FOO_ÜSER_RÖLES
(
   USER_ID bigint,
   ROLE_ID bigint,
   CONSTRAINT PK_FOO_USER_ROLES PRIMARY KEY (USER_ID, ROLE_ID)
);

CREATE TABLE FOO_CÖMPÄNY
(
   ID bigint PRIMARY KEY,
   SUPPLIER char(1),
   NAME varchar(100)
);

CREATE TABLE FOO_RÖLE
(
   ID bigint PRIMARY KEY,
   FIXED_ROLE char(1),
   ROLE_NAME varchar(100)
);

CREATE TABLE "FOO DATA"
(
   ID bigint PRIMARY KEY,
   SOME_DATA BLOB
);

ALTER TABLE FOO_ÜSER
ADD CONSTRAINT FK_FOO_COMPANY
FOREIGN KEY (COMPANY_ID)
REFERENCES FOO_CÖMPÄNY(ID);

ALTER TABLE FOO_ÜSER_CÖMPÄNY ADD CONSTRAINT FK_FOO_USER_COMPANY_COMPANY FOREIGN KEY (ASSIGNED_COMPANY_ID) REFERENCES FOO_CÖMPÄNY(ID);
ALTER TABLE FOO_ÜSER_CÖMPÄNY ADD CONSTRAINT FK_FOO_USER_COMPANY_USER FOREIGN KEY (USER_ID) REFERENCES FOO_ÜSER(ID);

CREATE UNIQUE INDEX FOO_USER_IDX ON FOO_ÜSER(ID);

CREATE INDEX COMPANY_IDX ON FOO_ÜSER(COMPANY_ID);

CREATE UNIQUE INDEX USERNAME_IDX ON FOO_ÜSER(USERNAME);

CREATE UNIQUE INDEX COMPANY_NAME_IDX ON FOO_CÖMPÄNY(NAME);

ALTER TABLE FOO_ÜSER_RÖLES
ADD CONSTRAINT FKFOO_USER_ROLES
FOREIGN KEY (ROLE_ID)
REFERENCES FOO_RÖLE(ID);

ALTER TABLE FOO_ÜSER_RÖLES
ADD CONSTRAINT FKFOO_USER_ROLES2
FOREIGN KEY (USER_ID)
REFERENCES FOO_ÜSER(ID);

CREATE UNIQUE INDEX FOO_ROLE_NAME ON FOO_RÖLE(ROLE_NAME);