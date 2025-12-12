-- MySQL dump 10.13  Distrib 9.5.0, for macos26.1 (arm64)
--
-- Host: localhost    Database: cco_system
-- ------------------------------------------------------
-- Server version	9.5.0

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
-- Current Database: `cco_system`
--

/*!40000 DROP DATABASE IF EXISTS `cco_system`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `cco_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `cco_system`;

--
-- Table structure for table `agency_working_hours`
--

DROP TABLE IF EXISTS `agency_working_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agency_working_hours` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agency_id` int NOT NULL COMMENT '机构ID',
  `day_of_week` tinyint NOT NULL COMMENT '星期几（1-7，1=周一，7=周日）',
  `start_time` time DEFAULT NULL COMMENT '开始时间（HH:MM格式，NULL表示不工作）',
  `end_time` time DEFAULT NULL COMMENT '结束时间（HH:MM格式，NULL表示不工作）',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agency_day` (`agency_id`,`day_of_week`),
  KEY `idx_agency_id` (`agency_id`),
  KEY `idx_agency_day` (`agency_id`,`day_of_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机构作息时间表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agency_working_hours`
--

LOCK TABLES `agency_working_hours` WRITE;
/*!40000 ALTER TABLE `agency_working_hours` DISABLE KEYS */;
/*!40000 ALTER TABLE `agency_working_hours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `case_queues`
--

DROP TABLE IF EXISTS `case_queues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `case_queues` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '所属甲方ID',
  `queue_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '队列编码',
  `queue_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '队列名称',
  `queue_name_en` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '队列名称（英文）',
  `queue_description` text COLLATE utf8mb4_unicode_ci COMMENT '队列描述',
  `overdue_days_start` int DEFAULT NULL COMMENT '逾期天数起始值',
  `overdue_days_end` int DEFAULT NULL COMMENT '逾期天数结束值',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件队列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `case_queues`
--

LOCK TABLES `case_queues` WRITE;
/*!40000 ALTER TABLE `case_queues` DISABLE KEYS */;
INSERT INTO `case_queues` (`id`, `tenant_id`, `queue_code`, `queue_name`, `queue_name_en`, `queue_description`, `overdue_days_start`, `overdue_days_end`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES (1,1,'C','C队列','C Queue','未逾期，提前还款客户',NULL,-1,1,1,'2025-11-25 17:37:55','2025-11-25 17:37:55'),(2,1,'S0','S0队列','S0 Queue','当日到期，需重点关注',0,0,2,1,'2025-11-25 17:37:55','2025-11-25 17:37:55'),(3,1,'S1','S1队列','S1 Queue','轻度逾期，友好提醒',1,5,3,1,'2025-11-25 17:37:55','2025-11-25 17:37:55'),(4,1,'L1','L1队列','L1 Queue','中度逾期，加强催收',6,90,4,1,'2025-11-25 17:37:55','2025-11-25 17:37:55'),(5,1,'M1','M1队列','M1 Queue','重度逾期，专项处理',91,NULL,5,1,'2025-11-25 17:37:55','2025-11-25 17:37:55');
/*!40000 ALTER TABLE `case_queues` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `case_reassign_configs`
--

DROP TABLE IF EXISTS `case_reassign_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `case_reassign_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '所属甲方ID',
  `config_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置类型: queue/agency/team',
  `target_id` bigint NOT NULL COMMENT '目标ID（队列ID/机构ID/小组ID）',
  `team_ids` json DEFAULT NULL COMMENT '小组ID列表（JSON数组），为空表示该队列下所有小组',
  `reassign_days` int NOT NULL COMMENT '重新分案天数（整数）',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `effective_date` date DEFAULT NULL COMMENT '生效日期（T+1日）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_type_target` (`tenant_id`,`config_type`,`target_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_effective_date` (`effective_date`),
  KEY `idx_config_type` (`config_type`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_queue_team` (`target_id`,`config_type`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件重新分案配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `case_reassign_configs`
--

LOCK TABLES `case_reassign_configs` WRITE;
/*!40000 ALTER TABLE `case_reassign_configs` DISABLE KEYS */;
INSERT INTO `case_reassign_configs` (`id`, `tenant_id`, `config_type`, `target_id`, `team_ids`, `reassign_days`, `is_active`, `effective_date`, `created_at`, `updated_at`) VALUES (1,1,'queue',1,'[1]',7,1,'2025-11-30','2025-11-29 21:21:40','2025-12-04 22:37:16'),(2,1,'queue',3,NULL,3,1,'2025-11-30','2025-11-29 21:22:35','2025-11-29 21:22:35'),(3,1,'queue',2,NULL,3,1,'2025-11-30','2025-11-29 21:22:51','2025-11-29 21:22:51'),(4,1,'queue',4,NULL,3,1,'2025-11-30','2025-11-29 21:36:04','2025-11-29 21:36:04');
/*!40000 ALTER TABLE `case_reassign_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cases`
--

DROP TABLE IF EXISTS `cases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cases` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `case_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '案件唯一标识',
  `tenant_id` bigint NOT NULL COMMENT '所属甲方ID',
  `agency_id` bigint DEFAULT NULL COMMENT '所属催收机构ID',
  `team_id` bigint DEFAULT NULL COMMENT '所属催收小组ID',
  `collector_id` bigint DEFAULT NULL COMMENT '分配催员ID',
  `queue_id` bigint DEFAULT NULL COMMENT '所属队列ID',
  `user_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户编号',
  `user_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户姓名',
  `mobile` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `case_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '案件状态',
  `product_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产品名称',
  `app_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'App名称',
  `overdue_days` int DEFAULT NULL COMMENT '逾期天数（用于自动分配队列）',
  `loan_amount` decimal(15,2) DEFAULT NULL COMMENT '贷款金额',
  `repaid_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已还款金额',
  `outstanding_amount` decimal(15,2) DEFAULT NULL COMMENT '逾期金额',
  `due_date` datetime DEFAULT NULL COMMENT '到期日期',
  `settlement_date` datetime DEFAULT NULL COMMENT '结清日期',
  `assigned_at` datetime DEFAULT NULL COMMENT '分配时间',
  `last_contact_at` datetime DEFAULT NULL COMMENT '最后联系时间',
  `next_follow_up_at` datetime DEFAULT NULL COMMENT '下次跟进时间',
  `is_stay` tinyint(1) DEFAULT '0' COMMENT '是否停留（独立状态字段，与case_status分离）',
  `stay_at` datetime DEFAULT NULL COMMENT '停留时间',
  `stay_by` bigint DEFAULT NULL COMMENT '停留操作人ID',
  `stay_released_at` datetime DEFAULT NULL COMMENT '解放停留时间',
  `stay_released_by` bigint DEFAULT NULL COMMENT '解放停留操作人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `case_code` (`case_code`),
  UNIQUE KEY `uk_case_code` (`case_code`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_agency_id` (`agency_id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_collector_id` (`collector_id`),
  KEY `idx_queue_id` (`queue_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_case_status` (`case_status`),
  KEY `idx_overdue_days` (`overdue_days`),
  KEY `idx_due_date` (`due_date`),
  KEY `idx_assigned_at` (`assigned_at`),
  KEY `idx_is_stay` (`is_stay`),
  KEY `idx_stay_at` (`stay_at`),
  KEY `idx_stay_by` (`stay_by`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cases`
--

LOCK TABLES `cases` WRITE;
/*!40000 ALTER TABLE `cases` DISABLE KEYS */;
INSERT INTO `cases` (`id`, `case_code`, `tenant_id`, `agency_id`, `team_id`, `collector_id`, `queue_id`, `user_id`, `user_name`, `mobile`, `case_status`, `product_name`, `app_name`, `overdue_days`, `loan_amount`, `repaid_amount`, `outstanding_amount`, `due_date`, `settlement_date`, `assigned_at`, `last_contact_at`, `next_follow_up_at`, `is_stay`, `stay_at`, `stay_by`, `stay_released_at`, `stay_released_by`, `created_at`, `updated_at`) VALUES (11,'BTQ-202411-001',1,NULL,NULL,1,NULL,'CUST2024001','李明','13812345678','pending_repayment','Personal Loan','FlashCash',15,15000.00,0.00,15000.00,'2025-11-10 20:44:22',NULL,NULL,NULL,NULL,1,'2025-11-28 16:43:48',1,NULL,NULL,'2025-10-11 20:44:22','2025-11-28 16:43:48'),(12,'BTQ-202411-002',1,NULL,NULL,1,NULL,'CUST2024002','王芳','13923456789','pending_repayment','Payday Loan','QuickMoney',28,25000.00,0.00,25000.00,'2025-10-28 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-09-28 20:44:22','2025-11-27 09:39:45'),(13,'BTQ-202411-003',1,NULL,NULL,1,NULL,'CUST2024003','张伟','13734567890','pending_repayment','Installment Loan','PayEasy',42,18000.00,0.00,18000.00,'2025-10-14 20:44:22',NULL,NULL,NULL,NULL,1,'2025-11-28 16:40:28',1,NULL,NULL,'2025-09-14 20:44:22','2025-11-28 16:40:28'),(14,'BTQ-202411-004',1,NULL,NULL,1,NULL,'CUST2024004','刘静','13645678901','pending_repayment','Quick Loan','LoanPro',7,12000.00,0.00,12000.00,'2025-11-18 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-19 20:44:22','2025-11-27 09:39:45'),(15,'BTQ-202411-005',1,NULL,NULL,1,NULL,'CUST2024005','陈强','13556789012','pending_repayment','Cash Loan','MegaPeso',35,30000.00,0.00,30000.00,'2025-10-21 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-09-21 20:44:22','2025-11-27 09:39:45'),(16,'BTQ-202411-006',1,NULL,NULL,1,NULL,'CUST2024006','杨丽','13467890123','partial_repayment','Personal Loan','FlashCash',20,20000.00,8000.00,12000.00,'2025-11-05 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-06 20:44:22','2025-11-27 09:39:45'),(17,'BTQ-202411-007',1,NULL,NULL,1,NULL,'CUST2024007','赵军','13378901234','partial_repayment','Payday Loan','QuickMoney',12,16000.00,5000.00,11000.00,'2025-11-13 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-14 20:44:22','2025-11-27 09:39:45'),(18,'BTQ-202411-008',1,NULL,NULL,1,NULL,'CUST2024008','孙敏','13289012345','partial_repayment','Installment Loan','PayEasy',25,22000.00,10000.00,12000.00,'2025-10-31 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-01 20:44:22','2025-11-27 09:39:45'),(19,'BTQ-202411-009',1,NULL,NULL,1,NULL,'CUST2024009','周涛','13190123456','partial_repayment','Quick Loan','LoanPro',18,14000.00,6000.00,8000.00,'2025-11-07 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-08 20:44:22','2025-11-27 09:39:45'),(20,'BTQ-202411-010',1,NULL,NULL,1,NULL,'CUST2024010','吴艳','13001234567','partial_repayment','Cash Loan','MegaPeso',30,28000.00,12000.00,16000.00,'2025-10-26 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-09-26 20:44:22','2025-11-27 09:39:45'),(21,'BTQ-202410-001',1,NULL,NULL,1,NULL,'CUST2024001','李明','13812345678','normal_settlement','Personal Loan','FlashCash',0,10000.00,10000.00,0.00,'2025-09-26 20:44:22','2025-10-01 20:44:22',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-08-27 20:44:22','2025-11-27 09:39:45'),(22,'BTQ-202410-002',1,NULL,NULL,1,NULL,'CUST2024011','郑华','13912345678','normal_settlement','Payday Loan','QuickMoney',0,15000.00,15000.00,0.00,'2025-10-06 20:44:22','2025-10-08 20:44:22',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-09-06 20:44:22','2025-11-27 09:39:45'),(23,'BTQ-202410-003',1,NULL,NULL,1,NULL,'CUST2024012','冯磊','13712345678','normal_settlement','Installment Loan','PayEasy',0,12000.00,12000.00,0.00,'2025-10-16 20:44:22','2025-10-18 20:44:22',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-09-16 20:44:22','2025-11-27 09:39:45'),(24,'BTQ-202409-001',1,NULL,NULL,1,NULL,'CUST2024013','韩雪','13612345678','extension_settlement','Quick Loan','LoanPro',0,18000.00,18000.00,0.00,'2025-09-06 20:44:22','2025-09-16 20:44:22',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-08-07 20:44:22','2025-11-27 09:39:45'),(25,'BTQ-202409-002',1,NULL,NULL,1,NULL,'CUST2024014','朱明','13512345678','extension_settlement','Cash Loan','MegaPeso',0,20000.00,20000.00,0.00,'2025-09-11 20:44:22','2025-09-21 20:44:22',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-08-12 20:44:22','2025-11-27 09:39:45'),(26,'BTQ-202411-011',1,NULL,NULL,1,NULL,'CUST2024015','马超','13412345678','pending_repayment','Personal Loan','FlashCash',3,8000.00,0.00,8000.00,'2025-11-22 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-23 20:44:22','2025-11-27 09:39:45'),(27,'BTQ-202411-012',1,NULL,NULL,1,NULL,'CUST2024016','林芳','13312345678','pending_repayment','Payday Loan','QuickMoney',5,10000.00,0.00,10000.00,'2025-11-20 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-21 20:44:22','2025-11-27 09:39:45'),(28,'BTQ-202411-013',1,NULL,NULL,1,NULL,'CUST2024017','黄伟','13212345678','pending_repayment','Installment Loan','PayEasy',1,9000.00,0.00,9000.00,'2025-11-24 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-25 20:44:22','2025-11-27 09:39:45'),(29,'BTQ-202409-003',1,NULL,NULL,1,NULL,'CUST2024018','徐静','13112345678','pending_repayment','Quick Loan','LoanPro',65,35000.00,0.00,35000.00,'2025-09-21 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-08-22 20:44:22','2025-11-27 09:39:45'),(30,'BTQ-202409-004',1,NULL,NULL,1,NULL,'CUST2024019','高强','13012345678','pending_repayment','Cash Loan','MegaPeso',72,40000.00,0.00,40000.00,'2025-09-14 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-08-15 20:44:22','2025-11-27 09:39:45'),(31,'BTQ-202411-014',1,NULL,NULL,1,NULL,'CUST2024020','罗敏','13923456780','partial_repayment','Personal Loan','FlashCash',22,50000.00,20000.00,30000.00,'2025-11-03 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-04 20:44:22','2025-11-27 09:39:45'),(32,'BTQ-202411-015',1,NULL,NULL,1,NULL,'CUST2024021','唐军','13823456781','partial_repayment','Payday Loan','QuickMoney',15,45000.00,15000.00,30000.00,'2025-11-10 20:44:22',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2025-10-11 20:44:22','2025-11-27 09:39:45');
/*!40000 ALTER TABLE `cases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collection_teams`
--

DROP TABLE IF EXISTS `collection_teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collection_teams` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT '所属甲方ID',
  `agency_id` bigint NOT NULL COMMENT '所属催收机构ID',
  `team_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '小组编码',
  `team_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '小组名称',
  `team_name_en` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小组名称（英文）',
  `team_leader_id` bigint DEFAULT NULL COMMENT '组长ID（催员ID）',
  `team_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '小组类型（如：电催组、外访组、法务组等）',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '小组描述',
  `max_case_count` int DEFAULT NULL COMMENT '最大案件数量（0表示不限制）',
  `sort_order` int DEFAULT NULL COMMENT '排序顺序',
  `is_active` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `created_at` datetime DEFAULT (now()) COMMENT '创建时间',
  `updated_at` datetime DEFAULT (now()) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `ix_collection_teams_tenant_id` (`tenant_id`),
  KEY `ix_collection_teams_id` (`id`),
  KEY `ix_collection_teams_agency_id` (`agency_id`),
  KEY `ix_collection_teams_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collection_teams`
--

LOCK TABLES `collection_teams` WRITE;
/*!40000 ALTER TABLE `collection_teams` DISABLE KEYS */;
/*!40000 ALTER TABLE `collection_teams` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collectors`
--

DROP TABLE IF EXISTS `collectors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collectors` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT '所属甲方ID',
  `agency_id` bigint NOT NULL COMMENT '所属机构ID',
  `team_id` bigint NOT NULL COMMENT '所属小组ID',
  `collector_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '催员编码',
  `collector_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '催员姓名',
  `login_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录ID',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希',
  `mobile` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `employee_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工号',
  `collector_level` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '催员等级（初级/中级/高级/资深）',
  `max_case_count` int DEFAULT NULL COMMENT '最大案件数量',
  `current_case_count` int DEFAULT NULL COMMENT '当前案件数量',
  `specialties` json DEFAULT NULL COMMENT '擅长领域（JSON数组，如：[''高额案件'',''法务处理'']）',
  `performance_score` decimal(5,2) DEFAULT NULL COMMENT '绩效评分',
  `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态：active/休假/离职等',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `is_active` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime DEFAULT (now()) COMMENT '创建时间',
  `updated_at` datetime DEFAULT (now()) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `login_id` (`login_id`),
  KEY `ix_collectors_tenant_id` (`tenant_id`),
  KEY `ix_collectors_collector_code` (`collector_code`),
  KEY `ix_collectors_status` (`status`),
  KEY `ix_collectors_agency_id` (`agency_id`),
  KEY `ix_collectors_is_active` (`is_active`),
  KEY `ix_collectors_team_id` (`team_id`),
  KEY `ix_collectors_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collectors`
--

LOCK TABLES `collectors` WRITE;
/*!40000 ALTER TABLE `collectors` DISABLE KEYS */;
/*!40000 ALTER TABLE `collectors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `field_groups`
--

DROP TABLE IF EXISTS `field_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `field_groups` (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组标识',
  `group_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组名称（中文）',
  `group_name_en` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分组名称（英文）',
  `parent_id` int DEFAULT NULL COMMENT '父分组ID',
  `sort_order` int DEFAULT NULL COMMENT '排序顺序',
  `is_active` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `created_at` datetime DEFAULT (now()),
  `updated_at` datetime DEFAULT (now()),
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_key` (`group_key`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `field_groups_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `field_groups` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `field_groups`
--

LOCK TABLES `field_groups` WRITE;
/*!40000 ALTER TABLE `field_groups` DISABLE KEYS */;
INSERT INTO `field_groups` (`id`, `group_key`, `group_name`, `group_name_en`, `parent_id`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES (1,'customer_basic','客户基础信息','Customer Basic Information',NULL,1,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(2,'loan_details','贷款详情','Loan Details',NULL,2,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(3,'borrowing_records','借款记录','Borrowing Records',NULL,3,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(4,'repayment_records','还款记录','Repayment Records',NULL,4,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(5,'installment_details','分期详情','Installment Details',NULL,5,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(11,'identity_info','基础身份信息','Identity Information',1,1,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(12,'education','教育信息','Education',1,2,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(13,'employment','职业信息','Employment',1,3,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(14,'user_behavior','用户行为与信用','User Behavior & Credit',1,4,1,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(15,'contact_info','联系方式','Contact Information',1,5,1,'2025-12-05 18:11:42','2025-12-05 18:11:42');
/*!40000 ALTER TABLE `field_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_configs`
--

DROP TABLE IF EXISTS `notification_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint DEFAULT NULL COMMENT '甲方ID（NULL表示全局配置）',
  `notification_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型：unreplied/nudge/case_update/performance/timeout',
  `is_enabled` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `config_data` json NOT NULL COMMENT '配置数据（JSON格式）',
  `created_at` datetime DEFAULT (now()) COMMENT '创建时间',
  `updated_at` datetime DEFAULT (now()) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `ix_notification_configs_notification_type` (`notification_type`),
  KEY `ix_notification_configs_tenant_id` (`tenant_id`),
  KEY `ix_notification_configs_id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_configs`
--

LOCK TABLES `notification_configs` WRITE;
/*!40000 ALTER TABLE `notification_configs` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_templates`
--

DROP TABLE IF EXISTS `notification_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_templates` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int DEFAULT NULL COMMENT '甲方ID（NULL表示全局模板）',
  `template_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板ID（唯一标识）',
  `template_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `template_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板类型：case_tag_change/case_payment/user_app_visit/user_payment_page_visit等',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '模板描述',
  `content_template` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知正文模板，支持变量如：{case_id}、{amount}、{tag_name}等',
  `jump_url_template` text COLLATE utf8mb4_unicode_ci COMMENT '点击后跳转的URL模板，支持变量',
  `target_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送对象类型：agency/team/collector',
  `target_agencies` json DEFAULT NULL COMMENT '目标机构ID列表（JSON数组）',
  `target_teams` json DEFAULT NULL COMMENT '目标小组ID列表（JSON数组）',
  `target_collectors` json DEFAULT NULL COMMENT '目标催员ID列表（JSON数组）',
  `is_forced_read` tinyint(1) DEFAULT NULL COMMENT '是否强制阅读',
  `repeat_interval_minutes` int DEFAULT NULL COMMENT '非强制阅读时的重复提醒间隔（分钟）',
  `max_remind_count` int DEFAULT NULL COMMENT '非强制阅读时的最大提醒次数',
  `notify_time_start` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知时间范围开始（HH:MM）',
  `notify_time_end` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知时间范围结束（HH:MM）',
  `priority` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优先级：high/medium/low',
  `display_duration_seconds` int DEFAULT NULL COMMENT '展示时长（秒）',
  `is_enabled` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `available_variables` json DEFAULT NULL COMMENT '可用变量列表及说明（JSON）',
  `total_sent` int DEFAULT NULL COMMENT '累计发送次数',
  `total_read` int DEFAULT NULL COMMENT '累计阅读次数',
  `created_at` datetime DEFAULT (now()) COMMENT '创建时间',
  `updated_at` datetime DEFAULT (now()) COMMENT '更新时间',
  `created_by` int DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ix_notification_templates_template_id` (`template_id`),
  KEY `ix_notification_templates_template_type` (`template_type`),
  KEY `ix_notification_templates_id` (`id`),
  KEY `ix_notification_templates_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_templates`
--

LOCK TABLES `notification_templates` WRITE;
/*!40000 ALTER TABLE `notification_templates` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_templates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_channels`
--

DROP TABLE IF EXISTS `payment_channels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_channels` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `party_id` bigint NOT NULL COMMENT '甲方ID',
  `channel_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付名称',
  `channel_icon` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标URL',
  `channel_type` enum('VA','H5','QR') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付类型：VA-虚拟账户，H5-H5链接，QR-二维码',
  `service_provider` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务公司',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '渠道描述',
  `api_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API地址',
  `api_method` enum('GET','POST') COLLATE utf8mb4_unicode_ci DEFAULT 'POST' COMMENT '请求方法',
  `auth_type` enum('API_KEY','BEARER','BASIC') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '认证方式',
  `auth_config` json DEFAULT NULL COMMENT '认证配置（加密存储）',
  `request_params` json DEFAULT NULL COMMENT '接口入参模板',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
  `sort_order` int DEFAULT '0' COMMENT '排序权重，越小越靠前',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_party_enabled` (`party_id`,`is_enabled`,`sort_order`),
  KEY `idx_party_id` (`party_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='还款渠道配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_channels`
--

LOCK TABLES `payment_channels` WRITE;
/*!40000 ALTER TABLE `payment_channels` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment_channels` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `public_notifications`
--

DROP TABLE IF EXISTS `public_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `public_notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int DEFAULT NULL COMMENT '甲方ID（NULL表示全局通知）',
  `agency_id` int DEFAULT NULL COMMENT '机构ID（NULL表示甲方级别通知）',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知正文内容',
  `h5_content` text COLLATE utf8mb4_unicode_ci COMMENT 'H5链接地址（可选）',
  `carousel_interval_seconds` int DEFAULT NULL COMMENT '轮播间隔（秒）',
  `is_forced_read` tinyint(1) DEFAULT NULL COMMENT '是否强制阅读',
  `is_enabled` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `repeat_interval_minutes` int DEFAULT NULL COMMENT '重复提醒时间间隔（分钟）',
  `max_remind_count` int DEFAULT NULL COMMENT '最大提醒次数',
  `notify_time_start` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知时间范围开始（HH:MM）',
  `notify_time_end` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知时间范围结束（HH:MM）',
  `effective_start_time` datetime DEFAULT NULL COMMENT '生效开始时间',
  `effective_end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
  `notify_roles` text COLLATE utf8mb4_unicode_ci COMMENT '通知对象角色列表（JSON字符串）',
  `sort_order` int DEFAULT NULL COMMENT '排序顺序',
  `created_at` datetime DEFAULT (now()) COMMENT '创建时间',
  `updated_at` datetime DEFAULT (now()) COMMENT '更新时间',
  `created_by` int DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `ix_public_notifications_agency_id` (`agency_id`),
  KEY `ix_public_notifications_id` (`id`),
  KEY `ix_public_notifications_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `public_notifications`
--

LOCK TABLES `public_notifications` WRITE;
/*!40000 ALTER TABLE `public_notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `public_notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `standard_fields`
--

DROP TABLE IF EXISTS `standard_fields`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `standard_fields` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `field_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段唯一标识',
  `field_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段名称（中文）',
  `field_name_en` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字段名称（英文）',
  `field_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段类型',
  `field_group_id` bigint NOT NULL COMMENT '所属分组ID',
  `is_required` tinyint(1) DEFAULT '0' COMMENT '是否必填',
  `is_extended` tinyint(1) DEFAULT '0' COMMENT '是否为拓展字段',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '字段说明',
  `example_value` text COLLATE utf8mb4_unicode_ci COMMENT '示例值',
  `validation_rules` json DEFAULT NULL COMMENT '验证规则（JSON格式）',
  `enum_options` json DEFAULT NULL COMMENT '枚举选项（如果是Enum类型）',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '软删除标记',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `field_key` (`field_key`),
  KEY `idx_field_key` (`field_key`),
  KEY `idx_field_group_id` (`field_group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标准字段定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard_fields`
--

LOCK TABLES `standard_fields` WRITE;
/*!40000 ALTER TABLE `standard_fields` DISABLE KEYS */;
INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `validation_rules`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES (1,'user_id','用户编号','user_id','String',11,1,0,'系统内部唯一用户标识','5983',NULL,NULL,1,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(2,'user_name','用户姓名','user_name','String',11,1,0,'借款人姓名','Juan Dela Cruz',NULL,NULL,2,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(3,'gender','性别','gender','Enum',11,0,0,'用户性别','Male / Female',NULL,'[{\"tenant_id\": \"male\", \"standard_id\": \"male\", \"tenant_name\": \"Male\", \"standard_name\": \"Male\"}, {\"tenant_id\": \"female\", \"standard_id\": \"female\", \"tenant_name\": \"Female\", \"standard_name\": \"Female\"}]',3,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(4,'birth_date','出生日期','birth_date','Date',11,0,0,'用户生日','1980/5/5',NULL,NULL,4,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(5,'nationality','国籍','nationality','String',11,0,1,'国籍或居住国家','Philippines',NULL,NULL,5,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(6,'marital_status','婚姻状况','marital_status','Enum',11,0,0,'婚姻状态','Single / Married',NULL,'[{\"tenant_id\": \"single\", \"standard_id\": \"single\", \"tenant_name\": \"Single\", \"standard_name\": \"Single\"}, {\"tenant_id\": \"married\", \"standard_id\": \"married\", \"tenant_name\": \"Married\", \"standard_name\": \"Married\"}]',6,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(7,'id_type','证件类型','id_type','Enum',11,0,0,'身份证件类型','National ID / Passport / Driver’s License',NULL,'[{\"tenant_id\": \"national_id\", \"standard_id\": \"national_id\", \"tenant_name\": \"National ID\", \"standard_name\": \"National ID\"}, {\"tenant_id\": \"passport\", \"standard_id\": \"passport\", \"tenant_name\": \"Passport\", \"standard_name\": \"Passport\"}, {\"tenant_id\": \"driver’s_license\", \"standard_id\": \"driver’s_license\", \"tenant_name\": \"Driver’s License\", \"standard_name\": \"Driver’s License\"}]',7,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(8,'id_number','证件号码','id_number','String',11,0,0,'证件号码','N2319594759',NULL,NULL,8,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(9,'address','居住地址','address','String',11,0,0,'包含街道、城市、省份、邮编','123 Main St, Quezon City',NULL,NULL,9,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(10,'years_at_address','居住年限','years_at_address','Integer',11,0,0,'当前居住地年数','5',NULL,NULL,10,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(11,'housing_type','居住类型','housing_type','Enum',11,0,0,'居住情况','Own / Rent / With Family',NULL,'[{\"tenant_id\": \"own\", \"standard_id\": \"own\", \"tenant_name\": \"Own\", \"standard_name\": \"Own\"}, {\"tenant_id\": \"rent\", \"standard_id\": \"rent\", \"tenant_name\": \"Rent\", \"standard_name\": \"Rent\"}, {\"tenant_id\": \"with_family\", \"standard_id\": \"with_family\", \"tenant_name\": \"With Family\", \"standard_name\": \"With Family\"}]',11,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(12,'mobile_number','手机号码','mobile_number','String',15,0,0,'用户注册手机号','+63 9123456789',NULL,NULL,12,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(13,'company_phone','公司联系电话','company_phone','String',15,0,0,'公司电话','+63 2 9123456',NULL,NULL,13,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(14,'education_level','教育程度','education_level','Enum',12,0,0,'最高学历','University / High School',NULL,'[{\"tenant_id\": \"university\", \"standard_id\": \"university\", \"tenant_name\": \"University\", \"standard_name\": \"University\"}, {\"tenant_id\": \"high_school\", \"standard_id\": \"high_school\", \"tenant_name\": \"High School\", \"standard_name\": \"High School\"}]',14,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(15,'school_name','学校名称','school_name','String',12,0,0,'毕业学校或当前学校','University of the Philippines',NULL,NULL,15,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(16,'major','专业','major','String',12,0,0,'学习或毕业专业','Business Administration',NULL,NULL,16,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(17,'employment_status','工作状态','employment_status','Enum',13,0,0,'当前职业状态','Employed / Self-employed / Student / Unemployed',NULL,'[{\"tenant_id\": \"employed\", \"standard_id\": \"employed\", \"tenant_name\": \"Employed\", \"standard_name\": \"Employed\"}, {\"tenant_id\": \"self-employed\", \"standard_id\": \"self-employed\", \"tenant_name\": \"Self-employed\", \"standard_name\": \"Self-employed\"}, {\"tenant_id\": \"student\", \"standard_id\": \"student\", \"tenant_name\": \"Student\", \"standard_name\": \"Student\"}, {\"tenant_id\": \"unemployed\", \"standard_id\": \"unemployed\", \"tenant_name\": \"Unemployed\", \"standard_name\": \"Unemployed\"}]',17,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(18,'company_name','公司名称','company_name','String',13,0,0,'所在公司','ABC Transport',NULL,NULL,18,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(19,'job_title','职位','job_title','String',13,0,0,'当前职位或职称','Driver',NULL,NULL,19,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(20,'industry','行业类别','industry','String',13,0,0,'所属行业','Transportation',NULL,NULL,20,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(21,'years_of_employment','工作年限','years_of_employment','Integer',13,0,0,'当前岗位工作时长','3',NULL,NULL,21,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(22,'work_address','工作地址','work_address','String',13,0,0,'工作地点','15 Pasay Ave, Manila',NULL,NULL,22,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(23,'income_type','收入类型','income_type','Enum',13,0,0,'工资发放周期','Monthly / Weekly / Daily',NULL,'[{\"tenant_id\": \"monthly\", \"standard_id\": \"monthly\", \"tenant_name\": \"Monthly\", \"standard_name\": \"Monthly\"}, {\"tenant_id\": \"weekly\", \"standard_id\": \"weekly\", \"tenant_name\": \"Weekly\", \"standard_name\": \"Weekly\"}, {\"tenant_id\": \"daily\", \"standard_id\": \"daily\", \"tenant_name\": \"Daily\", \"standard_name\": \"Daily\"}]',23,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(24,'payday','发薪日','payday','String',13,0,0,'发薪时间','15th / End of Month',NULL,NULL,24,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(25,'income_range','收入','income_range','String',13,0,0,'每月或每周收入区间','32000 PHP – 64000 PHP',NULL,NULL,25,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(26,'income_source','收入来源','income_source','Enum',13,0,0,'主或副收入来源','Primary / Secondary',NULL,'[{\"tenant_id\": \"primary\", \"standard_id\": \"primary\", \"tenant_name\": \"Primary\", \"standard_name\": \"Primary\"}, {\"tenant_id\": \"secondary\", \"standard_id\": \"secondary\", \"tenant_name\": \"Secondary\", \"standard_name\": \"Secondary\"}]',26,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(27,'income_proof_files','收入证明文件','income_proof_files','FileList',13,0,1,'上传的收入凭证文件','payslip.pdf / bank_statement.jpg',NULL,NULL,27,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(28,'last_app_open_time','最近打开时间','last_app_open_time','Datetime',14,0,0,'用户最近一次打开借款App的时间','2025/10/23 12:02:21',NULL,NULL,28,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(29,'last_repayment_page_visit_time','最近访问还款页时间','last_repayment_page_visit_time','Datetime',14,0,0,'用户最近访问还款页面的时间','2025/10/26 02:01:51',NULL,NULL,29,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(30,'total_loan_count','历史借款总笔数','total_loan_count','Integer',14,0,0,'统计借款人累计放款次数','5',NULL,NULL,30,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(31,'cleared_loan_count','已结清笔数','cleared_loan_count','Integer',14,0,0,'已全额还清的贷款订单数量','3',NULL,NULL,31,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(32,'overdue_loan_count','历史逾期笔数','overdue_loan_count','Integer',14,0,0,'曾经发生逾期的订单数量','2',NULL,NULL,32,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(33,'max_overdue_days','历史最大逾期天数','max_overdue_days','Integer',14,0,0,'用户历史上最长一次逾期天数','15',NULL,NULL,33,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(34,'avg_loan_amount','平均借款金额','avg_loan_amount','Decimal',14,0,0,'用户历次贷款的平均放款金额','1500',NULL,NULL,34,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(35,'credit_score_001','001信用评分','credit_score_001','Enum',14,0,0,'系统1计算的信用等级','A',NULL,'[{\"tenant_id\": \"a\", \"standard_id\": \"a\", \"tenant_name\": \"A\", \"standard_name\": \"A\"}]',35,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(36,'credit_score_002','002信用评分','credit_score_002','Enum',14,0,0,'系统2计算的信用等级','B',NULL,'[{\"tenant_id\": \"b\", \"standard_id\": \"b\", \"tenant_name\": \"B\", \"standard_name\": \"B\"}]',36,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(37,'credit_score_003','003信用评分','credit_score_003','Enum',14,0,0,'系统3计算的信用等级','C',NULL,'[{\"tenant_id\": \"c\", \"standard_id\": \"c\", \"tenant_name\": \"C\", \"standard_name\": \"C\"}]',37,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(38,'loan_id','贷款编号','loan_id','String',2,1,0,'系统生成的唯一标识','123',NULL,NULL,1,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(39,'case_status','案件状态','case_status','Enum',2,0,0,'当前借款订单状态','未结清 / 逾期 / 结清',NULL,'[{\"tenant_id\": \"未结清\", \"standard_id\": \"未结清\", \"tenant_name\": \"未结清\", \"standard_name\": \"未结清\"}, {\"tenant_id\": \"逾期\", \"standard_id\": \"逾期\", \"tenant_name\": \"逾期\", \"standard_name\": \"逾期\"}, {\"tenant_id\": \"结清\", \"standard_id\": \"结清\", \"tenant_name\": \"结清\", \"standard_name\": \"结清\"}]',2,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(40,'product_type','产品类别','product_type','String',2,0,0,'区分借款类型（借款 / 展期等）','借款订单',NULL,NULL,3,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(41,'disbursement_time','放款时间','disbursement_time','Datetime',2,0,0,'实际放款到账时间','2025/2/26 01:00:00',NULL,NULL,4,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(42,'total_due_amount','应还金额','total_due_amount','Decimal',2,0,0,'合同约定应还总额','1460',NULL,NULL,5,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(43,'total_paid_amount','已还金额','total_paid_amount','Decimal',2,0,0,'用户已偿还金额','975',NULL,NULL,6,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(44,'outstanding_amount','应还未还金额','outstanding_amount','Decimal',2,0,0,'当前未偿还金额','485',NULL,NULL,7,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(45,'principal_due','应收本金','principal_due','Decimal',2,0,0,'合同本金部分','1000',NULL,NULL,8,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(46,'interest_due','应收利息','interest_due','Decimal',2,0,0,'合同利息部分','180',NULL,NULL,9,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(47,'service_fee','服务费','service_fee','Decimal',2,0,0,'手续费或管理费','270',NULL,NULL,10,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(48,'penalty_fee','应收罚息','penalty_fee','Decimal',2,0,0,'逾期罚息','10',NULL,NULL,11,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(49,'account_number','代扣账号','account_number','String',2,0,0,'用户绑定还款账号','98546121',NULL,NULL,12,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(50,'bank_name','开户行','bank_name','String',2,0,0,'代扣银行或电子钱包名称','GCash',NULL,NULL,13,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(51,'loan_app','借款App','loan_app','String',2,0,0,'借款来源平台','MegaPeso',NULL,NULL,14,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(52,'contract_no','合同编号','contract_no','String',2,0,1,'与外部合同匹配的编号','MP20250226001',NULL,NULL,15,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(53,'disbursement_channel','放款渠道','disbursement_channel','String',2,0,1,'资金来源渠道','Partner Bank',NULL,NULL,16,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(54,'collection_entry_time','入催时间','collection_entry_time','Datetime',2,0,1,'进入催收系统的时间','2025/3/1 00:00:00',NULL,NULL,17,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(55,'overdue_days','当前逾期天数','overdue_days','Integer',2,0,1,'系统自动计算','3',NULL,NULL,18,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(56,'next_collection_time','下次催收时间','next_collection_time','Datetime',2,0,1,'系统生成或人工设定','2025/3/5 09:00:00',NULL,NULL,19,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(57,'collection_status','催收状态','collection_status','Enum',2,0,1,'当前催收进度','未联系 / 承诺还款 / 已结清',NULL,'[{\"tenant_id\": \"未联系\", \"standard_id\": \"未联系\", \"tenant_name\": \"未联系\", \"standard_name\": \"未联系\"}, {\"tenant_id\": \"承诺还款\", \"standard_id\": \"承诺还款\", \"tenant_name\": \"承诺还款\", \"standard_name\": \"承诺还款\"}, {\"tenant_id\": \"已结清\", \"standard_id\": \"已结清\", \"tenant_name\": \"已结清\", \"standard_name\": \"已结清\"}]',20,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(58,'collection_stage','催收阶段','collection_stage','Enum',2,0,1,'按逾期天数划分','早期 / 中期 / 后期',NULL,'[{\"tenant_id\": \"早期\", \"standard_id\": \"早期\", \"tenant_name\": \"早期\", \"standard_name\": \"早期\"}, {\"tenant_id\": \"中期\", \"standard_id\": \"中期\", \"tenant_name\": \"中期\", \"standard_name\": \"中期\"}, {\"tenant_id\": \"后期\", \"standard_id\": \"后期\", \"tenant_name\": \"后期\", \"standard_name\": \"后期\"}]',21,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(59,'repayment_method','还款方式','repayment_method','Enum',2,0,1,'当前订单的还款途径','银行转账 / 钱包 / 门店',NULL,'[{\"tenant_id\": \"银行转账\", \"standard_id\": \"银行转账\", \"tenant_name\": \"银行转账\", \"standard_name\": \"银行转账\"}, {\"tenant_id\": \"钱包\", \"standard_id\": \"钱包\", \"tenant_name\": \"钱包\", \"standard_name\": \"钱包\"}, {\"tenant_id\": \"门店\", \"standard_id\": \"门店\", \"tenant_name\": \"门店\", \"standard_name\": \"门店\"}]',22,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(60,'last_payment_date','最近还款日期','last_payment_date','Datetime',2,0,1,'上一次部分或全部还款时间','2025/2/28 14:33:22',NULL,NULL,23,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(61,'borrowing_loan_id','贷款编号','borrowing_loan_id','String',3,1,0,'系统内唯一的贷款订单编号','100023',NULL,NULL,1,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(62,'borrowing_user_id','用户编号','borrowing_user_id','String',3,1,0,'用户唯一标识','5983',NULL,NULL,2,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(63,'borrowing_user_name','用户姓名','borrowing_user_name','String',3,0,0,'借款人姓名','Juan Dela Cruz',NULL,NULL,3,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(64,'borrowing_mobile_number','手机号码','borrowing_mobile_number','String',3,0,0,'用户注册手机号','+63 9123456789',NULL,NULL,4,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(65,'app_name','App名称','app_name','String',3,0,0,'借款App来源','MegaPeso',NULL,NULL,5,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(66,'product_name','产品名称','product_name','String',3,0,0,'所属产品类型','Cash Loan',NULL,NULL,6,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(67,'merchant_name','贷超商户','merchant_name','String',3,0,1,'放款商户或渠道方名称','EasyLoan Partner',NULL,NULL,7,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(68,'system_name','所属系统','system_name','String',3,0,1,'当前数据所属业务系统','CollectionSystemV2',NULL,NULL,8,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(69,'app_download_url','App下载链接','app_download_url','String',3,0,1,'便于催员发送下载提醒','https://play.google.com/...',NULL,NULL,9,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(70,'collection_type','首复催类型','collection_type','Enum',3,0,0,'标记案件是否为首次或再次催收','首催 / 复催',NULL,'[{\"tenant_id\": \"首催\", \"standard_id\": \"首催\", \"tenant_name\": \"首催\", \"standard_name\": \"首催\"}, {\"tenant_id\": \"复催\", \"standard_id\": \"复催\", \"tenant_name\": \"复催\", \"standard_name\": \"复催\"}]',10,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(71,'reborrow_flag','是否复借','reborrow_flag','Boolean',3,0,1,'用户是否为老客再次借款','TRUE',NULL,NULL,11,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(72,'auto_reloan','自动复借','auto_reloan','Boolean',3,0,1,'是否系统自动生成复借单','FALSE',NULL,NULL,12,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(73,'first_term_days','首期期限','first_term_days','Integer',3,0,1,'首次借款天数或期限','14',NULL,NULL,13,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42'),(74,'due_date','应还款日期','due_date','Date',3,0,0,'应该完成还款的日期','2025/3/10',NULL,NULL,14,1,0,NULL,'2025-12-05 18:11:42','2025-12-05 18:11:42');
/*!40000 ALTER TABLE `standard_fields` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_admin_accounts`
--

DROP TABLE IF EXISTS `team_admin_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_admin_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` int NOT NULL COMMENT '所属甲方ID',
  `agency_id` int NOT NULL COMMENT '所属机构ID',
  `team_group_id` int DEFAULT NULL COMMENT '所属小组群ID（可选）',
  `team_id` int DEFAULT NULL COMMENT '所属小组ID（可选，SPV可以不关联小组）',
  `account_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号编码',
  `account_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号名称',
  `login_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录ID（唯一）',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希（BCrypt加密）',
  `role` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色：spv/team_leader/quality_inspector/statistician',
  `mobile` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `remark` text COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `last_login_at` datetime DEFAULT NULL COMMENT '最近登录时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_code` (`account_code`),
  UNIQUE KEY `login_id` (`login_id`),
  UNIQUE KEY `uk_login_id` (`login_id`),
  UNIQUE KEY `uk_account_code` (`account_code`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_agency_id` (`agency_id`),
  KEY `idx_team_group_id` (`team_group_id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_role` (`role`),
  KEY `idx_is_active` (`is_active`),
  CONSTRAINT `fk_team_admin_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组管理员账号表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_admin_accounts`
--

LOCK TABLES `team_admin_accounts` WRITE;
/*!40000 ALTER TABLE `team_admin_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_admin_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant_admins`
--

DROP TABLE IF EXISTS `tenant_admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_admins` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` int NOT NULL COMMENT '所属甲方ID',
  `account_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号编码',
  `account_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号名称（管理员姓名）',
  `login_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录ID（唯一）',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希（BCrypt加密）',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `mobile` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `last_login_at` datetime DEFAULT NULL COMMENT '最近登录时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_code` (`account_code`),
  UNIQUE KEY `login_id` (`login_id`),
  UNIQUE KEY `uk_login_id` (`login_id`),
  UNIQUE KEY `uk_account_code` (`account_code`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_is_active` (`is_active`),
  CONSTRAINT `fk_tenant_admin_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='甲方管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant_admins`
--

LOCK TABLES `tenant_admins` WRITE;
/*!40000 ALTER TABLE `tenant_admins` DISABLE KEYS */;
/*!40000 ALTER TABLE `tenant_admins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant_field_display_configs`
--

DROP TABLE IF EXISTS `tenant_field_display_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_field_display_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '所属甲方ID',
  `scene_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景类型',
  `scene_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场景名称',
  `field_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段标识',
  `field_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段名称',
  `field_data_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字段数据类型',
  `field_source` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字段来源：standard/extended/custom',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序顺序',
  `display_width` int NOT NULL DEFAULT '0' COMMENT '显示宽度（像素），0表示自动',
  `color_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'normal' COMMENT '颜色类型',
  `color_rule` json DEFAULT NULL COMMENT '颜色规则（条件表达式）',
  `hide_rule` json DEFAULT NULL COMMENT '隐藏规则',
  `hide_for_queues` json DEFAULT NULL COMMENT '对哪些队列隐藏（队列ID数组）',
  `hide_for_agencies` json DEFAULT NULL COMMENT '对哪些机构隐藏（机构ID数组）',
  `hide_for_teams` json DEFAULT NULL COMMENT '对哪些小组隐藏（小组ID数组）',
  `format_rule` json DEFAULT NULL COMMENT '格式化规则',
  `is_searchable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可搜索',
  `is_filterable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可筛选',
  `is_range_searchable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否支持范围检索',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_scene` (`tenant_id`,`scene_type`),
  KEY `idx_field_key` (`field_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='甲方字段展示配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant_field_display_configs`
--

LOCK TABLES `tenant_field_display_configs` WRITE;
/*!40000 ALTER TABLE `tenant_field_display_configs` DISABLE KEYS */;
/*!40000 ALTER TABLE `tenant_field_display_configs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenants`
--

DROP TABLE IF EXISTS `tenants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenants` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '甲方编码',
  `tenant_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '甲方名称',
  `tenant_name_en` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '甲方名称（英文）',
  `country_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家代码',
  `timezone` int DEFAULT NULL COMMENT '时区偏移量（UTC偏移，范围-12到+14）',
  `currency_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '货币代码',
  `is_active` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `created_at` datetime DEFAULT (now()),
  `updated_at` datetime DEFAULT (now()),
  `default_language` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '默认语言（Locale）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tenant_code` (`tenant_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenants`
--

LOCK TABLES `tenants` WRITE;
/*!40000 ALTER TABLE `tenants` DISABLE KEYS */;
INSERT INTO `tenants` (`id`, `tenant_code`, `tenant_name`, `tenant_name_en`, `country_code`, `timezone`, `currency_code`, `is_active`, `created_at`, `updated_at`, `default_language`) VALUES (1,'BTQ','百腾企业','Baiteng Enterprise','MX',8,'MXN',1,'2025-11-29 18:51:08','2025-12-04 17:28:23','es-MX'),(2,'BTSK','BTSK机构','BTSK Organization','CN',8,'CNY',1,'2025-11-29 18:51:08','2025-12-04 17:28:23','zh-CN'),(3,'XYQY','演示甲方','Demo Tenant','US',-5,'USD',1,'2025-11-29 18:51:08','2025-12-04 17:28:23','en-US');
/*!40000 ALTER TABLE `tenants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'cco_system'
--

--
-- Dumping routines for database 'cco_system'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-12  9:55:46
