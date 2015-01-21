SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `crawl` ;
CREATE SCHEMA IF NOT EXISTS `crawl` DEFAULT CHARACTER SET latin1 ;
USE `crawl` ;

-- -----------------------------------------------------
-- Table `crawl`.`runs`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`runs` ;

CREATE  TABLE IF NOT EXISTS `crawl`.`runs` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `seed` VARCHAR(255) NOT NULL ,
  `start` TIMESTAMP NULL DEFAULT NULL ,
  `end` TIMESTAMP NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 53
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `crawl`.`pages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`pages` ;

CREATE  TABLE IF NOT EXISTS `crawl`.`pages` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `runid` INT(11) NOT NULL ,
  `url` VARCHAR(2048) NOT NULL ,
  `status` INT(11) NULL DEFAULT NULL ,
  `title` VARCHAR(2048) NULL DEFAULT NULL ,
  `text` MEDIUMTEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`id`, `runid`) ,
  INDEX `fk_pages_runs_idx` (`runid` ASC) ,
  INDEX `idx_pages_url` (`url`(255) ASC) ,
  INDEX `idx_pages_status` (`status` ASC) ,
  CONSTRAINT `fk_pages_runs`
    FOREIGN KEY (`runid` )
    REFERENCES `crawl`.`runs` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 91023
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `crawl`.`links`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`links` ;

CREATE  TABLE IF NOT EXISTS `crawl`.`links` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `page_from` INT(11) NOT NULL ,
  `page_to` INT(11) NOT NULL ,
  `text` VARCHAR(2048) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_page_from_idx` (`page_from` ASC) ,
  INDEX `fk_page_to_idx` (`page_to` ASC) ,
  INDEX `text_idx` (`text`(767) ASC) ,
  CONSTRAINT `fk_page_from`
    FOREIGN KEY (`page_from` )
    REFERENCES `crawl`.`pages` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_page_to`
    FOREIGN KEY (`page_to` )
    REFERENCES `crawl`.`pages` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 123043
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `crawl`.`map_reduce_jobs`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`map_reduce_jobs` ;

CREATE  TABLE IF NOT EXISTS `crawl`.`map_reduce_jobs` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `runid` INT(11) NULL DEFAULT NULL ,
  `appenginemrid` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_mrj_runs` (`runid` ASC) ,
  INDEX `idx_appenginemrid` (`appenginemrid` ASC) ,
  CONSTRAINT `fk_mrj_runs`
    FOREIGN KEY (`runid` )
    REFERENCES `crawl`.`runs` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `crawl`.`words`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`words` ;

CREATE  TABLE IF NOT EXISTS `crawl`.`words` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `runid` INT(11) NULL DEFAULT NULL ,
  `word` VARCHAR(256) NULL DEFAULT NULL ,
  `occurrences` INT(11) NULL DEFAULT '0' ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `run_word_idx` (`runid` ASC, `word` ASC) ,
  INDEX `fk_words_runs_idx` (`runid` ASC) ,
  CONSTRAINT `fk_words_runs`
    FOREIGN KEY (`runid` )
    REFERENCES `crawl`.`runs` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `crawl`.`word_pages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `crawl`.`word_pages` ;

CREATE  TABLE IF NOT EXISTS `crawl`.`word_pages` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `word` INT(11) NULL DEFAULT NULL ,
  `page` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_word_pages_words_idx` (`word` ASC) ,
  INDEX `fk_word_pages_pages_idx` (`page` ASC) ,
  CONSTRAINT `fk_word_pages_words`
    FOREIGN KEY (`word` )
    REFERENCES `crawl`.`words` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_word_pages_pages`
    FOREIGN KEY (`page` )
    REFERENCES `crawl`.`pages` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
