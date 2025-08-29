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

 Date: 29/08/2025 23:43:47
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of appointments
-- ----------------------------
BEGIN;
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (1, 'P001', 'D001', '2025-09-15 10:00:00', 'Follow-up consultation..', 'Done', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (2, 'P001', 'D003', '2025-08-28 02:19:00', 'Bade amaruwak.', 'Done', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (3, 'P001', 'D003', '2025-08-28 03:19:00', 'dathaka case ekak', 'Done', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (9, 'p1', 'D001', '2025-08-28 05:36:00', 'd1 consaltaion', 'Scheduled', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (10, 'p3', 'D003', '2025-08-28 13:00:08', 'mata kassa wadi wela', 'Done', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (12, 'p3', 'D003', '2025-08-28 18:31:33', 'feaver', 'Scheduled', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (13, 'p3', 'D003', '2025-08-28 19:32:00', 'feaver', 'Scheduled', NULL);
INSERT INTO `appointments` (`appointment_id`, `patient_id`, `doctor_id`, `appointment_datetime`, `reason`, `status`, `doctor_notes`) VALUES (14, 'p1', 'D004', '2025-08-28 19:45:00', 'කොන්ඩේ පැහි​ලා', 'Done', NULL);
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
  PRIMARY KEY (`bill_id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `billing_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of billing
-- ----------------------------
BEGIN;
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`) VALUES (1, 'P001', 'Annual Cardiology Check-up', 250.00, 'INS-XYZ-12345', 'New', NULL, NULL);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`) VALUES (17, 'p1', 'new', 100.00, 'Gold', 'Closed - Pending Patient Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Gold (60%). Covered: $60.00\n- Final balance of $40.00 due from patient.\n', 40.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`) VALUES (18, 'p1', 'Channeling a Doctor', 1000.00, 'Gold', 'Closed - Pending Patient Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Gold (60%). Covered: $600.00\n- Final balance of $400.00 due from patient.\n', 400.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`) VALUES (19, 'p2', 'Channeling a lady Doctor', 1000.00, 'Bronze', 'Closed - Pending Patient Payment', 'Bill created.\n- Bill passed initial validation.\n- Insurance claim processed for policy Bronze (40%). Covered: $400.00\n- Final balance of $600.00 due from patient.\n', 600.00);
INSERT INTO `billing` (`bill_id`, `patient_id`, `service_description`, `amount`, `insurance_policy_number`, `status`, `processing_log`, `final_amount`) VALUES (20, 'p002', 'test', 1000.00, NULL, 'Closed - Pending Patient Payment', 'Bill created.\n- Bill passed initial validation.\n- No insurance on file. Skipping claim processing.\n- Final balance of $1000.00 due from patient.\n', 1000.00);
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of staff
-- ----------------------------
BEGIN;
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (1, 'doc', '1101', 'Doctor', NULL);
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (2, 'nurse', '1101', 'Nurse', NULL);
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (3, 'admin', '1101', 'Admin', NULL);
INSERT INTO `staff` (`staff_id`, `username`, `password_hash`, `role`, `doctor_id`) VALUES (4, 'ishara', '1101', 'Admin', NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
