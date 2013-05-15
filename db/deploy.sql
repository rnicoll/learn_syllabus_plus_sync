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
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity` (
  `tt_activity_id` varchar(32) NOT NULL,
  `tt_activity_name` varchar(255) DEFAULT NULL,
  `tt_module_id` varchar(32) DEFAULT NULL,
  `tt_template_id` varchar(32) DEFAULT NULL,
  `tt_type_id` varchar(32) DEFAULT NULL,
  `tt_jta_activity_id` varchar(32) DEFAULT NULL,
  `learn_group_id` varchar(80) DEFAULT NULL,
  `learn_group_name` varchar(255) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`tt_activity_id`),
  KEY `tt_module_id` (`tt_module_id`),
  KEY `tt_type_id` (`tt_type_id`),
  KEY `activity_template` (`tt_template_id`),
  CONSTRAINT `activity_module` FOREIGN KEY (`tt_module_id`) REFERENCES `module` (`tt_module_id`),
  CONSTRAINT `activity_template` FOREIGN KEY (`tt_template_id`) REFERENCES `activity_template` (`tt_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `activity_set_size_vw`
--

DROP TABLE IF EXISTS `activity_set_size_vw`;
/*!50001 DROP VIEW IF EXISTS `activity_set_size_vw`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `activity_set_size_vw` (
  `tt_activity_id` varchar(32),
  `set_size` bigint(21)
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `activity_template`
--

DROP TABLE IF EXISTS `activity_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_template` (
  `tt_template_id` varchar(32) NOT NULL,
  `tt_template_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`tt_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `activity_type`
--

DROP TABLE IF EXISTS `activity_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_type` (
  `tt_type_id` varchar(32) NOT NULL,
  `tt_type_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`tt_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `added_enrolment_vw`
--

DROP TABLE IF EXISTS `added_enrolment_vw`;
/*!50001 DROP VIEW IF EXISTS `added_enrolment_vw`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `added_enrolment_vw` (
  `run_id` int(11),
  `previous_run_id` int(11),
  `tt_student_set_id` varchar(32),
  `tt_activity_id` varchar(32),
  `change_type` varchar(3)
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

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
-- Table structure for table `change_result`
--

DROP TABLE IF EXISTS `change_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `change_result` (
  `result_code` varchar(20) NOT NULL,
  `label` varchar(80) NOT NULL,
  `retry` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`result_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `change_type`
--

DROP TABLE IF EXISTS `change_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `change_type` (
  `change_type` varchar(12) NOT NULL,
  PRIMARY KEY (`change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `enrolment_change`
--

DROP TABLE IF EXISTS `enrolment_change`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `enrolment_change` (
  `change_id` int(11) NOT NULL AUTO_INCREMENT,
  `run_id` int(11) NOT NULL,
  `tt_activity_id` varchar(32) NOT NULL,
  `tt_student_set_id` varchar(32) NOT NULL,
  `change_type` varchar(12) NOT NULL,
  `result_code` varchar(20) DEFAULT NULL,
  `update_completed` datetime DEFAULT NULL,
  PRIMARY KEY (`change_id`),
  UNIQUE KEY `run_id` (`run_id`,`tt_activity_id`,`tt_student_set_id`),
  KEY `enrolment_change_run` (`run_id`),
  KEY `enrolment_change_activ` (`tt_activity_id`),
  KEY `enrolment_change_stu` (`tt_student_set_id`),
  KEY `enrolment_change_type` (`change_type`),
  KEY `enrolment_change_res` (`result_code`),
  CONSTRAINT `enrolment_change_activ` FOREIGN KEY (`tt_activity_id`) REFERENCES `activity` (`tt_activity_id`),
  CONSTRAINT `enrolment_change_res` FOREIGN KEY (`result_code`) REFERENCES `change_result` (`result_code`),
  CONSTRAINT `enrolment_change_run` FOREIGN KEY (`run_id`) REFERENCES `synchronisation_run` (`run_id`),
  CONSTRAINT `enrolment_change_stu` FOREIGN KEY (`tt_student_set_id`) REFERENCES `student_set` (`tt_student_set_id`),
  CONSTRAINT `enrolment_change_type` FOREIGN KEY (`change_type`) REFERENCES `change_type` (`change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `module`
--

DROP TABLE IF EXISTS `module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `module` (
  `tt_module_id` varchar(32) NOT NULL,
  `tt_course_code` varchar(20) DEFAULT NULL,
  `tt_module_name` varchar(255) DEFAULT NULL,
  `tt_academic_year` varchar(12) DEFAULT NULL,
  `cache_semester_code` varchar(6) DEFAULT NULL,
  `cache_occurrence_code` varchar(6) DEFAULT NULL,
  `cache_course_code` varchar(12) DEFAULT NULL,
  `merge_course_code` varchar(40) DEFAULT NULL,
  `learn_academic_year` varchar(6) DEFAULT NULL,
  `learn_course_code` varchar(40) DEFAULT NULL,
  `learn_course_id` varchar(80) DEFAULT NULL,
  `webct_active` char(1) DEFAULT NULL,
  PRIMARY KEY (`tt_module_id`),
  KEY `cache_course_code` (`cache_course_code`,`cache_semester_code`,`cache_occurrence_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `removed_enrolment_vw`
--

DROP TABLE IF EXISTS `removed_enrolment_vw`;
/*!50001 DROP VIEW IF EXISTS `removed_enrolment_vw`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `removed_enrolment_vw` (
  `run_id` int(11),
  `previous_run_id` int(11),
  `tt_student_set_id` varchar(32),
  `tt_activity_id` varchar(32),
  `change_type` varchar(6)
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

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
-- Temporary table structure for view `sync_activities_vw`
--

DROP TABLE IF EXISTS `sync_activities_vw`;
/*!50001 DROP VIEW IF EXISTS `sync_activities_vw`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `sync_activities_vw` (
  `tt_activity_id` varchar(32),
  `tt_activity_name` varchar(255),
  `tt_jta_activity_id` varchar(32),
  `learn_group_id` varchar(80),
  `description` text,
  `learn_course_code` varchar(40),
  `learn_course_id` varchar(80),
  `set_size` bigint(21)
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `sync_student_set_vw`
--

DROP TABLE IF EXISTS `sync_student_set_vw`;
/*!50001 DROP VIEW IF EXISTS `sync_student_set_vw`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE TABLE `sync_student_set_vw` (
  `tt_student_set_id` varchar(32),
  `username` varchar(32),
  `learn_person_id` varchar(80)
) ENGINE=MyISAM */;
SET character_set_client = @saved_cs_client;

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
-- Final view structure for view `activity_set_size_vw`
--

