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

 Date: 30/08/2025 15:17:12
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of appointments
-- ----------------------------
BEGIN;
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (1, 'P001', 'D001', '2025-09-15 10:00:00', 'Follow-up consultation..', 'Done', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (2, 'P001', 'D003', '2025-08-28 02:19:00', 'Bade amaruwak.', 'Done', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (3, 'P001', 'D003', '2025-08-28 03:19:00', 'dathaka case ekak', 'Done', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (9, 'p1', 'D001', '2025-08-28 05:36:00', 'd1 consaltaion', 'Done', '');
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (10, 'p3', 'D003', '2025-08-28 13:00:08', 'mata kassa wadi wela', 'Done', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (12, 'p3', 'D003', '2025-08-28 18:31:33', 'feaver', 'Scheduled', '');
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (13, 'p3', 'D003', '2025-08-28 19:32:00', 'feaver', 'Done', 'test');
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (14, 'p1', 'D004', '2025-08-28 19:45:00', 'කොන්ඩේ පැහි​ලා', 'Done', 'konde pata karmu ....');
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (16, 'p1', 'D003', '2025-08-30 08:17:00', 'nikn ane', 'Scheduled', '');
COMMIT;

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
-- Records of billing
-- ----------------------------
BEGIN;
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (1, 'P001', 'Annual Cardiology Check-up', 250.00, 'INS-XYZ-12345', 'New', NULL, NULL, '2025-08-30 13:00:38', 0.00, 0.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (17, 'p1', 'new', 100.00, 'Gold', 'Partially Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Gold (60%). Covered: $60.00\n- Final balance of $40.00 due from patient.\n', 40.00, '2025-08-30 13:00:38', 20.00, 60.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (18, 'p1', 'Channeling a Doctor', 1000.00, 'Gold', 'Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Gold (60%). Covered: $600.00\n- Final balance of $400.00 due from patient.\n', 400.00, '2025-08-30 13:00:38', 401.00, 600.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (19, 'p2', 'Channeling a lady Doctor', 1000.00, 'Bronze', 'Partially Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Bronze (40%). Covered: $400.00\n- Final balance of $600.00 due from patient.\n', 600.00, '2025-08-30 13:00:38', 50.00, 400.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (20, 'p002', 'test', 1000.00, NULL, 'Closed - Pending Patient Payment', 'Bill created.\n- Bill passed initial validation.\n- No insurance on file. Skipping claim processing.\n- Final balance of $1000.00 due from patient.\n', 1000.00, '2025-08-30 13:00:38', 0.00, 0.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (22, 'p1', 'testing', 850.00, 'Gold', 'Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Gold (60%). Covered: $510.00\n- Final balance of $340.00 due from patient.\n- Bill successfully saved to database with ID: 22\n', 340.00, '2025-08-30 13:45:48', 340.00, 510.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (23, 'p1', 'test1', 1000.00, 'Gold', 'Opened - Pending Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Gold (60%). Covered: $600.00\n- Final balance of $400.00 due from patient.\n- Bill successfully saved to database with ID: 23\n', 400.00, '2025-08-30 13:51:57', 600.00, 600.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (24, 'p1', 'yes', 850.00, 'Gold', 'Opened - Pending Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Gold (60%). Covered: $510.00\n- Final balance of $340.00 due from patient.\n- Bill successfully saved to database with ID: 24\n', 340.00, '2025-08-30 13:53:03', 510.00, 510.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (25, 'p1', 'test2', 850.00, 'Gold', 'Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $510.0 applied.\n- Insurance claim processed for policy Gold (60%). Covered: $510.00\n- Final balance of $340.00 due from patient.\n- Bill successfully saved to database with ID: 25\n', 340.00, '2025-08-30 14:11:48', 340.00, 510.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (26, 'p1', 'test3', 1000.00, 'Gold', 'Opened - Pending Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $600.0 applied.\n- Insurance claim processed for policy Gold (60%). Covered: $600.00\n- Final balance of $400.00 due from patient.\n- Bill successfully saved to database with ID: 26\n', 400.00, '2025-08-30 14:12:29', 0.00, 600.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (27, 'p1', 'test4', 1000.00, 'Gold', 'Opened - Pending Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $600.0 applied.\n- Insurance claim processed for policy Gold (60%). Covered: $600.00\n- Final balance of $400.00 due from patient.\n- Bill successfully saved to database with ID: 27\n', 400.00, '2025-08-30 14:15:37', 0.00, 600.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (28, 'p1', 'test5', 1000.00, 'Gold', 'Partially Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $600.0 applied.\n- Insurance claim processed for policy Gold (60%). Covered: $600.00\n- Final balance of $400.00 due from patient.\n- Bill successfully saved to database with ID: 28\n', 400.00, '2025-08-30 14:21:01', 150.00, 600.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (29, 'p2', 'test6', 100.00, 'Bronze', 'Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $40.0 applied.\n- Insurance claim processed for policy Bronze (40%). Covered: $40.00\n- Final balance of $60.00 due from patient.\n- Bill successfully saved to database with ID: 29\n', 60.00, '2025-08-30 14:27:36', 60.00, 40.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (30, 'p2', 'test7', 1000.00, 'Bronze', 'Opened - Pending Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $400.0 applied.\n- Insurance claim processed for policy Bronze (40%). Covered: $400.00\n- Final balance of $1000.00 due from patient.\n- Bill successfully saved to database with ID: 30\n', 1000.00, '2025-08-30 14:38:58', 0.00, 400.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (31, 'p2', 'test8', 1000.00, 'Bronze', 'Opened - Pending Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $400.0 applied.\n- Insurance claim processed for policy Bronze (40%). Covered: $400.00\n- Final balance of $1000.00 due from patient.\n- Bill successfully saved to database with ID: 31\n', 1000.00, '2025-08-30 14:47:20', 0.00, 400.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (32, 'p1', 'test8', 1000.00, 'Gold', 'Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $600.0 applied.\n- Insurance claim processed for policy Gold (60%). Covered: $600.00\n- Final balance of $1000.00 due from patient.\n- Bill successfully saved to database with ID: 32\n', 1000.00, '2025-08-30 14:49:28', 400.00, 600.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (33, 'p2', 'test9', 100.00, 'Bronze', 'Partially Paid', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $40.0 applied.\n- Insurance claim processed for policy Bronze (40%). Covered: $40.00\n- Final balance of $100.00 due from patient.\n- Bill successfully saved to database with ID: 33\n', 100.00, '2025-08-30 14:57:56', 60.00, 40.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (34, 'p2', 'test10', 100.00, 'Bronze', 'Opened - Pending Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $40.0 applied.\n- Insurance claim processed for policy Bronze (40%). Covered: $40.00\n- Final balance of $100.00 due from patient.\n- Bill successfully saved to database with ID: 34\n', 100.00, '2025-08-30 15:00:15', 0.00, 40.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`, `billed_datetime`, `amount_paid`, `insurance_paid_amount`) VALUES (35, 'p1', 'test9', 1000.00, 'Gold', 'Opened - Pending Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance payment of $600.0 applied.\n- Insurance claim processed for policy Gold (60%). Covered: $600.00\n- Final balance of $1000.00 due from patient.\n- Bill successfully saved to database with ID: 35\n', 1000.00, '2025-08-30 15:10:17', 0.00, 600.00);
COMMIT;

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
-- Records of doctors
-- ----------------------------
BEGIN;
INSERT INTO `doctors` (`doctor_id`, `full_name`, `specialty`) VALUES ('D001', 'Dr. Alice Smith', 'Cardiology');
INSERT INTO `doctors` (`doctor_id`, `full_name`, `specialty`) VALUES ('D002', 'Dr. Bob Johnson', 'Neurology');
INSERT INTO `doctors` (`doctor_id`, `full_name`, `specialty`) VALUES ('D003', 'Dr. Carol White', 'Pediatrics');
INSERT INTO `doctors` (`doctor_id`, `full_name`, `specialty`) VALUES ('D004', 'Dr. Chamodi', 'Segeoun');
INSERT INTO `doctors` (`doctor_id`, `full_name`, `specialty`) VALUES ('D005', 'Dr. Sumane', 'Dentology');
INSERT INTO `doctors` (`doctor_id`, `full_name`, `specialty`) VALUES ('D006', 'Dr. Saravathi', 'Cardiology');
COMMIT;

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
-- Records of insurance_plans
-- ----------------------------
BEGIN;
INSERT INTO `insurance_plans` (`plan_id`, `plan_name`, `coverage_percent`) VALUES (1, 'Bronze', 40.00);
INSERT INTO `insurance_plans` (`plan_id`, `plan_name`, `coverage_percent`) VALUES (2, 'Silver', 50.00);
INSERT INTO `insurance_plans` (`plan_id`, `plan_name`, `coverage_percent`) VALUES (3, 'Gold', 60.00);
INSERT INTO `insurance_plans` (`plan_id`, `plan_name`, `coverage_percent`) VALUES (4, 'Platinum', 80.00);
COMMIT;

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
-- Records of patients
-- ----------------------------
BEGIN;
INSERT INTO `patients` (`patient_id`, `full_name`, `medical_history`, `treatment_plans`, `insurance_plan_id`) VALUES ('P001', 'Ishara Lakshitha', 'Diagnosed with Type 2 Diabetes in 2020.\nAllergic to Penicillin.', 'Prescribed Metformin.\nAnnual check-up required.', 4);
INSERT INTO `patients` (`patient_id`, `full_name`, `medical_history`, `treatment_plans`, `insurance_plan_id`) VALUES ('P002', 'Tharuka Bandara', 'Dath pata kara', 'dath tika whiteing karaganna inne', NULL);
INSERT INTO `patients` (`patient_id`, `full_name`, `medical_history`, `treatment_plans`, `insurance_plan_id`) VALUES ('p1', 'tharaka', 'konde sudui', 'thel ganawa', 3);
INSERT INTO `patients` (`patient_id`, `full_name`, `medical_history`, `treatment_plans`, `insurance_plan_id`) VALUES ('p2', 'Vishaka', 'Mahathata execise karanna.', 'kanna epa wadiya ', 1);
INSERT INTO `patients` (`patient_id`, `full_name`, `medical_history`, `treatment_plans`, `insurance_plan_id`) VALUES ('p3', 'Aiya', 'data', 'data', 2);
COMMIT;

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

-- ----------------------------
-- Records of staff
-- ----------------------------
BEGIN;
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (1, 'doc', '1101', 'Doctor', 'D003');
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (2, 'nurse', '1101', 'Nurse', NULL);
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (3, 'admin', '1101', 'Admin', NULL);
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (4, 'ishara', '1101', 'Admin', NULL);
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (5, 'doc1', '1101', 'Doctor', 'D004');
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (6, 'doc2', '1101', 'Doctor', 'D006');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
