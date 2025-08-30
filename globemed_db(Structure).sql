/*
 Navicat Premium Dump SQL

 Source Server         : mysql_3306
 Source Server Type    : MySQL
 Source Server Version : 90300 (9.3.0)
 Source Host           : localhost:3306
 Source Schema         : globemed_db

 Target Server Type    : MySQL
 Target Server Version : 90300 (9.3.0)
 File Encoding         : 65001

 Date: 30/08/2025 20:32:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for appointments
-- ----------------------------
DROP TABLE IF EXISTS `appointments`;
CREATE TABLE `appointments` (
  `appointment_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` varchar(50) NOT NULL,
  `doctor_id` varchar(50) NOT NULL,
  `appointment_datetime` datetime NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'Scheduled',
  `doctor_notes` text,
  PRIMARY KEY (`appointment_id`),
  KEY `patient_id` (`patient_id`),
  KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`),
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for billing
-- ----------------------------
DROP TABLE IF EXISTS `billing`;
CREATE TABLE `billing` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` varchar(50) NOT NULL,
  `service_description` varchar(255) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `insurance_policy_number` varchar(100) DEFAULT NULL,
  `status` varchar(100) DEFAULT 'New',
  `processing_log` text,
  `final_amount` decimal(10,2) DEFAULT NULL,
  `billed_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `amount_paid` decimal(10,2) NOT NULL DEFAULT '0.00',
  `insurance_paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`bill_id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `billing_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for doctors
-- ----------------------------
DROP TABLE IF EXISTS `doctors`;
CREATE TABLE `doctors` (
  `doctor_id` varchar(50) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `specialty` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for insurance_plans
-- ----------------------------
DROP TABLE IF EXISTS `insurance_plans`;
CREATE TABLE `insurance_plans` (
  `plan_id` int NOT NULL AUTO_INCREMENT,
  `plan_name` varchar(100) NOT NULL,
  `coverage_percent` decimal(5,2) NOT NULL,
  PRIMARY KEY (`plan_id`),
  UNIQUE KEY `plan_name` (`plan_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for patients
-- ----------------------------
DROP TABLE IF EXISTS `patients`;
CREATE TABLE `patients` (
  `patient_id` varchar(50) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `medical_history` text,
  `treatment_plans` text,
  `insurance_plan_id` int DEFAULT NULL,
  PRIMARY KEY (`patient_id`),
  KEY `insurance_plan_id` (`insurance_plan_id`),
  CONSTRAINT `patients_ibfk_1` FOREIGN KEY (`insurance_plan_id`) REFERENCES `insurance_plans` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Table structure for staff
-- ----------------------------
DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff` (
  `staff_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  `doctor_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `fk_staff_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3;

SET FOREIGN_KEY_CHECKS = 1;
