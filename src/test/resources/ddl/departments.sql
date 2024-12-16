/*
 * Encoding: UTF-8
 * Since: May, 2020
 * Author: gvenzl
 * Name: install.sql
 * Description: Setup script for departments and employees
 *
 * Copyright 2020 Gerald Venzl
 *
 * This work is licensed under the
 * Creative Commons Attribution 4.0 International Public License, CC BY 4.0
 *
 * Data Sources:
 * - This data set is fictional and has no data sources.
 *
 * The Data is provided "as is" without warranty or any representation of
 * accuracy, timeliness or completeness.
 *
 *    https://creativecommons.org/licenses/by/4.0/
 */

/*********************************************/
/*************** DEPARTMENTS *****************/
/*********************************************/

CREATE TABLE departments
(
    department_id SMALLINT    NOT NULL,
    name          VARCHAR(10) NOT NULL,
    location      VARCHAR(8)  NOT NULL,
    CONSTRAINT departments_pk PRIMARY KEY (department_id)
);

INSERT INTO departments (department_id, name, location)
VALUES (1, 'Operations', 'Vienna');
INSERT INTO departments (department_id, name, location)
VALUES (2, 'Sales', 'New York');
INSERT INTO departments (department_id, name, location)
VALUES (3, 'Marketing', 'Paris');
INSERT INTO departments (department_id, name, location)
VALUES (4, 'Accounting', 'London');

/*********************************************/
/**************** EMPLOYEES ******************/
/*********************************************/

CREATE TABLE employees
(
    employee_id   SMALLINT      NOT NULL,
    first_name    VARCHAR(8),
    last_name     VARCHAR(9)    NOT NULL,
    job_title     VARCHAR(9),
    manager_id    SMALLINT,
    hire_date     DATE          NOT NULL,
    salary        NUMERIC(7, 2) NOT NULL,
    commission    NUMERIC(7, 2),
    department_id SMALLINT      NOT NULL,
    CONSTRAINT employees_pk PRIMARY KEY (employee_id),
    CONSTRAINT employees_employees_fk001 FOREIGN KEY (manager_id) REFERENCES employees (employee_id),
    CONSTRAINT employees_departments_fk001 FOREIGN KEY (department_id) REFERENCES departments (department_id)
);
