import java.io.*;
import java.sql.*;
import jakarta.servlet.*;            // Tomcat 10 (Jakarta EE 9)
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/login")
public class EshopLoginServlet extends HttpServlet {
    
    public void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {
        response.setContentType("text/html");


        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        password = SHA256Hasher.getSHA256Hash(password);

        try (
         Connection conn = DriverManager.getConnection(
               "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
               "myuser", "1234");
         Statement stmt = conn.createStatement();
        ) {
            String sqlStr = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
            ResultSet rset = stmt.executeQuery(sqlStr);
            if (rset.next()) {
                HttpSession session = request.getSession();
                session.setAttribute("username", username);
                response.sendRedirect("eshopquery.html");
            } else {
                response.sendRedirect("ebookshoplogin.html?error=1");
            }
        } catch(SQLException ex) {
         ex.printStackTrace();
         response.sendRedirect("ebookshoplogin.html?error=2");
        }
    }
}