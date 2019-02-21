-- phpMyAdmin SQL Dump
-- version 4.8.3
-- https://www.phpmyadmin.net/
--
-- Host: serveur-du-placard.ml:53307:53306
-- Generation Time: Feb 21, 2019 at 10:08 AM
-- Server version: 10.2.10-MariaDB-10.2.10+maria~jessie
-- PHP Version: 7.2.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `maven_dep_usage`
--
CREATE DATABASE IF NOT EXISTS `maven_dep_usage` DEFAULT CHARACTER SET latin1 COLLATE latin1_bin;
USE `maven_dep_usage`;

DELIMITER $$
--
-- Functions
--
CREATE DEFINER=`root`@`%` FUNCTION `funcApiMemberID` (`mpackage` INT, `mclass` TEXT, `mmember` TEXT, `mlibraryid` INT) RETURNS INT(11) MODIFIES SQL DATA
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
	DECLARE r INT;
    SET @r = (SELECT id FROM api_member WHERE packageid=mpackage AND class=mclass AND member=mmember AND libraryid=mlibraryid);
    IF (@r IS NULL)
	THEN
		INSERT INTO api_member(id, packageid, class, member, libraryid)
    	VALUES(NULL, mpackage, mclass, mmember, mlibraryid);
    	SET @r = LAST_INSERT_ID();
    END IF;
    RETURN(@r);
END$$

CREATE DEFINER=`root`@`%` FUNCTION `funcClientID` (`ccoordinates` TEXT, `cgroupid` TEXT, `cartifactid` TEXT, `cversion` TEXT) RETURNS INT(11) MODIFIES SQL DATA
    DETERMINISTIC
    SQL SECURITY INVOKER
BEGIN
	DECLARE r INT;
    SET @r = (SELECT id FROM client WHERE coordinates=ccoordinates);
    IF (@r IS NULL)
    THEN
		INSERT INTO client(id, coordinates, groupid, artifactid, version)
    	VALUES (NULL, ccoordinates, cgroupid, cartifactid, cversion);
    	SET @r = LAST_INSERT_ID();
    END IF;
    RETURN(@r);
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `api_member`
--

