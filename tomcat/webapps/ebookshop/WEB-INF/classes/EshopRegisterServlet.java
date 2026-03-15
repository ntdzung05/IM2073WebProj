import java.io.*;
import java.sql.*;
import jakarta.servlet.*;            // Tomcat 10 (Jakarta EE 9)
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/register")
public class EshopRegisterServlet extends HttpServlet {
    
    public void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm_password = request.getParameter("confirm_password");

        if (!password.equals(confirm_password)) {
            response.sendRedirect("ebookshopregister.html?error=2");
            return;
        }

        password = SHA256Hasher.getSHA256Hash(password);

        try (
         Connection conn = DriverManager.getConnection(
               "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
               "myuser", "1234");
         Statement stmt = conn.createStatement();
        ) {
            String sqlStr = "SELECT COUNT(*) AS rowcount FROM users WHERE username = '" + username + "'";
            ResultSet rset = stmt.executeQuery(sqlStr);

            rset.next();
            int count = rset.getInt("rowcount");
            if (count > 0) {
                response.sendRedirect("ebookshopregister.html?error=1");
                return;
            }
            String role = request.getParameter("role");

            sqlStr = "INSERT INTO users VALUES ('" + username + "', '" + password + "', '" + role + "')";
            stmt.executeUpdate(sqlStr);
            response.sendRedirect("ebookshoplogin.html?success=1");
        }
        catch(SQLException ex) {
            ex.printStackTrace();
            response.sendRedirect("ebookshopregister.html?error=3");
        }
    }
}