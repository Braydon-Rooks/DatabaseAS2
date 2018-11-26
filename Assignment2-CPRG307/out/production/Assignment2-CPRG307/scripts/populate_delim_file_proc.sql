CREATE OR REPLACE PROCEDURE export_file_proc
  (p_dir IN VARCHAR2,
  p_filename IN VARCHAR2)
  IS

  k_comma CONSTANT VARCHAR2(1) := ',';

  file1 utl_file.file_type;

  CURSOR c_new_trans IS
    SELECT *
      FROM NEW_TRANSACTIONS;

  BEGIN
    file1 := utl_file.fopen(p_dir, p_filename, 'w', 32767);

    FOR r_new_trans IN c_new_trans LOOP
      utl_file.put(file1,  r_new_trans.TRANSACTION_NO || k_comma);
      utl_file.put(file1,  r_new_trans.TRANSACTION_DATE|| k_comma);
      utl_file.put(file1,  r_new_trans.DESCRIPTION || k_comma);
      utl_file.put(file1,  r_new_trans.ACCOUNT_NO|| k_comma);
      utl_file.put(file1,  r_new_trans.TRANSACTION_DATE || k_comma);
      utl_file.put(file1,  r_new_trans.TRANSACTION_AMOUNT);
      utl_file.new_line(file1, 1);
    END LOOP;

    utl_file.fclose(file1);
  END;
  /