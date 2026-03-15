import java.io.*;
import java.sql.*;
import jakarta.servlet.*;            // Tomcat 10 (Jakarta EE 9)
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.util.regex.Pattern;


@WebServlet("/eshoporder")   // Configure the request URL for this servlet (Tomcat 7/Servlet 3.0 upwards)
public class EshopOrderServlet extends HttpServlet {

   // The doGet() runs once per HTTP GET request to this servlet.
   @Override
   public void doPost(HttpServletRequest request, HttpServletResponse response)
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
         // Step 3 & 4: Execute a SQL SELECT query and Process the query result
         // Retrieve the books' id. Can order more than one books.
         String[] ids = request.getParameterValues("id");
         String cust_name = request.getParameter("cust_name");
         String cust_email = request.getParameter("cust_email");
         String cust_phone = request.getParameter("cust_phone");
         
         if (ids != null) {
            if (cust_name.isEmpty() && isValidEmail(cust_email) && cust_phone.length() == 8) {
               String sqlStr;
               int count;
   
               // Process each of the books
               for (int i = 0; i < ids.length; ++i) {
                  // Update the qty of the table books
                  sqlStr = "UPDATE books SET qty = qty - 1 WHERE id = " + ids[i];
                  out.println("<p>" + sqlStr + "</p>");  // for debugging
                  count = stmt.executeUpdate(sqlStr);
                  out.println("<p>" + count + " record updated.</p>");
   
                  // Create a transaction record
                  sqlStr = "INSERT INTO order_records (id, qty_ordered, cust_name, cust_email, cust_phone) VALUES ("
                        + ids[i] + ", 1, '" + cust_name + "', '" + cust_email + "', '" + cust_phone + "')";
                  out.println("<p>" + sqlStr + "</p>");  // for debugging
                  count = stmt.executeUpdate(sqlStr);
                  out.println("<p>" + count + " record inserted.</p>");
                  out.println("<h3>Your order for book id=" + ids[i]
                        + " has been confirmed.</h3>");
               }
               out.println("<h3>Thank you.<h3>");
            } else { //Incorrect email/phone input
               out.println("<h3>Please input your name and/or correct email and/or phone number</h3>");
            }

         } else { // No book selected
            out.println("<h3>Please go back and select a book...</h3>");
         }
         // === Step 4 ends HERE - Do NOT delete the following codes ===
      } catch(SQLException ex) {
         out.println("<p>Error: " + ex.getMessage() + "</p>");
         out.println("<p>Check Tomcat console for details.</p>");
         ex.printStackTrace();
      }  // Step 5: Close conn and stmt - Done automatically by try-with-resources (JDK 7)
 
      out.println("</body></html>");
      out.close();
   }
   
   //Pattern for correct email
   private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
   );

   //Function to check for valid email
   public static boolean isValidEmail(String email) {
      if (email == null || email.isEmpty()) return false;
      return EMAIL_PATTERN.matcher(email).matches();
   }
}