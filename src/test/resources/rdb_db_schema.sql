
  CREATE TABLE ACTIVITY
   (
    ID VARCHAR(32) NOT NULL,
    NAME VARCHAR(255), 
    HOST_KEY VARCHAR(255), 
    DESCRIPTION VARCHAR(255), 
    DEPARTMENT VARCHAR(32), 
    ACTIVITY_TYPE VARCHAR(32), 
    ZONE VARCHAR(32), 
    MODUL VARCHAR(32), 
    FACTOR INTEGER, 
    DURATION INTEGER, 
    LINK_SIZE INTEGER, 
    PLANNED_SIZE INTEGER, 
    SUGGESTED_DAYS VARCHAR(4), 
    SUGGESTED_PERIOD INTEGER, 
    ACTIVITY_TMPL VARCHAR(32), 
    WILDCARD_POSTS INTEGER, 
    WILDCARD_STAFF INTEGER, 
    WILDCARD_LOCATIONS INTEGER, 
    WILDCARD_EQUIPMENT INTEGER, 
    POOLED_RESOURCES VARCHAR(2000), 
    SECTION_ID VARCHAR(255), 
    DAYS_FOR_MINIMUM INTEGER, 
    MINIMUM_TIME INTEGER, 
    MINIMUM_DAYS INTEGER, 
    DAYS_FOR_MAXIMUM INTEGER, 
    MAXIMUM_TIME INTEGER, 
    MAXIMUM_DAYS INTEGER, 
    WHO_SCHEDULED VARCHAR(127), 
    SCHEDULING_METHOD INTEGER, 
    TIME_OF_SCHEDULING INTEGER, 
    SCHEDULED_PERIODS VARCHAR(2000), 
    ALLC_POOLED_RSRC VARCHAR(1800), 
    STUDENT_SET_SKIP INTEGER, 
    STAFF_SKIP INTEGER, 
    LOCATION_SKIP INTEGER, 
    EQUIPMENT_SKIP INTEGER, 
    MODULE_SKIP INTEGER, 
    POST_SKIP INTEGER, 
    NAMED_AVAILABILITY VARCHAR(32), 
    NAMED_USAGE_PREF VARCHAR(32), 
    NAMED_STARTS_PREF VARCHAR(32), 
    DICT VARCHAR(32), 
    WEEK_PATTERN VARCHAR(65), 
    STARTS_PREFS VARCHAR(32), 
    USAGE_PREFS VARCHAR(32), 
    BASE_AVAILABILITY VARCHAR(32), 
    USER_TEXT_1 VARCHAR(2000), 
    USER_TEXT_2 VARCHAR(255), 
    USER_TEXT_3 VARCHAR(255), 
    USER_TEXT_4 VARCHAR(255), 
    USER_TEXT_5 VARCHAR(255), 
    OBSOLETEFROM INTEGER, 
    LATESTTRANSACTION INTEGER, 
    WHENSCHEDULED DATETIME, 
    PRIMARY KEY (ID)
 );
 
CREATE TABLE MODULE (
    ID VARCHAR(32) NOT NULL,
    NAME VARCHAR(255), 
    HOST_KEY VARCHAR(255), 
    DESCRIPTION VARCHAR(255), 
    DEPARTMENT VARCHAR(32), 
    LINK_SIZE INTEGER, 
    PLANNED_SIZE INTEGER, 
    CREDIT_PROVIDED INTEGER, 
    RESERVED_SIZE INTEGER, 
    NAMED_AVAILABILITY VARCHAR(32), 
    NAMED_USAGE_PREF VARCHAR(32), 
    NAMED_STARTS_PREF VARCHAR(32), 
    DICT VARCHAR(32), 
    WEEK_PATTERN VARCHAR(65), 
    STARTS_PREFS VARCHAR(32), 
    USAGE_PREFS VARCHAR(32), 
    BASE_AVAILABILITY VARCHAR(32), 
    USER_TEXT_1 VARCHAR(2000), 
    USER_TEXT_2 VARCHAR(255), 
    USER_TEXT_3 VARCHAR(255), 
    USER_TEXT_4 VARCHAR(255), 
    USER_TEXT_5 VARCHAR(255), 
    OBSOLETEFROM INTEGER, 
    LATESTTRANSACTION INTEGER, 
    WEEKPATTERNLABEL VARCHAR(255), 
     PRIMARY KEY (ID)
);
 
