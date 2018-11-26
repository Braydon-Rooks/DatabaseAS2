CREATE OR REPLACE PROCEDURE zero_out_temp IS
  k_revenue CONSTANT VARCHAR2(2) := 'RE';
  k_expense CONSTANT VARCHAR2(2) := 'EX';
  k_owners_equity CONSTANT NUMBER := 5555;

  lv_trans_type CHAR(1);
  lv_owner_trans_type CHAR(1);

  CURSOR c_accounts IS
    SELECT *
      FROM ACCOUNT WHERE ACCOUNT_TYPE_CODE = k_expense OR ACCOUNT_TYPE_CODE = k_revenue;

  BEGIN
    FOR r_account IN c_accounts LOOP
      IF r_account.ACCOUNT_TYPE_CODE = k_expense THEN
        lv_trans_type := 'C';
        lv_owner_trans_type := 'D';
      ELSE
        lv_trans_type := 'D';
        lv_owner_trans_type := 'C';
      END IF;

      INSERT INTO NEW_TRANSACTIONS
      VALUES (WKIS_SEQ.nextval, TRUNC(SYSDATE), 'Monthend roll to owners equity', r_account.ACCOUNT_NO, lv_trans_type, r_account.ACCOUNT_BALANCE);

      INSERT INTO NEW_TRANSACTIONS
      VALUES (WKIS_SEQ.currval, TRUNC(SYSDATE), 'Monthend roll to owners equity', k_owners_equity, lv_owner_trans_type, r_account.ACCOUNT_BALANCE);

    END LOOP;
  END;
  /