CREATE TABLE `api_member` (
  `id` int(11) NOT NULL,
  `packageid` int(11) NOT NULL,
  `class` text COLLATE latin1_bin NOT NULL,
  `member` text COLLATE latin1_bin DEFAULT NULL,
  `libraryid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- --------------------------------------------------------

--
-- Table structure for table `api_member_full`
--

CREATE TABLE `api_member_full` (
  `id` int(11) NOT NULL,
  `package` int(11) NOT NULL,
  `class` text COLLATE latin1_bin NOT NULL,
  `member` text COLLATE latin1_bin NOT NULL,
  `isPublic` bit(1) NOT NULL,
  `isInterface` bit(1) NOT NULL,
  `isAnnotation` bit(1) NOT NULL,
  `isAbstract` bit(1) NOT NULL,
  `isStatic` bit(1) NOT NULL,
  `isField` bit(1) NOT NULL,
  `isSeen` bit(1) NOT NULL,
  `libraryid` int(11) NOT NULL,
  `apimemberid` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- --------------------------------------------------------

--
-- Table structure for table `api_usage`
--

CREATE TABLE `api_usage` (
  `clientid` int(11) NOT NULL,
  `apimemberid` int(11) NOT NULL,
  `nb` int(11) NOT NULL,
  `diversity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- --------------------------------------------------------

--
-- Table structure for table `client`
--

CREATE TABLE `client` (
  `id` int(11) NOT NULL,
  `coordinates` text COLLATE latin1_bin NOT NULL,
  `groupid` text COLLATE latin1_bin NOT NULL,
  `artifactid` text COLLATE latin1_bin NOT NULL,
  `version` text COLLATE latin1_bin NOT NULL,
  `versionInt` int(11) NOT NULL,
  `status` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- --------------------------------------------------------

--
-- Table structure for table `dependency`
--

CREATE TABLE `dependency` (
  `clientid` int(11) NOT NULL,
  `libraryid` int(11) NOT NULL,
  `intensity` int(11) DEFAULT NULL,
  `nbElement` int(11) DEFAULT NULL,
  `nbClass` int(11) DEFAULT NULL,
  `nbPackage` int(11) DEFAULT NULL,
  `diversity` int(11) DEFAULT NULL,
  `avg_diversity` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- --------------------------------------------------------

--
-- Table structure for table `library`
--

CREATE TABLE `library` (
  `id` int(11) NOT NULL,
  `coordinates` text COLLATE latin1_bin NOT NULL,
  `groupid` text COLLATE latin1_bin NOT NULL,
  `artifactid` text COLLATE latin1_bin NOT NULL,
  `version` text COLLATE latin1_bin NOT NULL,
  `versionInt` int(11) NOT NULL,
  `api_size` int(11) DEFAULT NULL,
  `api_size_seen` int(11) NOT NULL,
  `clients_the` int(11) NOT NULL,
  `clients_obs` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

-- --------------------------------------------------------

--
-- Table structure for table `package`
--

CREATE TABLE `package` (
  `id` int(11) NOT NULL,
  `libraryid` int(11) NOT NULL,
  `package` text COLLATE latin1_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `api_member`
--
ALTER TABLE `api_member`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UC_Member` (`packageid`,`class`(1000),`member`(2040)) USING BTREE,
  ADD KEY `fk_api_member_to_libraryid` (`libraryid`) USING BTREE,
  ADD KEY `packageid_key` (`packageid`),
  ADD KEY `class_key` (`class`(3072)),
  ADD KEY `member_key` (`member`(3072));

--
-- Indexes for table `api_member_full`
--
ALTER TABLE `api_member_full`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UC_pcm` (`package`,`class`(1000),`member`(2040)),
  ADD KEY `fk_api_member_full_to_libraryid` (`libraryid`),
  ADD KEY `fk_api_member_full_to_package` (`package`),
  ADD KEY `class_index` (`class`(1000)),
  ADD KEY `fk_api_member_full_to_api_member` (`apimemberid`) USING BTREE,
  ADD KEY `member_index` (`member`(2040)) USING BTREE;

--
-- Indexes for table `api_usage`
--
ALTER TABLE `api_usage`
  ADD UNIQUE KEY `UC_Unique` (`clientid`,`apimemberid`),
  ADD KEY `fk_api_usage_to_clientid` (`clientid`),
  ADD KEY `fk_api_usage_to_api_member` (`apimemberid`);

--
-- Indexes for table `client`
--
ALTER TABLE `client`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `coordinates` (`coordinates`(256));

--
-- Indexes for table `dependency`
--
ALTER TABLE `dependency`
  ADD KEY `fk_clientid` (`clientid`),
  ADD KEY `fk_libraryid` (`libraryid`);

--
-- Indexes for table `library`
--
ALTER TABLE `library`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `coordinates` (`coordinates`(256));

--
-- Indexes for table `package`
--
ALTER TABLE `package`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_package_to_librarid` (`libraryid`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `api_member`
--
ALTER TABLE `api_member`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `api_member_full`
--
ALTER TABLE `api_member_full`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `client`
--
ALTER TABLE `client`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `library`
--
ALTER TABLE `library`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `package`
--
ALTER TABLE `package`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `api_member_full`
--
ALTER TABLE `api_member_full`
  ADD CONSTRAINT `fk_api_member_full_to_libraryid` FOREIGN KEY (`libraryid`) REFERENCES `library` (`id`);

--
-- Constraints for table `dependency`
--
ALTER TABLE `dependency`
  ADD CONSTRAINT `fk_clientid` FOREIGN KEY (`clientid`) REFERENCES `client` (`id`),
  ADD CONSTRAINT `fk_libraryid` FOREIGN KEY (`libraryid`) REFERENCES `library` (`id`);

--
-- Constraints for table `package`
--
ALTER TABLE `package`
  ADD CONSTRAINT `fk_package_to_librarid` FOREIGN KEY (`libraryid`) REFERENCES `library` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