CREATE TABLE STUDENT_SET (
    ID VARCHAR(32) NOT NULL,
    NAME VARCHAR(255), 
    HOST_KEY VARCHAR(255), 
    DESCRIPTION VARCHAR(255), 
    DEPARTMENT VARCHAR(32), 
    NAMED_AVAILABILITY VARCHAR(32), 
    NAMED_USAGE_PREF VARCHAR(32), 
    NAMED_STARTS_PREF VARCHAR(32), 
    DICT VARCHAR(32), 
    LINK_SIZE INTEGER, 
    PLANNED_SIZE INTEGER, 
    PROG_OF_STUDY VARCHAR(32), 
    COST_BAND VARCHAR(32), 
    CONTRACT_HOURS INTEGER, 
    MAXIMUM_HOURS INTEGER, 
    FREE_HOURS_BY_WEEK VARCHAR(32), 
    PERCENT_MALE INTEGER, 
    PERCENT_FEMALE INTEGER, 
    WEEK_PATTERN VARCHAR(65), 
    STARTS_PREFS VARCHAR(32), 
    USAGE_PREFS VARCHAR(32), 
    BASE_AVAILABILITY VARCHAR(32), 
    USER_TEXT_1 VARCHAR(2000), 
    USER_TEXT_2 VARCHAR(255), 
    USER_TEXT_3 VARCHAR(255), 
    USER_TEXT_4 VARCHAR(255), 
    USER_TEXT_5 VARCHAR(255), 
    OBSOLETEFROM INTEGER, 
    LATESTTRANSACTION INTEGER, 
    PRIMARY KEY (ID)
 );
 
 
CREATE TABLE ACTIVITYTYPES (
    ID VARCHAR(32) NOT NULL,
    NAME VARCHAR(255), 
    HOST_KEY VARCHAR(255), 
    DESCRIPTION VARCHAR(255), 
    DEPARTMENT VARCHAR(32), 
    IS_CONTACT INTEGER, 
    IS_BOOKING INTEGER, 
    IS_COVER INTEGER, 
    IS_EXAMINATION INTEGER, 
    COLOR INTEGER, 
    DICT VARCHAR(32), 
    USER_TEXT_1 VARCHAR(2000), 
    USER_TEXT_2 VARCHAR(255), 
    USER_TEXT_3 VARCHAR(255), 
    USER_TEXT_4 VARCHAR(255), 
    USER_TEXT_5 VARCHAR(255), 
    OBSOLETEFROM INTEGER, 
    LATESTTRANSACTION INTEGER, 
    RED INTEGER, 
    GREEN INTEGER, 
    BLUE INTEGER, 
    PRIMARY KEY (ID)
 );

 
CREATE TABLE TEMPLATE (
    ID VARCHAR(32) NOT NULL,
    NAME VARCHAR(255), 
    HOST_KEY VARCHAR(255), 
    DESCRIPTION VARCHAR(255), 
    DEPARTMENT VARCHAR(32), 
    ACTIVITY_TYPE VARCHAR(32), 
    MODUL VARCHAR(32), 
    ZONE VARCHAR(32), 
    FACTOR INTEGER, 
    DURATION INTEGER, 
    LINK_SIZE INTEGER, 
    PLANNED_SIZE INTEGER, 
    SUGGESTED_DAYS VARCHAR(4), 
    SUGGESTED_PERIOD INTEGER, 
    WILDCARD_POSTS INTEGER, 
    WILDCARD_STAFF INTEGER, 
    WILDCARD_LOCATIONS INTEGER, 
    WILDCARD_EQUIPMENT INTEGER, 
    POOLED_RESOURCES VARCHAR(1800), 
    NAMED_AVAILABILITY VARCHAR(32), 
    NAMED_USAGE_PREF VARCHAR(32), 
    NAMED_STARTS_PREF VARCHAR(32), 
    DICT VARCHAR(32), 
    DAYS_FOR_MINIMUM INTEGER, 
    MINIMUM_TIME INTEGER, 
    MINIMUM_DAYS INTEGER, 
    DAYS_FOR_MAXIMUM INTEGER, 
    MAXIMUM_TIME INTEGER, 
    MAXIMUM_DAYS INTEGER, 
    WEEK_PATTERN VARCHAR(65), 
    STARTS_PREFS VARCHAR(32), 
    USAGE_PREFS VARCHAR(32), 
    BASE_AVAILABILITY VARCHAR(32), 
    USER_TEXT_1 VARCHAR(2000), 
    USER_TEXT_2 VARCHAR(255), 
    USER_TEXT_3 VARCHAR(255), 
    USER_TEXT_4 VARCHAR(255), 
    USER_TEXT_5 VARCHAR(255), 
    OBSOLETEFROM INTEGER, 
    LATESTTRANSACTION INTEGER, 
    WEEKPATTERNLABEL VARCHAR(255), 
    PRIMARY KEY (ID)
 );

CREATE TABLE ACTIVITIES_STUDENTSET (
    ID VARCHAR(32) NOT NULL,
    STUDENT_SET VARCHAR(32) NOT NULL,
    OBSOLETEFROM INTEGER, 
    LATESTTRANSACTION INTEGER, 
    PRIMARY KEY (ID, STUDENT_SET)
);

CREATE TABLE VARIANTJTAACTS (
    ID VARCHAR(32) NOT NULL,
    ISJTAPARENT TINYINT NOT NULL,
    ISJTACHILD TINYINT NOT NULL,
    ISVARIANTPARENT TINYINT NOT NULL,
    ISVARIANTCHILD TINYINT NOT NULL,
    LATESTTRANSACTION INTEGER
);

