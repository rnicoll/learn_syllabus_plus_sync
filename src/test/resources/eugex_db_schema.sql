
CREATE TABLE EUGEX_VLE_COURSES (
    VCL1_COURSE_CODE	VARCHAR(12) NOT NULL,
    VCL2_COURSE_OCCURENCE	VARCHAR(6) NOT NULL,
    VCL3_COURSE_YEAR_CODE	VARCHAR(12) NOT NULL,
    VCL4_COURSE_PERIOD	VARCHAR(6) NOT NULL,
    VCL5_COURSE_TITLE	VARCHAR(120),
    VCL6_SUBJECT_CODE	VARCHAR(50),
    VCL7_NORMAL_YEAR_CODE	VARCHAR(15),
    VCL8_SCHOOL_ID	VARCHAR(12),
    VCL9_COURSE_LEVEL	VARCHAR(6),
    VCL10_COURSE_YEAR_CODE	VARCHAR(45),
    VCL11_COURSE_ORG_REF	VARCHAR(20),
    VCL12_COURSE_SEC_REF	VARCHAR(20),
    VCL13_WEBCT_ACTIVE	VARCHAR(1),
    PRIMARY KEY(VCL1_COURSE_CODE, VCL2_COURSE_OCCURENCE, VCL3_COURSE_YEAR_CODE, VCL4_COURSE_PERIOD)
);

CREATE VIEW EUGEX_VLE_COURSES_VW AS (SELECT * FROM EUGEX_VLE_COURSES);
