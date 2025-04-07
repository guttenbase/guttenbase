CREATE TABLE FOO_USER
(
    ID              bigint PRIMARY KEY AUTO_INCREMENT,
    PERSONAL_NUMBER SMALLINT,
    USERNAME        varchar(100),
    NAME            varchar(100),
    PASSWORD        varchar(255),
    CREATED         TIMESTAMP,
    COMPANY_ID      bigint
);

CREATE TABLE FOO_ROLE
(
    ID         bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    FIXED_ROLE char(1),
    ROLE_NAME  varchar(100)
);

CREATE TABLE FOO_COMPANY
(
    ID       bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    SUPPLIER char(1),
    NAME     varchar(100)
);

CREATE TABLE FOO_USER_COMPANY
(
    USER_ID             bigint,
    ASSIGNED_COMPANY_ID bigint,

    CONSTRAINT FK_USER_COMPANY_COMPANY FOREIGN KEY (ASSIGNED_COMPANY_ID) REFERENCES FOO_COMPANY (ID),
    CONSTRAINT FK_USER_COMPANY_USER FOREIGN KEY (USER_ID) REFERENCES FOO_USER (ID)
);

CREATE TABLE FOO_USER_ROLES
(
    USER_ID bigint,
    ROLE_ID bigint,

    CONSTRAINT FK_USER_ROLES_USER FOREIGN KEY (USER_ID) REFERENCES FOO_USER (ID),
    CONSTRAINT FK_USER_ROLES_ROLE FOREIGN KEY (ROLE_ID) REFERENCES FOO_ROLE (ID)
);

CREATE TABLE FOO_DATA
(
    ID        bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    SOME_DATA BLOB
);

ALTER TABLE FOO_USER ADD CONSTRAINT FK_FOO_COMPANY FOREIGN KEY (COMPANY_ID) REFERENCES FOO_COMPANY (ID);

CREATE INDEX COMPANY_IDX ON FOO_USER (COMPANY_ID);

CREATE UNIQUE INDEX FOO_USER_IDX ON FOO_USER (ID);
CREATE UNIQUE INDEX USERNAME_IDX ON FOO_USER (USERNAME);
CREATE UNIQUE INDEX COMPANY_NAME_IDX ON FOO_COMPANY (NAME);
CREATE UNIQUE INDEX FOO_ROLE_NAME_IDX ON FOO_ROLE (ROLE_NAME);

CREATE OR REPLACE VIEW VIEW_DATA AS SELECT DISTINCT FR.ROLE_NAME FROM FOO_USER
    INNER JOIN FOO_USER_ROLES FUR ON FOO_USER.ID = FUR.USER_ID
    INNER JOIN FOO_ROLE FR ON FUR.ROLE_ID = FR.ID;