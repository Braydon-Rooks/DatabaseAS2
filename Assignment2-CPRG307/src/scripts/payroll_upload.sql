/* *******************************************
**  Program Name: payroll_upload_trg
**  Author:   Daniel Romero Mancia, Braydon Rooks
**  Created:  December 1, 2017
**  Description: As new information is inserted into the PAYROLL_LOAD table,
**				new rows are inserted into the NEW_TRANSACTIONS table using some of the same information.
******************************************** */
CREATE OR REPLACE TRIGGER payroll_upload_trg
	
	BEFORE INSERT ON payroll_load
	FOR EACH ROW

DECLARE
	
	v_debit_account Account.account_no%TYPE := 4045;
	v_credit_account Account.account_no%TYPE := 2050;
	 
	
BEGIN

	INSERT INTO New_transactions
		values (wkis_seq.NEXTVAL,:NEW.PAYROLL_DATE,'Payroll processed',v_credit_account,'C',:NEW.amount);

		INSERT INTO New_transactions
		values (wkis_seq.NEXTVAL,:NEW.PAYROLL_DATE,'Payroll processed',v_debit_account,'D',:NEW.amount);

	:NEW.STATUS := 'G';

	EXCEPTION
		WHEN OTHERS THEN
			:NEW.STATUS := 'B';

END payroll_upload_trg;
/