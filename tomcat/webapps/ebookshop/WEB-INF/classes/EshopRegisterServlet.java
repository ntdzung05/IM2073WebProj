import java.io.*;
import java.sql.*;
import jakarta.servlet.*;            // Tomcat 10 (Jakarta EE 9)
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/register")
public class EshopRegisterServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
                    throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm_password = request.getParameter("confirm_password");

        if (!password.equals(confirm_password)) {
            response.sendRedirect("ebookshopregister.html?error=2");
            return;
        }

        password = SHA256Hasher.getSHA256Hash(password);
        String role = request.getParameter("role");

        try (
            // Step 1: Allocate a database 'Connection' object
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    "myuser", "1234");
        ) {
            // Step 2: Check whether username already exists
            String checkSql = "SELECT COUNT(*) AS rowcount FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rset = checkStmt.executeQuery()) {
                    rset.next();
                    int count = rset.getInt("rowcount");
                    if (count > 0) {
                        response.sendRedirect("ebookshopregister.html?error=1");
                        return;
                    }
                }
            }

            // Step 3: Insert new user account
            String insertSql = "INSERT INTO users VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, role);
                insertStmt.executeUpdate();
            }
            response.sendRedirect("index.html?success=1");
        } catch(SQLException ex) {
            ex.printStackTrace();
            response.sendRedirect("ebookshopregister.html?error=3");
        }  // Step 4: Close resources - Done automatically by try-with-resources (JDK 7)
    }
}