import java.io.*;
import java.sql.*;
import jakarta.servlet.*;            // Tomcat 10 (Jakarta EE 9)
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;


@WebServlet("/eshopquery")   // Configure the request URL for this servlet (Tomcat 7/Servlet 3.0 upwards)
public class EshopQueryServlet extends HttpServlet {

   // The doGet() runs once per HTTP GET request to this servlet.
   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response)
               throws ServletException, IOException {
      // Set the MIME type for the response message
      response.setContentType("text/html");
      // Get a output writer to write the response message into the network socket
      PrintWriter out = response.getWriter();
      // Print an HTML page as the output of the query
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head><title>Query Response</title></head>");
      out.println("<body>");

      try (
         // Step 1: Allocate a database 'Connection' object
         Connection conn = DriverManager.getConnection(
               "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
               "myuser", "1234");   // For MySQL
               // The format is: "jdbc:mysql://hostname:port/databaseName", "username", "password"

         // Step 2: Allocate a 'Statement' object in the Connection
         Statement stmt = conn.createStatement();
      ) {
         // Step 3: Execute a SQL SELECT query
         // === Form the SQL command - BEGIN ===
         String[] authors = request.getParameterValues("author");  // Returns an array of Strings
         String sqlStr = "SELECT * FROM books WHERE author IN (";
         for (int i = 0; i < authors.length; ++i) {
            if (i < authors.length - 1) {
               sqlStr += "'" + authors[i] + "', ";  // need a commas
            } else {
               sqlStr += "'" + authors[i] + "'";    // no commas
            }
         }
         sqlStr += ") AND qty > 0 ORDER BY author ASC, title ASC";
         // === Form the SQL command - END ===

         out.println("<h3>Thank you for your query.</h3>");
         //out.println("<p>Your SQL statement is: " + sqlStr + "</p>"); // Echo for debugging
         ResultSet rset = stmt.executeQuery(sqlStr);  // Send the query to the server

         // Step 4: Process the query result
         // Print the <form> start tag
         out.println("<form method='get' action='eshoporder'>");
         
         // For each row in ResultSet, print one checkbox inside the <form>
         String table = (" <style>\r\n" + //
                        "  table, th, td {\r\n" + //
                        "    border: 1px solid black;\r\n" + //
                        "  }\r\n" + //
                        "</style><table><tr><th></th><th>Author</th><th>Title</th><th>Price</th></tr>");
         while(rset.next()) {
            table = table + ("<tr><td><input type='checkbox' name='id' value="
                  + "'" + rset.getString("id") + "' /></td>"
                  + "<td>" + rset.getString("author") + "</td>"
                  + "<td>" + rset.getString("title") + "</td>"
                  + "<td>$" + rset.getString("price") + "</td>"
                  + "</tr>");
         }
         table = table + "</table>";
         out.println(table);

         // Print the submit button and </form> end-tag
         out.println("<p><input type='submit' value='ORDER' />");
         out.println("<button type='submit' name='action' value='add_to_cart'>Add selected to cart and choose different authors</button>");
         out.println("</form>");
         // === Step 4 ends HERE - Do NOT delete the following codes ===
      } catch(SQLException ex) {
         out.println("<p>Error: " + ex.getMessage() + "</p>");
         out.println("<p>Check Tomcat console for details.</p>");
         ex.printStackTrace();
      }  // Step 5: Close conn and stmt - Done automatically by try-with-resources (JDK 7)
 
      out.println("</body></html>");
      out.close();
   }
}