package ch.unibas.dmi.dbis.fds._2pc;

import com.sun.tools.javac.util.Assert;
import org.junit.jupiter.api.*;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import javax.transaction.xa.XAException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Check the XA stuff here --> https://docs.oracle.com/en/database/oracle/oracle-database/21/jjdbc/distributed-transactions.html
 *
 * @author Alexander Stiemer (alexander.stiemer at unibas.ch)
 */
public class XaBankingAppTest {

    private static final Logger LOG = Logger.getLogger( XaBankingAppTest.class.getName() );

    private static final String DBMS_USERNAME = "db_19";
    private static final String DBMS_PASSWORD = "8H5JNVEg";


    static {
        if ( DBMS_USERNAME.equalsIgnoreCase( "Your username" ) ) {
            throw new IllegalArgumentException( "Please set your username properly." );
        }
        if ( DBMS_PASSWORD.equalsIgnoreCase( "Your password" ) ) {
            throw new IllegalArgumentException( "Please set your password properly." );
        }
    }


    public static void main( String[] args ) {
        final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().selectors(selectClass(XaBankingAppTest.class)).build();
        final Launcher launcher = LauncherFactory.create();
        final SummaryGeneratingListener listener = new SummaryGeneratingListener();

        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        List<TestExecutionSummary.Failure> failures = summary.getFailures();
        System.out.println("getTestsSucceededCount() - " + summary.getTestsSucceededCount());
        failures.forEach(failure -> System.out.println("failure - " + failure.getException()));
    }


    private enum Bank {
        V10( "v10", "jdbc:oracle:thin:@dmi-dbis-v10.dmi.unibas.ch:1521/XEPDB1", DBMS_USERNAME, DBMS_PASSWORD ),
        V11( "v11", "jdbc:oracle:thin:@dmi-dbis-v11.dmi.unibas.ch:1521/XEPDB1", DBMS_USERNAME, DBMS_PASSWORD );

        public final transient AbstractOracleXaBank bank;


        Bank( final String BIC, final String jdbcConnectionString, final String dbmsUsername, final String dbmsPassword ) {
            try {
                this.bank = new OracleXaBank( BIC, jdbcConnectionString, dbmsUsername, dbmsPassword );
            } catch ( SQLException ex ) {
                throw new InternalError( "Exception while creating the bank object.", ex );
            }
        }
    }


    @BeforeAll
    public static void initClass() throws Exception {
    }


    @AfterAll
    public static void cleanupClass() {
        for ( Bank b : Bank.values() ) {
            b.bank.closeConnection();
        }
    }


    @BeforeEach
    public void beforeTest() throws SQLException {
        resetAccountBalances();
        System.out.printf( "\n-----------------------------------------\n" );
    }


    private void resetAccountBalances() throws SQLException {
        for ( Bank b : Bank.values() ) {
            try ( Connection c = DriverManager.getConnection( b.bank.jdbcConnectionString, DBMS_USERNAME, DBMS_PASSWORD ) ) {
                c.setAutoCommit( false );

                Statement statement = c.createStatement();
                statement.executeUpdate( "UPDATE account SET balance = 8000 WHERE iban = 'CH5367B1'" );
                statement.executeUpdate( "UPDATE account SET balance = 15000 WHERE iban = 'CH5367B2'" );
                statement.executeUpdate( "UPDATE account SET balance = 5000 WHERE iban = 'CH5367B3'" );
                statement.executeUpdate( "UPDATE account SET balance = 1700 WHERE iban = 'CH5367B4'" );
                statement.executeUpdate( "UPDATE account SET balance = 2345 WHERE iban = 'CH5367B5'" );
                c.commit();
            }
        }
    }


    @AfterEach
    public void afterTest() {
        System.out.printf( "-----------------------------------------\n" );
    }


    private void printTestDescription( final String testName, final String ibanFrom, final String bicFrom, final String ibanTo, final String bicTo, final float transferValue ) {
        System.out.printf( "%s\n", testName );
        System.out.printf( "Move %1.2f from %s on %s to %s on %s.\n", transferValue, ibanFrom, bicFrom, ibanTo, bicTo );
    }


    private void printBalance( final boolean before, final String iban, final String bic, final float balance ) {
        System.out.printf( ("Account balance for %s at bank %s " + (before ? "BEFORE" : "AFTER") + " the transaction: %1.2f\n"), iban, bic, balance );
    }


