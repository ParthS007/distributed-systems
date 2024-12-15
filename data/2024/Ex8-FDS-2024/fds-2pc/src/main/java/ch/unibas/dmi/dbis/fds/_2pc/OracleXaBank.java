package ch.unibas.dmi.dbis.fds._2pc;


import oracle.jdbc.xa.OracleXAException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;//dv
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Check the XA stuff here --> https://docs.oracle.com/cd/B14117_01/java.101/b10979/xadistra.htm
 *
 * @author Alexander Stiemer (alexander.stiemer at unibas.ch)
 */
public class OracleXaBank extends AbstractOracleXaBank {


    public OracleXaBank( final String BIC, final String jdbcConnectionString, final String dbmsUsername, final String dbmsPassword ) throws SQLException {
        super( BIC, jdbcConnectionString, dbmsUsername, dbmsPassword );
    }


    @Override
    public float getBalance( final String iban ) throws SQLException {
        float balance = Float.NaN;
        Statement statement = this.getXaConnection().getConnection().createStatement();
        String query = "select balance from account where iban = '" + iban+ "'";
        try{
            statement = this.getXaConnection().getConnection().createStatement();
            ResultSet rs = statement.executeQuery(query);
            if(rs.next())
            {
                balance = rs.getFloat(1);
            }
            else{throw new SQLException("This account does not exist");}
        }
        catch (SQLException sql)
        {
            sql.getMessage();
            balance=-1;
        }
        finally {
            return balance;
            }
        // TODO: your turn ;-)
    }



    public void withdraw(final AbstractOracleXaBank FROM_BANK, final String ibanFrom, final float value, Xid xid) throws  SQLException, XAException{
        String strMessage = "success";
        try {
            if (value < 0) {
                strMessage="Negative value";
                throw new XAException(strMessage);
            }
            float balance;
            float new_balance;
            String query_w1 = "select balance from account where iban='" + ibanFrom + "'";
            String query_w2;

            Statement withdraw_s;
            withdraw_s = this.getXaConnection().getConnection().createStatement();
            ResultSet rw = withdraw_s.executeQuery(query_w1);
            if (rw.next()) {
                balance = rw.getFloat(1);
                new_balance = balance - value;
                if(new_balance<0)
                {   strMessage= "Not enough money into account";
                    throw new XAException(strMessage);
                }
                query_w2 = "UPDATE account SET balance = " + new_balance + " WHERE iban = '" + ibanFrom + "' ";
                int rowAffected = withdraw_s.executeUpdate(query_w2);
                if(rowAffected != 1)
                {
                    throw new XAException();
                }
                withdraw_s.close();
                this.endTransaction(xid, false);
            }
            else{
                strMessage = "This acc does not exist to withdraw";
                throw new XAException(strMessage);
            }
        }
        catch(XAException xae){
            this.endTransaction(xid,true);
        }
        catch (SQLException sql)
        {
            sql.printStackTrace();
        }
        finally {
//            return strMessage;
        }
    }


    public void deposit(final AbstractOracleXaBank TO_BANK, final String ibanTo, final float value, Xid xid) throws  SQLException, XAException{
        float deposit_balance;
        float new_deposit_balance;
        Statement deposit_s;
        String sel_dep_query = "select balance from account where iban='" + ibanTo + "'";
        String upd_dep_query;
        try{
            if (value < 0) {
                throw new XAException();
            }
            deposit_s = TO_BANK.getXaConnection().getConnection().createStatement();
            ResultSet dep_sel_res = deposit_s.executeQuery(sel_dep_query);
            if (dep_sel_res.next()) {
                deposit_balance = dep_sel_res.getFloat(1);
                new_deposit_balance = deposit_balance + value;
                upd_dep_query = "UPDATE account SET balance = " + new_deposit_balance + " WHERE iban = '" + ibanTo + "'";
                int rowAffected = deposit_s.executeUpdate(upd_dep_query);
                if(rowAffected != 1)
                {
                    throw new XAException();
                }
                deposit_s.close();
                TO_BANK.endTransaction(xid, false);
            }
            else{
                throw new XAException();
            }

        }
        catch (XAException xae)
        {
            TO_BANK.endTransaction(xid, true);
        }
        catch (SQLException sql)
        {
            sql.printStackTrace();
        }
        finally {
        }

    }


    @Override
    public void transfer( final AbstractOracleXaBank TO_BANK, final String ibanFrom, final String ibanTo, final float value ) throws SQLException, XAException {
        // TODO: your turn ;-))
        Xid xid =  this.startTransaction();
        Xid xid_withdraw = this.startTransaction(xid);;
        OracleXaBank to_bank = (OracleXaBank) TO_BANK;
        Xid xid_deposit = to_bank.startTransaction(xid);
        String strMessage = "success";
        boolean rollback = false;

        try {
            withdraw(this, ibanFrom, value, xid_withdraw);

            if (TO_BANK == this) {
                deposit(this, ibanTo, value, xid_deposit);
            }
            else {
                to_bank.deposit(to_bank, ibanTo, value, xid_deposit);
            }
        }
        catch (XAException xae)
        {

        }
        finally {
            int prp3 = this.getXaConnection().getXAResource().prepare(xid_withdraw);
            int prp2 = TO_BANK.getXaConnection().getXAResource().prepare(xid_deposit);


            if (!((prp3 == this.getXaConnection().getXAResource().XA_OK)))
                rollback = true;


            if (!((prp2 == TO_BANK.getXaConnection().getXAResource().XA_OK)))
                rollback = true;

            if (!rollback) {
                this.getXaConnection().getXAResource().commit(xid_withdraw, false);
                TO_BANK.getXaConnection().getXAResource().commit(xid_deposit, false);
                System.out.println("ALL TRANSACTION COMMITED");
            } else {
                this.getXaConnection().getXAResource().rollback(xid_withdraw);
                TO_BANK.getXaConnection().getXAResource().rollback(xid_deposit);
                System.out.println("ALL TRANSACTION ROLLBACK");
            }
        }



        /* This implementation does not support the implementation of PRESUMED 2PC (Two-Phase Commit) due to its reliance on synchronous communication.
           PRESUMED 2PC requires asynchronous communication to handle scenarios where the coordinator crashes,
           and an agent needs to request the commit decision. In this context, all communication is performed synchronously.
           To enable PRESUMED 2PC, the use of queues for asynchronous message handling would be necessary.
        */
    }
}