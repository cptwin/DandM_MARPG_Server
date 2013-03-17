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
    
    private String dburl = "jdbc:mysql://wintech.net.nz:3306/marpg";
    private String dbuser = "marpgdbuser";
    private String dbpassword = "sqLxqnQbZyhxyamz";
    
    public boolean loginMySQL(String username, String password)
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
                if (username.equals(user_name))
                {
                    System.out.println("Username Found in Database!");
                    String user_password = rs.getString("password");
                    success = checkHash(password, user_password);
                    if (success)
                    {
                        System.out.println("Password is correct!");
                    }
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
