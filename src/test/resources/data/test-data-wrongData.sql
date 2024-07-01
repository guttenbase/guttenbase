-- noinspection SqlResolveForFile
INSERT into FOO_COMPANY (ID, SUPPLIER, NAME) VALUES(1, 'Y', 'Company 4');
INSERT into FOO_COMPANY (ID, SUPPLIER, NAME) VALUES(2, 'Y', 'Company 3');
INSERT into FOO_COMPANY (ID, SUPPLIER, NAME) VALUES(3, 'Y', 'Company 2');
INSERT into FOO_COMPANY (ID, SUPPLIER, NAME) VALUES(4, 'Y', 'Häagen daß');

INSERT into FOO_ROLE (ID, FIXED_ROLE, ROLE_NAME) VALUES(1, 'Y', 'SD 1');
INSERT into FOO_ROLE (ID, FIXED_ROLE, ROLE_NAME) VALUES(2, 'Y', 'Rdsole 2');
INSERT into FOO_ROLE (ID, FIXED_ROLE, ROLE_NAME) VALUES(3, 'Y', 'Rosdsdle 3');
INSERT into FOO_ROLE (ID, FIXED_ROLE, ROLE_NAME) VALUES(4, 'Y', 'Rolsdsde 4');

INSERT into FOO_USER (ID, PERSONAL_NUMBER, USERNAME, NAME, PASSWORD, COMPANY_ID) VALUES(1, 123, 'User_1', 'User 1', 'secret', 1);
INSERT into FOO_USER (ID, PERSONAL_NUMBER, USERNAME, NAME, PASSWORD, COMPANY_ID) VALUES(2, 456, 'User_2', 'User 2', 'secret', 1);
INSERT into FOO_USER (ID, PERSONAL_NUMBER, USERNAME, NAME, PASSWORD, COMPANY_ID) VALUES(3, NULL, 'User_3', 'User 3', 'secret', 2);
INSERT into FOO_USER (ID, PERSONAL_NUMBER, USERNAME, NAME, PASSWORD, COMPANY_ID) VALUES(4, 777, 'User_4', 'User 4', 'secret', 3);
INSERT into FOO_USER (ID, PERSONAL_NUMBER, USERNAME, NAME, PASSWORD, COMPANY_ID) VALUES(5, NULL, 'User_5', 'User 5', 'secret', 4);

INSERT into FOO_USER_COMPANY (USER_ID, ASSIGNED_COMPANY_ID) VALUES(1, 4);
INSERT into FOO_USER_COMPANY (USER_ID, ASSIGNED_COMPANY_ID) VALUES(2, 2);
INSERT into FOO_USER_COMPANY (USER_ID, ASSIGNED_COMPANY_ID) VALUES(3, 3);

INSERT into FOO_USER_ROLES (USER_ID, ROLE_ID) VALUES(1, 1);
INSERT into FOO_USER_ROLES (USER_ID, ROLE_ID) VALUES(2, 1);
INSERT into FOO_USER_ROLES (USER_ID, ROLE_ID) VALUES(3, 3);
INSERT into FOO_USER_ROLES (USER_ID, ROLE_ID) VALUES(3, 4);
INSERT into FOO_USER_ROLES (USER_ID, ROLE_ID) VALUES(4, 4);