    @Test
    public void transfer() throws SQLException {
        final String ibanFrom = "CH5367B1", bicFrom = Bank.V10.name();

        final AbstractOracleXaBank FROM_BANK = Bank.valueOf( bicFrom ).bank;
        final String ibanTo = "CH5367B1", bicTo = Bank.V11.name();
        final AbstractOracleXaBank TO_BANK = Bank.V11.bank;
        final float transferValue = 100.5f;
        final float expectedBalanceFrom = FROM_BANK.getBalance( ibanFrom ) - transferValue;
        final float expectedBalanceTo = TO_BANK.getBalance( ibanTo ) + transferValue;

        assertFalse( Float.isNaN( expectedBalanceFrom ) );
        assertFalse( Float.isNaN( expectedBalanceTo ) );

        //
        printTestDescription( "Transfer", ibanFrom, bicFrom, ibanTo, bicTo, transferValue );
        printBalance( true, ibanFrom, bicFrom, FROM_BANK.getBalance( ibanFrom ) );
        printBalance( true, ibanTo, bicTo, TO_BANK.getBalance( ibanTo ) );

        try {
            System.out.println( "-- executing transfer --" );
            FROM_BANK.transfer( TO_BANK, ibanFrom, ibanTo, transferValue );
        } catch (XAException e) {
            e.printStackTrace();
        } finally {
            printBalance( false, ibanFrom, bicFrom, FROM_BANK.getBalance( ibanFrom ) );
            printBalance( false, ibanTo, bicTo, TO_BANK.getBalance( ibanTo ) );

            assertEquals( expectedBalanceFrom, FROM_BANK.getBalance( ibanFrom ), Float.MIN_VALUE );
            assertEquals( expectedBalanceTo, TO_BANK.getBalance( ibanTo ), Float.MIN_VALUE );
        }
    }


    @Test
    public void getBalanceWrongAccount() throws SQLException {
        final String ibanFrom = "CH5367B11", bicFrom = Bank.V10.name();
        final AbstractOracleXaBank FROM_BANK = Bank.valueOf( bicFrom ).bank;
        String strMessage = "";

        try {
            System.out.println( "-- executing getBalanceWrongAccount --" );
            final float get_balance = FROM_BANK.getBalance( ibanFrom );
            assertEquals(-1, get_balance);
            System.out.println("Test succeeded because getBalance returned -1 which means this account does not exist");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }

    }


    @Test
    public void transferWithdrawAccountDoesNotExist() throws SQLException {
        //to demonstrate that when it executes transfer --> it rollbacks, because withdraw account does not exist.
        //see the print in the console
        final String ibanFrom = "CH5367B11", bicFrom = Bank.V10.name();
        final AbstractOracleXaBank FROM_BANK = Bank.valueOf( bicFrom ).bank;
        final String ibanTo = "CH5367B1", bicTo = Bank.V11.name();
        final AbstractOracleXaBank TO_BANK = Bank.V11.bank;
        final float transferValue = 100.5f;

        final float expectedBalanceTo = TO_BANK.getBalance( ibanTo ) ;

        assertFalse( Float.isNaN( expectedBalanceTo ) );

        printTestDescription( "Transfer", ibanFrom, bicFrom, ibanTo, bicTo, transferValue );
        printBalance( true, ibanTo, bicTo, TO_BANK.getBalance( ibanTo ) );


        try {
            System.out.println( "-- executing transferWithdrawAccountDoesNotExist --" );
            FROM_BANK.transfer( TO_BANK, ibanFrom, ibanTo, transferValue );
        } catch (XAException e) {
            e.printStackTrace();
        } finally {
            printBalance( false, ibanTo, bicTo, TO_BANK.getBalance( ibanTo ) );
            assertEquals( expectedBalanceTo, TO_BANK.getBalance( ibanTo ), Float.MIN_VALUE );
        }
    }


