/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dandm_marpg_server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dajne Win
 */
public class MySQLDatabase {
    
    private String dburl = "jdbc:mysql://wintech.net.nz:3306/marpg"; //database url in the form jdbc:mysql://databaseurl:databaseport/databasename
    private String dbuser = "marpgdbuser"; //database username, unencrypted
    private String dbpassword = "sqLxqnQbZyhxyamz"; //database password, unencrypted
    
    /**
     * Attempts to log a player in checking username and password against the database
     * @param String username
     * @param String password
     * @return true if logging in for that player was successful
     */
    public int loginMySQL(String username, String password)
    {
        int success = 0;
        Connection con = null;
        Connection con2 = null;
        Statement st = null;
        Statement st2 = null;
        ResultSet rs = null;
        try {
            con = DriverManager.getConnection(dburl, dbuser, dbpassword);
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                String user_name = rs.getString("username");
                if (username.toLowerCase().equals(user_name))
                {
                    if (rs.getInt("loggedIn") == 1) {
                        success = 97; //97 already logged in

                    } else {
                        System.out.println("Username Found in Database!");
                        String user_password = rs.getString("password");
                        if (checkHash(password, user_password)) {
                            success = 99; //99 means pass correct
                        } else {
                            success = 98; //98 means pass incorrect
                        }
                        
                        if (success == 99) { //99 means pass correct
                            System.out.println("Password is correct!");
                        } else if ( success == 98) { //98 means pass incorrect
                            System.out.println("Password is incorrect!");
                        } else if (success == 97) { //97 already logged in
                            System.out.println("User is already logged in!");
                        }
                    }

                }
            }
            if (success == 99)
            {
                con2 = DriverManager.getConnection(dburl, dbuser, dbpassword);
                st2 = con.createStatement();
                st2.executeUpdate("UPDATE users SET loggedIn=1 WHERE username='" + username + "'");
            }

        } catch (SQLException ex) {
            Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (con2 != null) {
                    con2.close();
                }
                if (st2 != null) {
                    st2.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }
    
    public boolean logoutUser(String username)
    {
        boolean success = false;
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = DriverManager.getConnection(dburl, dbuser, dbpassword);
            st = con.createStatement();
            st.executeUpdate("UPDATE users SET loggedIn=0 WHERE username='" + username.toLowerCase() + "'");
            success = true;
        } catch (SQLException ex) {
            Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }
    
    public boolean resetIsLoggedIn()
    {
        boolean success = false;
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = DriverManager.getConnection(dburl, dbuser, dbpassword);
            st = con.createStatement();
            st.executeUpdate("UPDATE users SET loggedIn=0 WHERE loggedIn=1");
            success = true;
        } catch (SQLException ex) {
            Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }
    
    /**
     * Checks if a player is already registered in the database
     * @param String username
     * @return true if the user is already registered in the database
     */
    public boolean alreadyRegistered(String username)
    {
        boolean success = false;
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = DriverManager.getConnection(dburl, dbuser, dbpassword);
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                String user_name = rs.getString("username");
                if (username.toLowerCase().equals(user_name))
                {
                    success = true;
                    System.out.println("Username is Registered!");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }
    
    /**
     * Attempts to register a new user in the database
     * @param String username
     * @param String password
     * @return true if a player is successfully registered in the database
     */
    public boolean registerMySQL(String username, String password)
    {
        boolean success = false;
        
        if (!alreadyRegistered(username) && username.length() > 3 && password.length() > 5)
        {
            String passwordHash = sha512Hash(password);
            Connection con = null;
            try 
            {    
                con = DriverManager.getConnection(dburl, dbuser, dbpassword);
                PreparedStatement ps = con.prepareStatement("INSERT INTO `" + "users" + "` (`username`, `password`, `maxHealth`, `currentHealth`, `loggedIn`)  VALUES (?, ?, ?, ?, ?)", 1);
                ps.setString(1, username.toLowerCase());
                ps.setString(2, passwordHash);
                ps.setInt(3, 100);
                ps.setInt(4, 100);
                ps.setInt(5, 0);
                ps.executeUpdate();
                ps.close();
                success = true;
            } catch (SQLException ex) {
            Logger.getLogger(MySQLDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return success;
    }
    
    private String sha512Hash(String stringToHash)
    {
        String output = "";
        StringBuilder sha512Hash = new StringBuilder();
        MessageDigest md = null;
        try {

            md = MessageDigest.getInstance("SHA-256");
            md.update(stringToHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] sha512ByteHash = md.digest();
        for (int i = 0; i < sha512ByteHash.length; i++) {
            if ((0xff & sha512ByteHash[i]) < 0x10) {
                sha512Hash.append("0").append(Integer.toHexString((0xFF & sha512ByteHash[i])));
            } else {
                sha512Hash.append(Integer.toHexString(0xFF & sha512ByteHash[i]));
            }
        }
        output = sha512Hash.toString();
        return output;
    }
    
    /**
     * Checks if two password hashes are the same
     * @param String inputPassword
     * @param String checkAgainstPassword
     * @return true if both hashes/passwords match
     */
    private boolean checkHash(String inputPassword, String checkAgainstPassword)
    {
        if (sha512Hash(inputPassword).contentEquals(checkAgainstPassword))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
}
