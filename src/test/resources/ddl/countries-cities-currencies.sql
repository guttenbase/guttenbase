/*
 * Encoding: UTF-8
 * Since: March, 2020
 * Author: gvenzl
 * Name: install.sql
 * Description: DDL setup and data load script
 *
 * Copyright 2020 Gerald Venzl
 *
 * Data Sources:
 * - National Geographic Atlas of the World, 11th Edition, October 2019 (ISBN-13: 978-1426220586)
 * - GeoNames.org (https://www.geonames.org/)
 *
 * Provided with the generous help of my gorgeous wife Federica Venzl during the
 * Coronavirus (COVID-19) pandemic.
 *
 * This work is licensed under the
 * Creative Commons Attribution 4.0 International Public License, CC BY 4.0
 *
 * The Data is provided "as is" without warranty or any representation of
 * accuracy, timeliness or completeness.
 *
 *    https://creativecommons.org/licenses/by/4.0/
 */
/*********************************************/
/*********************************************/
/*********************************************/
/****** D a t a m o d e l   s e t u p ********/
/*********************************************/
/*********************************************/
/*********************************************/

/*********************************************/
/***************** REGIONS *******************/
/*********************************************/

CREATE TABLE regions
(
  region_id     VARCHAR(2)   NOT NULL,
  name          VARCHAR(13)  NOT NULL,
  CONSTRAINT regions_pk
    PRIMARY KEY (region_id)
);

/*********************************************/
/**************** COUNTRIES ******************/
/*********************************************/

CREATE TABLE countries
(
  country_id    VARCHAR(3)     NOT NULL,
  country_code  VARCHAR(2)     NOT NULL,
  name          VARCHAR(100)   NOT NULL,
  official_name VARCHAR(200),
  population    NUMERIC(10),
  area_sq_km    NUMERIC(10,2),
  latitude      NUMERIC(8,5),
  longitude     NUMERIC(8,5),
  timezone      VARCHAR(40),
  region_id     VARCHAR(2)     NOT NULL,
  CONSTRAINT countries_pk    PRIMARY KEY (country_id),
  CONSTRAINT countries_regions_fk001    FOREIGN KEY (region_id) REFERENCES regions (region_id)
);

CREATE INDEX countries_regions_fk001 ON countries (region_id);

/*********************************************/
/***************** CITIES ********************/
/*********************************************/

CREATE TABLE cities
(
  city_id       VARCHAR(7)    NOT NULL,
  name          VARCHAR(100)  NOT NULL,
  official_name VARCHAR(200),
  population    NUMERIC(8),
  is_capital    CHAR(1)       DEFAULT 'N' NOT NULL,
  latitude      NUMERIC(8,5),
  longitude     NUMERIC(8,5),
  timezone      VARCHAR(40),
  country_id    VARCHAR(3)    NOT NULL,
  CONSTRAINT cities_pk    PRIMARY KEY (city_id),
  CONSTRAINT cities_countries_fk001    FOREIGN KEY (country_id) REFERENCES countries (country_id),
  CONSTRAINT cities_is_capital_Y_N_check001    CHECK (is_capital IN ('Y','N'))
);

CREATE INDEX cities_countries_fk001 ON cities (country_id);

/*********************************************/
/***************** CURRENCIES ****************/
/*********************************************/

CREATE TABLE currencies
(
  currency_id       VARCHAR(3)    NOT NULL,
  name              VARCHAR(50)   NOT NULL,
  official_name     VARCHAR(200),
  symbol            VARCHAR(18)   NOT NULL,
  CONSTRAINT currencies_pk    PRIMARY KEY (currency_id)
);

/*********************************************/
/*********** CURRENCIES_COUNTRIES ************/
/*********************************************/

CREATE TABLE currencies_countries
(
  currency_id    VARCHAR(3)   NOT NULL,
  country_id     VARCHAR(3)   NOT NULL,
  CONSTRAINT currencies_countries_pk    PRIMARY KEY (currency_id, country_id),
  CONSTRAINT currencies_countries_currencies_fk001    FOREIGN KEY (currency_id) REFERENCES currencies (currency_id),
  CONSTRAINT currencies_countries_countries_fk002    FOREIGN KEY (country_id)  REFERENCES countries(country_id)
);