/*!50001 DROP TABLE IF EXISTS `activity_set_size_vw`*/;
/*!50001 DROP VIEW IF EXISTS `activity_set_size_vw`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `activity_set_size_vw` AS (select `a`.`tt_activity_id` AS `tt_activity_id`,count(`b`.`tt_activity_id`) AS `set_size` from ((`activity` `a` left join `activity_template` `t` on((`t`.`tt_template_id` = `a`.`tt_template_id`))) left join `activity` `b` on((`t`.`tt_template_id` = `b`.`tt_template_id`))) group by `a`.`tt_activity_id`) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `added_enrolment_vw`
--

/*!50001 DROP TABLE IF EXISTS `added_enrolment_vw`*/;
/*!50001 DROP VIEW IF EXISTS `added_enrolment_vw`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `added_enrolment_vw` AS (select `a`.`run_id` AS `run_id`,`a`.`previous_run_id` AS `previous_run_id`,`ca`.`tt_student_set_id` AS `tt_student_set_id`,`ca`.`tt_activity_id` AS `tt_activity_id`,'add' AS `change_type` from (((((`synchronisation_run` `a` join `cache_enrolment` `ca` on((`ca`.`run_id` = `a`.`run_id`))) join `sync_activities_vw` `act` on((`act`.`tt_activity_id` = `ca`.`tt_activity_id`))) join `sync_student_set_vw` `stu` on((`stu`.`tt_student_set_id` = `ca`.`tt_student_set_id`))) left join `synchronisation_run` `b` on((`b`.`run_id` = `a`.`previous_run_id`))) left join `cache_enrolment` `cb` on(((`cb`.`run_id` = `b`.`run_id`) and (`cb`.`tt_student_set_id` = `ca`.`tt_student_set_id`) and (`cb`.`tt_activity_id` = `ca`.`tt_activity_id`)))) where isnull(`cb`.`tt_student_set_id`)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `removed_enrolment_vw`
--

/*!50001 DROP TABLE IF EXISTS `removed_enrolment_vw`*/;
/*!50001 DROP VIEW IF EXISTS `removed_enrolment_vw`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `removed_enrolment_vw` AS (select `a`.`run_id` AS `run_id`,`a`.`previous_run_id` AS `previous_run_id`,`ca`.`tt_student_set_id` AS `tt_student_set_id`,`ca`.`tt_activity_id` AS `tt_activity_id`,'remove' AS `change_type` from (((((`synchronisation_run` `a` join `synchronisation_run` `b` on((`b`.`run_id` = `a`.`previous_run_id`))) join `cache_enrolment` `cb` on((`cb`.`run_id` = `b`.`run_id`))) join `sync_activities_vw` `act` on((`act`.`tt_activity_id` = `cb`.`tt_activity_id`))) join `sync_student_set_vw` `stu` on((`stu`.`tt_student_set_id` = `cb`.`tt_student_set_id`))) left join `cache_enrolment` `ca` on(((`ca`.`run_id` = `a`.`run_id`) and (`cb`.`tt_student_set_id` = `ca`.`tt_student_set_id`) and (`cb`.`tt_activity_id` = `ca`.`tt_activity_id`)))) where isnull(`ca`.`tt_student_set_id`)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `sync_activities_vw`
--

/*!50001 DROP TABLE IF EXISTS `sync_activities_vw`*/;
/*!50001 DROP VIEW IF EXISTS `sync_activities_vw`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `sync_activities_vw` AS (select `a`.`tt_activity_id` AS `tt_activity_id`,`a`.`tt_activity_name` AS `tt_activity_name`,`a`.`tt_jta_activity_id` AS `tt_jta_activity_id`,`a`.`learn_group_id` AS `learn_group_id`,`a`.`description` AS `description`,`m`.`learn_course_code` AS `learn_course_code`,`m`.`learn_course_id` AS `learn_course_id`,`s`.`set_size` AS `set_size` from ((`activity` `a` join `module` `m` on((`m`.`tt_module_id` = `a`.`tt_module_id`))) join `activity_set_size_vw` `s` on((`s`.`tt_activity_id` = `a`.`tt_activity_id`))) where ((`m`.`webct_active` = 'Y') and (`s`.`set_size` > '1'))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `sync_student_set_vw`
--

/*!50001 DROP TABLE IF EXISTS `sync_student_set_vw`*/;
/*!50001 DROP VIEW IF EXISTS `sync_student_set_vw`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `sync_student_set_vw` AS (select `s`.`tt_student_set_id` AS `tt_student_set_id`,`s`.`tt_host_key` AS `username`,`s`.`learn_person_id` AS `learn_person_id` from `student_set` `s` where (substr(`s`.`tt_host_key`,0,6) <> '#SPLUS')) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-05-15 15:17:37
