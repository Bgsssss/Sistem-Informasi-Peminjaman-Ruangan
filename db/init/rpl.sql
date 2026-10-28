-- MySQL dump 10.13  Distrib 8.0.30, for Win64 (x86_64)
--
-- Host: localhost    Database: rpl
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `admin_id` bigint unsigned NOT NULL,
  `action` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_id` bigint unsigned NOT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_created_at` (`created_at`),
  KEY `idx_audit_entity` (`entity`,`entity_id`),
  KEY `fk_audit_admin` (`admin_id`),
  CONSTRAINT `fk_audit_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,7,'ROOM_DEACTIVATED','room',7,'Room deactivated','2026-10-22 08:10:52'),(2,7,'ROOM_DEACTIVATED','room',7,'Room deactivated','2026-10-25 03:43:05'),(3,7,'ROOM_DEACTIVATED','room',7,'Room deactivated','2026-10-25 04:14:45'),(4,7,'ROOM_DELETED','room',7,'Room test1 deleted','2026-10-25 04:14:54');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookable_dates`
--

DROP TABLE IF EXISTS `bookable_dates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookable_dates` (
  `booking_date` date NOT NULL,
  `is_open` tinyint(1) NOT NULL DEFAULT '0',
  `opened_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`booking_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookable_dates`
--

LOCK TABLES `bookable_dates` WRITE;
/*!40000 ALTER TABLE `bookable_dates` DISABLE KEYS */;
INSERT INTO `bookable_dates` VALUES ('2026-10-12',1,'2026-10-12 07:40:30'),('2026-10-13',1,'2026-10-12 07:40:30'),('2026-10-14',1,'2026-10-12 07:40:30'),('2026-10-21',1,'2026-10-21 03:55:43'),('2026-10-22',1,'2026-10-21 03:55:43'),('2026-10-23',1,'2026-10-21 03:55:43');
/*!40000 ALTER TABLE `bookable_dates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `room_id` bigint unsigned NOT NULL,
  `booking_date` date NOT NULL,
  `slot_start` time NOT NULL,
  `slot_end` time NOT NULL,
  `status` enum('ACTIVE','ATTENDED','COMPLETED','CANCELLED_BY_USER','CANCELLED_BY_SYSTEM','CANCELLED_BY_ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `checkin_time` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  `cancel_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cancelled_by_admin_id` bigint unsigned DEFAULT NULL,
  `created_date` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `last_modified_date` datetime(6) DEFAULT NULL,
  `created_by` bigint unsigned NOT NULL,
  `last_modified_by` bigint unsigned DEFAULT NULL,
  `slot_taken` tinyint GENERATED ALWAYS AS (if((`status` in (_utf8mb4'CANCELLED_BY_USER',_utf8mb4'CANCELLED_BY_SYSTEM',_utf8mb4'CANCELLED_BY_ADMIN')),NULL,1)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_booking_slot` (`room_id`,`booking_date`,`slot_start`,`slot_taken`),
  KEY `idx_booking_date_status` (`booking_date`,`status`),
  KEY `idx_booking_student` (`student_id`,`status`),
  KEY `idx_booking_autocancel` (`status`,`booking_date`,`slot_start`),
  KEY `fk_booking_admin` (`cancelled_by_admin_id`),
  CONSTRAINT `fk_booking_admin` FOREIGN KEY (`cancelled_by_admin_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_booking_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`),
  CONSTRAINT `fk_booking_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `ck_booking_admin_reason` CHECK (((`status` <> _cp850'CANCELLED_BY_ADMIN') or ((`cancel_reason` is not null) and (`cancelled_by_admin_id` is not null)))),
  CONSTRAINT `ck_booking_slot` CHECK (((`slot_start` >= _cp850'08:00:00') and (`slot_end` <= _cp850'21:00:00') and (`slot_end` = addtime(`slot_start`,_cp850'01:00:00'))))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` (`id`, `student_id`, `room_id`, `booking_date`, `slot_start`, `slot_end`, `status`, `checkin_time`, `cancelled_at`, `cancel_reason`, `cancelled_by_admin_id`, `created_date`, `last_modified_date`, `created_by`, `last_modified_by`) VALUES (1,1,1,'2026-10-13','15:00:00','16:00:00','CANCELLED_BY_USER',NULL,'2026-10-12 14:57:00',NULL,NULL,'2026-10-12 14:41:57.987854','2026-10-12 14:57:00.131633',2,2),(2,1,1,'2026-10-13','16:00:00','17:00:00','CANCELLED_BY_SYSTEM',NULL,'2026-10-19 12:25:31','No check-in within 10 minutes',NULL,'2026-10-12 14:43:35.697303','2026-10-19 12:25:32.481111',2,NULL),(3,3,3,'2026-10-12','15:00:00','16:00:00','CANCELLED_BY_SYSTEM',NULL,'2026-10-19 12:25:31','No check-in within 10 minutes',NULL,'2026-10-12 14:45:29.007135','2026-10-19 12:25:32.270372',4,NULL),(4,1,4,'2026-10-21','11:00:00','12:00:00','COMPLETED','2026-10-21 11:00:14',NULL,NULL,NULL,'2026-10-21 10:58:08.340786','2026-10-22 14:49:42.269969',2,2),(5,1,1,'2026-10-21','12:00:00','13:00:00','CANCELLED_BY_USER',NULL,'2026-10-21 11:04:00',NULL,NULL,'2026-10-21 11:01:59.525235','2026-10-21 11:04:00.460158',2,2),(6,1,4,'2026-10-21','18:00:00','19:00:00','CANCELLED_BY_USER',NULL,'2026-10-21 11:05:30',NULL,NULL,'2026-10-21 11:05:18.888477','2026-10-21 11:05:30.067778',2,2),(7,1,1,'2026-10-21','12:00:00','13:00:00','CANCELLED_BY_USER',NULL,'2026-10-21 11:07:09',NULL,NULL,'2026-10-21 11:06:47.516641','2026-10-21 11:07:09.260617',2,2),(8,1,1,'2026-10-22','16:00:00','17:00:00','COMPLETED','2026-10-22 16:05:11',NULL,NULL,NULL,'2026-10-22 15:03:45.990692','2026-10-25 10:41:47.718121',2,2),(9,1,3,'2026-10-22','20:00:00','21:00:00','CANCELLED_BY_USER',NULL,'2026-10-22 15:50:12',NULL,NULL,'2026-10-22 15:49:20.254872','2026-10-22 15:50:12.189977',2,2);
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('SMALL','LARGE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `location` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `capacity` tinyint unsigned NOT NULL,
  `facilities` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `photo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_date` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `last_modified_date` datetime(6) DEFAULT NULL,
  `created_by` bigint unsigned NOT NULL,
  `last_modified_by` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_room_name` (`name`),
  KEY `idx_room_type_active` (`type`,`is_active`),
  CONSTRAINT `ck_room_capacity` CHECK ((((`type` = _cp850'SMALL') and (`capacity` between 2 and 4)) or ((`type` = _cp850'LARGE') and (`capacity` between 4 and 8))))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (1,'R-101','SMALL','Building A, Floor 1',4,'Whiteboard, AC, power outlets','/img/r101.jpg',1,'2026-10-07 19:43:15.756017',NULL,1,NULL),(2,'R-102','SMALL','Building A, Floor 1',4,'Whiteboard, AC','/img/r102.jpg',1,'2026-10-07 19:43:15.756017',NULL,1,NULL),(3,'R-201','SMALL','Building A, Floor 2',4,'Whiteboard, AC, power outlets','/img/r201.jpg',1,'2026-10-07 19:43:15.756017',NULL,1,NULL),(4,'R-202','LARGE','Building A, Floor 2',8,'Projector, whiteboard, AC, power outlets','/img/r202.jpg',1,'2026-10-07 19:43:15.756017',NULL,1,NULL),(5,'R-203','LARGE','Building A, Floor 2',8,'Projector, whiteboard, AC','/img/r203.jpg',1,'2026-10-07 19:43:15.756017',NULL,1,NULL),(6,'R-301','LARGE','Building B, Floor 3',8,'TV screen, whiteboard, AC, power outlets','/img/r301.jpg',1,'2026-10-07 19:43:15.756017',NULL,1,NULL);
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `npm` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `faculty` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `trust_score` tinyint unsigned NOT NULL DEFAULT '100',
  `created_date` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `last_modified_date` datetime(6) DEFAULT NULL,
  `created_by` bigint unsigned NOT NULL,
  `last_modified_by` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_student_number` (`npm`),
  CONSTRAINT `ck_student_score` CHECK ((`trust_score` between 0 and 100))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES (1,'2022010001','Rani Anggraini','Engineering and Science',1,85,'2026-10-07 19:43:15.778549','2026-10-25 10:41:47.668396',1,2),(2,'2022010002','Dimas Prakoso','Engineering and Science',1,50,'2026-10-07 19:43:15.778549',NULL,1,NULL),(3,'2022020003','Sinta Maharani','Economics',1,80,'2026-10-07 19:43:15.778549','2026-10-22 15:12:09.114608',1,7),(4,'2022030004','Fajar Nugroho','Law',1,100,'2026-10-07 19:43:15.778549',NULL,1,NULL),(5,'2021010005','Lestari Wulandari','Education',0,100,'2026-10-07 19:43:15.778549',NULL,1,NULL);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trust_score_history`
--

DROP TABLE IF EXISTS `trust_score_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trust_score_history` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `booking_id` bigint unsigned DEFAULT NULL,
  `admin_id` bigint unsigned DEFAULT NULL,
  `delta` smallint NOT NULL,
  `score_after` tinyint unsigned NOT NULL,
  `type` enum('INITIAL_SCORE','NO_SHOW','LATE_CANCELLATION','COMPLETED_ON_TIME','ADMIN_ADJUSTMENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tsh_student` (`student_id`,`created_at`),
  KEY `fk_tsh_booking` (`booking_id`),
  KEY `fk_tsh_admin` (`admin_id`),
  CONSTRAINT `fk_tsh_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_tsh_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  CONSTRAINT `fk_tsh_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `ck_tsh_note` CHECK (((`type` <> _cp850'ADMIN_ADJUSTMENT') or (`note` is not null))),
  CONSTRAINT `ck_tsh_score` CHECK ((`score_after` between 0 and 100))
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trust_score_history`
--

LOCK TABLES `trust_score_history` WRITE;
/*!40000 ALTER TABLE `trust_score_history` DISABLE KEYS */;
INSERT INTO `trust_score_history` VALUES (1,5,NULL,NULL,100,100,'INITIAL_SCORE','Account created','2026-10-07 12:43:15'),(2,1,NULL,NULL,100,100,'INITIAL_SCORE','Account created','2026-10-07 12:43:15'),(3,2,NULL,NULL,100,100,'INITIAL_SCORE','Account created','2026-10-07 12:43:15'),(4,3,NULL,NULL,100,100,'INITIAL_SCORE','Account created','2026-10-07 12:43:15'),(5,4,NULL,NULL,100,100,'INITIAL_SCORE','Account created','2026-10-07 12:43:15'),(6,3,NULL,7,-15,70,'ADMIN_ADJUSTMENT','cuma test','2026-10-12 05:19:44'),(7,3,3,NULL,-15,55,'NO_SHOW','Auto-cancel: no check-in within 10 minutes','2026-10-19 05:25:32'),(8,1,2,NULL,-15,85,'NO_SHOW','Auto-cancel: no check-in within 10 minutes','2026-10-19 05:25:32'),(9,1,5,NULL,-5,80,'LATE_CANCELLATION','Cancelled less than 2 hours before the schedule','2026-10-21 04:04:00'),(10,1,7,NULL,-5,75,'LATE_CANCELLATION','Cancelled less than 2 hours before the schedule','2026-10-21 04:07:09'),(11,1,4,NULL,5,80,'COMPLETED_ON_TIME','Booking completed with on-time check-in','2026-10-22 07:49:42'),(12,3,NULL,7,25,80,'ADMIN_ADJUSTMENT','test aja','2026-10-22 08:12:09'),(13,1,8,NULL,5,85,'COMPLETED_ON_TIME','Booking completed with on-time check-in','2026-10-25 03:41:47');
/*!40000 ALTER TABLE `trust_score_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ROLE_STUDENT','ROLE_ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_id` bigint unsigned DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_username` (`username`),
  UNIQUE KEY `uq_users_student` (`student_id`),
  CONSTRAINT `fk_users_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `ck_users_role` CHECK ((((`role` = _cp850'ROLE_STUDENT') and (`student_id` is not null)) or ((`role` = _cp850'ROLE_ADMIN') and (`student_id` is null))))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,'rani@mail.test','$2a$10$Ukg9F034ntf2.ISZlz4D9eiTCCpO/FWv3pZuoMsxHV/YVFy0/bfCe','ROLE_STUDENT',1,'2026-10-07 12:43:15'),(3,'dimas@mail.test','$2a$10$1wsz0hQjvMfIOeyo41ySIuoZfKcUSkkZQo.Zsj92JFZHMKussDMea','ROLE_STUDENT',2,'2026-10-07 12:43:15'),(4,'sinta@mail.test','$2a$10$eXLfQ7XbhyZ1Jydk.bLExOYPdUnCMADT.x4Rcx6SphyVvDLqD9Rjm','ROLE_STUDENT',3,'2026-10-07 12:43:15'),(5,'fajar@mail.test','$2a$10$uRKwfoAA/PSstArrPaY/uO4C39Y5vN822908N2PoPQKGnDEvmmNLO','ROLE_STUDENT',4,'2026-10-07 12:43:15'),(6,'lestari@mail.test','$2a$10$WT/cfhbdk6AOTszBeWQcLu1eVbNEhX3x5kuyo7Li3zJnC1FD5C.Wi','ROLE_STUDENT',5,'2026-10-07 12:43:15'),(7,'admin@gmail','$2a$10$zcqwOjTTWZFaAA26AP6m2uBZTZLVi8C9uFH7zug.NZS1dA8ULU.yu','ROLE_ADMIN',NULL,'2026-10-10 12:10:02');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-28 14:30:40
