import java.io.*;
import java.sql.*;
import jakarta.servlet.*;            // Tomcat 10 (Jakarta EE 9)
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;


@WebServlet("/eshoporder")   // Configure the request URL for this servlet (Tomcat 7/Servlet 3.0 upwards)
public class EshopOrderServlet extends HttpServlet {

   // The doGet() runs once per HTTP GET request to this servlet.
   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response)
               throws ServletException, IOException {
      String action = request.getParameter("action");
      String[] ids = request.getParameterValues("id");
      HttpSession session = request.getSession();
      // Get a output writer to write the response message into the network socket
      PrintWriter out = response.getWriter();

      //If choose add to cart
      if ("add_to_cart".equals(action)) {
         @SuppressWarnings("unchecked")
         List<Integer> cart = (List<Integer>) session.getAttribute("cart");
         if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
         }

         if (ids != null) {
            for (String id : ids) {
               try {
                  int bookId = Integer.parseInt(id);
                  cart.add(bookId);
               } catch (NumberFormatException ignore) {
               }
            }
         }

         response.sendRedirect("authorlist");
         return;
      }

      //If finished and customer ready to purchase
      if ("purchase_cart".equals(action) && (ids == null || ids.length == 0)) {
         @SuppressWarnings("unchecked")
         List<Integer> cart = (List<Integer>) session.getAttribute("cart");
         if (cart != null && !cart.isEmpty()) {
            ids = new String[cart.size()];
            for (int i = 0; i < cart.size(); ++i) {
               ids[i] = String.valueOf(cart.get(i));
            }
         }
      }

      //Retrieving customer's info
      String cust_name = request.getParameter("cust_name");
      String cust_email = request.getParameter("cust_email");
      String cust_phone = request.getParameter("cust_phone");

      //If customer did not choose a book/empty cart
      if (ids == null || ids.length == 0) {
         response.setContentType("text/html");
         out.println("<!DOCTYPE html>");
         out.println("<html>");
         out.println("<head><title>Order</title></head>");
         out.println("<body>");
         out.println("<h3>Please go back and select a book...</h3>");
         out.println("</body></html>");
         out.close();
         return;
      }

      //Initial Boxes for customer's info
      if (cust_name == null || cust_email == null || cust_phone == null) {
            response.setContentType("text/html");
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Customer Details</title></head>");
            out.println("<body>");
            out.println("<h3>Please enter your details to confirm order</h3>");
            out.println("<form method='get' action='eshoporder'>");
            if (action != null) {
               out.println("<input type='hidden' name='action' value='" + action + "' />");
            }
            for (String id : ids) {
               out.println("<input type='hidden' name='id' value='" + id + "' />");
            }
            out.println("<p>Enter your Name: <input type='text' name='cust_name' required /></p>");
            out.println("<p>Enter your Email: <input type='text' placeholder='abcd@domain.com' name='cust_email' required /></p>");
            out.println("<p>Enter your Phone Number: <input type='text' placeholder='12345678' name='cust_phone' required /></p>");
            out.println("<p><input type='submit' value='CONFIRM ORDER' /></p>");
            out.println("</form>");
            out.println("</body></html>");
            out.close();
            return;
      }

      //Incorrect email/phone
      if (!isValidEmail(cust_email) || cust_phone.length() != 8) {
         out.println("<h3>Please return and correct your email and/or phone number</h3>");
      }

      // Set the MIME type for the response message
      response.setContentType("text/html");
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
         //If all the information are filled correctly
         if (cust_name != null && isValidEmail(cust_email) && cust_phone.length() == 8){
            String sqlStr;
            int count;

            // Process each of the books
            for (int i = 0; i < ids.length; ++i) {
               // Update the qty of the table books
               sqlStr = "UPDATE books SET qty = qty - 1 WHERE id = " + ids[i];
               //out.println("<p>" + sqlStr + "</p>");  // for debugging
               count = stmt.executeUpdate(sqlStr);
               //out.println("<p>" + count + " record updated.</p>");

               // Create a transaction record
               sqlStr = "INSERT INTO order_records (id, qty_ordered, cust_name, cust_email, cust_phone) VALUES ("
                     + ids[i] + ", 1, '" + cust_name + "', '" + cust_email + "', '" + cust_phone + "')";
               //out.println("<p>" + sqlStr + "</p>");  // for debugging
               count = stmt.executeUpdate(sqlStr);
               //out.println("<p>" + count + " record inserted.</p>");
               out.println("<h3>Your order for book id=" + ids[i]
                     + " has been confirmed.</h3>");
            }

            if ("purchase_cart".equals(action)) {
               @SuppressWarnings("unchecked")
               List<Integer> cart = (List<Integer>) session.getAttribute("cart");
               if (cart != null) {
                  cart.clear();
               }
            }
            out.println("<h3>Thank you.<h3>");
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