    @Test
    public void transferWithdrawNotEnoughMoney() throws SQLException {
        //to demonstrate that when it executes transfer --> it rollbacks, because the balance would be negative.
        //see the print in the console
        final String ibanFrom = "CH5367B1", bicFrom = Bank.V10.name();
        final AbstractOracleXaBank FROM_BANK = Bank.valueOf( bicFrom ).bank;
        final String ibanTo = "CH5367B1", bicTo = Bank.V11.name();
        final AbstractOracleXaBank TO_BANK = Bank.V11.bank;
        final float transferValue = 8000f;
        final float expectedBalanceFrom = FROM_BANK.getBalance( ibanFrom ) ;
        final float expectedBalanceTo = TO_BANK.getBalance( ibanTo ) ;

        assertFalse( Float.isNaN( expectedBalanceFrom ) );
        assertFalse( Float.isNaN( expectedBalanceTo ) );

        printTestDescription( "Transfer", ibanFrom, bicFrom, ibanTo, bicTo, transferValue );
        printBalance( true, ibanFrom, bicFrom, FROM_BANK.getBalance( ibanFrom ) );
        printBalance( true, ibanTo, bicTo, TO_BANK.getBalance( ibanTo ) );

        try {
            System.out.println( "-- executing transferWithdrawNotEnoughMoney --" );
            FROM_BANK.transfer( TO_BANK, ibanFrom, ibanTo, transferValue );
        } catch (XAException e) {
            e.printStackTrace();
        } finally {
            printBalance( false, ibanFrom, bicFrom, FROM_BANK.getBalance( ibanFrom ) );
            printBalance( false, ibanTo, bicTo, TO_BANK.getBalance( ibanTo ) );

            assertEquals( expectedBalanceFrom, FROM_BANK.getBalance( ibanFrom ), Float.MIN_VALUE );
            assertEquals( expectedBalanceTo, TO_BANK.getBalance( ibanTo ), Float.MIN_VALUE );

        }
    }


    @Test
    public void transferWithdrawUnknownAccountAtDeposit() throws SQLException {
        //to demonstrate that when it executes transfer --> it rollbacks, because unknown account during withdraw.
        //see the print in the console
        final String ibanFrom = "CH5367B1", bicFrom = Bank.V10.name();
        final AbstractOracleXaBank FROM_BANK = Bank.valueOf( bicFrom ).bank;
        final String ibanTo = "CH5367B11", bicTo = Bank.V11.name();
        final AbstractOracleXaBank TO_BANK = Bank.V11.bank;
        final float transferValue = 100.0f;

        final float expectedBalanceFrom = FROM_BANK.getBalance( ibanFrom ) ;

        assertFalse( Float.isNaN( expectedBalanceFrom ) );

        printTestDescription( "Transfer", ibanFrom, bicFrom, ibanTo, bicTo, transferValue );
        printBalance( true, ibanFrom, bicFrom, FROM_BANK.getBalance( ibanFrom ) );

        try {
            System.out.println( "-- executing transferWithdrawUnknownAccountAtDeposit --" );
            FROM_BANK.transfer( TO_BANK, ibanFrom, ibanTo, transferValue );
        } catch (XAException e) {
            e.printStackTrace();
        } finally {
            printBalance( false, ibanFrom, bicFrom, FROM_BANK.getBalance( ibanFrom ) );
            assertEquals( expectedBalanceFrom, FROM_BANK.getBalance( ibanFrom ), Float.MIN_VALUE );
        }
    }


    @Test
    public void transfer_negative_value() throws SQLException {
        final String ibanFrom = "CH5367B1", bicFrom = Bank.V10.name();
        final AbstractOracleXaBank FROM_BANK = Bank.valueOf( bicFrom ).bank;
        final String ibanTo = "CH5367B1", bicTo = Bank.V11.name();
        final AbstractOracleXaBank TO_BANK = Bank.V11.bank;
        final float transferValue = -100.5f;
        final float expectedBalanceFrom = FROM_BANK.getBalance( ibanFrom ) ;
        final float expectedBalanceTo = TO_BANK.getBalance( ibanTo );

        assertFalse( Float.isNaN( expectedBalanceFrom ) );
        assertFalse( Float.isNaN( expectedBalanceTo ) );

        //
        printTestDescription( "Transfer", ibanFrom, bicFrom, ibanTo, bicTo, transferValue );
        printBalance( true, ibanFrom, bicFrom, FROM_BANK.getBalance( ibanFrom ) );
        printBalance( true, ibanTo, bicTo, TO_BANK.getBalance( ibanTo ) );

        try {
            System.out.println( "-- executing transfer with negative value--" );
            FROM_BANK.transfer( TO_BANK, ibanFrom, ibanTo, transferValue );
        } catch (XAException e) {
            e.printStackTrace();
        } finally {
            printBalance( false, ibanFrom, bicFrom, FROM_BANK.getBalance( ibanFrom ) );
            printBalance( false, ibanTo, bicTo, TO_BANK.getBalance( ibanTo ) );

            assertEquals( expectedBalanceFrom, FROM_BANK.getBalance( ibanFrom ), Float.MIN_VALUE );
            assertEquals( expectedBalanceTo, TO_BANK.getBalance( ibanTo ), Float.MIN_VALUE );
        }
    }


}