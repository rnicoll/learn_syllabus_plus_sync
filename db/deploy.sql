CREATE DATABASE  IF NOT EXISTS `learn_group` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `learn_group`;
-- MySQL dump 10.13  Distrib 5.5.16, for Win32 (x86)
--
-- Host: localhost    Database: learn_group
-- ------------------------------------------------------
-- Server version	5.5.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `module`
--

DROP TABLE IF EXISTS `module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `module` (
  `tt_module_id` varchar(32) NOT NULL,
  `tt_course_code` VARCHAR(20) DEFAULT NULL,
  `tt_academic_year` VARCHAR(12) DEFAULT NULL,
  `merge_course_code` VARCHAR(40) DEFAULT NULL,
  `learn_course_code` VARCHAR(40) DEFAULT NULL,
  `learn_course_id` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`tt_module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity` (
  `tt_activity_id` varchar(32) NOT NULL,
  `tt_module_id` varchar(32) DEFAULT NULL,
  `learn_group_id` varchar(80) DEFAULT NULL,
  `tt_jta_activity_id` VARCHAR(32) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`tt_activity_id`),
  KEY `tt_module_id` (`tt_module_id`),
  CONSTRAINT `activity_ibfk_1` FOREIGN KEY (`tt_module_id`) REFERENCES `module` (`tt_module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `student_set`
--

DROP TABLE IF EXISTS `student_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `student_set` (
  `tt_student_set_id` varchar(32) NOT NULL,
  `tt_host_key` varchar(32) NOT NULL,
  `learn_person_id` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`tt_student_set_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `synchronisation_run`
--

DROP TABLE IF EXISTS `synchronisation_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `synchronisation_run` (
  `run_id` int(11) NOT NULL AUTO_INCREMENT,
  `previous_run_id` int(11) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `cache_copy_completed` datetime DEFAULT NULL,
  `diff_completed` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`run_id`),
  UNIQUE KEY `synchronisation_previous_run` (`previous_run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cache_enrolment`
--

DROP TABLE IF EXISTS `cache_enrolment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cache_enrolment` (
  `run_id` int(11) NOT NULL,
  `tt_student_set_id` varchar(32) NOT NULL,
  `tt_activity_id` varchar(32) NOT NULL,
  PRIMARY KEY (`run_id`,`tt_student_set_id`,`tt_activity_id`),
  KEY `tt_activity_id` (`tt_activity_id`),
  CONSTRAINT `cache_enrolment_ibfk_1` FOREIGN KEY (`tt_activity_id`) REFERENCES `activity` (`tt_activity_id`),
  CONSTRAINT `cache_enrolment_ibfk_2` FOREIGN KEY (`run_id`) REFERENCES `synchronisation_run` (`run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



--
-- Table structure for table `enrolment_add`
--

DROP TABLE IF EXISTS `enrolment_add`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enrolment_add` (
  `change_id` int(11) NOT NULL AUTO_INCREMENT,
  `run_id` int(11) NOT NULL,
  `tt_activity_id` varchar(32) NOT NULL,
  `tt_student_set_id` varchar(32) NOT NULL,
  `update_completed` datetime DEFAULT NULL,
  PRIMARY KEY (`change_id`),
  UNIQUE KEY `run_id` (`run_id`,`tt_activity_id`,`tt_student_set_id`),
  KEY `enrolment_add_activ` (`tt_activity_id`),
  KEY `enrolment_add_stu` (`tt_student_set_id`),
  CONSTRAINT `enrolment_add_activ` FOREIGN KEY (`tt_activity_id`) REFERENCES `activity` (`tt_activity_id`),
  CONSTRAINT `enrolment_add_stu` FOREIGN KEY (`tt_student_set_id`) REFERENCES `student_set` (`tt_student_set_id`),
  CONSTRAINT `enrolment_add_run` FOREIGN KEY (`run_id`) REFERENCES `synchronisation_run` (`run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `enrolment_remove`
--

DROP TABLE IF EXISTS `enrolment_remove`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enrolment_remove` (
  `change_id` int(11) NOT NULL AUTO_INCREMENT,
  `run_id` int(11) NOT NULL,
  `tt_activity_id` varchar(32) NOT NULL,
  `tt_student_set_id` varchar(32) NOT NULL,
  `update_completed` datetime DEFAULT NULL,
  PRIMARY KEY (`change_id`),
  UNIQUE KEY `run_id` (`run_id`,`tt_activity_id`,`tt_student_set_id`),
  KEY `enrolment_remove_activ` (`tt_activity_id`),
  KEY `enrolment_remove_stu` (`tt_student_set_id`),
  CONSTRAINT `enrolment_remove_activ` FOREIGN KEY (`tt_activity_id`) REFERENCES `activity` (`tt_activity_id`),
  CONSTRAINT `enrolment_remove_stu` FOREIGN KEY (`tt_student_set_id`) REFERENCES `student_set` (`tt_student_set_id`),
  CONSTRAINT `enrolment_remove_run` FOREIGN KEY (`run_id`) REFERENCES `synchronisation_run` (`run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-05-02 15:51:11
