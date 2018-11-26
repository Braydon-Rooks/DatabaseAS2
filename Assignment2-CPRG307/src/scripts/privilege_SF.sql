/* *******************************************
**  Program Name: privilege_TF
**  Author:   Daniel Romero Mancia, Braydon Rooks
**  Created:  December 1, 2017
**  Description: Check to see if the given username has the correct permissions for UTL_FILE usage.
******************************************* */

CREATE OR REPLACE FUNCTION privilege_TF
	(p_username IN VARCHAR2)

	RETURN VARCHAR2
IS
	
	v_value VARCHAR2(1) := 'N';
	v_permission VARCHAR2(7) := 'EXECUTE';
	v_checkperm VARCHAR2(25);
	v_utl VARCHAR2(8) := 'UTL_FILE';
BEGIN
	
	select PRIVILEGE Into v_checkperm
	From USER_TAB_PRIVS 
	Where TABLE_NAME = v_utl AND GRANTEE = UPPER(p_username);
	
IF v_permission = v_checkperm
THEN v_value := 'Y';
end IF;

	RETURN v_value;
	
END privilege_TF;
/