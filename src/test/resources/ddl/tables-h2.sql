CREATE TABLE FOO_USER
(
   ID bigint PRIMARY KEY AUTO_INCREMENT,
   PERSONAL_NUMBER SMALLINT,
   USERNAME varchar(100),
   NAME varchar(100),
   PASSWORD varchar(255),
   COMPANY_ID bigint
);

CREATE TABLE FOO_USER_COMPANY
(
   USER_ID bigint,
   ASSIGNED_COMPANY_ID bigint,
   CONSTRAINT PK_FOO_USER_COMPANY PRIMARY KEY (USER_ID, ASSIGNED_COMPANY_ID)
);

CREATE TABLE FOO_USER_ROLES
(
   USER_ID bigint,
   ROLE_ID bigint,
   CONSTRAINT PK_FOO_USER_ROLES PRIMARY KEY (USER_ID, ROLE_ID)
);

CREATE TABLE FOO_COMPANY
(
   ID bigint PRIMARY KEY AUTO_INCREMENT,
   SUPPLIER char(1),
   NAME varchar(100)
);

CREATE TABLE FOO_ROLE
(
   ID bigint PRIMARY KEY AUTO_INCREMENT,
   FIXED_ROLE char(1),
   ROLE_NAME varchar(100)
);

CREATE TABLE FOO_DATA
(
   ID bigint, -- PRIMARY KEY
   SOME_DATA BLOB
);

ALTER TABLE FOO_USER ADD CONSTRAINT FK_FOO_COMPANY FOREIGN KEY (COMPANY_ID) REFERENCES FOO_COMPANY(ID);
ALTER TABLE FOO_USER_COMPANY ADD CONSTRAINT FK_FOO_USER_COMPANY_COMPANY FOREIGN KEY (ASSIGNED_COMPANY_ID) REFERENCES FOO_COMPANY(ID);
ALTER TABLE FOO_USER_COMPANY ADD CONSTRAINT FK_FOO_USER_COMPANY_USER FOREIGN KEY (USER_ID) REFERENCES FOO_USER(ID);

CREATE UNIQUE INDEX FOO_USER_IDX ON FOO_USER(ID);

CREATE INDEX COMPANY_IDX ON FOO_USER(COMPANY_ID);

CREATE UNIQUE INDEX USERNAME_IDX ON FOO_USER(USERNAME);

CREATE UNIQUE INDEX COMPANY_NAME_IDX ON FOO_COMPANY(NAME);

ALTER TABLE FOO_USER_ROLES ADD CONSTRAINT FKFOO_USER_ROLES FOREIGN KEY (ROLE_ID) REFERENCES FOO_ROLE(ID);
ALTER TABLE FOO_USER_ROLES ADD CONSTRAINT FKFOO_USER_ROLES2 FOREIGN KEY (USER_ID) REFERENCES FOO_USER(ID);

CREATE UNIQUE INDEX FOO_ROLE_NAME ON FOO_ROLE(ROLE_NAME);