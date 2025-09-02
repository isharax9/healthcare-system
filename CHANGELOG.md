# Changelog

All notable changes to the GlobeMed Healthcare Management System will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.0.0 (2025-09-01)


### ⚠ BREAKING CHANGES

* Version format changed from SNAPSHOT to semantic versioning

### Features

* implement automated releases and semantic versioning system ([ade840c](https://github.com/isharax9/healthcare-system/commit/ade840cae57bd5c3bde827a0706b8e49e1060ca6))


### Bug Fixes

* Update actions/upload-artifact from v3 to v4 in workflow ([5786ce7](https://github.com/isharax9/healthcare-system/commit/5786ce70098b7e61741f2a9f008a2269236231ab))


### Documentation

* add quick start guide for automated releases ([3dee92a](https://github.com/isharax9/healthcare-system/commit/3dee92adfd50b1e5582eed7af3759eab96d3db99))

## [Unreleased]

### Added
- Automated release and versioning system
- Semantic versioning support (v1.0.0 → v1.1.0, v1.2.0, etc.)
- Automated changelog generation
- Cross-platform build automation

### Changed
- Updated version format from SNAPSHOT to semantic versioning

### Fixed
- Improved CI/CD pipeline for reliable releases

## [1.0.0] - 2024-01-01

### Added
- Initial release of GlobeMed Healthcare Management System
- Patient management with full CRUD operations
- Doctor and staff management
- Appointment scheduling with conflict resolution
- Billing and insurance claims processing
- Medical records management
- Reporting system (financial, patient, operational reports)
- User authentication and role-based access control
- Cross-platform support (Windows, macOS, Linux)
- MySQL database integration
- Modern Swing GUI with professional look and feel

### Features
- **Authentication System**: Role-based access with Decorator Pattern
- **Patient Management**: History tracking with Memento Pattern
- **Appointment Scheduling**: Conflict resolution with Mediator Pattern
- **Insurance Processing**: Chain of Responsibility for claims
- **Report Generation**: PDF reports for analysis and printing
- **Cross-Platform Builds**: Native executables for all major platforms

### Technical
- Java 17+ requirement
- Maven build system
- MySQL 8.0+ database support
- JUnit 5 testing framework
- iText PDF generation
- Cross-platform deployment with jpackage

[Unreleased]: https://github.com/isharax9/healthcare-system/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/isharax9/healthcare-system/releases/tag/v1.0.0
