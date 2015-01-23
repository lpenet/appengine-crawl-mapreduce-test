-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema crawl
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `crawl` ;

-- -----------------------------------------------------
-- Schema crawl
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `crawl` DEFAULT CHARACTER SET latin1 ;
USE `crawl` ;

-- -----------------------------------------------------
-- Table `crawl`.`runs`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`runs` ;

CREATE TABLE IF NOT EXISTS `crawl`.`runs` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `seed` VARCHAR(255) NOT NULL,
  `start` TIMESTAMP NULL DEFAULT NULL,
  `end` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 53
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `crawl`.`pages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`pages` ;

CREATE TABLE IF NOT EXISTS `crawl`.`pages` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `runid` INT(11) NOT NULL,
  `url` VARCHAR(2048) NOT NULL,
  `status` INT(11) NULL DEFAULT NULL,
  `title` VARCHAR(2048) NULL DEFAULT NULL,
  `text` MEDIUMTEXT NULL DEFAULT NULL,
  PRIMARY KEY (`id`, `runid`),
  CONSTRAINT `fk_pages_runs`
    FOREIGN KEY (`runid`)
    REFERENCES `crawl`.`runs` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 91023
DEFAULT CHARACTER SET = utf8;

CREATE INDEX `fk_pages_runs_idx` ON `crawl`.`pages` (`runid` ASC);

CREATE INDEX `idx_pages_url` ON `crawl`.`pages` (`url`(255) ASC);

CREATE INDEX `idx_pages_status` ON `crawl`.`pages` (`status` ASC);


-- -----------------------------------------------------
-- Table `crawl`.`links`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`links` ;

CREATE TABLE IF NOT EXISTS `crawl`.`links` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `page_from` INT(11) NOT NULL,
  `page_to` INT(11) NOT NULL,
  `text` VARCHAR(2048) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_page_from`
    FOREIGN KEY (`page_from`)
    REFERENCES `crawl`.`pages` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_page_to`
    FOREIGN KEY (`page_to`)
    REFERENCES `crawl`.`pages` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 123043
DEFAULT CHARACTER SET = latin1;

CREATE INDEX `fk_page_from_idx` ON `crawl`.`links` (`page_from` ASC);

CREATE INDEX `fk_page_to_idx` ON `crawl`.`links` (`page_to` ASC);

CREATE INDEX `text_idx` ON `crawl`.`links` (`text`(767) ASC);


-- -----------------------------------------------------
-- Table `crawl`.`map_reduce_jobs`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`map_reduce_jobs` ;

CREATE TABLE IF NOT EXISTS `crawl`.`map_reduce_jobs` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `runid` INT(11) NULL DEFAULT NULL,
  `appenginemrid` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_mrj_runs`
    FOREIGN KEY (`runid`)
    REFERENCES `crawl`.`runs` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE INDEX `fk_mrj_runs` ON `crawl`.`map_reduce_jobs` (`runid` ASC);

CREATE INDEX `idx_appenginemrid` ON `crawl`.`map_reduce_jobs` (`appenginemrid` ASC);


-- -----------------------------------------------------
-- Table `crawl`.`word_pages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`word_pages` ;

CREATE TABLE IF NOT EXISTS `crawl`.`word_pages` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `word` VARCHAR(256) NULL DEFAULT NULL,
  `page` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_word_pages_pages`
    FOREIGN KEY (`page`)
    REFERENCES `crawl`.`pages` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE INDEX `fk_word_pages_words_idx` ON `crawl`.`word_pages` (`word` ASC);

CREATE INDEX `fk_word_pages_pages_idx` ON `crawl`.`word_pages` (`page` ASC);


-- -----------------------------------------------------
-- Table `crawl`.`words`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`words` ;

CREATE TABLE IF NOT EXISTS `crawl`.`words` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `runid` INT(11) NULL DEFAULT NULL,
  `word` VARCHAR(256) NULL DEFAULT NULL,
  `occurrences` INT(11) NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_words_runs`
    FOREIGN KEY (`runid`)
    REFERENCES `crawl`.`runs` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

CREATE UNIQUE INDEX `run_word_idx` ON `crawl`.`words` (`runid` ASC, `word` ASC);

CREATE INDEX `fk_words_runs_idx` ON `crawl`.`words` (`runid` ASC);


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
