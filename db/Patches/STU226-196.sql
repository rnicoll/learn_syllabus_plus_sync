/* Expand width of activity group name field, to handle activities with names up to 255 characters
 * (which a prefix is then applied to)
 */

ALTER TABLE ACTIVITY
  MODIFY learn_group_name NVARCHAR2(300);