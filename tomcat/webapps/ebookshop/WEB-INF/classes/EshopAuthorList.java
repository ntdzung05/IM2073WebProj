import java.io.*;
import java.sql.*;
import jakarta.servlet.*;            // Tomcat 10 (Jakarta EE 9)
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;


@WebServlet("/authorlist")   // Configure the request URL for this servlet (Tomcat 7/Servlet 3.0 upwards)
public class EshopAuthorList extends HttpServlet {

   // The doGet() runs once per HTTP GET request to this servlet.
   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response)
               throws ServletException, IOException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head><title>E-Bookshop</title></head>");
      out.println("<body>");
      out.println("<h2>Yet Another e-Bookshop</h2>");
      out.println("<form method='get' action='eshopquery'>");
      out.println("Choose an author:<br /><br />");
      try (
         Connection conn = DriverManager.getConnection(
               "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
               "myuser", "1234");
         Statement stmt = conn.createStatement();
      ) {
         String sqlStr = "SELECT DISTINCT author FROM books where qty > 0";
         ResultSet rset = stmt.executeQuery(sqlStr);
         while (rset.next()) {
            String author = rset.getString("author");
            out.println("<input type='checkbox' name='author' value='" + author + "' />" + author + "<br />");
         }
         out.println("<br /><input type='submit' value='Search' />");
         out.println("</form>");
         
      } catch(SQLException ex) {
         out.println("<p>Error: " + ex.getMessage() + "</p>");
         out.println("<p>Check Tomcat console for details.</p>");
         ex.printStackTrace();
      }  
 
      out.println("</body></html>");
      out.close();
   }
}