-- creating database if it doesn't exist

CONNECT;
CREATE DATABASE IF NOT EXISTS ecare;
use ecare;

-- creating tables for eCare database

CREATE TABLE IF NOT EXISTS `plans` (
    id INT unsigned NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS `options` (
    id INT unsigned NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    turn_on_price DOUBLE NOT NULL,
    description VARCHAR(1000),
    deprecated TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS `plan_options` (
    id INT unsigned NOT NULL AUTO_INCREMENT,
    plan_id INT unsigned NOT NULL,
    option_id INT unsigned NOT NULL,
    undisablable TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY(id, plan_id, option_id),
    FOREIGN KEY (plan_id) REFERENCES plans (id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES options (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `users` (
    id INT unsigned NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    passport VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    blocked TINYINT NOT NULL DEFAULT 1,
    disabled_by INT unsigned DEFAULT NULL REFERENCES users,
    authority VARCHAR(50) NOT NULL,

    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS `user_locations` (
    id INT unsigned NOT NULL AUTO_INCREMENT,
    user_id INT unsigned NOT NULL,
    lat DOUBLE NOT NULL,
    lon DOUBLE NOT NULL,

    FOREIGN KEY(user_id) REFERENCES users(id),
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS `contracts` (
    id INT unsigned NOT NULL AUTO_INCREMENT,
    phone_number VARCHAR(255) NOT NULL,
    user_id INT unsigned NOT NULL,
    plan_id INT unsigned NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(plan_id) REFERENCES plans(id)
);

CREATE TABLE IF NOT EXISTS `contract_options` (
    id INT unsigned NOT NULL AUTO_INCREMENT,
    contract_id INT unsigned NOT NULL,
    option_id INT unsigned NOT NULL,
    undisablable TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY(id, contract_id, option_id),
    FOREIGN KEY (contract_id) REFERENCES contracts (id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES options (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `option_rules` (
    id INT unsigned NOT NULL AUTO_INCREMENT,
    option_id1 INT unsigned NOT NULL,
    rule VARCHAR(255) NOT NULL,
    option_id2 INT unsigned NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (option_id1) REFERENCES options (id) ON DELETE CASCADE,
    FOREIGN KEY (option_id2) REFERENCES options (id) ON DELETE CASCADE
);