import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    public void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        password = hashPassword(password);

        if(username.equals("admin") && password.equals("1234")){

            HttpSession session = request.getSession();
            session.setAttribute("username", username);

            response.sendRedirect("dashboard");

        } else {
            response.sendRedirect("ebookshoplogin.html?error=1");
        }
    }
}